package com.achievement.exception;

import com.achievement.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 全局异常处理器测试
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/process-system/submissions");
        request.setMethod("POST");
    }

    @Test
    void testHandleProcessSystemValidationException() {
        // 准备验证异常
        List<String> errors = List.of("项目名称不能为空", "申报人姓名不能为空");
        ProcessSystemValidationException exception = new ProcessSystemValidationException(
                "数据验证失败", errors);

        // 处理异常
        Result<Map<String, Object>> result = exceptionHandler.handleProcessSystemValidationException(exception, request);

        // 验证结果
        assertEquals(10001, result.getCode());
        assertTrue(result.getMsg().contains("数据验证失败"));
        assertNotNull(result.getData());
        
        Map<String, Object> data = result.getData();
        assertTrue(data.containsKey("errors"));
        assertTrue(data.containsKey("request_id"));
        assertTrue(data.containsKey("timestamp"));
        
        @SuppressWarnings("unchecked")
        List<String> resultErrors = (List<String>) data.get("errors");
        assertEquals(2, resultErrors.size());
        assertTrue(resultErrors.contains("项目名称不能为空"));
        assertTrue(resultErrors.contains("申报人姓名不能为空"));
    }

    @Test
    void testHandleProcessSystemException() {
        // 准备业务异常
        ProcessSystemException exception = new ProcessSystemException(10008, "存储服务异常");

        // 处理异常
        Result<Map<String, Object>> result = exceptionHandler.handleProcessSystemException(exception, request);

        // 验证结果
        assertEquals(10008, result.getCode());
        assertEquals("存储服务异常", result.getMsg());
        assertNotNull(result.getData());
        
        Map<String, Object> data = result.getData();
        assertTrue(data.containsKey("request_id"));
        assertTrue(data.containsKey("timestamp"));
    }

    @Test
    void testHandleProcessSystemSecurityException() {
        // 准备安全异常
        ProcessSystemSecurityException exception = new ProcessSystemSecurityException(10002, "API密钥无效");

        // 处理异常
        Result<Map<String, Object>> result = exceptionHandler.handleProcessSystemSecurityException(exception, request);

        // 验证结果
        assertEquals(10002, result.getCode());
        assertEquals("API密钥无效", result.getMsg());
        assertNotNull(result.getData());
        
        Map<String, Object> data = result.getData();
        assertTrue(data.containsKey("request_id"));
        assertTrue(data.containsKey("timestamp"));
    }

    @Test
    void testHandleMethodArgumentNotValidException() {
        // 模拟验证异常
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        FieldError fieldError = new FieldError("processSubmissionRequest", "projectName", "项目名称不能为空");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        when(exception.getBindingResult()).thenReturn(bindingResult);

        // 处理异常
        Result<Map<String, Object>> result = exceptionHandler.handleMethodArgumentNotValidException(exception, request);

        // 验证结果
        assertEquals(10001, result.getCode());
        assertTrue(result.getMsg().contains("请求参数验证失败"));
        assertNotNull(result.getData());
        
        Map<String, Object> data = result.getData();
        assertTrue(data.containsKey("field"));
        assertTrue(data.containsKey("error"));
        assertTrue(data.containsKey("errors"));
        assertTrue(data.containsKey("request_id"));
        assertTrue(data.containsKey("timestamp"));
        
        assertEquals("projectName", data.get("field"));
        assertEquals("项目名称不能为空", data.get("error"));
    }

    @Test
    void testHandleGenericException() {
        // 准备通用异常
        Exception exception = new RuntimeException("系统内部错误");

        // 处理异常
        Result<Map<String, Object>> result = exceptionHandler.handleException(exception, request);

        // 验证结果
        assertEquals(500, result.getCode());
        assertEquals("系统内部错误", result.getMsg());
        assertNotNull(result.getData());
        
        Map<String, Object> data = result.getData();
        assertTrue(data.containsKey("request_id"));
        assertTrue(data.containsKey("timestamp"));
    }
}