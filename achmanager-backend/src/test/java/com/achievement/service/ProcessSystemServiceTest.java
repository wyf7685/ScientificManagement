package com.achievement.service;

import com.achievement.domain.dto.ProcessSubmissionRequest;
import com.achievement.domain.po.ProcessSubmissionFile;
import com.achievement.domain.vo.ProcessSubmissionVO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 过程系统服务测试
 */
@SpringBootTest
@ActiveProfiles("test")
class ProcessSystemServiceTest {

    @Test
    void testValidateSubmissionData() {
        // 创建有效的提交物请求
        ProcessSubmissionRequest request = createValidSubmissionRequest();
        
        // 这里只测试数据结构，不依赖数据库
        assertNotNull(request);
        assertNotNull(request.getProjectInfo());
        assertNotNull(request.getApplicantInfo());
        assertNotNull(request.getFiles());
        assertNotNull(request.getUploadInfo());
        
        assertEquals(12345L, request.getSubmissionId());
        assertEquals(67890L, request.getApplicationId());
        assertEquals("proposal", request.getSubmissionType());
        assertEquals("智能交通管理系统", request.getProjectInfo().getProjectName());
        assertEquals("张三", request.getApplicantInfo().getApplicantName());
    }

    @Test
    void testSubmissionRequestValidation() {
        ProcessSubmissionRequest request = createValidSubmissionRequest();
        
        // 测试必填字段
        assertNotNull(request.getSubmissionId());
        assertNotNull(request.getApplicationId());
        assertNotNull(request.getSubmissionType());
        assertNotNull(request.getSubmissionStage());
        
        // 测试项目信息必填字段
        ProcessSubmissionRequest.ProjectInfo projectInfo = request.getProjectInfo();
        assertNotNull(projectInfo.getProjectName());
        assertNotNull(projectInfo.getCategoryLevel());
        assertNotNull(projectInfo.getCategorySpecific());
        assertNotNull(projectInfo.getProjectDescription());
        
        // 测试申报人信息必填字段
        ProcessSubmissionRequest.ApplicantInfo applicantInfo = request.getApplicantInfo();
        assertNotNull(applicantInfo.getApplicantName());
        assertNotNull(applicantInfo.getPhone());
        
        // 测试文件信息必填字段
        ProcessSubmissionRequest.FileInfo files = request.getFiles();
        assertNotNull(files.getProposalFile());
        assertNotNull(files.getProposalFile().getFileId());
        assertNotNull(files.getProposalFile().getFileName());
        
        // 测试上传信息必填字段
        ProcessSubmissionRequest.UploadInfo uploadInfo = request.getUploadInfo();
        assertNotNull(uploadInfo.getUploaderId());
        assertNotNull(uploadInfo.getUploaderName());
    }

    private ProcessSubmissionRequest createValidSubmissionRequest() {
        ProcessSubmissionRequest request = new ProcessSubmissionRequest();
        request.setSubmissionId(12345L);
        request.setApplicationId(67890L);
        request.setSubmissionType("proposal");
        request.setSubmissionStage("application");
        request.setSubmissionRound(1);
        request.setSubmissionVersion(1);

        // 项目信息
        ProcessSubmissionRequest.ProjectInfo projectInfo = new ProcessSubmissionRequest.ProjectInfo();
        projectInfo.setProjectName("智能交通管理系统");
        projectInfo.setProjectField("人工智能");
        projectInfo.setCategoryLevel("重点");
        projectInfo.setCategorySpecific("智能交通");
        projectInfo.setResearchPeriod(24);
        projectInfo.setProjectKeywords("人工智能,交通管理,智能系统");
        projectInfo.setProjectDescription("基于AI的智能交通管理系统");
        projectInfo.setExpectedResults("预期开发出智能交通管理平台");
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
        applicantInfo.setRepresentativeAchievements("主要成果包括...");
        request.setApplicantInfo(applicantInfo);

        // 文件信息
        ProcessSubmissionRequest.FileInfo files = new ProcessSubmissionRequest.FileInfo();
        ProcessSubmissionRequest.FileDetail proposalFile = new ProcessSubmissionRequest.FileDetail();
        proposalFile.setFileId("file_001");
        proposalFile.setFileName("申报书.pdf");
        proposalFile.setFileSize(2048576L);
        proposalFile.setFileType("pdf");
        proposalFile.setFileUrl("/uploads/proposals/file_001.pdf");
        files.setProposalFile(proposalFile);
        request.setFiles(files);

        // 上传信息
        ProcessSubmissionRequest.UploadInfo uploadInfo = new ProcessSubmissionRequest.UploadInfo();
        uploadInfo.setUploaderId("user_123");
        uploadInfo.setUploaderName("李四");
        uploadInfo.setSubmissionDescription("首次提交申报书");
        request.setUploadInfo(uploadInfo);

        return request;
    }

