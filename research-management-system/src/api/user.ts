import request from '@/utils/request'
import type { ApiResponse } from './types'

/**
 * Keycloak 用户对象
 */
export interface KeycloakUser {
  /** 用户ID */
  id: number;
  /** Keycloak 用户 UUID */
  uuid: string;
  /** 用户名 */
  username: string;
  /** 邮箱 */
  email: string;
  /** 姓名 */
  name: string;
  /** 角色列表 */
  roles: string[];
  /** 用户是否启用 */
  enabled: boolean;
}

/**
 * 获取专家用户列表（用于审核人选择）
 */
export function getExpertUsers(): Promise<ApiResponse<KeycloakUser[]>> {
  return request({
    url: '/users/experts',
    method: 'get',
  });
}

/**
 * 获取所有业务用户列表
 */
export function getAllUsers(params?: any): Promise<ApiResponse<KeycloakUser[]>> {
  return request({
    url: '/users',
    method: 'get',
    params,
  });
}
