package com.achievement.domain.vo;

import lombok.Data;

@Data
public class TypeCountVO {
    private String typeName;   // 成果物类型
    private String typeCode;   // 成果物英文可选
    private Long count;        // 数量
}

