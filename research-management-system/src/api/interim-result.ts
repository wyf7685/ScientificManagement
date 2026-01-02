import request from '@/utils/request'
import type { ApiResponse } from './types'
import type { InterimResult, InterimResultStats } from '@/types'

/**
 * 获取中期成果物统计
 */
export function getInterimResultStats(): Promise<ApiResponse<InterimResultStats>> {
  return request({
    url: '/interim-results/stats',
    method: 'get'
  })
}

/**
 * 获取中期成果物列表
 */
export function getInterimResults(params?: {
  projectId?: string
  type?: string
  year?: string
  keyword?: string
  page?: number
  pageSize?: number
}): Promise<ApiResponse<{
  list: InterimResult[]
  total: number
  page: number
  pageSize: number
}>> {
  return request({
    url: '/interim-results',
    method: 'get',
    params
  })
}

/**
 * 获取中期成果物详情
 */
export function getInterimResultDetail(id: string): Promise<ApiResponse<InterimResult>> {
  return request({
    url: `/interim-results/${id}`,
    method: 'get'
  })
}

/**
 * 手动同步中期成果物
 */
export function syncInterimResults(projectId?: string): Promise<ApiResponse<{
  syncCount: number
  syncTime: string
}>> {
  return request({
    url: '/interim-results/sync',
    method: 'post',
    data: { projectId }
  })
}

/**
 * 下载附件
 */
export function getAttachmentDownloadUrl(attachmentId: string): string {
  return `${import.meta.env.VITE_API_BASE_URL}/interim-results/attachments/${attachmentId}/download`
}

/**
 * 批量下载（打包）
 */
export function batchDownload(resultIds: string[]): Promise<Blob> {
  return request({
    url: '/interim-results/batch-download',
    method: 'post',
    data: { resultIds },
    responseType: 'blob'
  })
}

/**
 * 导出列表
 */
export function exportInterimResults(params?: {
  projectId?: string
  type?: string
  year?: string
}): Promise<Blob> {
  return request({
    url: '/interim-results/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}



