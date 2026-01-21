package com.achievement.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 访问申请查询DTO
 */
@Data
@Schema(description = "访问申请查询条件")
public class AccessRequestQueryDTO {
    
    @Schema(description = "关键词（成果标题或申请人）")
    private String keyword;
    
    @Schema(description = "状态: pending-待审核, approved-已通过, rejected-已拒绝")
    private String status;
    
    @Schema(description = "页码")
    private Integer page;
    
    @Schema(description = "每页大小")
    private Integer pageSize;
}
