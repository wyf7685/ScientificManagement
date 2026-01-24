package com.achievement.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 过程系统API日志查询DTO
 *
 * @author system
 * @since 2026-01-24
 */
@Data
@Accessors(chain = true)
@Schema(name = "ProcessApiLogQueryDTO", description = "过程系统API日志查询参数")
public class ProcessApiLogQueryDTO {

    @Schema(description = "页码")
    private Integer page = 1;

    @Schema(description = "页大小")
    private Integer pageSize = 20;

    @Schema(description = "API密钥")
    private String apiKey;

    @Schema(description = "HTTP方法")
    private String method;

    @Schema(description = "请求URL")
    private String url;

    @Schema(description = "响应状态码")
    private Integer responseCode;

    @Schema(description = "操作类型")
    private String operationType;

    @Schema(description = "操作结果")
    private String operationResult;

    @Schema(description = "客户端IP")
    private String clientIp;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "最小响应时间(ms)")
    private Integer minResponseTime;

    @Schema(description = "最大响应时间(ms)")
    private Integer maxResponseTime;

    @Schema(description = "是否只查询错误日志")
    private Boolean errorsOnly = false;

    @Schema(description = "排序字段")
    private String sortField = "created_at";

    @Schema(description = "排序方向")
    private String sortOrder = "desc";
}