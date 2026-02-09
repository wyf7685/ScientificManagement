import { ElMessage, ElNotification } from 'element-plus'
import type { App } from 'vue'
import router from '@/router'

const IGNORED_RUNTIME_ERROR_PATTERNS = [
  /ResizeObserver loop limit exceeded/i,
  /ResizeObserver loop completed with undelivered notifications/i
]

function isIgnoredRuntimeError(message?: string) {
  if (!message) return false
  return IGNORED_RUNTIME_ERROR_PATTERNS.some((pattern) => pattern.test(message))
}

/**
 * 错误类型枚举
 */
export enum ErrorType {
  NETWORK = 'NETWORK',
  BUSINESS = 'BUSINESS',
  PERMISSION = 'PERMISSION',
  VALIDATION = 'VALIDATION',
  UNKNOWN = 'UNKNOWN',
  RUNTIME = 'RUNTIME'
}

/**
 * 应用错误类
 */
export class AppError extends Error {
  type: ErrorType
  code?: number | string
  details?: any

  constructor(
    message: string,
    type: ErrorType = ErrorType.UNKNOWN,
    code?: number | string,
    details?: any
  ) {
    super(message)
    this.name = 'AppError'
    this.type = type
    this.code = code
    this.details = details
  }
}

/**
 * 错误日志记录
 */
interface ErrorLog {
  message: string
  type: ErrorType
  code?: number | string
  timestamp: string
  url?: string
  userAgent?: string
  stack?: string
  details?: any
}

class ErrorLogger {
  private logs: ErrorLog[] = []
  private maxLogs = 100

  log(error: AppError | Error, context?: any) {
    const errorLog: ErrorLog = {
      message: error.message,
      type: error instanceof AppError ? error.type : ErrorType.RUNTIME,
      code: error instanceof AppError ? error.code : undefined,
      timestamp: new Date().toISOString(),
      url: window.location.href,
      userAgent: navigator.userAgent,
      stack: error.stack,
      details: error instanceof AppError ? error.details : context
    }

    this.logs.push(errorLog)

    // 限制日志数量
    if (this.logs.length > this.maxLogs) {
      this.logs.shift()
    }

    // 开发环境打印详细错误
    if (import.meta.env.DEV) {
      console.error('🔴 Error Log:', errorLog)
    }

    // 生产环境可以发送到错误监控服务
    if (import.meta.env.PROD) {
      this.reportToServer(errorLog)
    }
  }

  private reportToServer(errorLog: ErrorLog) {
    // TODO: 发送到错误监控服务 (Sentry, LogRocket 等)
    // fetch('/api/errors', {
    //   method: 'POST',
    //   body: JSON.stringify(errorLog)
    // }).catch(() => {})
  }

  getLogs() {
    return [...this.logs]
  }

  clearLogs() {
    this.logs = []
  }
}

export const errorLogger = new ErrorLogger()

/**
 * 错误处理器
 */
export class ErrorHandler {
  /**
   * 处理网络错误
   */
  static handleNetworkError(error: any): AppError {
    let message = '网络连接失败，请检查网络设置'
    let code = 'NETWORK_ERROR'

    if (error.code === 'ECONNABORTED') {
      message = '请求超时，请稍后重试'
      code = 'TIMEOUT'
    } else if (error.message === 'Network Error') {
      message = '网络异常，请检查网络连接'
      code = 'NETWORK_OFFLINE'
    }

    return new AppError(message, ErrorType.NETWORK, code, error)
  }

  /**
   * 处理 HTTP 错误
   */
  static handleHttpError(status: number, data?: any): AppError {
    const errorMap: Record<number, { message: string; type: ErrorType }> = {
      400: { message: '请求参数错误', type: ErrorType.VALIDATION },
      401: { message: '登录已过期，请重新登录', type: ErrorType.PERMISSION },
      403: { message: '没有权限访问该资源', type: ErrorType.PERMISSION },
      404: { message: '请求的资源不存在', type: ErrorType.BUSINESS },
      405: { message: '请求方法不允许', type: ErrorType.BUSINESS },
      408: { message: '请求超时', type: ErrorType.NETWORK },
      500: { message: '服务器内部错误', type: ErrorType.BUSINESS },
      502: { message: '网关错误', type: ErrorType.NETWORK },
      503: { message: '服务暂时不可用', type: ErrorType.NETWORK },
      504: { message: '网关超时', type: ErrorType.NETWORK }
    }

    const error = errorMap[status] || {
      message: data?.message || '未知错误',
      type: ErrorType.UNKNOWN
    }

    return new AppError(error.message, error.type, status, data)
  }

  /**
   * 处理业务错误
   */
  static handleBusinessError(code: number | string, message: string, data?: any): AppError {
    return new AppError(message, ErrorType.BUSINESS, code, data)
  }

  /**
   * 显示错误消息
   */
  static showError(error: AppError | Error, options?: {
    useNotification?: boolean
    duration?: number
  }) {
    const message = error.message || '操作失败'
    const duration = options?.duration || 3000

    // 记录错误日志
    errorLogger.log(error)

    // 根据错误类型选择提示方式
    if (options?.useNotification || error instanceof AppError && error.type === ErrorType.RUNTIME) {
      ElNotification({
        title: '错误',
        message,
        type: 'error',
        duration
      })
    } else {
      ElMessage.error({
        message,
        duration,
        showClose: true
      })
    }
  }

