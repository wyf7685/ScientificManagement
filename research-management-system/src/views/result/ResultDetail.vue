<template>
  <div class="result-detail-container">
    <!-- 顶部导航栏 -->
    <div class="top-nav">
      <div class="nav-back" @click="handleBack">
        <el-icon class="back-icon"><ArrowLeft /></el-icon>
        <span>返回列表</span>
      </div>
    </div>

    <div v-loading="loading" class="content-wrapper">
      <!-- 头部核心信息区 -->
      <header class="detail-header" v-if="result">
        <div class="header-main">
          <div class="title-row">
            <h1 class="page-title">{{ result.title }}</h1>
            <div class="status-badges">
              <el-tag :type="getStatusType(result.status)" effect="dark" class="status-tag">
                {{ getStatusText(result.status) }}
              </el-tag>
              <el-tag
                v-if="result.source"
                :type="sourceTagType(result.source)"
                effect="plain"
                class="source-tag"
              >
                {{ sourceText(result.source) }}
              </el-tag>
            </div>
          </div>

          <div class="meta-row">
            <div class="meta-item" v-if="result.authors?.length">
              <el-icon><User /></el-icon>
              <span class="meta-label">作者：</span>
              <span class="meta-value">{{ result.authors.join(', ') }}</span>
            </div>
            <div class="meta-item" v-if="result.year">
              <el-icon><Calendar /></el-icon>
              <span class="meta-label">年份：</span>
              <span class="meta-value">{{ result.year }}</span>
            </div>
            <div class="meta-item">
              <el-icon><View /></el-icon>
              <span class="meta-label">可见性：</span>
              <span class="meta-value">{{ getVisibilityText(result.visibility) }}</span>
            </div>
          </div>
        </div>

        <div class="header-actions">
          <el-button circle plain @click="loadDetail" :loading="loading">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>
      </header>

      <!-- 主要内容布局：左侧详情，右侧属性 -->
      <div class="layout-grid" v-if="result">
        <!-- 左侧：主要内容区域 -->
        <div class="main-column">
          <!-- 权限横幅 -->
          <div
            class="access-banner"
            :class="{
              'access-banner--ok': hasFullAccess,
              'access-banner--warning': !hasFullAccess
            }"
          >
            <div class="banner-icon">
              <el-icon v-if="hasFullAccess" class="icon-ok"><Unlock /></el-icon>
              <el-icon v-else class="icon-lock"><Lock /></el-icon>
            </div>
            <div class="banner-content">
              <div class="banner-title">{{ accessTitle }}</div>
              <div class="banner-desc">{{ accessDesc }}</div>
            </div>
            <div class="banner-actions">
              <el-button
                v-if="!hasFullAccess && canRequestAccess"
                type="primary"
                round
                :loading="applying"
                @click="openApplyDialog"
              >
                {{ isRejected ? '重新申请' : '申请查看全文' }}
              </el-button>
              <el-tag v-else-if="isPending" type="warning" effect="dark">审核中</el-tag>
              <el-tag v-else-if="isRejected" type="danger" effect="dark">已拒绝</el-tag>
            </div>
          </div>

          <!-- 摘要卡片 -->
          <section class="content-card abstract-section">
            <div class="section-header">
              <h3 class="section-title">摘要</h3>
            </div>
            <div class="section-body abstract-text">
              {{ result.abstract || '暂无摘要' }}
            </div>
          </section>

          <!-- 动态块级字段 (RichText, Textarea) -->
          <template v-if="blockFields.length > 0">
            <section v-for="field in blockFields" :key="field.id" class="content-card">
              <div class="section-header">
                <h3 class="section-title">{{ field.label }}</h3>
              </div>

              <div v-if="hasFullAccess" class="section-body paper-content">
                <div v-if="getFieldValueRaw(field)">
                  <div v-if="field.isHtml" v-html="getFieldValueRaw(field)" class="rich-text-display"></div>
                  <div v-else class="text-display">{{ getFieldValueRaw(field) }}</div>
                </div>
                <div v-else class="empty-text">暂无{{ field.label }}</div>
              </div>
              <div v-else class="restricted-placeholder">
                <el-icon class="lock-icon"><Lock /></el-icon>
                <p>该内容受限，请申请查看全文。</p>
              </div>
            </section>
          </template>

          <!-- 默认正文 (兼容旧数据) -->
          <section v-else-if="result?.content" class="content-card">
            <div class="section-header">
              <h3 class="section-title">正文内容</h3>
            </div>
            <div v-if="hasFullAccess" class="section-body paper-content">
               <div class="text-display">{{ result.content }}</div>
            </div>
            <div v-else class="restricted-placeholder">
              <el-icon class="lock-icon"><Lock /></el-icon>
              <p>正文内容受限，请申请查看全文。</p>
            </div>
          </section>

          <!-- 附件区 -->
          <section class="content-card attachments-section">
            <div class="section-header">
              <h3 class="section-title">附件材料</h3>
              <el-tag v-if="result.attachments?.length" type="info" round size="small">
                {{ result.attachments.length }} 个文件
              </el-tag>
            </div>

            <div v-if="hasFullAccess">
              <el-empty v-if="!result.attachments?.length" description="暂无附件" :image-size="60" />
              <div v-else class="attachment-grid">
                <div v-for="file in result.attachments" :key="file.id" class="attachment-item" @click="downloadFile(file)">
                  <div class="file-icon">
                    <el-icon><Document /></el-icon>
                  </div>
                  <div class="file-info">
                    <div class="file-name" :title="file.name">{{ file.name }}</div>
                    <div class="file-size">{{ formatFileSize(file.size) }}</div>
                  </div>
                  <div class="file-action">
                    <el-icon><Download /></el-icon>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="restricted-placeholder">
              <p>附件不可见，请先申请访问权限。</p>
            </div>
          </section>
        </div>

        <!-- 右侧：属性侧边栏 -->
        <aside class="side-column">
          <!-- 基础信息卡片 -->
          <div class="side-card">
            <h4 class="side-title">基本信息</h4>
            <div class="props-list">
              <div class="prop-item">
                <span class="prop-label">成果类型</span>
                <span class="prop-value">{{ result.type }}</span>
              </div>
              <div class="prop-item">
                <span class="prop-label">项目阶段</span>
                <span class="prop-value">{{ phaseText(result.projectPhase) || '-' }}</span>
              </div>
              <div class="prop-item" v-if="result.sourceStage">
                <span class="prop-label">来源阶段</span>
                <span class="prop-value">{{ stageText(result.sourceStage) }}</span>
              </div>
              <div class="prop-item">
                <span class="prop-label">更新时间</span>
                <span class="prop-value date-text">{{ result.syncTime || result.updatedAt }}</span>
              </div>
            </div>
          </div>

          <!-- 动态行内字段 -->
          <div class="side-card" v-if="inlineFields.length">
            <h4 class="side-title">扩展属性</h4>
            <div class="props-list">
              <template v-for="field in inlineFields" :key="field.id">
                <div class="prop-item">
                  <span class="prop-label">{{ field.label }}</span>
                  <span class="prop-value">{{ getFieldValueDisplay(field) }}</span>
                </div>
              </template>
            </div>
          </div>

          <!-- 关联项目 -->
          <div class="side-card project-card">
            <h4 class="side-title">所属项目</h4>
            <div v-if="result.projectName" class="project-info">
              <div class="project-name">{{ result.projectName }}</div>
              <div class="project-code">{{ result.projectCode }}</div>
              <el-button type="primary" link size="small" @click="goProjectFilter(result)">
                查看该项目成果 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
            <div v-else class="text-muted">无关联项目</div>
          </div>

          <!-- 关键词 -->
          <div class="side-card" v-if="result.keywords?.length">
            <h4 class="side-title">关键词</h4>
            <div class="tags-cloud">
              <el-tag
                v-for="kw in result.keywords"
                :key="kw"
                class="keyword-tag"
                type="info"
                effect="light"
              >
                {{ kw }}
              </el-tag>
            </div>
          </div>
        </aside>
      </div>
    </div>

    <!-- 申请弹窗 -->
    <el-dialog
      v-model="applyDialogVisible"
      title="申请查看全文"
      width="500px"
      align-center
      class="apply-dialog"
    >
      <div class="dialog-body-custom">
        <p class="dialog-instruction">请详细说明您申请查看该成果全文的理由，通过审核后将开放正文及附件下载权限。</p>
        <el-form label-position="top">
          <el-form-item label="申请理由" required>
            <el-input
              v-model="applyReason"
              type="textarea"
              :rows="4"
              placeholder="例如：需参考该成果进行后续研发..."
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="applyDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="applying"
            @click="handleSubmitAccessRequest"
          >
            提交申请
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft, Document, Refresh, User, Calendar,
  View, Lock, Unlock, Download, ArrowRight
} from '@element-plus/icons-vue'
import { getResult, requestResultAccess, getFieldDefsByType } from '@/api/result'
import { mapFieldType, FrontendFieldType } from '@/config/dynamicFields'
import {
  AccessPermissionStatus,
  AccessRequestStatus,
  ResultStatus,
  ResultVisibility,
  type ResearchResult
} from '@/types'
import { useUserStore } from '@/stores/user'
import { useAsyncAction } from '@/composables/useErrorHandler'
import { AppError, ErrorType } from '@/utils/errorHandler'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
type PermissionStatus = typeof AccessPermissionStatus[keyof typeof AccessPermissionStatus]
type RequestStatus = typeof AccessRequestStatus[keyof typeof AccessRequestStatus]
type ResultDetail = ResearchResult & {
  permissionStatus?: PermissionStatus
  accessRequestStatus?: RequestStatus
  canRequestAccess?: boolean
  rejectedReason?: string
  lastRequestAt?: string
  content?: string
  typeId?: string
  metadata?: Record<string, any>
}
const result = ref<ResultDetail | null>(null)
const applyDialogVisible = ref(false)
const applyReason = ref('')
const dynamicFields = ref<any[]>([])

