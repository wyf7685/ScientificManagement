import { parseUserFromToken, refreshKeycloakToken, verifyToken } from "@/api/auth";
import { handleMockRequest } from "@/mocks/handler";
import router from "@/router";
import { useUserStore } from "@/stores/user";
import axios, { type AxiosRequestConfig } from "axios";
import { ElMessage } from "element-plus";
import { storeToRefs } from "pinia";
import { ErrorHandler } from "./errorHandler";

declare module "axios" {
  export interface AxiosRequestConfig {
    /** 跳过鉴权（不附加 Authorization 头） */
    skipAuth?: boolean;
    /** 允许单个请求控制是否使用 Mock */
    mock?: boolean;
    /** 静默模式，不弹出全局错误提示 */
    silent?: boolean;
  }
}

const isMockEnabled = import.meta.env.VITE_USE_MOCK === "true";

const service = axios.create({
  baseURL: "/api",
  timeout: 15000,
  headers: {
    "Content-Type": "application/json",
  },
});

// ============ Token 刷新队列（处理并发请求时的竞合） ============
let isRefreshing = false;
let failedQueue: Array<{ resolve: (token: string) => void; reject: (err: any) => void }> = [];

function addToQueue(resolve: (token: string) => void, reject: (err: any) => void) {
  failedQueue.push({ resolve, reject });
}

function processQueue(error: any, token: string = "") {
  failedQueue.forEach((promise) => {
    if (error) {
      promise.reject(error);
    } else {
      promise.resolve(token);
    }
  });
  failedQueue = [];
}

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    if (config.skipAuth) {
      return config;
    }

    const userStore = useUserStore();
    const { accessToken } = storeToRefs(userStore);
    if (accessToken.value) {
      config.headers = {
        ...(config.headers as any),
        Authorization: `Bearer ${accessToken.value}`,
      } as any;
    }
    return config;
  },
  (error) => {
    console.error("请求错误:", error);
    return Promise.reject(error);
  },
);

// 响应拦截器
service.interceptors.response.use(
  (response) => handleBusinessResponse(response),
  async (error) => {
    console.error("响应错误:", error);

    const config = error.config;
    if (!config || config.skipAuth || error.response?.status !== 401) {
      return handleErrorResponse(error);
    }

    // ============ 401 Token 过期 - 尝试刷新 ============
    if (isRefreshing) {
      // 已有刷新请求进行中，等待结果
      return new Promise((resolve, reject) => {
        addToQueue((token) => {
          config.headers.Authorization = `Bearer ${token}`;
          resolve(service(config));
        }, reject);
      });
    }

    isRefreshing = true;
    const userStore = useUserStore();
    const { refreshToken } = storeToRefs(userStore);

    try {
      if (!refreshToken.value) {
        throw new Error("No refresh token available");
      }

      // 调用 Keycloak 刷新接口
      const tokenRes = await refreshKeycloakToken(refreshToken.value);
      const newAccessToken = tokenRes.access_token;
      const newRefreshToken = tokenRes.refresh_token;

      // 更新 store
      userStore.setTokens(newAccessToken, newRefreshToken);

      // 解析用户信息（补全系统 ID）
      const partialUser = parseUserFromToken(newAccessToken);
      if (partialUser) {
        try {
          const fullUserRes = await verifyToken(newAccessToken);
          if (fullUserRes?.data) {
            userStore.userInfo = fullUserRes.data;
          }
        } catch (e) {
          console.warn("Failed to verify token, using partial user info:", e);
        }
      }

      // 重试原请求
      config.headers.Authorization = `Bearer ${newAccessToken}`;
      processQueue(null, newAccessToken);
      return service(config);
    } catch (refreshError) {
      console.error("Token refresh failed:", refreshError);
      processQueue(refreshError, "");

      // 刷新失败，清空登录状态，跳转登录页
      userStore.logout();
      ElMessage.error("登录已过期，请重新登录");
      router.push("/login");

      return Promise.reject(refreshError);
    } finally {
      isRefreshing = false;
    }
  },
);

function handleErrorResponse(error: any) {
  const config = error.config;

  if (config?.silent) {
    return Promise.reject(error);
  }

  if (error.response) {
    switch (error.response.status) {
      case 401:
        ElMessage.error("登录已过期，请重新登录");
        const userStore = useUserStore();
        userStore.logout();
        router.push("/login");
        break;
      case 403:
        ElMessage.error("没有权限访问");
        break;
      case 404:
        ElMessage.error("请求的资源不存在");
        break;
      case 500:
        ElMessage.error("服务器错误，请稍后重试");
        break;
      default:
        ElMessage.error(error.response.data?.message || "请求失败");
    }
  } else if (error.request) {
    ElMessage.error("网络错误，请检查网络连接");
  } else {
    ElMessage.error("请求配置错误");
  }

  return Promise.reject(error);
}

// 【修改 2】：接收完整的 response 对象
function handleBusinessResponse(response: any) {
  const skipAuth = response.config?.skipAuth === true;
  const res = response.data;

  const hasBusinessCode = res && res.code !== undefined && res.code !== null;

  // 兼容返回体没有 code 字段的接口（如 Strapi/Mock 的 { data, meta } 响应）
  // 此类接口依赖 HTTP 状态码表达成功/失败
  if (!hasBusinessCode) {
    return res;
  }

  const successCodes = new Set([200, 1, 0, "200", "1", "0"]);
  const isSuccess = successCodes.has(res?.code);

  if (!isSuccess) {
    const error = ErrorHandler.handleBusinessError(
      res?.code,
      res?.msg || "请求失败",
      res,
    );

    // 401：只有在“需要鉴权”的请求里才触发全局登出/跳转
    // 否则（比如登录失败/游客接口），仅提示错误并交给页面自行处理
    if (res?.code === 401 && !skipAuth) {
      const userStore = useUserStore();
      userStore.logout();
      ErrorHandler.handlePermissionError(error);
    } else {
      ErrorHandler.showError(error);
    }

    return Promise.reject(error);
  }

  return res;
}

export default async function request(config: AxiosRequestConfig) {
  const finalConfig = { ...config };

  // 如果全局开启了 Mock，且当前请求没有显式禁用 Mock
  if (isMockEnabled && finalConfig.mock !== false) {
    // 模拟请求拦截器的 Token 注入逻辑，确保 Mock 接口能拿到 Token
    if (!finalConfig.skipAuth) {
      const { accessToken } = storeToRefs(useUserStore());
      if (accessToken.value) {
        finalConfig.headers = {
          ...(finalConfig.headers || {}),
          Authorization: `Bearer ${accessToken.value}`,
        };
      }
    }

    const mockRes = handleMockRequest(finalConfig);
    if (mockRes) {
      return handleBusinessResponse({
        data: mockRes,
        status: 200,
        statusText: "OK",
        headers: {},
        /** @ts-ignore */
        config: finalConfig,
        request: {},
      });
    }
  }
  return service(finalConfig);
}