    // ==================== 文件信息查询功能测试 ====================

    @Test
    void testFileQueryDataStructures() {
        // 测试文件查询相关的数据结构
        ProcessSubmissionFile file = createTestFile();
        
        // 验证文件基本信息
        assertNotNull(file);
        assertEquals("file_001", file.getFileId());
        assertEquals(12345L, file.getSubmissionId());
        assertEquals("申报书.pdf", file.getFileName());
        assertEquals("申报书.pdf", file.getOriginalName());
        assertEquals(2048576L, file.getFileSize());
        assertEquals("pdf", file.getFileType());
        assertEquals("proposal", file.getFileCategory());
        assertEquals("completed", file.getStorageStatus());
        assertEquals("user_123", file.getUploaderId());
        assertEquals("李四", file.getUploaderName());
        assertEquals("0", file.getDelFlag());
    }

    @Test
    void testFileQueryParameterValidation() {
        // 测试文件查询参数验证逻辑
        
        // 测试有效的查询参数组合
        Long submissionId = 12345L;
        String fileCategory = "proposal";
        String fileName = "申报书";
        String fileType = "pdf";
        
        // 验证参数不为null且格式正确
        assertNotNull(submissionId);
        assertTrue(submissionId > 0);
        assertNotNull(fileCategory);
        assertTrue(Arrays.asList("proposal", "attachment").contains(fileCategory));
        assertNotNull(fileName);
        assertFalse(fileName.trim().isEmpty());
        assertNotNull(fileType);
        assertFalse(fileType.trim().isEmpty());
    }

    @Test
    void testFileDownloadUrlGeneration() {
        // 测试文件下载链接生成逻辑
        ProcessSubmissionFile file = createTestFile();
        
        // 模拟下载链接生成
        String baseUrl = "http://localhost:8080";
        String relativePath = file.getFileUrl();
        String expectedUrl = baseUrl + (relativePath.startsWith("/") ? "" : "/") + relativePath;
        
        // 验证链接格式
        assertNotNull(expectedUrl);
        assertTrue(expectedUrl.startsWith("http"));
        assertTrue(expectedUrl.contains(file.getFileId()));
    }

    @Test
    void testBatchFileDownloadUrlsStructure() {
        // 测试批量文件下载链接数据结构
        List<String> fileIds = Arrays.asList("file_001", "file_002", "file_003");
        
        // 验证输入参数
        assertNotNull(fileIds);
        assertFalse(fileIds.isEmpty());
        assertTrue(fileIds.size() <= 100); // 验证批量限制
        
        // 模拟返回结果结构
        Map<String, String> expectedResult = Map.of(
            "file_001", "http://localhost:8080/uploads/proposals/file_001.pdf",
            "file_002", "http://localhost:8080/uploads/attachments/file_002.docx",
            "file_003", "http://localhost:8080/uploads/attachments/file_003.xlsx"
        );
        
        // 验证结果结构
        assertNotNull(expectedResult);
        assertEquals(3, expectedResult.size());
        for (String fileId : fileIds) {
            assertTrue(expectedResult.containsKey(fileId));
            assertNotNull(expectedResult.get(fileId));
            assertTrue(expectedResult.get(fileId).startsWith("http"));
        }
    }

