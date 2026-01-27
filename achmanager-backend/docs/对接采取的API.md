# 成果管理系统 RESTful API 文档 (精简版)

## 1. 概述

本文档为 **过程管理系统 (Process System)** 对接 **成果管理系统 (Achievement Manager)** 的标准接口规范。
本版已根据业务需求进行精简，仅保留成果归档与展示所需的核心字段，移除冗余的项目过程详情与敏感个人信息。

- **版本**: v2.0
- **基础路径**: `/api/v1/process-system`
- **协议**: HTTP/HTTPS
- **数据格式**: JSON (UTF-8)

### 1.1 环境配置

| 环境 | Base URL | 说明 |
|------|----------|------|
| 开发环境 | `http://localhost:8080/api/v1/process-system` | 本地开发调试 |
| 测试环境 | `http://test.achievement.com:8080/api/v1/process-system` | 联调测试环境 |
| 生产环境 | `https://api.achievement.com/v1/process-system` | 正式生产环境 |

### 1.2 认证鉴权

所有请求必须包含以下 HTTP Headers：

| Header 名称 | 必填 | 说明 | 示例 |
| :--- | :--- | :--- | :--- |
| `X-API-Key` | 是 | 系统分配的唯一标识 | `proc_sys_001` |
| `X-Signature` | 是 | HMAC-SHA256 签名 (Base64) | `aGVsbG93b3JsZA==` |
| `X-Timestamp` | 是 | Unix 时间戳 (秒) | `1706256000` |
| `Content-Type` | POST 时必填 | 内容类型 | `application/json` |

**签名算法**：

```text
签名字符串 = HTTP_METHOD + "\n" + REQUEST_URI + "\n" + QUERY_STRING + "\n" + TIMESTAMP
签名值 = Base64(HMAC-SHA256(签名字符串, SECRET_KEY))
```

**Java 示例**：

```java
String signatureString = method + "\n" + uri + "\n" + 
    (queryString != null ? queryString : "") + "\n" + timestamp;
Mac mac = Mac.getInstance("HmacSHA256");
SecretKeySpec secretKeySpec = new SecretKeySpec(
    secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
mac.init(secretKeySpec);
String signature = Base64.getEncoder()
    .encodeToString(mac.doFinal(signatureString.getBytes(StandardCharsets.UTF_8)));
```

### 1.3 请求频率限制

- 默认限制：**100 次/分钟**
- 超限响应：HTTP 429 Too Many Requests
- 响应头包含：
  - `X-RateLimit-Limit`: 窗口内允许的最大请求数
  - `X-RateLimit-Remaining`: 当前窗口剩余请求数
  - `X-RateLimit-Reset`: 窗口重置时间戳

### 1.4 统一响应格式

所有接口返回统一的 JSON 结构：

```json
{
  "code": 1,           // 业务状态码：1-成功，其他-失败
  "msg": "success",    // 响应消息
  "data": {...}        // 业务数据 (成功时返回)
}
```

**常见错误码**：

| code | HTTP Status | 说明 |
|------|-------------|------|
| 1 | 200 | 成功 |
| 10001 | 400 | 请求参数校验失败 |
| 10002 | 401 | API Key 无效 |
| 10003 | 401 | 签名验证失败 |
| 10005 | 401 | 时间戳过期 (超过5分钟) |
| 10004 | 429 | 请求频率超限 |
| 10008 | 500 | 系统内部错误 |
| 404 | 200 | 资源不存在 |

---

## 2. 核心数据模型

### 2.1 提交物对象 (Submission)

基于业务表结构 (`application`, `contract`, `deliverable`) 提取的通用与专用字段模型。

| 字段名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| **通用头信息** | | | |
| `submission_id` | Long | 是 | 过程系统唯一标识 (PK) |
| `application_id` | Long | 是 | 关联项目申报ID (FK) |
| `submission_type` | String | 是 | `proposal`, `signed_contract`, `deliverable_report` |
| `submission_stage` | String | 是 | 阶段: `application`, `execution`, `review` |
| `upload_time` | String | 是 | 提交时间 |
| **项目/申请人快照** | | | |
| `project_name` | String | 是 | 项目名称 |
| `applicant_name` | String | 是 | 负责人姓名 |
| `work_unit` | String | 否 | 单位 (申报书表包含) |
| **业务扩展信息** | Object | 否 | 根据类型不同包含不同字段 (见下文) |
| **文件集合** | Object | 是 | 包含具体业务文件 (见 2.2) |

#### 业务扩展信息 (`business_data`) 定义

**A. 申报书类型 (`proposal`)**
| 字段名 | 类型 | 对应表字段 | 说明 |
| :--- | :--- | :--- | :--- |
| `project_description` | String | `project_description` | 项目描述 (必填) |
| `project_keywords` | String | `project_keywords` | 关键词 |
| `project_field` | String | `project_field` | 领域 |

