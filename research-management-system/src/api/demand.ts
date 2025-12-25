import request from '@/utils/request'
import type { ApiResponse, QueryParams } from './types'

// 获取需求列表
export function getDemands(params?: QueryParams): Promise<ApiResponse<any>> {
  return request({
    url: '/demand',
    method: 'get',
    params
  })
}

// 获取需求详情
export function getDemandDetail(id: string): Promise<ApiResponse<any>> {
  return request({
    url: `/demand/${id}`,
    method: 'get'
  })
}

// 重新匹配需求
export function rematchDemand(id: string): Promise<ApiResponse<any>> {
  return request({
    url: `/demand/${id}/rematch`,
    method: 'post'
  })
}
