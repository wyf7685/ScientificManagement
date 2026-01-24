package com.achievement.controller;

import com.achievement.domain.dto.ProcessSubmissionQueryDTO;
import com.achievement.domain.dto.ProcessSubmissionRequest;
import com.achievement.domain.po.ProcessSubmissionFile;
import com.achievement.domain.vo.ProcessSubmissionVO;
import com.achievement.result.Result;
import com.achievement.service.IProcessSystemService;
import com.achievement.service.ProcessSystemSecurityService;
import com.achievement.service.ProcessSystemValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 过程系统集成控制器
 * 
 * @author system
 * @since 2026-01-24
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/process-system")
@RequiredArgsConstructor
@Validated
@Tag(name = "过程系统集成", description = "过程管理系统集成相关接口")
@SecurityRequirement(name = "API Key Authentication")
public class ProcessSystemController {

    private final IProcessSystemService processSystemService;
    private final ProcessSystemSecurityService securityService;
    private final ProcessSystemValidationService validationService;

    /**
     * 存储项目提交物
     * 
     * @param request 提交物存储请求
     * @param httpRequest HTTP请求
     * @return 存储结果
     */
    @PostMapping("/submissions")
    @Operation(summary = "存储项目提交物", description = "接收过程管理系统的项目提交物并存储到成果管理系统")
    public Result<Map<String, Object>> storeSubmission(
            @Valid @RequestBody ProcessSubmissionRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("接收到存储提交物请求: submissionId={}, applicationId={}, clientIp={}", 
                request.getSubmissionId(), request.getApplicationId(), 
                securityService.getClientIpAddress(httpRequest));
        
        // 验证请求数据
        validationService.validateSubmissionRequest(request);
        
        try {
            // 额外的安全检查
            if (securityService.isSuspiciousRequest(httpRequest)) {
                log.warn("检测到可疑请求，拒绝处理: submissionId={}", request.getSubmissionId());
                return Result.error(10006, "请求被安全策略拒绝");
            }

            // 存储提交物
            ProcessSubmissionVO result = processSystemService.storeSubmission(request);
            
            // 构建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("submission_id", result.getSubmissionId());
            responseData.put("application_id", result.getApplicationId());
            responseData.put("sync_time", result.getSyncTime());
            
            log.info("提交物存储成功: submissionId={}, applicationId={}", 
                    result.getSubmissionId(), result.getApplicationId());
            
            return Result.success(responseData);
            
        } catch (Exception e) {
            log.error("存储提交物失败: submissionId={}, applicationId={}, error={}", 
                    request.getSubmissionId(), request.getApplicationId(), e.getMessage(), e);
            return Result.error("存储提交物失败: " + e.getMessage());
        }
    }

