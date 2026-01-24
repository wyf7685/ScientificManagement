package com.achievement.service;

import com.achievement.config.ProcessSystemProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 过程系统频率限制服务
 *
 * @author system
 * @since 2026-01-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessSystemRateLimitService {

    private final ProcessSystemProperties processSystemProperties;
    
    // 存储每个API密钥的请求计数器
    private final ConcurrentHashMap<String, RateLimitCounter> rateLimitCounters = new ConcurrentHashMap<>();

    /**
     * 检查是否超出频率限制
     *
     * @param apiKey API密钥
     * @return true表示允许请求，false表示超出限制
     */
    public boolean isAllowed(String apiKey) {
        if (apiKey == null) {
            return false;
        }

        RateLimitCounter counter = rateLimitCounters.computeIfAbsent(apiKey, k -> new RateLimitCounter());
        return counter.isAllowed();
    }

    /**
     * 获取剩余请求次数
     *
     * @param apiKey API密钥
     * @return 剩余请求次数
     */
    public int getRemainingRequests(String apiKey) {
        if (apiKey == null) {
            return 0;
        }

        RateLimitCounter counter = rateLimitCounters.get(apiKey);
        if (counter == null) {
            return processSystemProperties.getSecurity().getRateLimitPerMinute();
        }

        return counter.getRemainingRequests();
    }

    /**
     * 获取重置时间（秒）
     *
     * @param apiKey API密钥
     * @return 重置时间
     */
    public long getResetTime(String apiKey) {
        if (apiKey == null) {
            return 0;
        }

        RateLimitCounter counter = rateLimitCounters.get(apiKey);
        if (counter == null) {
            return 60; // 默认1分钟
        }

        return counter.getResetTime();
    }

    /**
     * 清理过期的计数器
     */
    public void cleanupExpiredCounters() {
        long currentTime = System.currentTimeMillis();
        rateLimitCounters.entrySet().removeIf(entry -> {
            RateLimitCounter counter = entry.getValue();
            return currentTime - counter.getLastResetTime() > 120000; // 2分钟后清理
        });
    }

    /**
     * 频率限制计数器
     */
    private class RateLimitCounter {
        private final AtomicInteger requestCount = new AtomicInteger(0);
        private final AtomicLong lastResetTime = new AtomicLong(System.currentTimeMillis());

        /**
         * 检查是否允许请求
         */
        public boolean isAllowed() {
            long currentTime = System.currentTimeMillis();
            long lastReset = lastResetTime.get();

            // 检查是否需要重置计数器（每分钟重置一次）
            if (currentTime - lastReset >= 60000) {
                if (lastResetTime.compareAndSet(lastReset, currentTime)) {
                    requestCount.set(0);
                    log.debug("Rate limit counter reset for current minute");
                }
            }

            // 检查是否超出限制
            int currentCount = requestCount.incrementAndGet();
            int limit = processSystemProperties.getSecurity().getRateLimitPerMinute();

            if (currentCount > limit) {
                log.warn("Rate limit exceeded: {} requests in current minute (limit: {})", currentCount, limit);
                return false;
            }

            return true;
        }

        /**
         * 获取剩余请求次数
         */
        public int getRemainingRequests() {
            int limit = processSystemProperties.getSecurity().getRateLimitPerMinute();
            int current = requestCount.get();
            return Math.max(0, limit - current);
        }

        /**
         * 获取重置时间（秒）
         */
        public long getResetTime() {
            long currentTime = System.currentTimeMillis();
            long lastReset = lastResetTime.get();
            long elapsed = currentTime - lastReset;
            return Math.max(0, (60000 - elapsed) / 1000);
        }

        /**
         * 获取最后重置时间
         */
        public long getLastResetTime() {
            return lastResetTime.get();
        }
    }
}