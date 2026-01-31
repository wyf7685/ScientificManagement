import request from '@/utils/request'

export type ApiResponse<T> = {
  code: number
  message: string
  data: T
}

export type LoginPayload = {
  username: string
  password: string
}

export type LoginData = {
  token: string
  user: Record<string, any>
}

/**
 * 用户简要信息
 * 来自 Keycloak JWT Token
 */
export interface UserProfile {
  /** 用户ID */
  id: number;
  /** 用户UUID */
  uuid: string;
  /** 用户名 */
  username: string;
  /** 真实姓名 */
  name: string;
  /** 邮箱 */
  email: string;
  /** 角色列表 */
  roles: string[];
}


// 登录（标记 skipAuth：不带旧 token，也不触发全局 401 跳转）
// TODO: 移除前端登录API&界面，使用来自 login-portal 的单点登录
export function login(data: LoginPayload) {
  return request({
    url: '/auth/login',
    method: 'post',
    data,
    skipAuth: true
  }) as Promise<ApiResponse<LoginData>>
}

// 登出
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  }) as Promise<ApiResponse<true>>
}

// 获取当前用户信息
export function getCurrentUser() {
  return request({
    url: '/auth/current',
    method: 'get'
  }) as Promise<ApiResponse<UserProfile>>
}
