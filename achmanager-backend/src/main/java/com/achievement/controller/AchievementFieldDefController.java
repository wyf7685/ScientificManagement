package com.achievement.controller;

import com.achievement.service.IAchievementFieldDefsService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/achievementFieldDef")
@RequiredArgsConstructor
@Tag(name = "成果物字段定义相关接口")
public class AchievementFieldDefController {

    private final IAchievementFieldDefsService fieldDefsService;

    @PostMapping("/create")
    @Operation(description = "新增字段定义")
    // req 支持：单个 JSON 对象 或 JSON 数组（批量新增）
    public JsonNode createFieldDef(@RequestBody JsonNode req) {
        log.info("新增字段定义");
        return fieldDefsService.createFieldDef(req);
    }

    @PutMapping("/defs/{fieldDefDocId}")
    @Operation(description = "更新字段定义")
    public JsonNode updateFieldDef(@PathVariable String fieldDefDocId, @RequestBody JsonNode req) {
        log.info("更新字段定义 fieldDefDocId={}", fieldDefDocId);
        return fieldDefsService.updateFieldDef(fieldDefDocId, req);
    }

    @PutMapping("/update")
    @Operation(description = "批量更新字段定义")
    // req 支持：单个 JSON 对象 或 JSON 数组；每个元素需包含 id/docId/fieldDefDocId 之一
    public JsonNode updateFieldDefs(@RequestBody JsonNode req) {
        log.info("批量更新字段定义");
        return fieldDefsService.updateFieldDefs(req);
    }
    @PutMapping("/delete/{fieldDefDocId}")
    @Operation(description = "Soft delete field definition (is_delete=1)")
    public JsonNode deleteFieldDef(@PathVariable String fieldDefDocId) {
        log.info("Soft delete field definition fieldDefDocId={}", fieldDefDocId);
        return fieldDefsService.deleteFieldDef(fieldDefDocId);
    }
}
