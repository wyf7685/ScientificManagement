package com.achievement.domain.vo;

import lombok.Data;

@Data
public class TypeYearTrendRow {
    private Integer year;
    private String typeCode;
    private String typeName;
    private Integer totalCount;
    private Integer approvedCount;
}

