import request from '@/utils/request'
import { normalizeStrapiCollection, normalizeStrapiSingle } from '@/utils/strapi'
import type { ApiResponse, StrapiCollectionResponse, StrapiSingleResponse, QueryParams } from './types'

// 获取项目列表
export async function getProjects(params?: QueryParams): Promise<StrapiCollectionResponse<any>> {
  const res = await request({
    url: '/projects',
    method: 'get',
    params
  })
  return normalizeStrapiCollection(res)
}

// 获取项目详情
export async function getProject(id: string): Promise<StrapiSingleResponse<any>> {
  const res = await request({
    url: `/projects/${id}`,
    method: 'get'
  })
  return normalizeStrapiSingle(res)
}

// 创建项目
export function createProject(data: { name: string; code: string }): Promise<ApiResponse<any>> {
  return request({
    url: '/projects',
    method: 'post',
    data
  })
}
