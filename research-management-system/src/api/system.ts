import request from '@/utils/request'

export type CrawlerStatus = 'healthy' | 'warning' | 'error' | 'idle'
export type CrawlerType = 'rss' | 'html' | 'api' | 'file'
export type AuthType = 'none' | 'api_key' | 'cookie' | 'basic'
export type CrawlerPriority = 'low' | 'medium' | 'high'

export interface CrawlerDataSource {
  id?: string
  name: string
  type: CrawlerType
  industry?: string
  region?: string
  baseUrl: string
  description?: string
  authType: AuthType
  credentials?: Record<string, any>
  frequencyHours: number
  priority: CrawlerPriority
  tags?: string[]
  enabled: boolean
  lastRunAt?: string
  lastSuccessAt?: string
  status?: CrawlerStatus
  failureReason?: string
}

export interface CrawlerSettings {
  defaultFrequencyHours: number
  retryLimit: number
  autoTagging: boolean
  deduplicateThreshold: number
  notifyEmails: string[]
  notifyWebhook?: string
}

export function getCrawlerSources(params?: Record<string, any>) {
  return request({
    url: '/system/crawler-sources',
    method: 'get',
    params
  })
}

export function createCrawlerSource(data: CrawlerDataSource) {
  return request({
    url: '/system/crawler-sources',
    method: 'post',
    data
  })
}

export function updateCrawlerSource(id: string, data: Partial<CrawlerDataSource>) {
  return request({
    url: `/system/crawler-sources/${id}`,
    method: 'put',
    data
  })
}

export function deleteCrawlerSource(id: string) {
  return request({
    url: `/system/crawler-sources/${id}`,
    method: 'delete'
  })
}

export function testCrawlerSource(id: string) {
  return request({
    url: `/system/crawler-sources/${id}/test`,
    method: 'post'
  })
}

export function getCrawlerSettings() {
  return request({
    url: '/system/crawler-settings',
    method: 'get'
  })
}

export function updateCrawlerSettings(data: Partial<CrawlerSettings>) {
  return request({
    url: '/system/crawler-settings',
    method: 'put',
    data
  })
}
