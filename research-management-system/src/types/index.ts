// 业务常量：集中放置角色、状态和值域，避免魔法字符串。
export const UserRole = {
  ADMIN: 'research_admin',
  RESEARCHER: 'project_leader',
  EXPERT: 'research_expert',
  DECISION_ORG: 'decision_org',
} as const

export type UserRole = typeof UserRole[keyof typeof UserRole]

export const ResultStatus = {
  ALL: 'all',
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
  PUBLIC: 'public'
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

export interface ResultAccessRequest {
  id: string
  resultId: string
  resultTitle: string
  resultType?: string
  projectName?: string
  visibility?: ResultVisibility
  userId: string
  userName: string
  reason: string
  status: AccessRequestStatus
  createdAt: string
  reviewedAt?: string
  reviewer?: string
  comment?: string
}

// ==================== 中期成果物类型定义 ====================

/**
 * 中期成果物类型枚举
 */
export const InterimResultType = {
  CONTRACT: 'contract',                    // 合同
  CONTRACT_TEMPLATE: 'contract_template',  // 合同模板
  SIGNED_CONTRACT: 'signed_contract',      // 已签署合同
  APPLICATION: 'application',              // 申报书
  DELIVERABLE_REPORT: 'deliverable_report', // 成果物报告
  FEASIBILITY_REPORT: 'feasibility_report', // 可行性报告
  REQUIREMENT_DOC: 'requirement_doc',      // 需求文档
  DESIGN_DOC: 'design_doc',                // 设计文档
  PROGRESS_REPORT: 'progress_report',      // 进展报告
  TEST_REPORT: 'test_report',              // 测试报告
  OTHER: 'other'                           // 其他
} as const

export type InterimResultType = typeof InterimResultType[keyof typeof InterimResultType]

/**
 * 中期成果物类型映射（中文）
 */
export const INTERIM_RESULT_TYPE_MAP: Record<InterimResultType, string> = {
  [InterimResultType.CONTRACT]: '合同',
  [InterimResultType.CONTRACT_TEMPLATE]: '合同模板',
  [InterimResultType.SIGNED_CONTRACT]: '已签署合同',
  [InterimResultType.APPLICATION]: '申报书',
  [InterimResultType.DELIVERABLE_REPORT]: '成果物报告',
  [InterimResultType.FEASIBILITY_REPORT]: '可行性报告',
  [InterimResultType.REQUIREMENT_DOC]: '需求文档',
  [InterimResultType.DESIGN_DOC]: '设计文档',
  [InterimResultType.PROGRESS_REPORT]: '进展报告',
  [InterimResultType.TEST_REPORT]: '测试报告',
  [InterimResultType.OTHER]: '其他'
}

/**
 * 附件信息
 */
export interface Attachment {
  id: string
  name: string
  url: string
  size?: number
  ext?: string
  uploadedAt?: string
}

/**
 * 中期成果物接口
 */
export interface InterimResult {
  id: string

  // 项目信息
  projectId: string
  projectName: string
  projectCode?: string
  projectPhase: string

  // 成果物信息
  name: string
  type: InterimResultType
  typeLabel: string
  description?: string

  // 附件
  attachments: Attachment[]

  // 人员信息
  submitter?: string
  submitterDept?: string

  // 时间信息
  submittedAt: string
  syncedAt: string

  // 来源标识
  source: 'process_system' | 'contract_template' | 'signed_contract' | 'deliverable_report'
  sourceRef: string
  sourceUrl?: string

  // 业务标识
  tags?: string[]
  status?: string
}

/**
 * 项目树节点
 */
export interface ProjectTreeNode {
  id: string
  label: string
  type: 'year' | 'project' | 'category'
  count: number
  children?: ProjectTreeNode[]
  projectId?: string
  year?: string
  categoryType?: InterimResultType
}

/**
 * 中期成果物统计数据
 */
export interface InterimResultStats {
  totalProjects: number
  totalResults: number
  byType: Record<string, number>
  byYear: Record<string, number>
  recentSyncTime?: string
}
