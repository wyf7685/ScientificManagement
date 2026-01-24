package com.achievement.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
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
 * 过程系统提交物文件表
 * </p>
 *
 * @author system
 * @since 2026-01-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("process_submission_files")
@Schema(name="ProcessSubmissionFile对象", description="过程系统提交物文件表")
public class ProcessSubmissionFile implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "文件唯一标识")
    @TableId(value = "file_id", type = IdType.INPUT)
    private String fileId;

    @Schema(description = "关联的提交物ID")
    @TableField("submission_id")
    private Long submissionId;

    @Schema(description = "文件名称")
    @TableField("file_name")
    private String fileName;

    @Schema(description = "文件原始名称")
    @TableField("original_name")
    private String originalName;

    @Schema(description = "文件大小(字节)")
    @TableField("file_size")
    private Long fileSize;

    @Schema(description = "文件类型/扩展名")
    @TableField("file_type")
    private String fileType;

    @Schema(description = "MIME类型")
    @TableField("mime_type")
    private String mimeType;

    @Schema(description = "文件存储路径")
    @TableField("file_path")
    private String filePath;

    @Schema(description = "文件访问URL")
    @TableField("file_url")
    private String fileUrl;

    @Schema(description = "文件分类 - proposal/attachment")
    @TableField("file_category")
    private String fileCategory;

    @Schema(description = "文件描述")
    @TableField("file_description")
    private String fileDescription;

    @Schema(description = "文件MD5校验值")
    @TableField("file_md5")
    private String fileMd5;

    @Schema(description = "存储状态 - uploaded/processing/completed/failed")
    @TableField("storage_status")
    private String storageStatus;

    @Schema(description = "上传者用户ID")
    @TableField("uploader_id")
    private String uploaderId;

    @Schema(description = "上传者名称")
    @TableField("uploader_name")
    private String uploaderName;

    @Schema(description = "上传时间")
    @TableField("upload_time")
    private LocalDateTime uploadTime;

    // 系统字段
    @Schema(description = "创建者ID")
    @TableField("create_by")
    private String createBy;

    @Schema(description = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @Schema(description = "更新者ID")
    @TableField("update_by")
    private String updateBy;

    @Schema(description = "更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;

    @Schema(description = "删除标志")
    @TableField("del_flag")
    private String delFlag;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
}