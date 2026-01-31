package com.achievement.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.achievement.annotation.CurrentUser;
import com.achievement.domain.dto.KeycloakUser;
import com.achievement.service.IKeycloakUserService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 当前用户参数解析器
 * 从 Spring Security Context 中的 JWT Token 解析用户信息并注入到 Controller 方法参数
 *
 * @author system
 * @since 2026-01-31
 */
@Slf4j
@Component
@AllArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final KeycloakConfig keycloakConfig;
    private final IKeycloakUserService keycloakUserService;

    /**
     * 判断是否支持该参数
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && KeycloakUser.class.isAssignableFrom(parameter.getParameterType());
    }

    /**
     * 解析参数 - 从 Spring Security Context 的 JWT Token 中提取用户信息
     */
    @Override
    public Object resolveArgument(MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {

        try {
            // 获取当前认证对象
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("未找到认证信息");
                throw new RuntimeException("未登录，请先登录");
            }

            // 从认证对象中获取 JWT Token
            if (!(authentication instanceof AbstractAuthenticationToken)) {
                log.warn("认证对象类型不正确");
                throw new RuntimeException("认证信息格式错误");
            }

            if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
                log.warn("认证 principal 不是 JWT 对象");
                throw new RuntimeException("认证信息格式错误");
            }

            // 从 JWT 提取用户信息
            String userId = jwt.getSubject(); // sub claim - Keycloak UUID
            String username = jwt.getClaim("preferred_username");
            String email = jwt.getClaim("email");
            String name = jwt.getClaim("name");

            // 提取角色并过滤掉内部角色
            List<String> roles = extractBusinessRolesFromJwt(jwt);

            KeycloakUser user = KeycloakUser.builder()
                    .id(keycloakUserService.getOrCreateUserId(userId))
                    .uuid(userId)
                    .username(username)
                    .email(email)
                    .name(name)
                    .roles(roles)
                    .build();

            log.debug("解析用户信息成功: userId={}, username={}, roles={}", userId, username, roles);

            return user;

        } catch (RuntimeException e) {
            log.error("解析用户信息失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("解析用户信息异常", e);
            throw new RuntimeException("解析用户信息异常");
        }
    }

    /**
     * 从 JWT Token 中提取业务角色
     * 过滤掉 Keycloak 内部角色
     *
     * @param jwt JWT Token
     * @return 业务角色列表
     */
    @SuppressWarnings("unchecked")
    private List<String> extractBusinessRolesFromJwt(Jwt jwt) {
        try {
            // 从 realm_access claim 中获取角色
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");

            if (realmAccess == null) {
                log.debug("JWT 中不存在 realm_access claim");
                return Collections.emptyList();
            }

            Object rolesObj = realmAccess.get("roles");

            if (!(rolesObj instanceof List)) {
                log.debug("realm_access.roles 不是 List 对象");
                return Collections.emptyList();
            }

            List<String> allRoles = (List<String>) rolesObj;

            // 过滤掉内部角色，只保留业务角色
            return allRoles.stream()
                    .filter(role -> !keycloakConfig.isDefaultRole(role))
                    .map(role -> "ROLE_" + role.trim().toUpperCase())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("提取 JWT 角色时出错", e);
            return Collections.emptyList();
        }
    }
}
