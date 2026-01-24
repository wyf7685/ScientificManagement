package com.achievement.exception;

import lombok.Getter;

/**
 * 过程系统安全异常
 * 
 * @author system
 * @since 2026-01-24
 */
@Getter
public class ProcessSystemSecurityException extends RuntimeException {

    /**
     * 错误码
     */
    private final Integer errorCode;

    /**
     * 构造函数
     * 
     * @param message 异常消息
     */
    public ProcessSystemSecurityException(String message) {
        super(message);
        this.errorCode = 10002; // 默认安全异常错误码
    }

    /**
     * 构造函数
     * 
     * @param errorCode 错误码
     * @param message 异常消息
     */
    public ProcessSystemSecurityException(Integer errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     * 
     * @param message 异常消息
     * @param cause 原因异常
     */
    public ProcessSystemSecurityException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = 10002;
    }

    /**
     * 构造函数
     * 
     * @param errorCode 错误码
     * @param message 异常消息
     * @param cause 原因异常
     */
    public ProcessSystemSecurityException(Integer errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}