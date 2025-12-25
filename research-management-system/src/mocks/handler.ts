import { users, results, resultTypes, projects, uploads, nextId, demands, dashboardAnalytics, crawlerSources, crawlerSettings, resultAccessRequests, interimResults } from './data'

const now = () => new Date().toISOString().slice(0, 10)
const nowWithTime = () => new Date().toISOString().replace('T', ' ').slice(0, 19)

// Strapi 风格的成果类型/字段定义数据，支持增删改查
const achievementTypesData = initializeAchievementTypes()
const achievementFieldDefsData = initializeAchievementFieldDefs()

export function handleMockRequest(config: Record<string, any> = {}) {
  const method = (config.method || 'get').toLowerCase()
  const url = (config.url || '').split('?')[0]
  const params = config.params || {}
  const body = parseBody(config.data)
  const token = extractToken(config)
  const currentUser = getUserByToken(token)

  console.log('Mock Request:', method, url, body)

  // 认证相关
  if (method === 'post' && url === '/auth/login') {
    const user = users.find(
      (u) => u.username === body.username && u.password === body.password
    )
    if (!user) return fail(401, '账号或密码错误')
    return success({
      token: user.token,
      user: stripUser(user)
    })
  }

  if (method === 'post' && url === '/auth/logout') {
    return success(true, '已退出')
  }

  if (method === 'get' && url === '/auth/current') {
    if (!currentUser) return fail(401, '未登录')
    return success(stripUser(currentUser))
  }

  // 项目
  if (method === 'get' && url === '/projects') {
    const keyword = params.keyword || params.keywords || ''
    let list = [...projects]
    if (keyword) {
      list = list.filter(
        (item) =>
          item.name.includes(keyword) ||
          item.code.includes(keyword) ||
          item.description?.includes(keyword)
      )
    }
    return success(list)
  }

  if (method === 'get' && /^\/projects\/[^/]+$/.test(url)) {
    const id = url.split('/')[2]
    const project = projects.find((item) => item.id === id || item.code === id)
    if (!project) return fail(404, '未找到项目')
    return success(project)
  }

  if (method === 'post' && url === '/projects') {
    if (!currentUser) return fail(401, '未登录')
    const newProject = {
      ...body,
      id: nextId('p')
    }
    projects.unshift(newProject)
    return success(newProject, '项目创建成功')
  }

  // 统计
  if (method === 'get' && url === '/results/statistics') {
    return success(buildStatistics(results))
  }

  if (method === 'get' && url === '/results/my-statistics') {
    if (!currentUser) return fail(401, '未登录')
    const mine = results.filter((item) => item.createdBy === currentUser.name)
    return success(buildStatistics(mine))
  }

  if (method === 'get' && url === '/results/advanced-distribution') {
    const dimension = params.dimension || 'type'
    const items = dashboardAnalytics.distribution[dimension] || []
    const indexLevelItems = dashboardAnalytics.distribution.indexLevel || []
    return success({
      dimension,
      items,
      indexLevelItems
    })
  }

  if (method === 'get' && url === '/results/stacked-trend') {
    const dimension = params.dimension || 'type'
    const range = params.range || '5y'
    const trend = dashboardAnalytics.stackedTrend[dimension] || dashboardAnalytics.stackedTrend.type
    return success({
      dimension,
      range,
      ...sliceTrendByRange(trend, range)
    })
  }

  if (method === 'get' && url === '/results/keywords') {
    const range = params.range || '1y'
    return success({
      range,
      nodes: dashboardAnalytics.keywordGraph.nodes,
      links: dashboardAnalytics.keywordGraph.links
    })
  }

  // 成果类型
  if (method === 'get' && url === '/result-types') {
    return success(resultTypes)
  }

  // 成果类型 (Strapi 风格接口，兼容 /achievement-types 与 /api/achievement-types)
  const isAchievementTypes = url === '/achievement-types' || url === '/api/achievement-types'
  if (method === 'get' && isAchievementTypes) {
    const excludeDeleted = params['filters[is_delete][$ne]']
    const filtered = achievementTypesData.filter((item) => {
      if (excludeDeleted === undefined || excludeDeleted === null) return true
      return item.is_delete !== Number(excludeDeleted)
    })
    return strapiListResponse(filtered, params)
  }

  if (method === 'post' && isAchievementTypes) {
    const payload = body?.data || {}
    const documentId = payload.documentId || payload.type_code || nextId('rt')
    const newType = {
      id: achievementTypesData.length + 1,
      documentId,
      type_name: payload.type_name || payload.name || '',
      type_code: payload.type_code || payload.code || documentId,
      description: payload.description || '',
      is_delete: payload.is_delete ?? 0,
      createdAt: now(),
      updatedAt: now(),
      publishedAt: now()
    }
    achievementTypesData.push(newType)
    resultTypes.push({
      id: documentId,
      name: newType.type_name || newType.type_code,
      code: newType.type_code,
      description: newType.description,
      enabled: newType.is_delete === 0,
      fields: []
    })
    return { data: newType, meta: {} }
  }

  if (method === 'put' && (url.startsWith('/achievement-types/') || url.startsWith('/api/achievement-types/'))) {
    const documentId = url.split('/')[2]
    const payload = body?.data || {}
    const target = achievementTypesData.find((item) => item.documentId === documentId)
    if (!target) return fail(404, '未找到成果类型')

    target.type_name = payload.type_name ?? target.type_name
    target.type_code = payload.type_code ?? target.type_code
    target.description = payload.description ?? target.description
    target.is_delete = payload.is_delete ?? target.is_delete
    target.updatedAt = now()

    const baseType = resultTypes.find((item) => item.id === documentId)
    if (baseType) {
      baseType.name = target.type_name
      baseType.code = target.type_code
      baseType.description = target.description
      baseType.enabled = target.is_delete === 0
    }

    return { data: target, meta: {} }
  }

  // 成果类型字段 (Strapi 风格接口，兼容 /achievement-field-defs 与 /api/achievement-field-defs)
  const isAchievementFieldDefs = url === '/achievement-field-defs' || url === '/api/achievement-field-defs'
  if (method === 'get' && isAchievementFieldDefs) {
    const typeFilterKey = 'filters[achievement_type_id][documentId][$eq]'
    const typeId = params[typeFilterKey]
    const excludeDeleted = params['filters[is_delete][$ne]']

    const filtered = achievementFieldDefsData.filter((item) => {
      if (typeId && item.achievement_type_id !== typeId) return false
      if (excludeDeleted !== undefined && excludeDeleted !== null && item.is_delete === Number(excludeDeleted)) return false
      return true
    })

    return strapiListResponse(filtered, params)
  }

  if (method === 'post' && isAchievementFieldDefs) {
    const payload = body?.data || {}
    const achievementTypeId = payload.achievement_type_id || payload.achievement_type || ''
    const documentId = payload.documentId || `${achievementTypeId || 'field'}-${nextId('afd')}`
    const newField = {
      id: achievementFieldDefsData.length + 1,
      documentId,
      achievement_type_id: achievementTypeId,
      field_code: payload.field_code,
      field_name: payload.field_name,
      field_type: (payload.field_type || 'TEXT').toString().toUpperCase(),
      is_required: Number(payload.is_required || 0),
      description: payload.description || '',
      is_delete: payload.is_delete ?? 0,
      createdAt: now(),
      updatedAt: now(),
      publishedAt: now()
    }
    achievementFieldDefsData.push(newField)
    return { data: newField, meta: {} }
  }

  if (method === 'put' && (url.startsWith('/achievement-field-defs/') || url.startsWith('/api/achievement-field-defs/'))) {
    const documentId = url.split('/')[2]
    const payload = body?.data || {}
    const target = achievementFieldDefsData.find((item) => item.documentId === documentId)
    if (!target) return fail(404, '未找到字段定义')

    target.field_code = payload.field_code ?? target.field_code
    target.field_name = payload.field_name ?? target.field_name
    target.field_type = payload.field_type ? payload.field_type.toString().toUpperCase() : target.field_type
    target.is_required = payload.is_required ?? target.is_required
    target.is_delete = payload.is_delete ?? target.is_delete
    target.achievement_type_id = payload.achievement_type_id || payload.achievement_type || target.achievement_type_id
    target.description = payload.description ?? target.description
    target.updatedAt = now()

    return { data: target, meta: {} }
  }

  if (method === 'get' && url.startsWith('/result-types/')) {
    const id = url.split('/')[2]
    const type = resultTypes.find((item) => item.id === id || item.code === id)
    return success(type || null)
  }

  // 成果列表
  if (method === 'get' && url === '/results') {
    return success(pageResults(results, params))
  }

  if (method === 'get' && url === '/results/my') {
    if (!currentUser) return fail(401, '未登录')
    const mine = results.filter((item) => item.createdBy === currentUser.name)
    return success(pageResults(mine, params))
  }

  // 成果详情（放在列表后以避免与统计接口冲突）
  if (method === 'get' && /^\/results\/[^/]+$/.test(url)) {
    const id = url.split('/')[2]
    const detail = results.find((item) => item.id === id)
    if (!detail) return fail(404, '未找到成果')
    return success(detail)
  }

  if (method === 'post' && /^\/results\/[^/]+\/access-requests$/.test(url)) {
    if (!currentUser) return fail(401, '未登录')
    const id = url.split('/')[2]
    const detail = results.find((item) => item.id === id)
    if (!detail) return fail(404, '未找到成果')
    const reason = (body?.reason || '').toString().trim()
    if (!reason) return fail(400, '请填写申请理由')
    if (detail.permissionStatus === 'full' || detail.accessRequestStatus === 'approved') {
      return fail(400, '已拥有全文权限，无需申请')
    }
    if (detail.accessRequestStatus === 'pending') {
      return fail(400, '申请已提交，等待审核')
    }
    if (detail.canRequestAccess === false && detail.accessRequestStatus !== 'rejected') {
      return fail(400, '当前状态不可申请')
    }

    const createdAt = nowWithTime()
    const record = {
      id: nextId('req'),
      resultId: id,
      resultTitle: detail.title,
      resultType: detail.type,
      projectName: detail.projectName,
      visibility: detail.visibility,
      userId: currentUser.id,
      userName: currentUser.name,
      reason,
      status: 'pending',
      createdAt
    }
    resultAccessRequests.unshift(record)
    detail.accessRequestStatus = 'pending'
    detail.permissionStatus = detail.permissionStatus || 'summary'
    detail.lastRequestAt = createdAt
    detail.rejectedReason = ''
    detail.canRequestAccess = false
    return success(record, '申请已提交，等待管理员审核')
  }

  if (method === 'get' && url === '/results/access-requests') {
    let list = resultAccessRequests.map((item) => {
      const target = results.find((r) => r.id === item.resultId)
      return {
        ...item,
        resultTitle: item.resultTitle || target?.title || item.resultId,
        resultType: item.resultType || target?.type,
        projectName: item.projectName || target?.projectName,
        visibility: item.visibility || target?.visibility,
        resultStatus: target?.status
      }
    })
    if (params.keyword) {
      list = list.filter(
        (item) =>
          item.resultTitle?.includes(params.keyword) ||
          item.userName?.includes(params.keyword) ||
          item.reason?.includes(params.keyword)
      )
    }
    if (params.status) {
      const filters = Array.isArray(params.status) ? params.status : [params.status]
      list = list.filter((item) => filters.includes(item.status))
    }
    const page = Number(params.page) || 1
    const pageSize = Number(params.pageSize) || 10
    const total = list.length
    const start = (page - 1) * pageSize
    const end = start + pageSize
    return success({
      list: list.slice(start, end),
      total,
      page,
      pageSize
    })
  }

  const reviewAccessMatch = url.match(/^\/results\/access-requests\/([^/]+)\/review$/)
  if (method === 'post' && reviewAccessMatch) {
    if (!currentUser) return fail(401, '未登录')
    const requestId = reviewAccessMatch[1]
    const record = resultAccessRequests.find((req) => req.id === requestId)
    if (!record) return fail(404, '未找到申请')
    const action = body?.action
    if (!['approve', 'reject'].includes(action)) return fail(400, '无效的操作类型')
    const target = results.find((item) => item.id === record.resultId)
    const reviewedAt = nowWithTime()
    record.status = action === 'approve' ? 'approved' : 'rejected'
    record.reviewedAt = reviewedAt
    record.reviewer = currentUser.name
    record.comment = body?.comment || ''

    if (target) {
      record.resultTitle = target.title
      record.resultType = target.type
      record.projectName = target.projectName
      record.visibility = target.visibility
      target.lastRequestAt = reviewedAt
      if (action === 'approve') {
        target.permissionStatus = 'full'
        target.accessRequestStatus = 'approved'
        target.canRequestAccess = false
        target.rejectedReason = ''
      } else {
        target.permissionStatus = target.permissionStatus || 'summary'
        target.accessRequestStatus = 'rejected'
        target.canRequestAccess = true
        target.rejectedReason = record.comment || '管理员已拒绝申请'
      }
    }

    const message = action === 'approve' ? '已通过申请' : '已拒绝申请'
    return success(record, message)
  }

  // 创建/草稿
  if (method === 'post' && (url === '/results' || url === '/results/')) {
    try {
      if (!currentUser) return fail(401, '未登录')
      const typeId = body.typeId || body.type
      const projectFields = mergeProjectInfo(body)
      const source = body.source || 'manual_upload'
      const isProcess = source === 'process_system'
      const baseStatus = isProcess ? 'pending' : (body.status || 'pending')
      const newResult = {
        ...body,
        type: typeId,
        typeId,
        id: nextId(),
        source,
        status: baseStatus,
        assignedReviewers: isProcess ? [] : body.assignedReviewers || [],
        createdBy: isProcess ? '过程管理系统' : currentUser.name,
        createdAt: now(),
        updatedAt: now(),
        reviewHistory: [],
        formatChecked: isProcess ? false : true,
        formatStatus: isProcess ? 'pending' : 'passed',
        formatNote: '',
        ...projectFields
      }
      results.unshift(newResult)
      return success(newResult, '创建成功')
    } catch (e: any) {
      console.error('Mock create result error:', e)
      return fail(500, 'Mock创建失败: ' + e.message)
    }
  }

  if (method === 'post' && url === '/results/draft') {
    if (!currentUser) return fail(401, '未登录')
    if (body.source === 'process_system') return fail(400, '过程管理系统成果不支持草稿')
    const typeId = body.typeId || body.type
    const projectFields = mergeProjectInfo(body)
    const draft = {
      ...body,
      type: typeId,
      typeId,
      id: nextId(),
      status: 'draft',
      createdBy: currentUser.name,
      createdAt: now(),
      updatedAt: now(),
      reviewHistory: [],
      ...projectFields
    }
    results.unshift(draft)
    return success(draft, '草稿已保存')
  }

  // 更新
  if (method === 'put' && /^\/results\/[^/]+$/.test(url)) {
    const id = url.split('/')[2]
    const index = results.findIndex((item) => item.id === id)
    if (index === -1) return fail(404, '未找到成果')
    const typeId = body.typeId || body.type || results[index].typeId
    const projectFields = mergeProjectInfo(body, results[index])
    results[index] = { ...results[index], ...body, ...projectFields, typeId, type: typeId, updatedAt: now() }
    return success(results[index], '更新成功')
  }

  // 删除
  if (method === 'delete' && /^\/results\/[^/]+$/.test(url)) {
    const id = url.split('/')[2]
    const index = results.findIndex((item) => item.id === id)
    if (index === -1) return fail(404, '未找到成果')
    results.splice(index, 1)
    return success(true, '删除成功')
  }

  // 提交审核
  if (method === 'post' && /^\/results\/[^/]+\/submit$/.test(url)) {
    const id = url.split('/')[2]
    const item = results.find((r) => r.id === id)
    if (!item) return fail(404, '未找到成果')
    if (item.source === 'process_system') return fail(400, '过程管理系统成果无需提交，需分配审核')
    item.status = 'reviewing'
    item.updatedAt = now()
    return success(true, '已提交审核')
  }

  // 分配审核人
  if (method === 'post' && /^\/results\/[^/]+\/assign-reviewers$/.test(url)) {
    const id = url.split('/')[2]
    const item = results.find((r) => r.id === id)
    if (!item) return fail(404, '未找到成果')
    const reviewers = Array.isArray(body.reviewers) ? body.reviewers : (body.reviewers ? String(body.reviewers).split(',').map((r) => r.trim()).filter(Boolean) : [])
    item.assignedReviewers = reviewers
    item.status = 'reviewing'
    item.updatedAt = now()
    return success(item, '已分配审核人')
  }

  // 审核
  if (method === 'post' && /^\/results\/[^/]+\/review$/.test(url)) {
    const id = url.split('/')[2]
    const item = results.find((r) => r.id === id)
    if (!item) return fail(404, '未找到成果')
    const reviewerName = currentUser?.name || '审核专家'
    const action = body.action === 'reject' ? 'reject' : 'approve'
    item.status = action === 'approve' ? 'published' : 'rejected'
    item.updatedAt = now()
    item.reviewHistory = item.reviewHistory || []
    item.reviewHistory.push({
      id: nextId('rev'),
      reviewerId: currentUser?.id || '2',
      reviewerName,
      action,
      comment: body.comment || '',
      createdAt: now()
    })
    return success(true, '审核完成')
  }

  // 退回修改
  if (method === 'post' && /^\/results\/[^/]+\/request-changes$/.test(url)) {
    const id = url.split('/')[2]
    const item = results.find((r) => r.id === id)
    if (!item) return fail(404, '未找到成果')
    item.status = 'revision'
    item.updatedAt = now()
    item.reviewHistory = item.reviewHistory || []
    item.reviewHistory.push({
      id: nextId('rev'),
      reviewerId: currentUser?.id || '2',
      reviewerName: currentUser?.name || '审核专家',
      action: 'request_changes',
      comment: body.comment || '请按要求修改后重新提交',
      createdAt: now()
    })
    return success(true, '已退回修改')
  }

  // 格式审查通过
  if (method === 'post' && /^\/results\/[^/]+\/format-check$/.test(url)) {
    const id = url.split('/')[2]
    const item = results.find((r) => r.id === id)
    if (!item) return fail(404, '未找到成果')
    item.formatChecked = true
    item.formatStatus = 'passed'
    item.formatNote = ''
    item.updatedAt = now()
    return success(item, '格式审查已通过')
  }

  // 格式审查不通过
  if (method === 'post' && /^\/results\/[^/]+\/format-reject$/.test(url)) {
    const id = url.split('/')[2]
    const item = results.find((r) => r.id === id)
    if (!item) return fail(404, '未找到成果')
    item.formatChecked = false
    item.formatStatus = 'failed'
    item.formatNote = body.reason || '格式问题待修复'
    item.updatedAt = now()
    return success(item, '已标记格式不通过')
  }

  // 待审核/审核中看板
  if (method === 'get' && url === '/results/review-backlog') {
    const pending = results.filter((r) => r.status === 'pending')
    const reviewing = results.filter((r) => r.status === 'reviewing')
    const processPending = pending.filter((r) => r.source === 'process_system')
    const manualPending = pending.filter((r) => r.source === 'manual_upload')
    return success({
      pending,
      reviewing,
      summary: {
        pending: pending.length,
        reviewing: reviewing.length,
        processPending: processPending.length,
        manualPending: manualPending.length
      }
    })
  }

  // 智能补全
  if (method === 'get' && url === '/results/auto-fill') {
    const value = params.value || body.value
    const type = params.type || body.type
    const sample = {
      title: `智能推荐的成果标题${type ? `（${type}）` : ''}`,
      authors: value ? [value, '合作者A'] : ['作者A', '作者B'],
      abstract: '这是根据标识符自动补全的摘要示例，可根据需要修改。',
      keywords: ['自动补全', '示例'],
      year: now().slice(0, 4)
    }
    return success(sample, '已补全')
  }

  // 企业需求列表
  if (method === 'get' && url === '/demand') {
    return success(pageDemands(demands, params))
  }

  // 企业需求详情
  if (method === 'get' && /^\/demand\/[^/]+$/.test(url)) {
    const id = url.split('/')[2]
    const item = demands.find((d) => d.id === id)
    if (!item) return fail(404, '未找到需求')
    return success(item)
  }

  // 重新匹配
  if (method === 'post' && /^\/demand\/[^/]+\/rematch$/.test(url)) {
    const id = url.split('/')[2]
    const item = demands.find((d) => d.id === id)
    if (!item) return fail(404, '未找到需求')
    item.matches = (item.matches || []).map((m) => {
      const noise = (Math.random() - 0.5) * 0.1 // -0.05 ~ 0.05
      const score = Math.min(0.98, Math.max(0.1, (m.matchScore || 0) + noise))
      return {
        ...m,
        matchScore: Number(score.toFixed(2)),
        updatedAt: now()
      }
    }).sort((a, b) => (b.matchScore || 0) - (a.matchScore || 0))
    item.bestMatchScore = item.matches[0]?.matchScore || 0
    item.status = item.matches.length ? 'matched' : 'unmatched'
    return success(item, '已重新匹配')
  }

  // 爬虫数据源列表
  if (method === 'get' && url === '/system/crawler-sources') {
    return success({
      list: crawlerSources,
      total: crawlerSources.length
    })
  }

  // 新建数据源
  if (method === 'post' && url === '/system/crawler-sources') {
    const newSource = {
      ...body,
      id: nextId('cs'),
      lastRunAt: body.lastRunAt || '-',
      lastSuccessAt: body.lastSuccessAt || '-',
      status: body.status || 'idle'
    }
    crawlerSources.unshift(newSource)
    return success(newSource, '数据源已创建')
  }

  // 更新数据源
  const updateSourceMatch = url.match(/^\/system\/crawler-sources\/([^/]+)$/)
  if (method === 'put' && updateSourceMatch) {
    const id = updateSourceMatch[1]
    const index = crawlerSources.findIndex((item) => item.id === id)
    if (index === -1) return fail(404, '未找到数据源')
    crawlerSources[index] = {
      ...crawlerSources[index],
      ...body
    }
    return success(crawlerSources[index], '数据源已更新')
  }

  // 删除数据源
  const deleteSourceMatch = url.match(/^\/system\/crawler-sources\/([^/]+)$/)
  if (method === 'delete' && deleteSourceMatch) {
    const id = deleteSourceMatch[1]
    const index = crawlerSources.findIndex((item) => item.id === id)
    if (index === -1) return fail(404, '未找到数据源')
    crawlerSources.splice(index, 1)
    return success(true, '数据源已删除')
  }

  // 测试数据源
  const testSourceMatch = url.match(/^\/system\/crawler-sources\/([^/]+)\/test$/)
  if (method === 'post' && testSourceMatch) {
    const id = testSourceMatch[1]
    const target = crawlerSources.find((item) => item.id === id)
    if (!target) return fail(404, '未找到数据源')
    const pass = Math.random() > 0.2
    target.lastRunAt = now()
    if (pass) {
      target.status = 'healthy'
      target.lastSuccessAt = now()
      target.failureReason = ''
      return success(target, '连接成功')
    } else {
      target.status = 'error'
      target.failureReason = '模拟连接失败，请检查配置'
      return fail(500, '连接失败')
    }
  }

  // 系统设置
  if (method === 'get' && url === '/system/crawler-settings') {
    return success(crawlerSettings)
  }

  if (method === 'put' && url === '/system/crawler-settings') {
    Object.assign(crawlerSettings, body)
    return success(crawlerSettings, '设置已保存')
  }

  // 上传
  if (method === 'post' && url === '/upload') {
    const file = body?.get ? body.get('file') : null
    const uploadResult = {
      url: '/mock/upload/' + nextId('file'),
      name: file?.name || 'mock-file',
      size: file?.size || 1024 * 100
    }
    uploads.push(uploadResult)
    return success(uploadResult, '上传成功')
  }

  // ==================== 中期成果物接口 ====================
  
  // 获取中期成果物统计
  if (method === 'get' && url === '/interim-results/stats') {
    const stats = buildInterimStats(interimResults)
    return success(stats)
  }

  // 获取中期成果物列表
  if (method === 'get' && url === '/interim-results') {
    return success(pageInterimResults(interimResults, params))
  }

  // 获取中期成果物详情
  if (method === 'get' && /^\/interim-results\/[^/]+$/.test(url)) {
    const id = url.split('/')[2]
    const item = interimResults.find((r) => r.id === id)
    if (!item) return fail(404, '未找到中期成果物')
    return success(item)
  }

  // 手动同步中期成果物
  if (method === 'post' && url === '/interim-results/sync') {
    const projectId = body?.projectId
    return success({
      syncCount: projectId ? 2 : interimResults.length,
      syncTime: nowWithTime()
    }, '同步完成')
  }

  // 批量下载
  if (method === 'post' && url === '/interim-results/batch-download') {
    // 模拟返回blob
    return success({ message: '批量下载功能需要后端支持' })
  }

  // 导出列表
  if (method === 'get' && url === '/interim-results/export') {
    // 模拟返回blob
    return success({ message: '导出功能需要后端支持' })
  }

  console.warn('Mock request not handled:', method, url)
  return fail(404, `Mock接口不存在: ${method} ${url}`)
}

