package com.achievement.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

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
    private String creatorName;

    private Integer typeId;
    private String typeDocId;
    private String typeName;
    private String typeCode;
}

