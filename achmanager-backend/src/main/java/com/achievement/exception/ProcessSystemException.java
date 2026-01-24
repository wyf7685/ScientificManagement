package com.achievement.exception;

import lombok.Getter;

/**
 * 过程系统业务异常
 * 
 * @author system
 * @since 2026-01-24
 */
@Getter
public class ProcessSystemException extends RuntimeException {

    /**
     * 错误码
     */
    private final Integer errorCode;

    /**
     * 构造函数
     * 
     * @param message 异常消息
     */
    public ProcessSystemException(String message) {
        super(message);
        this.errorCode = null;
    }

    /**
     * 构造函数
     * 
     * @param errorCode 错误码
     * @param message 异常消息
     */
    public ProcessSystemException(Integer errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     * 
     * @param message 异常消息
     * @param cause 原因异常
     */
    public ProcessSystemException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    /**
     * 构造函数
     * 
     * @param errorCode 错误码
     * @param message 异常消息
     * @param cause 原因异常
     */
    public ProcessSystemException(Integer errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}