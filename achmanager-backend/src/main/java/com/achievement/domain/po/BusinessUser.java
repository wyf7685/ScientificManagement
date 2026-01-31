package com.achievement.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 用户 ID 映射类
 * 主要用于将 Keycloak 用户 UUID 映射到本地业务用户 ID
 * 可根据需要扩展其他业务字段
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

    @Schema(description = "Keycloak 用户 UUID")
    private String keycloakUserId;

    // 其他拓展字段...
}
