# AchManager Backend API

后端 HTTP 接口文档（以 `src/main/java/com/achievement/controller` 为准）。

## 通用约定

- Base URL：`http://localhost:8080`
- Strapi 写入风格：多数写接口使用 Strapi v4 的 `{"data":{...}}` 结构；部分接口也兼容平铺结构 `{...}`（后端会自动包一层 `data`）。
- `documentId`：这里的 `documentId` 指 Strapi 返回的文档 ID（字符串），不是 MySQL 的自增 `id`。
- 软删除：多数表使用 `is_delete`（或兼容 `isDelete`）表示软删除，`0=正常`、`1=删除`。
- 统一返回：大部分接口返回 `Result<T>`，结构如下（`code=0` 表示成功）：
```json
{ "code": 0, "msg": "string", "data": {} }
```

## 1) 成果物类型（AchievementTypeController）

Base Path：`/achievementType`

### 1.1 查询类型列表

- Method：`POST`
- Path：`/achievementType/list`
- Content-Type：`application/json`
- Body：无
- Response：`Result<List<AchTypeListVO>>`

### 1.2 查询类型详情

- Method：`GET`
- Path：`/achievementType/detail`
- Query：
  - `typeDocId`：类型 `documentId`
- 示例：`GET /achievementType/detail?typeDocId=u80hojhjvjojx39frhn7gbq7`
- Response：`Result<AchTypeDetailVO>`

### 1.3 新增类型

- Method：`POST`
- Path：`/achievementType/types`
- Content-Type：`application/json`
- Response：`JsonNode`（Strapi 原样返回）
- Body（示例）：
```json
{
  "data": {
    "type_name": "论文",
    "type_code": "PAPER",
    "is_delete": 0
  }
}
```

### 1.4 更新类型

- Method：`PUT`
- Path：`/achievementType/types/{typeDocId}`
- Content-Type：`application/json`
- Response：`JsonNode`
- Body（示例）：
```json
{
  "data": {
    "type_name": "论文（更新）"
  }
}
```

### 1.5 删除类型（软删除）

- Method：`PUT`
- Path：`/achievementType/types/{typeDocId}/delete`
- Content-Type：`application/json`
- Body：无
- Response：`JsonNode`

## 2) 成果物字段定义（AchievementFieldDefController）

Base Path：`/achievementFieldDef`

> 这些接口入参是 `JsonNode`，支持：
> - 单个 JSON 对象
> - JSON 数组（批量）
> 同时支持两种结构：
> - `{"data":{...}}`
> - 平铺 `{...}`（后端会自动包一层 `data`）

### 2.1 新增字段定义（单条/批量）

- Method：`POST`
- Path：`/achievementFieldDef/create`
- Content-Type：`application/json`
- Response：`JsonNode`（Strapi 原样返回；单条返回对象、批量返回数组）
- Body（批量示例）：
```json
[
  {
    "data": {
      "field_code": "paperTitle",
      "field_name": "论文题目",
      "field_type": "TEXT",
      "is_required": 0,
      "description": "....",
      "is_delete": 0
    }
  },
  {
    "data": {
      "field_code": "paperNum",
      "field_name": "论文编号",
      "field_type": "TEXT",
      "is_required": 0,
      "description": "....",
      "is_delete": 0
    }
  }
]
```

### 2.2 更新字段定义（单条，id 在 URL）

- Method：`PUT`
- Path：`/achievementFieldDef/defs/{fieldDefDocId}`
- Content-Type：`application/json`
- Response：`JsonNode`
- Body（示例）：
```json
{
  "data": {
    "field_name": "论文题目1"
  }
}
```

### 2.3 批量更新字段定义（id 在 body）

- Method：`PUT`
- Path：`/achievementFieldDef/update`
- Content-Type：`application/json`
- Response：`JsonNode`（单条返回对象、批量返回数组）
- Body：支持单对象或数组；每个元素必须携带 `id/docId/documentId/fieldDefDocId` 之一
- Body（示例，按你当前调用方式）：
```json
[
  { "documentId": "qzw4sh5nluywadt2ewff6m83", "data": { "field_name": "论文题目1" } },
  { "documentId": "meybvqnrrko4vn7v7iwr6zq6", "data": { "field_name": "论文编号1" } }
]
```

### 2.4 删除字段定义（软删除）

- Method：`PUT`
- Path：`/achievementFieldDef/delete/{fieldDefDocId}`
- Content-Type：`application/json`
- Body：无
- Response：`JsonNode`

## 3) 成果物字段值（AchievementFieldValueController）

Base Path：`/achievementFieldValue`

> 同字段定义：支持单条/批量，支持 `{"data":{...}}` 或平铺 `{...}`。

### 3.1 新增字段值（单条/批量）

