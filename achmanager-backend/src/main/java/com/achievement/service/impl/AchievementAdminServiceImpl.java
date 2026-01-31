package com.achievement.service.impl;

import com.achievement.client.StrapiClient;
import com.achievement.service.IAchievementAdminService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementAdminServiceImpl implements IAchievementAdminService {

    private static final String ACHIEVEMENT_COLLECTION = "achievement-mains";
    private static final String FIELD_VALUE_COLLECTION = "achievement-field-values";
    // Strapi content-type: achievement_files (API ID), adjust if your Strapi API ID differs
    private static final String ACHIEVEMENT_FILE_COLLECTION = "achievement-files";

    private final StrapiClient strapiClient;
    private final ObjectMapper objectMapper;

    @Override
    public JsonNode createAchievement(Map<String, Object> req, Integer userId) {
        MainAndFields mainAndFields = parseMainAndFields(req);
        Map<String, Object> mainReq = mainAndFields.mainReq;
        List<Map<String, Object>> fields = mainAndFields.fields;
        List<Map<String, Object>> attachments = mainAndFields.attachments;
        // 新增：写入创建人
        Object dataObj = mainReq.get("data");
        if (!(dataObj instanceof Map<?, ?> dataAny)) {
            throw new RuntimeException("mainReq 必须包含 data 对象");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) dataAny;

        // 字段在 Strapi 里是 Text，传字符串更稳
        data.put("created_by_user_id", String.valueOf(userId));

        log.info("创建成果物，创建人 userId={}", userId);
        String mainRaw = strapiClient.create(ACHIEVEMENT_COLLECTION, mainReq);
        JsonNode mainJson = readJson(mainRaw);
        String achievementDocId = extractDocumentId(mainJson);
        if (achievementDocId == null || achievementDocId.isBlank()) {
            throw new RuntimeException("Strapi create achievement_mains 返回未包含 documentId");
        }

        ArrayNode fieldResults = objectMapper.createArrayNode();
        for (Map<String, Object> fieldItem : fields) {
            FieldValueRequest fieldReq = normalizeFieldValueRequest(fieldItem, achievementDocId, false);
            if (fieldReq.isAllValueEmpty()) {
                continue;
            }
            String raw = strapiClient.create(FIELD_VALUE_COLLECTION, fieldReq.body);
            fieldResults.add(readJson(raw));
        }

        ArrayNode attachmentResults = objectMapper.createArrayNode();
        for (Map<String, Object> attachmentItem : attachments) {
            AchievementFileRequest fileReq = normalizeAchievementFileRequest(attachmentItem, achievementDocId);
            if (fileReq.isAllEmpty()) {
                continue;
            }
            String raw = (fileReq.documentId == null || fileReq.documentId.isBlank())
                    ? strapiClient.create(ACHIEVEMENT_FILE_COLLECTION, fileReq.body)
                    : strapiClient.update(ACHIEVEMENT_FILE_COLLECTION, fileReq.documentId, fileReq.body);
            attachmentResults.add(readJson(raw));
        }

        ObjectNode out = objectMapper.createObjectNode();
        out.set("achievement", mainJson);
        out.set("fields", fieldResults);
        out.set("attachments", attachmentResults);
        return out;
    }

    @Override
    @SuppressWarnings("unchecked")
    public JsonNode createAchievementWithFiles(Map<String, Object> req, MultipartFile[] files, Integer userId) {
        if (files == null || files.length == 0) {
            return createAchievement(req,userId);
        }

        String uploadRaw = strapiClient.upload(files);
        JsonNode uploadJson = readJson(uploadRaw);
        if (uploadJson == null || !uploadJson.isArray() || uploadJson.isEmpty()) {
            return createAchievement(req,userId);
        }

        List<Integer> fileIds = new ArrayList<>();
        for (JsonNode item : uploadJson) {
            JsonNode idNode = item.get("id");
            if (idNode == null || !idNode.isNumber()) {
                continue;
            }
            fileIds.add(idNode.intValue());
        }
        if (fileIds.isEmpty()) {
            return createAchievement(req,userId);
        }

        Object dataObj = req.get("data");
        if (!(dataObj instanceof Map<?, ?> dataAny)) {
            throw new RuntimeException("请求体必须包含 data 对象");
        }
        Map<String, Object> data = (Map<String, Object>) dataAny;

        Object attachmentsObj = data.get("attachments");
        List<Map<String, Object>> attachments;
        if (attachmentsObj instanceof List<?> list) {
            attachments = (List<Map<String, Object>>) list;
        } else {
            attachments = new ArrayList<>();
            data.put("attachments", attachments);
        }

        Map<String, Object> attachmentData = new HashMap<>();
        attachmentData.put("files", fileIds);
        attachmentData.put("is_delete", 0);

        Map<String, Object> attachmentItem = new HashMap<>();
        attachmentItem.put("data", attachmentData);
        attachments.add(attachmentItem);

        return createAchievement(req,userId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public JsonNode updateAchievementWithFiles(String achievementDocId, Map<String, Object> req, MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return updateAchievement(achievementDocId, req);
        }

        String uploadRaw = strapiClient.upload(files);
        JsonNode uploadJson = readJson(uploadRaw);
        if (uploadJson == null || !uploadJson.isArray() || uploadJson.isEmpty()) {
            return updateAchievement(achievementDocId, req);
        }

        List<Integer> fileIds = new ArrayList<>();
        for (JsonNode item : uploadJson) {
            JsonNode idNode = item.get("id");
            if (idNode == null || !idNode.isNumber()) {
                continue;
            }
            fileIds.add(idNode.intValue());
        }
        if (fileIds.isEmpty()) {
            return updateAchievement(achievementDocId, req);
        }

        Object dataObj = req.get("data");
        if (!(dataObj instanceof Map<?, ?> dataAny)) {
            throw new RuntimeException("请求体必须包含 data 对象");
        }
        Map<String, Object> data = (Map<String, Object>) dataAny;

        Object attachmentsObj = data.get("attachments");
        List<Map<String, Object>> attachments;
        if (attachmentsObj instanceof List<?> list) {
            attachments = (List<Map<String, Object>>) list;
        } else {
            attachments = new ArrayList<>();
            data.put("attachments", attachments);
        }

        Map<String, Object> attachmentData = new HashMap<>();
        attachmentData.put("files", fileIds);
        attachmentData.put("is_delete", 0);

        Map<String, Object> attachmentItem = new HashMap<>();
        attachmentItem.put("data", attachmentData);
        attachments.add(attachmentItem);

        return updateAchievement(achievementDocId, req);
    }

    @Override
    public JsonNode updateAchievement(String achievementDocId, Map<String, Object> req) {
        MainAndFields mainAndFields = parseMainAndFields(req);
        Map<String, Object> mainReq = mainAndFields.mainReq;
        List<Map<String, Object>> fields = mainAndFields.fields;
        List<Map<String, Object>> attachments = mainAndFields.attachments;

        // 管理员修改成果物：后端强制将审核状态置为 PENDING，避免前端遗漏或越权传值
        Object mainDataObj = mainReq.get("data");
        if (mainDataObj instanceof Map<?, ?> data) {
            @SuppressWarnings("unchecked")
            Map<String, Object> mainData = (Map<String, Object>) data;
            mainData.put("achievement_status", "PENDING");
        }

        String mainRaw = strapiClient.update(ACHIEVEMENT_COLLECTION, achievementDocId, mainReq);
        JsonNode mainJson = readJson(mainRaw);

        ArrayNode fieldResults = objectMapper.createArrayNode();
        for (Map<String, Object> fieldItem : fields) {
            FieldValueRequest fieldReq = normalizeFieldValueRequest(fieldItem, achievementDocId, true);
            if (fieldReq.isAllValueEmpty()) {
                continue;
            }

            String raw = (fieldReq.documentId == null || fieldReq.documentId.isBlank())
                    ? strapiClient.create(FIELD_VALUE_COLLECTION, fieldReq.body)
                    : strapiClient.update(FIELD_VALUE_COLLECTION, fieldReq.documentId, fieldReq.body);
            fieldResults.add(readJson(raw));
        }

        ArrayNode attachmentResults = objectMapper.createArrayNode();
        for (Map<String, Object> attachmentItem : attachments) {
            AchievementFileRequest fileReq = normalizeAchievementFileRequest(attachmentItem, achievementDocId);
            if (fileReq.isAllEmpty()) {
                continue;
            }
            String raw = (fileReq.documentId == null || fileReq.documentId.isBlank())
                    ? strapiClient.create(ACHIEVEMENT_FILE_COLLECTION, fileReq.body)
                    : strapiClient.update(ACHIEVEMENT_FILE_COLLECTION, fileReq.documentId, fileReq.body);
            attachmentResults.add(readJson(raw));
        }

        ObjectNode out = objectMapper.createObjectNode();
        out.set("achievement", mainJson);
        out.set("fields", fieldResults);
        out.set("attachments", attachmentResults);
        return out;
    }

    /**
     * 管理员单独修改成果物可见范围：只更新 visibility_range，不触发 achievement_status 强制改为 PENDING。
     * <p>
     * 期望请求体：
     * {"documentId":"xxx","data":{"visibility_range":...}}
     */
    @Override
    @SuppressWarnings("unchecked")
    public JsonNode updateVisibilityRange(Map<String, Object> req) {
        Object documentIdObj = req.get("documentId");
        String documentId = documentIdObj == null ? null : String.valueOf(documentIdObj).trim();
        if (documentId == null || documentId.isBlank()) {
            throw new RuntimeException("请求体必须包含 documentId");
        }

        Object dataObj = req.get("data");
        if (!(dataObj instanceof Map<?, ?> data)) {
            throw new RuntimeException("请求体必须包含 data 对象");
        }
        Object visibilityRange = ((Map<String, Object>) data).get("visibility_range");
        if (visibilityRange == null || String.valueOf(visibilityRange).trim().isEmpty()) {
            throw new RuntimeException("data.visibility_range 不能为空");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("data", Map.of("visibility_range", visibilityRange));

        String raw = strapiClient.update(ACHIEVEMENT_COLLECTION, documentId, body);
        return readJson(raw);
    }

    @SuppressWarnings("unchecked")
    private MainAndFields parseMainAndFields(Map<String, Object> req) {
        Object dataObj = req.get("data");
        if (!(dataObj instanceof Map<?, ?> data)) {
            throw new RuntimeException("请求体必须包含 data 对象");
        }

        Object fieldsObj = ((Map<?, ?>) data).get("fields");
        List<Map<String, Object>> fields = Collections.emptyList();
        if (fieldsObj instanceof List<?> list) {
            fields = (List<Map<String, Object>>) list;
        }

        Object attachmentsObj = ((Map<?, ?>) data).get("attachments");
        List<Map<String, Object>> attachments = Collections.emptyList();
        if (attachmentsObj instanceof List<?> list) {
            attachments = (List<Map<String, Object>>) list;
        }

        Map<String, Object> mainData = new HashMap<>((Map<String, Object>) data);
        mainData.remove("fields");
        mainData.remove("attachments");
        // Strapi 不允许在 body.data 写 documentId（只能在响应中出现），这里统一剔除，避免前端误传导致 400
        mainData.remove("documentId");
        // 兼容前端驼峰命名 -> Strapi/DB 下划线字段
        normalizeMainKeys(mainData);

        Map<String, Object> mainReq = new HashMap<>();
        mainReq.put("data", mainData);
        return new MainAndFields(mainReq, fields, attachments);
    }

    private void normalizeMainKeys(Map<String, Object> mainData) {
        // 审核状态：achievementStatus -> achievement_status（前端驼峰）
        moveKey(mainData, "achievementStatus", "achievement_status");
        // 成果物类型外键：typeDocId -> achievement_type_id
        moveKey(mainData, "typeDocId", "achievement_type_id");
        
        // 新增基础字段映射（前端驼峰 -> Strapi 下划线）
        moveKey(mainData, "projectCode", "project_code");
        moveKey(mainData, "projectName", "project_name");
        moveKey(mainData, "visibilityRange", "visibility_range");
        
        // 前端展示字段不写入 Strapi
        mainData.remove("typeName");
        mainData.remove("typeCode");
    }

    /**
     * Normalize one achievement_file (attachment record) item to a Strapi request body.
     * <p>
     * Input element supports either:
     * - {"data":{...}} (Strapi style)
     * - {...} (flat style)
     * <p>
     * Rules:
     * - documentId is used only for update URL, removed from body.data
     * - Always bind achievement_main_id to the achievement documentId
     * - Add is_delete (default 0) and accept isDelete alias
     */
    @SuppressWarnings("unchecked")
    private AchievementFileRequest normalizeAchievementFileRequest(Map<String, Object> attachmentItem, String achievementDocId) {
        Map<String, Object> data;
        Object dataObj = attachmentItem.get("data");
        if (dataObj instanceof Map<?, ?> m) {
            data = new HashMap<>((Map<String, Object>) m);
        } else {
            data = new HashMap<>(attachmentItem);
            data.remove("data");
        }

        String documentId = null;
        Object docIdObj = attachmentItem.get("documentId");
        if (docIdObj == null) {
            docIdObj = data.get("documentId");
        }
        if (docIdObj != null) {
            documentId = String.valueOf(docIdObj).trim();
        }
        data.remove("documentId");

        data.putIfAbsent("achievement_main_id", achievementDocId);

        // Strapi media field key: files (tolerate legacy key file)
        if (!data.containsKey("files") && data.containsKey("file")) {
            data.put("files", data.get("file"));
        }
        data.remove("file");

        // Tolerate common front-end typos/aliases
        if (!data.containsKey("is_delete") && data.containsKey("is_elete")) {
            data.put("is_delete", data.get("is_elete"));
        }
        if (!data.containsKey("is_delete") && data.containsKey("isDelete")) {
            data.put("is_delete", data.get("isDelete"));
        }
        data.remove("is_elete");
        data.remove("isDelete");
        data.putIfAbsent("is_delete", 0);

        Map<String, Object> body = new HashMap<>();
        body.put("data", data);
        return new AchievementFileRequest(documentId, body);
    }

    @SuppressWarnings("unchecked")
    private FieldValueRequest normalizeFieldValueRequest(Map<String, Object> fieldItem, String achievementDocId, boolean isUpdate) {
        Map<String, Object> data;
        Object dataObj = fieldItem.get("data");
        if (dataObj instanceof Map<?, ?> m) {
            data = new HashMap<>((Map<String, Object>) m);
        } else {
            data = new HashMap<>(fieldItem);
            data.remove("data");
        }

        String fieldValueDocId = null;
        Object docIdObj = fieldItem.get("documentId");
        if (docIdObj == null) {
            docIdObj = data.get("documentId");
        }
        if (docIdObj != null) {
            fieldValueDocId = String.valueOf(docIdObj);
        }
        // documentId 只用于拼接更新路由，不允许作为 Strapi 可写字段出现在 body.data 中
        data.remove("documentId");

        data.putIfAbsent("achievement_id", achievementDocId);


        // 容错：is_elete -> is_delete
        if (!data.containsKey("is_delete") && data.containsKey("is_elete")) {
            data.put("is_delete", data.get("is_elete"));
        }
        // 容错：驼峰 isDelete -> is_delete
        if (!data.containsKey("is_delete") && data.containsKey("isDelete")) {
            data.put("is_delete", data.get("isDelete"));
        }
        data.remove("is_elete");

        // 如果是更新，并且前端已给 fieldValue 的 documentId，则无需再次传 fieldDef（不改关联）
        String fieldDefDocId = pickFirstNonBlank(
                data.get("achievement_field_def_id"),
                data.get("achievementFieldDefId"),
                data.get("fieldDefDocId")
        );
        if ((fieldValueDocId == null || fieldValueDocId.isBlank()) && fieldDefDocId == null) {
            throw new RuntimeException("字段值新增时必须包含 achievement_field_def_id（字段定义 documentId）");
        }
        if (fieldDefDocId != null) {
            data.put("achievement_field_def_id", fieldDefDocId);
        }
        data.remove("achievementFieldDefId");
        data.remove("fieldDefDocId");

        // create/update 都不要向 Strapi body 传 documentId

        // 约束：fields 里只会有一种 value（text/boolean/number/date/email）
        enforceSingleValue(data);
        // 前端 fields 里 value 用驼峰，转成 Strapi 需要的下划线字段
        normalizeValueKeys(data);

        Map<String, Object> body = new HashMap<>();
        body.put("data", data);
        return new FieldValueRequest(fieldValueDocId, body);
    }

    private void normalizeValueKeys(Map<String, Object> data) {
        moveKey(data, "textValue", "text_value");
        moveKey(data, "booleanValue", "boolean_value");
        moveKey(data, "numberValue", "number_value");
        moveKey(data, "dateValue", "date_value");
        moveKey(data, "emailValue", "email_value");
        moveKey(data, "isDelete", "is_delete");
    }

    private void moveKey(Map<String, Object> data, String from, String to) {
        if (!data.containsKey(to) && data.containsKey(from)) {
            data.put(to, data.get(from));
        }
        data.remove(from);
    }

    private void enforceSingleValue(Map<String, Object> data) {
        int present = 0;
        present += hasValue(data, "text_value", "textValue") ? 1 : 0;
        present += hasValue(data, "boolean_value", "booleanValue") ? 1 : 0;
        present += hasValue(data, "number_value", "numberValue") ? 1 : 0;
        present += hasValue(data, "date_value", "dateValue") ? 1 : 0;
        present += hasValue(data, "email_value", "emailValue") ? 1 : 0;
        if (present == 0) {
            throw new RuntimeException("字段值必须至少包含一种 value（text/boolean/number/date/email）");
        }
        if (present > 1) {
            throw new RuntimeException("字段值只能包含一种 value（text/boolean/number/date/email），请不要同时传多个");
        }
    }

    private boolean hasValue(Map<String, Object> data, String snakeKey, String camelKey) {
        if (data.containsKey(snakeKey)) {
            Object v = data.get(snakeKey);
            return !isBlank(v);
        }
        if (data.containsKey(camelKey)) {
            Object v = data.get(camelKey);
            return !isBlank(v);
        }
        return false;
    }

    private String pickFirstNonBlank(Object... values) {
        for (Object v : values) {
            if (v == null) {
                continue;
            }
            String s = String.valueOf(v).trim();
            if (!s.isEmpty()) {
                return s;
            }
        }
        return null;
    }

    private boolean isBlank(Object val) {
        return val == null || String.valueOf(val).trim().isEmpty();
    }

    private static final class FieldValueRequest {
        private final String documentId;
        private final Map<String, Object> body;

        private FieldValueRequest(String documentId, Map<String, Object> body) {
            this.documentId = documentId;
            this.body = body;
        }

        private boolean isAllValueEmpty() {
            Object dataObj = body.get("data");
            if (!(dataObj instanceof Map<?, ?> data)) {
                return true;
            }
            return isBlank(data.get("text_value"))
                    && isBlank(data.get("textValue"))
                    && isBlank(data.get("email_value"))
                    && isBlank(data.get("emailValue"))
                    && isBlank(data.get("date_value"))
                    && isBlank(data.get("dateValue"))
                    && data.get("boolean_value") == null
                    && data.get("booleanValue") == null
                    && data.get("number_value") == null
                    && data.get("numberValue") == null;
        }

        private boolean isBlank(Object val) {
            return val == null || String.valueOf(val).trim().isEmpty();
        }
    }

    private static final class MainAndFields {
        private final Map<String, Object> mainReq;
        private final List<Map<String, Object>> fields;
        private final List<Map<String, Object>> attachments;

        private MainAndFields(Map<String, Object> mainReq, List<Map<String, Object>> fields, List<Map<String, Object>> attachments) {
            this.mainReq = mainReq;
            this.fields = fields;
            this.attachments = attachments;
        }
    }

    private static final class AchievementFileRequest {
        private final String documentId;
        private final Map<String, Object> body;

        private AchievementFileRequest(String documentId, Map<String, Object> body) {
            this.documentId = documentId;
            this.body = body;
        }

        @SuppressWarnings("unchecked")
        private boolean isAllEmpty() {
            Object dataObj = body.get("data");
            if (!(dataObj instanceof Map<?, ?> data)) {
                return true;
            }
            Object files = ((Map<String, Object>) data).get("files");
            if (files instanceof List<?> list) {
                return list.isEmpty();
            }
            return files == null;
        }
    }

    private JsonNode readJson(String raw) {
        try {
            return objectMapper.readTree(raw);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String extractDocumentId(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.hasNonNull("documentId")) {
            return node.get("documentId").asText();
        }
        JsonNode data = node.get("data");
        if (data != null && data.isObject() && data.hasNonNull("documentId")) {
            return data.get("documentId").asText();
        }
        if (data != null && data.isObject()) {
            JsonNode attrs = data.get("attributes");
            if (attrs != null && attrs.hasNonNull("documentId")) {
                return attrs.get("documentId").asText();
            }
        }
        JsonNode attrs = node.get("attributes");
        if (attrs != null && attrs.hasNonNull("documentId")) {
            return attrs.get("documentId").asText();
        }
        return null;
    }

    /**
     * 软删除成果物：仅把 achievement_mains.is_delete 更新为 1。
     */
    @Override
    public JsonNode deleteAchievement(String achievementDocId) {
        Map<String, Object> body = new HashMap<>();
        body.put("data", Map.of("is_delete", 1));
        String raw = strapiClient.update(ACHIEVEMENT_COLLECTION, achievementDocId, body);
        return readJson(raw);
    }
}
