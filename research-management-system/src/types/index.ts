// 业务常量：集中放置角色、状态和值域，避免魔法字符串。
export const UserRole = {
  ADMIN: 'admin',
  MANAGER: 'manager',
  RESEARCHER: 'researcher',
  EXPERT: 'expert',
  GUEST: 'guest'
} as const

export type UserRole = typeof UserRole[keyof typeof UserRole]

export const ResultStatus = {
  DRAFT: 'draft',
  PENDING: 'pending',
  REVIEWING: 'reviewing',
  REVISION: 'revision',
  REJECTED: 'rejected',
  PUBLISHED: 'published',
  REVOKED: 'revoked'
} as const

export type ResultStatus = typeof ResultStatus[keyof typeof ResultStatus]

export const ResultSource = {
  MANUAL_UPLOAD: 'manual_upload',
  PROCESS_SYSTEM: 'process_system'
} as const

export type ResultSource = typeof ResultSource[keyof typeof ResultSource]

export const ProjectPhase = {
  INITIATION: 'initiation',
  DESIGN: 'design',
  DEVELOPMENT: 'development',
  EXPERIMENT: 'experiment',
  UAT: 'uat',
  DELIVERY: 'delivery',
  OPERATION: 'operation'
} as const

export type ProjectPhase = typeof ProjectPhase[keyof typeof ProjectPhase]

export const ResultVisibility = {
  PRIVATE: 'private',
  INTERNAL_ABSTRACT: 'internal_abstract',
  INTERNAL_FULL: 'internal_full',
  PUBLIC_ABSTRACT: 'public_abstract',
  PUBLIC_FULL: 'public_full'
} as const

export type ResultVisibility = typeof ResultVisibility[keyof typeof ResultVisibility]

// 成果访问权限状态
export const AccessPermissionStatus = {
  FULL: 'full',
  SUMMARY: 'summary',
  DENIED: 'denied'
} as const

export type AccessPermissionStatus = typeof AccessPermissionStatus[keyof typeof AccessPermissionStatus]

// 访问申请状态
export const AccessRequestStatus = {
  NONE: 'none',
  PENDING: 'pending',
  APPROVED: 'approved',
  REJECTED: 'rejected'
} as const

export type AccessRequestStatus = typeof AccessRequestStatus[keyof typeof AccessRequestStatus]

// 企业需求匹配
export type DemandStatus = 'unmatched' | 'matched' | 'in_follow_up'

export interface DemandMatch {
  resultId: string
  resultTitle: string
  resultType: string
  owner?: string
  matchScore: number
  reason?: string
  sourceSnippet?: string
  updatedAt?: string
}

export interface DemandItem {
  id: string
  title: string
  summary: string
  fullText?: string
  llmSummary?: string
  keywords?: string[]
  tags?: string[]
  industry?: string
  region?: string
  sourceCategory?: string
  sourceSite?: string
  sourceUrl?: string
  capturedAt?: string
  confidence?: number
  status: DemandStatus
  bestMatchScore?: number
  matches?: DemandMatch[]
}

export const FieldType = {
  TEXT: 'text',
  TEXTAREA: 'textarea',
  NUMBER: 'number',
  DATE: 'date',
  SELECT: 'select',
  MULTI_SELECT: 'multi_select',
  FILE: 'file',
  BOOLEAN: 'boolean'
} as const

export type FieldType = typeof FieldType[keyof typeof FieldType]

// 项目基本信息
export interface Project {
  id: string
  name: string
  code: string
  description?: string
}

// 成果基础模型（部分页面使用）
export interface ResearchResult {
  id?: string
  title: string
  typeId: string
  type?: string
  authors: string[]
  year: string | number
  abstract?: string
  keywords?: string[]
  visibility: ResultVisibility
  metadata: Record<string, any>
  attachments?: Array<{ id?: string; name: string; url: string; size?: number }>
  projectId?: string
  projectName?: string
  projectCode?: string
  status?: ResultStatus
  source?: ResultSource
  sourceStage?: string
  projectPhase?: ProjectPhase | string
  sourceRef?: string
  syncTime?: string
  assignedReviewers?: string[]
  formatChecked?: boolean
  formatStatus?: 'pending' | 'passed' | 'failed'
  formatNote?: string
  createdAt?: string
  updatedAt?: string
  permissionStatus?: AccessPermissionStatus
  accessRequestStatus?: AccessRequestStatus
  canRequestAccess?: boolean
  rejectedReason?: string
  lastRequestAt?: string
  content?: string
}
