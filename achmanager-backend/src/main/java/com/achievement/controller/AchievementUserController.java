package com.achievement.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.achievement.annotation.CurrentUser;
import com.achievement.domain.dto.AchListDTO;
import com.achievement.domain.dto.AchListDTO2;
import com.achievement.domain.dto.KeycloakUser;
import com.achievement.domain.vo.AchDetailVO;
import com.achievement.domain.vo.AchListVO;
import com.achievement.result.Result;
import com.achievement.service.IAchievementAdminService;
import com.achievement.service.IAchievementMainsService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//12.17 目前开发成果物有关的查询功能
@Slf4j
@RestController
@RequestMapping("/user/achievement")
@RequiredArgsConstructor
@Tag(name = "用户成果物管理相关接口")
public class AchievementUserController {
    private final IAchievementMainsService achievementMainsService;
    private final IAchievementAdminService achievementAdminService;
    private final ObjectMapper objectMapper;

    private Map<String, Object> readJsonMap(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("Invalid JSON in multipart part 'data'", e);
        }
    }

    /*
     * 用户分页查询个人成果物列表
     *
     */
    @Operation(description = "用户分页查询成果物列表接口")
    @PostMapping("/pageList")
    public Result<Page<AchListVO>> pageList(@RequestBody AchListDTO achListDTO, @CurrentUser KeycloakUser currentUser) {
        if (currentUser == null) {
            return Result.error("未登录");
        }
        return Result.success(achievementMainsService.pageList4User(achListDTO, currentUser.getId()));
    }

    /*
     * TODO 用户分页查询所有 可见范围内 所有成果物
     */
    @Operation(description = "用户分页查询所有可见成果物列表接口")
    @PostMapping("/pageListAllVisible")
    public Result<Page<AchListVO>> pageListAllVisible(@RequestBody AchListDTO2 achListDTO) {
        return Result.success(achievementMainsService.pageList4Visibility(achListDTO));
    }

    @Operation(description = "用户新增成果物（主信息+多个字段值）")
    @PostMapping("/create")
    public Result<JsonNode> create(@RequestBody Map<String, Object> req,
            @CurrentUser KeycloakUser currentUser) {
        try {
            return Result.success(achievementAdminService.createAchievement(req, currentUser.getId()));
        } catch (Exception e) {
            log.error("创建成果失败: reqKeys={}", req == null ? null : req.keySet(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @Operation(description = "用户一次请求上传文件并创建成果物（multipart：data+files）")
    @PostMapping(value = "/createWithFiles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<JsonNode> createWithFiles(@RequestPart("data") String dataJson,
            @RequestPart(value = "files", required = false) MultipartFile[] files,
            @CurrentUser KeycloakUser currentUser) {
        Map<String, Object> req = readJsonMap(dataJson);
        try {
            return Result
                    .success(achievementAdminService.createAchievementWithFiles(req, files, currentUser.getId()));
        } catch (Exception e) {
            log.error("创建成果(含附件)失败: reqKeys={}, filesCount={}",
                    req == null ? null : req.keySet(),
                    files == null ? 0 : files.length,
                    e);
            return Result.error(500, e.getMessage());
        }
    }

    @Operation(description = "用户修改成果物（主信息+多个字段值，仅处理前端传回的改动项）")
    @PutMapping("/update/{achievementDocId}")
    public Result<JsonNode> update(@PathVariable String achievementDocId, @RequestBody Map<String, Object> req) {
        return Result.success(achievementAdminService.updateAchievement(achievementDocId, req));
    }

    @Operation(description = "用户一次请求上传文件并更新成果物（multipart：data+files）")
    @PutMapping(value = "/updateWithFiles/{achievementDocId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<JsonNode> updateWithFiles(@PathVariable String achievementDocId,
            @RequestPart("data") String dataJson,
            @RequestPart(value = "files", required = false) MultipartFile[] files) {
        Map<String, Object> req = readJsonMap(dataJson);
        return Result.success(achievementAdminService.updateAchievementWithFiles(achievementDocId, req, files));
    }

    @Operation(description = "用户修改成果物可见范围（仅更新 visibility_range，不触发状态强制变更）")
    @PutMapping("/updateVisibilityRange")
    public Result<JsonNode> updateVisibilityRange(@RequestBody Map<String, Object> req) {
        return Result.success(achievementAdminService.updateVisibilityRange(req));
    }

    // TODO 用户查询成果物详情 ，与管理员查询成果物详情类似
    @Operation(description = "用户成果物详情接口")
    @GetMapping("/detail")
    public Result<AchDetailVO> detail(@RequestParam String achDocId) {
        log.info("用户查询成果物详情，achDocId={}", achDocId);
        return Result.success(achievementMainsService.selectDetail(achDocId));
    }
}