**B. 合同类型 (`signed_contract`)**
| 字段名 | 类型 | 对应表字段 | 说明 |
| :--- | :--- | :--- | :--- |
| `contract_remarks` | String | `contract_remarks` | 合同备注 |

**C. 成果物类型 (`deliverable_report`)**
| 字段名 | 类型 | 对应表字段 | 说明 |
| :--- | :--- | :--- | :--- |
| `check_round` | Integer | `check_round` | **核心**: 检查轮次 (1=首轮, 0=最终验收) |
| `check_title` | String | `check_title` | 验收轮次名称 |
| `project_progress` | String | `project_progress` | 项目进展描述 (必填) |
| `major_achievements` | String | `major_achievements` | 主要成果描述 (必填) |
| `problems_solutions` | String | `problems`/`solutions` | 问题与对策 (合并存储) |

### 2.2 文件集合对象 (Files)

为精确匹配表结构，不再使用通用的 `main_file`，而是使用**业务专用文件键名**。

**A. 申报书文件结构**
```json
"files": {
  "proposal_file": { "file_id": "...", "file_url": "..." },  // 对应 proposal_file_*
  "attachments": [] 
}
```

**B. 合同文件结构**
```json
"files": {
  "signed_contract_file": { "file_id": "...", "file_url": "..." }, // 对应 signed_contract_file_*
  "attachments": []
}
```

**C. 成果物文件结构 (包含双核心文件)**
```json
"files": {
  "deliverable_file": { "file_id": "...", "file_url": "..." },       // 对应 deliverable_file_*
  "inspection_report_file": { "file_id": "...", "file_url": "..." },  // 对应 inspection_report_file_*
  "attachments": []
}
```

> **注意**: `attachments` 对应表中的非结构化附件（如有），若表中无明确附件列可忽略。

### 2.3 文件详情 (FileDetail)

(通用结构保持不变: `file_id`, `file_name`, `file_url`, `file_size`, `file_type`)

| 字段名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `file_id` | String | 是 | 文件唯一标识 (最大长度 200) |
| `file_name` | String | 是 | 原文件名 (含扩展名，最大长度 200) |
| `file_url` | String | 是 | 文件下载地址 (需确保成果系统可访问) |
| `file_size` | Long | 否 | 文件大小 (字节，最大 50MB) |
| `file_type` | String | 否 | 文件扩展名 (如: pdf, docx, xlsx) |

**文件类型限制**：
- 支持的扩展名: `pdf`, `doc`, `docx`, `xls`, `xlsx`, `ppt`, `pptx`, `txt`, `zip`, `rar`
- 单文件最大: 50 MB (52428800 字节)
- `file_url` 必须为可公开访问的 HTTP/HTTPS 地址

---

## 3. API 接口详情

### 3.1 创建/同步提交物 ⭐️

用于过程系统向成果系统推送新的成果数据。

**接口信息**：
- **URL**: `POST /api/v1/process-system/submissions`
- **Content-Type**: `application/json`
- **幂等性**: 是 (基于 `submission_id` 去重)

**请求体结构 (示例：成果物提交)**：

```json
{
  "submission_id": 10086,
  "application_id": 555,
  "submission_type": "deliverable_report",
  "submission_stage": "execution",
  
  "project_name": "新一代高性能计算框架研究",
  "applicant_name": "王研究员",
  "upload_time": "2026-01-27T14:30:00",
  
  // 业务扩展信息
  "business_data": {
    "check_round": 1,
    "check_title": "中期检查",
    "project_progress": "已完成核心算法开发...",
    "major_achievements": "发表论文2篇，申请专利1项...",
    "problems_solutions": "人员调度紧张，已申请增援。"
  },
  
  // 文件集合 (精确匹配表列名)
  "files": {
    // 对应 deliverable_file_*
    "deliverable_file": {
      "file_id": "DEL_001",
      "file_name": "中期技术报告.pdf",
      "file_url": "http://process-sys/files/DEL_001",
      "file_size": 2048576,
      "file_type": "pdf"
    },
    // 对应 inspection_report_file_*
    "inspection_report_file": {
      "file_id": "INS_001",
      "file_name": "中期自查报告.pdf",
      "file_url": "http://process-sys/files/INS_001",
      "file_size": 1024000,
      "file_type": "pdf"
    }
  }
}
```      "file_url": "http://process-sys.example.com/files/get/FILE_X1",
      "file_size": 2048576,
      "file_type": "pdf"
    },
    // 其他非结构化附件
    "attachments": [
      {
        "file_id": "FILE_X2",

        "file_name": "实验数据.xlsx",
        "file_url": "http://process-sys.example.com/files/get/FILE_X2",
        "file_size": 512000,
        "file_type": "xlsx"
      }
    ]
  }
}
```

**成功响应** (HTTP 200):

```json
{
  "code": 1,
  "msg": "提交物同步成功",
  "data": {
    "submission_id": 10086,
    "application_id": 555,
    "sync_status": "success",
    "sync_time": "2026-01-27T14:30:15"
  }
}
```

**失败响应示例**：

```json
// 参数校验失败 (HTTP 200)
{
  "code": 10001,
  "msg": "参数校验失败: project_name 不能为空",
  "data": null
}

