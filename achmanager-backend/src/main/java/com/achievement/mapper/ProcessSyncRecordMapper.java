package com.achievement.mapper;

import com.achievement.domain.po.ProcessSyncRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 过程系统同步记录表 Mapper 接口
 * </p>
 *
 * @author system
 * @since 2026-01-24
 */
public interface ProcessSyncRecordMapper extends BaseMapper<ProcessSyncRecord> {

    /**
     * 根据申报ID获取最新同步记录
     */
    ProcessSyncRecord selectLatestByApplicationId(@Param("applicationId") Long applicationId);

    /**
     * 根据申报ID和同步类型获取最新同步记录
     */
    ProcessSyncRecord selectLatestByApplicationIdAndType(@Param("applicationId") Long applicationId,
                                                        @Param("syncType") String syncType);

    /**
     * 分页查询同步记录
     */
    Page<ProcessSyncRecord> selectPageByConditions(Page<?> page,
                                                  @Param("applicationId") Long applicationId,
                                                  @Param("syncType") String syncType,
                                                  @Param("syncStatus") String syncStatus,
                                                  @Param("operationType") String operationType,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 统计同步记录
     */
    List<ProcessSyncRecord> selectSyncStatistics(@Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * 查询失败的同步记录
     */
    List<ProcessSyncRecord> selectFailedRecords(@Param("applicationIds") List<Long> applicationIds);

    /**
     * 查询需要重试的同步记录
     */
    List<ProcessSyncRecord> selectRetryRecords(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 删除过期的同步记录
     */
    int deleteExpiredRecords(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 根据同步ID查询记录
     */
    List<ProcessSyncRecord> selectBySyncId(@Param("syncId") String syncId);

    /**
     * 更新重试信息
     */
    int updateRetryInfo(@Param("id") Long id,
                       @Param("retryCount") Integer retryCount,
                       @Param("nextRetryTime") LocalDateTime nextRetryTime,
                       @Param("errorMessage") String errorMessage);
}