package com.achievement.controller;

import com.achievement.domain.dto.LoginDTO;
import com.achievement.domain.vo.LoginVO;
import com.achievement.domain.vo.UserProfileVO;
import com.achievement.result.Result;
import com.achievement.service.IAuthService;
import com.achievement.annotation.CurrentUser;
import com.achievement.domain.po.BusinessUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * @author system
 * @since 2026-01-21
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证相关接口")
public class AuthController {

    private final IAuthService authService;

    /**
     * 用户登录
     */
    @Operation(description = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Validated @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = authService.login(loginDTO);
        return Result.success(loginVO);
    }

    /**
     * 获取当前登录用户信息
     */
    @Operation(description = "获取当前登录用户信息")
    @GetMapping("/current")
    public Result<UserProfileVO> current(@CurrentUser BusinessUser currentUser) {
        UserProfileVO profile = UserProfileVO.builder()
                .id(currentUser.getId())
                .username(currentUser.getUsername())
                .name(currentUser.getName())
                .email(currentUser.getEmail())
                .role(currentUser.getRole())
                .department(currentUser.getDepartment())
                .phone(currentUser.getPhone())
                .build();
        return Result.success(profile);
    }

    /**
     * 退出登录（前端清除token即可）
     */
    @Operation(description = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        // 前端清除token即可，后端无需处理
        // 如果需要黑名单机制，可以将token加入Redis黑名单
        return Result.success();
    }
}
