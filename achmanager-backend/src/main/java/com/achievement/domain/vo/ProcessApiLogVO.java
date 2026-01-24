package com.achievement.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 过程系统API日志VO
 *
 * @author system
 * @since 2026-01-24
 */
@Data
@Accessors(chain = true)
@Schema(name = "ProcessApiLogVO", description = "过程系统API日志视图对象")
public class ProcessApiLogVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "请求ID")
    private String requestId;

    @Schema(description = "API密钥")
    private String apiKey;

    @Schema(description = "HTTP方法")
    private String method;

    @Schema(description = "请求URL")
    private String url;

    @Schema(description = "响应状态码")
    private Integer responseCode;

    @Schema(description = "响应时间(ms)")
    private Integer responseTime;

    @Schema(description = "客户端IP")
    private String clientIp;

    @Schema(description = "User Agent")
    private String userAgent;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "业务操作类型")
    private String operationType;

    @Schema(description = "操作结果")
    private String operationResult;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "请求体大小")
    private Integer requestBodySize;

    @Schema(description = "响应体大小")
    private Integer responseBodySize;

    @Schema(description = "是否有错误")
    private Boolean hasError;

    @Schema(description = "响应时间级别")
    private String responseTimeLevel;

    /**
     * 从ProcessApiLog转换为VO
     */
    public static ProcessApiLogVO fromEntity(com.achievement.domain.po.ProcessApiLog entity) {
        if (entity == null) {
            return null;
        }

        ProcessApiLogVO vo = new ProcessApiLogVO()
                .setId(entity.getId())
                .setRequestId(entity.getRequestId())
                .setApiKey(maskApiKey(entity.getApiKey()))
                .setMethod(entity.getMethod())
                .setUrl(entity.getUrl())
                .setResponseCode(entity.getResponseCode())
                .setResponseTime(entity.getResponseTime())
                .setClientIp(entity.getClientIp())
                .setUserAgent(entity.getUserAgent())
                .setErrorMessage(entity.getErrorMessage())
                .setOperationType(entity.getOperationType())
                .setOperationResult(entity.getOperationResult())
                .setCreatedAt(entity.getCreatedAt());

        // 计算请求体和响应体大小
        vo.setRequestBodySize(entity.getRequestBody() != null ? entity.getRequestBody().length() : 0);
        vo.setResponseBodySize(entity.getResponseBody() != null ? entity.getResponseBody().length() : 0);

        // 判断是否有错误
        vo.setHasError(entity.getResponseCode() != null && entity.getResponseCode() >= 400);

        // 设置响应时间级别
        vo.setResponseTimeLevel(getResponseTimeLevel(entity.getResponseTime()));

        return vo;
    }

    /**
     * 掩码API密钥，只显示前4位和后4位
     */
    private static String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) {
            return apiKey;
        }
        
        String prefix = apiKey.substring(0, 4);
        String suffix = apiKey.substring(apiKey.length() - 4);
        return prefix + "****" + suffix;
    }

    /**
     * 获取响应时间级别
     */
    private static String getResponseTimeLevel(Integer responseTime) {
        if (responseTime == null) {
            return "unknown";
        }
        
        if (responseTime < 100) {
            return "fast";
        } else if (responseTime < 500) {
            return "normal";
        } else if (responseTime < 2000) {
            return "slow";
        } else {
            return "very_slow";
        }
    }
}