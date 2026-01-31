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
        <el-table-column prop="title" label="成果名称" min-width="240">
          <template #default="{ row }">
            <div class="title-cell">
              <div class="title">{{ row.title }}</div>
              <div class="meta">
                <span class="pill pill-type">{{ row.type }}</span>
                <span v-if="row.projectPhase" class="pill pill-ghost">
                  {{ phaseMap[row.projectPhase] || row.projectPhase }}
                </span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="130">
          <template #default="{ row }">
            <div class="source-block">
              <span :class="['pill', row.source === 'process_system' ? 'pill-warn' : 'pill-success']">
                {{ sourceText(row.source) }}
              </span>
              <div
                v-if="row.source === 'process_system'"
                class="format-line"
                :class="formatClass(row)"
              >
                {{ formatText(row) }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="plain" class="status-soft">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <div class="ops">
              <el-button type="primary" link size="small" @click="viewResult(row)">查看</el-button>
              <el-dropdown trigger="click">
                <span class="more-link">
                  更多
                  <el-icon><ArrowDown /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-if="canAssign(row)" @click="openAssign(row)">分配审核</el-dropdown-item>
                    <el-dropdown-item v-if="canFormatCheck(row)" @click="handleFormatCheck(row)">
                      格式审查通过
                    </el-dropdown-item>
                    <el-dropdown-item v-if="canFormatReject(row)" @click="handleFormatReject(row)">
                      格式退回
                    </el-dropdown-item>
                    <el-dropdown-item :disabled="row.source === 'process_system'" @click="editResult(row)">
                      编辑
                    </el-dropdown-item>
                    <el-dropdown-item divided @click="removeResult(row)">
                      <span class="danger">删除</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
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
          @current-change="handleSearch"
        />
      </div>
    </el-card>

    <el-dialog v-model="assignDialogVisible" title="分配审核人" width="420px">
      <el-form label-width="88px">
        <el-form-item label="审核人" required>
          <el-select
            v-model="assignForm.reviewerIds"
            multiple
            placeholder="选择审核人"
            style="width: 100%"
          >
            <el-option
              v-for="user in expertList"
              :key="user.id"
              :label="`${user.name}`"
              :value="user.id"
            />
          </el-select>
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
import { ArrowDown } from '@element-plus/icons-vue'
import { getResults, deleteResult, assignReviewers, markFormatChecked, markFormatRejected } from '@/api/result'
import { getExpertUsers, type KeycloakUser } from '@/api/user'
import { ResultStatus } from '@/types'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const assignDialogVisible = ref(false)
const assigning = ref(false)
const formatChecking = ref(false)
const currentAssignId = ref('')
const expertList = ref<KeycloakUser[]>([])
const assignForm = reactive({
  reviewerIds: [] as number[]
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
  loadExpertList()
})

async function loadExpertList() {
  try {
    const res = await getExpertUsers()
    expertList.value = res?.data || []
  } catch (e) {
    console.error('加载专家列表失败', e)
  }
}

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

function canFormatCheck(row: any) {
  return row?.source === 'process_system' && row?.formatStatus !== 'passed'
}

function canFormatReject(row: any) {
  return row?.source === 'process_system' && row?.formatStatus !== 'failed'
}

function openAssign(row: any) {
  currentAssignId.value = row.id
  assignForm.reviewerIds = Array.isArray(row?.assignedReviewerIds) ? [...row.assignedReviewerIds] : []
  assignDialogVisible.value = true
}

async function handleAssign() {
  if (!currentAssignId.value) return
  if (!assignForm.reviewerIds || assignForm.reviewerIds.length === 0) {
    ElMessage.warning('请选择至少一位审核人')
    return
  }
  assigning.value = true
  try {
    const reviewerNames = assignForm.reviewerIds
      .map((id) => expertList.value.find((u) => u.id === id)?.name)
      .filter(Boolean) as string[]

    await assignReviewers(currentAssignId.value, {
      reviewerIds: assignForm.reviewerIds,
      reviewerNames
    })
    ElMessage.success('已分配审核人')
    assignDialogVisible.value = false
    handleSearch()
  } catch (e) {
    ElMessage.error('分配失败')
  } finally {
    assigning.value = false
  }
}

async function handleFormatCheck(row: any) {
  if (!row?.id) return
  formatChecking.value = true
  try {
    await markFormatChecked(row.id)
    ElMessage.success('格式审查通过')
    handleSearch()
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    formatChecking.value = false
  }
}

async function handleFormatReject(row: any) {
  if (!row?.id) return
  try {
    const { value, action } = await ElMessageBox.prompt('请填写格式问题说明', '格式退回', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：缺少章节编号、摘要格式不规范',
      inputType: 'textarea'
    })
    if (action === 'confirm') {
      formatChecking.value = true
      await markFormatRejected(row.id, { reason: value })
      ElMessage.success('已退回格式修改')
      handleSearch()
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  } finally {
    formatChecking.value = false
  }
}

function formatClass(row: any) {
  if (row?.formatStatus === 'failed') return 'format-failed'
  if (row?.formatStatus === 'passed' || row?.formatChecked === true) return 'format-ok'
  return 'format-pending'
}

function formatText(row: any) {
  if (row?.formatStatus === 'failed') return row.formatNote || '格式不通过'
  if (row?.formatStatus === 'passed' || row?.formatChecked === true) return '格式已审'
  return '待格式审查'
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

.title-cell .title {
  font-weight: 600;
  color: #0f172a;
}

.title-cell .meta {
  margin-top: 6px;
  display: flex;
  gap: 6px;
}

.pill {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  background: #f3f4f6;
  color: #4b5563;
}

.pill-type {
  background: rgba(29, 91, 255, 0.12);
  color: #1d5bff;
}

.pill-ghost {
  background: #eef2ff;
  color: #4338ca;
}

.pill-warn {
  background: rgba(244, 159, 10, 0.12);
  color: #b45309;
}

.pill-success {
  background: rgba(46, 204, 113, 0.14);
  color: #15803d;
}

.source-block {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.format-line {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 8px;
  width: fit-content;
  border: 1px solid transparent;
}

.format-pending {
  background: rgba(255, 170, 73, 0.16);
  color: #b45309;
}

.format-ok {
  background: rgba(46, 204, 113, 0.14);
  color: #15803d;
}

.format-failed {
  background: rgba(248, 113, 113, 0.14);
  color: #b91c1c;
  border-color: rgba(248, 113, 113, 0.35);
}

.status-soft {
  border-color: transparent;
  background: #f8fafc;
  color: #0f172a;
}

.ops {
  display: flex;
  align-items: center;
  gap: 6px;
}

.more-link {
  color: #1d5bff;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.danger {
  color: #e11d48;
}
</style>
