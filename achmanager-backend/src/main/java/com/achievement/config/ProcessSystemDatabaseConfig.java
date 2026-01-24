package com.achievement.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * 过程系统数据库初始化配置
 *
 * @author system
 * @since 2026-01-23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessSystemDatabaseConfig implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            initializeProcessSystemTables();
        } catch (Exception e) {
            log.error("Failed to initialize process system database tables", e);
            // 不抛出异常，避免影响应用启动
        }
    }

    /**
     * 初始化过程系统相关数据库表
     */
    private void initializeProcessSystemTables() {
        try {
            log.info("Checking process system database tables...");

            // 检查表是否存在
            if (!tableExists("process_submissions")) {
                log.info("Process system tables not found, creating...");
                executeSqlScript("db/migration/process_system_tables.sql");
                log.info("Process system tables created successfully");
            } else {
                log.info("Process system tables already exist");
            }

        } catch (Exception e) {
            log.error("Error initializing process system tables", e);
            throw new RuntimeException("Failed to initialize process system tables", e);
        }
    }

    /**
     * 检查表是否存在
     */
    private boolean tableExists(String tableName) {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName);
            return count != null && count > 0;
        } catch (Exception e) {
            log.warn("Error checking table existence for {}: {}", tableName, e.getMessage());
            return false;
        }
    }

    /**
     * 执行SQL脚本
     */
    private void executeSqlScript(String scriptPath) throws Exception {
        ClassPathResource resource = new ClassPathResource(scriptPath);
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            
            String sqlScript = reader.lines().collect(Collectors.joining("\n"));
            
            // 分割SQL语句（以分号分隔）
            String[] sqlStatements = sqlScript.split(";");
            
            for (String sql : sqlStatements) {
                String trimmedSql = sql.trim();
                if (!trimmedSql.isEmpty() && !trimmedSql.startsWith("--")) {
                    try {
                        jdbcTemplate.execute(trimmedSql);
                        log.debug("Executed SQL: {}", trimmedSql.substring(0, Math.min(50, trimmedSql.length())));
                    } catch (Exception e) {
                        log.warn("Failed to execute SQL statement: {}", trimmedSql.substring(0, Math.min(100, trimmedSql.length())));
                        log.warn("Error: {}", e.getMessage());
                        // 继续执行其他语句
                    }
                }
            }
        }
    }
}