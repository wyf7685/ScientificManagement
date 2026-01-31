package com.achievement.domain.vo;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户简要信息
 * 来自 Keycloak JWT Token
 *
 * @author system
 * @since 2026-01-31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户简要信息")
public class UserProfileVO {

    @Schema(description = "用户ID")
    private Integer id;

    @Schema(description = "用户UUID")
    private String uuid;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String name;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "角色列表")
    private List<String> roles;
}
