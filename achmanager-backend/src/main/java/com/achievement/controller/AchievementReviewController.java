package com.achievement.controller;

import com.achievement.annotation.CurrentUser;
import com.achievement.domain.dto.AssignReviewerDTO;
import com.achievement.domain.dto.ReviewRequestDTO;
import com.achievement.domain.po.BusinessUser;
import com.achievement.domain.vo.ReviewBacklogVO;
import com.achievement.domain.vo.ReviewHistoryVO;
import com.achievement.domain.vo.ReviewResultVO;
import com.achievement.result.Result;
import com.achievement.service.IAchievementReviewService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 成果审核控制器
 */
@Slf4j
@RestController
@RequestMapping("/results")
@RequiredArgsConstructor
@Tag(name = "成果审核相关接口")
public class AchievementReviewController {
    
    private final IAchievementReviewService achievementReviewService;
    
    /**
     * 提交成果进入审核流程
     */
    @Operation(description = "提交成果进入审核流程")
    @PostMapping("/{id}/submit")
    public Result<ReviewResultVO> submitForReview(
            @PathVariable("id") String achievementDocId,
            @CurrentUser BusinessUser currentUser) {
        
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        ReviewResultVO result = achievementReviewService.submitForReview(achievementDocId, currentUser.getId());
        return Result.success(result);
    }
    
    /**
     * 审核成果
     */
    @Operation(description = "审核成果")
    @PostMapping("/{id}/review")
    public Result<ReviewResultVO> reviewAchievement(
            @PathVariable("id") String achievementDocId,
            @Validated @RequestBody ReviewRequestDTO reviewRequest,
            @CurrentUser BusinessUser currentUser) {
        
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        // 验证用户是否有审核权限
        if (!"admin".equals(currentUser.getRole()) && !"expert".equals(currentUser.getRole()) && !"manager".equals(currentUser.getRole())) {
            return Result.error(403, "无权限：只有管理员、专家或审核管理员可以审核成果");
        }
        
        ReviewResultVO result = achievementReviewService.reviewAchievement(
                achievementDocId, reviewRequest, currentUser.getId());
        return Result.success(result);
    }
    
    /**
     * 分配审核人
     */
    @Operation(description = "分配审核人")
    @PostMapping("/{id}/assign-reviewers")
    public Result<ReviewResultVO> assignReviewers(
            @PathVariable("id") String achievementDocId,
            @Validated @RequestBody AssignReviewerDTO assignReviewerDTO,
            @CurrentUser BusinessUser currentUser) {
        
        // 验证用户权限
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        // 只有管理员和审核管理员可以分配审核人
        if (!"admin".equals(currentUser.getRole()) && !"manager".equals(currentUser.getRole())) {
            return Result.error(403, "无权限：只有管理员可以分配审核人");
        }
        
        ReviewResultVO result = achievementReviewService.assignReviewers(
                achievementDocId, assignReviewerDTO, currentUser.getId());
        return Result.success(result);
    }
    
    /**
     * 获取审核待办列表
     */
    @Operation(description = "获取审核待办列表")
    @GetMapping("/review-backlog")
    public Result<Page<ReviewBacklogVO>> getReviewBacklog(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @CurrentUser BusinessUser currentUser) {
        
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        // 验证用户是否有审核权限
        if (!"admin".equals(currentUser.getRole()) && !"expert".equals(currentUser.getRole()) && !"manager".equals(currentUser.getRole())) {
            return Result.error(403, "无权限：只有管理员、专家或审核管理员可以查看审核待办");
        }
        
        Page<ReviewBacklogVO> result = achievementReviewService.pageReviewBacklog(
                currentUser.getId(), page, pageSize);
        return Result.success(result);
    }
    
    /**
     * 获取审核历史列表
     */
    @Operation(description = "获取审核历史列表")
    @GetMapping("/review-history")
    public Result<Page<ReviewHistoryVO>> getReviewHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @CurrentUser BusinessUser currentUser) {
        
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        // 验证用户是否有审核权限
        if (!"admin".equals(currentUser.getRole()) && !"expert".equals(currentUser.getRole()) && !"manager".equals(currentUser.getRole())) {
            return Result.error(403, "无权限：只有管理员、专家或审核管理员可以查看审核历史");
        }
        
        Page<ReviewHistoryVO> result = achievementReviewService.pageReviewHistory(
                currentUser.getId(), page, pageSize);
        return Result.success(result);
    }
}
