<template>
  <div class="result-detail">
    <!-- 返回按钮 -->
    <div class="back-navigation">
      <el-button @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
        <div class="card-title-wrap">
          <h2>{{ result?.title }}</h2>
          <div class="card-actions">
            <el-tag :type="getStatusType(result?.status)">
              {{ getStatusText(result?.status) }}
            </el-tag>
            <el-tag v-if="result?.source" effect="plain" size="small" :type="sourceTagType(result?.source)">
              {{ sourceText(result?.source) }}
            </el-tag>
            <el-button
              text
              size="small"
              :loading="loading"
              @click="loadDetail"
            >
                <el-icon><Refresh /></el-icon>
                刷新状态
              </el-button>
            </div>
          </div>
        </div>
      </template>

      <div
        v-if="result"
        class="access-banner"
        :class="{
          'access-banner--ok': hasFullAccess,
          'access-banner--warning': !hasFullAccess
        }"
      >
        <div class="banner-main">
          <div class="banner-title">{{ accessTitle }}</div>
          <div class="banner-desc">{{ accessDesc }}</div>
        </div>
        <div class="banner-actions">
          <el-tag v-if="hasFullAccess" type="success">已授权</el-tag>
          <el-tag v-else-if="isPending" type="warning">审核中</el-tag>
          <el-tag v-else-if="isRejected" type="danger">已拒绝</el-tag>
          <el-button
            v-if="!hasFullAccess && canRequestAccess"
            type="primary"
            size="small"
            :loading="applying"
            @click="openApplyDialog"
          >
            {{ isRejected ? '重新申请' : '申请查看全文' }}
          </el-button>
        </div>
      </div>

      <el-descriptions v-if="result" :column="2" border>
        <el-descriptions-item label="成果类型">
          {{ result.type }}
        </el-descriptions-item>
        <el-descriptions-item label="来源">
          <el-tag :type="sourceTagType(result.source)" size="small">
            {{ sourceText(result.source) }}
          </el-tag>
          <span v-if="result.sourceStage" class="inline-text">· 来源阶段：{{ stageText(result.sourceStage) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="年份">
          {{ result.year }}
        </el-descriptions-item>
        <el-descriptions-item label="项目阶段">
          {{ phaseText(result.projectPhase) || '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="所属项目" :span="2">
          <template v-if="result.projectName">
            {{ result.projectName }} ({{ result.projectCode }})
            <el-button type="primary" link @click="goProjectFilter(result)">
              查看该项目下成果
            </el-button>
          </template>
          <span v-else class="text-muted">无所属/其他</span>
        </el-descriptions-item>
        <el-descriptions-item label="作者" :span="2">
          {{ result.authors?.join(', ') }}
        </el-descriptions-item>
        <el-descriptions-item label="关键词" :span="2">
          <el-tag v-for="kw in result.keywords" :key="kw" style="margin-right: 8px">
            {{ kw }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="摘要" :span="2">
          {{ result.abstract }}
        </el-descriptions-item>
        <el-descriptions-item label="同步时间">
          {{ result.syncTime || result.updatedAt || '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="可见范围">
          {{ getVisibilityText(result.visibility) }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ result.createdAt }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <div class="content-section" v-if="result">
        <div class="content-header">
          <h3>正文内容</h3>
        </div>
        <div v-if="hasFullAccess" class="content-body">
          <div v-if="result.content">
            {{ result.content }}
          </div>
          <div v-else class="text-muted">暂无正文内容</div>
        </div>
        <div v-else class="restricted-block">
          <p>当前仅展示摘要，正文内容已隐藏。如需查看请提交申请，管理员审核通过后可查看全文。</p>
        </div>
      </div>

      <div class="attachments">
        <h3>附件列表</h3>
        <el-empty v-if="hasFullAccess && !result?.attachments?.length" description="暂无附件" />
        <div v-else-if="hasFullAccess" class="attachment-list">
          <div v-for="file in result.attachments" :key="file.id" class="attachment-item">
            <el-icon><Document /></el-icon>
            <span class="filename">{{ file.name }}</span>
            <span class="filesize">{{ formatFileSize(file.size) }}</span>
            <el-button type="primary" link @click="downloadFile(file)">下载</el-button>
          </div>
        </div>
        <div v-else class="restricted-block">
          附件因权限限制暂不可查看，请提交申请后等待管理员审核。
        </div>
      </div>
    </el-card>

    <el-dialog
      v-model="applyDialogVisible"
      title="申请查看全文"
      width="480px"
    >
      <el-form label-width="88px">
        <el-form-item label="申请理由" required>
          <el-input
            v-model="applyReason"
            type="textarea"
            :rows="4"
            placeholder="请简要说明申请原因，例如科研协同、复用参考等"
          />
        </el-form-item>
      </el-form>
      <p class="dialog-tip">提交后由科研管理员审核，请耐心等待并可稍后刷新状态。</p>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="applying"
          @click="handleSubmitAccessRequest"
        >
          提交申请
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Document, Refresh } from '@element-plus/icons-vue'
import { getResult, requestResultAccess } from '@/api/result'
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
}
const result = ref<ResultDetail | null>(null)
const applyDialogVisible = ref(false)
const applyReason = ref('')

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
  // 管理员和科研管理角色默认可查看全部内容
  if (userStore.isAdmin) return true

  // 兼容历史数据：未返回权限字段时视为有权限
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
  // canRequestAccess 为 false 时禁止重新申请
  if (current.canRequestAccess === false) return false
  return true
})

const accessTitle = computed(() => {
  if (hasFullAccess.value) return '已获得全文访问权限'
  if (isPending.value) return '申请已提交，等待管理员审核'
  if (isRejected.value) return '申请已被拒绝，可补充理由后重新申请'
  return '当前仅可查看摘要'
})

const accessDesc = computed(() => {
  if (!result.value) return ''
  if (hasFullAccess.value) return '可以查看正文及附件。'
  if (isPending.value) {
    return result.value.lastRequestAt
      ? `提交时间：${result.value.lastRequestAt}`
      : '管理员审核后会通知你，可稍后刷新状态。'
  }
  if (isRejected.value) {
    return result.value.rejectedReason
      ? `拒绝原因：${result.value.rejectedReason}`
      : '可补充更详细的申请理由后重新提交。'
  }
  return '出于权限控制，当前仅展示摘要，正文和附件已隐藏。如需查看请提交申请。'
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
  } catch (error) {
    ElMessage.error('加载详情失败')
  } finally {
    loading.value = false
  }
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
  window.open(file.url, '_blank')
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
  // 检查浏览器历史记录长度
  // 如果 history.length <= 1，说明这是新打开的标签页，没有可返回的历史
  if (window.history.length <= 1) {
    // 尝试关闭当前标签页
    window.close()
    // 由于浏览器安全限制，window.close() 可能失败
    // 如果用户还在页面上，100ms 后根据角色跳转到合适的页面
    setTimeout(() => {
      // 根据用户角色智能返回
      if (userStore.isExpert) {
        // 专家用户优先返回审核页面
        router.push('/expert/reviews')
      } else if (userStore.isAdmin || userStore.isManager) {
        // 管理员返回管理页面
        router.push('/admin/results')
      } else {
        // 普通用户返回个人成果页面
        router.push('/results/my')
      }
    }, 100)
  } else {
    // 有历史记录，使用浏览器后退
    router.back()
  }
}
</script>

<style scoped>
.back-navigation {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header .card-title-wrap {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  gap: 12px;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.access-banner {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  margin-bottom: 16px;
  background: #f9fafb;
}

.access-banner--ok {
  background: #f0fdf4;
  border-color: #bbf7d0;
}

.access-banner--warning {
  background: #f5f7ff;
  border-color: #e0e7ff;
}

.banner-main {
  flex: 1;
}

.banner-title {
  font-weight: 700;
  color: #111827;
  margin-bottom: 4px;
}

.banner-desc {
  color: #6b7280;
  font-size: 13px;
  line-height: 1.5;
}

.banner-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.content-section {
  margin-bottom: 16px;
}

.attachments {
  margin-top: 20px;
}

.attachments h3 {
  margin-bottom: 16px;
}

.content-body {
  padding: 12px;
  background: #f8fafc;
  border-radius: 6px;
  line-height: 1.6;
  color: #1f2937;
  white-space: pre-line;
}

.restricted-block {
  padding: 12px;
  border: 1px dashed #d1d5db;
  border-radius: 6px;
  color: #6b7280;
  background: #f9fafb;
  line-height: 1.6;
}

.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f5f6fa;
  border-radius: 4px;
}

.filename {
  flex: 1;
  font-size: 14px;
}

.filesize {
  color: #909399;
  font-size: 12px;
}

.dialog-tip {
  margin: 8px 0 0;
  color: #909399;
  font-size: 13px;
}
</style>
