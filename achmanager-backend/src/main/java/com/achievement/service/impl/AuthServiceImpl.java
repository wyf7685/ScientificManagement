package com.achievement.service.impl;

import org.springframework.stereotype.Service;

import com.achievement.service.IAuthService;

import lombok.extern.slf4j.Slf4j;

/**
 * 认证服务实现
 * 
 * 前端直接调用 Keycloak API 获取和刷新 token，
 * 后端主要负责以下功能：
 * - 通过 CurrentUserArgumentResolver 验证 token
 * - 提供 /auth/current 端点获取用户信息
 * - 可扩展：token 黑名单、审计日志等
 *
 * @author system
 * @since 2026-01-31
 */
@Slf4j
@Service
public class AuthServiceImpl implements IAuthService {

        // 认证相关的具体实现可在此扩展
}
