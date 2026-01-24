package com.achievement.interceptor;

import com.achievement.filter.RequestResponseLoggingWrapper;
import com.achievement.service.ProcessSystemLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;

/**
 * 过程系统API日志拦截器
 *
 * @author system
 * @since 2026-01-23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessSystemApiLogInterceptor implements HandlerInterceptor {

    private final ProcessSystemLogService processSystemLogService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 记录请求开始时间
        request.setAttribute("startTime", System.currentTimeMillis());
        
        // 如果是POST/PUT请求，尝试获取请求体
        if ("POST".equalsIgnoreCase(request.getMethod()) || "PUT".equalsIgnoreCase(request.getMethod())) {
            try {
                // 如果请求已经被包装过，直接获取请求体
                if (request instanceof ContentCachingRequestWrapper) {
                    ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
                    byte[] content = wrapper.getContentAsByteArray();
                    if (content.length > 0) {
                        String requestBody = new String(content, StandardCharsets.UTF_8);
                        request.setAttribute("requestBody", requestBody);
                    }
                } else if (request instanceof RequestResponseLoggingWrapper.CachingRequestWrapper) {
                    RequestResponseLoggingWrapper.CachingRequestWrapper wrapper = 
                        (RequestResponseLoggingWrapper.CachingRequestWrapper) request;
                    String requestBody = wrapper.getBody();
                    request.setAttribute("requestBody", requestBody);
                }
            } catch (Exception e) {
                log.warn("Failed to capture request body", e);
            }
        }
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                              Object handler, Exception ex) {
        try {
            // 尝试获取响应体
            if (response instanceof ContentCachingResponseWrapper) {
                ContentCachingResponseWrapper wrapper = (ContentCachingResponseWrapper) response;
                byte[] content = wrapper.getContentAsByteArray();
                if (content.length > 0) {
                    String responseBody = new String(content, StandardCharsets.UTF_8);
                    request.setAttribute("responseBody", responseBody);
                }
            } else if (response instanceof RequestResponseLoggingWrapper.CachingResponseWrapper) {
                RequestResponseLoggingWrapper.CachingResponseWrapper wrapper = 
                    (RequestResponseLoggingWrapper.CachingResponseWrapper) response;
                String responseBody = wrapper.getBody();
                request.setAttribute("responseBody", responseBody);
            }
            
            // 异步记录API访问日志
            processSystemLogService.logApiAccess(request, response, ex);
        } catch (Exception e) {
            log.error("Failed to log API access", e);
        }
    }
}