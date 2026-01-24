package com.achievement.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 批量同步请求DTO
 */
@Data
@Schema(description = "批量同步请求")
public class BatchSyncRequest {

    @Schema(description = "申报ID列表")
    @NotEmpty(message = "申报ID列表不能为空")
    private List<Long> applicationIds;

    @Schema(description = "同步类型")
    private String syncType = "manual";

    @Schema(description = "强制同步（忽略时间戳检查）")
    private Boolean forceSync = false;

    @Schema(description = "同步开始时间")
    private LocalDateTime syncStartTime;

    @Schema(description = "同步结束时间")
    private LocalDateTime syncEndTime;

    @Schema(description = "操作人ID")
    private String operatorId;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "同步备注")
    private String syncRemark;
}