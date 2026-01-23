package com.achievement.controller;

import com.achievement.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目管理控制器 (临时补全接口，用于连通性测试)
 * <p>
 * 注意：当前 Strapi 中缺失 Project 模型，此处暂时使用硬编码数据。
 * 后续需要在 Strapi 中创建 'project' Content Type 并在此对接 Service 层。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/projects")
@Tag(name = "项目管理相关接口")
public class ProjectController {

    // 模拟项目数据结构 (对应前端 API)
    @Data
    @Builder
    static class ProjectVO {
        private String id;
        private String name;
        private String code;
        private String description;
        private String type;
        private String field;
    }

    /**
     * 获取项目列表
     * 对应前端: getProjects -> GET /api/projects
     */
    @Operation(description = "获取项目列表")
    @GetMapping
    public Result<Map<String, Object>> getProjects(@RequestParam(required = false) String keyword) {
        log.info("接收到获取项目列表请求，关键词: {}", keyword);
        
        List<ProjectVO> list = new ArrayList<>();
        
        // 模拟返回两条数据，证明后端已连通
        list.add(ProjectVO.builder()
                .id("p-backend-001")
                .name("后端直连测试项目-A (来自Java)")
                .code("TEST-2024-001")
                .type("基础研究")
                .description("这是从 Java 后端返回的真实数据")
                .build());
                
        list.add(ProjectVO.builder()
                .id("p-backend-002")
                .name("后端直连测试项目-B (来自Java)")
                .code("TEST-2024-002")
                .type("应用研究")
                .description("前端与后端数据链路已打通")
                .build());

        // 构造 Strapi 风格的分页响应结构，适配前端 normalizeStrapiCollection
        // 前端 result.ts 中的 normalizeStrapiCollection 期望 { data: [...], meta: { pagination: ... } }
        // 但注意：您现有的 Result 包装类可能是 { code: 200, data: T, message: ... }
        // 前端 request.ts 会剥离一层 Result，所以这里返回 Map 作为 Result 的 data
        
        Map<String, Object> response = new HashMap<>(); // 对应 Strapi 的顶层
        response.put("data", list);  // Strapi 数据列表
        
        Map<String, Object> meta = new HashMap<>();
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", 1);
        pagination.put("pageSize", 20);
        pagination.put("total", 2);
        pagination.put("pageCount", 1);
        meta.put("pagination", pagination);
        
        response.put("meta", meta);

        return Result.success(response);
    }
}
