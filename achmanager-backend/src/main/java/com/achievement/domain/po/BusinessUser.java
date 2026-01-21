package com.achievement.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 业务用户实体类
 *
 * @author system
 * @since 2026-01-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("business_users")
@Schema(name = "BusinessUser对象", description = "业务用户表")
public class BusinessUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Integer id;

    @Schema(description = "Strapi文档ID")
    private String documentId;

    @Schema(description = "用户名（登录用）")
    private String username;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "真实姓名")
    private String name;

    @Schema(description = "部门")
    private String department;

    @Schema(description = "角色：researcher/expert/manager/admin")
    private String role;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "是否激活")
    private Boolean isActive;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "发布时间")
    private LocalDateTime publishedAt;

    @Schema(description = "创建人ID")
    private Integer createdById;

    @Schema(description = "更新人ID")
    private Integer updatedById;

    @Schema(description = "语言")
    private String locale;

    @Schema(description = "软删除标记")
    private Integer isDelete;
}
