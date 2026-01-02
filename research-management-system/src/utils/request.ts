import axios, { type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { useUserStore } from '@/stores/user'
import router from '@/router'
import { handleMockRequest } from '@/mocks/handler'
import { ErrorHandler, AppError, ErrorType } from './errorHandler'

declare module 'axios' {
  export interface AxiosRequestConfig {
    skipAuth?: boolean
    mock?: boolean // 新增：允许单个请求控制是否使用 Mock
  }
}

const isMockEnabled = import.meta.env.VITE_USE_MOCK === 'true'

const service = axios.create({
 baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
 timeout: 15000,
 headers: {
  'Content-Type': 'application/json'
 }
})

// 请求拦截器
service.interceptors.request.use(
 (config) => {
  // 跳过认证头逻辑
  if (config.skipAuth) {
   return config
  }

  const userStore = useUserStore()
  const { token } = storeToRefs(userStore)
  if (token.value) {
    config.headers = {
     ...(config.headers as any),
     Authorization: `Bearer ${token.value}`
    } as any
  }
  return config
 },
 (error) => {
  console.error('请求错误:', error)
  return Promise.reject(error)
 }
)

// 响应拦截器
service.interceptors.response.use(
 (response) => handleBusinessResponse(response), // 【修改 1】：传入完整的 response 对象
 (error) => {
  console.error('响应错误:', error)

  if (error.response) {
   switch (error.response.status) {
    case 401: {
     ElMessage.error('登录已过期，请重新登录')
     const userStore = useUserStore()
     userStore.logout()
     router.push('/login')
     break
    }
    case 403:
     ElMessage.error('没有权限访问')
     break
    case 404:
     ElMessage.error('请求的资源不存在')
     break
    case 500:
     ElMessage.error('服务器错误，请稍后重试')
     break
    default:
     ElMessage.error(error.response.data?.message || '请求失败')
   }
  } else if (error.request) {
   ElMessage.error('网络错误，请检查网络连接')
  } else {
   ElMessage.error('请求配置错误')
  }

  return Promise.reject(error)
 }
)

// 【修改 2】：接收完整的 response 对象
function handleBusinessResponse(response: any) {
  const skipAuth = response.config?.skipAuth === true
  const res = response.data

  const hasBusinessCode = res && res.code !== undefined && res.code !== null

  // 兼容返回体没有 code 字段的接口（如 Strapi/Mock 的 { data, meta } 响应）
  // 此类接口依赖 HTTP 状态码表达成功/失败
  if (!hasBusinessCode) {
    return res
  }

  const successCodes = new Set([200, 1, 0, '200', '1', '0'])
  const isSuccess = successCodes.has(res?.code)

  if (!isSuccess) {
    const error = ErrorHandler.handleBusinessError(
      res?.code,
      res?.message || res?.msg || '请求失败',
      res
    )

    // 401：只有在“需要鉴权”的请求里才触发全局登出/跳转
    // 否则（比如登录失败/游客接口），仅提示错误并交给页面自行处理
    if (res?.code === 401 && !skipAuth) {
      const userStore = useUserStore()
      userStore.logout()
      ErrorHandler.handlePermissionError(error)
    } else {
      ErrorHandler.showError(error)
    }

    return Promise.reject(error)
  }

  return res
}

export default async function request(config: AxiosRequestConfig) {
  const finalConfig = { ...config }

  // 如果全局开启了 Mock，且当前请求没有显式禁用 Mock
  if (isMockEnabled && finalConfig.mock !== false) {
    // 模拟请求拦截器的 Token 注入逻辑，确保 Mock 接口能拿到 Token
    if (!finalConfig.skipAuth) {
      const userStore = useUserStore()
      const token = userStore.token
      if (token) {
        finalConfig.headers = {
          ...(finalConfig.headers || {}),
          Authorization: `Bearer ${token}`
        }
      }
    }

    const mockRes = handleMockRequest(finalConfig)
    if (mockRes) {
      return handleBusinessResponse({
        data: mockRes,
        status: 200,
        statusText: 'OK',
        headers: {},
        config: finalConfig,
        request: {}
      })
    }
  }
  return service(finalConfig)
}
