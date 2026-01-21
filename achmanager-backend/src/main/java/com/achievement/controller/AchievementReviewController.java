package com.achievement.controller;

import com.achievement.domain.dto.AssignReviewerDTO;
import com.achievement.domain.dto.ReviewRequestDTO;
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
        
        // 验证用户角色（仅专家和管理员可审核）
        String role = currentUser.getRole();
        if (!"expert".equals(role) && !"admin".equals(role)) {
            throw new RuntimeException("无权限审核成果");
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

        // 验证用户角色（仅管理员/科研管理可分配审核人）
        if (!"admin".equals(currentUser.getRole()) && !"manager".equals(currentUser.getRole())) {
            throw new RuntimeException("无权限分配审核人");
        }

        ReviewResultVO result = achievementReviewService.assignReviewers(
                achievementDocId, assignReviewerDTO, currentUser.getId());
        return Result.success(result);
    }

    /**
     * 审核待办列表
     */
    @Operation(description = "获取审核待办列表")
    @GetMapping("/review-backlog")
    public Result<Page<ReviewBacklogVO>> reviewBacklog(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @CurrentUser BusinessUser currentUser) {
        Page<ReviewBacklogVO> result = achievementReviewService.pageReviewBacklog(
                currentUser.getId(), page, pageSize);
        return Result.success(result);
    }

    /**
     * 审核历史列表
     */
    @Operation(description = "获取审核历史列表")
    @GetMapping("/review-history")
    public Result<Page<ReviewHistoryVO>> reviewHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @CurrentUser BusinessUser currentUser) {
        Page<ReviewHistoryVO> result = achievementReviewService.pageReviewHistory(
                currentUser.getId(), page, pageSize);
        return Result.success(result);
    }
}

    /**
     * 获取审核待办列表
     */
    @Operation(description = "获取审核待办列表")
    @GetMapping("/review-backlog")
    public Result<Page<ReviewBacklogVO>> getReviewBacklog(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        // TODO: 从当前登录用户获取reviewerId
        Integer reviewerId = 1; // 临时硬编码
        
        Page<ReviewBacklogVO> result = achievementReviewService.pageReviewBacklog(
                reviewerId, page, pageSize);
        return Result.success(result);
    }
    
    /**
     * 获取审核历史列表
     */
    @Operation(description = "获取审核历史列表")
    @GetMapping("/review-history")
    public Result<Page<ReviewHistoryVO>> getReviewHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        // TODO: 从当前登录用户获取reviewerId
        Integer reviewerId = 1; // 临时硬编码
        
        Page<ReviewHistoryVO> result = achievementReviewService.pageReviewHistory(
                reviewerId, page, pageSize);
        return Result.success(result);
    }
