package com.achievement.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 同步结果VO
 */
@Data
@Schema(description = "同步结果")
public class SyncResultVO {

    @Schema(description = "同步ID")
    private String syncId;

    @Schema(description = "同步类型")
    private String syncType;

    @Schema(description = "同步状态")
    private String syncStatus;

    @Schema(description = "同步开始时间")
    private LocalDateTime syncStartTime;

    @Schema(description = "同步结束时间")
    private LocalDateTime syncEndTime;

    @Schema(description = "同步记录数")
    private Integer syncCount;

    @Schema(description = "总记录数")
    private Integer totalCount;

    @Schema(description = "成功记录数")
    private Integer successCount;

    @Schema(description = "失败记录数")
    private Integer failedCount;

    @Schema(description = "跳过记录数")
    private Integer skippedCount;

    @Schema(description = "新增记录数")
    private Integer newCount;

    @Schema(description = "更新记录数")
    private Integer updatedCount;

    @Schema(description = "同步消息")
    private String message;

    @Schema(description = "错误信息列表")
    private List<SyncErrorInfo> errors;

    @Schema(description = "同步详情列表")
    private List<SyncDetailInfo> details;

    @Data
    @Schema(description = "同步错误信息")
    public static class SyncErrorInfo {
        @Schema(description = "申报ID")
        private Long applicationId;

        @Schema(description = "错误代码")
        private String errorCode;

        @Schema(description = "错误信息")
        private String errorMessage;

        @Schema(description = "错误时间")
        private LocalDateTime errorTime;
    }

    @Data
    @Schema(description = "同步详情信息")
    public static class SyncDetailInfo {
        @Schema(description = "申报ID")
        private Long applicationId;

        @Schema(description = "提交物ID")
        private Long submissionId;

        @Schema(description = "操作类型")
        private String operationType;

        @Schema(description = "操作状态")
        private String operationStatus;

        @Schema(description = "操作时间")
        private LocalDateTime operationTime;

        @Schema(description = "备注")
        private String remark;
    }
}
