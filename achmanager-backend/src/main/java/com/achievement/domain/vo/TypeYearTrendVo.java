package com.achievement.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class TypeYearTrendVo {
    private List<String> timeline; // ["2022","2023","2024","2025","2026"]

    // 按类型分组，每个类型一条“总量 bar/line”也行
    private List<TypeYearTrendSeries> series;

    // 两条线：总量 vs 已通过（你现在说“不需要submitted”，那我给的是 total vs approved）
    private List<Integer> totalCounts;     // 每年总数（可按你口径排除 DRAFT）
    private List<Integer> approvedCounts;  // 每年审核通过数
}
