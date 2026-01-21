package com.achievement.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 访问申请VO
 */
@Data
@Schema(description = "访问申请")
public class AccessRequestVO {
    
    @Schema(description = "申请ID")
    private String id;
    
    @Schema(description = "成果documentId")
    private String resultId;
    
    @Schema(description = "成果标题")
    private String resultTitle;
    
    @Schema(description = "成果类型")
    private String resultType;
    
    @Schema(description = "成果状态")
    private String resultStatus;
    
    @Schema(description = "所属项目名称")
    private String projectName;
    
    @Schema(description = "可见范围")
    private String visibility;
    
    @Schema(description = "申请人ID")
    private Integer userId;
    
    @Schema(description = "申请人姓名")
    private String userName;
    
    @Schema(description = "申请理由")
    private String reason;
    
    @Schema(description = "申请状态: pending-待审核, approved-已通过, rejected-已拒绝")
    private String status;
    
    @Schema(description = "审批人")
    private String reviewer;
    
    @Schema(description = "审批意见")
    private String comment;
    
    @Schema(description = "申请时间")
    private LocalDateTime createdAt;
    
    @Schema(description = "审批时间")
    private LocalDateTime reviewedAt;
}