function initializeAchievementTypes() {
  return resultTypes.map((t, index) => ({
    id: index + 1,
    documentId: t.id,
    type_name: t.name,
    type_code: t.code,
    description: t.description,
    is_delete: t.enabled ? 0 : 1,
    createdAt: now(),
    updatedAt: now(),
    publishedAt: now()
  }))
}

function initializeAchievementFieldDefs() {
  const list: any[] = []

  resultTypes.forEach((type) => {
    ;(type.fields || []).forEach((field, index) => {
      list.push({
        id: list.length + 1,
        documentId: `${type.id}-field-${field.id || index + 1}`,
        achievement_type_id: type.id,
        field_code: field.name,
        field_name: field.label,
        field_type: (field.type || 'text').toString().toUpperCase(),
        is_required: field.required ? 1 : 0,
        description: 'description' in field ? (field as { description?: string }).description || '' : '',
        is_delete: 0,
        createdAt: now(),
        updatedAt: now(),
        publishedAt: now()
      })
    })
  })

  return list
}

function strapiListResponse(list: any[], params: Record<string, any> = {}) {
  const page = Number(params['pagination[page]']) || 1
  const pageSize = Number(params['pagination[pageSize]']) || list.length || 10
  const start = (page - 1) * pageSize
  const end = start + pageSize
  const total = list.length
  const data = list.slice(start, end)

  return {
    data,
    meta: {
      pagination: {
        page,
        pageSize,
        pageCount: Math.max(Math.ceil(total / pageSize), 1),
        total
      }
    }
  }
}

