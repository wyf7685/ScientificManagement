package com.achievement.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AchListVO {
    private String documentId;  // 成果物ID，对应 m.id
    private String title;           // 成果物名称，对应 m.title
    private String typeName;       // 成果物类型
    private String auditStatus;    // 审核状态
    private String creatorName;    // 创建者用户名称
    private String authorName;     // 成果物作者（若与创建者不同，按你的表字段映射）
    private String projectName;    // 所属项目名称
    private String summary;        // 成果物概要
    private LocalDateTime createdAt; //创建时间
    private String visibilityRange;   // 可见范围
}

