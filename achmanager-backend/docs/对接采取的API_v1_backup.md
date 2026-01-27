# 成果管理系统 RESTful API 文档

## 概述

本文档为过程管理系统提供的对接API规范，用于项目成果物的存储、查询和管理。

**版本**: v1  
**基础路径**: `http://<host>:<port>/api/v1/process-system`  
**协议**: HTTP/HTTPS  
**数据格式**: JSON (UTF-8)  
**认证方式**: API Key + HMAC-SHA256 签名

### 环境配置

| 环境 | Base URL |
|------|----------|
| 开发环境 | `http://localhost:8080/api/v1/process-system` |
| 测试环境 | `http://test-server:8080/api/v1/process-system` |
| 生产环境 | `https://api.achievement.com/v1/process-system` |

---

## 认证与安全

所有API请求必须包含以下认证信息：

### HTTP Headers

每个请求必须包含以下请求头：

| 字段名 | 必填 | 说明 | 示例 |
| :--- | :--- | :--- | :--- |
| `X-API-Key` | 是 | 分配给过程系统的唯一标识 Key。也可使用 `Authorization: Bearer <ApiKey>` | `proc_sys_001` |
| `X-Signature` | 是 | 请求签名，用于验证请求的完整性和来源 | `aGVsbG...` (Base64编码) |
| `X-Timestamp` | 是 | 请求发起时的 Unix 时间戳（**秒**） | `1706083200` |

### 签名算法 (HMAC-SHA256)

为保证请求安全，所有请求必须使用 HMAC-SHA256 算法生成签名。

**签名生成步骤**：

1.  **构造签名字符串 (Signature String)**:
    按照以下顺序拼接字符串，字段间使用换行符 `\n` 分隔。
    ```text
    HTTP_METHOD + "\n" +
    REQUEST_URI + "\n" +
    QUERY_STRING + "\n" +
    TIMESTAMP
    ```
    *   `HTTP_METHOD`: 请求方法，大写 (e.g., `POST`, `GET`)。
    *   `REQUEST_URI`: 请求路径 (e.g., `/api/v1/process-system/submissions`)。
    *   `QUERY_STRING`: URL 查询参数字符串（不包含 `?`，保持原始顺序）。如果无查询参数，则为空字符串（即直接接下一个换行符）。
    *   `TIMESTAMP`: 与请求头 `X-Timestamp` 一致的值。

2.  **生成 HMAC-SHA256 摘要**:
    使用分配的 **Secret Key** 对上述签名字符串进行 HMAC-SHA256 加密。

3.  **Base64 编码**:
    将加密后的二进制数据进行 Base64 编码，得到最终的 `X-Signature` 值。

**代码示例 (Java)**:
```java
String signatureString = method + "\n" + uri + "\n" + (queryString != null ? queryString : "") + "\n" + timestamp;
Mac mac = Mac.getInstance("HmacSHA256");
SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
mac.init(secretKeySpec);
String signature = Base64.getEncoder().encodeToString(mac.doFinal(signatureString.getBytes(StandardCharsets.UTF_8)));
```

### 时间戳有效期

- 默认有效期：300秒（5分钟）
- 超过有效期的请求将被拒绝

### 请求频率限制

- 默认限制：100 次/分钟
- 响应头包含限流信息：
  - `X-RateLimit-Limit`: 当前窗口允许的最大请求数
  - `X-RateLimit-Remaining`: 当前窗口剩余请求数
  - `X-RateLimit-Reset`: 重置时间戳（Unix 秒）

---

## 响应格式

### 标准响应结构

所有API返回统一的JSON响应格式：

```json
{
  "code": 1,          // 状态码：1 表示成功，其他值表示失败
  "msg": "success",   // 提示信息
  "data": { ... }     // 业务数据
}
```

**字段说明**：

- `code`: 业务状态码，1 表示成功，其他值表示失败
- `msg`: 响应消息
- `data`: 业务数据，成功时返回

### HTTP 状态码

| HTTP Status | 场景 |
|-------------|------|
| 200 | 请求成功或业务错误 |
| 400 | 请求参数错误 |
| 401 | 认证失败 |
| 403 | 权限不足/IP限制 |
| 404 | 资源不存在 |
| 429 | 请求过于频繁 |
| 500 | 服务器内部错误 |

### 错误码对照表

