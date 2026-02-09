package com.achievement.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AchFieldRow {
    private Integer fieldId;
    // 字段定义 documentId
    private String documentId;
    // 字段值 documentId（用于编辑时幂等更新）
    private String fieldValueDocumentId;
    private String fieldCode;
    private String fieldName;
    private String fieldType;
    private Integer isRequired;

    private String textValue;
    private Boolean booleanValue;
    private BigDecimal numberValue;
    private LocalDate dateValue;
    private String emailValue;
}
