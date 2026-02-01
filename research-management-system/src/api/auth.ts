import request from "@/utils/request";
import type { AxiosInstance } from "axios";
import axios from "axios";
import type { ApiResponse } from "./types";

/**
 * Keycloak token 请求负载（密码授权流程）
 */
export type KeycloakTokenRequest = {
  username: string;
  password: string;
};

/**
 * Keycloak token 响应
 */
export interface KeycloakTokenResponse {
  access_token: string;
  refresh_token: string;
  expires_in: number;
  refresh_expires_in: number;
  token_type: string;
  scope: string;
}

/**
 * JWT Claims（用于提取用户信息）
 */
export interface JwtClaims {
  sub: string;
  email_verified: boolean;
  realm_access: {
    roles: string[];
  };
  iss: string;
  name: string;
  preferred_username: string;
  given_name: string;
  family_name: string;
  email: string;
  iat: number;
  exp: number;
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

// ============ Keycloak Token Endpoint ============

const clientId = import.meta.env.VITE_KEYCLOAK_CLIENT_ID;
const realm = import.meta.env.VITE_KEYCLOAK_REALM;
const tokenEndpoint = `/realms/${realm}/protocol/openid-connect/token`;

let keycloakClient: AxiosInstance | null = null;

function getKeycloakClient(): AxiosInstance {
  if (!keycloakClient) {
    keycloakClient = axios.create({
      baseURL: "/auth",
      timeout: 10000,
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
    });
  }
  return keycloakClient;
}

/**
 * 获取 Keycloak token（密码授权流程）
 * 前端直接调用，不走 backend 代理
 * @param username 用户名
 * @param password 密码
 */
export async function getKeycloakToken(
  username: string,
  password: string,
): Promise<KeycloakTokenResponse> {
  const params = new URLSearchParams();
  params.append("grant_type", "password");
  params.append("client_id", clientId);
  params.append("username", username);
  params.append("password", password);

  const client = getKeycloakClient();
  const response = await client.post<KeycloakTokenResponse>(tokenEndpoint, params);
  return response.data;
}

/**
 * 刷新 Keycloak token
 * @param refreshToken 刷新令牌
 */
export async function refreshKeycloakToken(refreshToken: string): Promise<KeycloakTokenResponse> {
  const params = new URLSearchParams();
  params.append("grant_type", "refresh_token");
  params.append("client_id", clientId);
  params.append("refresh_token", refreshToken);

  const client = getKeycloakClient();
  const response = await client.post<KeycloakTokenResponse>(tokenEndpoint, params);
  return response.data;
}

/**
 * 从 JWT access_token 中提取用户信息
 * @param accessToken JWT token
 */
export function parseUserFromToken(accessToken: string): UserProfile | null {
  try {
    const payload = accessToken.split(".")[1];
    if (!payload) return null;

    const decoded = JSON.parse(atob(payload)) as JwtClaims;

    // 从 realm_access.roles 中过滤业务角色
    const businessRoles = (decoded.realm_access?.roles || []).filter(
      (role) =>
        !role.startsWith("default-") && role !== "offline_access" && role !== "uma_authorization",
    );

    return {
      id: 0, // 暂时为 0，需要后端补全
      uuid: decoded.sub,
      username: decoded.preferred_username || "",
      name: decoded.name || "",
      email: decoded.email || "",
      roles: businessRoles,
    };
  } catch (e) {
    console.error("Failed to parse JWT token:", e);
    return null;
  }
}

// ============ Backend 认证接口 ============

/**
 * 验证 JWT token 并获取完整用户信息（包含系统 ID）
 * 前端登录后调用此接口，补全用户的系统 ID
 */
export function verifyToken(accessToken: string) {
  return request({
    url: "/auth/verify",
    method: "post",
    data: { accessToken },
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  }) as Promise<ApiResponse<UserProfile>>;
}

/**
 * 登出
 */
export function logout() {
  return request({
    url: "/auth/logout",
    method: "post",
  }) as Promise<ApiResponse<true>>;
}

/**
 * 获取当前用户信息
 */
export function getCurrentUser() {
  return request({
    url: "/auth/current",
    method: "get",
  }) as Promise<ApiResponse<UserProfile>>;
}