const STATUS_TYPE_MAP = {
  [ResultStatus.DRAFT]: 'info',
  [ResultStatus.PENDING]: 'warning',
  [ResultStatus.REVIEWING]: 'primary',
  [ResultStatus.REVISION]: 'warning',
  [ResultStatus.REJECTED]: 'danger',
  [ResultStatus.PUBLISHED]: 'success',
  [ResultStatus.REVOKED]: 'info'
}

const STATUS_TEXT_MAP = {
  [ResultStatus.DRAFT]: '草稿',
  [ResultStatus.PENDING]: '待审核',
  [ResultStatus.REVIEWING]: '审核中',
  [ResultStatus.REVISION]: '退回修改',
  [ResultStatus.REJECTED]: '已驳回',
  [ResultStatus.PUBLISHED]: '已发布',
  [ResultStatus.REVOKED]: '已撤销'
}

const phaseMap: Record<string, string> = {
  initiation: '立项',
  design: '设计',
  development: '研发',
  experiment: '实验/测试',
  uat: '验收',
  delivery: '交付',
  operation: '运营/维护'
}

const stageMap: Record<string, string> = {
  design: '设计',
  experiment: '实验/测试',
  data_collection: '数据采集',
  delivery: '交付',
  uat: '验收',
  operation: '运维'
}

