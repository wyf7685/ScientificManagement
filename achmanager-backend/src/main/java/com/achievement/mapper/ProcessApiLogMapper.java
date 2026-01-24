package com.achievement.mapper;

import com.achievement.domain.po.ProcessApiLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 过程系统API访问日志表 Mapper 接口
 * </p>
 *
 * @author system
 * @since 2026-01-24
 */
public interface ProcessApiLogMapper extends BaseMapper<ProcessApiLog> {

    /**
     * 分页查询API日志
     */
    Page<ProcessApiLog> selectPageByConditions(Page<?> page,
                                             @Param("apiKey") String apiKey,
                                             @Param("method") String method,
                                             @Param("responseCode") Integer responseCode,
                                             @Param("operationType") String operationType,
                                             @Param("operationResult") String operationResult,
                                             @Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);

    /**
     * 根据API密钥统计请求次数
     */
    Long countByApiKey(@Param("apiKey") String apiKey, 
                      @Param("startTime") LocalDateTime startTime,
                      @Param("endTime") LocalDateTime endTime);

    /**
     * 查询最近的错误日志
     */
    List<ProcessApiLog> selectRecentErrors(@Param("limit") Integer limit);

    /**
     * 统计响应时间分布
     */
    List<ProcessApiLog> selectResponseTimeStats(@Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);
}