package com.achievement.service.impl;

import com.achievement.config.ProcessSystemProperties;
import com.achievement.domain.po.ProcessSubmissionFile;
import com.achievement.mapper.InterimResultFileMapper;
import com.achievement.service.IInterimResultFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 中期成果物文件服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterimResultFileServiceImpl implements IInterimResultFileService {

    private final InterimResultFileMapper interimResultFileMapper;
    private final ProcessSystemProperties processSystemProperties;

    @Override
    public ProcessSubmissionFile getFileMetadata(String fileId) {
        if (!StringUtils.hasText(fileId)) {
            return null;
        }
        return interimResultFileMapper.selectFileById(fileId);
    }

    @Override
    public Resource getFileResource(String fileUrl) {
        Path filePath = resolveFilePath(fileUrl);
        if (filePath == null || !Files.exists(filePath)) {
            return null;
        }
        return new FileSystemResource(filePath.toFile());
    }

    private Path resolveFilePath(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return null;
        }

        String normalizedUrl = fileUrl.trim();
        String urlPrefix = processSystemProperties.getFileStorage().getUrlPrefix();
        Path basePath = Paths.get(processSystemProperties.getFileStorage().getBasePath()).toAbsolutePath().normalize();

        int queryIndex = normalizedUrl.indexOf('?');
        if (queryIndex >= 0) {
            normalizedUrl = normalizedUrl.substring(0, queryIndex);
        }

        if (normalizedUrl.startsWith("file:")) {
            try {
                Path path = Paths.get(URI.create(normalizedUrl)).toAbsolutePath().normalize();
                return path.startsWith(basePath) ? path : null;
            } catch (Exception e) {
                log.warn("解析文件URL失败: {}", normalizedUrl, e);
                return null;
            }
        }

        if (normalizedUrl.startsWith("http://") || normalizedUrl.startsWith("https://")) {
            int prefixIndex = StringUtils.hasText(urlPrefix) ? normalizedUrl.indexOf(urlPrefix) : -1;
            if (prefixIndex >= 0) {
                normalizedUrl = normalizedUrl.substring(prefixIndex + urlPrefix.length());
            } else {
                return null;
            }
        }

        if (StringUtils.hasText(urlPrefix) && normalizedUrl.startsWith(urlPrefix)) {
            normalizedUrl = normalizedUrl.substring(urlPrefix.length());
        }

        if (normalizedUrl.startsWith("/")) {
            normalizedUrl = normalizedUrl.substring(1);
        }

        Path resolved = basePath.resolve(normalizedUrl).normalize();
        if (!resolved.startsWith(basePath)) {
            return null;
        }

        return resolved;
    }
}