| 错误码 (code) | 状态码 (HTTP) | 说明 |
| :--- | :--- | :--- |
| 1 | 200 | 请求成功 |
| 10001 | 400 | 请求参数校验失败/绑定失败/JSON格式错误 |
| 10002 | 401 | API 密钥无效 |
| 10003 | 401 | 签名验证失败 |
| 10005 | 401 | 请求时间戳无效（超过有效期） |
| 10004 | 403 | IP 地址不在白名单中 |
| 10004 | 429 | 请求频率超出限制 |
| 10006 | 200 | 请求被安全策略拒绝 |
| 10008 | 500 | 系统内部错误 |
| 404 | 200 | 资源不存在（如 submission/file 不存在） |

---

## 数据模型

### 字段命名约定

- **请求参数**: 同时支持 `snake_case` 和 `camelCase`，推荐使用 `snake_case`
- **响应数据**: 使用 `camelCase`（部分接口使用 `snake_case`）

### 数据校验规则

#### 提交物类型 (submission_type)

支持的类型及说明：

| 值 | 说明 | 对应场景 |
|------|------|----------|
| `proposal` | 申报书 | 项目申报阶段的申报书文件 |
| `application_attachment` | 申报附件 | 申报阶段的其他附件 |
| `contract_template` | 合同模板 | 管理员上传的合同模板 |
| `signed_contract` | 已签署合同 | 用户上传的已签署合同 |
| `deliverable_report` | 成果物报告 | 项目成果物及验收材料 |

#### 文件分类 (file_category)

| 值 | 说明 |
|------|------|
| `proposal` | 申报书文件 |
| `attachment` | 附件 |
| `contract_template` | 合同模板文件 |
| `signed_contract` | 已签署合同文件 |
| `deliverable` | 成果物文件 |
| `inspection_report` | 验收报告文件 |

#### 其他校验规则
*   `submission_stage` 仅支持: `application`, `review`, `execution`。
*   `category_level` 仅支持: `重点`, `一般`。
*   `submission_round` / `submission_version` 必须大于 0。
*   `file_type` 为文件扩展名（不区分大小写），仅支持: `pdf`, `doc`, `docx`, `xls`, `xlsx`, `ppt`, `pptx`, `txt`, `zip`, `rar`。
*   单个文件大小 `file_size` 不能超过 50MB，且必须大于 0。
*   `upload_time` 支持格式: `yyyy-MM-dd'T'HH:mm:ss`, `yyyy-MM-dd HH:mm:ss`, `yyyy/MM/dd HH:mm:ss`, `yyyy-MM-dd`。
*   常见格式校验: 手机号为 11 位中国大陆号码，邮箱/身份证号需满足标准格式。
*   主要长度限制: `project_name` <= 200，`project_description` <= 5000，`applicant_name` <= 100，`work_unit` <= 200，`file_name`/`file_id` <= 200，`uploader_id` <= 64，`uploader_name` <= 100，`submission_description` <= 1000。
*   `willing_adjust` 仅支持 `Y` 或 `N`。

---

## API 接口

### 1. 提交物管理

#### 1.1 提交物响应模型 (ProcessSubmissionVO)

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `submissionId` | Long | 提交物ID |
| `applicationId` | Long | 申报ID |
| `submissionType` | String | 提交物类型 |
| `submissionStage` | String | 提交阶段 |
| `submissionRound` | Integer | 提交轮次 |
| `submissionVersion` | Integer | 版本号 |
| `projectName` | String | 项目名称 |
| `projectField` | String | 所属领域 |
| `categoryLevel` | String | 类别级别 |
| `categorySpecific` | String | 具体分类 |
| `researchPeriod` | Integer | 研究周期(月) |
| `projectKeywords` | String | 关键词 |
| `projectDescription` | String | 项目描述 |
| `expectedResults` | String | 预期成果 |
| `willingAdjust` | String | 是否愿意调剂 (Y/N) |
| `applicantName` | String | 负责人姓名 |
| `idCard` | String | 证件号码 |
| `educationDegree` | String | 学历学位 |
| `technicalTitle` | String | 技术职称 |
| `email` | String | 邮箱 |
| `phone` | String | 联系电话 |
| `workUnit` | String | 工作单位 |
| `unitAddress` | String | 单位地址 |
| `representativeAchievements` | String | 代表性成果 |
| `proposalFile` | FileVO | 申报书文件 |
| `otherAttachments` | FileVO[] | 其他附件 |
| `uploaderId` | String | 上传者ID |
| `uploaderName` | String | 上传者名称 |
| `uploadTime` | String | 上传时间 (ISO-8601) |
| `submissionDescription` | String | 提交物描述 |
| `syncTime` | String | 同步时间 (ISO-8601) |