- Method：`POST`
- Path：`/achievementFieldValue/create`
- Content-Type：`application/json`
- Response：`JsonNode`（Strapi 原样返回；单条返回对象、批量返回数组）
- Body（批量示例，注意外键 `achievement_field_def_id` 必须是字段定义的 `documentId`）：
```json
[
  {
    "data": {
      "text_value": "此处是论文标题",
      "is_delete": 0,
      "achievement_field_def_id": "qzw4sh5nluywadt2ewff6m83"
    }
  },
  {
    "data": {
      "text_value": "此处是论文编号",
      "is_delete": 0,
      "achievement_field_def_id": "meybvqnrrko4vn7v7iwr6zq6"
    }
  }
]
```

### 3.2 更新字段值（单条，id 在 URL）

- Method：`PUT`
- Path：`/achievementFieldValue/values/{fieldValueDocId}`
- Content-Type：`application/json`
- Response：`JsonNode`
- Body（示例）：
```json
{
  "data": {
    "text_value": "此处是论文标题1"
  }
}
```

### 3.3 批量更新字段值（id 在 body）

- Method：`PUT`
- Path：`/achievementFieldValue/update`
- Content-Type：`application/json`
- Response：`JsonNode`（单条返回对象、批量返回数组）
- Body：支持单对象或数组；每个元素必须携带 `id/docId/documentId/fieldValueDocId` 之一
- Body（示例，按你当前调用方式）：
```json
[
  { "documentId": "lelkwetd5cptyhzzebd1eslq", "data": { "text_value": "此处是论文标题1" } },
  { "documentId": "q529osbzs4k9pzhj0zlldgay", "data": { "text_value": "此处是论文编号1" } }
]
```

### 3.4 删除字段值（软删除）

- Method：`PUT`
- Path：`/achievementFieldValue/delete/{fieldValueDocId}`
- Content-Type：`application/json`
- Body：无
- Response：`JsonNode`

## 4) 管理员成果物管理（AchievementManageController）

Base Path：`/admin/achievement`

### 4.1 分页列表

- Method：`POST`
- Path：`/admin/achievement/pageList`
- Content-Type：`application/json`
- Body（`AchListDTO` 示例）：
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "mainTitle": "论文",
  "typeId": 1,
  "status": "PENDING",
  "projectId": 1,
  "creatorId": 1,
  "visibilityRange": "PERSONAL"
}
```
- Response：`Result<Page<AchListVO>>`

### 4.2 详情（含附件）

- Method：`GET`
- Path：`/admin/achievement/detail`
- Query：
  - `achDocId`：成果物 `documentId`
- 示例：`GET /admin/achievement/detail?achDocId=kashrvfxl0ohti6xt3cp96ug`
- 说明：返回 `AchDetailVO`，并且会额外通过 Strapi 查询 `achievement-files`（`populate=files`）将附件信息填充到 `attachments` 字段。
- Response：`Result<AchDetailVO>`

### 4.3 创建成果物（JSON）

- Method：`POST`
- Path：`/admin/achievement/create`
- Content-Type：`application/json`
- Response：`Result<JsonNode>`（返回中包含 `achievement` / `fields` / `attachments`）
- Body（示例，按你当前格式）：
```json
{
  "data": {
    "title": "机器学习研究论文",
    "summary": "......................",
    "achievementStatus": "PENDING",
    "typeDocId": "u80hojhjvjojx39frhn7gbq7",
    "fields": [
      { "achievement_field_def_id": "qzw4sh5nluywadt2ewff6m83", "textValue": "B001", "isDelete": 0 },
      { "achievement_field_def_id": "meybvqnrrko4vn7v7iwr6zq6", "textValue": "100", "isDelete": 0 }
    ]
  }
}
```

### 4.4 一次请求“上传文件 + 创建成果物”（multipart/form-data）

- Method：`POST`
- Path：`/admin/achievement/createWithFiles`
- Content-Type：`multipart/form-data`
- Response：`Result<JsonNode>`
- Form Data：
  - `data`：Text（JSON 字符串，内容就是 4.3 的 JSON）
  - `files`：File（可多文件，可不传）
- Postman 填写要点：
  - `data` 必须是 Text，不要选 File；否则会把该 part 当成 `application/octet-stream`，导致 415
- `data`（示例，原样粘贴为文本）：
```json
{
  "data": {
    "title": "机器学习研究论文",
    "summary": "......................",
    "achievementStatus": "PENDING",
    "typeDocId": "u80hojhjvjojx39frhn7gbq7",
    "fields": [
      { "achievement_field_def_id": "qzw4sh5nluywadt2ewff6m83", "textValue": "B001", "isDelete": 0 },
      { "achievement_field_def_id": "meybvqnrrko4vn7v7iwr6zq6", "textValue": "100", "isDelete": 0 }
    ]
  }
}
```
- 说明：后端会先调用 Strapi `POST /api/upload` 上传文件，拿到返回的文件 `id`，再写入 Strapi `achievement-files` 的媒体字段 `files` 完成关联。

### 4.5 更新成果物（JSON）

- Method：`PUT`
- Path：`/admin/achievement/update/{achievementDocId}`
- Content-Type：`application/json`
- Response：`Result<JsonNode>`
- Body（示例，按你当前格式）：
```json
{
  "data": {
    "title": "深度学习研究论文（修订版）",
    "summary": "更新后的摘要",
    "fields": [
      { "documentId": "f4gfyu5laa8efvxajqpb15ok", "textValue": "A-002" },
      { "documentId": "kf1nmtlbn5zl9mbnqn3p1etb", "textValue": "A-001" }
    ]
  }
}
```
- 说明：管理员更新时，后端会强制把 `achievement_status` 改为 `PENDING`（即使前端没传）。

### 4.6 一次请求“上传文件 + 更新成果物”（multipart/form-data）

- Method：`PUT`
- Path：`/admin/achievement/updateWithFiles/{achievementDocId}`
- Content-Type：`multipart/form-data`
- Response：`Result<JsonNode>`
- Form Data：
  - `data`：Text（JSON 字符串，内容就是 4.5 的 JSON）
  - `files`：File（可多文件，可不传）

### 4.7 单独修改可见范围（不触发强制 PENDING）

- Method：`PUT`
- Path：`/admin/achievement/updateVisibilityRange`
- Content-Type：`application/json`
- Response：`Result<JsonNode>`
- Body（示例）：
```json
{
  "documentId": "kashrvfxl0ohti6xt3cp96ug",
  "data": {
    "visibility_range": "PERSONAL"
  }
}
```

### 4.8 删除成果物（软删除）

- Method：`PUT`
- Path：`/admin/achievement/delete/{achievementDocId}`
- Content-Type：`application/json`
- Body：无
- Response：`Result<JsonNode>`

## 5) 用户成果物管理（AchievementUserController）

Base Path：`/user/achievement`

### 5.1 分页列表（当前用户）

- Method：`POST`
- Path：`/user/achievement/pageList`
- Content-Type：`application/json`
- Body（`AchListDTO` 示例，同 4.1）
- Response：`Result<Page<AchListVO>>`

### 5.2 分页列表（所有可见）

- Method：`POST`
- Path：`/user/achievement/pageListAllVisible`
- Content-Type：`application/json`
- Body（`AchListDTO2` 示例）：
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "typeId": 1,
  "recentRange": "THREE_YEARS",
  "creatorId": 1,
  "visibilityRange": "PERSONAL"
}
```
- Response：`Result<Page<AchListVO>>`

