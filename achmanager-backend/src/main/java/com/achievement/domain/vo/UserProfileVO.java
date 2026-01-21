package com.achievement.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 用户简要信息
 */
@Data
@Builder
@Schema(description = "用户简要信息")
public class UserProfileVO {

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