function parseBody(raw) {
  if (!raw) return {}
  if (typeof raw === 'string') {
    try {
      return JSON.parse(raw)
    } catch (e) {
      return {}
    }
  }
  return raw
}

function extractToken(config) {
  const auth = config.headers?.Authorization || ''
  if (auth.startsWith('Bearer ')) return auth.replace('Bearer ', '')
  if (config.params?.token) return config.params.token
  return ''
}

function getUserByToken(token) {
  if (!token) return null
  return users.find((u) => u.token === token) || null
}

function stripUser(user) {
  const { password, ...rest } = user || {}
  return rest
}

function success(data, message = 'success') {
  return { code: 200, message, data }
}

function fail(code = 400, message = '请求失败') {
  return { code, message, data: null }
}

function pageResults(sourceList, params) {
  const keyword = params.keyword || params.keywords || ''
  const status = params.status || ''
  const type = params.type || params.typeId || ''
  const author = params.author || ''
  const projectId = params.projectId || params.project || ''
  const sourceFilter = params.source || ''
  const projectPhase = params.projectPhase || params.phase || ''
  const yearRange = params.yearRange || params.years || []
  const page = Number(params.page) || 1
  const pageSize = Number(params.pageSize) || 10

  let list = [...sourceList]

  if (keyword) {
    list = list.filter(
      (item) =>
        item.title.includes(keyword) ||
        item.abstract?.includes(keyword) ||
        item.authors?.some((a) => a.includes(keyword))
    )
  }

  if (status) {
    const statusList = Array.isArray(status) ? status : [status]
    list = list.filter((item) => statusList.includes(item.status))
  }

  if (type) {
    list = list.filter((item) => item.type === type || item.typeId === type)
  }

  if (author) {
    list = list.filter((item) => item.authors?.some((a) => a.includes(author)))
  }

  if (sourceFilter) {
    list = list.filter((item) => item.source === sourceFilter)
  }

  if (projectPhase) {
    list = list.filter((item) => item.projectPhase === projectPhase)
  }

  if (projectId) {
    list = list.filter((item) => item.projectId === projectId)
  }

  if (Array.isArray(yearRange) && yearRange.length === 2) {
    const [start, end] = yearRange
    list = list.filter((item) => {
      const year = Number(item.year)
      return (!start || year >= Number(start)) && (!end || year <= Number(end))
    })
  }

  const total = list.length
  const start = (page - 1) * pageSize
  const end = start + pageSize

  return {
    list: list.slice(start, end),
    total,
    page,
    pageSize
  }
}

