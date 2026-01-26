package com.achievement.service;

import com.achievement.domain.dto.ProcessSubmissionRequest;
import com.achievement.exception.ProcessSystemValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 过程系统数据验证服务
 * 
 * @author system
 * @since 2026-01-24
 */
@Slf4j
@Service
public class ProcessSystemValidationService {

    // 手机号正则表达式
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    
    // 邮箱正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    
    // 身份证号正则表达式
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$");
    
    // 支持的文件类型
    private static final List<String> SUPPORTED_FILE_TYPES = List.of(
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "zip", "rar"
    );
    
    // 文件大小限制 (50MB)
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;
    
    // 支持的提交物类型
    private static final List<String> SUPPORTED_SUBMISSION_TYPES = List.of(
        "proposal",              // 申报书
        "application_attachment", // 申报附件
        "contract_template",      // 合同模板
        "signed_contract",        // 已签署合同
        "deliverable_report"      // 成果物报告（包含验收材料）
    );
    
    // 支持的提交阶段
    private static final List<String> SUPPORTED_SUBMISSION_STAGES = List.of(
        "application", "review", "execution"
    );
    
    // 支持的类别级别
    private static final List<String> SUPPORTED_CATEGORY_LEVELS = List.of(
        "重点", "一般"
    );

    /**
     * 验证提交物请求数据
     * 
     * @param request 提交物请求
     * @throws ProcessSystemValidationException 验证失败时抛出异常
     */
    public void validateSubmissionRequest(ProcessSubmissionRequest request) {
        List<String> errors = new ArrayList<>();
        
        // 验证基本字段
        validateBasicFields(request, errors);
        
        // 验证项目信息
        validateProjectInfo(request.getProjectInfo(), errors);
        
        // 验证申报人信息
        validateApplicantInfo(request.getApplicantInfo(), errors);
        
        // 验证文件信息
        validateFileInfo(request.getFiles(), errors);
        
        // 验证上传信息
        validateUploadInfo(request.getUploadInfo(), errors);
        
        // 如果有验证错误，抛出异常
        if (!errors.isEmpty()) {
            String errorMessage = String.join("; ", errors);
            log.warn("提交物数据验证失败: submissionId={}, errors={}", 
                    request.getSubmissionId(), errorMessage);
            throw new ProcessSystemValidationException("数据验证失败: " + errorMessage, errors);
        }
        
        log.info("提交物数据验证通过: submissionId={}", request.getSubmissionId());
    }

    /**
     * 验证基本字段
     */
    private void validateBasicFields(ProcessSubmissionRequest request, List<String> errors) {
        // 验证提交物类型
        if (!SUPPORTED_SUBMISSION_TYPES.contains(request.getSubmissionType())) {
            errors.add("不支持的提交物类型: " + request.getSubmissionType());
        }
        
        // 验证提交阶段
        if (!SUPPORTED_SUBMISSION_STAGES.contains(request.getSubmissionStage())) {
            errors.add("不支持的提交阶段: " + request.getSubmissionStage());
        }
        
        // 验证轮次和版本号
        if (request.getSubmissionRound() != null && request.getSubmissionRound() < 1) {
            errors.add("提交轮次必须大于0");
        }
        
        if (request.getSubmissionVersion() != null && request.getSubmissionVersion() < 1) {
            errors.add("版本号必须大于0");
        }
    }

    /**
     * 验证项目信息
     */
    private void validateProjectInfo(ProcessSubmissionRequest.ProjectInfo projectInfo, List<String> errors) {
        if (projectInfo == null) {
            errors.add("项目信息不能为空");
            return;
        }
        
        // 验证项目名称长度
        if (StringUtils.hasText(projectInfo.getProjectName()) && 
            projectInfo.getProjectName().length() > 200) {
            errors.add("项目名称长度不能超过200个字符");
        }
        
        // 验证类别级别
        if (!SUPPORTED_CATEGORY_LEVELS.contains(projectInfo.getCategoryLevel())) {
            errors.add("不支持的类别级别: " + projectInfo.getCategoryLevel());
        }
        
        // 验证研究周期
        if (projectInfo.getResearchPeriod() != null && 
            (projectInfo.getResearchPeriod() < 1 || projectInfo.getResearchPeriod() > 120)) {
            errors.add("研究周期必须在1-120个月之间");
        }
        
        // 验证项目描述长度
        if (StringUtils.hasText(projectInfo.getProjectDescription()) && 
            projectInfo.getProjectDescription().length() > 5000) {
            errors.add("项目描述长度不能超过5000个字符");
        }
        
        // 验证是否愿意调整字段
        if (StringUtils.hasText(projectInfo.getWillingAdjust()) && 
            !List.of("Y", "N").contains(projectInfo.getWillingAdjust())) {
            errors.add("是否愿意调整字段只能是Y或N");
        }
    }

    /**
     * 验证申报人信息
     */
    private void validateApplicantInfo(ProcessSubmissionRequest.ApplicantInfo applicantInfo, List<String> errors) {
        if (applicantInfo == null) {
            errors.add("申报人信息不能为空");
            return;
        }
        
        // 验证申报人姓名长度
        if (StringUtils.hasText(applicantInfo.getApplicantName()) && 
            applicantInfo.getApplicantName().length() > 100) {
            errors.add("申报人姓名长度不能超过100个字符");
        }
        
        // 验证身份证号格式
        if (StringUtils.hasText(applicantInfo.getIdCard()) && 
            !ID_CARD_PATTERN.matcher(applicantInfo.getIdCard()).matches()) {
            errors.add("身份证号格式不正确");
        }
        
        // 验证邮箱格式
        if (StringUtils.hasText(applicantInfo.getEmail()) && 
            !EMAIL_PATTERN.matcher(applicantInfo.getEmail()).matches()) {
            errors.add("邮箱格式不正确");
        }
        
        // 验证手机号格式
        if (StringUtils.hasText(applicantInfo.getPhone()) && 
            !PHONE_PATTERN.matcher(applicantInfo.getPhone()).matches()) {
            errors.add("手机号格式不正确");
        }
        
        // 验证工作单位长度
        if (StringUtils.hasText(applicantInfo.getWorkUnit()) && 
            applicantInfo.getWorkUnit().length() > 200) {
            errors.add("工作单位名称长度不能超过200个字符");
        }
    }

