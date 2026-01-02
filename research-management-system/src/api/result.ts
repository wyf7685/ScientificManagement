import request from '@/utils/request'
import type {
  ApiResponse,
  StrapiPaginatedResponse,
  StrapiSingleResponse,
  QueryParams,
  StatisticsData,
  KeywordCloudData
} from './types'

function normalizePageResult(res: any, mapper?: (item: any) => any): StrapiPaginatedResponse<any> {
  const page = (res && res.data) || {}
  const list = Array.isArray(page.records) ? page.records : []
  const mapped = mapper ? list.map(mapper) : list
  return {
    data: {
      list: mapped,
      total: page.total ?? mapped.length,
      page: page.current ?? 1,
      pageSize: page.size ?? mapped.length
    }
  }
}

function mapStatus(status?: string) {
  if (!status) return status
  const normalized = status.toString().toUpperCase()
  const map: Record<string, string> = {
    PENDING: 'pending',
    UNDER_REVIEW: 'reviewing',
    APPROVED: 'published',
    REJECTED: 'rejected',
    NEEDS_MODIFICATION: 'revision'
  }
  return map[normalized] || status
}

function mapStatusToBackend(status?: string) {
  if (!status) return status
  const normalized = status.toString().toLowerCase()
  const map: Record<string, string> = {
    pending: 'PENDING',
    reviewing: 'UNDER_REVIEW',
    published: 'APPROVED',
    rejected: 'REJECTED',
    revision: 'NEEDS_MODIFICATION'
  }
  return map[normalized] || status.toString().toUpperCase()
}

function mapListItem(item: any) {
  return {
    ...item,
    id: item.documentId || item.id,
    status: mapStatus(item.auditStatus || item.status),
    type: item.typeName || item.type,
    authors: item.authorName ? [item.authorName] : item.creatorName ? [item.creatorName] : [],
    visibility: item.visibilityRange || item.visibility
  }
}

function mapDetailItem(item: any) {
  const fields = Array.isArray(item?.fields) ? item.fields : []
  const metadata: Record<string, any> = {}
  fields.forEach((field) => {
    if (field?.fieldCode) {
      metadata[field.fieldCode] = field.value
    }
  })
  return {
    ...item,
    id: item.documentId || item.id,
    status: mapStatus(item.auditStatus || item.status),
    type: item.typeName || item.type,
    typeId: item.typeDocId || item.typeId,
    typeName: item.typeName,
    typeCode: item.typeCode,
    abstract: item.summary || item.abstract,
    authors: item.authorName ? [item.authorName] : item.creatorName ? [item.creatorName] : [],
    metadata,
    attachments: [],
    visibility: item.visibilityRange || item.visibility || 'internal_abstract'
  }
}

function buildAchListPayload(params?: QueryParams) {
  const rawStatus = Array.isArray(params?.status) ? params?.status?.[0] : params?.status
  const status = mapStatusToBackend(rawStatus)
  return {
    pageNum: params?.page,
    pageSize: params?.pageSize,
    mainTitle: params?.keyword,
    typeId: params?.typeId ?? params?.type,
    status,
    projectId: params?.projectId
  }
}

// 获取统计数据
export function getStatistics(): Promise<ApiResponse<StatisticsData>> {
  return request({
    url: '/results/statistics',
    method: 'get'
  })
}

