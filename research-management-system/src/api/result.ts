import request from '@/utils/request'

// 获取统计数据 (保持不变)
export function getStatistics() {
 return request({
  url: '/results/statistics',
  method: 'get'
 })
}

// 获取个人统计 (保持不变)
export function getMyStatistics() {
 return request({
  url: '/results/my-statistics',
  method: 'get'
 })
}

// 获取成果列表 (保持不变)
export function getResults(params: any) {
 return request({
  url: '/results',
  method: 'get',
  params
 })
}

// 获取我的成果列表 (保持不变)
export function getMyResults(params: any) {
 return request({
  url: '/results/my',
  method: 'get',
  params
 })
}

// 获取成果详情 (保持不变)
export function getResult(id: string) {
 return request({
  url: `/results/${id}`,
  method: 'get'
 })
}

// 申请查看成果全文 (保持不变)
export function requestResultAccess(id: string, data: any) {
 return request({
  url: `/results/${id}/access-requests`,
  method: 'post',
  data
 })
}

// 创建成果 (保持不变)
export function createResult(data: any) {
 return request({
  url: '/results',
  method: 'post',
  data
 })
}

// 更新成果 (保持不变)
export function updateResult(id: string, data: any) {
 return request({
  url: `/results/${id}`,
  method: 'put',
  data
 })
}

// 删除成果 (保持不变)
export function deleteResult(id: string) {
 return request({
  url: `/results/${id}`,
  method: 'delete'
 })
}

// 保存草稿 (保持不变)
export function saveDraft(data: any) {
 return request({
  url: '/results/draft',
  method: 'post',
  data
 })
}

// 提交审核 (保持不变)
export function submitReview(id: string) {
 return request({
  url: `/results/${id}/submit`,
  method: 'post'
 })
}

// 审核成果 (保持不变)
export function reviewResult(id: string, data: any) {
 return request({
  url: `/results/${id}/review`,
  method: 'post',
  data
 })
}

// 智能补全（通过 DOI 等） (保持不变)
export function autoFillMetadata(params: any) {
 return request({
  url: '/results/auto-fill',
  method: 'get',
  params
 })
}

// 上传附件 (保持不变)
export function uploadAttachment(file: File) {
 const formData = new FormData()
 formData.append('file', file)
 return request({
  url: '/upload',
  method: 'post',
  data: formData,
  headers: {
   'Content-Type': 'multipart/form-data'
  }
 })
}

// --- 1. 定义接口 (字段名调整以适应 Strapi POST/PUT 请求) ---

// 成果类型 (AchievementType)
export interface AchievementType {
 id?: number
 documentId?: string
 type_code: string
 type_name: string
 description?: string
 is_delete?: number
}

// 字段定义 (AchievementFieldDef)
export interface AchievementFieldDef {
 id?: number
 documentId?: string
 field_code: string
 field_name: string
 field_type: string // "TEXT", "NUMBER" 等
 is_required: number // 数据库存的是 0 或 1
 description?: string
 is_delete?: number
 // 核心修改：前端数据结构不变，但在 POST/PUT 时需要使用 achievement_type_id
 achievement_type?: string | number | object 
}

// --- 2. 成果类型 API (保持不变，因为这些是顶级模型) ---

export function getResultTypes() {
 return request({
  url: '/api/achievement-types',
  skipAuth: true,
  method: 'get',
  params: {
   'filters[is_delete][$ne]': 1,
   'pagination[pageSize]': 100
  }
 })
}

export function createResultType(data: Partial<AchievementType>) {
 return request({
  url: '/api/achievement-types',
  skipAuth: true,
  method: 'post',
  data: { data }
 })
}

export function updateResultType(documentId: string, data: Partial<AchievementType>) {
 return request({
  url: `/api/achievement-types/${documentId}`,
  skipAuth: true,
  method: 'put',
  data: { data }
 })
}

export function deleteResultType(documentId: string) {
 // 逻辑删除
 return updateResultType(documentId, { is_delete: 1 })
}

// --- 3. 动态字段 API (使用 _id 后缀) ---

// 获取某类型下的所有字段
export function getFieldDefsByType(typeDocumentId: string) {
 return request({
  url: '/api/achievement-field-defs',
  skipAuth: true,
  method: 'get',
  params: {
   // 【已修改】：使用确认的键名 achievement_type_id 进行过滤
   'filters[achievement_type_id][documentId][$eq]': typeDocumentId, 
   'filters[is_delete][$ne]': 1,
   'pagination[pageSize]': 100
  }
 })
}

// 新增字段
export function createFieldDef(data: AchievementFieldDef) {
 // 【关键修改】：重构请求体，将 achievement_type 字段赋值给 achievement_type_id
 const payload = {
    ...data,
    achievement_type_id: data.achievement_type, // 替换关联字段名
    achievement_type: undefined // 清理原始字段，防止冲突
  } as Partial<AchievementFieldDef>
    
 return request({
  url: '/api/achievement-field-defs',
  skipAuth: true,
  method: 'post',
  data: { data: payload }
 })
}

// 更新字段
export function updateFieldDef(documentId: string, data: Partial<AchievementFieldDef>) {
 // 【关键修改】：重构请求体，将 achievement_type 字段赋值给 achievement_type_id
  const payload = {
    ...data,
    achievement_type_id: data.achievement_type, // 替换关联字段名
    achievement_type: undefined // 清理原始字段，防止冲突
  } as Partial<AchievementFieldDef>
    
 return request({
  url: `/api/achievement-field-defs/${documentId}`,
  skipAuth: true,
  method: 'put',
  data: { data: payload }
 })
}

// 删除字段 (保持不变)
export function deleteFieldDef(documentId: string) {
 return updateFieldDef(documentId, { is_delete: 1 })
}