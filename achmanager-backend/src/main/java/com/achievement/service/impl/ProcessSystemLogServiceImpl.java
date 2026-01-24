package com.achievement.service.impl;

import com.achievement.domain.po.ProcessApiLog;
import com.achievement.mapper.ProcessApiLogMapper;
import com.achievement.service.ProcessSystemLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 过程系统日志服务实现
 *
 * @author system
 * @since 2026-01-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessSystemLogServiceImpl implements ProcessSystemLogService {

    private final ProcessApiLogMapper processApiLogMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Async
    public void logApiAccess(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        try {
            String requestId = UUID.randomUUID().toString();
            String apiKey = (String) request.getAttribute("apiKey");
            String method = request.getMethod();
            String url = request.getRequestURL().toString();
            if (request.getQueryString() != null) {
                url += "?" + request.getQueryString();
            }
            
            int responseCode = response.getStatus();
            String clientIp = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            
            Long startTime = (Long) request.getAttribute("startTime");
            int responseTime = startTime != null ? 
                (int) (System.currentTimeMillis() - startTime) : 0;

            // 获取请求体和响应体
            String requestBody = (String) request.getAttribute("requestBody");
            String responseBody = (String) request.getAttribute("responseBody");
            
            // 获取请求头和响应头
            String requestHeaders = getRequestHeaders(request);
            String responseHeaders = getResponseHeaders(response);
            
            // 确定操作类型
            String operationType = determineOperationType(method, url);
            
            // 确定操作结果
            String operationResult = (responseCode >= 200 && responseCode < 300) ? "success" : "failed";
            
            // 错误信息
            String errorMessage = null;
            if (exception != null) {
                errorMessage = exception.getMessage();
                if (errorMessage != null && errorMessage.length() > 1000) {
                    errorMessage = errorMessage.substring(0, 1000) + "...";
                }
            }

            // 创建日志记录
            ProcessApiLog apiLog = new ProcessApiLog()
                    .setRequestId(requestId)
                    .setApiKey(apiKey)
                    .setMethod(method)
                    .setUrl(url)
                    .setRequestBody(truncateString(requestBody, 5000))
                    .setResponseCode(responseCode)
                    .setResponseBody(truncateString(responseBody, 5000))
                    .setResponseTime(responseTime)
                    .setClientIp(clientIp)
                    .setUserAgent(userAgent)
                    .setRequestHeaders(truncateString(requestHeaders, 2000))
                    .setResponseHeaders(truncateString(responseHeaders, 2000))
                    .setErrorMessage(errorMessage)
                    .setOperationType(operationType)
                    .setOperationResult(operationResult)
                    .setCreatedAt(LocalDateTime.now());

            // 保存到数据库
            processApiLogMapper.insert(apiLog);

            // 记录访问日志
            log.info("Process System API Access - RequestId: {}, ApiKey: {}, Method: {}, URL: {}, " +
                    "ResponseCode: {}, ResponseTime: {}ms, ClientIP: {}, UserAgent: {}, OperationType: {}, Result: {}", 
                    requestId, apiKey, method, url, responseCode, responseTime, clientIp, userAgent, operationType, operationResult);

            if (exception != null) {
                log.error("Process System API Error - RequestId: {}, Error: {}", 
                        requestId, exception.getMessage(), exception);
            }
            
        } catch (Exception e) {
            log.error("Failed to log API access", e);
        }
    }

    @Override
    @Async
    public void logOperation(String operation, String details, boolean success) {
        try {
            String requestId = UUID.randomUUID().toString();
            
            // 创建操作日志记录
            ProcessApiLog operationLog = new ProcessApiLog()
                    .setRequestId(requestId)
                    .setApiKey("SYSTEM")
                    .setMethod("OPERATION")
                    .setUrl("/internal/operation")
                    .setRequestBody(details)
                    .setResponseCode(success ? 200 : 500)
                    .setResponseBody(success ? "操作成功" : "操作失败")
                    .setResponseTime(0)
                    .setClientIp("127.0.0.1")
                    .setUserAgent("System")
                    .setOperationType(operation)
                    .setOperationResult(success ? "success" : "failed")
                    .setCreatedAt(LocalDateTime.now());

            // 保存到数据库
            processApiLogMapper.insert(operationLog);
            
            log.info("Process System Operation - RequestId: {}, Operation: {}, Details: {}, Success: {}", 
                    requestId, operation, details, success);
            
        } catch (Exception e) {
            log.error("Failed to log operation", e);
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
    
    /**
     * 获取请求头信息
     */
    private String getRequestHeaders(HttpServletRequest request) {
        try {
            Map<String, String> headers = new HashMap<>();
            Enumeration<String> headerNames = request.getHeaderNames();
            
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                // 过滤敏感信息
                if (!isSensitiveHeader(headerName)) {
                    headers.put(headerName, request.getHeader(headerName));
                }
            }
            
            return objectMapper.writeValueAsString(headers);
        } catch (Exception e) {
            log.warn("Failed to serialize request headers", e);
            return "{}";
        }
    }
    
    /**
     * 获取响应头信息
     */
    private String getResponseHeaders(HttpServletResponse response) {
        try {
            Map<String, String> headers = new HashMap<>();
            
            for (String headerName : response.getHeaderNames()) {
                // 过滤敏感信息
                if (!isSensitiveHeader(headerName)) {
                    headers.put(headerName, response.getHeader(headerName));
                }
            }
            
            return objectMapper.writeValueAsString(headers);
        } catch (Exception e) {
            log.warn("Failed to serialize response headers", e);
            return "{}";
        }
    }
    
    /**
     * 判断是否为敏感头信息
     */
    private boolean isSensitiveHeader(String headerName) {
        if (headerName == null) {
            return true;
        }
        
        String lowerName = headerName.toLowerCase();
        return lowerName.contains("authorization") || 
               lowerName.contains("cookie") || 
               lowerName.contains("token") ||
               lowerName.contains("password") ||
               lowerName.contains("secret");
    }
    
    /**
     * 根据请求方法和URL确定操作类型
     */
    private String determineOperationType(String method, String url) {
        if (url == null) {
            return "UNKNOWN";
        }
        
        if (url.contains("/submissions")) {
            if ("POST".equals(method)) {
                return "STORE_SUBMISSION";
            } else if ("GET".equals(method)) {
                if (url.matches(".*/submissions/\\d+$")) {
                    return "GET_SUBMISSION_DETAIL";
                } else {
                    return "QUERY_SUBMISSIONS";
                }
            }
        }
        
        if (url.contains("/files")) {
            if ("GET".equals(method)) {
                return "DOWNLOAD_FILE";
            } else if ("POST".equals(method)) {
                return "UPLOAD_FILE";
            }
        }
        
        if (url.contains("/sync")) {
            return "SYNC_DATA";
        }
        
        return method + "_REQUEST";
    }
    
    /**
     * 截断字符串到指定长度
     */
    private String truncateString(String str, int maxLength) {
        if (!StringUtils.hasText(str)) {
            return str;
        }
        
        if (str.length() <= maxLength) {
            return str;
        }
        
        return str.substring(0, maxLength) + "...[truncated]";
    }
}