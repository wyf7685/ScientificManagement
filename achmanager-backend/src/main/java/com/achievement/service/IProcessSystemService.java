package com.achievement.service;

import com.achievement.domain.dto.ProcessSubmissionQueryDTO;
import com.achievement.domain.dto.ProcessSubmissionRequest;
import com.achievement.domain.po.ProcessSubmission;
import com.achievement.domain.po.ProcessSubmissionFile;
import com.achievement.domain.vo.ProcessSubmissionVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 过程系统服务接口
 * </p>
 *
 * @author system
 * @since 2026-01-24
 */
public interface IProcessSystemService extends IService<ProcessSubmission> {

    /**
     * 存储项目提交物
     * 
     * @param request 提交物存储请求
     * @return 存储结果
     */
    ProcessSubmissionVO storeSubmission(ProcessSubmissionRequest request);

    /**
     * 根据提交物ID获取详情
     * 
     * @param submissionId 提交物ID
     * @return 提交物详情
     */
    ProcessSubmissionVO getSubmissionDetail(Long submissionId);

    /**
     * 查询提交物列表
     * 
     * @param queryDTO 查询条件
     * @return 提交物列表
     */
    List<ProcessSubmissionVO> getSubmissions(ProcessSubmissionQueryDTO queryDTO);

    /**
     * 分页查询提交物列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<ProcessSubmissionVO> getSubmissionsPage(ProcessSubmissionQueryDTO queryDTO);

    /**
     * 根据申报ID查询提交物列表
     * 
     * @param applicationId 申报ID
     * @return 提交物列表
     */
    List<ProcessSubmissionVO> getSubmissionsByApplicationId(Long applicationId);

    /**
     * 根据申报ID和提交阶段查询提交物列表
     * 
     * @param applicationId 申报ID
     * @param submissionStage 提交阶段
     * @return 提交物列表
     */
    List<ProcessSubmissionVO> getSubmissionsByApplicationIdAndStage(Long applicationId, String submissionStage);

    /**
     * 验证提交物数据
     * 
     * @param request 提交物请求
     * @return 验证结果
     */
    boolean validateSubmissionData(ProcessSubmissionRequest request);

    /**
     * 标准化提交物数据
     * 
     * @param request 提交物请求
     * @return 标准化后的数据
     */
    ProcessSubmissionRequest normalizeSubmissionData(ProcessSubmissionRequest request);

    /**
     * 检查是否存在重复提交物
     * 
     * @param request 提交物请求
     * @return 是否重复
     */
    boolean isDuplicateSubmission(ProcessSubmissionRequest request);

    /**
     * 获取下一个版本号
     * 
     * @param applicationId 申报ID
     * @param submissionType 提交物类型
     * @param submissionStage 提交阶段
     * @param submissionRound 提交轮次
     * @return 下一个版本号
     */
    Integer getNextVersion(Long applicationId, String submissionType, String submissionStage, Integer submissionRound);

    /**
     * 统计提交物数量
     * 
     * @param queryDTO 查询条件
     * @return 数量
     */
    Long countSubmissions(ProcessSubmissionQueryDTO queryDTO);

    /**
     * 根据版本号查询提交物历史记录
     * 
     * @param applicationId 申报ID
     * @param submissionType 提交物类型
     * @param submissionStage 提交阶段
     * @param submissionRound 提交轮次
     * @return 历史版本列表
     */
    List<ProcessSubmissionVO> getSubmissionVersionHistory(Long applicationId, String submissionType, 
                                                         String submissionStage, Integer submissionRound);

    /**
     * 根据轮次查询提交物历史记录
     * 
     * @param applicationId 申报ID
     * @param submissionType 提交物类型
     * @param submissionStage 提交阶段
     * @return 轮次历史列表
     */
    List<ProcessSubmissionVO> getSubmissionRoundHistory(Long applicationId, String submissionType, String submissionStage);

    /**
     * 获取指定版本的提交物详情
     * 
     * @param applicationId 申报ID
     * @param submissionType 提交物类型
     * @param submissionStage 提交阶段
     * @param submissionRound 提交轮次
     * @param submissionVersion 版本号
     * @return 提交物详情
     */
    ProcessSubmissionVO getSubmissionByVersion(Long applicationId, String submissionType, 
                                              String submissionStage, Integer submissionRound, Integer submissionVersion);

    /**
     * 获取提交物的所有历史版本
     * 
     * @param applicationId 申报ID
     * @param submissionType 提交物类型
     * @param submissionStage 提交阶段
     * @return 完整历史记录列表
     */
    List<ProcessSubmissionVO> getSubmissionFullHistory(Long applicationId, String submissionType, String submissionStage);

    // ==================== 文件信息查询相关方法 ====================

    /**
     * 查询文件列表
     * 
     * @param submissionId 提交物ID（可选）
     * @param fileCategory 文件分类（可选）
     * @param fileName 文件名称（可选，支持模糊查询）
     * @param fileType 文件类型（可选）
     * @return 文件列表
     */
    List<ProcessSubmissionFile> getFiles(Long submissionId, String fileCategory, String fileName, String fileType);

    /**
     * 统计文件数量
     * 
     * @param submissionId 提交物ID（可选）
     * @param fileCategory 文件分类（可选）
     * @param fileName 文件名称（可选，支持模糊查询）
     * @param fileType 文件类型（可选）
     * @return 文件数量
     */
    Long countFiles(Long submissionId, String fileCategory, String fileName, String fileType);

    /**
     * 根据文件ID获取文件元数据
     * 
     * @param fileId 文件ID
     * @return 文件元数据信息
     */
    ProcessSubmissionFile getFileMetadata(String fileId);

    /**
     * 根据提交物ID获取文件列表
     * 
     * @param submissionId 提交物ID
     * @param fileCategory 文件分类（可选）
     * @return 文件列表
     */
    List<ProcessSubmissionFile> getFilesBySubmissionId(Long submissionId, String fileCategory);

    /**
     * 获取文件下载链接
     * 
     * @param fileId 文件ID
     * @return 下载链接
     */
    String getFileDownloadUrl(String fileId);

    /**
     * 批量获取文件下载链接
     * 
     * @param fileIds 文件ID列表
     * @return 文件ID到下载链接的映射
     */
    Map<String, String> getBatchFileDownloadUrls(List<String> fileIds);
}