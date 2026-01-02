// API 通用类型定义

// 标准API响应格式
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// Strapi 分页响应格式
export interface StrapiPaginatedResponse<T = any> {
  data: {
    list: T[]
    total: number
    page: number
    pageSize: number
  }
  meta?: any
}

// Strapi 集合响应格式
export interface StrapiCollectionResponse<T = any> {
  data: T[]
  meta?: any
}

// Strapi 单项响应格式
export interface StrapiSingleResponse<T = any> {
  data: T
  meta?: any
}

// 分页查询参数
export interface PaginationParams {
  page?: number
  pageSize?: number
}

// 通用查询参数
export type QueryParams = Record<string, any>

// 统计数据响应
export interface StatisticsData {
  totalResults?: number
  publishedResults?: number
  draftResults?: number
  pendingReviews?: number
  [key: string]: any
}

// 关键词图谱节点
export interface KeywordNode {
  name: string
  value: number
  category?: string
}

// 关键词图谱连接
export interface KeywordLink {
  source: string
  target: string
  value?: number
}

// 关键词云数据
export interface KeywordCloudData {
  range?: string
  nodes: KeywordNode[]
  links: KeywordLink[]
}






