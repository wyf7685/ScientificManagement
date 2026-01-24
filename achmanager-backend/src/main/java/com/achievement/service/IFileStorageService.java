package com.achievement.service;

import com.achievement.domain.po.ProcessSubmissionFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 文件存储服务接口
 *
 * @author system
 * @since 2026-01-24
 */
public interface IFileStorageService {

    /**
     * 存储文件
     *
     * @param file 上传的文件
     * @param submissionId 提交物ID
     * @param fileCategory 文件分类 (proposal/attachment)
     * @param uploaderId 上传者ID
     * @param uploaderName 上传者名称
     * @return 文件存储信息
     * @throws IOException 文件操作异常
     */
    ProcessSubmissionFile storeFile(MultipartFile file, Long submissionId, String fileCategory, 
                                   String uploaderId, String uploaderName) throws IOException;

    /**
     * 存储文件（从URL或路径）
     *
     * @param fileName 文件名
     * @param fileContent 文件内容
     * @param submissionId 提交物ID
     * @param fileCategory 文件分类
     * @param uploaderId 上传者ID
     * @param uploaderName 上传者名称
     * @return 文件存储信息
     * @throws IOException 文件操作异常
     */
    ProcessSubmissionFile storeFile(String fileName, byte[] fileContent, Long submissionId, 
                                   String fileCategory, String uploaderId, String uploaderName) throws IOException;

    /**
     * 批量存储文件
     *
     * @param files 文件列表
     * @param submissionId 提交物ID
     * @param fileCategory 文件分类
     * @param uploaderId 上传者ID
     * @param uploaderName 上传者名称
     * @return 文件存储信息列表
     * @throws IOException 文件操作异常
     */
    List<ProcessSubmissionFile> storeFiles(List<MultipartFile> files, Long submissionId, 
                                          String fileCategory, String uploaderId, String uploaderName) throws IOException;

    /**
     * 验证文件类型
     *
     * @param fileName 文件名
     * @return 是否为允许的文件类型
     */
    boolean isAllowedFileType(String fileName);

    /**
     * 验证文件大小
     *
     * @param fileSize 文件大小（字节）
     * @return 是否在允许的大小范围内
     */
    boolean isAllowedFileSize(long fileSize);

    /**
     * 生成文件存储路径
     *
     * @param fileName 原始文件名
     * @param fileCategory 文件分类
     * @return 存储路径
     */
    String generateStoragePath(String fileName, String fileCategory);

    /**
     * 生成文件访问URL
     *
     * @param filePath 文件存储路径
     * @return 访问URL
     */
    String generateFileUrl(String filePath);

    /**
     * 获取文件扩展名
     *
     * @param fileName 文件名
     * @return 文件扩展名
     */
    String getFileExtension(String fileName);

    /**
     * 计算文件MD5
     *
     * @param fileContent 文件内容
     * @return MD5值
     */
    String calculateMD5(byte[] fileContent);

    /**
     * 删除文件
     *
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    boolean deleteFile(String filePath);

    /**
     * 检查文件是否存在
     *
     * @param filePath 文件路径
     * @return 文件是否存在
     */
    boolean fileExists(String filePath);

    /**
     * 获取文件下载URL
     *
     * @param fileId 文件ID
     * @return 下载URL
     */
    String getDownloadUrl(String fileId);
}