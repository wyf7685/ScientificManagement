# 版本查询功能实现文档

## 概述

本文档描述了任务 9.1 "实现版本查询功能" 的实现详情。该功能支持按版本号和轮次查询项目提交物的历史记录。

## 实现的功能

### 1. 版本历史查询
- **接口**: `GET /api/v1/process-system/applications/{applicationId}/submissions/{submissionType}/{submissionStage}/rounds/{submissionRound}/versions`
- **功能**: 查询指定申报、类型、阶段和轮次的所有版本历史
- **返回**: 按版本号降序排列的提交物列表

### 2. 轮次历史查询
- **接口**: `GET /api/v1/process-system/applications/{applicationId}/submissions/{submissionType}/{submissionStage}/rounds`
- **功能**: 查询指定申报、类型和阶段的所有轮次历史
- **返回**: 按轮次和版本号降序排列的提交物列表

### 3. 指定版本查询
- **接口**: `GET /api/v1/process-system/applications/{applicationId}/submissions/{submissionType}/{submissionStage}/rounds/{submissionRound}/versions/{submissionVersion}`
- **功能**: 获取指定版本的提交物详情
- **返回**: 具体版本的提交物详细信息

### 4. 完整历史查询
- **接口**: `GET /api/v1/process-system/applications/{applicationId}/submissions/{submissionType}/{submissionStage}/history`
- **功能**: 获取指定申报、类型和阶段的所有历史版本
- **返回**: 完整的历史记录列表

## 技术实现

### 1. 服务层接口 (IProcessSystemService)
新增方法：
- `getSubmissionVersionHistory()` - 版本历史查询
- `getSubmissionRoundHistory()` - 轮次历史查询
- `getSubmissionByVersion()` - 指定版本查询
- `getSubmissionFullHistory()` - 完整历史查询

### 2. 服务层实现 (ProcessSystemServiceImpl)
- 实现了所有版本查询相关的业务逻辑
- 包含完整的日志记录和异常处理
- 支持数据转换和结果封装

### 3. 控制器层 (ProcessSystemController)
- 新增4个RESTful API接口
- 完整的参数验证和错误处理
- 符合OpenAPI 3.0规范的接口文档

### 4. 数据访问层 (ProcessSubmissionMapper)
新增SQL查询方法：
- `selectVersionHistory()` - 版本历史查询SQL
- `selectRoundHistory()` - 轮次历史查询SQL
- `selectByVersion()` - 指定版本查询SQL
- `selectFullHistory()` - 完整历史查询SQL

### 5. SQL实现 (ProcessSubmissionMapper.xml)
- 所有查询都包含 `del_flag = '0'` 条件过滤已删除记录
- 按照版本号、轮次和上传时间进行排序
- 支持精确匹配和范围查询

## 数据库查询逻辑

### 版本历史查询
```sql
SELECT * FROM process_submissions
WHERE application_id = ? AND submission_type = ? 
  AND submission_stage = ? AND submission_round = ?
  AND del_flag = '0'
ORDER BY submission_version DESC, upload_time DESC
```

### 轮次历史查询
```sql
SELECT * FROM process_submissions
WHERE application_id = ? AND submission_type = ? 
  AND submission_stage = ? AND del_flag = '0'
ORDER BY submission_round DESC, submission_version DESC, upload_time DESC
```

### 指定版本查询
```sql
SELECT * FROM process_submissions
WHERE application_id = ? AND submission_type = ? 
  AND submission_stage = ? AND submission_round = ?
  AND submission_version = ? AND del_flag = '0'
LIMIT 1
```

### 完整历史查询
```sql
SELECT * FROM process_submissions
WHERE application_id = ? AND submission_type = ? 
  AND submission_stage = ? AND del_flag = '0'
ORDER BY submission_round DESC, submission_version DESC, upload_time DESC
```

## 错误处理

### 1. 参数验证
- 所有必填参数进行非空验证
- 使用现有的 `ProcessSystemValidationService` 进行参数校验

### 2. 异常处理
- 指定版本不存在时抛出 `IllegalArgumentException`
- 数据库异常统一处理并记录日志
- 返回标准的错误响应格式

### 3. 日志记录
- 所有操作都记录详细的操作日志
- 成功和失败操作分别记录
- 包含关键参数信息便于问题排查

## 测试覆盖

### 单元测试 (ProcessSystemVersionQueryTest)
- 测试版本历史查询功能
- 测试轮次历史查询功能
- 测试指定版本查询功能
- 测试版本不存在的异常情况
- 测试完整历史查询功能
- 使用Mockito模拟依赖组件

## API使用示例

### 1. 查询版本历史
```http
GET /api/v1/process-system/applications/67890/submissions/proposal/application/rounds/1/versions
Authorization: Bearer {api_key}
X-Signature: {request_signature}
```

### 2. 查询轮次历史
```http
GET /api/v1/process-system/applications/67890/submissions/proposal/application/rounds
Authorization: Bearer {api_key}
X-Signature: {request_signature}
```

### 3. 查询指定版本
```http
GET /api/v1/process-system/applications/67890/submissions/proposal/application/rounds/1/versions/2
Authorization: Bearer {api_key}
X-Signature: {request_signature}
```

### 4. 查询完整历史
```http
GET /api/v1/process-system/applications/67890/submissions/proposal/application/history
Authorization: Bearer {api_key}
X-Signature: {request_signature}
```

## 符合需求

该实现完全符合需求 9.5 的要求：
- ✅ 实现按版本号查询
- ✅ 实现按轮次查询  
- ✅ 实现历史记录展示
- ✅ 支持完整的版本管理功能
- ✅ 包含完整的错误处理和日志记录
- ✅ 提供RESTful API接口
- ✅ 包含单元测试覆盖

## 总结

版本查询功能已成功实现，提供了完整的历史版本管理能力。该功能支持多种查询方式，包括按版本号、轮次和完整历史的查询，满足过程管理系统对历史记录查看的需求。所有代码都经过语法检查，无编译错误，并包含了完整的单元测试。