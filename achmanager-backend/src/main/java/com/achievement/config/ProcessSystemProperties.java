package com.achievement.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 过程系统集成配置属性
 *
 * @author system
 * @since 2026-01-23
 */
@Data
@Component
@ConfigurationProperties(prefix = "process-system")
public class ProcessSystemProperties {

    /**
     * API密钥配置
     */
    private Api api = new Api();

    /**
     * 文件存储配置
     */
    private FileStorage fileStorage = new FileStorage();

    /**
     * 安全配置
     */
    private Security security = new Security();

    @Data
    public static class Api {
        /**
         * 有效的API密钥列表
         */
        private Map<String, String> keys;

        /**
         * API请求签名密钥
         */
        private String signatureSecret = "default-signature-secret";

        /**
         * API版本
         */
        private String version = "v1";

        /**
         * API基础路径
         */
        private String basePath = "/api/v1/process-system";
    }

    @Data
    public static class FileStorage {
        /**
         * 文件存储根路径
         */
        private String basePath = "/data/process-system/files";

        /**
         * 申报书文件存储路径
         */
        private String proposalPath = "proposals";

        /**
         * 附件文件存储路径
         */
        private String attachmentPath = "attachments";

        /**
         * 允许的文件类型
         */
        private List<String> allowedTypes = List.of("pdf", "doc", "docx", "xls", "xlsx", "txt", "zip", "rar");

        /**
         * 最大文件大小（字节）
         */
        private long maxFileSize = 100 * 1024 * 1024; // 100MB

        /**
         * 文件访问URL前缀
         */
        private String urlPrefix = "/process-system/files";
    }

    @Data
    public static class Security {
        /**
         * 请求频率限制（每分钟）
         */
        private int rateLimitPerMinute = 100;

        /**
         * 签名有效期（秒）
         */
        private long signatureValiditySeconds = 300; // 5分钟

        /**
         * 是否启用IP白名单
         */
        private boolean enableIpWhitelist = false;

        /**
         * IP白名单
         */
        private List<String> ipWhitelist;
    }
}