const VISIBILITY_TEXT_MAP = {
  [ResultVisibility.PRIVATE]: '私有',
  [ResultVisibility.PUBLIC]: '公开'
}

const hasFullAccess = computed(() => {
  const current = result.value
  if (!current) return false
  if (userStore.isAdmin) return true
  const noAccessFields = !current.permissionStatus && !current.accessRequestStatus
  if (noAccessFields) return true
  return (
    current.permissionStatus === AccessPermissionStatus.FULL ||
    current.accessRequestStatus === AccessRequestStatus.APPROVED
  )
})

const isPending = computed(() => result.value?.accessRequestStatus === AccessRequestStatus.PENDING)
const isRejected = computed(() => result.value?.accessRequestStatus === AccessRequestStatus.REJECTED)

const canRequestAccess = computed(() => {
  const current = result.value
  if (!current) return false
  if (hasFullAccess.value || isPending.value) return false
  if (current.canRequestAccess === false) return false
  return true
})

const accessTitle = computed(() => {
  if (hasFullAccess.value) return '已授权访问'
  if (isPending.value) return '权限审核中'
  if (isRejected.value) return '申请被拒绝'
  return '访问受限'
})

const accessDesc = computed(() => {
  if (!result.value) return ''
  if (hasFullAccess.value) return '您拥有查看正文及下载附件的完整权限。'
  if (isPending.value) {
    return result.value.lastRequestAt
      ? `您于 ${result.value.lastRequestAt} 提交了申请，请耐心等待管理员审核。`
      : '管理员正在审核您的申请。'
  }
  if (isRejected.value) {
    return result.value.rejectedReason
      ? `拒绝原因：${result.value.rejectedReason}`
      : '您可以补充理由后再次申请。'
  }
  return '当前仅展示摘要信息。如需查看完整研究内容及附件，请提交访问申请。'
})

