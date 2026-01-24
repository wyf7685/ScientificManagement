package com.achievement.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件存储配置
 *
 * @author system
 * @since 2026-01-23
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class FileStorageConfig implements WebMvcConfigurer {

    private final ProcessSystemProperties processSystemProperties;

    /**
     * 初始化文件存储目录
     */
    @PostConstruct
    public void initFileStorageDirectories() {
        try {
            ProcessSystemProperties.FileStorage fileStorage = processSystemProperties.getFileStorage();
            
            // 创建基础存储目录
            Path basePath = Paths.get(fileStorage.getBasePath());
            if (!Files.exists(basePath)) {
                Files.createDirectories(basePath);
                log.info("Created base storage directory: {}", basePath.toAbsolutePath());
            }

            // 创建申报书存储目录
            Path proposalPath = basePath.resolve(fileStorage.getProposalPath());
            if (!Files.exists(proposalPath)) {
                Files.createDirectories(proposalPath);
                log.info("Created proposal storage directory: {}", proposalPath.toAbsolutePath());
            }

            // 创建附件存储目录
            Path attachmentPath = basePath.resolve(fileStorage.getAttachmentPath());
            if (!Files.exists(attachmentPath)) {
                Files.createDirectories(attachmentPath);
                log.info("Created attachment storage directory: {}", attachmentPath.toAbsolutePath());
            }

            log.info("File storage directories initialized successfully");
            
        } catch (Exception e) {
            log.error("Failed to initialize file storage directories", e);
            throw new RuntimeException("Failed to initialize file storage directories", e);
        }
    }

    /**
     * 配置静态资源处理器，用于文件访问
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        ProcessSystemProperties.FileStorage fileStorage = processSystemProperties.getFileStorage();
        
        // 配置过程系统文件访问路径
        registry.addResourceHandler(fileStorage.getUrlPrefix() + "/**")
                .addResourceLocations("file:" + fileStorage.getBasePath() + File.separator)
                .setCachePeriod(3600); // 缓存1小时

        log.info("Configured file access handler: {} -> {}", 
                fileStorage.getUrlPrefix(), fileStorage.getBasePath());
    }
}