package com.achievement.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface IAchievementAdminService {

    JsonNode createAchievement(Map<String, Object> req);

    JsonNode updateAchievement(String achievementDocId, Map<String, Object> req);

    /**
     * 一次请求：上传文件到 Strapi 并创建成果物。
     * <p>
     * 前端使用 multipart/form-data：
     * - data: application/json（原 createAchievement 的请求体）
     * - files: 文件数组（可选）
     * <p>
     * 后端会先调用 Strapi /api/upload 拿到文件 id，自动写入 data.attachments[].data.file。
     */
    JsonNode createAchievementWithFiles(Map<String, Object> req, MultipartFile[] files);

    /**
     * 一次请求：上传文件到 Strapi 并更新成果物。
     * <p>
     * 前端使用 multipart/form-data：
     * - data: application/json（原 updateAchievement 的请求体）
     * - files: 文件数组（可选）
     * <p>
     * 后端会先调用 Strapi /api/upload 拿到文件 id，追加写入 data.attachments[].data.file 后再执行 update。
     */
    JsonNode updateAchievementWithFiles(String achievementDocId, Map<String, Object> req, MultipartFile[] files);

    /**
     * 仅更新成果物可见范围（visibility_range），不触发 achievement_status 的强制状态变更。
     * <p>
     * 期望请求体：
     * {"documentId":"xxx","data":{"visibility_range":...}}
     */
    JsonNode updateVisibilityRange(Map<String, Object> req);

    /**
     * 软删除成果物（achievement_mains.is_delete=1）。
     */
    JsonNode deleteAchievement(String achievementDocId);
}