**FileVO 结构**:

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `fileId` | String | 文件ID |
| `fileName` | String | 文件名称 |
| `fileSize` | Long | 文件大小 (Bytes) |
| `fileType` | String | 文件扩展名 |
| `fileUrl` | String | 文件URL |

#### 1.2 创建提交物

存储项目提交物（申报书、合同、成果物等）。

```http
POST /api/v1/process-system/submissions
Content-Type: application/json
X-API-Key: {api_key}
X-Signature: {signature}
X-Timestamp: {timestamp}
```

**Request Body**

| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `submission_id` | Long | 是 | 过程系统中的唯一提交记录ID |
| `application_id` | Long | 是 | 项目申报ID |
| `submission_type` | String | 是 | 提交物类型 (`proposal`/`application_attachment`/`contract_template`/`signed_contract`/`deliverable_report`) |
| `submission_stage` | String | 是 | 提交阶段 (`application`, `review`, `execution`) |
| `submission_round` | Integer | 否 | 提交轮次 (默认 1) |
| `submission_version` | Integer | 否 | 版本号 (默认 1) |
| `project_info` | Object | 是 | 项目基本信息 |
| `applicant_info` | Object | 是 | 申报人/负责人信息 |
| `files` | Object | 是 | 文件列表信息 |
| `upload_info` | Object | 是 | 上传操作信息 |

**ProjectInfo 结构**:
| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `project_name` | String | 是 | 项目名称 |
| `project_field` | String | 否 | 所属领域 |
| `category_level` | String | 是 | 类别级别 (`重点`, `一般`) |
| `category_specific` | String | 是 | 具体分类 |
| `research_period` | Integer | 否 | 研究周期(月) |
| `project_keywords` | String | 否 | 关键词 |
| `project_description` | String | 是 | 项目摘要/描述 |
| `expected_results` | String | 否 | 预期成果 |
| `willing_adjust` | String | 否 | 是否愿意调剂 (Y/N) |

**ApplicantInfo 结构**:
| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `applicant_name` | String | 是 | 负责人姓名 |
| `id_card` | String | 否 | 证件号码 |
| `education_degree` | String | 否 | 学历学位 |
| `technical_title` | String | 否 | 技术职称 |
| `email` | String | 否 | 邮箱 |
| `phone` | String | 是 | 联系电话 |
| `work_unit` | String | 否 | 工作单位 |
| `unit_address` | String | 否 | 单位地址 |
| `representative_achievements` | String | 否 | 代表性成果 |

**FileInfo 结构**:
| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `proposal_file` | FileDetail | 是 | 主文件（如申报书PDF） |
| `other_attachments` | FileDetail[] | 否 | 其他附件列表 |

**FileDetail 结构**:
| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `file_id` | String | 是 | 文件唯一ID |
| `file_name` | String | 是 | 文件名（含后缀） |
| `file_size` | Long | 否 | 文件大小 (Bytes) |
| `file_type` | String | 否 | 文件扩展名 |
| `file_url` | String | 否 | 下载链接 |

**UploadInfo 结构**:
| 字段 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `uploader_id` | String | 是 | 操作人ID |
| `uploader_name` | String | 是 | 操作人姓名 |
| `submission_description` | String | 否 | 提交备注 |
| `upload_time` | String | 否 | 上传时间 |

**响应示例**:
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "submission_id": 12345,
    "application_id": 1001,
    "sync_time": "2026-01-24T10:00:00"
  }
}
```

#### 1.3 查询提交物列表

根据条件查询提交物列表。

```http
GET /api/v1/process-system/submissions?applicationId={id}&submissionType={type}
X-API-Key: {api_key}
X-Signature: {signature}
X-Timestamp: {timestamp}
```

**Query Parameters**

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `applicationId` | Long | 否 | 申报ID |
| `submissionStage` | String | 否 | 提交阶段 |
| `submissionType` | String | 否 | 提交物类型 |
| `projectName` | String | 否 | 项目名称（模糊匹配） |
| `applicantName` | String | 否 | 申报人姓名（模糊匹配） |

**响应示例**:
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "total": 10,
    "submissions": [
      {
        "submissionId": 12345,
        "applicationId": 1001,
        "projectName": "测试项目",
        "submissionType": "proposal",
        "submissionStage": "application"
      }
    ]
  }
}
```