function sliceTrendByRange(trend: any = {}, range = '5y') {
  const timeline = Array.isArray(trend.timeline) ? [...trend.timeline] : []
  const stacks = Array.isArray(trend.stacks)
    ? trend.stacks.map((item) => ({ ...item, data: Array.isArray(item.data) ? [...item.data] : [] }))
    : []
  const citations = Array.isArray(trend.citations) ? [...trend.citations] : []

  if (range === '3y' && timeline.length > 3) {
    const start = timeline.length - 3
    return {
      timeline: timeline.slice(start),
      stacks: stacks.map((item) => ({ ...item, data: item.data.slice(start) })),
      citations: citations.slice(start)
    }
  }

  return { timeline, stacks, citations }
}

function buildStatistics(source) {
  const totalResults = source.length
  const paperCount = source.filter((item) => item.type === 'paper').length
  const patentCount = source.filter((item) => item.type === 'patent').length
  const monthlyNew = source.filter((item) => (item.createdAt || '').startsWith(now().slice(0, 7))).length

  const typeMap = {}
  source.forEach((item) => {
    const key = item.type || item.typeId
    typeMap[key] = (typeMap[key] || 0) + 1
  })
  
  const typeNameMap = {
    paper: '学术论文',
    patent: '发明专利',
    software: '软件著作权'
  }

  const typeDistribution = Object.entries(typeMap).map(([key, value]) => ({
    name: typeNameMap[key] || key,
    value
  }))

  const currentYear = Number(now().slice(0, 4))
  const yearlyTrend = []
  for (let i = 4; i >= 0; i--) {
    const year = (currentYear - i).toString()
    const count = source.filter((item) => String(item.year) === year).length
    yearlyTrend.push({ year, count })
  }

  return {
    totalResults,
    paperCount,
    patentCount,
    monthlyNew,
    typeDistribution,
    yearlyTrend
  }
}

