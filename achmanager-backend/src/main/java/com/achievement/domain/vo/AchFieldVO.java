package com.achievement.domain.vo;

import lombok.Data;

@Data
public class AchFieldVO {
    private Integer id;
    private String documentId;
    private String fieldCode;
    private String fieldName;
    private String fieldType;
    private Integer isRequired;

    // 统一给前端一个 value（真实值是什么类型由 fieldType 决定）
    private Object value;
}
