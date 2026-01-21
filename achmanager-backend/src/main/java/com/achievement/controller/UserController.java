package com.achievement.controller;

import com.achievement.domain.po.BusinessUser;
import com.achievement.mapper.BusinessUserMapper;
import com.achievement.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    private final BusinessUserMapper businessUserMapper;

    /**
     * 获取专家用户列表（用于审核人选择）
     */
    @Operation(description = "获取专家用户列表")
    @GetMapping("/experts")
    public Result<List<BusinessUser>> getExpertUsers() {
        LambdaQueryWrapper<BusinessUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BusinessUser::getRole, "expert")
                .eq(BusinessUser::getIsActive, true)
                .eq(BusinessUser::getIsDelete, 0)
                .select(BusinessUser::getId, BusinessUser::getUsername, 
                        BusinessUser::getName, BusinessUser::getDepartment, 
                        BusinessUser::getEmail, BusinessUser::getPhone)
                .orderByAsc(BusinessUser::getName);
        
        List<BusinessUser> experts = businessUserMapper.selectList(queryWrapper);
        return Result.success(experts);
    }

    /**
     * 获取所有业务用户列表
     */
    @Operation(description = "获取所有业务用户列表")
    @GetMapping
    public Result<List<BusinessUser>> getAllUsers(@RequestParam(required = false) String role) {
        LambdaQueryWrapper<BusinessUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BusinessUser::getIsActive, true)
                .eq(BusinessUser::getIsDelete, 0)
                .select(BusinessUser::getId, BusinessUser::getUsername, 
                        BusinessUser::getName, BusinessUser::getDepartment, 
                        BusinessUser::getEmail, BusinessUser::getPhone, BusinessUser::getRole);
        
        if (role != null && !role.isEmpty()) {
            queryWrapper.eq(BusinessUser::getRole, role);
        }
        
        queryWrapper.orderByAsc(BusinessUser::getRole, BusinessUser::getName);
        
        List<BusinessUser> users = businessUserMapper.selectList(queryWrapper);
        return Result.success(users);
    }
}
