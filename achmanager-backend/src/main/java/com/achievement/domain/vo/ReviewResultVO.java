package com.achievement.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核结果VO
 */
@Data
@Schema(description = "审核结果")
public class ReviewResultVO {
    
    @Schema(description = "成果documentId")
    private String achievementDocId;
    
    @Schema(description = "审核后状态")
    private String status;
    
    @Schema(description = "审核人ID")
    private Integer reviewerId;
    
    @Schema(description = "审核人姓名")
    private String reviewerName;
    
    @Schema(description = "审核意见")
    private String comment;
    
    @Schema(description = "审核时间")
    private LocalDateTime reviewedAt;
    
    @Schema(description = "是否成功")
    private Boolean success;
    
    @Schema(description = "消息")
    private String message;
}
