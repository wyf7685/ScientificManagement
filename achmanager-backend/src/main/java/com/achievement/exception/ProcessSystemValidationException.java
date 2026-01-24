package com.achievement.exception;

import lombok.Getter;

import java.util.List;

/**
 * 过程系统数据验证异常
 * 
 * @author system
 * @since 2026-01-24
 */
@Getter
public class ProcessSystemValidationException extends RuntimeException {

    /**
     * 验证错误详情列表
     */
    private final List<String> validationErrors;

    /**
     * 构造函数
     * 
     * @param message 异常消息
     * @param validationErrors 验证错误详情列表
     */
    public ProcessSystemValidationException(String message, List<String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }

    /**
     * 构造函数
     * 
     * @param message 异常消息
     * @param validationErrors 验证错误详情列表
     * @param cause 原因异常
     */
    public ProcessSystemValidationException(String message, List<String> validationErrors, Throwable cause) {
        super(message, cause);
        this.validationErrors = validationErrors;
    }
}