package com.achievement.service;

import com.achievement.domain.po.AchievementFieldDefs;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author author
 * @since 2025-12-11
 */
public interface IAchievementFieldDefsService extends IService<AchievementFieldDefs> {

    JsonNode createFieldDef(JsonNode req);

    JsonNode updateFieldDef(String fieldDefDocId, JsonNode req);

    JsonNode updateFieldDefs(JsonNode req);

    /**
     * 软删除字段定义（is_delete=1）。
     */
    JsonNode deleteFieldDef(String fieldDefDocId);
}