function mergeProjectInfo(body: Record<string, any> = {}, fallback: Record<string, any> = {}) {
  const projectId = body.projectId || fallback.projectId || ''
  const project = findProject(projectId)
  return {
    projectId,
    projectName: body.projectName || project?.name || fallback.projectName || '',
    projectCode: body.projectCode || project?.code || fallback.projectCode || ''
  }
}

function findProject(id) {
  if (!id) return null
  return projects.find((item) => item.id === id || item.code === id) || null
}

function pageDemands(source, params) {
  const keyword = params.keyword || ''
  const industry = params.industry || ''
  const region = params.region || ''
  const sourceCategory = params.sourceCategory || ''
  const status = params.status || ''
  const page = Number(params.page) || 1
  const pageSize = Number(params.pageSize) || 10

  let list = [...source]

  if (keyword) {
    list = list.filter(
      (item) =>
        item.title.includes(keyword) ||
        item.summary?.includes(keyword) ||
        item.industry?.includes(keyword) ||
        item.region?.includes(keyword) ||
        item.tags?.some((t) => t.includes(keyword))
    )
  }

  if (industry) {
    list = list.filter((item) => item.industry === industry)
  }

  if (region) {
    list = list.filter((item) => item.region === region)
  }

  if (sourceCategory) {
    list = list.filter((item) => item.sourceCategory === sourceCategory)
  }

  if (status) {
    list = list.filter((item) => item.status === status)
  }

  const total = list.length
  const start = (page - 1) * pageSize
  const end = start + pageSize

  return {
    list: list.slice(start, end),
    total,
    page,
    pageSize
  }
}

