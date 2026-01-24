package com.achievement.service;

import com.achievement.domain.po.ProcessSubmission;
import com.achievement.domain.vo.ProcessSubmissionVO;
import com.achievement.mapper.ProcessSubmissionMapper;
import com.achievement.service.impl.ProcessSystemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 版本查询功能测试
 * 
 * @author system
 * @since 2026-01-24
 */
@ExtendWith(MockitoExtension.class)
class ProcessSystemVersionQueryTest {

    @Mock
    private ProcessSubmissionMapper processSubmissionMapper;

    @Mock
    private ProcessSystemLogService processSystemLogService;

    @InjectMocks
    private ProcessSystemServiceImpl processSystemService;

    private ProcessSubmission mockSubmission1;
    private ProcessSubmission mockSubmission2;

    @BeforeEach
    void setUp() {
        // 创建测试数据
        mockSubmission1 = createMockSubmission(12345L, 67890L, "proposal", "application", 1, 1);
        mockSubmission2 = createMockSubmission(12346L, 67890L, "proposal", "application", 1, 2);
    }

    @Test
    void testGetSubmissionVersionHistory() {
        // 准备测试数据
        Long applicationId = 67890L;
        String submissionType = "proposal";
        String submissionStage = "application";
        Integer submissionRound = 1;
        
        List<ProcessSubmission> mockSubmissions = Arrays.asList(mockSubmission2, mockSubmission1);
        
        // 模拟mapper调用
        when(processSubmissionMapper.selectVersionHistory(applicationId, submissionType, submissionStage, submissionRound))
                .thenReturn(mockSubmissions);
        
        // 执行测试
        List<ProcessSubmissionVO> result = processSystemService.getSubmissionVersionHistory(
                applicationId, submissionType, submissionStage, submissionRound);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(12346L, result.get(0).getSubmissionId()); // 版本2在前
        assertEquals(12345L, result.get(1).getSubmissionId()); // 版本1在后
        
        // 验证mapper调用
        verify(processSubmissionMapper).selectVersionHistory(applicationId, submissionType, submissionStage, submissionRound);
        verify(processSystemLogService).logOperation(eq("QUERY_VERSION_HISTORY"), anyString(), eq(true));
    }

    @Test
    void testGetSubmissionRoundHistory() {
        // 准备测试数据
        Long applicationId = 67890L;
        String submissionType = "proposal";
        String submissionStage = "application";
        
        List<ProcessSubmission> mockSubmissions = Arrays.asList(mockSubmission1, mockSubmission2);
        
        // 模拟mapper调用
        when(processSubmissionMapper.selectRoundHistory(applicationId, submissionType, submissionStage))
                .thenReturn(mockSubmissions);
        
        // 执行测试
        List<ProcessSubmissionVO> result = processSystemService.getSubmissionRoundHistory(
                applicationId, submissionType, submissionStage);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // 验证mapper调用
        verify(processSubmissionMapper).selectRoundHistory(applicationId, submissionType, submissionStage);
        verify(processSystemLogService).logOperation(eq("QUERY_ROUND_HISTORY"), anyString(), eq(true));
    }

    @Test
    void testGetSubmissionByVersion() {
        // 准备测试数据
        Long applicationId = 67890L;
        String submissionType = "proposal";
        String submissionStage = "application";
        Integer submissionRound = 1;
        Integer submissionVersion = 1;
        
        // 模拟mapper调用
        when(processSubmissionMapper.selectByVersion(applicationId, submissionType, submissionStage, submissionRound, submissionVersion))
                .thenReturn(mockSubmission1);
        
        // 执行测试
        ProcessSubmissionVO result = processSystemService.getSubmissionByVersion(
                applicationId, submissionType, submissionStage, submissionRound, submissionVersion);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(12345L, result.getSubmissionId());
        assertEquals("智能交通管理系统", result.getProjectName());
        
        // 验证mapper调用
        verify(processSubmissionMapper).selectByVersion(applicationId, submissionType, submissionStage, submissionRound, submissionVersion);
        verify(processSystemLogService).logOperation(eq("GET_SUBMISSION_BY_VERSION"), anyString(), eq(true));
    }

    @Test
    void testGetSubmissionByVersionNotFound() {
        // 准备测试数据
        Long applicationId = 67890L;
        String submissionType = "proposal";
        String submissionStage = "application";
        Integer submissionRound = 1;
        Integer submissionVersion = 999;
        
        // 模拟mapper调用返回null
        when(processSubmissionMapper.selectByVersion(applicationId, submissionType, submissionStage, submissionRound, submissionVersion))
                .thenReturn(null);
        
        // 执行测试并验证异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            processSystemService.getSubmissionByVersion(
                    applicationId, submissionType, submissionStage, submissionRound, submissionVersion);
        });
        
        assertEquals("指定版本的提交物不存在", exception.getMessage());
        
        // 验证mapper调用
        verify(processSubmissionMapper).selectByVersion(applicationId, submissionType, submissionStage, submissionRound, submissionVersion);
        verify(processSystemLogService).logOperation(eq("GET_SUBMISSION_BY_VERSION"), anyString(), eq(false));
    }

    @Test
    void testGetSubmissionFullHistory() {
        // 准备测试数据
        Long applicationId = 67890L;
        String submissionType = "proposal";
        String submissionStage = "application";
        
        List<ProcessSubmission> mockSubmissions = Arrays.asList(mockSubmission2, mockSubmission1);
        
        // 模拟mapper调用
        when(processSubmissionMapper.selectFullHistory(applicationId, submissionType, submissionStage))
                .thenReturn(mockSubmissions);
        
        // 执行测试
        List<ProcessSubmissionVO> result = processSystemService.getSubmissionFullHistory(
                applicationId, submissionType, submissionStage);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // 验证mapper调用
        verify(processSubmissionMapper).selectFullHistory(applicationId, submissionType, submissionStage);
        verify(processSystemLogService).logOperation(eq("QUERY_FULL_HISTORY"), anyString(), eq(true));
    }

    /**
     * 创建模拟提交物对象
     */
    private ProcessSubmission createMockSubmission(Long submissionId, Long applicationId, 
                                                 String submissionType, String submissionStage, 
                                                 Integer submissionRound, Integer submissionVersion) {
        ProcessSubmission submission = new ProcessSubmission();
        submission.setSubmissionId(submissionId);
        submission.setApplicationId(applicationId);
        submission.setSubmissionType(submissionType);
        submission.setSubmissionStage(submissionStage);
        submission.setSubmissionRound(submissionRound);
        submission.setSubmissionVersion(submissionVersion);
        submission.setProjectName("智能交通管理系统");
        submission.setProjectField("人工智能");
        submission.setCategoryLevel("重点");
        submission.setCategorySpecific("智能交通");
        submission.setProjectDescription("基于AI的智能交通管理系统");
        submission.setApplicantName("张三");
        submission.setPhone("13800138000");
        submission.setWorkUnit("某某大学");
        submission.setProposalFileId("file_001");
        submission.setProposalFileName("申报书.pdf");
        submission.setProposalFileSize(2048576L);
        submission.setProposalFileType("pdf");
        submission.setProposalFileUrl("/uploads/proposals/file_001.pdf");
        submission.setOtherAttachmentsJson("[]");
        submission.setUploaderId("user_123");
        submission.setUploaderName("李四");
        submission.setUploadTime(LocalDateTime.now());
        submission.setSubmissionDescription("测试提交");
        submission.setCreateBy("user_123");
        submission.setCreateTime(LocalDateTime.now());
        submission.setDelFlag("0");
        submission.setSyncTime(LocalDateTime.now());
        return submission;
    }
}