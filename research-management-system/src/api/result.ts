import request from '@/utils/request'
import { normalizeStrapiList, normalizeStrapiSingle, normalizeStrapiMedia } from '@/utils/strapi'
import type { 
  ApiResponse, 
  StrapiPaginatedResponse, 
  StrapiSingleResponse,
  QueryParams,
  StatisticsData,
  KeywordCloudData
} from './types'

// 获取统计数据
export function getStatistics(): Promise<ApiResponse<StatisticsData>> {
  return request({
    url: '/results/statistics',
    method: 'get'
  })
}

// 获取高级分布数据
export function getAdvancedDistribution(params?: QueryParams): Promise<ApiResponse<any>> {
  return request({
    url: '/results/advanced-distribution',
    method: 'get',
    params
  })
}

// 获取堆叠趋势数据
export function getStackedTrend(params?: QueryParams): Promise<ApiResponse<any>> {
  return request({
    url: '/results/stacked-trend',
    method: 'get',
    params
  })
}

// 获取热点关键词图谱
export function getKeywordCloud(params?: QueryParams): Promise<ApiResponse<KeywordCloudData>> {
  return request({
    url: '/results/keywords',
    method: 'get',
    params
  })
}

// 获取个人统计数据
export function getMyStatistics(): Promise<ApiResponse<StatisticsData>> {
  return request({
    url: '/results/my-statistics',
    method: 'get'
  })
}

// 获取成果列表
export async function getResults(params?: QueryParams): Promise<StrapiPaginatedResponse<any>> {
  const res = await request({
    url: '/results',
    method: 'get',
    params
  })
  return normalizeStrapiList(res, mapResultEntity, {
    page: params?.page,
    pageSize: params?.pageSize
  })
}

