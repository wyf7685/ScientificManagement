package com.achievement.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 中期成果物列表响应VO
 */
@Data
@Schema(description = "中期成果物列表响应")
public class InterimResultListVO {

    @Schema(description = "成果物列表")
    private List<InterimResultVO> list;

    @Schema(description = "总数")
    private Long total;

    @Schema(description = "当前页")
    private Integer page;

    @Schema(description = "页大小")
    private Integer pageSize;
}