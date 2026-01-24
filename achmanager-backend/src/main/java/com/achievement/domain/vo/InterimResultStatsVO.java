package com.achievement.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 中期成果物统计响应VO
 */
@Data
@Schema(description = "中期成果物统计响应")
public class InterimResultStatsVO {

    @Schema(description = "项目总数")
    private Integer totalProjects;

    @Schema(description = "成果物总数")
    private Integer totalResults;

    @Schema(description = "按类型统计")
    private Map<String, Integer> byType;

    @Schema(description = "按年份统计")
    private Map<String, Integer> byYear;

    @Schema(description = "最近同步时间")
    private LocalDateTime recentSyncTime;
}