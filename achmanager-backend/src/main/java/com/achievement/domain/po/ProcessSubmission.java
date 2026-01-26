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
 * 过程系统项目提交物表
 * </p>
 *
 * @author system
 * @since 2026-01-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("process_submissions")
@Schema(name="ProcessSubmission对象", description="过程系统项目提交物表")
public class ProcessSubmission implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "提交物唯一标识")
    @TableId(value = "submission_id", type = IdType.INPUT)
    private Long submissionId;

    @Schema(description = "外键 - 关联申报记录")
    @TableField("application_id")
    private Long applicationId;

    @Schema(description = "提交物类型 - proposal(申报书)/application_attachment(申报附件)/contract_template(合同模板)/signed_contract(已签署合同)/deliverable_report(成果物报告)")
    @TableField("submission_type")
    private String submissionType;

    @Schema(description = "提交阶段 - application")
    @TableField("submission_stage")
    private String submissionStage;

    @Schema(description = "提交轮次")
    @TableField("submission_round")
    private Integer submissionRound;

    @Schema(description = "版本号")
    @TableField("submission_version")
    private Integer submissionVersion;

    // 项目基本信息
    @Schema(description = "项目名称")
    @TableField("project_name")
    private String projectName;

    @Schema(description = "项目所属领域")
    @TableField("project_field")
    private String projectField;

    @Schema(description = "类别级别 - 重点/一般")
    @TableField("category_level")
    private String categoryLevel;

    @Schema(description = "具体分类")
    @TableField("category_specific")
    private String categorySpecific;

    @Schema(description = "研究周期(月)")
    @TableField("research_period")
    private Integer researchPeriod;

    @Schema(description = "项目关键词")
    @TableField("project_keywords")
    private String projectKeywords;

    // 项目内容信息
    @Schema(description = "项目描述")
    @TableField("project_description")
    private String projectDescription;

    @Schema(description = "预期成果")
    @TableField("expected_results")
    private String expectedResults;

    @Schema(description = "是否愿意调整为一般项目")
    @TableField("willing_adjust")
    private String willingAdjust;

    // 申报人信息
    @Schema(description = "负责人姓名")
    @TableField("applicant_name")
    private String applicantName;

    @Schema(description = "证件号码")
    @TableField("id_card")
    private String idCard;

    @Schema(description = "学历学位")
    @TableField("education_degree")
    private String educationDegree;

    @Schema(description = "技术职称")
    @TableField("technical_title")
    private String technicalTitle;

    @Schema(description = "邮箱")
    @TableField("email")
    private String email;

    @Schema(description = "联系电话")
    @TableField("phone")
    private String phone;

    @Schema(description = "工作单位")
    @TableField("work_unit")
    private String workUnit;

    @Schema(description = "单位地址")
    @TableField("unit_address")
    private String unitAddress;

    @Schema(description = "申报人代表成果")
    @TableField("representative_achievements")
    private String representativeAchievements;

    // 文件信息
    @Schema(description = "申报书文件ID")
    @TableField("proposal_file_id")
    private String proposalFileId;

    @Schema(description = "申报书文件名称")
    @TableField("proposal_file_name")
    private String proposalFileName;

    @Schema(description = "申报书文件大小(字节)")
    @TableField("proposal_file_size")
    private Long proposalFileSize;

    @Schema(description = "文件类型")
    @TableField("proposal_file_type")
    private String proposalFileType;

    @Schema(description = "申报书访问路径")
    @TableField("proposal_file_url")
    private String proposalFileUrl;

    @Schema(description = "其他附件列表(JSON格式)")
    @TableField("other_attachments_json")
    private String otherAttachmentsJson;

    // 上传信息
    @Schema(description = "上传者用户ID")
    @TableField("uploader_id")
    private String uploaderId;

    @Schema(description = "上传者名称")
    @TableField("uploader_name")
    private String uploaderName;

    @Schema(description = "上传时间")
    @TableField("upload_time")
    private LocalDateTime uploadTime;

    @Schema(description = "提交物备注说明")
    @TableField("submission_description")
    private String submissionDescription;

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

    // 同步字段
    @Schema(description = "同步时间")
    @TableField("sync_time")
    private LocalDateTime syncTime;
}