// 成果分布（类型饼图 - 后端实现）
export function getTypePie(creatorId?: number): Promise<ApiResponse<any>> {
  return request({
    url: '/admin/stat/typePie',
    method: 'get',
    params: creatorId ? { creatorId } : undefined,
    mock: false
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

// 获取成果列表（管理员口径）
export async function getResults(params?: QueryParams): Promise<StrapiPaginatedResponse<any>> {
  const res = await request({
    url: '/admin/achievement/pageList',
    method: 'post',
    data: buildAchListPayload(params),
    mock: false
  })
  return normalizePageResult(res, mapListItem)
}

// 获取成果列表（用户可见范围）
export async function getVisibleResults(params?: QueryParams): Promise<StrapiPaginatedResponse<any>> {
  const res = await request({
    url: '/user/achievement/pageListAllVisible',
    method: 'post',
    data: buildAchListPayload(params),
    mock: false
  })
  return normalizePageResult(res, mapListItem)
}

// 导出成果列表
export function exportResults(params?: QueryParams): Promise<Blob> {
  if (import.meta.env.VITE_USE_MOCK === 'true') {
    const content = 'Mock export is not implemented yet.\n'
    return Promise.resolve(new Blob([content], { type: 'text/plain;charset=utf-8' }))
  }
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
    url: '/user/achievement/pageList',
    method: 'post',
    data: buildAchListPayload(params),
    mock: false
  })
  return normalizePageResult(res, mapListItem)
}

// 获取成果详情
export async function getResult(id: string): Promise<StrapiSingleResponse<any>> {
  const res = await request({
    url: '/user/achievement/detail',
    method: 'get',
    params: { achDocId: id },
    mock: false
  })
  return { data: mapDetailItem(res?.data || {}) }
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
  return {
    data: {
      list: res?.data?.list || [],
      total: res?.data?.total || 0,
      page: res?.data?.page || params?.page || 1,
      pageSize: res?.data?.pageSize || params?.pageSize || 10
    }
  }
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
    url: '/user/achievement/create',
    method: 'post',
    data,
    mock: false
  })
}

// 更新成果
export function updateResult(id: string, data: Record<string, any>): Promise<ApiResponse<any>> {
  return request({
    url: `/user/achievement/update/${id}`,
    method: 'put',
    data,
    mock: false
  })
}

// 删除成果
export function deleteResult(id: string): Promise<ApiResponse<any>> {
  return request({
    url: `/admin/achievement/delete/${id}`,
    method: 'put',
    mock: false
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
  const isJournalRank = params?.type === 'journalRank'
  return request({
    url: '/results/auto-fill',
    method: 'get',
    params,
    mock: !isJournalRank
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
    url: '/achievementType/list',
    method: 'post',
    mock: false
  }).then((res: any) => {
    const list = Array.isArray(res?.data) ? res.data : []
    const normalized = list.map((item: any) => ({
      ...item,
      type_name: item.type_name ?? item.typeName,
      type_code: item.type_code ?? item.typeCode,
      is_delete: item.is_delete ?? item.isDelete ?? 0
    }))
    return { data: normalized }
  })
}

// 创建成果类型
export function createResultType(data: Partial<AchievementType>): Promise<any> {
  return request({
    url: '/achievementType/types',
    method: 'post',
    mock: false,
    data: { data }
  })
}

// 更新成果类型
export function updateResultType(documentId: string, data: Partial<AchievementType>): Promise<any> {
  return request({
    url: `/achievementType/types/${documentId}`,
    method: 'put',
    mock: false,
    data: { data }
  })
}

// 删除成果类型（逻辑删除）
export function deleteResultType(documentId: string): Promise<any> {
  return request({
    url: `/achievementType/types/${documentId}/delete`,
    method: 'put',
    mock: false
  })
}

// ==================== 动态字段API ====================

// 获取指定成果类型的所有字段定义
export function getFieldDefsByType(typeDocumentId: string): Promise<any> {
  return request({
    url: '/achievementType/detail',
    method: 'get',
    params: { typeDocId: typeDocumentId },
    mock: false
  }).then((res: any) => {
    const fields = res?.data?.fieldDefinitions || []
    const normalized = fields.map((field: any) => ({
      documentId: field.documentId,
      field_code: field.fieldCode,
      field_name: field.fieldName,
      field_type: field.fieldType,
      description: field.description,
      is_required: field.isRequired ?? 0
    }))
    return { data: normalized }
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
    url: '/achievementFieldDef/create',
    method: 'post',
    mock: false,
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
    url: `/achievementFieldDef/defs/${documentId}`,
    method: 'put',
    mock: false,
    data: { data: payload }
  })
}

// 删除字段定义（逻辑删除）
export function deleteFieldDef(documentId: string): Promise<any> {
  return request({
    url: `/achievementFieldDef/delete/${documentId}`,
    method: 'put',
    mock: false
  })
}
