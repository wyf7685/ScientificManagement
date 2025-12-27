package com.achievement.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AchFieldRow {
    private Integer fieldId;
    private String documentId;
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

