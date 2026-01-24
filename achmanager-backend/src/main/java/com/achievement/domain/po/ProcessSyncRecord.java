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
 * 过程系统同步记录表
 * </p>
 *
 * @author system
 * @since 2026-01-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("process_sync_records")
@Schema(name="ProcessSyncRecord对象", description="过程系统同步记录表")
public class ProcessSyncRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "同步ID")
    @TableField("sync_id")
    private String syncId;

    @Schema(description = "申报ID")
    @TableField("application_id")
    private Long applicationId;

    @Schema(description = "提交物ID")
    @TableField("submission_id")
    private Long submissionId;

    @Schema(description = "同步类型")
    @TableField("sync_type")
    private String syncType;

    @Schema(description = "同步状态")
    @TableField("sync_status")
    private String syncStatus;

    @Schema(description = "操作类型")
    @TableField("operation_type")
    private String operationType;

    @Schema(description = "同步开始时间")
    @TableField("sync_start_time")
    private LocalDateTime syncStartTime;

    @Schema(description = "同步结束时间")
    @TableField("sync_end_time")
    private LocalDateTime syncEndTime;

    @Schema(description = "同步记录数")
    @TableField("sync_count")
    private Integer syncCount;

    @Schema(description = "成功记录数")
    @TableField("success_count")
    private Integer successCount;

    @Schema(description = "失败记录数")
    @TableField("failed_count")
    private Integer failedCount;

    @Schema(description = "错误信息")
    @TableField("error_message")
    private String errorMessage;

    @Schema(description = "错误代码")
    @TableField("error_code")
    private String errorCode;

    @Schema(description = "数据版本")
    @TableField("data_version")
    private String dataVersion;

    @Schema(description = "源数据哈希")
    @TableField("source_data_hash")
    private String sourceDataHash;

    @Schema(description = "操作人ID")
    @TableField("operator_id")
    private String operatorId;

    @Schema(description = "操作人姓名")
    @TableField("operator_name")
    private String operatorName;

    @Schema(description = "同步备注")
    @TableField("sync_remark")
    private String syncRemark;

    @Schema(description = "重试次数")
    @TableField("retry_count")
    private Integer retryCount;

    @Schema(description = "下次重试时间")
    @TableField("next_retry_time")
    private LocalDateTime nextRetryTime;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}