import type { InterimResult, ProjectTreeNode, InterimResultType, INTERIM_RESULT_TYPE_MAP } from '@/types'

/**
 * 格式化文件大小
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i]
}

/**
 * 获取文件扩展名
 */
export function getFileExtension(filename: string): string {
  return filename.split('.').pop()?.toLowerCase() || ''
}

/**
 * 获取文件图标名称（Element Plus Icons）
 */
export function getFileIconName(filename: string): string {
  const ext = getFileExtension(filename)
  const iconMap: Record<string, string> = {
    pdf: 'Document',
    doc: 'Document',
    docx: 'Document',
    xls: 'Document',
    xlsx: 'Document',
    ppt: 'Document',
    pptx: 'Document',
    txt: 'Document',
    zip: 'FolderOpened',
    rar: 'FolderOpened',
    jpg: 'Picture',
    jpeg: 'Picture',
    png: 'Picture',
    gif: 'Picture',
    dwg: 'Document'
  }
  return iconMap[ext] || 'Document'
}

/**
 * 构建项目树（按年份分组）
 */
export function buildProjectTree(results: InterimResult[]): ProjectTreeNode[] {
  const tree: ProjectTreeNode[] = []
  const yearMap = new Map<string, Map<string, InterimResult[]>>()
  
  // 按年份和项目分组
  results.forEach(result => {
    const year = result.submittedAt.substring(0, 4)
    
    if (!yearMap.has(year)) {
      yearMap.set(year, new Map())
    }
    
    const projectMap = yearMap.get(year)!
    if (!projectMap.has(result.projectId)) {
      projectMap.set(result.projectId, [])
    }
    
    projectMap.get(result.projectId)!.push(result)
  })
  
  // 构建树结构
  Array.from(yearMap.keys()).sort().reverse().forEach(year => {
    const projectMap = yearMap.get(year)!
    const yearNode: ProjectTreeNode = {
      id: `year-${year}`,
      label: `${year}年项目`,
      type: 'year',
      count: Array.from(projectMap.values()).flat().length,
      year,
      children: []
    }
    
    Array.from(projectMap.entries()).forEach(([projectId, projectResults]) => {
      const projectName = projectResults[0].projectName
      yearNode.children!.push({
        id: `project-${projectId}`,
        label: `${projectName} (${projectResults.length})`,
        type: 'project',
        count: projectResults.length,
        projectId
      })
    })
    
    tree.push(yearNode)
  })
  
  return tree
}

/**
 * 构建类型分类树
 */
export function buildTypeTree(results: InterimResult[]): ProjectTreeNode[] {
  const typeMap = new Map<string, InterimResult[]>()
  
  results.forEach(result => {
    const typeLabel = result.typeLabel || result.type
    if (!typeMap.has(typeLabel)) {
      typeMap.set(typeLabel, [])
    }
    typeMap.get(typeLabel)!.push(result)
  })
  
  return Array.from(typeMap.entries()).map(([typeLabel, items]) => ({
    id: `type-${typeLabel}`,
    label: `${typeLabel} (${items.length})`,
    type: 'category',
    count: items.length,
    categoryType: items[0]?.type as InterimResultType
  }))
}

/**
 * 判断文件是否可预览
 */
export function canPreview(filename: string): boolean {
  const ext = getFileExtension(filename)
  return ['pdf', 'jpg', 'jpeg', 'png', 'gif', 'txt'].includes(ext)
}

/**
 * 格式化日期时间
 */
export function formatDateTime(dateTime?: string): string {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ').substring(0, 19)
}

/**
 * 格式化为简短日期
 */
export function formatShortDate(dateTime?: string): string {
  if (!dateTime) return '-'
  const date = dateTime.substring(0, 10)
  const today = new Date().toISOString().substring(0, 10)
  
  if (date === today) {
    return '今天 ' + dateTime.substring(11, 16)
  }
  
  const yesterday = new Date(Date.now() - 86400000).toISOString().substring(0, 10)
  if (date === yesterday) {
    return '昨天 ' + dateTime.substring(11, 16)
  }
  
  // 今年的日期只显示月-日
  if (date.substring(0, 4) === today.substring(0, 4)) {
    return date.substring(5) + ' ' + dateTime.substring(11, 16)
  }
  
  return date + ' ' + dateTime.substring(11, 16)
}



