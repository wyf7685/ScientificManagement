package com.achievement.controller;

import com.achievement.service.IAchievementFieldValuesService;
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
@RequestMapping("/achievementFieldValue")
@RequiredArgsConstructor
@Tag(name = "成果物字段值相关接口")
public class AchievementFieldValueController {

    private final IAchievementFieldValuesService fieldValuesService;

    @PostMapping("/create")
    @Operation(description = "新增字段值")
    // req 支持：单个 JSON 对象 或 JSON 数组（批量新增）
    public JsonNode createFieldValue(@RequestBody JsonNode req) {
        log.info("新增字段值");
        return fieldValuesService.createFieldValue(req);
    }

    @PutMapping("/values/{fieldValueDocId}")
    @Operation(description = "更新字段值")
    public JsonNode updateFieldValue(@PathVariable String fieldValueDocId, @RequestBody JsonNode req) {
        log.info("更新字段值 fieldValueDocId={}", fieldValueDocId);
        return fieldValuesService.updateFieldValue(fieldValueDocId, req);
    }

    @PutMapping("/update")
    @Operation(description = "批量更新字段值")
    // req 支持：单个 JSON 对象 或 JSON 数组；每个元素需包含 id/docId/fieldValueDocId 之一
    public JsonNode updateFieldValues(@RequestBody JsonNode req) {
        log.info("批量更新字段值");
        return fieldValuesService.updateFieldValues(req);
    }
    @PutMapping("/delete/{fieldValueDocId}")
    @Operation(description = "Soft delete field value (is_delete=1)")
    public JsonNode deleteFieldValue(@PathVariable String fieldValueDocId) {
        log.info("Soft delete field value fieldValueDocId={}", fieldValueDocId);
        return fieldValuesService.deleteFieldValue(fieldValueDocId);
    }
}