const transformedFields = computed(() => {
  if (!dynamicFields.value.length) return []
  return dynamicFields.value.map(field => ({
    id: field.field_code,
    name: field.field_code,
    label: field.field_name,
    type: mapFieldType(field.field_type),
    isHtml: field.field_type === 'RICHTEXT',
    order: field.order || 0,
    span: 1
  })).sort((a, b) => (a.order || 0) - (b.order || 0))
})

const inlineFields = computed(() => {
  return transformedFields.value.filter(f =>
    f.type !== FrontendFieldType.TEXTAREA
  )
})

const blockFields = computed(() => {
  return transformedFields.value.filter(f =>
    f.type === FrontendFieldType.TEXTAREA
  )
})

onMounted(async () => {
  await loadDetail()
})

async function loadDetail() {
  loading.value = true
  try {
    const resultId = route.params.id?.toString()
    if (!resultId) {
      ElMessage.error('缺少成果 ID')
      return
    }

    const res = await getResult(resultId)
    result.value = res?.data

    if (result.value?.typeId) {
      await loadDynamicFields(result.value.typeId)
    }
  } catch (error) {
    ElMessage.error('加载详情失败')
  } finally {
    loading.value = false
  }
}

async function loadDynamicFields(typeId: string) {
  try {
    const res = await getFieldDefsByType(typeId)
    dynamicFields.value = res?.data || []
  } catch (error) {
    console.error('加载动态字段定义失败', error)
  }
}

function getFieldValueRaw(field: any) {
  if (!result.value?.metadata) return ''
  return result.value.metadata[field.name]
}

function getFieldValueDisplay(field: any) {
  const val = getFieldValueRaw(field)
  if (val === undefined || val === null || val === '') return '—'

  if (field.type === 'switch' || field.type === 'checkbox') {
    return val ? '是' : '否'
  }
  return val
}

function getStatusType(status) {
  if (!status) return 'info'
  return STATUS_TYPE_MAP[status] || 'info'
}

function getStatusText(status) {
  if (!status) return ''
  return STATUS_TEXT_MAP[status] || status
}

function sourceTagType(source) {
  if (source === 'process_system') return 'warning'
  if (source === 'manual_upload') return 'success'
  return 'info'
}

function sourceText(source) {
  if (source === 'process_system') return '过程管理系统'
  if (source === 'manual_upload') return '手工上传'
  return source || '未知'
}

