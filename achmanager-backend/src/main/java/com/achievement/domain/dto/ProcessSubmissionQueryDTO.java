package com.achievement.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 过程系统提交物查询DTO
 */
@Data
@Schema(description = "过程系统提交物查询条件")
public class ProcessSubmissionQueryDTO {

    @Schema(description = "申报ID")
    private Long applicationId;

    @Schema(description = "提交物ID")
    private Long submissionId;

    @Schema(description = "提交阶段")
    private String submissionStage;

    @Schema(description = "提交物类型")
    private String submissionType;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "申报人姓名")
    private String applicantName;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Schema(description = "页大小")
    private Integer pageSize = 10;
}