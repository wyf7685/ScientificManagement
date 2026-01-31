package com.achievement.controller;

import com.achievement.constant.RoleConstants;
import com.achievement.domain.dto.KeycloakUser;
import com.achievement.result.Result;
import com.achievement.service.IKeycloakUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理控制器
 *
 * @author system
 * @since 2026-01-21
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "用户管理相关接口")
public class UserController {

    private final IKeycloakUserService keycloakUserService;

    /**
     * 获取专家用户列表（用于审核人选择）
     */
    @Operation(description = "获取专家用户列表")
    @GetMapping("/experts")
    public Result<List<KeycloakUser>> getExpertUsers() {
        List<KeycloakUser> experts = keycloakUserService.getUsersByRole(RoleConstants.RESEARCH_EXPERT);
        return Result.success(experts.stream().filter(u -> u.isEnabled()).collect(Collectors.toList()));
    }

    /**
     * 获取所有业务用户列表
     */
    @Operation(description = "获取所有业务用户列表")
    @GetMapping
    public Result<List<KeycloakUser>> getAllUsers(@RequestParam(required = false) String role) {
        List<KeycloakUser> users = StringUtils.hasText(role)
                ? keycloakUserService.getUsersByRole(role)
                : keycloakUserService.getAllUsers();
        users.sort((u1, u2) -> {
            int roleCompare = u1.getRoles().toString().compareTo(u2.getRoles().toString());
            if (roleCompare != 0) {
                return roleCompare;
            }
            return u1.getName().compareTo(u2.getName());
        });
        return Result.success(users);
    }
}
