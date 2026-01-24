package com.achievement.filter;

import com.achievement.config.ProcessSystemProperties;
import com.achievement.result.Result;
import com.achievement.service.ProcessSystemRateLimitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

/**
 * 过程系统API密钥验证过滤器
 *
 * @author system
 * @since 2026-01-23
 */
@Slf4j
@RequiredArgsConstructor
public class ProcessSystemApiKeyFilter implements Filter {

    private final ProcessSystemProperties processSystemProperties;
    private final ProcessSystemRateLimitService rateLimitService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 跳过OPTIONS请求
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        try {
            // 验证API密钥
            String apiKey = extractApiKey(httpRequest);
            if (!validateApiKey(httpRequest, apiKey)) {
                sendErrorResponse(httpResponse, 10002, "API密钥无效", HttpStatus.UNAUTHORIZED);
                return;
            }

            // 验证频率限制
            if (!validateRateLimit(httpRequest, httpResponse, apiKey)) {
                return; // 错误响应已在方法内发送
            }

            // 验证请求签名
            if (!validateSignature(httpRequest)) {
                sendErrorResponse(httpResponse, 10003, "签名验证失败", HttpStatus.UNAUTHORIZED);
                return;
            }

            // 验证时间戳（防重放攻击）
            if (!validateTimestamp(httpRequest)) {
                sendErrorResponse(httpResponse, 10005, "请求时间戳无效", HttpStatus.UNAUTHORIZED);
                return;
            }

            // 验证IP白名单（如果启用）
            if (!validateIpWhitelist(httpRequest)) {
                sendErrorResponse(httpResponse, 10004, "IP地址不在白名单中", HttpStatus.FORBIDDEN);
                return;
            }

            // 验证通过，继续处理请求
            chain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Process system API key filter error", e);
            sendErrorResponse(httpResponse, 10008, "系统内部错误", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 验证API密钥
     */
    private boolean validateApiKey(HttpServletRequest request, String apiKey) {
        if (StringUtils.isBlank(apiKey)) {
            log.warn("Missing API key in request");
            return false;
        }

        Map<String, String> validKeys = processSystemProperties.getApi().getKeys();
        if (validKeys == null || !validKeys.containsKey(apiKey)) {
            log.warn("Invalid API key: {}", apiKey);
            return false;
        }

        // 将API密钥信息存储到请求属性中，供后续使用
        request.setAttribute("apiKey", apiKey);
        request.setAttribute("apiKeyName", validKeys.get(apiKey));

        return true;
    }

    /**
     * 验证频率限制
     */
    private boolean validateRateLimit(HttpServletRequest request, HttpServletResponse response, String apiKey) 
            throws IOException {
        if (!rateLimitService.isAllowed(apiKey)) {
            // 添加频率限制相关的响应头
            response.setHeader("X-RateLimit-Limit", 
                    String.valueOf(processSystemProperties.getSecurity().getRateLimitPerMinute()));
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setHeader("X-RateLimit-Reset", 
                    String.valueOf(rateLimitService.getResetTime(apiKey)));
            
            sendErrorResponse(response, 10004, "请求频率超出限制", HttpStatus.TOO_MANY_REQUESTS);
            return false;
        }

        // 添加频率限制信息到响应头
        response.setHeader("X-RateLimit-Limit", 
                String.valueOf(processSystemProperties.getSecurity().getRateLimitPerMinute()));
        response.setHeader("X-RateLimit-Remaining", 
                String.valueOf(rateLimitService.getRemainingRequests(apiKey)));
        response.setHeader("X-RateLimit-Reset", 
                String.valueOf(rateLimitService.getResetTime(apiKey)));

        return true;
    }

    /**
     * 验证请求签名
     */
    private boolean validateSignature(HttpServletRequest request) {
        String signature = request.getHeader("X-Signature");
        if (StringUtils.isBlank(signature)) {
            log.warn("Missing signature in request");
            return false;
        }

        try {
            // 构建签名字符串
            String signatureString = buildSignatureString(request);
            
            // 计算期望的签名
            String expectedSignature = calculateSignature(signatureString);
            
            // 比较签名
            boolean isValid = signature.equals(expectedSignature);
            if (!isValid) {
                log.warn("Signature mismatch. Expected: {}, Actual: {}", expectedSignature, signature);
            }
            
            return isValid;
            
        } catch (Exception e) {
            log.error("Error validating signature", e);
            return false;
        }
    }

    /**
     * 验证时间戳（防重放攻击）
     */
    private boolean validateTimestamp(HttpServletRequest request) {
        String timestampStr = request.getHeader("X-Timestamp");
        if (StringUtils.isBlank(timestampStr)) {
            log.warn("Missing timestamp in request");
            return false;
        }

        try {
            long timestamp = Long.parseLong(timestampStr);
            long currentTime = Instant.now().getEpochSecond();
            long validitySeconds = processSystemProperties.getSecurity().getSignatureValiditySeconds();
            
            // 检查时间戳是否在有效范围内
            if (Math.abs(currentTime - timestamp) > validitySeconds) {
                log.warn("Request timestamp is too old or too new. Current: {}, Request: {}, Validity: {}s", 
                        currentTime, timestamp, validitySeconds);
                return false;
            }
            
            return true;
            
        } catch (NumberFormatException e) {
            log.warn("Invalid timestamp format: {}", timestampStr);
            return false;
        }
    }

    /**
     * 验证IP白名单
     */
    private boolean validateIpWhitelist(HttpServletRequest request) {
        if (!processSystemProperties.getSecurity().isEnableIpWhitelist()) {
            return true; // 未启用IP白名单，直接通过
        }

        String clientIp = getClientIpAddress(request);
        var ipWhitelist = processSystemProperties.getSecurity().getIpWhitelist();
        
        if (ipWhitelist == null || ipWhitelist.isEmpty()) {
            return true; // 白名单为空，直接通过
        }

        boolean isAllowed = ipWhitelist.contains(clientIp);
        if (!isAllowed) {
            log.warn("IP address {} not in whitelist", clientIp);
        }

        return isAllowed;
    }

    /**
     * 提取API密钥
     */
    private String extractApiKey(HttpServletRequest request) {
        // 从Authorization头中提取
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 从X-API-Key头中提取
        return request.getHeader("X-API-Key");
    }

    /**
     * 构建签名字符串
     */
    private String buildSignatureString(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append(request.getMethod()).append("\n");
        sb.append(request.getRequestURI()).append("\n");
        
        String queryString = request.getQueryString();
        if (StringUtils.isNotBlank(queryString)) {
            sb.append(queryString);
        }
        sb.append("\n");
        
        // 添加时间戳（从请求头中获取）
        String timestamp = request.getHeader("X-Timestamp");
        if (StringUtils.isNotBlank(timestamp)) {
            sb.append(timestamp);
        }
        
        return sb.toString();
    }

    /**
     * 计算签名
     */
    private String calculateSignature(String signatureString) throws NoSuchAlgorithmException, InvalidKeyException {
        String secretKey = processSystemProperties.getApi().getSignatureSecret();
        
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        
        byte[] signatureBytes = mac.doFinal(signatureString.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.isNotBlank(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, int code, String message, HttpStatus status)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Result<Object> errorResult = Result.error(code, message);
        String jsonResponse = objectMapper.writeValueAsString(errorResult);
        
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}