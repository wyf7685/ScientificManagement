package com.achievement.controller;

import com.achievement.domain.dto.ProcessApiLogQueryDTO;
import com.achievement.domain.po.ProcessApiLog;
import com.achievement.domain.vo.ProcessApiLogVO;
import com.achievement.mapper.ProcessApiLogMapper;
import com.achievement.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 过程系统日志查询控制器
 * 
 * @author system
 * @since 2026-01-24
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/process-system/logs")
@RequiredArgsConstructor
@Validated
@Tag(name = "过程系统日志", description = "过程系统API访问日志查询接口")
public class ProcessSystemLogController {

    private final ProcessApiLogMapper processApiLogMapper;

    /**
     * 分页查询API访问日志
     * 
     * @param queryDTO 查询参数
     * @return 日志列表
     */
    @GetMapping
    @Operation(summary = "分页查询API访问日志", description = "根据条件分页查询过程系统API访问日志")
    public Result<Map<String, Object>> getApiLogs(ProcessApiLogQueryDTO queryDTO) {
        
        log.info("查询API访问日志: page={}, pageSize={}, apiKey={}, method={}, responseCode={}", 
                queryDTO.getPage(), queryDTO.getPageSize(), queryDTO.getApiKey(), 
                queryDTO.getMethod(), queryDTO.getResponseCode());
        
        try {
            // 创建分页对象
            Page<ProcessApiLog> pageObj = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
            
            // 分页查询
            Page<ProcessApiLog> result = processApiLogMapper.selectPageByConditions(
                    pageObj, queryDTO.getApiKey(), queryDTO.getMethod(), queryDTO.getResponseCode(), 
                    queryDTO.getOperationType(), queryDTO.getOperationResult(), 
                    queryDTO.getStartTime(), queryDTO.getEndTime());
            
            // 转换为VO
            List<ProcessApiLogVO> records = result.getRecords().stream()
                    .map(ProcessApiLogVO::fromEntity)
                    .collect(Collectors.toList());
            
            // 构建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("records", records);
            responseData.put("total", result.getTotal());
            responseData.put("current", result.getCurrent());
            responseData.put("size", result.getSize());
            responseData.put("pages", result.getPages());
            
            log.info("查询API访问日志成功: total={}, current={}", result.getTotal(), result.getCurrent());
            
            return Result.success(responseData);
            
        } catch (Exception e) {
            log.error("查询API访问日志失败: error={}", e.getMessage(), e);
            return Result.error("查询API访问日志失败: " + e.getMessage());
        }
    }

