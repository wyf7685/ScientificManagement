package com.achievement.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Keycloak Token Endpoint 响应
 * 对应 Keycloak 的 token 端点返回结果
 *
 * @author system
 * @since 2026-01-31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Keycloak Token Endpoint 响应")
public class KeycloakTokenResponse {

    @Schema(description = "访问令牌 (access token)")
    @JsonProperty("access_token")
    private String accessToken;

    @Schema(description = "刷新令牌 (refresh token)")
    @JsonProperty("refresh_token")
    private String refreshToken;

    @Schema(description = "Token 类型，通常为 \"Bearer\"")
    @JsonProperty("token_type")
    private String tokenType;

    @Schema(description = "过期时间 (秒)")
    @JsonProperty("expires_in")
    private Integer expiresIn;

    @Schema(description = "刷新令牌过期时间 (秒)")
    @JsonProperty("refresh_expires_in")
    private Integer refreshExpiresIn;

    @Schema(description = "权限范围")
    private String scope;

    @Schema(description = "Session 状态 (用于 OIDC)")
    @JsonProperty("session_state")
    private String sessionState;
}