#### 1.4 获取提交物详情

根据提交物ID获取详细信息。

```http
GET /api/v1/process-system/submissions/{submissionId}
X-API-Key: {api_key}
X-Signature: {signature}
X-Timestamp: {timestamp}
```

**Path Parameters**

| 参数 | 类型 | 说明 |
|------|------|------|
| submissionId | Long | 提交物ID |

**Response**

返回 `ProcessSubmissionVO` 对象。

#### 1.5 根据申报ID获取提交物

获取指定申报ID的所有提交物。

```http
GET /api/v1/process-system/applications/{applicationId}/submissions
X-API-Key: {api_key}
X-Signature: {signature}
X-Timestamp: {timestamp}
```

**Path Parameters**

| 参数 | 类型 | 说明 |
|------|------|------|
| applicationId | Long | 申报ID |

**Response**

返回 `ProcessSubmissionVO[]` 数组。

#### 1.6 根据申报ID和阶段获取提交物

获取指定申报和阶段的提交物。

```http
GET /api/v1/process-system/applications/{applicationId}/submissions/{submissionStage}
X-API-Key: {api_key}
X-Signature: {signature}
X-Timestamp: {timestamp}
```

**Path Parameters**

| 参数 | 类型 | 说明 |
|------|------|------|
| applicationId | Long | 申报ID |
| submissionStage | String | 提交阶段 |

**Response**

返回 `ProcessSubmissionVO[]` 数组。

#### 1.7 获取版本历史

获取指定轮次的所有版本历史。

```http
GET /api/v1/process-system/applications/{applicationId}/submissions/{submissionType}/{submissionStage}/rounds/{submissionRound}/versions
X-API-Key: {api_key}
X-Signature: {signature}
X-Timestamp: {timestamp}
```

**Path Parameters**

| 参数 | 类型 | 说明 |
|------|------|------|
| applicationId | Long | 申报ID |
| submissionType | String | 提交物类型 |
| submissionStage | String | 提交阶段 |
| submissionRound | Integer | 提交轮次 |

**Response**

返回 `ProcessSubmissionVO[]` 数组。

#### 1.8 获取轮次历史

获取指定类型和阶段的所有轮次历史。

```http
GET /api/v1/process-system/applications/{applicationId}/submissions/{submissionType}/{submissionStage}/rounds
X-API-Key: {api_key}
X-Signature: {signature}
X-Timestamp: {timestamp}
```

**Path Parameters**

| 参数 | 类型 | 说明 |
|------|------|------|
| applicationId | Long | 申报ID |
| submissionType | String | 提交物类型 |
| submissionStage | String | 提交阶段 |

**Response**

返回 `ProcessSubmissionVO[]` 数组。

#### 1.9 获取指定版本详情

获取特定轮次和版本的提交物详情。

```http
GET /api/v1/process-system/applications/{applicationId}/submissions/{submissionType}/{submissionStage}/rounds/{submissionRound}/versions/{submissionVersion}
X-API-Key: {api_key}
X-Signature: {signature}
X-Timestamp: {timestamp}
```

**Path Parameters**

| 参数 | 类型 | 说明 |
|------|------|------|
| applicationId | Long | 申报ID |
| submissionType | String | 提交物类型 |
| submissionStage | String | 提交阶段 |
| submissionRound | Integer | 提交轮次 |
| submissionVersion | Integer | 版本号 |

**Response**

返回 `ProcessSubmissionVO` 对象。

#### 1.10 获取完整历史

获取指定类型和阶段的所有历史记录（包括所有轮次和版本）。

```http
GET /api/v1/process-system/applications/{applicationId}/submissions/{submissionType}/{submissionStage}/history
X-API-Key: {api_key}
X-Signature: {signature}
X-Timestamp: {timestamp}
```

**Path Parameters**

| 参数 | 类型 | 说明 |
|------|------|------|
| applicationId | Long | 申报ID |
| submissionType | String | 提交物类型 |
| submissionStage | String | 提交阶段 |

**Response**

返回 `ProcessSubmissionVO[]` 数组，按时间倒序排列。

---

### 2. 文件管理