    /**
     * 验证文件信息
     */
    private void validateFileInfo(ProcessSubmissionRequest.FileInfo fileInfo, List<String> errors) {
        if (fileInfo == null) {
            errors.add("文件信息不能为空");
            return;
        }
        
        // 验证申报书文件
        validateFileDetail(fileInfo.getProposalFile(), "申报书文件", errors);
        
        // 验证其他附件
        if (fileInfo.getOtherAttachments() != null) {
            for (int i = 0; i < fileInfo.getOtherAttachments().size(); i++) {
                validateFileDetail(fileInfo.getOtherAttachments().get(i), 
                        "附件" + (i + 1), errors);
            }
        }
    }

    /**
     * 验证单个文件详情
     */
    private void validateFileDetail(ProcessSubmissionRequest.FileDetail fileDetail, 
                                  String fileLabel, List<String> errors) {
        if (fileDetail == null) {
            errors.add(fileLabel + "不能为空");
            return;
        }
        
        // 验证文件名称
        if (!StringUtils.hasText(fileDetail.getFileName())) {
            errors.add(fileLabel + "名称不能为空");
        } else if (fileDetail.getFileName().length() > 200) {
            errors.add(fileLabel + "名称长度不能超过200个字符");
        }
        
        // 验证文件类型
        if (StringUtils.hasText(fileDetail.getFileType())) {
            String fileType = fileDetail.getFileType().toLowerCase();
            if (!SUPPORTED_FILE_TYPES.contains(fileType)) {
                errors.add(fileLabel + "类型不支持: " + fileDetail.getFileType());
            }
        }
        
        // 验证文件大小
        if (fileDetail.getFileSize() != null) {
            if (fileDetail.getFileSize() <= 0) {
                errors.add(fileLabel + "大小必须大于0");
            } else if (fileDetail.getFileSize() > MAX_FILE_SIZE) {
                errors.add(fileLabel + "大小不能超过50MB");
            }
        }
        
        // 验证文件ID格式
        if (StringUtils.hasText(fileDetail.getFileId()) && 
            fileDetail.getFileId().length() > 200) {
            errors.add(fileLabel + "ID长度不能超过200个字符");
        }
    }

    /**
     * 验证上传信息
     */
    private void validateUploadInfo(ProcessSubmissionRequest.UploadInfo uploadInfo, List<String> errors) {
        if (uploadInfo == null) {
            errors.add("上传信息不能为空");
            return;
        }
        
        // 验证上传者ID长度
        if (StringUtils.hasText(uploadInfo.getUploaderId()) && 
            uploadInfo.getUploaderId().length() > 64) {
            errors.add("上传者ID长度不能超过64个字符");
        }
        
        // 验证上传者名称长度
        if (StringUtils.hasText(uploadInfo.getUploaderName()) && 
            uploadInfo.getUploaderName().length() > 100) {
            errors.add("上传者名称长度不能超过100个字符");
        }
        
        // 验证提交物描述长度
        if (StringUtils.hasText(uploadInfo.getSubmissionDescription()) && 
            uploadInfo.getSubmissionDescription().length() > 1000) {
            errors.add("提交物描述长度不能超过1000个字符");
        }
    }

    /**
     * 验证查询参数
     * 
     * @param applicationId 申报ID
     * @param submissionStage 提交阶段
     * @param submissionType 提交物类型
     * @throws ProcessSystemValidationException 验证失败时抛出异常
     */
    public void validateQueryParams(Long applicationId, String submissionStage, String submissionType) {
        List<String> errors = new ArrayList<>();
        
        // 验证申报ID
        if (applicationId != null && applicationId <= 0) {
            errors.add("申报ID必须大于0");
        }
        
        // 验证提交阶段
        if (StringUtils.hasText(submissionStage) && 
            !SUPPORTED_SUBMISSION_STAGES.contains(submissionStage)) {
            errors.add("不支持的提交阶段: " + submissionStage);
        }
        
        // 验证提交物类型
        if (StringUtils.hasText(submissionType) && 
            !SUPPORTED_SUBMISSION_TYPES.contains(submissionType)) {
            errors.add("不支持的提交物类型: " + submissionType);
        }
        
        // 如果有验证错误，抛出异常
        if (!errors.isEmpty()) {
            String errorMessage = String.join("; ", errors);
            log.warn("查询参数验证失败: errors={}", errorMessage);
            throw new ProcessSystemValidationException("查询参数验证失败: " + errorMessage, errors);
        }
    }

    /**
     * 验证提交物ID
     * 
     * @param submissionId 提交物ID
     * @throws ProcessSystemValidationException 验证失败时抛出异常
     */
    public void validateSubmissionId(Long submissionId) {
        if (submissionId == null || submissionId <= 0) {
            log.warn("提交物ID验证失败: submissionId={}", submissionId);
            throw new ProcessSystemValidationException("提交物ID必须大于0", List.of("提交物ID必须大于0"));
        }
    }
}