### 5.3 详情（含附件）

- Method：`GET`
- Path：`/user/achievement/detail`
- Query：
  - `achDocId`：成果物 `documentId`
- 示例：`GET /user/achievement/detail?achDocId=kashrvfxl0ohti6xt3cp96ug`
- Response：`Result<AchDetailVO>`

### 5.4 创建成果物（JSON）

- Method：`POST`
- Path：`/user/achievement/create`
- Content-Type：`application/json`
- Body：同 4.3
- Response：`Result<JsonNode>`

### 5.5 更新成果物（JSON）

- Method：`PUT`
- Path：`/user/achievement/update/{achievementDocId}`
- Content-Type：`application/json`
- Body：同 4.5
- Response：`Result<JsonNode>`

### 5.6 单独修改可见范围

- Method：`PUT`
- Path：`/user/achievement/updateVisibilityRange`
- Content-Type：`application/json`
- Body：同 4.7
- Response：`Result<JsonNode>`

## 6) 统计（AdminStatController / UserStatController）

### 6.1 管理员统计

Base Path：`/admin/stat`

- `GET /admin/stat/AchCount`：系统成果物总数（无参）
- Response：`Result<Long>`
- `GET /admin/stat/TypeAchCount?typeId=1`：某类型成果物数量
- Response：`Result<Long>`
- `GET /admin/stat/MonthNewAchCount`：本月新增成果物数量（无参）
- Response：`Result<Long>`
- `GET /admin/stat/typePie`：类型饼图统计
  - Query：`creatorId` 可选；不传 = 全量
  - Response：`Result<List<TypeCountVO>>`

### 6.2 用户统计

Base Path：`/user/stat`

- `GET /user/stat/userAchCount`：当前用户成果物数量（无参）
- Response：`Result<Long>`
- `GET /user/stat/userTypeAchCount?typeId=1`：当前用户某类型成果物数量
- Response：`Result<Long>`
- `GET /user/stat/userMonthNewAchCount`：当前用户本月新增成果物数量（无参）
- Response：`Result<Long>`

## 7) 智能补全（AutoFillController）

Base Path：`/results`

### 7.1 期刊等级查询（后端代理 EasyScholar）

- Method：`GET`
- Path：`/results/auto-fill`
- Query：
  - `type`：固定值 `journalRank`
  - `value`：期刊名称（publicationName）
- Response：`Result<JsonNode>`（`data` 为 EasyScholar 的 `data` 节点）
- 说明：
  - 通过 `EASYSCHOLAR_SECRET_KEY`（读取 `.env`）进行鉴权
  - `code != 200` 会返回 `Result.error(msg)`
  - 请求速率限制：最多 2 次/秒
