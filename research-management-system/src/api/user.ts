import request from '@/utils/request'
import type { ApiResponse } from './types'

/**
 * 获取专家用户列表（用于审核人选择）
 */
export function getExpertUsers(): Promise<ApiResponse<any[]>> {
  return request({
    url: '/users/experts',
    method: 'get'
  })
}

/**
 * 获取所有业务用户列表
 */
export function getBusinessUsers(params?: any): Promise<ApiResponse<any[]>> {
  return request({
    url: '/users',
    method: 'get',
    params
  })
}
