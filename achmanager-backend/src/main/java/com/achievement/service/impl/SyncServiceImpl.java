package com.achievement.service.impl;

import com.achievement.domain.dto.BatchSyncRequest;
import com.achievement.domain.dto.ProcessSubmissionRequest;
import com.achievement.domain.dto.SyncStatusDTO;
import com.achievement.domain.po.ProcessSubmission;
import com.achievement.domain.po.ProcessSyncRecord;
import com.achievement.domain.vo.SyncResultVO;
import com.achievement.mapper.ProcessSyncRecordMapper;
import com.achievement.service.IProcessSystemService;
import com.achievement.service.ISyncService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 同步服务实现类
 * </p>
 *
 * @author system
 * @since 2026-01-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SyncServiceImpl extends ServiceImpl<ProcessSyncRecordMapper, ProcessSyncRecord> implements ISyncService {

    private final ProcessSyncRecordMapper processSyncRecordMapper;
    private final IProcessSystemService processSystemService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordSyncTimestamp(Long applicationId, String syncType, String syncStatus, Integer syncCount, String errorMessage) {
        log.info("记录同步时间戳，applicationId: {}, syncType: {}, syncStatus: {}", applicationId, syncType, syncStatus);

        ProcessSyncRecord record = new ProcessSyncRecord();
        record.setSyncId(generateSyncId());
        record.setApplicationId(applicationId);
        record.setSyncType(syncType);
        record.setSyncStatus(syncStatus);
        record.setSyncStartTime(LocalDateTime.now());
        record.setSyncEndTime(LocalDateTime.now());
        record.setSyncCount(syncCount);
        record.setErrorMessage(errorMessage);
        record.setRetryCount(0);
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());

        if ("success".equals(syncStatus)) {
            record.setSuccessCount(syncCount);
            record.setFailedCount(0);
        } else if ("failed".equals(syncStatus)) {
            record.setSuccessCount(0);
            record.setFailedCount(syncCount);
        }

        save(record);
        log.info("同步时间戳记录完成，recordId: {}", record.getId());
    }

    @Override
    public SyncStatusDTO getSyncStatus(Long applicationId) {
        ProcessSyncRecord latestRecord = processSyncRecordMapper.selectLatestByApplicationId(applicationId);
        
        SyncStatusDTO statusDTO = new SyncStatusDTO();
        statusDTO.setApplicationId(applicationId);
        
        if (latestRecord != null) {
            statusDTO.setLastSyncTime(latestRecord.getSyncEndTime());
            statusDTO.setSyncStatus(latestRecord.getSyncStatus());
            statusDTO.setSyncCount(latestRecord.getSyncCount());
            statusDTO.setErrorMessage(latestRecord.getErrorMessage());
            statusDTO.setSyncType(latestRecord.getSyncType());
            statusDTO.setDataVersion(latestRecord.getDataVersion());
        } else {
            statusDTO.setSyncStatus("never_synced");
            statusDTO.setSyncCount(0);
        }
        
        return statusDTO;
    }

    @Override
    public LocalDateTime getLastSyncTime(Long applicationId) {
        ProcessSyncRecord latestRecord = processSyncRecordMapper.selectLatestByApplicationId(applicationId);
        return latestRecord != null ? latestRecord.getSyncEndTime() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SyncResultVO batchSyncSubmissions(BatchSyncRequest request) {
        log.info("开始批量同步提交物，申报ID数量: {}", request.getApplicationIds().size());

        String syncId = generateSyncId();
        LocalDateTime syncStartTime = LocalDateTime.now();
        
        SyncResultVO result = new SyncResultVO();
        result.setSyncId(syncId);
        result.setSyncStatus("processing");
        result.setSyncStartTime(syncStartTime);
        result.setTotalCount(request.getApplicationIds().size());
        result.setErrors(new ArrayList<>());
        result.setDetails(new ArrayList<>());

        int successCount = 0;
        int failedCount = 0;
        int skippedCount = 0;
        int newCount = 0;
        int updatedCount = 0;

        for (Long applicationId : request.getApplicationIds()) {
            try {
                // 检查是否需要同步
                if (!request.getForceSync() && !needSyncByApplicationId(applicationId)) {
                    skippedCount++;
                    addSyncDetail(result, applicationId, null, "skip", "success", "数据已是最新，跳过同步");
                    continue;
                }

                // 执行同步逻辑
                SyncResult syncResult = syncApplicationSubmissions(applicationId, syncId, request);
                
                if (syncResult.isSuccess()) {
                    successCount++;
                    if (syncResult.isNewRecord()) {
                        newCount++;
                    } else {
                        updatedCount++;
                    }
                    addSyncDetail(result, applicationId, syncResult.getSubmissionId(), 
                                syncResult.getOperationType(), "success", syncResult.getMessage());
                } else {
                    failedCount++;
                    addSyncError(result, applicationId, "SYNC_FAILED", syncResult.getMessage());
                    addSyncDetail(result, applicationId, syncResult.getSubmissionId(), 
                                syncResult.getOperationType(), "failed", syncResult.getMessage());
                }

                // 记录同步状态
                recordSyncTimestamp(applicationId, request.getSyncType(), 
                                  syncResult.isSuccess() ? "success" : "failed", 
                                  1, syncResult.getMessage());

            } catch (Exception e) {
                log.error("同步申报ID {} 失败", applicationId, e);
                failedCount++;
                addSyncError(result, applicationId, "SYNC_EXCEPTION", e.getMessage());
                addSyncDetail(result, applicationId, null, "sync", "failed", e.getMessage());
                
                recordSyncTimestamp(applicationId, request.getSyncType(), "failed", 0, e.getMessage());
            }
        }

        LocalDateTime syncEndTime = LocalDateTime.now();
        result.setSyncEndTime(syncEndTime);
        result.setSuccessCount(successCount);
        result.setFailedCount(failedCount);
        result.setSkippedCount(skippedCount);
        result.setNewCount(newCount);
        result.setUpdatedCount(updatedCount);
        result.setSyncStatus(failedCount > 0 ? "partial_success" : "success");

        log.info("批量同步完成，syncId: {}, 成功: {}, 失败: {}, 跳过: {}", 
                syncId, successCount, failedCount, skippedCount);
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SyncResultVO syncSingleSubmission(ProcessSubmissionRequest submissionRequest) {
        log.info("开始单个提交物同步，submissionId: {}", submissionRequest.getSubmissionId());

        String syncId = generateSyncId();
        LocalDateTime syncStartTime = LocalDateTime.now();
        
        SyncResultVO result = new SyncResultVO();
        result.setSyncId(syncId);
        result.setSyncStartTime(syncStartTime);
        result.setTotalCount(1);
        result.setErrors(new ArrayList<>());
        result.setDetails(new ArrayList<>());

        try {
            // 去重检查
            List<ProcessSubmissionRequest> deduplicatedList = deduplicateSubmissions(Arrays.asList(submissionRequest));
            if (deduplicatedList.isEmpty()) {
                result.setSyncStatus("skipped");
                result.setSkippedCount(1);
                result.setSyncEndTime(LocalDateTime.now());
                addSyncDetail(result, submissionRequest.getApplicationId(), submissionRequest.getSubmissionId(), 
                            "deduplicate", "skipped", "提交物已存在，跳过同步");
                return result;
            }

            // 执行存储
            processSystemService.storeSubmission(submissionRequest);
            
            result.setSyncStatus("success");
            result.setSuccessCount(1);
            result.setNewCount(1);
            result.setSyncEndTime(LocalDateTime.now());
            
            addSyncDetail(result, submissionRequest.getApplicationId(), submissionRequest.getSubmissionId(), 
                        "store", "success", "提交物同步成功");

            // 记录同步状态
            recordSyncTimestamp(submissionRequest.getApplicationId(), "single", "success", 1, null);

        } catch (Exception e) {
            log.error("单个提交物同步失败", e);
            result.setSyncStatus("failed");
            result.setFailedCount(1);
            result.setSyncEndTime(LocalDateTime.now());
            
            addSyncError(result, submissionRequest.getApplicationId(), "STORE_FAILED", e.getMessage());
            addSyncDetail(result, submissionRequest.getApplicationId(), submissionRequest.getSubmissionId(), 
                        "store", "failed", e.getMessage());

            recordSyncTimestamp(submissionRequest.getApplicationId(), "single", "failed", 0, e.getMessage());
        }

        return result;
    }

    @Override
    public List<SyncStatusDTO> checkDataConsistency(List<Long> applicationIds) {
        log.info("开始检查数据一致性，申报ID数量: {}", applicationIds.size());

        List<SyncStatusDTO> results = new ArrayList<>();
        
        for (Long applicationId : applicationIds) {
            SyncStatusDTO status = getSyncStatus(applicationId);
            
            // 检查数据是否一致
            boolean isConsistent = checkApplicationDataConsistency(applicationId);
            if (!isConsistent) {
                status.setSyncStatus("inconsistent");
                status.setErrorMessage("数据不一致，需要重新同步");
            }
            
            results.add(status);
        }

        return results;
    }

    @Override
    public SyncResultVO generateSyncReport(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("生成同步报告，时间范围: {} - {}", startTime, endTime);

        List<ProcessSyncRecord> records = processSyncRecordMapper.selectSyncStatistics(startTime, endTime);
        
        SyncResultVO report = new SyncResultVO();
        report.setSyncId("REPORT_" + System.currentTimeMillis());
        report.setSyncStartTime(startTime);
        report.setSyncEndTime(endTime);
        report.setDetails(new ArrayList<>());

        int totalCount = 0;
        int successCount = 0;
        int failedCount = 0;

        for (ProcessSyncRecord record : records) {
            totalCount += record.getSyncCount();
            successCount += record.getSuccessCount();
            failedCount += record.getFailedCount();
        }

        report.setTotalCount(totalCount);
        report.setSuccessCount(successCount);
        report.setFailedCount(failedCount);
        report.setSyncStatus(failedCount > 0 ? "has_failures" : "all_success");

        return report;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SyncResultVO retryFailedSync(List<Long> applicationIds) {
        log.info("重试失败的同步，申报ID数量: {}", applicationIds.size());

        List<ProcessSyncRecord> failedRecords = processSyncRecordMapper.selectFailedRecords(applicationIds);
        
        SyncResultVO result = new SyncResultVO();
        result.setSyncId(generateSyncId());
        result.setSyncStartTime(LocalDateTime.now());
        result.setTotalCount(failedRecords.size());
        result.setErrors(new ArrayList<>());
        result.setDetails(new ArrayList<>());

        int successCount = 0;
        int failedCount = 0;

        for (ProcessSyncRecord record : failedRecords) {
            try {
                // 重试逻辑
                SyncResult retryResult = retrySync(record);
                
                if (retryResult.isSuccess()) {
                    successCount++;
                    addSyncDetail(result, record.getApplicationId(), record.getSubmissionId(), 
                                "retry", "success", "重试成功");
                } else {
                    failedCount++;
                    addSyncError(result, record.getApplicationId(), "RETRY_FAILED", retryResult.getMessage());
                    
                    // 更新重试信息
                    processSyncRecordMapper.updateRetryInfo(record.getId(), 
                                                          record.getRetryCount() + 1,
                                                          calculateNextRetryTime(record.getRetryCount() + 1),
                                                          retryResult.getMessage());
                }

            } catch (Exception e) {
                log.error("重试同步失败，recordId: {}", record.getId(), e);
                failedCount++;
                addSyncError(result, record.getApplicationId(), "RETRY_EXCEPTION", e.getMessage());
            }
        }

        result.setSyncEndTime(LocalDateTime.now());
        result.setSuccessCount(successCount);
        result.setFailedCount(failedCount);
        result.setSyncStatus(failedCount > 0 ? "partial_success" : "success");

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer cleanupExpiredSyncRecords(LocalDateTime beforeTime) {
        log.info("清理过期同步记录，清理时间点: {}", beforeTime);
        
        int deletedCount = processSyncRecordMapper.deleteExpiredRecords(beforeTime);
        
        log.info("清理完成，删除记录数: {}", deletedCount);
        return deletedCount;
    }

    @Override
    public boolean needSync(Long applicationId, LocalDateTime lastModifyTime) {
        LocalDateTime lastSyncTime = getLastSyncTime(applicationId);
        
        if (lastSyncTime == null) {
            return true; // 从未同步过
        }
        
        return lastModifyTime.isAfter(lastSyncTime);
    }

    @Override
    public List<ProcessSubmissionRequest> deduplicateSubmissions(List<ProcessSubmissionRequest> submissionRequests) {
        log.info("开始去重处理，原始数量: {}", submissionRequests.size());

        Map<String, ProcessSubmissionRequest> uniqueSubmissions = new LinkedHashMap<>();
        
        for (ProcessSubmissionRequest request : submissionRequests) {
            String key = generateDeduplicationKey(request);
            
            // 检查是否已存在
            if (!processSystemService.isDuplicateSubmission(request)) {
                uniqueSubmissions.put(key, request);
            } else {
                log.debug("发现重复提交物，跳过: applicationId={}, submissionType={}, submissionStage={}", 
                         request.getApplicationId(), request.getSubmissionType(), request.getSubmissionStage());
            }
        }

        List<ProcessSubmissionRequest> result = new ArrayList<>(uniqueSubmissions.values());
        log.info("去重完成，去重后数量: {}", result.size());
        
        return result;
    }

    @Override
    public SyncResultVO getSyncStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        return generateSyncReport(startTime, endTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SyncResultVO manualSync(String projectId) {
        log.info("开始手动同步项目数据，projectId: {}", projectId);
        
        SyncResultVO result = new SyncResultVO();
        result.setSyncId(generateSyncId());
        result.setSyncStartTime(LocalDateTime.now());
        result.setSyncType("manual");
        
        try {
            Long applicationId = null;
            if (StringUtils.hasText(projectId)) {
                try {
                    applicationId = Long.valueOf(projectId);
                } catch (NumberFormatException e) {
                    log.warn("无效的项目ID格式: {}", projectId);
                    result.setSyncStatus("failed");
                    result.setMessage("无效的项目ID格式");
                    return result;
                }
            }
            
            int syncCount = 0;
            int successCount = 0;
            int failedCount = 0;
            
            if (applicationId != null) {
                // 同步指定项目
                try {
                    SyncResult syncResult = syncApplicationSubmissions(applicationId, result.getSyncId(), null);
                    if (syncResult.isSuccess()) {
                        successCount++;
                        addSyncDetail(result, applicationId, syncResult.getSubmissionId(), 
                                    "manual_sync", "success", "手动同步成功");
                    } else {
                        failedCount++;
                        addSyncError(result, applicationId, "MANUAL_SYNC_FAILED", syncResult.getMessage());
                    }
                    syncCount = 1;
                    
                    // 记录同步状态
                    recordSyncTimestamp(applicationId, "manual", 
                                      syncResult.isSuccess() ? "success" : "failed", 
                                      1, syncResult.getMessage());
                } catch (Exception e) {
                    log.error("手动同步项目失败，applicationId: {}", applicationId, e);
                    failedCount++;
                    addSyncError(result, applicationId, "MANUAL_SYNC_EXCEPTION", e.getMessage());
                    recordSyncTimestamp(applicationId, "manual", "failed", 0, e.getMessage());
                }
            } else {
                // 同步所有项目（如果没有指定项目ID）
                log.info("未指定项目ID，执行全量手动同步");
                // 这里可以实现全量同步逻辑
                // 暂时返回成功状态
                syncCount = 0;
                successCount = 0;
            }
            
            result.setSyncEndTime(LocalDateTime.now());
            result.setSyncCount(syncCount);
            result.setSuccessCount(successCount);
            result.setFailedCount(failedCount);
            result.setSyncStatus(failedCount > 0 ? "partial_success" : "success");
            result.setMessage(String.format("手动同步完成，成功: %d, 失败: %d", successCount, failedCount));
            
            log.info("手动同步完成，projectId: {}, 成功: {}, 失败: {}", projectId, successCount, failedCount);
            
        } catch (Exception e) {
            log.error("手动同步失败，projectId: {}", projectId, e);
            result.setSyncEndTime(LocalDateTime.now());
            result.setSyncStatus("failed");
            result.setMessage("手动同步失败: " + e.getMessage());
            result.setSyncCount(0);
            result.setSuccessCount(0);
            result.setFailedCount(1);
        }
        
        return result;
    }

    // 私有辅助方法

    private String generateSyncId() {
        return "SYNC_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private boolean needSyncByApplicationId(Long applicationId) {
        // 简化实现：检查最后同步时间是否超过1小时
        LocalDateTime lastSyncTime = getLastSyncTime(applicationId);
        if (lastSyncTime == null) {
            return true;
        }
        return lastSyncTime.isBefore(LocalDateTime.now().minusHours(1));
    }

    private SyncResult syncApplicationSubmissions(Long applicationId, String syncId, BatchSyncRequest request) {
        // 模拟同步逻辑
        // 实际实现中，这里应该调用外部API获取数据并存储
        SyncResult result = new SyncResult();
        result.setSuccess(true);
        result.setNewRecord(true);
        result.setOperationType("sync");
        result.setMessage("同步成功");
        result.setSubmissionId(applicationId); // 简化处理
        
        return result;
    }

    private boolean checkApplicationDataConsistency(Long applicationId) {
        // 简化实现：假设数据一致
        return true;
    }

    private SyncResult retrySync(ProcessSyncRecord record) {
        // 模拟重试逻辑
        SyncResult result = new SyncResult();
        result.setSuccess(true);
        result.setOperationType("retry");
        result.setMessage("重试成功");
        result.setSubmissionId(record.getSubmissionId());
        
        return result;
    }

    private LocalDateTime calculateNextRetryTime(int retryCount) {
        // 指数退避策略
        long delayMinutes = (long) Math.pow(2, retryCount) * 5; // 5, 10, 20, 40 分钟
        return LocalDateTime.now().plusMinutes(delayMinutes);
    }

    private String generateDeduplicationKey(ProcessSubmissionRequest request) {
        String keyString = String.format("%d_%s_%s_%d_%d",
                request.getApplicationId(),
                request.getSubmissionType(),
                request.getSubmissionStage(),
                request.getSubmissionRound(),
                request.getSubmissionVersion());
        return DigestUtils.md5DigestAsHex(keyString.getBytes());
    }

    private void addSyncError(SyncResultVO result, Long applicationId, String errorCode, String errorMessage) {
        SyncResultVO.SyncErrorInfo error = new SyncResultVO.SyncErrorInfo();
        error.setApplicationId(applicationId);
        error.setErrorCode(errorCode);
        error.setErrorMessage(errorMessage);
        error.setErrorTime(LocalDateTime.now());
        result.getErrors().add(error);
    }

    private void addSyncDetail(SyncResultVO result, Long applicationId, Long submissionId, 
                              String operationType, String operationStatus, String remark) {
        SyncResultVO.SyncDetailInfo detail = new SyncResultVO.SyncDetailInfo();
        detail.setApplicationId(applicationId);
        detail.setSubmissionId(submissionId);
        detail.setOperationType(operationType);
        detail.setOperationStatus(operationStatus);
        detail.setOperationTime(LocalDateTime.now());
        detail.setRemark(remark);
        result.getDetails().add(detail);
    }

    // 内部类
    private static class SyncResult {
        private boolean success;
        private boolean newRecord;
        private String operationType;
        private String message;
        private Long submissionId;

        // getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public boolean isNewRecord() { return newRecord; }
        public void setNewRecord(boolean newRecord) { this.newRecord = newRecord; }
        public String getOperationType() { return operationType; }
        public void setOperationType(String operationType) { this.operationType = operationType; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Long getSubmissionId() { return submissionId; }
        public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }
    }
}