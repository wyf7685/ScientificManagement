package com.achievement.controller;

import com.achievement.annotation.CurrentUser;
import com.achievement.domain.dto.AccessRequestQueryDTO;
import com.achievement.domain.dto.AccessRequestReviewDTO;
import com.achievement.domain.dto.KeycloakUser;
import com.achievement.domain.vo.AccessRequestVO;
import com.achievement.result.Result;
import com.achievement.service.IAccessRequestService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 访问权限申请控制器
 */
@Slf4j
@RestController
@RequestMapping("/results/access-requests")
@RequiredArgsConstructor
@Tag(name = "访问权限申请相关接口")
public class AccessRequestController {
    
    private final IAccessRequestService accessRequestService;
    
    /**
     * 获取访问申请列表
     */
    @Operation(description = "获取访问申请列表")
    @GetMapping
    public Result<Page<AccessRequestVO>> getAccessRequests(
            AccessRequestQueryDTO queryDTO,
            @CurrentUser KeycloakUser currentUser) {
        if (!currentUser.hasAnyRole("admin", "manager")) {
            throw new RuntimeException("无权限查看访问申请");
        }
        Page<AccessRequestVO> result = accessRequestService.pageAccessRequests(queryDTO);
        return Result.success(result);
    }
    
    /**
     * 审核访问申请
     */
    @Operation(description = "审核访问申请")
    @PostMapping("/{id}/review")
    public Result<AccessRequestVO> reviewAccessRequest(
            @PathVariable("id") String requestId,
            @Validated @RequestBody AccessRequestReviewDTO reviewDTO,
            @CurrentUser KeycloakUser currentUser) {

        if (!currentUser.hasAnyRole("admin", "manager")) {
            throw new RuntimeException("无权限审核访问申请");
        }

        AccessRequestVO result = accessRequestService.reviewAccessRequest(
                requestId, reviewDTO, currentUser);
        return Result.success(result);
    }
}
