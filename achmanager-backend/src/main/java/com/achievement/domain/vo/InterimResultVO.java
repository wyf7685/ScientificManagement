package com.achievement.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 中期成果物响应VO
 */
@Data
@Schema(description = "中期成果物响应")
public class InterimResultVO {

    @Schema(description = "成果物ID")
    private String id;

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "项目编码")
    private String projectCode;

    @Schema(description = "项目阶段")
    private String projectPhase;

    @Schema(description = "成果物名称")
    private String name;

    @Schema(description = "成果物类型")
    private String type;

    @Schema(description = "成果物类型标签")
    private String typeLabel;

    @Schema(description = "成果物描述")
    private String description;

    @Schema(description = "附件列表")
    private List<AttachmentVO> attachments;

    @Schema(description = "提交者")
    private String submitter;

    @Schema(description = "提交者部门")
    private String submitterDept;

    @Schema(description = "提交时间")
    private LocalDateTime submittedAt;

    @Schema(description = "同步时间")
    private LocalDateTime syncedAt;

    @Schema(description = "数据来源")
    private String source;

    @Schema(description = "来源引用ID")
    private String sourceRef;

    @Schema(description = "来源URL")
    private String sourceUrl;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "状态")
    private String status;

    @Data
    @Schema(description = "附件信息")
    public static class AttachmentVO {
        @Schema(description = "附件ID")
        private String id;

        @Schema(description = "附件名称")
        private String name;

        @Schema(description = "附件URL")
        private String url;

        @Schema(description = "附件大小")
        private Long size;

        @Schema(description = "文件扩展名")
        private String ext;

        @Schema(description = "附件分类")
        private String category;
    }
}