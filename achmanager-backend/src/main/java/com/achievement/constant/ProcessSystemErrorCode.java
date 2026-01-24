package com.achievement.constant;

/**
 * 过程系统错误码常量
 * 
 * @author system
 * @since 2026-01-24
 */
public class ProcessSystemErrorCode {

    /**
     * 请求参数无效
     */
    public static final int INVALID_REQUEST_PARAMS = 10001;

    /**
     * API密钥无效
     */
    public static final int INVALID_API_KEY = 10002;

    /**
     * 签名验证失败
     */
    public static final int SIGNATURE_VERIFICATION_FAILED = 10003;

    /**
     * 访问频率超限
     */
    public static final int RATE_LIMIT_EXCEEDED = 10004;

    /**
     * 资源不存在
     */
    public static final int RESOURCE_NOT_FOUND = 10005;

    /**
     * 文件大小超限
     */
    public static final int FILE_SIZE_EXCEEDED = 10006;

    /**
     * 文件类型不支持
     */
    public static final int UNSUPPORTED_FILE_TYPE = 10007;

    /**
     * 存储服务异常
     */
    public static final int STORAGE_SERVICE_ERROR = 10008;

    /**
     * 数据库异常
     */
    public static final int DATABASE_ERROR = 10009;

    /**
     * 权限不足
     */
    public static final int INSUFFICIENT_PERMISSIONS = 10010;

    /**
     * 数据格式错误
     */
    public static final int DATA_FORMAT_ERROR = 10011;

    /**
     * 业务规则验证失败
     */
    public static final int BUSINESS_RULE_VIOLATION = 10012;

    /**
     * 系统繁忙
     */
    public static final int SYSTEM_BUSY = 10013;

    /**
     * 网络超时
     */
    public static final int NETWORK_TIMEOUT = 10014;

    /**
     * 配置错误
     */
    public static final int CONFIGURATION_ERROR = 10015;

    private ProcessSystemErrorCode() {
        // 工具类，禁止实例化
    }
}