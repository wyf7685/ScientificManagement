package com.achievement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 过程系统定时任务服务
 *
 * @author system
 * @since 2026-01-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessSystemScheduledService {

    private final ProcessSystemRateLimitService rateLimitService;

    /**
     * 清理过期的频率限制计数器
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void cleanupExpiredRateLimitCounters() {
        try {
            log.debug("Starting cleanup of expired rate limit counters");
            rateLimitService.cleanupExpiredCounters();
            log.debug("Completed cleanup of expired rate limit counters");
        } catch (Exception e) {
            log.error("Error during rate limit counters cleanup", e);
        }
    }
}