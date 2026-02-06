package com.achievement.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核待办列表VO
 */
@Data
@Schema(description = "审核待办列表")
public class ReviewBacklogVO {
    
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
    
    @Schema(description = "提交人")
    private String creatorName;
    @Schema(description = "提交人ID")
    private String creatorId;
    
    @Schema(description = "提交时间")
    private LocalDateTime createdAt;
    
    @Schema(description = "成果状态")
    private String status;
}
