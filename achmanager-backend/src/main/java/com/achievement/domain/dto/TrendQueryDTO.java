package com.achievement.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TrendQueryDTO {
    private String dimension; // type
    @Schema(description = "范围：3y/5y，默认5y", example = "5y")
    private String range = "5y";
    private Integer fromYear;
    private Integer toYear;
}