function phaseText(phase) {
  return phase ? phaseMap[phase] || phase : ''
}

function stageText(stage) {
  return stage ? stageMap[stage] || stage : ''
}

function getVisibilityText(visibility) {
  if (!visibility) return ''
  return VISIBILITY_TEXT_MAP[visibility] || visibility
}

function formatFileSize(bytes) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

function downloadFile(file) {
  if (!file?.url) {
    ElMessage.error('无效的下载链接')
    return
  }

  // 修复：如果 URL 错误地指向了 8080 (Spring Boot)，强制转为相对路径走 Proxy
  let downloadUrl = file.url
  if (downloadUrl.includes(':8080/uploads/')) {
    downloadUrl = downloadUrl.replace(/^http:\/\/[^/]+/, '')
  }

  // 使用 fetch 获取 blob，避免浏览器直接打开或处理错误
  fetch(downloadUrl)
    .then(res => {
      if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`)
      return res.blob()
    })
    .then(blob => {
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url

      // 确保文件名有后缀
      let filename = file.name || 'download'
      if (file.ext && !filename.endsWith(file.ext)) {
        filename += file.ext
      } else if (!file.ext && !filename.includes('.')) {
        // 兜底：如果没有扩展名且名字里也没有点，尝试根据 blob type 加后缀?
        // 暂时不硬猜，防止加错
      }

      link.download = filename
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    })
    .catch(err => {
      console.error('Download error:', err)
      ElMessage.error('下载失败，文件可能不存在或已损坏')
    })
}

function goProjectFilter(item) {
  if (!item?.projectId) return
  router.push({
    path: '/results/search',
    query: {
      projectId: item.projectId,
      keyword: item.projectName || ''
    }
  })
}

function openApplyDialog() {
  if (!canRequestAccess.value) {
    ElMessage.info('当前状态不可申请')
    return
  }
  applyReason.value = ''
  applyDialogVisible.value = true
}

const { executing: applying, execute: submitAccessRequest } = useAsyncAction(
  async () => {
    const resultId = route.params.id?.toString()
    if (!resultId) {
      throw new AppError('缺少成果 ID', ErrorType.VALIDATION, 'MISSING_RESULT_ID')
    }

    const reason = applyReason.value.trim()
    if (!reason) {
      throw new AppError('请填写申请理由', ErrorType.VALIDATION, 'REASON_REQUIRED')
    }

    await requestResultAccess(resultId, { reason })
  },
  {
    successMessage: '已提交申请，等待管理员审核',
    onSuccess: () => {
      applyDialogVisible.value = false
      if (result.value) {
        result.value = {
          ...result.value,
          accessRequestStatus: AccessRequestStatus.PENDING
        }
      }
    }
  }
)

async function handleSubmitAccessRequest() {
  await submitAccessRequest()
}

function handleBack() {
  if (window.history.length <= 1) {
    // 尝试关闭当前标签页
    window.close()
    // 由于浏览器安全限制，window.close() 可能失败
    setTimeout(() => {
      if (userStore.isExpert) {
        router.push('/expert/reviews')
      } else if (userStore.isAdmin) {
        router.push('/admin/results')
      } else {
        router.push('/results/my')
      }
    }, 100)
  } else {
    router.back()
  }
}
</script>

<style scoped>
.result-detail-container {
  min-height: 100vh;
  background-color: #f8fafc; /* 浅灰底色 */
  padding-bottom: 40px;
}

.top-nav {
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 10;
}

.nav-back {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #64748b;
  font-size: 14px;
  transition: color 0.2s;
}

.nav-back:hover {
  color: #0f172a;
}

.back-icon {
  font-size: 16px;
  margin-right: 4px;
}

.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

/* Header */
.detail-header {
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
  margin: 0;
  line-height: 1.4;
}

.status-badges {
  display: flex;
  gap: 8px;
}

.meta-row {
  display: flex;
  gap: 24px;
  color: #64748b;
  font-size: 14px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.meta-value {
  color: #334155;
  font-weight: 500;
}

/* Layout */
.layout-grid {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 24px;
  align-items: start;
}

@media (max-width: 900px) {
  .layout-grid {
    grid-template-columns: 1fr;
  }
}

/* Main Column Base Style */
.main-column {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* Cards */
.content-card, .side-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05); /* 轻微阴影 */
  border: 1px solid #e2e8f0;
  overflow: hidden;
}

.content-card {
  padding: 24px;
}

.side-card {
  padding: 20px;
  margin-bottom: 24px;
}

.section-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
  display: flex;
  align-items: center;
}

.section-title::before {
  content: '';
  display: block;
  width: 4px;
  height: 16px;
  background: #3b82f6; /* Primary Color */
  border-radius: 2px;
  margin-right: 8px;
}

.side-title {
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
  margin: 0 0 16px 0;
}

/* Access Banner */
.access-banner {
  display: flex;
  gap: 16px;
  padding: 16px 20px;
  border-radius: 12px;
  align-items: center;
  margin-bottom: 0px; /* main column uses gap */
  border: 1px solid transparent;
}

.access-banner--ok {
  background: linear-gradient(to right, #ecfdf5, #f0fdf4);
  border-color: #bbf7d0;
}

.access-banner--warning {
  background: linear-gradient(to right, #fff7ed, #fffbeb);
  border-color: #fed7aa;
}

.banner-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255,255,255,0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.icon-ok { color: #10b981; }
.icon-lock { color: #f97316; }

.banner-content {
  flex: 1;
}

.banner-title {
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
}

.banner-desc {
  font-size: 13px;
  color: #64748b;
}

/* Content Styles */
.abstract-text {
  font-size: 15px;
  line-height: 1.7;
  color: #334155;
  background: #f8fafc;
  padding: 16px;
  border-radius: 8px;
  border: 1px dashed #cbd5e1;
}

.text-display, .rich-text-display {
  font-size: 16px;
  line-height: 1.8;
  color: #1e293b;
  white-space: pre-wrap;
  word-wrap: break-word;
  overflow-wrap: anywhere;
}

.empty-text {
  color: #94a3b8;
  font-style: italic;
  padding: 20px 0;
  text-align: center;
}

/* Restricted Placeholder */
.restricted-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #f8fafc;
  border-radius: 8px;
  color: #94a3b8;
  gap: 12px;
}

.lock-icon {
  font-size: 32px;
  color: #cbd5e1;
}

/* Attachments */
.attachment-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.attachment-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}

.attachment-item:hover {
  border-color: #bbf7d0; /* Highlight color */
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  transform: translateY(-2px);
}

.file-icon {
  font-size: 24px;
  color: #3b82f6;
  margin-right: 12px;
}

.file-info {
  flex: 1;
  overflow: hidden;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
  color: #334155;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-size {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}

.file-action {
  color: #cbd5e1;
  transition: color 0.2s;
}

.attachment-item:hover .file-action {
  color: #3b82f6;
}

/* Sidebar Properties */
.props-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.prop-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start; /* 允许顶部对齐，适应长文本 */
  font-size: 14px;
}

.prop-label {
  color: #64748b;
  flex-shrink: 0;
  min-width: 70px;
}

.prop-value {
  color: #0f172a;
  font-weight: 500;
  text-align: right;
  word-break: break-word; /* 防止长词撑开 */
  padding-left: 12px;
}

.project-info {
  background: #f1f5f9;
  padding: 12px;
  border-radius: 8px;
}

.project-name {
  font-weight: 600;
  color: #334155;
  margin-bottom: 4px;
  font-size: 14px;
}

.project-code {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 8px;
  font-family: monospace;
}

.tags-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.keyword-tag {
  border: none;
  background: #f1f5f9;
  color: #475569;
}

.dialog-instruction {
  color: #64748b;
  font-size: 14px;
  margin-bottom: 20px;
}
</style>
