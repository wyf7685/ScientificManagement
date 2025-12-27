package com.achievement.service;

import com.achievement.domain.po.AchievementFieldValues;
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
public interface IAchievementFieldValuesService extends IService<AchievementFieldValues> {

    JsonNode createFieldValue(JsonNode req);

    JsonNode updateFieldValue(String fieldValueDocId, JsonNode req);

    JsonNode updateFieldValues(JsonNode req);

    /**
     * 软删除字段值（is_delete=1）。
     */
    JsonNode deleteFieldValue(String fieldValueDocId);
}
