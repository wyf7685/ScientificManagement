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

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author author
 * @since 2025-12-11
 */
// 12.11 目前开发成果物有关的查询功能
@Slf4j
@RestController
@RequestMapping("/admin/achievement")
@RequiredArgsConstructor
@Tag(name = "成果物管理相关接口")
public class AchievementManageController {
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
     * 管理员分页查询所有成果物列表
     */
    @Operation(description = "分页查询成果物列表接口")
    @PostMapping("/pageList")
    public Result<Page<AchListVO>> pageList(@RequestBody AchListDTO achListDTO, @CurrentUser KeycloakUser currentUser) {
        if (!currentUser.hasRole("admin")) {
            return Result.error("无权限：仅管理员可访问");
        }
        return Result.success(achievementMainsService.pageList(achListDTO));
    }

    /*
     * TODO管理员查询成果物详情
     */
    @Operation(description = "查询成果物详情接口")
    @GetMapping("/detail")
    public Result<AchDetailVO> detail(@RequestParam String achDocId) {
        log.info("查询成果物详情，achDocId={}", achDocId);
        return Result.success(achievementMainsService.selectDetail(achDocId));
    }

    /*
     * TODO管理员新增成果物
     */
    @Operation(description = "管理员新增成果物（主信息+多个字段值）")
    @PostMapping("/create")
    public Result<JsonNode> create(@RequestBody Map<String, Object> req, @CurrentUser KeycloakUser currentUser) {
        log.info("新增成果物");
        return Result.success(achievementAdminService.createAchievement(req, currentUser.getId()));
    }

    @Operation(description = "管理员一次请求上传文件并创建成果物（multipart：data+files）")
    @PostMapping(value = "/createWithFiles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<JsonNode> createWithFiles(
            @RequestPart("data") String dataJson,
            @RequestPart(value = "files", required = false) MultipartFile[] files,
            @CurrentUser KeycloakUser currentUser) {
        Map<String, Object> req = readJsonMap(dataJson);
        return Result.success(achievementAdminService.createAchievementWithFiles(req, files, currentUser.getId()));
    }

    /*
     * TODO管理员修改成果物 包括 公共属性 和 自定义属性
     */
    @Operation(description = "管理员修改成果物（主信息+多个字段值，仅处理前端传回的改动项）")
    @PutMapping("/update/{achievementDocId}")
    public Result<JsonNode> update(@PathVariable String achievementDocId, @RequestBody Map<String, Object> req) {
        return Result.success(achievementAdminService.updateAchievement(achievementDocId, req));
    }

    @Operation(description = "管理员一次请求上传文件并更新成果物（multipart：data+files）")
    @PutMapping(value = "/updateWithFiles/{achievementDocId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<JsonNode> updateWithFiles(
            @PathVariable String achievementDocId,
            @RequestPart("data") String dataJson,
            @RequestPart(value = "files", required = false) MultipartFile[] files) {
        Map<String, Object> req = readJsonMap(dataJson);
        return Result.success(achievementAdminService.updateAchievementWithFiles(achievementDocId, req, files));
    }

    /*
     * 管理员单独修改成果物可见范围（不触发 achievement_status 强制改为 PENDING）
     * 前端传参示例：
     * {
     *   "documentId": "xxx",
     *   "data": { "visibility_range": "..." }
     * }
     */
    @Operation(description = "管理员修改成果物可见范围（仅更新 visibility_range，不触发状态变更）")
    @PutMapping("/updateVisibilityRange")
    public Result<JsonNode> updateVisibilityRange(@RequestBody Map<String, Object> req) {
        return Result.success(achievementAdminService.updateVisibilityRange(req));
    }

    /*
     * TODO 管理员审核成果物，即修改成果物状态
     *  TODO 目前缺少用户ID
     *
     * */
    /*
     * TODO 管理员分配审核专家
     *
     * */

    @Operation(description = "Admin soft delete achievement (is_delete=1)")
    @PutMapping("/delete/{achievementDocId}")
    public Result<JsonNode> delete(@PathVariable String achievementDocId) {
        return Result.success(achievementAdminService.deleteAchievement(achievementDocId));
    }

    @Operation(description = "管理员分页查询所有可见成果物列表接口")
    @PostMapping("/pageListAllVisible")
    public Result<Page<AchListVO>> pageListAllVisible(@RequestBody AchListDTO2 achListDTO) {
        return Result.success(achievementMainsService.pageList4Admin(achListDTO));
    }
}
