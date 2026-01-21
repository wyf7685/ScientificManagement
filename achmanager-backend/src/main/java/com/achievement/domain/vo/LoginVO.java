package com.achievement.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应VO
 *
 * @author system
 * @since 2026-01-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginVO {

    @Schema(description = "JWT令牌")
    private String token;

    @Schema(description = "用户信息")
    private UserProfileVO user;

    @Schema(description = "用户ID")
    private Integer id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String name;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "角色")
    private String role;

    @Schema(description = "部门")
    private String department;

    @Schema(description = "联系电话")
    private String phone;
}