// 文件 URL 不可访问 (HTTP 200)
{
  "code": 10006,
  "msg": "文件下载失败: FILE_X1 无法访问",
  "data": null
}
```

**注意事项**：
1. `submission_id` 相同时视为更新操作，会覆盖已存在的记录
2. 文件下载失败不会阻断提交，但会在响应中标注失败文件
3. `upload_time` 若未提供，系统将使用当前时间

---

### 3.2 查询提交物列表

根据条件查询已同步的成果数据。

**接口信息**：
- **URL**: `GET /api/v1/process-system/submissions`
- **分页**: 支持

**Query 参数**：

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `applicationId` | Long | 否 | 项目申报ID |
| `projectName` | String | 否 | 项目名称 (模糊搜索) |
| `applicantName` | String | 否 | 提交人姓名 (模糊搜索) |
| `submissionType` | String | 否 | 提交物类型 |
| `submissionStage` | String | 否 | 提交阶段 |
| `page` | Integer | 否 | 页码 (默认 1) |
| `pageSize` | Integer | 否 | 每页数量 (默认 20，最大 100) |

**请求示例**：

```http
GET /api/v1/process-system/submissions?applicationId=555&page=1&pageSize=10
X-API-Key: proc_sys_001
X-Signature: aGVsbG93b3JsZA==
X-Timestamp: 1706256000
```

**成功响应**：

```json
{
  "code": 1,
  "msg": "查询成功",
  "data": {
    "total": 25,
    "page": 1,
    "pageSize": 10,
    "totalPages": 3,
    "submissions": [
      {
        "submissionId": 10086,
        "applicationId": 555,
        "projectName": "新一代高性能计算框架研究",
        "submissionType": "deliverable_report",
        "submissionStage": "execution",
        "applicantName": "王研究员",
        "uploadTime": "2026-01-27T14:30:00",
        "fileCount": 2,
        "syncTime": "2026-01-27T14:30:15"
      }
      // ... 更多记录
    ]
  }
}
```

---

### 3.3 获取提交物详情

根据 `submission_id` 获取完整的提交物信息（包括文件列表）。

**接口信息**：
- **URL**: `GET /api/v1/process-system/submissions/{submissionId}`

**Path 参数**：

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `submissionId` | Long | 是 | 提交物唯一ID |

**请求示例**：

```http
GET /api/v1/process-system/submissions/10086
X-API-Key: proc_sys_001
X-Signature: aGVsbG93b3JsZA==
X-Timestamp: 1706256000
```

**成功响应**：

```json
{
  "code": 1,
  "msg": "查询成功",
  "data": {
    "submissionId": 10086,
    "applicationId": 555,
    "submissionType": "deliverable_report",
    "submissionStage": "execution",
    "submissionRound": 1,
    "submissionVersion": 1,
    "projectName": "新一代高性能计算框架研究",
    "projectDescription": "本项目旨在解决分布式计算中的调度优化问题...",
    "projectKeywords": "HPC, 分布式计算, 负载均衡",
    "projectField": "计算机科学与技术",
    "applicantName": "王研究员",
    "workUnit": "计算技术研究所",
    "uploadTime": "2026-01-27T14:30:00",
    "syncTime": "2026-01-27T14:30:15",
    "files": [
      {
        "fileId": "FILE_X1",
        "fileName": "结题报告_v1.pdf",
        "fileUrl": "http://process-sys.example.com/files/get/FILE_X1",
        "fileSize": 2048576,
        "fileType": "pdf",
        "fileCategory": "deliverable"
      },
      {
        "fileId": "FILE_X2",
        "fileName": "实验数据.xlsx",
        "fileUrl": "http://process-sys.example.com/files/get/FILE_X2",
        "fileSize": 512000,
        "fileType": "xlsx",
        "fileCategory": "attachment"
      }
    ]
  }
}
```

**失败响应**：

```json
{
  "code": 404,
  "msg": "提交物不存在",
  "data": null
}
```

---

### 3.4 根据申报ID获取提交物列表

获取指定项目的所有成果提交记录。

**接口信息**：
- **URL**: `GET /api/v1/process-system/applications/{applicationId}/submissions`

**Path 参数**：

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `applicationId` | Long | 是 | 项目申报ID |

**成功响应**：

```json
{
  "code": 1,
  "msg": "查询成功",
  "data": [
    {
      "submissionId": 10086,
      "submissionType": "deliverable_report",
      "submissionStage": "execution",
      "projectName": "新一代高性能计算框架研究",
      "uploadTime": "2026-01-27T14:30:00"
    }
    // ... 更多提交物
  ]
}
```

---

### 3.5 文件下载代理接口

为过程系统提供的文件下载接口（可选实现）。

**接口信息**：
- **URL**: `GET /api/v1/process-system/files/{fileId}/download`
- **响应类型**: 文件流 (application/octet-stream)

**请求示例**：

```http
GET /api/v1/process-system/files/FILE_X1/download
X-API-Key: proc_sys_001
X-Signature: aGVsbG93b3JsZA==
X-Timestamp: 1706256000
```

**成功响应**：
- HTTP 200，返回文件二进制流
- Response Headers:
  - `Content-Type`: 对应的 MIME 类型
  - `Content-Disposition`: `attachment; filename="结题报告_v1.pdf"`
  - `Content-Length`: 文件大小

---

### 3.6 健康检查

用于监控服务状态和配置信息。

**接口信息**：
- **URL**: `GET /api/v1/process-system/health`
- **鉴权**: 可选 (建议白名单IP访问)

**成功响应**：

```json
{
  "code": 1,
  "msg": "服务正常",
  "data": {
    "status": "UP",
    "timestamp": "2026-01-27T15:00:00",
    "version": "v2.0",
    "apiConfig": {
      "maxFileSize": 52428800,
      "allowedFileTypes": ["pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "zip", "rar"],
      "rateLimitPerMinute": 100
    }
  }
}
```

---

## 4. 附录

### 4.1 完整请求示例 (cURL)

```bash
# 创建提交物
curl -X POST "http://localhost:8080/api/v1/process-system/submissions" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: proc_sys_001" \
  -H "X-Signature: aGVsbG93b3JsZA==" \
  -H "X-Timestamp: 1706256000" \
  -d '{
    "submission_id": 10086,
    "application_id": 555,
    "submission_type": "deliverable_report",
    "submission_stage": "execution",
    "project_info": {
      "project_name": "新一代高性能计算框架研究",
      "project_description": "本项目旨在...",
      "project_keywords": "HPC, 分布式"
    },
    "applicant_info": {
      "applicant_name": "王研究员",
      "work_unit": "计算所"
    },
    "upload_info": {
      "upload_time": "2026-01-27T14:30:00"
    },
    "files": {
      "main_file": {
        "file_id": "FILE_X1",
        "file_name": "结题报告.pdf",
        "file_url": "http://process-sys/files/FILE_X1"
      }
    }
  }'

