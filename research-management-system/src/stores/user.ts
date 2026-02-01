import type { UserProfile } from "@/api/auth";
import { AppError, errorLogger, ErrorType } from "@/utils/errorHandler";
import { ElMessage } from "element-plus";
import { defineStore } from "pinia";
import { computed, ref } from "vue";

const TOKEN_KEY = "access_token";
const REFRESH_TOKEN_KEY = "refresh_token";
const USER_INFO_KEY = "user_info";
const AVAILABLE_STORAGES = () => {
  if (typeof window === "undefined") return [];
  return [window.sessionStorage, window.localStorage];
};

export const useUserStore = defineStore("user", () => {
  const accessToken = ref<string>(getStoredToken());
  const refreshToken = ref<string>(getStoredRefreshToken());
  const userInfo = ref<UserProfile | null>(getStoredUserInfo());

  const isLoggedIn = computed(() => !!accessToken.value);

  /**
   * 登录：保存 token 和用户信息
   */
  function login(
    newAccessToken: string,
    newRefreshToken: string,
    user: UserProfile,
    remember = false,
  ) {
    // 先更新内存
    accessToken.value = newAccessToken;
    refreshToken.value = newRefreshToken;
    userInfo.value = user;

    const targetStorage = remember ? localStorage : sessionStorage;
    const staleStorage = remember ? sessionStorage : localStorage;

    // 保存到存储
    const success =
      safeSetItem(targetStorage, TOKEN_KEY, newAccessToken) &&
      safeSetItem(targetStorage, REFRESH_TOKEN_KEY, newRefreshToken) &&
      safeSetItem(targetStorage, USER_INFO_KEY, JSON.stringify(user));

    // 清理另一份存储
    safeRemoveItem(staleStorage, TOKEN_KEY);
    safeRemoveItem(staleStorage, REFRESH_TOKEN_KEY);
    safeRemoveItem(staleStorage, USER_INFO_KEY);

    if (!success) {
      accessToken.value = "";
      refreshToken.value = "";
      userInfo.value = null;
      return false;
    }

    if (import.meta.env.DEV) {
      console.log("✅ 登录成功:", {
        username: user?.username,
        roles: user?.roles,
      });
    }

    return true;
  }

  /**
   * 刷新 token
   */
  function setTokens(newAccessToken: string, newRefreshToken: string) {
    accessToken.value = newAccessToken;
    refreshToken.value = newRefreshToken;

    AVAILABLE_STORAGES().forEach((storage) => {
      safeSetItem(storage, TOKEN_KEY, newAccessToken);
      safeSetItem(storage, REFRESH_TOKEN_KEY, newRefreshToken);
    });
  }

  /**
   * 登出
   */
  function logout() {
    accessToken.value = "";
    refreshToken.value = "";
    userInfo.value = null;

    AVAILABLE_STORAGES().forEach((storage) => {
      safeRemoveItem(storage, TOKEN_KEY);
      safeRemoveItem(storage, REFRESH_TOKEN_KEY);
      safeRemoveItem(storage, USER_INFO_KEY);
    });
  }

  /**
   * 初始化用户信息
   */
  function initUserInfo() {
    const stored = getStoredUserInfo();
    if (stored) {
      userInfo.value = stored;
      return;
    }

    if (accessToken.value) {
      ElMessage.warning("用户信息已过期，请重新登录");
      logout();
    }
  }

  /**
   * 检查是否拥有指定角色（任一即可）
   */
  function hasRole(roles: string | string[]): boolean {
    if (!userInfo.value?.roles) return false;
    const checkRoles = Array.isArray(roles) ? roles : [roles];
    return checkRoles.some((r) => userInfo.value!.roles.includes(r));
  }

  /**
   * 检查是否拥有全部指定角色
   */
  function hasAllRoles(roles: string[]): boolean {
    if (!userInfo.value?.roles) return false;
    return roles.every((r) => userInfo.value!.roles.includes(r));
  }

  /**
   * 计算属性：是否为管理员
   */
  const isAdmin = computed(() => hasRole("research_admin"));

  /**
   * 计算属性：是否为专家
   */
  const isExpert = computed(() => hasRole("research_expert"));

  /**
   * 计算属性：是否为项目负责人
   */
  const isProjectLeader = computed(() => hasRole("project_leader"));

  /**
   * 计算属性：是否为决策机构
   */
  const isDecisionOrg = computed(() => hasRole("decision_org"));

  // ============ Storage Helpers ============

  function safeSetItem(storage: Storage, key: string, value: string) {
    try {
      storage.setItem(key, value);
      return true;
    } catch (e) {
      const error = new AppError("存储空间不足或浏览器限制", ErrorType.RUNTIME, "STORAGE_ERROR", e);
      errorLogger.log(error);
      ElMessage.error("保存用户信息失败，请检查浏览器设置");
      return false;
    }
  }

  function safeRemoveItem(storage: Storage, key: string) {
    try {
      storage.removeItem(key);
    } catch (e) {
      console.error("删除存储失败:", e);
    }
  }

  function safeGetItem(storage: Storage, key: string) {
    try {
      return storage.getItem(key);
    } catch (e) {
      console.error("读取存储失败:", e);
      return null;
    }
  }

  function getStoredToken(): string {
    for (const storage of AVAILABLE_STORAGES()) {
      const stored = safeGetItem(storage, TOKEN_KEY);
      if (stored) return stored;
    }
    return "";
  }

  function getStoredRefreshToken(): string {
    for (const storage of AVAILABLE_STORAGES()) {
      const stored = safeGetItem(storage, REFRESH_TOKEN_KEY);
      if (stored) return stored;
    }
    return "";
  }

  function getStoredUserInfo(): UserProfile | null {
    let parseError: AppError | null = null;

    for (const storage of AVAILABLE_STORAGES()) {
      const stored = safeGetItem(storage, USER_INFO_KEY);
      if (!stored) continue;

      try {
        return JSON.parse(stored) as UserProfile;
      } catch (e) {
        parseError = new AppError(
          "用户信息已损坏，请重新登录",
          ErrorType.VALIDATION,
          "USER_INFO_PARSE_ERROR",
          e,
        );
        errorLogger.log(parseError);
        safeRemoveItem(storage, USER_INFO_KEY);
      }
    }

    return null;
  }

  return {
    accessToken,
    refreshToken,
    userInfo,
    isLoggedIn,
    isAdmin,
    isExpert,
    isProjectLeader,
    isDecisionOrg,
    login,
    setTokens,
    logout,
    initUserInfo,
    hasRole,
    hasAllRoles,
  };
});
