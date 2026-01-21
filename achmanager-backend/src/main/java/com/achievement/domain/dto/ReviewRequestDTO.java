package com.achievement.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 审核请求DTO
 */
@Data
@Schema(description = "审核请求")
public class ReviewRequestDTO {
    
    @NotBlank(message = "审核动作不能为空")
    @Schema(description = "审核动作: approve-通过, reject-驳回, request_changes-要求修改")
    private String action;
    
    @Schema(description = "审核意见")
    private String comment;
    
    @Schema(description = "新的可见范围(仅approve时可设置)")
    private String visibilityRange;
}