    /**
     * 检索项目提交物列表
     * 
     * @param applicationId 申报ID
     * @param submissionStage 提交阶段
     * @param submissionType 提交物类型
     * @param projectName 项目名称
     * @param applicantName 申报人姓名
     * @return 提交物列表
     */
    @GetMapping("/submissions")
    @Operation(summary = "检索项目提交物列表", description = "根据查询条件检索项目提交物列表")
    public Result<Map<String, Object>> getSubmissions(
            @Parameter(description = "申报ID") @RequestParam(required = false) Long applicationId,
            @Parameter(description = "提交阶段") @RequestParam(required = false) String submissionStage,
            @Parameter(description = "提交物类型") @RequestParam(required = false) String submissionType,
            @Parameter(description = "项目名称") @RequestParam(required = false) String projectName,
            @Parameter(description = "申报人姓名") @RequestParam(required = false) String applicantName) {
        
        log.info("接收到查询提交物列表请求: applicationId={}, submissionStage={}, submissionType={}", 
                applicationId, submissionStage, submissionType);
        
        // 验证查询参数
        validationService.validateQueryParams(applicationId, submissionStage, submissionType);
        
        try {
            // 构建查询条件
            ProcessSubmissionQueryDTO queryDTO = new ProcessSubmissionQueryDTO();
            queryDTO.setApplicationId(applicationId);
            queryDTO.setSubmissionStage(submissionStage);
            queryDTO.setSubmissionType(submissionType);
            queryDTO.setProjectName(projectName);
            queryDTO.setApplicantName(applicantName);
            
            // 查询提交物列表
            List<ProcessSubmissionVO> submissions = processSystemService.getSubmissions(queryDTO);
            
            // 统计总数
            Long total = processSystemService.countSubmissions(queryDTO);
            
            // 构建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("total", total);
            responseData.put("submissions", submissions);
            
            log.info("查询提交物列表成功: total={}", total);
            
            return Result.success(responseData);
            
        } catch (Exception e) {
            log.error("查询提交物列表失败: applicationId={}, error={}", applicationId, e.getMessage(), e);
            return Result.error("查询提交物列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取提交物详情
     * 
     * @param submissionId 提交物ID
     * @return 提交物详情
     */
    @GetMapping("/submissions/{submissionId}")
    @Operation(summary = "获取提交物详情", description = "根据提交物ID获取详细信息")
    public Result<ProcessSubmissionVO> getSubmissionDetail(
            @Parameter(description = "提交物ID") @PathVariable @NotNull Long submissionId) {
        
        log.info("接收到获取提交物详情请求: submissionId={}", submissionId);
        
        // 验证提交物ID
        validationService.validateSubmissionId(submissionId);
        
        try {
            // 获取提交物详情
            ProcessSubmissionVO submission = processSystemService.getSubmissionDetail(submissionId);
            
            if (submission == null) {
                log.warn("提交物不存在: submissionId={}", submissionId);
                return Result.error(404, "提交物不存在");
            }
            
            log.info("获取提交物详情成功: submissionId={}, projectName={}", 
                    submissionId, submission.getProjectName());
            
            return Result.success(submission);
            
        } catch (Exception e) {
            log.error("获取提交物详情失败: submissionId={}, error={}", submissionId, e.getMessage(), e);
            return Result.error("获取提交物详情失败: " + e.getMessage());
        }
    }

    /**
     * 根据申报ID获取提交物列表
     * 
     * @param applicationId 申报ID
     * @return 提交物列表
     */
    @GetMapping("/applications/{applicationId}/submissions")
    @Operation(summary = "根据申报ID获取提交物列表", description = "获取指定申报的所有提交物")
    public Result<List<ProcessSubmissionVO>> getSubmissionsByApplicationId(
            @Parameter(description = "申报ID") @PathVariable @NotNull Long applicationId) {
        
        log.info("接收到根据申报ID查询提交物请求: applicationId={}", applicationId);
        
        // 验证申报ID
        validationService.validateQueryParams(applicationId, null, null);
        
        try {
            // 查询提交物列表
            List<ProcessSubmissionVO> submissions = processSystemService.getSubmissionsByApplicationId(applicationId);
            
            log.info("根据申报ID查询提交物成功: applicationId={}, count={}", applicationId, submissions.size());
            
            return Result.success(submissions);
            
        } catch (Exception e) {
            log.error("根据申报ID查询提交物失败: applicationId={}, error={}", applicationId, e.getMessage(), e);
            return Result.error("查询提交物失败: " + e.getMessage());
        }
    }

    /**
     * 根据申报ID和阶段获取提交物列表
     * 
     * @param applicationId 申报ID
     * @param submissionStage 提交阶段
     * @return 提交物列表
     */
    @GetMapping("/applications/{applicationId}/submissions/{submissionStage}")
    @Operation(summary = "根据申报ID和阶段获取提交物列表", description = "获取指定申报和阶段的提交物")
    public Result<List<ProcessSubmissionVO>> getSubmissionsByApplicationIdAndStage(
            @Parameter(description = "申报ID") @PathVariable @NotNull Long applicationId,
            @Parameter(description = "提交阶段") @PathVariable @NotNull String submissionStage) {
        
        log.info("接收到根据申报ID和阶段查询提交物请求: applicationId={}, submissionStage={}", 
                applicationId, submissionStage);
        
        // 验证参数
        validationService.validateQueryParams(applicationId, submissionStage, null);
        
        try {
            // 查询提交物列表
            List<ProcessSubmissionVO> submissions = processSystemService
                    .getSubmissionsByApplicationIdAndStage(applicationId, submissionStage);
            
            log.info("根据申报ID和阶段查询提交物成功: applicationId={}, submissionStage={}, count={}", 
                    applicationId, submissionStage, submissions.size());
            
            return Result.success(submissions);
            
        } catch (Exception e) {
            log.error("根据申报ID和阶段查询提交物失败: applicationId={}, submissionStage={}, error={}", 
                    applicationId, submissionStage, e.getMessage(), e);
            return Result.error("查询提交物失败: " + e.getMessage());
        }
    }

    /**
     * 获取提交物版本历史记录
     * 
     * @param applicationId 申报ID
     * @param submissionType 提交物类型
     * @param submissionStage 提交阶段
     * @param submissionRound 提交轮次
     * @return 版本历史列表
     */
    @GetMapping("/applications/{applicationId}/submissions/{submissionType}/{submissionStage}/rounds/{submissionRound}/versions")
    @Operation(summary = "获取提交物版本历史记录", description = "根据申报ID、类型、阶段和轮次获取版本历史")
    public Result<List<ProcessSubmissionVO>> getSubmissionVersionHistory(
            @Parameter(description = "申报ID") @PathVariable @NotNull Long applicationId,
            @Parameter(description = "提交物类型") @PathVariable @NotNull String submissionType,
            @Parameter(description = "提交阶段") @PathVariable @NotNull String submissionStage,
            @Parameter(description = "提交轮次") @PathVariable @NotNull Integer submissionRound) {
        
        log.info("接收到查询版本历史请求: applicationId={}, type={}, stage={}, round={}", 
                applicationId, submissionType, submissionStage, submissionRound);
        
        // 验证参数
        validationService.validateQueryParams(applicationId, submissionStage, submissionType);
        
        try {
            // 查询版本历史
            List<ProcessSubmissionVO> submissions = processSystemService
                    .getSubmissionVersionHistory(applicationId, submissionType, submissionStage, submissionRound);
            
            log.info("查询版本历史成功: applicationId={}, type={}, stage={}, round={}, count={}", 
                    applicationId, submissionType, submissionStage, submissionRound, submissions.size());
            
            return Result.success(submissions);
            
        } catch (Exception e) {
            log.error("查询版本历史失败: applicationId={}, type={}, stage={}, round={}, error={}", 
                    applicationId, submissionType, submissionStage, submissionRound, e.getMessage(), e);
            return Result.error("查询版本历史失败: " + e.getMessage());
        }
    }

    /**
     * 获取提交物轮次历史记录
     * 
     * @param applicationId 申报ID
     * @param submissionType 提交物类型
     * @param submissionStage 提交阶段
     * @return 轮次历史列表
     */
    @GetMapping("/applications/{applicationId}/submissions/{submissionType}/{submissionStage}/rounds")
    @Operation(summary = "获取提交物轮次历史记录", description = "根据申报ID、类型和阶段获取轮次历史")
    public Result<List<ProcessSubmissionVO>> getSubmissionRoundHistory(
            @Parameter(description = "申报ID") @PathVariable @NotNull Long applicationId,
            @Parameter(description = "提交物类型") @PathVariable @NotNull String submissionType,
            @Parameter(description = "提交阶段") @PathVariable @NotNull String submissionStage) {
        
        log.info("接收到查询轮次历史请求: applicationId={}, type={}, stage={}", 
                applicationId, submissionType, submissionStage);
        
        // 验证参数
        validationService.validateQueryParams(applicationId, submissionStage, submissionType);
        
        try {
            // 查询轮次历史
            List<ProcessSubmissionVO> submissions = processSystemService
                    .getSubmissionRoundHistory(applicationId, submissionType, submissionStage);
            
            log.info("查询轮次历史成功: applicationId={}, type={}, stage={}, count={}", 
                    applicationId, submissionType, submissionStage, submissions.size());
            
            return Result.success(submissions);
            
        } catch (Exception e) {
            log.error("查询轮次历史失败: applicationId={}, type={}, stage={}, error={}", 
                    applicationId, submissionType, submissionStage, e.getMessage(), e);
            return Result.error("查询轮次历史失败: " + e.getMessage());
        }
    }

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
    @GetMapping("/applications/{applicationId}/submissions/{submissionType}/{submissionStage}/rounds/{submissionRound}/versions/{submissionVersion}")
    @Operation(summary = "获取指定版本的提交物详情", description = "根据完整版本信息获取提交物详情")
    public Result<ProcessSubmissionVO> getSubmissionByVersion(
            @Parameter(description = "申报ID") @PathVariable @NotNull Long applicationId,
            @Parameter(description = "提交物类型") @PathVariable @NotNull String submissionType,
            @Parameter(description = "提交阶段") @PathVariable @NotNull String submissionStage,
            @Parameter(description = "提交轮次") @PathVariable @NotNull Integer submissionRound,
            @Parameter(description = "版本号") @PathVariable @NotNull Integer submissionVersion) {
        
        log.info("接收到查询指定版本提交物请求: applicationId={}, type={}, stage={}, round={}, version={}", 
                applicationId, submissionType, submissionStage, submissionRound, submissionVersion);
        
        // 验证参数
        validationService.validateQueryParams(applicationId, submissionStage, submissionType);
        
        try {
            // 查询指定版本提交物
            ProcessSubmissionVO submission = processSystemService
                    .getSubmissionByVersion(applicationId, submissionType, submissionStage, submissionRound, submissionVersion);
            
            if (submission == null) {
                log.warn("指定版本提交物不存在: applicationId={}, type={}, stage={}, round={}, version={}", 
                        applicationId, submissionType, submissionStage, submissionRound, submissionVersion);
                return Result.error(404, "指定版本的提交物不存在");
            }
            
            log.info("查询指定版本提交物成功: applicationId={}, type={}, stage={}, round={}, version={}", 
                    applicationId, submissionType, submissionStage, submissionRound, submissionVersion);
            
            return Result.success(submission);
            
        } catch (Exception e) {
            log.error("查询指定版本提交物失败: applicationId={}, type={}, stage={}, round={}, version={}, error={}", 
                    applicationId, submissionType, submissionStage, submissionRound, submissionVersion, e.getMessage(), e);
            return Result.error("查询指定版本提交物失败: " + e.getMessage());
        }
    }

    /**
     * 获取提交物完整历史记录
     * 
     * @param applicationId 申报ID
     * @param submissionType 提交物类型
     * @param submissionStage 提交阶段
     * @return 完整历史记录列表
     */
    @GetMapping("/applications/{applicationId}/submissions/{submissionType}/{submissionStage}/history")
    @Operation(summary = "获取提交物完整历史记录", description = "获取指定申报、类型和阶段的所有历史版本")
    public Result<List<ProcessSubmissionVO>> getSubmissionFullHistory(
            @Parameter(description = "申报ID") @PathVariable @NotNull Long applicationId,
            @Parameter(description = "提交物类型") @PathVariable @NotNull String submissionType,
            @Parameter(description = "提交阶段") @PathVariable @NotNull String submissionStage) {
        
        log.info("接收到查询完整历史请求: applicationId={}, type={}, stage={}", 
                applicationId, submissionType, submissionStage);
        
        // 验证参数
        validationService.validateQueryParams(applicationId, submissionStage, submissionType);
        
        try {
            // 查询完整历史
            List<ProcessSubmissionVO> submissions = processSystemService
                    .getSubmissionFullHistory(applicationId, submissionType, submissionStage);
            
            log.info("查询完整历史成功: applicationId={}, type={}, stage={}, count={}", 
                    applicationId, submissionType, submissionStage, submissions.size());
            
            return Result.success(submissions);
            
        } catch (Exception e) {
            log.error("查询完整历史失败: applicationId={}, type={}, stage={}, error={}", 
                    applicationId, submissionType, submissionStage, e.getMessage(), e);
            return Result.error("查询完整历史失败: " + e.getMessage());
        }
    }

    // ==================== 文件信息查询接口 ====================

    /**
     * 获取文件列表
     * 
     * @param submissionId 提交物ID（可选）
     * @param fileCategory 文件分类（可选）
     * @param fileName 文件名称（可选，支持模糊查询）
     * @param fileType 文件类型（可选）
     * @return 文件列表
     */
    @GetMapping("/files")
    @Operation(summary = "获取文件列表", description = "根据查询条件获取文件清单")
    public Result<Map<String, Object>> getFiles(
            @Parameter(description = "提交物ID") @RequestParam(required = false) Long submissionId,
            @Parameter(description = "文件分类 (proposal/attachment)") @RequestParam(required = false) String fileCategory,
            @Parameter(description = "文件名称（支持模糊查询）") @RequestParam(required = false) String fileName,
            @Parameter(description = "文件类型") @RequestParam(required = false) String fileType) {
        
        log.info("接收到查询文件列表请求: submissionId={}, fileCategory={}, fileName={}, fileType={}", 
                submissionId, fileCategory, fileName, fileType);
        
        try {
            // 查询文件列表
            List<ProcessSubmissionFile> files = processSystemService.getFiles(submissionId, fileCategory, fileName, fileType);
            
            // 统计总数
            Long total = processSystemService.countFiles(submissionId, fileCategory, fileName, fileType);
            
            // 构建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("total", total);
            responseData.put("files", files);
            
            log.info("查询文件列表成功: total={}", total);
            
            return Result.success(responseData);
            
        } catch (Exception e) {
            log.error("查询文件列表失败: submissionId={}, error={}", submissionId, e.getMessage(), e);
            return Result.error("查询文件列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件元数据
     * 
     * @param fileId 文件ID
     * @return 文件元数据信息
     */
    @GetMapping("/files/{fileId}")
    @Operation(summary = "获取文件元数据", description = "根据文件ID获取文件的详细元数据信息")
    public Result<ProcessSubmissionFile> getFileMetadata(
            @Parameter(description = "文件ID") @PathVariable @NotNull String fileId) {
        
        log.info("接收到获取文件元数据请求: fileId={}", fileId);
        
        // 验证文件ID
        if (fileId == null || fileId.trim().isEmpty()) {
            return Result.error(10001, "文件ID不能为空");
        }
        
        try {
            // 获取文件元数据
            ProcessSubmissionFile file = processSystemService.getFileMetadata(fileId);
            
            if (file == null) {
                log.warn("文件不存在: fileId={}", fileId);
                return Result.error(404, "文件不存在或无权限访问");
            }
            
            log.info("获取文件元数据成功: fileId={}, fileName={}", fileId, file.getFileName());
            
            return Result.success(file);
            
        } catch (Exception e) {
            log.error("获取文件元数据失败: fileId={}, error={}", fileId, e.getMessage(), e);
            return Result.error("获取文件元数据失败: " + e.getMessage());
        }
    }

    /**
     * 根据提交物ID获取文件列表
     * 
     * @param submissionId 提交物ID
     * @param fileCategory 文件分类（可选）
     * @return 文件列表
     */
    @GetMapping("/submissions/{submissionId}/files")
    @Operation(summary = "根据提交物ID获取文件列表", description = "获取指定提交物的所有文件")
    public Result<List<ProcessSubmissionFile>> getFilesBySubmissionId(
            @Parameter(description = "提交物ID") @PathVariable @NotNull Long submissionId,
            @Parameter(description = "文件分类 (proposal/attachment)") @RequestParam(required = false) String fileCategory) {
        
        log.info("接收到根据提交物ID查询文件请求: submissionId={}, fileCategory={}", submissionId, fileCategory);
        
        // 验证提交物ID
        validationService.validateSubmissionId(submissionId);
        
        try {
            // 查询文件列表
            List<ProcessSubmissionFile> files = processSystemService.getFilesBySubmissionId(submissionId, fileCategory);
            
            log.info("根据提交物ID查询文件成功: submissionId={}, fileCategory={}, count={}", 
                    submissionId, fileCategory, files.size());
            
            return Result.success(files);
            
        } catch (Exception e) {
            log.error("根据提交物ID查询文件失败: submissionId={}, fileCategory={}, error={}", 
                    submissionId, fileCategory, e.getMessage(), e);
            return Result.error("查询文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件下载链接
     * 
     * @param fileId 文件ID
     * @return 文件下载链接
     */
    @GetMapping("/files/{fileId}/download-url")
    @Operation(summary = "获取文件下载链接", description = "获取文件的临时下载链接")
    public Result<Map<String, Object>> getFileDownloadUrl(
            @Parameter(description = "文件ID") @PathVariable @NotNull String fileId) {
        
        log.info("接收到获取文件下载链接请求: fileId={}", fileId);
        
        // 验证文件ID
        if (fileId == null || fileId.trim().isEmpty()) {
            return Result.error(10001, "文件ID不能为空");
        }
        
        try {
            // 检查文件是否存在
            ProcessSubmissionFile file = processSystemService.getFileMetadata(fileId);
            if (file == null) {
                log.warn("文件不存在: fileId={}", fileId);
                return Result.error(404, "文件不存在或无权限访问");
            }
            
            // 获取下载链接
            String downloadUrl = processSystemService.getFileDownloadUrl(fileId);
            
            // 构建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("file_id", fileId);
            responseData.put("file_name", file.getFileName());
            responseData.put("download_url", downloadUrl);
            responseData.put("expires_in", 3600); // 1小时过期
            
            log.info("获取文件下载链接成功: fileId={}, fileName={}", fileId, file.getFileName());
            
            return Result.success(responseData);
            
        } catch (Exception e) {
            log.error("获取文件下载链接失败: fileId={}, error={}", fileId, e.getMessage(), e);
            return Result.error("获取文件下载链接失败: " + e.getMessage());
        }
    }

    /**
     * 批量获取文件下载链接
     * 
     * @param fileIds 文件ID列表
     * @return 文件下载链接列表
     */
    @PostMapping("/files/batch-download-urls")
    @Operation(summary = "批量获取文件下载链接", description = "批量获取多个文件的下载链接")
    public Result<Map<String, Object>> getBatchFileDownloadUrls(
            @Parameter(description = "文件ID列表") @RequestBody @Valid List<String> fileIds) {
        
        log.info("接收到批量获取文件下载链接请求: fileIds={}", fileIds);
        
        // 验证文件ID列表
        if (fileIds == null || fileIds.isEmpty()) {
            return Result.error(10001, "文件ID列表不能为空");
        }
        
        if (fileIds.size() > 100) {
            return Result.error(10001, "单次批量下载文件数量不能超过100个");
        }
        
        try {
            // 批量获取下载链接
            Map<String, String> downloadUrls = processSystemService.getBatchFileDownloadUrls(fileIds);
            
            // 构建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("total", downloadUrls.size());
            responseData.put("download_urls", downloadUrls);
            responseData.put("expires_in", 3600); // 1小时过期
            
            log.info("批量获取文件下载链接成功: requestCount={}, successCount={}", 
                    fileIds.size(), downloadUrls.size());
            
            return Result.success(responseData);
            
        } catch (Exception e) {
            log.error("批量获取文件下载链接失败: fileIds={}, error={}", fileIds, e.getMessage(), e);
            return Result.error("批量获取文件下载链接失败: " + e.getMessage());
        }
    }
}