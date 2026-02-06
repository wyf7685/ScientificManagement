package com.achievement.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AchMainBaseRow {
    private Integer mainId;
    private String documentId;
    private String title;
    private String summary;
    private String auditStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private String createdByUserId;

    private Integer typeId;
    private String typeDocId;
    private String typeName;
    private String typeCode;
    
    // 新增基础字段
    private String year;
    private String authorsJson;  // JSON 字符串,需要反序列化
    private String keywordsJson; // JSON 字符串,需要反序列化
    private String projectCode;
    private String projectName;
    private String visibilityRange;
}

