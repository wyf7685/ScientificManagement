package com.achievement;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableAsync
@EnableCaching
@MapperScan("com.achievement.mapper")
@Slf4j
public class AchievementManagerBackApplication {
    public static void main(String[] args) {
        SpringApplication.run(AchievementManagerBackApplication.class, args);
        log.info("Achievement Manager Backend Server Started Successfully");
        log.info("Process System Integration Module Enabled");
    }
}

