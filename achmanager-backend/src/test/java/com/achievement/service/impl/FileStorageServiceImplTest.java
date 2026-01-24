package com.achievement.service.impl;

import com.achievement.config.ProcessSystemProperties;
import com.achievement.domain.po.ProcessSubmissionFile;
import com.achievement.service.IFileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * FileStorageService 单元测试
 *
 * @author system
 * @since 2026-01-24
 */
@ExtendWith(MockitoExtension.class)
class FileStorageServiceImplTest {

    @Mock
    private ProcessSystemProperties processSystemProperties;

    @Mock
    private ProcessSystemProperties.FileStorage fileStorageConfig;

    @InjectMocks
    private FileStorageServiceImpl fileStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // 使用 lenient() 来避免 UnnecessaryStubbingException
        lenient().when(processSystemProperties.getFileStorage()).thenReturn(fileStorageConfig);
        lenient().when(fileStorageConfig.getBasePath()).thenReturn(tempDir.toString());
        lenient().when(fileStorageConfig.getProposalPath()).thenReturn("proposals");
        lenient().when(fileStorageConfig.getAttachmentPath()).thenReturn("attachments");
        lenient().when(fileStorageConfig.getUrlPrefix()).thenReturn("/process-system/files");
        lenient().when(fileStorageConfig.getMaxFileSize()).thenReturn(100 * 1024 * 1024L); // 100MB
        lenient().when(fileStorageConfig.getAllowedTypes()).thenReturn(Arrays.asList("pdf", "doc", "docx", "txt"));
    }

    @Test
    void testIsAllowedFileType() {
        // 测试允许的文件类型
        assertTrue(fileStorageService.isAllowedFileType("test.pdf"));
        assertTrue(fileStorageService.isAllowedFileType("document.doc"));
        assertTrue(fileStorageService.isAllowedFileType("file.DOCX")); // 大小写不敏感
        assertTrue(fileStorageService.isAllowedFileType("readme.txt"));
        
        // 测试不允许的文件类型
        assertFalse(fileStorageService.isAllowedFileType("image.jpg"));
        assertFalse(fileStorageService.isAllowedFileType("script.exe"));
        assertFalse(fileStorageService.isAllowedFileType(""));
        assertFalse(fileStorageService.isAllowedFileType(null));
        assertFalse(fileStorageService.isAllowedFileType("noextension"));
    }

    @Test
    void testIsAllowedFileSize() {
        // 测试允许的文件大小
        assertTrue(fileStorageService.isAllowedFileSize(1024)); // 1KB
        assertTrue(fileStorageService.isAllowedFileSize(50 * 1024 * 1024)); // 50MB
        assertTrue(fileStorageService.isAllowedFileSize(100 * 1024 * 1024)); // 100MB (边界值)
        
        // 测试不允许的文件大小
        assertFalse(fileStorageService.isAllowedFileSize(0)); // 空文件
        assertFalse(fileStorageService.isAllowedFileSize(-1)); // 负数
        assertFalse(fileStorageService.isAllowedFileSize(101 * 1024 * 1024)); // 超过100MB
    }

    @Test
    void testGetFileExtension() {
        assertEquals("pdf", fileStorageService.getFileExtension("document.pdf"));
        assertEquals("docx", fileStorageService.getFileExtension("file.DOCX"));
        assertEquals("txt", fileStorageService.getFileExtension("readme.txt"));
        assertEquals("", fileStorageService.getFileExtension("noextension"));
        assertEquals("", fileStorageService.getFileExtension(""));
        assertEquals("", fileStorageService.getFileExtension(null));
        assertEquals("pdf", fileStorageService.getFileExtension("file.name.with.dots.pdf"));
    }

    @Test
    void testGenerateStoragePath() {
        String proposalPath = fileStorageService.generateStoragePath("test.pdf", "proposal");
        assertTrue(proposalPath.startsWith("proposals/"));
        assertTrue(proposalPath.endsWith("test.pdf"));
        assertTrue(proposalPath.contains("/2026/")); // 当前年份
        
        String attachmentPath = fileStorageService.generateStoragePath("attachment.doc", "attachment");
        assertTrue(attachmentPath.startsWith("attachments/"));
        assertTrue(attachmentPath.endsWith("attachment.doc"));
    }

    @Test
    void testGenerateFileUrl() {
        String url = fileStorageService.generateFileUrl("proposals/2026/01/24/test.pdf");
        assertEquals("/process-system/files/proposals/2026/01/24/test.pdf", url);
        
        // 测试Windows路径分隔符转换
        String windowsPath = "proposals\\2026\\01\\24\\test.pdf";
        String windowsUrl = fileStorageService.generateFileUrl(windowsPath);
        assertEquals("/process-system/files/proposals/2026/01/24/test.pdf", windowsUrl);
    }

    @Test
    void testCalculateMD5() {
        byte[] testData = "Hello, World!".getBytes();
        String md5 = fileStorageService.calculateMD5(testData);
        
        assertNotNull(md5);
        assertEquals(32, md5.length()); // MD5 hash length
        assertEquals("65a8e27d8879283831b664bd8b7f0ad4", md5); // Known MD5 for "Hello, World!"
    }

    @Test
    void testStoreFileWithMultipartFile() throws IOException {
        // 创建测试文件
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", 
                "test.pdf", 
                "application/pdf", 
                "Test file content".getBytes()
        );
        
        // 存储文件
        ProcessSubmissionFile result = fileStorageService.storeFile(
                mockFile, 12345L, "proposal", "user123", "Test User"
        );
        
        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getFileId());
        assertEquals(12345L, result.getSubmissionId());
        assertEquals("test.pdf", result.getOriginalName());
        assertEquals("pdf", result.getFileType());
        assertEquals("proposal", result.getFileCategory());
        assertEquals("completed", result.getStorageStatus());
        assertEquals("user123", result.getUploaderId());
        assertEquals("Test User", result.getUploaderName());
        assertTrue(result.getFileUrl().startsWith("/process-system/files/"));
        assertNotNull(result.getFileMd5());
    }

    @Test
    void testStoreFileValidationFailures() {
        // 测试空文件
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "text/plain", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> 
                fileStorageService.storeFile(emptyFile, 123L, "proposal", "user", "User"));
        
        // 测试不支持的文件类型
        MockMultipartFile invalidTypeFile = new MockMultipartFile(
                "file", "test.exe", "application/octet-stream", "content".getBytes());
        assertThrows(IllegalArgumentException.class, () -> 
                fileStorageService.storeFile(invalidTypeFile, 123L, "proposal", "user", "User"));
    }

    @Test
    void testGetDownloadUrl() {
        String downloadUrl = fileStorageService.getDownloadUrl("file_123456");
        assertEquals("/api/v1/process-system/files/download/file_123456", downloadUrl);
    }
}