    /**
     * 根据请求ID获取日志详情
     * 
     * @param requestId 请求ID
     * @return 日志详情
     */
    @GetMapping("/{requestId}")
    @Operation(summary = "获取日志详情", description = "根据请求ID获取API访问日志详情")
    public Result<ProcessApiLog> getLogDetail(
            @Parameter(description = "请求ID") @PathVariable String requestId) {
        
        log.info("获取日志详情: requestId={}", requestId);
        
        try {
            // 根据请求ID查询日志
            ProcessApiLog apiLog = processApiLogMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProcessApiLog>()
                            .eq(ProcessApiLog::getRequestId, requestId));
            
            if (apiLog == null) {
                log.warn("日志不存在: requestId={}", requestId);
                return Result.error(404, "日志不存在");
            }
            
            log.info("获取日志详情成功: requestId={}, method={}, url={}", 
                    requestId, apiLog.getMethod(), apiLog.getUrl());
            
            return Result.success(apiLog);
            
        } catch (Exception e) {
            log.error("获取日志详情失败: requestId={}, error={}", requestId, e.getMessage(), e);
            return Result.error("获取日志详情失败: " + e.getMessage());
        }
    }

    /**
     * 统计API访问次数
     * 
     * @param apiKey API密钥
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 访问次数统计
     */
    @GetMapping("/stats/count")
    @Operation(summary = "统计API访问次数", description = "根据API密钥和时间范围统计访问次数")
    public Result<Map<String, Object>> getAccessCount(
            @Parameter(description = "API密钥") @RequestParam String apiKey,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        log.info("统计API访问次数: apiKey={}, startTime={}, endTime={}", apiKey, startTime, endTime);
        
        try {
            // 统计访问次数
            Long count = processApiLogMapper.countByApiKey(apiKey, startTime, endTime);
            
            // 构建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("apiKey", apiKey);
            responseData.put("count", count);
            responseData.put("startTime", startTime);
            responseData.put("endTime", endTime);
            
            log.info("统计API访问次数成功: apiKey={}, count={}", apiKey, count);
            
            return Result.success(responseData);
            
        } catch (Exception e) {
            log.error("统计API访问次数失败: apiKey={}, error={}", apiKey, e.getMessage(), e);
            return Result.error("统计API访问次数失败: " + e.getMessage());
        }
    }

    /**
     * 获取最近的错误日志
     * 
     * @param limit 限制数量
     * @return 错误日志列表
     */
    @GetMapping("/errors/recent")
    @Operation(summary = "获取最近的错误日志", description = "获取最近发生的API错误日志")
    public Result<List<ProcessApiLogVO>> getRecentErrors(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") Integer limit) {
        
        log.info("获取最近的错误日志: limit={}", limit);
        
        try {
            // 查询最近的错误日志
            List<ProcessApiLog> errorLogs = processApiLogMapper.selectRecentErrors(limit);
            
            // 转换为VO
            List<ProcessApiLogVO> errorLogVOs = errorLogs.stream()
                    .map(ProcessApiLogVO::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("获取最近的错误日志成功: count={}", errorLogVOs.size());
            
            return Result.success(errorLogVOs);
            
        } catch (Exception e) {
            log.error("获取最近的错误日志失败: error={}", e.getMessage(), e);
            return Result.error("获取最近的错误日志失败: " + e.getMessage());
        }
    }

    /**
     * 获取响应时间统计
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 响应时间统计
     */
    @GetMapping("/stats/response-time")
    @Operation(summary = "获取响应时间统计", description = "获取指定时间范围内的响应时间统计")
    public Result<Map<String, Object>> getResponseTimeStats(
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        log.info("获取响应时间统计: startTime={}, endTime={}", startTime, endTime);
        
        try {
            // 查询响应时间统计
            List<ProcessApiLog> logs = processApiLogMapper.selectResponseTimeStats(startTime, endTime);
            
            // 计算统计数据
            Map<String, Object> stats = calculateResponseTimeStats(logs);
            
            log.info("获取响应时间统计成功: count={}", logs.size());
            
            return Result.success(stats);
            
        } catch (Exception e) {
            log.error("获取响应时间统计失败: error={}", e.getMessage(), e);
            return Result.error("获取响应时间统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取操作类型统计
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 操作类型统计
     */
    @GetMapping("/stats/operations")
    @Operation(summary = "获取操作类型统计", description = "获取指定时间范围内的操作类型统计")
    public Result<Map<String, Object>> getOperationStats(
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        log.info("获取操作类型统计: startTime={}, endTime={}", startTime, endTime);
        
        try {
            // 查询操作统计数据
            List<ProcessApiLog> logs = processApiLogMapper.selectResponseTimeStats(startTime, endTime);
            
            // 按操作类型分组统计
            Map<String, Long> operationCounts = logs.stream()
                    .collect(Collectors.groupingBy(
                            log -> log.getOperationType() != null ? log.getOperationType() : "UNKNOWN",
                            Collectors.counting()));
            
            // 按操作结果分组统计
            Map<String, Long> resultCounts = logs.stream()
                    .collect(Collectors.groupingBy(
                            log -> log.getOperationResult() != null ? log.getOperationResult() : "unknown",
                            Collectors.counting()));
            
            // 构建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("operationCounts", operationCounts);
            responseData.put("resultCounts", resultCounts);
            responseData.put("totalCount", logs.size());
            responseData.put("startTime", startTime);
            responseData.put("endTime", endTime);
            
            log.info("获取操作类型统计成功: totalCount={}", logs.size());
            
            return Result.success(responseData);
            
        } catch (Exception e) {
            log.error("获取操作类型统计失败: error={}", e.getMessage(), e);
            return Result.error("获取操作类型统计失败: " + e.getMessage());
        }
    }

    /**
     * 计算响应时间统计数据
     */
    private Map<String, Object> calculateResponseTimeStats(List<ProcessApiLog> logs) {
        Map<String, Object> stats = new HashMap<>();
        
        if (logs.isEmpty()) {
            stats.put("count", 0);
            stats.put("avgResponseTime", 0);
            stats.put("maxResponseTime", 0);
            stats.put("minResponseTime", 0);
            return stats;
        }
        
        int count = logs.size();
        int sum = logs.stream().mapToInt(ProcessApiLog::getResponseTime).sum();
        int max = logs.stream().mapToInt(ProcessApiLog::getResponseTime).max().orElse(0);
        int min = logs.stream().mapToInt(ProcessApiLog::getResponseTime).min().orElse(0);
        double avg = (double) sum / count;
        
        stats.put("count", count);
        stats.put("avgResponseTime", Math.round(avg * 100.0) / 100.0);
        stats.put("maxResponseTime", max);
        stats.put("minResponseTime", min);
        stats.put("totalResponseTime", sum);
        
        // 响应时间分布
        Map<String, Long> distribution = logs.stream()
                .collect(Collectors.groupingBy(
                        log -> getResponseTimeRange(log.getResponseTime()),
                        Collectors.counting()));
        
        stats.put("distribution", distribution);
        
        return stats;
    }
    
    /**
     * 获取响应时间范围
     */
    private String getResponseTimeRange(Integer responseTime) {
        if (responseTime == null) {
            return "unknown";
        }
        
        if (responseTime < 100) {
            return "0-100ms";
        } else if (responseTime < 500) {
            return "100-500ms";
        } else if (responseTime < 1000) {
            return "500ms-1s";
        } else if (responseTime < 2000) {
            return "1-2s";
        } else if (responseTime < 5000) {
            return "2-5s";
        } else {
            return "5s+";
        }
    }
}