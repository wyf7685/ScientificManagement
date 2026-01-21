package com.achievement.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 访问申请审核DTO
 */
@Data
@Schema(description = "访问申请审核")
public class AccessRequestReviewDTO {
    
    @NotBlank(message = "审核动作不能为空")
    @Schema(description = "审核动作: approve-通过, reject-拒绝")
    private String action;
    
    @Schema(description = "审核意见")
    private String comment;
}
