package com.achievement.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核历史列表VO
 */
@Data
@Schema(description = "审核历史列表")
public class ReviewHistoryVO {
    
    @Schema(description = "成果documentId")
    private String id;
    
    @Schema(description = "成果标题")
    private String title;
    
    @Schema(description = "成果类型")
    private String type;
    
    @Schema(description = "所属项目名称")
    private String projectName;
    
    @Schema(description = "所属项目编号")
    private String projectCode;
    
    @Schema(description = "审核结果: approve-通过, reject-驳回")
    private String reviewResult;
    
    @Schema(description = "审核时间")
    private LocalDateTime reviewedAt;
    
    @Schema(description = "审核意见")
    private String comment;
}