  /**
   * 处理权限错误
   */
  static handlePermissionError(error: AppError) {
    errorLogger.log(error)
    
    ElMessage.error({
      message: error.message,
      duration: 3000
    })

    // 401 跳转到登录页
    if (error.code === 401) {
      setTimeout(() => {
        router.push({
          path: '/login',
          query: { redirect: router.currentRoute.value.fullPath }
        })
      }, 1500)
    }
    // 403 跳转到无权限页面
    else if (error.code === 403) {
      setTimeout(() => {
        router.push('/403')
      }, 1500)
    }
  }

  /**
   * 处理验证错误
   */
  static handleValidationError(errors: Record<string, string[]> | string) {
    if (typeof errors === 'string') {
      ElMessage.error(errors)
      return
    }

    const messages = Object.entries(errors)
      .map(([field, msgs]) => `${field}: ${msgs.join(', ')}`)
      .join('\n')

    ElNotification({
      title: '表单验证失败',
      message: messages,
      type: 'error',
      duration: 5000
    })
  }
}

/**
 * 全局错误处理插件
 */
export function setupErrorHandler(app: App) {
  // Vue 错误处理
  app.config.errorHandler = (err: any, instance, info) => {
    if (isIgnoredRuntimeError(err?.message)) return

    console.error('Vue Error:', err, info)
    
    const error = new AppError(
      err.message || '应用运行时错误',
      ErrorType.RUNTIME,
      'RUNTIME_ERROR',
      { info, componentName: instance?.$options?.name }
    )

    ErrorHandler.showError(error, { useNotification: true })
  }

  // Vue 警告处理 (仅开发环境)
  if (import.meta.env.DEV) {
    app.config.warnHandler = (msg, instance, trace) => {
      console.warn('Vue Warning:', msg, trace)
    }
  }

  // 全局未捕获的 Promise 错误
  window.addEventListener('unhandledrejection', (event) => {
    const reasonMessage = typeof event.reason === 'string'
      ? event.reason
      : event.reason?.message
    if (isIgnoredRuntimeError(reasonMessage)) {
      event.preventDefault()
      return
    }

    console.error('Unhandled Promise Rejection:', event.reason)
    
    const error = new AppError(
      event.reason?.message || '未处理的异步错误',
      ErrorType.RUNTIME,
      'UNHANDLED_REJECTION',
      event.reason
    )

    ErrorHandler.showError(error, { useNotification: true })
    event.preventDefault()
  })

  // 全局 JavaScript 错误
  window.addEventListener('error', (event) => {
    const runtimeMessage = event.error?.message || event.message
    if (isIgnoredRuntimeError(runtimeMessage)) {
      event.preventDefault()
      return
    }

    console.error('Global Error:', event.error)
    
    const error = new AppError(
      event.error?.message || event.message || '全局错误',
      ErrorType.RUNTIME,
      'GLOBAL_ERROR',
      {
        filename: event.filename,
        lineno: event.lineno,
        colno: event.colno
      }
    )

    ErrorHandler.showError(error, { useNotification: true })
  })

  // 资源加载错误
  window.addEventListener('error', (event) => {
    const target = event.target as any
    if (target?.tagName) {
      console.error('Resource Load Error:', target.src || target.href)
      
      // 静默处理资源加载错误，只记录日志
      errorLogger.log(
        new AppError(
          `资源加载失败: ${target.src || target.href}`,
          ErrorType.NETWORK,
          'RESOURCE_LOAD_ERROR'
        )
      )
    }
  }, true)
}

/**
 * 异步错误包装器
 */
export function asyncErrorHandler<T extends (...args: any[]) => Promise<any>>(
  fn: T,
  options?: {
    showError?: boolean
    errorMessage?: string
    onError?: (error: any) => void
  }
): T {
  return (async (...args: any[]) => {
    try {
      return await fn(...args)
    } catch (error: any) {
      console.error('Async Error:', error)

      if (options?.showError !== false) {
        const message = options?.errorMessage || error.message || '操作失败'
        ErrorHandler.showError(
          error instanceof AppError ? error : new AppError(message, ErrorType.UNKNOWN)
        )
      }

      if (options?.onError) {
        options.onError(error)
      }

      throw error
    }
  }) as T
}

/**
 * Try-Catch 辅助函数
 */
export async function tryCatch<T>(
  fn: () => Promise<T>,
  options?: {
    showError?: boolean
    errorMessage?: string
    defaultValue?: T
  }
): Promise<[T | null, AppError | null]> {
  try {
    const result = await fn()
    return [result, null]
  } catch (error: any) {
    const appError = error instanceof AppError 
      ? error 
      : new AppError(
          options?.errorMessage || error.message || '操作失败',
          ErrorType.UNKNOWN
        )

    if (options?.showError !== false) {
      ErrorHandler.showError(appError)
    }

    return [options?.defaultValue ?? null, appError]
  }
}
