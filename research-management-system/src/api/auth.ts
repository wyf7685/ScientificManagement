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

// 登录（标记 skipAuth：不带旧 token，也不触发全局 401 跳转）
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
  }) as Promise<ApiResponse<Record<string, any>>>
}
