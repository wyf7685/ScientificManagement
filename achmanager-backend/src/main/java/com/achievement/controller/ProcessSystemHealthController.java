package com.achievement.controller;

import com.achievement.config.ProcessSystemProperties;
import com.achievement.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 过程系统集成健康检查控制器
 *
 * @author system
 * @since 2026-01-23
 */
@RestController
@RequestMapping("/api/v1/process-system")
@RequiredArgsConstructor
public class ProcessSystemHealthController {

    private final ProcessSystemProperties processSystemProperties;

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("version", processSystemProperties.getApi().getVersion());
        healthInfo.put("fileStorageBasePath", processSystemProperties.getFileStorage().getBasePath());
        healthInfo.put("maxFileSize", processSystemProperties.getFileStorage().getMaxFileSize());
        healthInfo.put("allowedFileTypes", processSystemProperties.getFileStorage().getAllowedTypes());
        
        return Result.success(healthInfo);
    }
}