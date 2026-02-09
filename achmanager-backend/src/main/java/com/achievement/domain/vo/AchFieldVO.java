package com.achievement.domain.vo;

import lombok.Data;

@Data
public class AchFieldVO {
    private Integer id;
    // 字段定义 documentId
    private String documentId;
    // 字段值 documentId（编辑时回传可用于更新而非新增）
    private String fieldValueDocumentId;
    private String fieldCode;
    private String fieldName;
    private String fieldType;
    private Integer isRequired;

    // 统一给前端一个 value（真实值是什么类型由 fieldType 决定）
    private Object value;
}
