package com.achievement.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class TypeYearTrendSeries {
    private String typeCode;
    private String typeName;
    private List<Integer> total;     // 每年该类型总数
    private List<Integer> approved;  // 每年该类型已通过数
}
