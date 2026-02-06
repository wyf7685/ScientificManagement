<template>
  <div class="access-requests">
    <el-card>
      <el-form :inline="true" :model="searchForm" class="filter-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索成果 / 申请人"
            clearable
            @keyup.enter="handleSearch(1)"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已拒绝" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch(1)">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="resultTitle" label="成果" min-width="240">
          <template #default="{ row }">
            <div class="result-cell">
              <div class="title">{{ row.resultTitle }}</div>
              <div class="meta">
                <el-tag v-if="row.resultType" size="small">{{ row.resultType }}</el-tag>
                <span v-if="row.projectName" class="project">{{ row.projectName }}</span>
              </div>
              <div class="meta">
                <span class="muted">可见范围：{{ getVisibilityText(row.visibility) }}</span>
                <span v-if="row.resultStatus" class="muted">当前状态：{{ row.resultStatus }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="申请人" width="160">
          <template #default="{ row }">
            <div class="applicant">
              <div class="name">{{ row.userName }}</div>
              <div class="time">提交时间：{{ formatDateTime(row.createdAt) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="申请理由" min-width="220" show-overflow-tooltip />
        <el-table-column label="审批信息" width="220">
          <template #default="{ row }">
            <div class="approval">
              <el-tag :type="statusTagType(row.status)" size="small">
                {{ statusText(row.status) }}
              </el-tag>
              <div v-if="row.reviewer" class="reviewer">
                审批人：{{ row.reviewer }}
              </div>
              <div v-if="row.reviewedAt" class="time">审批时间：{{ formatDateTime(row.reviewedAt) }}</div>
              <div v-if="row.comment" class="comment">备注：{{ row.comment }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <div class="ops" v-if="row.status === AccessRequestStatus.PENDING">
              <el-button
                type="success"
                link
                size="small"
                @click="handleApprove(row)"
              >
                通过
              </el-button>
              <el-button
                type="danger"
                link
                size="small"
                @click="handleReject(row)"
              >
                拒绝
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          :page-size="pagination.pageSize"
          :total="pagination.total"
          layout="total, prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getResultAccessRequests, reviewResultAccessRequest } from '@/api/result'
import { AccessRequestStatus, ResultVisibility, type ResultAccessRequest } from '@/types'
import { formatDateTime } from '@/utils/date'

const isMockMode = import.meta.env.VITE_USE_MOCK === 'true'
const loading = ref(false)
const tableData = ref<ResultAccessRequest[]>([])
const searchForm = reactive({
  keyword: '',
  status: ''
})
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const VISIBILITY_TEXT = {
  [ResultVisibility.PRIVATE]: '私有',
  [ResultVisibility.PUBLIC]: '公开'
}

const STATUS_TEXT: Record<string, string> = {
  pending: '待审核',
  approved: '已通过',
  rejected: '已拒绝'
}

const STATUS_TAG: Record<string, string> = {
  pending: 'warning',
  approved: 'success',
  rejected: 'danger'
}

onMounted(() => {
  handleSearch()
})

function getVisibilityText(value?: string) {
  if (!value) return '—'
  return VISIBILITY_TEXT[value as keyof typeof VISIBILITY_TEXT] || value
}

function statusText(status: string) {
  return STATUS_TEXT[status] || status
}

function statusTagType(status: string) {
  return STATUS_TAG[status] || 'info'
}


function resetFilters() {
  searchForm.keyword = ''
  searchForm.status = ''
  handleSearch(1)
}

async function handleSearch(page?: number) {
  if (page) {
    pagination.page = page
  }
  loading.value = true
  try {
    const res = await getResultAccessRequests({
      ...searchForm,
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    const { data } = res || {}
    const list = data?.list || []
    tableData.value = list
    pagination.total = data?.total ?? list.length ?? 0
    if ((!list.length || !pagination.total) && isMockMode) {
      await loadMockFallback()
    }
  } catch (error) {
    console.error('加载权限申请失败:', error)
    if (isMockMode) {
      await loadMockFallback()
    }
  } finally {
    loading.value = false
  }
}

async function loadMockFallback() {
  try {
    const mockModule = await import('@/mocks/data')
    const mockList = mockModule.resultAccessRequests || []
    tableData.value = mockList as ResultAccessRequest[]
    pagination.total = mockList.length
  } catch (error) {
    console.error('加载 Mock 权限申请失败:', error)
  }
}

function handlePageChange(page: number) {
  pagination.page = page
  handleSearch()
}

async function handleApprove(row: ResultAccessRequest) {
  await review(row, 'approve')
}

async function handleReject(row: ResultAccessRequest) {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝理由', '拒绝申请', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '例如：申请理由不充分、缺少安全承诺等',
      inputValidator: (val) => !!val?.trim() || '请填写拒绝理由'
    })
    await review(row, 'reject', value)
  } catch {
    // cancel
  }
}

async function review(row: ResultAccessRequest, action: 'approve' | 'reject', comment?: string) {
  try {
    await reviewResultAccessRequest(row.id, { action, comment: comment?.trim() })
    ElMessage.success(action === 'approve' ? '已通过申请' : '已拒绝申请')
    handleSearch()
  } catch (error) {
    console.error(error)
  }
}
</script>

<style scoped>
.access-requests {
  padding: 8px;
}

.filter-form {
  margin-bottom: 12px;
}

.result-cell .title {
  font-weight: 600;
  margin-bottom: 4px;
}

.result-cell .meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  font-size: 13px;
  color: #909399;
}

.result-cell .project {
  color: #606266;
}

.result-cell .muted {
  font-size: 12px;
}

.applicant .name {
  font-weight: 500;
  margin-bottom: 4px;
}

.applicant .time {
  font-size: 12px;
  color: #909399;
}

.approval .reviewer,
.approval .time,
.approval .comment {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.ops {
  display: flex;
  gap: 8px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
