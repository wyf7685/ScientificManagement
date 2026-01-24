package com.achievement.domain.po;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 过程系统实体类测试
 */
@SpringBootTest
@ActiveProfiles("test")
class ProcessSystemEntitiesTest {

    @Test
    void testProcessSubmissionEntity() {
        ProcessSubmission submission = new ProcessSubmission();
        
        // 测试基本字段设置
        submission.setSubmissionId(12345L)
                .setApplicationId(67890L)
                .setSubmissionType("proposal")
                .setSubmissionStage("application")
                .setSubmissionRound(1)
                .setSubmissionVersion(1)
                .setProjectName("测试项目")
                .setProjectField("人工智能")
                .setCategoryLevel("重点")
                .setCategorySpecific("智能交通")
                .setProjectDescription("测试项目描述")
                .setApplicantName("张三")
                .setPhone("13800138000")
                .setProposalFileId("file_001")
                .setProposalFileName("申报书.pdf")
                .setUploaderId("user_123")
                .setUploaderName("李四")
                .setUploadTime(LocalDateTime.now())
                .setCreateTime(LocalDateTime.now())
                .setUpdateTime(LocalDateTime.now())
                .setSyncTime(LocalDateTime.now())
                .setDelFlag("0");

        // 验证字段设置
        assertEquals(12345L, submission.getSubmissionId());
        assertEquals(67890L, submission.getApplicationId());
        assertEquals("proposal", submission.getSubmissionType());
        assertEquals("测试项目", submission.getProjectName());
        assertEquals("张三", submission.getApplicantName());
        assertEquals("0", submission.getDelFlag());
        
        assertNotNull(submission.getUploadTime());
        assertNotNull(submission.getCreateTime());
        assertNotNull(submission.getSyncTime());
    }

    @Test
    void testProcessSubmissionFileEntity() {
        ProcessSubmissionFile file = new ProcessSubmissionFile();
        
        // 测试基本字段设置
        file.setFileId("file_001")
                .setSubmissionId(12345L)
                .setFileName("申报书.pdf")
                .setOriginalName("申报书_原始.pdf")
                .setFileSize(2048576L)
                .setFileType("pdf")
                .setMimeType("application/pdf")
                .setFilePath("/uploads/proposals/file_001.pdf")
                .setFileUrl("http://localhost:8080/uploads/proposals/file_001.pdf")
                .setFileCategory("proposal")
                .setFileDescription("项目申报书文件")
                .setFileMd5("d41d8cd98f00b204e9800998ecf8427e")
                .setStorageStatus("completed")
                .setUploaderId("user_123")
                .setUploaderName("李四")
                .setUploadTime(LocalDateTime.now())
                .setCreateTime(LocalDateTime.now())
                .setUpdateTime(LocalDateTime.now())
                .setDelFlag("0");

        // 验证字段设置
        assertEquals("file_001", file.getFileId());
        assertEquals(12345L, file.getSubmissionId());
        assertEquals("申报书.pdf", file.getFileName());
        assertEquals(2048576L, file.getFileSize());
        assertEquals("pdf", file.getFileType());
        assertEquals("proposal", file.getFileCategory());
        assertEquals("completed", file.getStorageStatus());
        assertEquals("0", file.getDelFlag());
        
        assertNotNull(file.getUploadTime());
        assertNotNull(file.getCreateTime());
    }

    @Test
    void testProcessApiLogEntity() {
        ProcessApiLog log = new ProcessApiLog();
        
        // 测试基本字段设置
        log.setRequestId("req_123456789")
                .setApiKey("process-system-key-001")
                .setMethod("POST")
                .setUrl("/api/v1/process-system/submissions")
                .setRequestBody("{\"submission_id\":12345}")
                .setResponseCode(200)
                .setResponseBody("{\"code\":200,\"message\":\"success\"}")
                .setResponseTime(150)
                .setClientIp("192.168.1.100")
                .setUserAgent("ProcessSystemClient/1.0")
                .setRequestHeaders("{\"Content-Type\":\"application/json\"}")
                .setResponseHeaders("{\"Content-Type\":\"application/json\"}")
                .setOperationType("store_submission")
                .setOperationResult("success")
                .setCreatedAt(LocalDateTime.now());

        // 验证字段设置
        assertEquals("req_123456789", log.getRequestId());
        assertEquals("process-system-key-001", log.getApiKey());
        assertEquals("POST", log.getMethod());
        assertEquals("/api/v1/process-system/submissions", log.getUrl());
        assertEquals(200, log.getResponseCode());
        assertEquals(150, log.getResponseTime());
        assertEquals("192.168.1.100", log.getClientIp());
        assertEquals("store_submission", log.getOperationType());
        assertEquals("success", log.getOperationResult());
        
        assertNotNull(log.getCreatedAt());
    }
}