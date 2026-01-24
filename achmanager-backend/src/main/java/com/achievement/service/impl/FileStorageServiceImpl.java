package com.achievement.service.impl;

import com.achievement.config.ProcessSystemProperties;
import com.achievement.domain.po.ProcessSubmissionFile;
import com.achievement.service.IFileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文件存储服务实现
 *
 * @author system
 * @since 2026-01-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements IFileStorageService {

    private final ProcessSystemProperties processSystemProperties;

    private static final String DATE_FORMAT = "yyyy/MM/dd";
    private static final String PROPOSAL_CATEGORY = "proposal";
    private static final String ATTACHMENT_CATEGORY = "attachment";

    @Override
    public ProcessSubmissionFile storeFile(MultipartFile file, Long submissionId, String fileCategory,
                                          String uploaderId, String uploaderName) throws IOException {
        
        // 验证文件
        validateFile(file);
        
        // 生成文件ID和存储路径
        String fileId = generateFileId();
        String originalName = file.getOriginalFilename();
        String fileName = generateFileName(originalName, fileId);
        String storagePath = generateStoragePath(fileName, fileCategory);
        
        // 创建目标目录
        Path targetPath = Paths.get(processSystemProperties.getFileStorage().getBasePath(), storagePath);
        Files.createDirectories(targetPath.getParent());
        
        // 保存文件
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        // 计算MD5
        byte[] fileContent = file.getBytes();
        String md5 = calculateMD5(fileContent);
        
        // 创建文件记录
        ProcessSubmissionFile fileRecord = new ProcessSubmissionFile();
        fileRecord.setFileId(fileId);
        fileRecord.setSubmissionId(submissionId);
        fileRecord.setFileName(fileName);
        fileRecord.setOriginalName(originalName);
        fileRecord.setFileSize(file.getSize());
        fileRecord.setFileType(getFileExtension(originalName));
        fileRecord.setMimeType(file.getContentType());
        fileRecord.setFilePath(storagePath);
        fileRecord.setFileUrl(generateFileUrl(storagePath));
        fileRecord.setFileCategory(fileCategory);
        fileRecord.setFileMd5(md5);
        fileRecord.setStorageStatus("completed");
        fileRecord.setUploaderId(uploaderId);
        fileRecord.setUploaderName(uploaderName);
        fileRecord.setUploadTime(LocalDateTime.now());
        fileRecord.setCreateTime(LocalDateTime.now());
        fileRecord.setUpdateTime(LocalDateTime.now());
        fileRecord.setDelFlag("0");
        
        log.info("File stored successfully: fileId={}, originalName={}, size={}, path={}", 
                fileId, originalName, file.getSize(), storagePath);
        
        return fileRecord;
    }

    @Override
    public ProcessSubmissionFile storeFile(String fileName, byte[] fileContent, Long submissionId,
                                          String fileCategory, String uploaderId, String uploaderName) throws IOException {
        
        // 验证文件
        validateFile(fileName, fileContent);
        
        // 生成文件ID和存储路径
        String fileId = generateFileId();
        String generatedFileName = generateFileName(fileName, fileId);
        String storagePath = generateStoragePath(generatedFileName, fileCategory);
        
        // 创建目标目录
        Path targetPath = Paths.get(processSystemProperties.getFileStorage().getBasePath(), storagePath);
        Files.createDirectories(targetPath.getParent());
        
        // 保存文件
        Files.write(targetPath, fileContent);
        
        // 计算MD5
        String md5 = calculateMD5(fileContent);
        
        // 创建文件记录
        ProcessSubmissionFile fileRecord = new ProcessSubmissionFile();
        fileRecord.setFileId(fileId);
        fileRecord.setSubmissionId(submissionId);
        fileRecord.setFileName(generatedFileName);
        fileRecord.setOriginalName(fileName);
        fileRecord.setFileSize((long) fileContent.length);
        fileRecord.setFileType(getFileExtension(fileName));
        fileRecord.setMimeType(getMimeType(fileName));
        fileRecord.setFilePath(storagePath);
        fileRecord.setFileUrl(generateFileUrl(storagePath));
        fileRecord.setFileCategory(fileCategory);
        fileRecord.setFileMd5(md5);
        fileRecord.setStorageStatus("completed");
        fileRecord.setUploaderId(uploaderId);
        fileRecord.setUploaderName(uploaderName);
        fileRecord.setUploadTime(LocalDateTime.now());
        fileRecord.setCreateTime(LocalDateTime.now());
        fileRecord.setUpdateTime(LocalDateTime.now());
        fileRecord.setDelFlag("0");
        
        log.info("File stored successfully: fileId={}, originalName={}, size={}, path={}", 
                fileId, fileName, fileContent.length, storagePath);
        
        return fileRecord;
    }

    @Override
    public List<ProcessSubmissionFile> storeFiles(List<MultipartFile> files, Long submissionId,
                                                 String fileCategory, String uploaderId, String uploaderName) throws IOException {
        
        List<ProcessSubmissionFile> fileRecords = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                ProcessSubmissionFile fileRecord = storeFile(file, submissionId, fileCategory, uploaderId, uploaderName);
                fileRecords.add(fileRecord);
            }
        }
        
        log.info("Batch file storage completed: count={}, submissionId={}", fileRecords.size(), submissionId);
        
        return fileRecords;
    }

    @Override
    public boolean isAllowedFileType(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return false;
        }
        
        String extension = getFileExtension(fileName).toLowerCase();
        List<String> allowedTypes = processSystemProperties.getFileStorage().getAllowedTypes();
        
        return allowedTypes.contains(extension);
    }

    @Override
    public boolean isAllowedFileSize(long fileSize) {
        long maxSize = processSystemProperties.getFileStorage().getMaxFileSize();
        return fileSize > 0 && fileSize <= maxSize;
    }

    @Override
    public String generateStoragePath(String fileName, String fileCategory) {
        // 根据文件分类选择子目录
        String categoryPath = PROPOSAL_CATEGORY.equals(fileCategory) ? 
                processSystemProperties.getFileStorage().getProposalPath() :
                processSystemProperties.getFileStorage().getAttachmentPath();
        
        // 按日期分组存储
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
        
        return Paths.get(categoryPath, datePath, fileName).toString().replace("\\", "/");
    }

    @Override
    public String generateFileUrl(String filePath) {
        String urlPrefix = processSystemProperties.getFileStorage().getUrlPrefix();
        return urlPrefix + "/" + filePath.replace("\\", "/");
    }

    @Override
    public String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        
        return "";
    }

    @Override
    public String calculateMD5(byte[] fileContent) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(fileContent);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to calculate MD5", e);
            return "";
        }
    }

    @Override
    public boolean deleteFile(String filePath) {
        try {
            Path fullPath = Paths.get(processSystemProperties.getFileStorage().getBasePath(), filePath);
            boolean deleted = Files.deleteIfExists(fullPath);
            
            if (deleted) {
                log.info("File deleted successfully: {}", filePath);
            } else {
                log.warn("File not found for deletion: {}", filePath);
            }
            
            return deleted;
        } catch (IOException e) {
            log.error("Failed to delete file: {}", filePath, e);
            return false;
        }
    }

    @Override
    public boolean fileExists(String filePath) {
        Path fullPath = Paths.get(processSystemProperties.getFileStorage().getBasePath(), filePath);
        return Files.exists(fullPath);
    }

    @Override
    public String getDownloadUrl(String fileId) {
        // 这里可以生成临时下载链接或直接返回文件访问URL
        // 实际实现中可能需要查询数据库获取文件路径
        return "/api/v1/process-system/files/download/" + fileId;
    }

    /**
     * 生成唯一文件ID
     */
    private String generateFileId() {
        return "file_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成存储文件名（保留扩展名，添加唯一标识）
     */
    private String generateFileName(String originalName, String fileId) {
        if (!StringUtils.hasText(originalName)) {
            return fileId;
        }
        
        String extension = getFileExtension(originalName);
        String baseName = originalName.substring(0, originalName.lastIndexOf('.'));
        
        // 清理文件名中的特殊字符
        baseName = baseName.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5._-]", "_");
        
        return StringUtils.hasText(extension) ? 
                baseName + "_" + fileId + "." + extension :
                baseName + "_" + fileId;
    }

    /**
     * 验证MultipartFile
     */
    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        String originalName = file.getOriginalFilename();
        if (!StringUtils.hasText(originalName)) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        if (!isAllowedFileType(originalName)) {
            throw new IllegalArgumentException("不支持的文件类型: " + getFileExtension(originalName));
        }
        
        if (!isAllowedFileSize(file.getSize())) {
            throw new IllegalArgumentException("文件大小超出限制: " + file.getSize() + " bytes");
        }
    }

    /**
     * 验证文件名和内容
     */
    private void validateFile(String fileName, byte[] fileContent) throws IOException {
        if (!StringUtils.hasText(fileName)) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        if (fileContent == null || fileContent.length == 0) {
            throw new IllegalArgumentException("文件内容不能为空");
        }
        
        if (!isAllowedFileType(fileName)) {
            throw new IllegalArgumentException("不支持的文件类型: " + getFileExtension(fileName));
        }
        
        if (!isAllowedFileSize(fileContent.length)) {
            throw new IllegalArgumentException("文件大小超出限制: " + fileContent.length + " bytes");
        }
    }

    /**
     * 根据文件扩展名获取MIME类型
     */
    private String getMimeType(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        
        switch (extension) {
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "txt":
                return "text/plain";
            case "zip":
                return "application/zip";
            case "rar":
                return "application/x-rar-compressed";
            default:
                return "application/octet-stream";
        }
    }
}