#### 2.1 文件响应模型 (ProcessSubmissionFile)

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `fileId` | String | 文件唯一标识 |
| `submissionId` | Long | 关联的提交物ID |
| `fileName` | String | 文件名称 |
| `originalName` | String | 文件原始名称 |
| `fileSize` | Long | 文件大小 (Bytes) |
| `fileType` | String | 文件扩展名 |
| `mimeType` | String | MIME类型 |
| `filePath` | String | 文件存储路径 |
| `fileUrl` | String | 文件访问URL |
| `fileCategory` | String | 文件分类 (`proposal`/`attachment`/`contract_template`/`signed_contract`/`deliverable`/`inspection_report`) |
| `fileDescription` | String | 文件描述 |
| `fileMd5` | String | 文件MD5校验值 |
| `storageStatus` | String | 存储状态 (`uploaded`/`processing`/`completed`/`failed`) |
| `uploaderId` | String | 上传者用户ID |
| `uploaderName` | String | 上传者名称 |
| `uploadTime` | String | 上传时间 (ISO-8601) |
| `createBy` | String | 创建者ID |
| `createTime` | String | 创建时间 |
| `updateBy` | String | 更新者ID |
| `updateTime` | String | 更新时间 |
| `delFlag` | String | 删除标志 |
| `remark` | String | 备注 |

#### 2.2 查询文件列表

根据条件查询文件列表。

```http
GET /api/v1/process-system/files?submissionId={id}&fileCategory={category}
X-API-Key: {api_key}
X-Signature: {signature}
X-Timestamp: {timestamp}
```

**Query Parameters**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `submissionId` | Long | 否 | 提交物ID |
| `fileCategory` | String | 否 | 文件分类 (`proposal`/`attachment`/`contract_template`/`signed_contract`/`deliverable`/`inspection_report`) |
| `fileName` | String | 否 | 文件名称（模糊匹配） |
| `fileType` | String | 否 | 文件扩展名 |

**响应**:
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "total": 2,
    "files": [ { ... } ]
  }
}
```

#### 2.3 获取文件元数据

根据文件ID获取详细元数据信息。

```http
GET /api/v1/process-system/files/{fileId}
X-API-Key: {api_key}
X-Signature: {signature}
X-Timestamp: {timestamp}
```

**Path Parameters**

| 参数 | 类型 | 说明 |
|------|------|------|
| fileId | String | 文件ID |

**Response**

返回 `ProcessSubmissionFile` 对象。

#### 2.4 根据提交物ID获取文件

获取指定提交物的所有文件。

```http
GET /api/v1/process-system/submissions/{submissionId}/files?fileCategory={category}
X-API-Key: {api_key}
X-Signature: {signature}
X-Timestamp: {timestamp}
```

**Path Parameters**

| 参数 | 类型 | 说明 |
|------|------|------|
| submissionId | Long | 提交物ID |

**Query Parameters**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| fileCategory | String | 否 | 文件分类过滤 |

**Response**

返回 `ProcessSubmissionFile[]` 数组。

#### 2.5 获取文件下载链接

生成单个文件的临时下载链接。

```http
GET /api/v1/process-system/files/{fileId}/download-url
X-API-Key: {api_key}
X-Signature: {signature}
X-Timestamp: {timestamp}
```

**Path Parameters**

| 参数 | 类型 | 说明 |
|------|------|------|
| fileId | String | 文件ID |

**Response**
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "file_id": "F123",
    "file_name": "proposal.pdf",
    "download_url": "https://example.com/download/xxx",
    "expires_in": 3600
  }
}
```

#### 2.6 批量获取下载链接

批量生成多个文件的临时下载链接。

```http
POST /api/v1/process-system/files/batch-download-urls
Content-Type: application/json
X-API-Key: {api_key}
X-Signature: {signature}
X-Timestamp: {timestamp}
```

**Request Body**
```json
["F001", "F002", "F003"]
```

**说明**: 单次最多 100 个文件ID。

**响应示例**:
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "total": 3,
    "download_urls": {
      "F001": "https://example.com/download/aaa",
      "F002": "https://example.com/download/bbb",
      "F003": "https://example.com/download/ccc"
    },
    "expires_in": 3600
  }
}
```

---

### 3. 系统监控

#### 3.1 健康检查

检查API服务状态和配置信息。

```http
GET /api/v1/process-system/health
X-API-Key: {api_key}
X-Signature: {signature}
X-Timestamp: {timestamp}
```

**Response**
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "status": "UP",
    "timestamp": "2026-01-24T10:00:00",
    "version": "v1",
    "fileStorageBasePath": "/data/process-system/files",
    "maxFileSize": 104857600,
    "allowedFileTypes": ["pdf", "doc", "docx", "xls", "xlsx", "txt", "zip", "rar"]
  }
}
```