// ==================== 中期成果物辅助函数 ====================

function buildInterimStats(source) {
  const totalProjects = new Set(source.map((item) => item.projectId)).size
  const totalResults = source.length
  
  const byType = {}
  source.forEach((item) => {
    const key = item.typeLabel || item.type
    byType[key] = (byType[key] || 0) + 1
  })
  
  const byYear = {}
  source.forEach((item) => {
    const year = (item.submittedAt || '').slice(0, 4)
    if (year) {
      byYear[year] = (byYear[year] || 0) + 1
    }
  })
  
  const recentSyncTime = source.length > 0 
    ? source.sort((a, b) => (b.syncedAt || '').localeCompare(a.syncedAt || ''))[0]?.syncedAt
    : undefined
  
  return {
    totalProjects,
    totalResults,
    byType,
    byYear,
    recentSyncTime
  }
}

function pageInterimResults(source, params) {
  const projectId = params.projectId || ''
  const type = params.type || ''
  const year = params.year || ''
  const keyword = params.keyword || ''
  const page = Number(params.page) || 1
  const pageSize = Number(params.pageSize) || 10

  let list = [...source]

  if (projectId) {
    list = list.filter((item) => item.projectId === projectId)
  }

  if (type) {
    list = list.filter((item) => item.type === type)
  }

  if (year) {
    list = list.filter((item) => (item.submittedAt || '').startsWith(year))
  }

  if (keyword) {
    list = list.filter(
      (item) =>
        item.name.includes(keyword) ||
        item.projectName?.includes(keyword) ||
        item.description?.includes(keyword) ||
        item.submitter?.includes(keyword)
    )
  }

  const total = list.length
  const start = (page - 1) * pageSize
  const end = start + pageSize

  return {
    list: list.slice(start, end),
    total,
    page,
    pageSize
  }
}
