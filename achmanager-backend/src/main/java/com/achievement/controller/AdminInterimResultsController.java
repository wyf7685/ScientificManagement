package com.achievement.controller;

import com.achievement.domain.po.ProcessSubmissionFile;
import com.achievement.domain.vo.*;
import com.achievement.result.Result;
import com.achievement.service.IExcelExportService;
import com.achievement.service.IInterimResultFileService;
import com.achievement.service.IInterimResultViewService;
import com.achievement.service.ISyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 管理员中期成果物控制器
 * 
 * @author system
 * @since 2026-01-24
 */
@Slf4j
@RestController
@RequestMapping({"/api/v1/interim-results", "/interim-results"})
@RequiredArgsConstructor
@Validated
@Tag(name = "管理员中期成果物", description = "管理员中期成果物管理相关接口")
public class AdminInterimResultsController {

    private final IInterimResultViewService interimResultViewService;
    private final IInterimResultFileService interimResultFileService;
    private final ISyncService syncService;
    private final IExcelExportService excelExportService;

    /**
     * 获取中期成果物统计数据
     * 
     * @return 统计数据
     */
    @GetMapping("/stats")
    @Operation(summary = "获取中期成果物统计数据", description = "获取项目总数、成果物总数等统计信息")
    public Result<InterimResultStatsVO> getStats() {
        log.info("接收到获取中期成果物统计数据请求");
        
        try {
            InterimResultStatsVO stats = interimResultViewService.getStats();
            
            log.info("获取中期成果物统计数据成功: totalProjects={}, totalResults={}", 
                    stats.getTotalProjects(), stats.getTotalResults());
            
            return Result.success(stats);
            
        } catch (Exception e) {
            log.error("获取中期成果物统计数据失败: error={}", e.getMessage(), e);
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取中期成果物列表
     * 
     * @param projectId 项目ID
     * @param type 成果物类型
     * @param projectName 项目名称
     * @param submitter 提交者
     * @param page 页码
     * @param pageSize 页大小
     * @return 成果物列表
     */
    @GetMapping
    @Operation(summary = "获取中期成果物列表", description = "分页查询中期成果物列表，支持多种筛选条件")
    public Result<InterimResultListVO> getInterimResults(
            @Parameter(description = "项目ID") @RequestParam(required = false) String projectId,
            @Parameter(description = "成果物类型") @RequestParam(required = false) String type,
            @Parameter(description = "项目名称") @RequestParam(required = false) String projectName,
            @Parameter(description = "提交者") @RequestParam(required = false) String submitter,
            @Parameter(description = "关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "年份") @RequestParam(required = false) String year,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        
        log.info("接收到获取中期成果物列表请求: projectId={}, type={}, page={}, pageSize={}", 
                projectId, type, page, pageSize);
        
        try {
            InterimResultListVO response = interimResultViewService.getInterimResults(
                    projectId, type, projectName, submitter, keyword, year, page, pageSize);
            
            log.info("获取中期成果物列表成功: total={}, page={}, pageSize={}", 
                    response.getTotal(), page, pageSize);
            
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("获取中期成果物列表失败: error={}", e.getMessage(), e);
            return Result.error("获取成果物列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取中期成果物详情
     * 
     * @param id 成果物ID
     * @return 成果物详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取中期成果物详情", description = "根据成果物ID获取详细信息")
    public Result<InterimResultVO> getInterimResultDetail(
            @Parameter(description = "成果物ID") @PathVariable @NotBlank String id) {
        
        log.info("接收到获取中期成果物详情请求: id={}", id);
        
        try {
            InterimResultVO result = interimResultViewService.getDetailVO(id);

            if (result == null) {
                log.warn("中期成果物不存在: id={}", id);
                return Result.error(404, "成果物不存在");
            }
            
            log.info("获取中期成果物详情成功: id={}, projectName={}", id, result.getProjectName());
            
            return Result.success(result);
            
        } catch (NumberFormatException e) {
            log.warn("无效的成果物ID格式: {}", id);
            return Result.error(400, "无效的成果物ID格式");
        } catch (Exception e) {
            log.error("获取中期成果物详情失败: id={}, error={}", id, e.getMessage(), e);
            return Result.error("获取成果物详情失败: " + e.getMessage());
        }
    }

    /**
     * 手动同步中期成果物
     * 
     * @param request 同步请求
     * @return 同步结果
     */
    @PostMapping("/sync")
    @Operation(summary = "手动同步中期成果物", description = "手动触发从过程管理系统同步数据")
    public Result<SyncResultVO> syncInterimResults(@Valid @RequestBody SyncRequestVO request) {
        log.info("接收到手动同步中期成果物请求: projectId={}", request.getProjectId());
        
        try {
            // 执行同步操作
            SyncResultVO result = syncService.manualSync(request.getProjectId());
            
            log.info("手动同步中期成果物成功: projectId={}, syncCount={}", 
                    request.getProjectId(), result.getSyncCount());
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("手动同步中期成果物失败: projectId={}, error={}", 
                    request.getProjectId(), e.getMessage(), e);
            return Result.error("同步失败: " + e.getMessage());
        }
    }

    /**
     * 导出中期成果物列表
     * 
     * @param projectId 项目ID
     * @param type 成果物类型
     * @param projectName 项目名称
     * @param submitter 提交者
     * @param response HTTP响应
     */
    @GetMapping("/export")
    @Operation(summary = "导出中期成果物列表", description = "导出Excel格式的中期成果物列表")
    public void exportInterimResults(
            @Parameter(description = "项目ID") @RequestParam(required = false) String projectId,
            @Parameter(description = "成果物类型") @RequestParam(required = false) String type,
            @Parameter(description = "项目名称") @RequestParam(required = false) String projectName,
            @Parameter(description = "提交者") @RequestParam(required = false) String submitter,
            @Parameter(description = "关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "年份") @RequestParam(required = false) String year,
            HttpServletResponse response) throws IOException {
        
        log.info("接收到导出中期成果物列表请求: projectId={}, type={}", projectId, type);
        
        try {
            List<InterimResultVO> interimResults = interimResultViewService.listInterimResults(
                    projectId, type, projectName, submitter, keyword, year);
            
            // 生成Excel文件
            String fileName = "interim_results_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            
            // 使用Excel导出服务生成文件
            try (ByteArrayOutputStream excelStream = excelExportService.exportInterimResultsToExcel(interimResults)) {
                response.getOutputStream().write(excelStream.toByteArray());
                response.getOutputStream().flush();
            }
            
            log.info("导出中期成果物列表成功: count={}, fileName={}", interimResults.size(), fileName);
            
        } catch (Exception e) {
            log.error("导出中期成果物列表失败: error={}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("导出失败: " + e.getMessage());
        }
    }

    /**
     * 获取附件下载链接
     * 
     * @param attachmentId 附件ID
     * @return 文件流或重定向
     */
    @GetMapping("/attachments/{attachmentId}/download")
    @Operation(summary = "下载附件", description = "根据附件ID下载文件")
    public ResponseEntity<Resource> downloadAttachment(
            @Parameter(description = "附件ID") @PathVariable @NotBlank String attachmentId) {
        
        log.info("接收到下载附件请求: attachmentId={}", attachmentId);
        
        try {
            ProcessSubmissionFile file = interimResultFileService.getFileMetadata(attachmentId);
            if (file == null || "1".equals(file.getDelFlag())) {
                log.warn("附件不存在: attachmentId={}", attachmentId);
                return ResponseEntity.notFound().build();
            }

            Resource resource = interimResultFileService.getFileResource(file.getFileUrl());
            if (resource == null || !resource.exists()) {
                log.warn("附件文件不存在: attachmentId={}", attachmentId);
                return ResponseEntity.notFound().build();
            }

            String fileName = StringUtils.hasText(file.getFileName())
                    ? file.getFileName()
                    : "attachment_" + attachmentId;
            String encodedFileName = UriUtils.encode(fileName, StandardCharsets.UTF_8);

            log.info("下载附件成功: attachmentId={}, fileName={}", attachmentId, fileName);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                    .body(resource);
            
        } catch (Exception e) {
            log.error("下载附件失败: attachmentId={}, error={}", attachmentId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 将前端类型映射为数据库提交物类型
     */
}