// 导出成果列表
export function exportResults(params?: QueryParams): Promise<Blob> {
  return request({
    url: '/results/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 获取我的成果列表
export async function getMyResults(params?: QueryParams): Promise<StrapiPaginatedResponse<any>> {
  const res = await request({
    url: '/results/my',
    method: 'get',
    params
  })
  return normalizeStrapiList(res, mapResultEntity, {
    page: params?.page,
    pageSize: params?.pageSize
  })
}

// 获取成果详情
export async function getResult(id: string): Promise<StrapiSingleResponse<any>> {
  const res = await request({
    url: `/results/${id}`,
    method: 'get'
  })
  return normalizeStrapiSingle(res, mapResultEntity)
}

// 申请查看成果全文
export function requestResultAccess(id: string, data: Record<string, any>): Promise<ApiResponse<any>> {
  return request({
    url: `/results/${id}/access-requests`,
    method: 'post',
    data
  })
}

// 获取成果访问申请列表
export async function getResultAccessRequests(params?: QueryParams): Promise<StrapiPaginatedResponse<any>> {
  const res = await request({
    url: '/results/access-requests',
    method: 'get',
    params
  })
  return normalizeStrapiList(res, undefined, {
    page: params?.page,
    pageSize: params?.pageSize
  })
}

// 审核成果访问申请
export function reviewResultAccessRequest(
  id: string, 
  data: { action: 'approve' | 'reject'; comment?: string }
): Promise<ApiResponse<any>> {
  return request({
    url: `/results/access-requests/${id}/review`,
    method: 'post',
    data
  })
}

// 创建成果
export function createResult(data: Record<string, any>): Promise<ApiResponse<any>> {
  return request({
    url: '/results',
    method: 'post',
    data
  })
}

// 更新成果
export function updateResult(id: string, data: Record<string, any>): Promise<ApiResponse<any>> {
  return request({
    url: `/results/${id}`,
    method: 'put',
    data
  })
}

// 删除成果
export function deleteResult(id: string): Promise<ApiResponse<any>> {
  return request({
    url: `/results/${id}`,
    method: 'delete'
  })
}

// 保存草稿
export function saveDraft(data: Record<string, any>): Promise<ApiResponse<any>> {
  return request({
    url: '/results/draft',
    method: 'post',
    data
  })
}

// 提交审核
export function submitReview(id: string): Promise<ApiResponse<any>> {
  return request({
    url: `/results/${id}/submit`,
    method: 'post'
  })
}

// 审核成果
export function reviewResult(id: string, data: Record<string, any>): Promise<ApiResponse<any>> {
  return request({
    url: `/results/${id}/review`,
    method: 'post',
    data
  })
}

// 分配审核人
export function assignReviewers(id: string, data: Record<string, any>): Promise<ApiResponse<any>> {
  return request({
    url: `/results/${id}/assign-reviewers`,
    method: 'post',
    data
  })
}

// 退回修改
export function requestChanges(id: string, data: Record<string, any>): Promise<ApiResponse<any>> {
  return request({
    url: `/results/${id}/request-changes`,
    method: 'post',
    data
  })
}

// 标记格式审查通过
export function markFormatChecked(id: string): Promise<ApiResponse<any>> {
  return request({
    url: `/results/${id}/format-check`,
    method: 'post'
  })
}

// 标记格式审查不通过
export function markFormatRejected(id: string, data: Record<string, any>): Promise<ApiResponse<any>> {
  return request({
    url: `/results/${id}/format-reject`,
    method: 'post',
    data
  })
}

// 获取审核待办列表
export function getReviewBacklog(params?: QueryParams): Promise<ApiResponse<any>> {
  return request({
    url: '/results/review-backlog',
    method: 'get',
    params
  })
}

// 智能补全元数据（通过DOI等）
export function autoFillMetadata(params?: QueryParams): Promise<ApiResponse<any>> {
  return request({
    url: '/results/auto-fill',
    method: 'get',
    params
  })
}

// 上传附件
export function uploadAttachment(file: File): Promise<ApiResponse<any>> {
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

// ==================== 类型定义 ====================

// 成果类型
export interface AchievementType {
 id?: number
 documentId?: string
 type_code: string
 type_name: string
 description?: string
 is_delete?: number
}

// 成果字段定义
export interface AchievementFieldDef {
  id?: number
  documentId?: string
  field_code: string
  field_name: string
  field_type: string // "TEXT", "NUMBER" 等
  is_required: number // 0 或 1
  description?: string
  is_delete?: number
  achievement_type?: string | number | object 
}

// ==================== 成果类型API ====================

// 获取成果类型列表
export function getResultTypes(): Promise<any> {
  return request({
    url: '/achievement-types',
    skipAuth: true,
    mock: false,
    method: 'get',
    params: {
      'filters[is_delete][$ne]': 1,
      'pagination[pageSize]': 100
    }
  })
}

// 创建成果类型
export function createResultType(data: Partial<AchievementType>): Promise<any> {
  return request({
    url: '/achievement-types',
    skipAuth: true,
    mock: false,
    method: 'post',
    data: { data }
  })
}

// 更新成果类型
export function updateResultType(documentId: string, data: Partial<AchievementType>): Promise<any> {
  return request({
    url: `/achievement-types/${documentId}`,
    skipAuth: true,
    mock: false,
    method: 'put',
    data: { data }
  })
}

// 删除成果类型（逻辑删除）
export function deleteResultType(documentId: string): Promise<any> {
  return updateResultType(documentId, { is_delete: 1 })
}

// ==================== 动态字段API ====================

// 获取指定成果类型的所有字段定义
export function getFieldDefsByType(typeDocumentId: string): Promise<any> {
  return request({
    url: '/achievement-field-defs',
    skipAuth: true,
    mock: false,
    method: 'get',
    params: {
      'filters[achievement_type_id][documentId][$eq]': typeDocumentId, 
      'filters[is_delete][$ne]': 1,
      'pagination[pageSize]': 100
    }
  })
}

// 创建字段定义
export function createFieldDef(data: AchievementFieldDef): Promise<any> {
  const payload = {
    ...data,
    achievement_type_id: data.achievement_type,
    achievement_type: undefined
  } as Partial<AchievementFieldDef>
    
  return request({
    url: '/achievement-field-defs',
    skipAuth: true,
    mock: false,
    method: 'post',
    data: { data: payload }
  })
}

// 更新字段定义
export function updateFieldDef(documentId: string, data: Partial<AchievementFieldDef>): Promise<any> {
  const payload = {
    ...data,
    achievement_type_id: data.achievement_type,
    achievement_type: undefined
  } as Partial<AchievementFieldDef>
    
  return request({
    url: `/achievement-field-defs/${documentId}`,
    skipAuth: true,
    mock: false,
    method: 'put',
    data: { data: payload }
  })
}

// 删除字段定义（逻辑删除）
export function deleteFieldDef(documentId: string): Promise<any> {
  return updateFieldDef(documentId, { is_delete: 1 })
}

// ==================== 辅助方法 ====================

// 映射成果实体数据
function mapResultEntity(entity: any) {
  const projectData = entity.project?.data || entity.project
  const projectAttrs = projectData?.attributes || projectData

  return {
    ...entity,
    authors: normalizeToArray(entity.authors),
    keywords: normalizeToArray(entity.keywords || entity.tags),
    projectId: entity.projectId || projectData?.id,
    projectName: entity.projectName || projectAttrs?.name,
    projectCode: entity.projectCode || projectAttrs?.code,
    attachments: normalizeStrapiMedia(entity.attachments)
  }
}

function normalizeToArray(value: any) {
  if (Array.isArray(value)) return value
  if (typeof value === 'string') {
    return value
      .split(/[,;；、\s]+/)
      .map((item) => item.trim())
      .filter(Boolean)
  }
  return []
}
