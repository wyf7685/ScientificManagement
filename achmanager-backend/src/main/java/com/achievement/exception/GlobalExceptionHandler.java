package com.achievement.exception;

import com.achievement.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 
 * @author system
 * @since 2026-01-24
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    /**
     * 处理过程系统验证异常
     */
    @ExceptionHandler(ProcessSystemValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, Object>> handleProcessSystemValidationException(
            ProcessSystemValidationException ex, HttpServletRequest request) {
        
        log.warn("过程系统数据验证失败: uri={}, errors={}", request.getRequestURI(), ex.getValidationErrors());
        
        Map<String, Object> details = new HashMap<>();
        details.put("errors", ex.getValidationErrors());
        details.put("request_id", generateRequestId(request));
        details.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        
        return createErrorResult(10001, ex.getMessage(), details);
    }

    /**
     * 处理过程系统业务异常
     */
    @ExceptionHandler(ProcessSystemException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Map<String, Object>> handleProcessSystemException(
            ProcessSystemException ex, HttpServletRequest request) {
        
        log.error("过程系统业务异常: uri={}, message={}", request.getRequestURI(), ex.getMessage(), ex);
        
        Map<String, Object> details = new HashMap<>();
        details.put("request_id", generateRequestId(request));
        details.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        
        Integer errorCode = ex.getErrorCode() != null ? ex.getErrorCode() : 10008;
        return createErrorResult(errorCode, ex.getMessage(), details);
    }

    /**
     * 处理过程系统安全异常
     */
    @ExceptionHandler(ProcessSystemSecurityException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Map<String, Object>> handleProcessSystemSecurityException(
            ProcessSystemSecurityException ex, HttpServletRequest request) {
        
        log.warn("过程系统安全异常: uri={}, clientIp={}, message={}", 
                request.getRequestURI(), getClientIpAddress(request), ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        details.put("request_id", generateRequestId(request));
        details.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        
        return createErrorResult(ex.getErrorCode(), ex.getMessage(), details);
    }

    /**
     * 处理Bean验证异常 (@Valid注解验证失败)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, Object>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        
        log.warn("请求参数验证失败: uri={}, errors={}", request.getRequestURI(), errors);
        
        Map<String, Object> details = new HashMap<>();
        details.put("field", ex.getBindingResult().getFieldErrors().get(0).getField());
        details.put("error", ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage());
        details.put("errors", errors);
        details.put("request_id", generateRequestId(request));
        details.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        
        return createErrorResult(10001, "请求参数验证失败", details);
    }

    /**
     * 处理Bean绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, Object>> handleBindException(
            BindException ex, HttpServletRequest request) {
        
        List<String> errors = ex.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        
        log.warn("请求参数绑定失败: uri={}, errors={}", request.getRequestURI(), errors);
        
        Map<String, Object> details = new HashMap<>();
        details.put("errors", errors);
        details.put("request_id", generateRequestId(request));
        details.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        
        return createErrorResult(10001, "请求参数绑定失败", details);
    }

    /**
     * 处理约束违反异常 (@Validated注解验证失败)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, Object>> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        
        List<String> errors = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
        
        log.warn("约束验证失败: uri={}, errors={}", request.getRequestURI(), errors);
        
        Map<String, Object> details = new HashMap<>();
        details.put("errors", errors);
        details.put("request_id", generateRequestId(request));
        details.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        
        return createErrorResult(10001, "约束验证失败", details);
    }

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, Object>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        
        log.warn("缺少请求参数: uri={}, parameter={}", request.getRequestURI(), ex.getParameterName());
        
        Map<String, Object> details = new HashMap<>();
        details.put("field", ex.getParameterName());
        details.put("error", "缺少必需的请求参数: " + ex.getParameterName());
        details.put("request_id", generateRequestId(request));
        details.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        
        return createErrorResult(10001, "缺少必需的请求参数: " + ex.getParameterName(), details);
    }

    /**
     * 处理方法参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, Object>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        log.warn("参数类型不匹配: uri={}, parameter={}, value={}", 
                request.getRequestURI(), ex.getName(), ex.getValue());
        
        Map<String, Object> details = new HashMap<>();
        details.put("field", ex.getName());
        details.put("error", "参数类型不匹配，期望类型: " + ex.getRequiredType().getSimpleName());
        details.put("request_id", generateRequestId(request));
        details.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        
        return createErrorResult(10001, "参数类型不匹配", details);
    }

    /**
     * 处理HTTP消息不可读异常 (JSON格式错误)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, Object>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        
        log.warn("HTTP消息不可读: uri={}, message={}", request.getRequestURI(), ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        details.put("error", "请求体格式错误，请检查JSON格式");
        details.put("request_id", generateRequestId(request));
        details.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        
        return createErrorResult(10001, "请求体格式错误", details);
    }

    /**
     * 处理不支持的HTTP方法异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Map<String, Object>> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        
        log.warn("不支持的HTTP方法: uri={}, method={}", request.getRequestURI(), request.getMethod());
        
        Map<String, Object> details = new HashMap<>();
        details.put("method", request.getMethod());
        details.put("supported_methods", ex.getSupportedMethods());
        details.put("request_id", generateRequestId(request));
        details.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        
        return createErrorResult(405, "不支持的HTTP方法: " + request.getMethod(), details);
    }

    /**
     * 处理不支持的媒体类型异常
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public Result<Map<String, Object>> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        
        log.warn("不支持的媒体类型: uri={}, contentType={}", 
                request.getRequestURI(), request.getContentType());
        
        Map<String, Object> details = new HashMap<>();
        details.put("content_type", request.getContentType());
        details.put("supported_types", ex.getSupportedMediaTypes());
        details.put("request_id", generateRequestId(request));
        details.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        
        return createErrorResult(415, "不支持的媒体类型", details);
    }

    /**
     * 处理文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public Result<Map<String, Object>> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        
        log.warn("文件上传大小超限: uri={}, maxSize={}", request.getRequestURI(), ex.getMaxUploadSize());
        
        Map<String, Object> details = new HashMap<>();
        details.put("max_size", ex.getMaxUploadSize());
        details.put("request_id", generateRequestId(request));
        details.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        
        return createErrorResult(10006, "文件大小超限", details);
    }

    /**
     * 处理404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Map<String, Object>> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpServletRequest request) {
        
        log.warn("资源不存在: uri={}, method={}", request.getRequestURI(), request.getMethod());
        
        Map<String, Object> details = new HashMap<>();
        details.put("path", request.getRequestURI());
        details.put("method", request.getMethod());
        details.put("request_id", generateRequestId(request));
        details.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        
        return createErrorResult(10005, "资源不存在", details);
    }

    /**
     * 处理数据库访问异常
     */
    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Map<String, Object>> handleDataAccessException(
            DataAccessException ex, HttpServletRequest request) {
        
        log.error("数据库访问异常: uri={}, message={}", request.getRequestURI(), ex.getMessage(), ex);
        
        Map<String, Object> details = new HashMap<>();
        details.put("request_id", generateRequestId(request));
        details.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        
        return createErrorResult(10009, "数据库异常", details);
    }

    /**
     * 处理其他未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Map<String, Object>> handleException(Exception ex, HttpServletRequest request) {
        
        log.error("系统异常: uri={}, message={}", request.getRequestURI(), ex.getMessage(), ex);
        
        Map<String, Object> details = new HashMap<>();
        details.put("request_id", generateRequestId(request));
        details.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        
        return createErrorResult(500, "系统内部错误", details);
    }

    /**
     * 创建错误结果
     */
    private Result<Map<String, Object>> createErrorResult(Integer code, String message, Map<String, Object> details) {
        Result<Map<String, Object>> result = Result.error(code, message);
        result.setData(details);
        return result;
    }

    /**
     * 生成请求ID
     */
    private String generateRequestId(HttpServletRequest request) {
        return "req_" + System.currentTimeMillis() + "_" + Math.abs(request.hashCode());
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}