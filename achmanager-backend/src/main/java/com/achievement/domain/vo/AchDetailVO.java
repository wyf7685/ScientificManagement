package com.achievement.domain.vo;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "成果物详情返回对象")
public class AchDetailVO {
        // 成果物公共属性
        @Schema(description = "成果物ID")
        private String documentId;
        private String title;
        private String summary;
        private String auditStatus;     // achievement_status
        private String creatorName;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime publishedAt;

        // 类型信息（可按你需要增减）
        private String typeDocId;
        private String typeName;
        private String typeCode;

        // 动态字段：字段定义 + 值
        private List<AchFieldVO> fields;

        // 附件信息（Strapi achievement_files，populate=files 的原始返回）
        private JsonNode attachments;
    }
