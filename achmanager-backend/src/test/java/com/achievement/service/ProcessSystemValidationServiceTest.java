package com.achievement.service;

import com.achievement.domain.dto.ProcessSubmissionRequest;
import com.achievement.exception.ProcessSystemValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 过程系统验证服务测试
 */
class ProcessSystemValidationServiceTest {

    private ProcessSystemValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ProcessSystemValidationService();
    }

    @Test
    void testValidateSubmissionRequest_ValidData_ShouldPass() {
        // 准备有效的请求数据
        ProcessSubmissionRequest request = createValidRequest();
        
        // 验证应该通过，不抛出异常
        assertDoesNotThrow(() -> validationService.validateSubmissionRequest(request));
    }

    @Test
    void testValidateSubmissionRequest_InvalidSubmissionType_ShouldFail() {
        // 准备无效的提交物类型
        ProcessSubmissionRequest request = createValidRequest();
        request.setSubmissionType("invalid_type");
        
        // 验证应该失败
        ProcessSystemValidationException exception = assertThrows(
                ProcessSystemValidationException.class,
                () -> validationService.validateSubmissionRequest(request)
        );
        
        assertTrue(exception.getMessage().contains("不支持的提交物类型"));
        assertTrue(exception.getValidationErrors().stream()
                .anyMatch(error -> error.contains("不支持的提交物类型")));
    }

    @Test
    void testValidateSubmissionRequest_InvalidPhone_ShouldFail() {
        // 准备无效的手机号
        ProcessSubmissionRequest request = createValidRequest();
        request.getApplicantInfo().setPhone("invalid_phone");
        
        // 验证应该失败
        ProcessSystemValidationException exception = assertThrows(
                ProcessSystemValidationException.class,
                () -> validationService.validateSubmissionRequest(request)
        );
        
        assertTrue(exception.getMessage().contains("手机号格式不正确"));
    }

    @Test
    void testValidateSubmissionRequest_InvalidEmail_ShouldFail() {
        // 准备无效的邮箱
        ProcessSubmissionRequest request = createValidRequest();
        request.getApplicantInfo().setEmail("invalid_email");
        
        // 验证应该失败
        ProcessSystemValidationException exception = assertThrows(
                ProcessSystemValidationException.class,
                () -> validationService.validateSubmissionRequest(request)
        );
        
        assertTrue(exception.getMessage().contains("邮箱格式不正确"));
    }

    @Test
    void testValidateSubmissionRequest_FileSizeExceeded_ShouldFail() {
        // 准备文件大小超限的数据
        ProcessSubmissionRequest request = createValidRequest();
        request.getFiles().getProposalFile().setFileSize(100L * 1024 * 1024); // 100MB
        
        // 验证应该失败
        ProcessSystemValidationException exception = assertThrows(
                ProcessSystemValidationException.class,
                () -> validationService.validateSubmissionRequest(request)
        );
        
        assertTrue(exception.getMessage().contains("大小不能超过50MB"));
    }

    @Test
    void testValidateQueryParams_ValidParams_ShouldPass() {
        // 验证有效的查询参数
        assertDoesNotThrow(() -> validationService.validateQueryParams(12345L, "application", "proposal"));
    }

    @Test
    void testValidateQueryParams_InvalidApplicationId_ShouldFail() {
        // 验证无效的申报ID
        ProcessSystemValidationException exception = assertThrows(
                ProcessSystemValidationException.class,
                () -> validationService.validateQueryParams(-1L, "application", "proposal")
        );
        
        assertTrue(exception.getMessage().contains("申报ID必须大于0"));
    }

    @Test
    void testValidateSubmissionId_ValidId_ShouldPass() {
        // 验证有效的提交物ID
        assertDoesNotThrow(() -> validationService.validateSubmissionId(12345L));
    }

    @Test
    void testValidateSubmissionId_InvalidId_ShouldFail() {
        // 验证无效的提交物ID
        ProcessSystemValidationException exception = assertThrows(
                ProcessSystemValidationException.class,
                () -> validationService.validateSubmissionId(null)
        );
        
        assertTrue(exception.getMessage().contains("提交物ID必须大于0"));
    }

    /**
     * 创建有效的请求数据
     */
    private ProcessSubmissionRequest createValidRequest() {
        ProcessSubmissionRequest request = new ProcessSubmissionRequest();
        request.setSubmissionId(12345L);
        request.setApplicationId(67890L);
        request.setSubmissionType("proposal");
        request.setSubmissionStage("application");
        request.setSubmissionRound(1);
        request.setSubmissionVersion(1);

        // 项目信息
        ProcessSubmissionRequest.ProjectInfo projectInfo = new ProcessSubmissionRequest.ProjectInfo();
        projectInfo.setProjectName("测试项目");
        projectInfo.setProjectField("人工智能");
        projectInfo.setCategoryLevel("重点");
        projectInfo.setCategorySpecific("智能交通");
        projectInfo.setResearchPeriod(24);
        projectInfo.setProjectKeywords("AI,交通");
        projectInfo.setProjectDescription("这是一个测试项目描述");
        projectInfo.setExpectedResults("预期成果");
        projectInfo.setWillingAdjust("Y");
        request.setProjectInfo(projectInfo);

        // 申报人信息
        ProcessSubmissionRequest.ApplicantInfo applicantInfo = new ProcessSubmissionRequest.ApplicantInfo();
        applicantInfo.setApplicantName("张三");
        applicantInfo.setIdCard("110101199001011234");
        applicantInfo.setEducationDegree("博士");
        applicantInfo.setTechnicalTitle("高级工程师");
        applicantInfo.setEmail("zhangsan@example.com");
        applicantInfo.setPhone("13800138000");
        applicantInfo.setWorkUnit("某某大学");
        applicantInfo.setUnitAddress("北京市海淀区");
        applicantInfo.setRepresentativeAchievements("主要成果");
        request.setApplicantInfo(applicantInfo);

        // 文件信息
        ProcessSubmissionRequest.FileInfo fileInfo = new ProcessSubmissionRequest.FileInfo();
        
        ProcessSubmissionRequest.FileDetail proposalFile = new ProcessSubmissionRequest.FileDetail();
        proposalFile.setFileId("file_001");
        proposalFile.setFileName("申报书.pdf");
        proposalFile.setFileSize(2048576L);
        proposalFile.setFileType("pdf");
        proposalFile.setFileUrl("/uploads/proposals/file_001.pdf");
        fileInfo.setProposalFile(proposalFile);

        ProcessSubmissionRequest.FileDetail attachment = new ProcessSubmissionRequest.FileDetail();
        attachment.setFileId("file_002");
        attachment.setFileName("附件1.docx");
        attachment.setFileSize(1024000L);
        attachment.setFileType("docx");
        attachment.setFileUrl("/uploads/attachments/file_002.docx");
        fileInfo.setOtherAttachments(List.of(attachment));
        
        request.setFiles(fileInfo);

        // 上传信息
        ProcessSubmissionRequest.UploadInfo uploadInfo = new ProcessSubmissionRequest.UploadInfo();
        uploadInfo.setUploaderId("user_123");
        uploadInfo.setUploaderName("李四");
        uploadInfo.setSubmissionDescription("首次提交申报书");
        request.setUploadInfo(uploadInfo);

        return request;
    }
}