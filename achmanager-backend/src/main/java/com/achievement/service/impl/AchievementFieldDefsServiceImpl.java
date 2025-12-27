package com.achievement.service.impl;

import com.achievement.client.StrapiClient;
import com.achievement.domain.po.AchievementFieldDefs;
import com.achievement.mapper.AchievementFieldDefsMapper;
import com.achievement.service.IAchievementFieldDefsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author author
 * @since 2025-12-11
 */
@Service
@RequiredArgsConstructor
public class AchievementFieldDefsServiceImpl extends ServiceImpl<AchievementFieldDefsMapper, AchievementFieldDefs> implements IAchievementFieldDefsService {

    private final StrapiClient strapiClient;
    private final ObjectMapper objectMapper;

    /**
     * 新增字段定义：兼容前端传单个 JSON 对象或 JSON 数组。
     * <p>
     * - 如果请求体是单个对象：返回单个 Strapi 响应对象
     * - 如果请求体是数组：逐条调用 Strapi 新增并返回数组结果
     * <p>
     * 同时兼容两种入参结构：
     * - Strapi 风格：{"data":{...}}
     * - 平铺风格：{...}（后端会自动包装成 {"data":{...}}）
     */
    @Override
    public JsonNode createFieldDef(JsonNode req) {
        List<JsonNode> items = normalizeToItems(req);
        if (!req.isArray() && items.size() == 1) {
            return createOne(items.get(0));
        }

        ArrayNode results = objectMapper.createArrayNode();
        for (JsonNode item : items) {
            results.add(createOne(item));
        }
        return results;
    }

    /**
     * 单条更新字段定义：更新目标 id 通过 URL path 传入，更新内容通过 body.data 传给 Strapi。
     */
    @Override
    public JsonNode updateFieldDef(String fieldDefDocId, JsonNode req) {
        String raw = strapiClient.update("achievement-field-defs", fieldDefDocId, toStrapiBody(req, idFieldNamesForFieldDef()));
        return readTree(raw);
    }

    /**
     * 批量更新字段定义：兼容前端传单个 JSON 对象或 JSON 数组。
     * <p>
     * 每个元素必须携带可识别的 id 字段之一（见 {@link #idFieldNamesForFieldDef()}），用于拼接 Strapi 的更新 URL。
     * 更新内容会放入请求体的 data 中。
     */
    @Override
    public JsonNode updateFieldDefs(JsonNode req) {
        List<JsonNode> items = normalizeToItems(req);
        if (!req.isArray() && items.size() == 1) {
            JsonNode item = items.get(0);
            String id = extractId(item, idFieldNamesForFieldDef());
            String raw = strapiClient.update("achievement-field-defs", id, toStrapiBody(item, idFieldNamesForFieldDef()));
            return readTree(raw);
        }

        ArrayNode results = objectMapper.createArrayNode();
        for (JsonNode item : items) {
            String id = extractId(item, idFieldNamesForFieldDef());
            String raw = strapiClient.update("achievement-field-defs", id, toStrapiBody(item, idFieldNamesForFieldDef()));
            results.add(readTree(raw));
        }
        return results;
    }

    /**
     * 软删除字段定义：仅把 is_delete 更新为 1。
     */
    @Override
    public JsonNode deleteFieldDef(String fieldDefDocId) {
        Map<String, Object> body = new HashMap<>();
        body.put("data", Map.of("is_delete", 1));
        String raw = strapiClient.update("achievement-field-defs", fieldDefDocId, body);
        return readTree(raw);
    }

    private JsonNode createOne(JsonNode item) {
        String raw = strapiClient.create("achievement-field-defs", toStrapiBody(item, idFieldNamesForFieldDef()));
        return readTree(raw);
    }

    /**
     * 将请求体归一化为条目列表：
     * - req 为数组 => 返回数组元素列表
     * - req 为对象 => 返回单元素列表
     */
    private List<JsonNode> normalizeToItems(JsonNode req) {
        if (req == null || req.isNull()) {
            return List.of();
        }
        if (req.isArray()) {
            List<JsonNode> items = new ArrayList<>();
            req.forEach(items::add);
            return items;
        }
        return List.of(req);
    }

    private List<String> idFieldNamesForFieldDef() {
        // 说明：
        // - 这些字段用于“批量更新”时从每个元素里提取更新目标 id，然后拼接到 Strapi URL：PUT /api/{collection}/{id}
        // - 同时这些字段会在平铺入参模式下从 data 中移除，避免把 id 当作普通字段更新到 Strapi
        // - documentId 是为了兼容部分前端/Strapi 返回字段命名（有的项目用 documentId 表示文档 ID）
        return List.of("id", "docId", "documentId", "fieldDefDocId");
    }

    /**
     * 从单条请求元素中提取更新所需的 id（用于 URL 拼接）。
     */
    private String extractId(JsonNode item, List<String> idFieldNames) {
        for (String key : idFieldNames) {
            JsonNode node = item.get(key);
            if (node != null && !node.isNull() && node.isValueNode()) {
                String id = node.asText();
                if (!id.isBlank()) {
                    return id;
                }
            }
        }
        throw new IllegalArgumentException("Missing id field in request item (accepted: " + idFieldNames + ")");
    }

    /**
     * 组装 Strapi v4 的请求体：{"data":{...}}。
     * <p>
     * - 若前端已传 {"data":{...}}，直接使用 data 部分
     * - 若前端传平铺对象 {...}，则自动包装成 {"data":{...}}；同时会把 id/docId 等标识字段从 data 中移除，
     *   避免把它们当作普通字段更新到 Strapi。
     */
    private Map<String, Object> toStrapiBody(JsonNode item, List<String> idFieldNames) {
        JsonNode dataNode;
        if (item != null && item.hasNonNull("data")) {
            dataNode = item.get("data");
        } else {
            ObjectNode obj = objectMapper.convertValue(item, ObjectNode.class);
            idFieldNames.forEach(obj::remove);
            dataNode = obj;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("data", objectMapper.convertValue(dataNode, new TypeReference<Map<String, Object>>() {
        }));
        return body;
    }

    private JsonNode readTree(String raw) {
        try {
            return objectMapper.readTree(raw);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
