package com.achievement.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 分配审核人请求DTO
 */
@Data
@Schema(description = "分配审核人请求")
public class AssignReviewerDTO {
    
    @NotNull(message = "审核人ID列表不能为空")
    @Schema(description = "审核人ID列表")
    private List<Integer> reviewerIds;
    
    @Schema(description = "审核人姓名列表(与ID对应)")
    private List<String> reviewerNames;
}
