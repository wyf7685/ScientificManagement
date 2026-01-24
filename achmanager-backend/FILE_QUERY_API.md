# 文件信息查询API接口文档

## 概述

本文档描述了过程系统集成中新增的文件信息查询API接口，这些接口允许过程管理系统查询和获取之前存储在成果管理系统中的文件信息。

## API接口列表

### 1. 获取文件列表

**接口地址**: `GET /api/v1/process-system/files`

**接口描述**: 根据查询条件获取文件清单

**请求参数**:
- `submissionId` (可选): 提交物ID
- `fileCategory` (可选): 文件分类 (proposal/attachment)
- `fileName` (可选): 文件名称（支持模糊查询）
- `fileType` (可选): 文件类型

**请求示例**:
```http
GET /api/v1/process-system/files?submissionId=12345&fileCategory=proposal
Authorization: Bearer {api_key}
X-Signature: {request_signature}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 2,
    "files": [
      {
        "fileId": "file_001",
        "submissionId": 12345,
        "fileName": "申报书.pdf",
        "originalName": "申报书.pdf",
        "fileSize": 2048576,
        "fileType": "pdf",
        "mimeType": "application/pdf",
        "filePath": "/uploads/proposals/file_001.pdf",
        "fileUrl": "/uploads/proposals/file_001.pdf",
        "fileCategory": "proposal",
        "fileDescription": "项目申报书文件",
        "fileMd5": "d41d8cd98f00b204e9800998ecf8427e",
        "storageStatus": "completed",
        "uploaderId": "user_123",
        "uploaderName": "李四",
        "uploadTime": "2026-01-24T10:30:00"
      }
    ]
  }
}
```

### 2. 获取文件元数据

**接口地址**: `GET /api/v1/process-system/files/{fileId}`

**接口描述**: 根据文件ID获取文件的详细元数据信息

**路径参数**:
- `fileId`: 文件ID（必填）

**请求示例**:
```http
GET /api/v1/process-system/files/file_001
Authorization: Bearer {api_key}
X-Signature: {request_signature}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "fileId": "file_001",
    "submissionId": 12345,
    "fileName": "申报书.pdf",
    "originalName": "申报书.pdf",
    "fileSize": 2048576,
    "fileType": "pdf",
    "mimeType": "application/pdf",
    "filePath": "/uploads/proposals/file_001.pdf",
    "fileUrl": "/uploads/proposals/file_001.pdf",
    "fileCategory": "proposal",
    "fileDescription": "项目申报书文件",
    "fileMd5": "d41d8cd98f00b204e9800998ecf8427e",
    "storageStatus": "completed",
    "uploaderId": "user_123",
    "uploaderName": "李四",
    "uploadTime": "2026-01-24T10:30:00",
    "createTime": "2026-01-24T10:30:00",
    "delFlag": "0"
  }
}
```

### 3. 根据提交物ID获取文件列表

**接口地址**: `GET /api/v1/process-system/submissions/{submissionId}/files`

**接口描述**: 获取指定提交物的所有文件

**路径参数**:
- `submissionId`: 提交物ID（必填）

**请求参数**:
- `fileCategory` (可选): 文件分类 (proposal/attachment)

**请求示例**:
```http
GET /api/v1/process-system/submissions/12345/files?fileCategory=proposal
Authorization: Bearer {api_key}
X-Signature: {request_signature}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "fileId": "file_001",
      "submissionId": 12345,
      "fileName": "申报书.pdf",
      "fileSize": 2048576,
      "fileType": "pdf",
      "fileCategory": "proposal",
      "fileUrl": "/uploads/proposals/file_001.pdf",
      "storageStatus": "completed",
      "uploadTime": "2026-01-24T10:30:00"
    }
  ]
}
```

### 4. 获取文件下载链接

**接口地址**: `GET /api/v1/process-system/files/{fileId}/download-url`

**接口描述**: 获取文件的临时下载链接

**路径参数**:
- `fileId`: 文件ID（必填）

**请求示例**:
```http
GET /api/v1/process-system/files/file_001/download-url
Authorization: Bearer {api_key}
X-Signature: {request_signature}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "获取下载链接成功",
  "data": {
    "file_id": "file_001",
    "file_name": "申报书.pdf",
    "download_url": "http://localhost:8080/uploads/proposals/file_001.pdf",
    "expires_in": 3600
  }
}
```

### 5. 批量获取文件下载链接

**接口地址**: `POST /api/v1/process-system/files/batch-download-urls`

**接口描述**: 批量获取多个文件的下载链接

**请求体**:
```json
["file_001", "file_002", "file_003"]
```

**请求示例**:
```http
POST /api/v1/process-system/files/batch-download-urls
Authorization: Bearer {api_key}
X-Signature: {request_signature}
Content-Type: application/json

["file_001", "file_002", "file_003"]
```

**响应示例**:
```json
{
  "code": 200,
  "message": "批量获取下载链接成功",
  "data": {
    "total": 3,
    "download_urls": {
      "file_001": "http://localhost:8080/uploads/proposals/file_001.pdf",
      "file_002": "http://localhost:8080/uploads/attachments/file_002.docx",
      "file_003": "http://localhost:8080/uploads/attachments/file_003.xlsx"
    },
    "expires_in": 3600
  }
}
```

## 错误响应

### 常见错误码

| 错误码 | HTTP状态码 | 描述 | 处理方式 |
|--------|------------|------|----------|
| 10001 | 400 | 请求参数无效 | 检查请求格式和必填字段 |
| 10002 | 401 | API密钥无效 | 检查API密钥是否正确 |
| 10003 | 401 | 签名验证失败 | 检查签名算法和密钥 |
| 404 | 404 | 文件不存在或无权限访问 | 检查文件ID是否正确 |

### 错误响应格式

```json
{
  "code": 404,
  "message": "文件不存在或无权限访问",
  "request_id": "req_123456789",
  "timestamp": "2026-01-24T10:30:00Z"
}
```

## 使用说明

### 认证方式

所有API接口都需要进行API密钥认证和签名验证：

1. **API密钥**: 在请求头中添加 `Authorization: Bearer {api_key}`
2. **请求签名**: 在请求头中添加 `X-Signature: {request_signature}`

### 文件分类说明

- `proposal`: 申报书文件
- `attachment`: 其他附件文件

### 存储状态说明

- `uploaded`: 已上传
- `processing`: 处理中
- `completed`: 已完成
- `failed`: 处理失败

### 批量操作限制

- 批量获取下载链接：单次最多100个文件
- 下载链接有效期：1小时（3600秒）

## 实现要求

根据需求文档 Requirements 10.1 和 10.3：

1. **文件列表查询接口**: 支持按提交物ID、文件分类、文件名称、文件类型等条件查询
2. **文件元数据查询接口**: 返回完整的文件元数据信息，包括文件基本信息、存储信息、上传信息等
3. **错误处理**: 当文件不存在或无权限访问时，返回相应的错误信息和状态码
4. **日志记录**: 所有查询操作都会记录到系统日志中，便于审计和问题排查

## 测试验证

实现包含了完整的单元测试，验证：

1. 数据结构的正确性
2. 参数验证逻辑
3. 响应格式的完整性
4. 错误处理机制
5. 批量操作的限制和逻辑

测试文件位置：
- `ProcessSystemServiceTest.java`: 服务层测试
- `ProcessSystemFileQueryTest.java`: 控制器层测试