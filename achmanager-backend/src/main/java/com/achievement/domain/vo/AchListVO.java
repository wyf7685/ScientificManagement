package com.achievement.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AchListVO {
    private String documentId;  // 成果物ID，对应 m.id
    private String title;           // 成果物名称，对应 m.title
    private String typeName;       // 成果物类型
    private String auditStatus;    // 审核状态
    private String creatorId;      // 创建者用户ID
    private String creatorName;    // 创建者用户名称
    private String year; // 成果物年份
    private List<String> authors;     // 成果物作者（若与创建者不同，按你的表字段映射）
    private List<String> keywords;    // 成果物关键词
    private String projectName;    // 所属项目名称
    private String summary;        // 成果物概要
    private LocalDateTime createdAt; //创建时间
    private String visibilityRange;   // 可见范围
}

