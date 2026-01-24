package com.achievement.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.time.LocalDateTime;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 中期成果物视图
 * </p>
 *
 * @author system
 * @since 2026-01-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("interim_results_view")
@Schema(name="InterimResultView对象", description="中期成果物视图")
public class InterimResultView implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "成果物ID")
    @TableId("id")
    private Long id;

    @Schema(description = "来源引用ID")
    @TableField("source_ref")
    private Long sourceRef;

    @Schema(description = "项目ID")
    @TableField("project_id")
    private Long projectId;

    @Schema(description = "项目名称")
    @TableField("project_name")
    private String projectName;

    @Schema(description = "项目编码")
    @TableField("project_code")
    private String projectCode;

    @Schema(description = "项目阶段")
    @TableField("project_phase")
    private String projectPhase;

    @Schema(description = "成果物名称")
    @TableField("name")
    private String name;

    @Schema(description = "成果物类型")
    @TableField("type")
    private String type;

    @Schema(description = "成果物类型标签")
    @TableField("type_label")
    private String typeLabel;

    @Schema(description = "成果物描述")
    @TableField("description")
    private String description;

    @Schema(description = "提交者")
    @TableField("submitter")
    private String submitter;

    @Schema(description = "提交者部门")
    @TableField("submitter_dept")
    private String submitterDept;

    @Schema(description = "提交时间")
    @TableField("submitted_at")
    private LocalDateTime submittedAt;

    @Schema(description = "同步时间")
    @TableField("synced_at")
    private LocalDateTime syncedAt;

    @Schema(description = "数据来源")
    @TableField("source")
    private String source;

    @Schema(description = "来源URL")
    @TableField("source_url")
    private String sourceUrl;

    @Schema(description = "标签JSON")
    @TableField("tags")
    private String tags;

    @Schema(description = "状态")
    @TableField("status")
    private String status;

    @Schema(description = "附件JSON")
    @TableField("attachments_json")
    private String attachmentsJson;

    @Schema(description = "上传年份")
    @TableField("upload_year")
    private Integer uploadYear;

    @Schema(description = "类别级别")
    @TableField("category_level")
    private String categoryLevel;

    @Schema(description = "项目领域")
    @TableField("project_field")
    private String projectField;
}