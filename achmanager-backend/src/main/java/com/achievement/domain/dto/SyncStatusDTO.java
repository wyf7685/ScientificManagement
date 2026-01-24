package com.achievement.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 同步状态DTO
 */
@Data
@Schema(description = "同步状态信息")
public class SyncStatusDTO {

    @Schema(description = "申报ID")
    private Long applicationId;

    @Schema(description = "最后同步时间")
    private LocalDateTime lastSyncTime;

    @Schema(description = "同步状态")
    private String syncStatus;

    @Schema(description = "同步记录数")
    private Integer syncCount;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "同步类型")
    private String syncType;

    @Schema(description = "数据版本")
    private String dataVersion;
}