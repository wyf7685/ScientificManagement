package com.achievement.domain.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 过程系统提交物存储请求DTO
 */
@Data
@Schema(description = "过程系统提交物存储请求")
public class ProcessSubmissionRequest {

    @Schema(description = "提交物ID")
    @NotNull(message = "提交物ID不能为空")
    @JsonAlias("submission_id")
    private Long submissionId;

    @Schema(description = "申报ID")
    @NotNull(message = "申报ID不能为空")
    @JsonAlias("application_id")
    private Long applicationId;

    @Schema(description = "提交物类型")
    @NotBlank(message = "提交物类型不能为空")
    @JsonAlias("submission_type")
    private String submissionType;

    @Schema(description = "提交阶段")
    @NotBlank(message = "提交阶段不能为空")
    @JsonAlias("submission_stage")
    private String submissionStage;

    @Schema(description = "提交轮次")
    @JsonAlias("submission_round")
    private Integer submissionRound = 1;

    @Schema(description = "版本号")
    @JsonAlias("submission_version")
    private Integer submissionVersion = 1;

    @Schema(description = "项目信息")
    @Valid
    @NotNull(message = "项目信息不能为空")
    @JsonAlias("project_info")
    private ProjectInfo projectInfo;

    @Schema(description = "申报人信息")
    @Valid
    @NotNull(message = "申报人信息不能为空")
    @JsonAlias("applicant_info")
    private ApplicantInfo applicantInfo;

    @Schema(description = "文件信息")
    @Valid
    @NotNull(message = "文件信息不能为空")
    @JsonAlias("files")
    private FileInfo files;

    @Schema(description = "上传信息")
    @Valid
    @NotNull(message = "上传信息不能为空")
    @JsonAlias("upload_info")
    private UploadInfo uploadInfo;

    @Data
    @Schema(description = "项目信息")
    public static class ProjectInfo {
        @Schema(description = "项目名称")
        @NotBlank(message = "项目名称不能为空")
        @JsonAlias("project_name")
        private String projectName;

        @Schema(description = "项目所属领域")
        @JsonAlias("project_field")
        private String projectField;

        @Schema(description = "类别级别")
        @NotBlank(message = "类别级别不能为空")
        @JsonAlias("category_level")
        private String categoryLevel;

        @Schema(description = "具体分类")
        @NotBlank(message = "具体分类不能为空")
        @JsonAlias("category_specific")
        private String categorySpecific;

        @Schema(description = "研究周期(月)")
        @JsonAlias("research_period")
        private Integer researchPeriod;

        @Schema(description = "项目关键词")
        @JsonAlias("project_keywords")
        private String projectKeywords;

        @Schema(description = "项目描述")
        @NotBlank(message = "项目描述不能为空")
        @JsonAlias("project_description")
        private String projectDescription;

        @Schema(description = "预期成果")
        @JsonAlias("expected_results")
        private String expectedResults;

        @Schema(description = "是否愿意调整为一般项目")
        @JsonAlias("willing_adjust")
        private String willingAdjust;
    }

    @Data
    @Schema(description = "申报人信息")
    public static class ApplicantInfo {
        @Schema(description = "负责人姓名")
        @NotBlank(message = "负责人姓名不能为空")
        @JsonAlias("applicant_name")
        private String applicantName;

        @Schema(description = "证件号码")
        @JsonAlias("id_card")
        private String idCard;

        @Schema(description = "学历学位")
        @JsonAlias("education_degree")
        private String educationDegree;

        @Schema(description = "技术职称")
        @JsonAlias("technical_title")
        private String technicalTitle;

        @Schema(description = "邮箱")
        @JsonAlias("email")
        private String email;

        @Schema(description = "联系电话")
        @NotBlank(message = "联系电话不能为空")
        @JsonAlias("phone")
        private String phone;

        @Schema(description = "工作单位")
        @JsonAlias("work_unit")
        private String workUnit;

        @Schema(description = "单位地址")
        @JsonAlias("unit_address")
        private String unitAddress;

        @Schema(description = "申报人代表成果")
        @JsonAlias("representative_achievements")
        private String representativeAchievements;
    }

    @Data
    @Schema(description = "文件信息")
    public static class FileInfo {
        @Schema(description = "申报书文件")
        @Valid
        @NotNull(message = "申报书文件不能为空")
        @JsonAlias("proposal_file")
        private FileDetail proposalFile;

        @Schema(description = "其他附件")
        @JsonAlias("other_attachments")
        private List<FileDetail> otherAttachments;
    }

    @Data
    @Schema(description = "文件详情")
    public static class FileDetail {
        @Schema(description = "文件ID")
        @NotBlank(message = "文件ID不能为空")
        @JsonAlias("file_id")
        private String fileId;

        @Schema(description = "文件名称")
        @NotBlank(message = "文件名称不能为空")
        @JsonAlias("file_name")
        private String fileName;

        @Schema(description = "文件大小")
        @JsonAlias("file_size")
        private Long fileSize;

        @Schema(description = "文件类型")
        @JsonAlias("file_type")
        private String fileType;

        @Schema(description = "文件URL")
        @JsonAlias("file_url")
        private String fileUrl;
    }

    @Data
    @Schema(description = "上传信息")
    public static class UploadInfo {
        @Schema(description = "上传者ID")
        @NotBlank(message = "上传者ID不能为空")
        @JsonAlias("uploader_id")
        private String uploaderId;

        @Schema(description = "上传者名称")
        @NotBlank(message = "上传者名称不能为空")
        @JsonAlias("uploader_name")
        private String uploaderName;

        @Schema(description = "提交物描述")
        @JsonAlias("submission_description")
        private String submissionDescription;

        @Schema(description = "上传时间")
        @JsonAlias("upload_time")
        private String uploadTime;
    }
}
