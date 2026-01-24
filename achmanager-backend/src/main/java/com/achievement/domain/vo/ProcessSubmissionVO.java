package com.achievement.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 过程系统提交物响应VO
 */
@Data
@Schema(description = "过程系统提交物响应")
public class ProcessSubmissionVO {

    @Schema(description = "提交物ID")
    private Long submissionId;

    @Schema(description = "申报ID")
    private Long applicationId;

    @Schema(description = "提交物类型")
    private String submissionType;

    @Schema(description = "提交阶段")
    private String submissionStage;

    @Schema(description = "提交轮次")
    private Integer submissionRound;

    @Schema(description = "版本号")
    private Integer submissionVersion;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "项目所属领域")
    private String projectField;

    @Schema(description = "类别级别")
    private String categoryLevel;

    @Schema(description = "具体分类")
    private String categorySpecific;

    @Schema(description = "研究周期(月)")
    private Integer researchPeriod;

    @Schema(description = "项目关键词")
    private String projectKeywords;

    @Schema(description = "项目描述")
    private String projectDescription;

    @Schema(description = "预期成果")
    private String expectedResults;

    @Schema(description = "是否愿意调整为一般项目")
    private String willingAdjust;

    @Schema(description = "负责人姓名")
    private String applicantName;

    @Schema(description = "证件号码")
    private String idCard;

    @Schema(description = "学历学位")
    private String educationDegree;

    @Schema(description = "技术职称")
    private String technicalTitle;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "工作单位")
    private String workUnit;

    @Schema(description = "单位地址")
    private String unitAddress;

    @Schema(description = "申报人代表成果")
    private String representativeAchievements;

    @Schema(description = "申报书文件")
    private FileVO proposalFile;

    @Schema(description = "其他附件")
    private List<FileVO> otherAttachments;

    @Schema(description = "上传者ID")
    private String uploaderId;

    @Schema(description = "上传者名称")
    private String uploaderName;

    @Schema(description = "上传时间")
    private LocalDateTime uploadTime;

    @Schema(description = "提交物描述")
    private String submissionDescription;

    @Schema(description = "同步时间")
    private LocalDateTime syncTime;

    @Data
    @Schema(description = "文件信息")
    public static class FileVO {
        @Schema(description = "文件ID")
        private String fileId;

        @Schema(description = "文件名称")
        private String fileName;

        @Schema(description = "文件大小")
        private Long fileSize;

        @Schema(description = "文件类型")
        private String fileType;

        @Schema(description = "文件URL")
        private String fileUrl;
    }
}