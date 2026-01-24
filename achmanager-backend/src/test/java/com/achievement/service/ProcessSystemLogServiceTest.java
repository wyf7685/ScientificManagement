package com.achievement.service;

import com.achievement.domain.po.ProcessApiLog;
import com.achievement.mapper.ProcessApiLogMapper;
import com.achievement.service.impl.ProcessSystemLogServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 过程系统日志服务测试
 *
 * @author system
 * @since 2026-01-24
 */
@ExtendWith(MockitoExtension.class)
class ProcessSystemLogServiceTest {

    @Mock
    private ProcessApiLogMapper processApiLogMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ProcessSystemLogServiceImpl processSystemLogService;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void testLogApiAccess_Success() throws Exception {
        // 准备测试数据
        request.setMethod("POST");
        request.setRequestURI("/api/v1/process-system/submissions");
        request.setAttribute("apiKey", "test-api-key");
        request.setAttribute("startTime", System.currentTimeMillis() - 100);
        request.addHeader("User-Agent", "Test-Agent");
        request.setRemoteAddr("127.0.0.1");
        
        response.setStatus(200);

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // 执行测试
        processSystemLogService.logApiAccess(request, response, null);

        // 等待异步执行完成
        Thread.sleep(100);

        // 验证结果
        ArgumentCaptor<ProcessApiLog> logCaptor = ArgumentCaptor.forClass(ProcessApiLog.class);
        verify(processApiLogMapper, times(1)).insert(logCaptor.capture());

        ProcessApiLog capturedLog = logCaptor.getValue();
        assertNotNull(capturedLog);
        assertEquals("POST", capturedLog.getMethod());
        assertTrue(capturedLog.getUrl().contains("/api/v1/process-system/submissions"));
        assertEquals("test-api-key", capturedLog.getApiKey());
        assertEquals(200, capturedLog.getResponseCode());
        assertEquals("127.0.0.1", capturedLog.getClientIp());
        assertEquals("Test-Agent", capturedLog.getUserAgent());
        assertEquals("STORE_SUBMISSION", capturedLog.getOperationType());
        assertEquals("success", capturedLog.getOperationResult());
        assertNotNull(capturedLog.getRequestId());
        assertNotNull(capturedLog.getCreatedAt());
    }

    @Test
    void testLogApiAccess_WithError() throws Exception {
        // 准备测试数据
        request.setMethod("GET");
        request.setRequestURI("/api/v1/process-system/submissions/123");
        request.setAttribute("apiKey", "test-api-key");
        request.setAttribute("startTime", System.currentTimeMillis() - 200);
        
        response.setStatus(404);
        Exception testException = new RuntimeException("Test error message");

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // 执行测试
        processSystemLogService.logApiAccess(request, response, testException);

        // 等待异步执行完成
        Thread.sleep(100);

        // 验证结果
        ArgumentCaptor<ProcessApiLog> logCaptor = ArgumentCaptor.forClass(ProcessApiLog.class);
        verify(processApiLogMapper, times(1)).insert(logCaptor.capture());

        ProcessApiLog capturedLog = logCaptor.getValue();
        assertNotNull(capturedLog);
        assertEquals("GET", capturedLog.getMethod());
        assertEquals(404, capturedLog.getResponseCode());
        assertEquals("GET_SUBMISSION_DETAIL", capturedLog.getOperationType());
        assertEquals("failed", capturedLog.getOperationResult());
        assertEquals("Test error message", capturedLog.getErrorMessage());
    }

    @Test
    void testLogOperation_Success() throws Exception {
        // 执行测试
        processSystemLogService.logOperation("TEST_OPERATION", "Test operation details", true);

        // 等待异步执行完成
        Thread.sleep(100);

        // 验证结果
        ArgumentCaptor<ProcessApiLog> logCaptor = ArgumentCaptor.forClass(ProcessApiLog.class);
        verify(processApiLogMapper, times(1)).insert(logCaptor.capture());

        ProcessApiLog capturedLog = logCaptor.getValue();
        assertNotNull(capturedLog);
        assertEquals("OPERATION", capturedLog.getMethod());
        assertEquals("/internal/operation", capturedLog.getUrl());
        assertEquals("SYSTEM", capturedLog.getApiKey());
        assertEquals(200, capturedLog.getResponseCode());
        assertEquals("TEST_OPERATION", capturedLog.getOperationType());
        assertEquals("success", capturedLog.getOperationResult());
        assertEquals("Test operation details", capturedLog.getRequestBody());
        assertEquals("操作成功", capturedLog.getResponseBody());
    }

    @Test
    void testLogOperation_Failed() throws Exception {
        // 执行测试
        processSystemLogService.logOperation("TEST_OPERATION", "Test operation failed", false);

        // 等待异步执行完成
        Thread.sleep(100);

        // 验证结果
        ArgumentCaptor<ProcessApiLog> logCaptor = ArgumentCaptor.forClass(ProcessApiLog.class);
        verify(processApiLogMapper, times(1)).insert(logCaptor.capture());

        ProcessApiLog capturedLog = logCaptor.getValue();
        assertNotNull(capturedLog);
        assertEquals("OPERATION", capturedLog.getMethod());
        assertEquals(500, capturedLog.getResponseCode());
        assertEquals("TEST_OPERATION", capturedLog.getOperationType());
        assertEquals("failed", capturedLog.getOperationResult());
        assertEquals("Test operation failed", capturedLog.getRequestBody());
        assertEquals("操作失败", capturedLog.getResponseBody());
    }

    @Test
    void testDetermineOperationType() throws Exception {
        // 测试不同URL的操作类型判断
        request.setMethod("POST");
        request.setRequestURI("/api/v1/process-system/submissions");
        request.setAttribute("apiKey", "test-key");
        request.setAttribute("startTime", System.currentTimeMillis());
        response.setStatus(200);

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        processSystemLogService.logApiAccess(request, response, null);
        Thread.sleep(100);

        ArgumentCaptor<ProcessApiLog> logCaptor = ArgumentCaptor.forClass(ProcessApiLog.class);
        verify(processApiLogMapper).insert(logCaptor.capture());
        assertEquals("STORE_SUBMISSION", logCaptor.getValue().getOperationType());

        // 重置mock
        reset(processApiLogMapper);

        // 测试GET请求
        request.setMethod("GET");
        request.setRequestURI("/api/v1/process-system/submissions");
        processSystemLogService.logApiAccess(request, response, null);
        Thread.sleep(100);

        verify(processApiLogMapper).insert(logCaptor.capture());
        assertEquals("QUERY_SUBMISSIONS", logCaptor.getValue().getOperationType());
    }
}