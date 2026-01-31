package com.achievement.domain.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Keycloak 用户对象
 * 替代原有的 BusinessUser，作为认证用户的核心对象
 * 包含来自 JWT Token 的用户信息和角色
 *
 * @author system
 * @since 2026-01-31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Keycloak 用户对象")
public class KeycloakUser {

    @Schema(description = "用户ID")
    private Integer id;

    @Schema(description = "Keycloak 用户 UUID")
    private String uuid;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "角色列表")
    private List<String> roles;

    @Schema(description = "用户是否启用")
    private boolean enabled;

    /**
     * 检查用户是否拥有指定角色
     *
     * @param role 角色名
     * @return 是否拥有该角色
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    /**
     * 检查用户是否拥有任何一个指定角色
     *
     * @param roleArray 角色名数组
     * @return 是否拥有任何一个角色
     */
    public boolean hasAnyRole(String... roleArray) {
        if (roles == null || roleArray == null) {
            return false;
        }
        for (String role : roleArray) {
            if (roles.contains(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查用户是否拥有所有指定角色
     *
     * @param roleArray 角色名数组
     * @return 是否拥有所有角色
     */
    public boolean hasAllRoles(String... roleArray) {
        if (roleArray == null || roles == null) {
            return false;
        }
        for (String role : roleArray) {
            if (!roles.contains(role)) {
                return false;
            }
        }
        return true;
    }
}
