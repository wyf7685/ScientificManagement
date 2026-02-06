package com.achievement.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewAssignVO {
    @Schema(description = "成果documentId")
    private String documentId;

    @Schema(description = "成果标题")
    private String title;

    @Schema(description = "成果类型")
    private String type;
    @Schema(description = "成果物提交人")
    private String creatorName;
    @Schema(description = "分配时间")
    private LocalDateTime assignedAt;
    @Schema(description = "被分配人名称")
    private String reviewerName;
    //仅用于转换用户名
    private String creatorId;
}
