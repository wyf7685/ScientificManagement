import axios from 'axios'
import { ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { useUserStore } from '@/stores/user'
import router from '@/router'
import { handleMockRequest } from '@/mocks/handler'
import { ErrorHandler, AppError, ErrorType } from './errorHandler'

declare module 'axios' {
  export interface AxiosRequestConfig {
    skipAuth?: boolean
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
   config.headers = config.headers || {}
   config.headers.Authorization = `Bearer ${token.value}`
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
  const isStrapiRequest = response.config.skipAuth === true;
  const res = response.data; // 提取实际响应体

  // 【新增逻辑】：如果是 Strapi 请求，跳过 code: 200 检查
  if (isStrapiRequest) {
    // 成功（HTTP 状态码 200）且是 Strapi 接口，直接返回数据
    // Strapi 接口通常没有 code 字段
    return res;
  }

  // --- 以下是针对你自己的后端逻辑（需要 code: 200 检查） ---

 if (res?.code !== 200) {
  const error = ErrorHandler.handleBusinessError(
   res?.code,
   res?.message || '请求失败',
   res
  )

  if (res?.code === 401) {
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

export default async function request(config: axios.AxiosRequestConfig) {
 const finalConfig = config

 if (isMockEnabled) {
  const mockRes = handleMockRequest(finalConfig)
  if (mockRes) return handleBusinessResponse(mockRes)
 }
 return service(finalConfig)
}