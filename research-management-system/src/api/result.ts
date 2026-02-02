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

const strapiAssetBase = (import.meta.env.VITE_STRAPI_BASE_URL || '').toString()

function resolveAssetUrl(url?: string) {
  if (!url) return ''
  if (/^https?:\/\//i.test(url)) return url
  if (!strapiAssetBase) return url
  const base = strapiAssetBase.replace(/\/$/, '')
  const path = url.startsWith('/') ? url : `/${url}`
  return `${base}${path}`
}

function mapListItem(item: any) {
  return {
    ...item,
    id: item.documentId || item.id,
    status: mapStatus(item.auditStatus || item.status),
    type: item.typeName || item.type,

    // ZZQ改 : 关键：优先使用后端的 authors 数组
    authors: Array.isArray(item.authors)
      ? item.authors
      : (item.authorName ? [item.authorName] : item.creatorName ? [item.creatorName] : []),

    visibility: item.visibilityRange || item.visibility,
    createdBy: item.createdBy || item.creatorName
  }
  // ZZQ改
}


function mapReviewHistoryItem(item: any) {
  return {
    ...item,
    id: item.documentId || item.id,
    type: item.typeName || item.type,
    projectName: item.projectName,
    projectCode: item.projectCode,
    reviewResult: item.reviewResult || item.action,
    reviewedAt: item.reviewedAt
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

  // 解析附件: 后端返回格式 {data:[{id, files:[{name,url,size,mime}]}]}
  console.log('[DEBUG] 原始 item.attachments:', JSON.stringify(item?.attachments, null, 2))
  let attachments = []
  if (item?.attachments?.data) {
    // 兼容 Strapi v4 两种可能的返回格式：
    // 1. data: [ { attributes: { files: { data: [...] } } } ]
    // 2. data: [ { files: [...] } ] (如果后端做了扁平化)
    attachments = Array.isArray(item.attachments.data)
      ? item.attachments.data.flatMap((fileRecord: any) => {
        const attributes = fileRecord.attributes || fileRecord
        // 尝试获取 files 数组，兼容 files.data 结构
        const rawFiles = attributes.files?.data || attributes.files || []

        return Array.isArray(rawFiles)
          ? rawFiles.map((f: any) => {
            const file = f.attributes || f
            return {
              id: file.id || f.id,
              name: file.name || 'unknown',
              ext: file.ext || (file.name ? '.' + file.name.split('.').pop() : ''), // 确保有扩展名
              url: resolveAssetUrl(file.url || ''),
              size: file.size || 0,
              mime: file.mime || ''
            }
          })
          : []
      })
      : []
  }
  console.log('[DEBUG] 解析后的 attachments:', attachments)



  return {
    ...item,
    id: item.documentId || item.id,
    status: mapStatus(item.auditStatus || item.status),
    type: item.typeName || item.type,
    typeId: item.typeDocId || item.typeId,
    typeName: item.typeName,
    typeCode: item.typeCode,
    abstract: item.summary || item.abstract,
    authors: Array.isArray(item.authors) ? item.authors : item.authorName ? [item.authorName] : item.creatorName ? [item.creatorName] : [],
    keywords: Array.isArray(item.keywords) ? item.keywords : [],
    year: item.year,
    projectCode: item.projectCode,
    projectName: item.projectName,
    metadata,
    attachments,
    visibility: item.visibilityRange || item.visibility || 'private'
  }
}

function buildAchListPayload(params?: QueryParams, useTypeCode = false, onlyUnassigned = false) {
  const rawStatus = Array.isArray(params?.status) ? params?.status?.[0] : params?.status
  const status = mapStatusToBackend(rawStatus)
  const payload: any = {
    pageNum: params?.page,
    pageSize: params?.pageSize,
    // 成果名称
    title: params?.title,
    // 关键词
    keyword: params?.keyword,
    status,
    projectId: params?.projectId,
    onlyUnassigned,
    // ✅ 新增：作者
    author: (params as any)?.author
  }

  // ZZQ改 ：✅ 追加：年份范围（页面传的是 yearRange: [start,end]）
  const yr: any = (params as any)?.yearRange
  if (Array.isArray(yr) && yr.length === 2) {
    payload.yearStart = String(yr[0] ?? '')
    payload.yearEnd = String(yr[1] ?? '')
  }
  // ZZQ改

  // 根据useTypeCode参数决定使用typeCode还是typeId
  if (useTypeCode) {
    payload.typeCode = params?.typeId ?? params?.type
  } else {
    payload.typeId = params?.typeId ?? params?.type
  }

  return payload
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
    params: creatorId ? { creatorId } : undefined
  })
}
export function getTypePie4User(creatorId?: number): Promise<ApiResponse<any>> {
  return request({
    url: '/user/stat/typePie',
    method: 'get',
    params: creatorId ? { creatorId } : undefined
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
export async function getResults(params?: QueryParams, useTypeCode = false): Promise<StrapiPaginatedResponse<any>> {
  const res = await request({
    url: '/admin/achievement/pageList',
    method: 'post',
    data: buildAchListPayload(params, useTypeCode)
  })
  return normalizePageResult(res, mapListItem)
}

// 获取成果列表（用户可见范围）
export async function getVisibleResults(params?: QueryParams): Promise<StrapiPaginatedResponse<any>> {
  const res = await request({
    url: '/user/achievement/pageListAllVisible',
    method: 'post',
    data: buildAchListPayload(params, true) // 使用typeCode
  })
  return normalizePageResult(res, mapListItem)
}
// 获取成果列表（管理员口径，包含所有可见成果）
export async function getVisibleResults4Admin(params?: QueryParams): Promise<StrapiPaginatedResponse<any>> {
  const res = await request({
    url: '/admin/achievement/pageListAllVisible',
    method: 'post',
    data: buildAchListPayload(params, true) // 使用typeCode
  })
  return normalizePageResult(res, mapListItem)
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

// 获取我的成果列表（也可用于用户端通用检索）
export async function getMyResults(params?: QueryParams, useTypeCode = false): Promise<StrapiPaginatedResponse<any>> {
  const res = await request({
    url: '/user/achievement/pageList',
    method: 'post',
    data: buildAchListPayload(params, useTypeCode)
  })
  return normalizePageResult(res, mapListItem)
}

// 获取成果详情
export async function getResult(id: string): Promise<StrapiSingleResponse<any>> {
  const res = await request({
    url: '/user/achievement/detail', // ✅ 详情页统一走这个
    method: 'get',
    params: { achDocId: id }
  })
  return { data: mapDetailItem(res?.data || {}) }
}

export async function selectResults(params?: QueryParams, useTypeCode = false): Promise<StrapiPaginatedResponse<any>> {
  const res = await request({
    url: '/user/achievement/pageList',
    method: 'post',
    data: buildAchListPayload(params, useTypeCode)
  })
  return normalizePageResult(res, mapListItem)
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
  return normalizePageResult(res)
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
    data
  })
}

// 创建成果（带文件）
export function createResultWithFiles(data: Record<string, any>, files: File[]): Promise<ApiResponse<any>> {
  const formData = new FormData()
  formData.append('data', JSON.stringify(data))
  files.forEach(file => {
    formData.append('files', file)
  })
  return request({
    url: '/user/achievement/createWithFiles',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 更新成果
export function updateResult(id: string, data: Record<string, any>): Promise<ApiResponse<any>> {
  return request({
    url: `/user/achievement/update/${id}`,
    method: 'put',
    data
  })
}

// 更新成果（带文件）
export function updateResultWithFiles(id: string, data: Record<string, any>, files: File[]): Promise<ApiResponse<any>> {
  const formData = new FormData()
  formData.append('data', JSON.stringify(data))
  files.forEach(file => {
    formData.append('files', file)
  })
  return request({
    url: `/user/achievement/updateWithFiles/${id}`,
    method: 'put',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除成果
export function deleteResult(id: string): Promise<ApiResponse<any>> {
  return request({
    url: `/admin/achievement/delete/${id}`,
    method: 'put'
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
export async function getReviewBacklog(params?: QueryParams): Promise<StrapiPaginatedResponse<any>> {
  const res = await request({
    url: '/results/review-backlog',
    method: 'get',
    params
  })
  return normalizePageResult(res, mapListItem)
}

// 获取审核历史列表
export async function getReviewHistory(params?: QueryParams): Promise<StrapiPaginatedResponse<any>> {
  const res = await request({
    url: '/results/review-history',
    method: 'get',
    params
  })
  return normalizePageResult(res, mapReviewHistoryItem)
}

// 获取待分配审核的成果列表
export async function getPendingAssignResults(params?: QueryParams): Promise<StrapiPaginatedResponse<any>> {
  const res = await request({
    url: '/admin/achievement/pageList',
    method: 'post',
    data: buildAchListPayload({ ...params, status: 'pending' }, false, true) // 使用onlyUnassigned=true
  })
  return normalizePageResult(res, mapListItem)
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
    url: '/achievementType/list',
    method: 'post'
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
    data: { data }
  })
}

// 更新成果类型
export function updateResultType(documentId: string, data: Partial<AchievementType>): Promise<any> {
  return request({
    url: `/achievementType/types/${documentId}`,
    method: 'put',
    data: { data }
  })
}

// 删除成果类型（逻辑删除）
export function deleteResultType(documentId: string): Promise<any> {
  return request({
    url: `/achievementType/types/${documentId}/delete`,
    method: 'put'
  })
}

// ==================== 动态字段API ====================

// 获取指定成果类型的所有字段定义
export function getFieldDefsByType(typeDocumentId: string): Promise<any> {
  return request({
    url: '/achievementType/detail',
    method: 'get',
    params: { typeDocId: typeDocumentId },
    silent: true
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
    data: { data: payload }
  })
}

// 删除字段定义（逻辑删除）
export function deleteFieldDef(documentId: string): Promise<any> {
  return request({
    url: `/achievementFieldDef/delete/${documentId}`,
    method: 'put'
  })
}