# 查询提交物列表
curl -X GET "http://localhost:8080/api/v1/process-system/submissions?applicationId=555&page=1" \
  -H "X-API-Key: proc_sys_001" \
  -H "X-Signature: aGVsbG93b3JsZA==" \
  -H "X-Timestamp: 1706256000"
```

### 4.2 数据类型映射

| API 类型 | Java 类型 | 数据库类型 | 说明 |
|---------|----------|-----------|------|
| Long | Long | BIGINT | 主键、ID 字段 |
| String | String | VARCHAR | 文本字段 |
| Integer | Integer | INT | 轮次、版本号 |
| Object | DTO/VO | JSON/扁平字段 | 嵌套对象 |

### 4.3 常见问题 (FAQ)

**Q1: 如何处理文件下载失败？**
A: 系统会在响应中标注失败文件，但不会阻断整个提交。建议过程系统监控失败记录并重试。

**Q2: 相同 submission_id 重复提交会怎样？**
A: 会覆盖已存在的记录，视为更新操作。

**Q3: 文件 URL 有效期要求？**
A: 建议提供永久有效或至少 7 天有效的下载链接。

**Q4: 是否支持批量创建？**
A: 当前版本不支持，请逐条调用创建接口。

**Q5: 时间戳过期后如何处理？**
A: 重新生成签名并更新 `X-Timestamp` 头，有效期为 5 分钟。

### 4.4 变更记录

| 版本 | 日期 | 变更内容 |
|------|------|---------|
| **v2.0** | 2026-01-27 | ✅ 精简字段模型，移除敏感信息和冗余项目详情<br>✅ 保留 `project_description` 和 `project_keywords`<br>✅ 完善接口文档，增加详细示例 |
| **v1.0** | 2026-01-24 | 初始版本，包含完整项目字段 |

---

## 5. 联系方式

- **技术支持**: dev-support@achievement.com
- **API 问题反馈**: api-feedback@achievement.com
- **文档更新通知**: 关注项目 `docs/` 目录变更记录
