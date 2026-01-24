package com.achievement.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

/**
 * 过程系统日志过滤器
 * 用于包装请求和响应以便捕获请求体和响应体
 *
 * @author system
 * @since 2026-01-24
 */
@Slf4j
@Component
@Order(1)
public class ProcessSystemLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            
            // 只对过程系统API进行日志记录
            String requestURI = httpRequest.getRequestURI();
            if (requestURI != null && requestURI.startsWith("/api/v1/process-system")) {
                
                // 包装请求和响应以便捕获内容
                ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest);
                ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(httpResponse);
                
                try {
                    // 继续过滤器链
                    chain.doFilter(wrappedRequest, wrappedResponse);
                } finally {
                    // 将响应内容复制回原始响应
                    wrappedResponse.copyBodyToResponse();
                }
            } else {
                // 非过程系统API，直接通过
                chain.doFilter(request, response);
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}