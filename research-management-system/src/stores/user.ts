import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { UserRole } from '@/types'
import { errorLogger, AppError, ErrorType } from '@/utils/errorHandler'

const TOKEN_KEY = 'token'
const USER_INFO_KEY = 'userInfo'
const AVAILABLE_STORAGES = () => {
  if (typeof window === 'undefined') return []
  return [window.sessionStorage, window.localStorage]
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getStoredToken())
  const userInfo = ref<Record<string, any> | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const userRole = computed(() => userInfo.value?.role)

  // 登录
  function login(newToken: string, user: Record<string, any>, remember = false) {
    // 先更新内存中的数据 (同步操作,立即生效)
    token.value = newToken
    userInfo.value = user

    const targetStorage = remember ? localStorage : sessionStorage
    const staleStorage = remember ? sessionStorage : localStorage

    // 再保存到存储
    const success =
      safeSetItem(targetStorage, TOKEN_KEY, newToken) &&
      safeSetItem(targetStorage, USER_INFO_KEY, JSON.stringify(user))

    // 清理另一份存储，避免旧状态干扰
    safeRemoveItem(staleStorage, TOKEN_KEY)
    safeRemoveItem(staleStorage, USER_INFO_KEY)

    if (!success) {
      // 如果存储失败,清空内存中的数据
      token.value = ''
      userInfo.value = null
      return false
    }

    // 添加日志,帮助调试
    if (import.meta.env.DEV) {
      console.log('✅ 登录成功:', {
        username: user?.name,
        role: user?.role
      })
    }

    return true
  }

  // 登出
  function logout() {
    token.value = ''
    userInfo.value = null
    AVAILABLE_STORAGES().forEach((storage) => {
      safeRemoveItem(storage, TOKEN_KEY)
      safeRemoveItem(storage, USER_INFO_KEY)
    })
  }

  // 初始化用户信息
  function initUserInfo() {
    const { data, error } = getStoredUserInfo()
    if (data) {
      userInfo.value = data
      return
    }

    if (error && token.value) {
      ElMessage.warning('用户信息已过期，请重新登录')
      logout()
    }
  }

  // 安全的 localStorage 操作
  function safeSetItem(storage: Storage, key: string, value: string) {
    try {
      storage.setItem(key, value)
      return true
    } catch (e) {
      const error = new AppError(
        '存储空间不足或浏览器限制',
        ErrorType.RUNTIME,
        'STORAGE_ERROR',
        e
      )
      errorLogger.log(error)
      ElMessage.error('保存用户信息失败，请检查浏览器设置')
      return false
    }
  }

  function safeRemoveItem(storage: Storage, key: string) {
    try {
      storage.removeItem(key)
    } catch (e) {
      console.error('删除存储失败:', e)
    }
  }

  function getStoredUserInfo(): {
    data: Record<string, any> | null
    error: AppError | null
  } {
    let parseError: AppError | null = null

    for (const storage of AVAILABLE_STORAGES()) {
      const stored = safeGetItem(storage, USER_INFO_KEY)
      if (!stored) continue

      try {
        return {
          data: JSON.parse(stored),
          error: null
        }
      } catch (e) {
        parseError = new AppError(
          '用户信息已损坏，请重新登录',
          ErrorType.VALIDATION,
          'USER_INFO_PARSE_ERROR',
          e
        )
        errorLogger.log(parseError)
        safeRemoveItem(storage, USER_INFO_KEY)
      }
    }

    return {
      data: null,
      error: parseError
    }
  }

  function getStoredToken() {
    for (const storage of AVAILABLE_STORAGES()) {
      const stored = safeGetItem(storage, TOKEN_KEY)
      if (stored) return stored
    }
    return ''
  }

  function safeGetItem(storage: Storage, key: string) {
    try {
      return storage.getItem(key)
    } catch (e) {
      console.error('读取存储失败:', e)
      return null
    }
  }

  // 检查权限
  function hasRole(role: string | string[]) {
    if (!userInfo.value) return false
    const roles = Array.isArray(role) ? role : [role]
    return roles.includes(userInfo.value.role)
  }

  // 检查是否为管理员
  const isAdmin = computed(() => hasRole(UserRole.ADMIN))
  const isManager = computed(() => hasRole([UserRole.ADMIN, UserRole.MANAGER]))
  const isExpert = computed(() => hasRole([UserRole.ADMIN, UserRole.EXPERT]))

  return {
    token,
    userInfo,
    isLoggedIn,
    userRole,
    isAdmin,
    isManager,
    isExpert,
    login,
    logout,
    initUserInfo,
    hasRole
  }
})
