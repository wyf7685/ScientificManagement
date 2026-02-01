package com.achievement.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.achievement.annotation.CurrentUser;
import com.achievement.domain.dto.KeycloakUser;
import com.achievement.domain.vo.UserProfileVO;
import com.achievement.result.Result;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 认证控制器
 * 支持 Keycloak 认证和多角色
 *
 * @author system
 * @since 2026-01-31
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证相关接口")
public class AuthController {

    /**
     * 获取当前登录用户信息
     * 前端获取 Keycloak token 后，调用此端点获取用户信息
     */
    @Operation(description = "获取当前登录用户信息")
    @GetMapping("/current")
    public Result<UserProfileVO> current(@CurrentUser KeycloakUser currentUser) {
        log.debug("获取当前用户信息: userId={}, username={}, roles={}",
                currentUser.getId(), currentUser.getUsername(), currentUser.getRoles());

        UserProfileVO profile = UserProfileVO.builder()
                .id(currentUser.getId())
                .uuid(currentUser.getUuid())
                .username(currentUser.getUsername())
                .name(currentUser.getName())
                .email(currentUser.getEmail())
                .roles(currentUser.getRoles())
                .build();

        return Result.success(profile);
    }

    /**
     * 退出登录
     * 前端调用此端点执行登出操作（可选）
     * 前端主要通过清除本地 token 实现登出
     */
    @Operation(description = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(@CurrentUser KeycloakUser currentUser) {
        log.info("用户登出: userId={}, username={}", currentUser.getId(), currentUser.getUsername());

        // 后端可在此处执行一些登出相关的操作，如：
        // - 记录登出日志
        // - 清除会话信息
        // - 将 token 加入黑名单（如果需要）

        return Result.success();
    }

    /**
     * 验证 Token
     * 前端可调用此端点验证当前 token 的有效性
     */
    @Operation(description = "验证 Token")
    @PostMapping("/verify")
    public Result<KeycloakUser> verify(@CurrentUser KeycloakUser currentUser) {
        log.debug("验证 Token: userId={}, username={}", currentUser.getId(), currentUser.getUsername());
        return Result.success(currentUser);
    }
}
