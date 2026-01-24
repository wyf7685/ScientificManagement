package com.achievement.service;

import com.achievement.config.ProcessSystemProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 过程系统安全验证服务
 *
 * @author system
 * @since 2026-01-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessSystemSecurityService {

    private final ProcessSystemProperties processSystemProperties;
    
    // 可疑请求模式
    private static final Pattern SUSPICIOUS_PATTERNS = Pattern.compile(
            ".*(script|javascript|vbscript|onload|onerror|eval|alert|confirm|prompt|document\\.|window\\.).*",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 验证API密钥是否有效
     *
     * @param apiKey API密钥
     * @return 是否有效
     */
    public boolean isValidApiKey(String apiKey) {
        if (StringUtils.isBlank(apiKey)) {
            return false;
        }

        Map<String, String> validKeys = processSystemProperties.getApi().getKeys();
        return validKeys != null && validKeys.containsKey(apiKey);
    }

    /**
     * 获取API密钥对应的名称
     *
     * @param apiKey API密钥
     * @return 密钥名称
     */
    public String getApiKeyName(String apiKey) {
        if (StringUtils.isBlank(apiKey)) {
            return null;
        }

        Map<String, String> validKeys = processSystemProperties.getApi().getKeys();
        return validKeys != null ? validKeys.get(apiKey) : null;
    }

    /**
     * 验证请求签名
     *
     * @param request HTTP请求
     * @return 是否验证通过
     */
    public boolean validateRequestSignature(HttpServletRequest request) {
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
     * 检测可疑请求模式
     *
     * @param request HTTP请求
     * @return 是否为可疑请求
     */
    public boolean isSuspiciousRequest(HttpServletRequest request) {
        // 检查URL参数
        String queryString = request.getQueryString();
        if (StringUtils.isNotBlank(queryString) && SUSPICIOUS_PATTERNS.matcher(queryString).matches()) {
            log.warn("Suspicious query string detected: {}", queryString);
            return true;
        }

        // 检查User-Agent
        String userAgent = request.getHeader("User-Agent");
        if (StringUtils.isNotBlank(userAgent) && SUSPICIOUS_PATTERNS.matcher(userAgent).matches()) {
            log.warn("Suspicious User-Agent detected: {}", userAgent);
            return true;
        }

        // 检查Referer
        String referer = request.getHeader("Referer");
        if (StringUtils.isNotBlank(referer) && SUSPICIOUS_PATTERNS.matcher(referer).matches()) {
            log.warn("Suspicious Referer detected: {}", referer);
            return true;
        }

        return false;
    }

    /**
     * 验证请求时间戳
     *
     * @param request HTTP请求
     * @return 是否有效
     */
    public boolean validateTimestamp(HttpServletRequest request) {
        String timestampStr = request.getHeader("X-Timestamp");
        if (StringUtils.isBlank(timestampStr)) {
            return false;
        }

        try {
            long timestamp = Long.parseLong(timestampStr);
            long currentTime = Instant.now().getEpochSecond();
            long validitySeconds = processSystemProperties.getSecurity().getSignatureValiditySeconds();
            
            return Math.abs(currentTime - timestamp) <= validitySeconds;
            
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 获取客户端IP地址
     *
     * @param request HTTP请求
     * @return IP地址
     */
    public String getClientIpAddress(HttpServletRequest request) {
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
     * 验证IP是否在白名单中
     *
     * @param request HTTP请求
     * @return 是否在白名单中
     */
    public boolean isIpInWhitelist(HttpServletRequest request) {
        if (!processSystemProperties.getSecurity().isEnableIpWhitelist()) {
            return true; // 未启用IP白名单，直接通过
        }

        String clientIp = getClientIpAddress(request);
        var ipWhitelist = processSystemProperties.getSecurity().getIpWhitelist();
        
        if (ipWhitelist == null || ipWhitelist.isEmpty()) {
            return true; // 白名单为空，直接通过
        }

        return ipWhitelist.contains(clientIp);
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
}