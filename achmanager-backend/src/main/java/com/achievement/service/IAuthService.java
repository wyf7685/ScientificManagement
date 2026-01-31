package com.achievement.service;

/**
 * 认证服务接口
 * 
 * 注意：前端直接调用 Keycloak API 获取和刷新 token
 * 后端主要负责验证 token 和提供用户信息端点
 *
 * @author system
 * @since 2026-01-31
 */
public interface IAuthService {

    // 认证相关服务可在此扩展
    // 如 token 黑名单管理、审计日志等
}
