package com.achievement.controller;

import com.achievement.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    private final JdbcTemplate jdbcTemplate;

    /**
     * 获取专家用户列表（用于审核人选择）
     * 临时使用admin_users表，后续需要创建business_users表
     */
    @Operation(description = "获取专家用户列表")
    @GetMapping("/experts")
    public Result<List<Map<String, Object>>> getExpertUsers() {
        String sql = """
            SELECT id, username, 
                   CONCAT(COALESCE(firstname, ''), ' ', COALESCE(lastname, '')) as name,
                   email, 'admin' as department
            FROM admin_users 
            WHERE is_active = 1 
            ORDER BY firstname, lastname
            """;
        
        List<Map<String, Object>> experts = jdbcTemplate.queryForList(sql);
        return Result.success(experts);
    }

    /**
     * 获取所有业务用户列表
     */
    @Operation(description = "获取所有业务用户列表")
    @GetMapping
    public Result<List<Map<String, Object>>> getAllUsers(@RequestParam(required = false) String role) {
        String sql = """
            SELECT id, username, 
                   CONCAT(COALESCE(firstname, ''), ' ', COALESCE(lastname, '')) as name,
                   email, 'admin' as role, 'admin' as department
            FROM admin_users 
            WHERE is_active = 1 
            ORDER BY firstname, lastname
            """;
        
        List<Map<String, Object>> users = jdbcTemplate.queryForList(sql);
        return Result.success(users);
    }
}
