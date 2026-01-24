package com.achievement.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 过程系统日志服务接口
 *
 * @author system
 * @since 2026-01-23
 */
public interface ProcessSystemLogService {

    /**
     * 记录API访问日志
     *
     * @param request HTTP请求
     * @param response HTTP响应
     * @param exception 异常信息（如果有）
     */
    void logApiAccess(HttpServletRequest request, HttpServletResponse response, Exception exception);

    /**
     * 记录操作日志
     *
     * @param operation 操作类型
     * @param details 操作详情
     * @param success 是否成功
     */
    void logOperation(String operation, String details, boolean success);
}