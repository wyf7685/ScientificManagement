package com.achievement.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.time.LocalDateTime;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 过程系统API访问日志表
 * </p>
 *
 * @author system
 * @since 2026-01-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("process_api_logs")
@Schema(name="ProcessApiLog对象", description="过程系统API访问日志表")
public class ProcessApiLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "请求ID")
    @TableField("request_id")
    private String requestId;

    @Schema(description = "API密钥")
    @TableField("api_key")
    private String apiKey;

    @Schema(description = "HTTP方法")
    @TableField("method")
    private String method;

    @Schema(description = "请求URL")
    @TableField("url")
    private String url;

    @Schema(description = "请求体")
    @TableField("request_body")
    private String requestBody;

    @Schema(description = "响应状态码")
    @TableField("response_code")
    private Integer responseCode;

    @Schema(description = "响应体")
    @TableField("response_body")
    private String responseBody;

    @Schema(description = "响应时间(ms)")
    @TableField("response_time")
    private Integer responseTime;

    @Schema(description = "客户端IP")
    @TableField("client_ip")
    private String clientIp;

    @Schema(description = "User Agent")
    @TableField("user_agent")
    private String userAgent;

    @Schema(description = "请求头信息")
    @TableField("request_headers")
    private String requestHeaders;

    @Schema(description = "响应头信息")
    @TableField("response_headers")
    private String responseHeaders;

    @Schema(description = "错误信息")
    @TableField("error_message")
    private String errorMessage;

    @Schema(description = "业务操作类型")
    @TableField("operation_type")
    private String operationType;

    @Schema(description = "操作结果 - success/failed")
    @TableField("operation_result")
    private String operationResult;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;
}