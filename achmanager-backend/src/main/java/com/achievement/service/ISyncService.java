package com.achievement.service;

import com.achievement.domain.dto.BatchSyncRequest;
import com.achievement.domain.dto.ProcessSubmissionRequest;
import com.achievement.domain.dto.SyncStatusDTO;
import com.achievement.domain.vo.SyncResultVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 同步服务接口
 * </p>
 *
 * @author system
 * @since 2026-01-24
 */
public interface ISyncService {

    /**
     * 记录同步时间戳
     * 
     * @param applicationId 申报ID
     * @param syncType 同步类型
     * @param syncStatus 同步状态
     * @param syncCount 同步记录数
     * @param errorMessage 错误信息
     */
    void recordSyncTimestamp(Long applicationId, String syncType, String syncStatus, Integer syncCount, String errorMessage);

    /**
     * 获取同步状态
     * 
     * @param applicationId 申报ID
     * @return 同步状态
     */
    SyncStatusDTO getSyncStatus(Long applicationId);

    /**
     * 获取最新同步时间
     * 
     * @param applicationId 申报ID
     * @return 最新同步时间
     */
    LocalDateTime getLastSyncTime(Long applicationId);

    /**
     * 批量同步提交物
     * 
     * @param request 批量同步请求
     * @return 同步结果
     */
    SyncResultVO batchSyncSubmissions(BatchSyncRequest request);

    /**
     * 单个提交物同步
     * 
     * @param submissionRequest 提交物请求
     * @return 同步结果
     */
    SyncResultVO syncSingleSubmission(ProcessSubmissionRequest submissionRequest);

    /**
     * 检查数据一致性
     * 
     * @param applicationIds 申报ID列表
     * @return 一致性检查结果
     */
    List<SyncStatusDTO> checkDataConsistency(List<Long> applicationIds);

    /**
     * 生成同步报告
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 同步报告
     */
    SyncResultVO generateSyncReport(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 重试失败的同步
     * 
     * @param applicationIds 申报ID列表
     * @return 重试结果
     */
    SyncResultVO retryFailedSync(List<Long> applicationIds);

    /**
     * 清理过期的同步记录
     * 
     * @param beforeTime 清理时间点
     * @return 清理数量
     */
    Integer cleanupExpiredSyncRecords(LocalDateTime beforeTime);

    /**
     * 检查是否需要同步
     * 
     * @param applicationId 申报ID
     * @param lastModifyTime 最后修改时间
     * @return 是否需要同步
     */
    boolean needSync(Long applicationId, LocalDateTime lastModifyTime);

    /**
     * 去重处理
     * 
     * @param submissionRequests 提交物请求列表
     * @return 去重后的请求列表
     */
    List<ProcessSubmissionRequest> deduplicateSubmissions(List<ProcessSubmissionRequest> submissionRequests);

    /**
     * 获取同步统计信息
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    SyncResultVO getSyncStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 手动同步指定项目的数据
     * 
     * @param projectId 项目ID
     * @return 同步结果
     */
    SyncResultVO manualSync(String projectId);
}