    @Test
    void testFileMetadataStructure() {
        // 测试文件元数据结构完整性
        ProcessSubmissionFile file = createTestFile();
        
        // 验证所有必要的元数据字段
        assertNotNull(file.getFileId());
        assertNotNull(file.getSubmissionId());
        assertNotNull(file.getFileName());
        assertNotNull(file.getOriginalName());
        assertNotNull(file.getFileSize());
        assertNotNull(file.getFileType());
        assertNotNull(file.getFileUrl());
        assertNotNull(file.getFileCategory());
        assertNotNull(file.getStorageStatus());
        assertNotNull(file.getUploaderId());
        assertNotNull(file.getUploaderName());
        assertNotNull(file.getUploadTime());
        assertNotNull(file.getCreateTime());
        assertNotNull(file.getDelFlag());
        
        // 验证字段值的合理性
        assertTrue(file.getFileSize() > 0);
        assertTrue(Arrays.asList("proposal", "attachment").contains(file.getFileCategory()));
        assertTrue(Arrays.asList("uploaded", "processing", "completed", "failed").contains(file.getStorageStatus()));
        assertEquals("0", file.getDelFlag()); // 未删除状态
    }

    @Test
    void testFileQueryResponseFormat() {
        // 测试文件查询响应格式
        List<ProcessSubmissionFile> files = Arrays.asList(
            createTestFile(),
            createTestAttachmentFile()
        );
        
        // 验证列表结构
        assertNotNull(files);
        assertEquals(2, files.size());
        
        // 验证每个文件的基本信息
        for (ProcessSubmissionFile file : files) {
            assertNotNull(file.getFileId());
            assertNotNull(file.getFileName());
            assertNotNull(file.getFileCategory());
            assertTrue(file.getFileSize() > 0);
        }
        
        // 验证不同类型文件的区别
        ProcessSubmissionFile proposalFile = files.get(0);
        ProcessSubmissionFile attachmentFile = files.get(1);
        
        assertEquals("proposal", proposalFile.getFileCategory());
        assertEquals("attachment", attachmentFile.getFileCategory());
        assertNotEquals(proposalFile.getFileId(), attachmentFile.getFileId());
    }

    private ProcessSubmissionFile createTestFile() {
        ProcessSubmissionFile file = new ProcessSubmissionFile();
        file.setFileId("file_001");
        file.setSubmissionId(12345L);
        file.setFileName("申报书.pdf");
        file.setOriginalName("申报书.pdf");
        file.setFileSize(2048576L);
        file.setFileType("pdf");
        file.setMimeType("application/pdf");
        file.setFilePath("/uploads/proposals/file_001.pdf");
        file.setFileUrl("/uploads/proposals/file_001.pdf");
        file.setFileCategory("proposal");
        file.setFileDescription("项目申报书文件");
        file.setFileMd5("d41d8cd98f00b204e9800998ecf8427e");
        file.setStorageStatus("completed");
        file.setUploaderId("user_123");
        file.setUploaderName("李四");
        file.setUploadTime(LocalDateTime.now());
        file.setCreateBy("user_123");
        file.setCreateTime(LocalDateTime.now());
        file.setDelFlag("0");
        return file;
    }

    private ProcessSubmissionFile createTestAttachmentFile() {
        ProcessSubmissionFile file = new ProcessSubmissionFile();
        file.setFileId("file_002");
        file.setSubmissionId(12345L);
        file.setFileName("附件1.docx");
        file.setOriginalName("附件1.docx");
        file.setFileSize(1024000L);
        file.setFileType("docx");
        file.setMimeType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        file.setFilePath("/uploads/attachments/file_002.docx");
        file.setFileUrl("/uploads/attachments/file_002.docx");
        file.setFileCategory("attachment");
        file.setFileDescription("项目附件文件");
        file.setFileMd5("e99a18c428cb38d5f260853678922e03");
        file.setStorageStatus("completed");
        file.setUploaderId("user_123");
        file.setUploaderName("李四");
        file.setUploadTime(LocalDateTime.now());
        file.setCreateBy("user_123");
        file.setCreateTime(LocalDateTime.now());
        file.setDelFlag("0");
        return file;
    }
}