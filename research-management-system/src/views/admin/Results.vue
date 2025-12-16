<template>
  <div class="admin-results">
    <el-card>
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="搜索" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="待审核" value="pending" />
            <el-option label="审核中" value="reviewing" />
            <el-option label="退回修改" value="revision" />
            <el-option label="已发布" value="published" />
            <el-option label="已驳回" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源">
          <el-select v-model="searchForm.source" placeholder="全部" clearable>
            <el-option label="手工上传" value="manual_upload" />
            <el-option label="过程管理系统" value="process_system" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="title" label="成果名称" min-width="220">
          <template #default="{ row }">
            <div class="title-cell">
              <div class="title">{{ row.title }}</div>
              <div class="meta">
                <el-tag size="small" effect="plain">{{ row.type }}</el-tag>
                <el-tag v-if="row.projectPhase" size="small" type="info" effect="plain">
                  {{ phaseMap[row.projectPhase] || row.projectPhase }}
                </el-tag>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="130">
          <template #default="{ row }">
            <el-tag :type="row.source === 'process_system' ? 'warning' : 'success'" size="small">
              {{ sourceText(row.source) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="plain">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewResult(row)">查看</el-button>
            <el-button
              v-if="canAssign(row)"
              type="primary"
              link
              size="small"
              @click="openAssign(row)"
            >
              分配审核
            </el-button>
            <el-button
              type="primary"
              link
              size="small"
              :disabled="row.source === 'process_system'"
              @click="editResult(row)"
            >
              编辑
            </el-button>
            <el-button type="danger" link size="small" @click="removeResult(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          :page-size="pagination.pageSize"
          :total="pagination.total"
          layout="total, prev, pager, next"
          @current-change="handleSearch"
        />
      </div>
    </el-card>

    <el-dialog v-model="assignDialogVisible" title="分配审核人" width="420px">
      <el-form label-width="88px">
        <el-form-item label="审核人" required>
          <el-input
            v-model="assignForm.reviewers"
            placeholder="输入审核人，支持逗号分隔多个"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="assigning" @click="handleAssign">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getResults, deleteResult, assignReviewers } from '@/api/result'
import { ResultStatus } from '@/types'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const assignDialogVisible = ref(false)
const assigning = ref(false)
const currentAssignId = ref('')
const assignForm = reactive({
  reviewers: ''
})

const searchForm = reactive({
  keyword: '',
  status: '',
  source: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

onMounted(() => {
  handleSearch()
})

async function handleSearch() {
  loading.value = true
  try {
    const res = await getResults({ ...searchForm, ...pagination })
    const { data } = res || {}
    tableData.value = data?.list || []
    pagination.total = data?.total || 0
  } finally {
    loading.value = false
  }
}

function viewResult(row) {
  if (!row?.id) return
  router.push(`/results/${row.id}`)
}

function editResult(row) {
  if (!row?.id) return
  router.push(`/results/${row.id}/edit`)
}

function canAssign(row: any) {
  return row?.source === 'process_system' && row?.status === ResultStatus.PENDING
}

function openAssign(row: any) {
  currentAssignId.value = row.id
  assignForm.reviewers = (row.assignedReviewers || []).join(', ')
  assignDialogVisible.value = true
}

async function handleAssign() {
  if (!currentAssignId.value) return
  assigning.value = true
  try {
    await assignReviewers(currentAssignId.value, { reviewers: assignForm.reviewers })
    ElMessage.success('已分配审核人')
    assignDialogVisible.value = false
    handleSearch()
  } catch (e) {
    ElMessage.error('分配失败')
  } finally {
    assigning.value = false
  }
}

async function removeResult(row) {
  if (!row?.id) return
  try {
    await ElMessageBox.confirm('确定要删除该成果吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteResult(row.id)
    ElMessage.success('删除成功')
    handleSearch()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

function statusType(status: string) {
  const map: Record<string, string> = {
    [ResultStatus.DRAFT]: 'info',
    [ResultStatus.PENDING]: 'warning',
    [ResultStatus.REVIEWING]: 'primary',
    [ResultStatus.REVISION]: 'warning',
    [ResultStatus.REJECTED]: 'danger',
    [ResultStatus.PUBLISHED]: 'success',
    [ResultStatus.REVOKED]: 'info'
  }
  return map[status] || 'info'
}

function statusText(status: string) {
  const map: Record<string, string> = {
    [ResultStatus.DRAFT]: '草稿',
    [ResultStatus.PENDING]: '待审核',
    [ResultStatus.REVIEWING]: '审核中',
    [ResultStatus.REVISION]: '退回修改',
    [ResultStatus.REJECTED]: '已驳回',
    [ResultStatus.PUBLISHED]: '已发布',
    [ResultStatus.REVOKED]: '已撤销'
  }
  return map[status] || status
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

function sourceText(source: string) {
  if (source === 'process_system') return '过程管理系统'
  if (source === 'manual_upload') return '手工上传'
  return source || '未知'
}
</script>

<style scoped>
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
