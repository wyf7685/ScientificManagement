<template>
  <div class="review-assign">
    <el-card class="card">
      <template #header>
        <div class="card-header">
          <span class="title">分配审核专家</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 待分配 -->
        <el-tab-pane label="待分配" name="pending">
          <el-table
            :data="tableData"
            v-loading="loading"
            stripe
            border
            class="table"
            :header-cell-style="headerStyle"
          >
            <el-table-column prop="title" label="成果名称" min-width="220" show-overflow-tooltip />
            <el-table-column prop="type" label="类型" width="120" align="center" />
            <el-table-column prop="createdBy" label="提交人" width="140" align="center" show-overflow-tooltip />
            <el-table-column prop="createdAt" label="提交时间" width="180" align="center" show-overflow-tooltip />
            <el-table-column label="操作" width="140" align="center" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="assignExpert(row)">
                  分配专家
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 已分配 -->
        <el-tab-pane label="已分配审核" name="assigned">
          <el-table
            :data="assignedTableData"
            v-loading="loading"
            stripe
            border
            class="table"
            :header-cell-style="headerStyle"
          >
            <el-table-column prop="title" label="成果名称" min-width="240" show-overflow-tooltip />
            <el-table-column prop="type" label="成果类型" width="120" align="center" />
            <el-table-column prop="createdBy" label="成果物提交人" width="160" align="center" show-overflow-tooltip />
            <el-table-column prop="reviewerName" label="已分配审核人名称" min-width="180" align="center" show-overflow-tooltip />
            <el-table-column label="分配日期" width="190" align="center">
              <template #default="{ row }">
                <span class="time-text">{{ formatTime(row.assignedAt) }}</span>
              </template>
            </el-table-column>
          </el-table>

          <!-- 已分配分页 -->
          <div class="pagination">
            <el-pagination
              v-model:current-page="assignedPagination.page"
              v-model:page-size="assignedPagination.pageSize"
              :total="assignedPagination.total"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleAssignedSizeChange"
              @current-change="handleAssignedCurrentChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 分配弹窗 -->
    <el-dialog v-model="assignDialog" title="分配审核专家" width="600px">
      <el-form label-width="100px">
        <el-form-item label="选择专家">
          <el-select
            v-model="selectedExperts"
            multiple
            placeholder="请选择专家"
            v-loading="expertsLoading"
            style="width: 100%"
          >
            <el-option
              v-for="expert in experts"
              :key="expert.id"
              :label="expert.name"
              :value="expert.id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="assignDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmAssign" :loading="assignLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getPendingAssignResults, assignReviewers, getAssignReviewersList } from '@/api/result'
import { getExpertUsers, type KeycloakUser } from '@/api/user'

// 如果你项目已有 dayjs：
// import dayjs from 'dayjs'

const loading = ref(false)
const expertsLoading = ref(false)
const assignLoading = ref(false)

const activeTab = ref<'pending' | 'assigned'>('pending')

// 待分配
const tableData = ref<any[]>([])

// 已分配
const assignedTableData = ref<any[]>([])
const assignedPagination = ref({
  page: 1,
  pageSize: 10,
  total: 0
})

// 分配弹窗
const assignDialog = ref(false)
const selectedExperts = ref<any[]>([])
const currentResult = ref<any>(null)
const experts = ref<KeycloakUser[]>([])

const headerStyle = computed(() => ({
  background: '#fafafa',
  color: '#303133',
  fontWeight: 600
}))

onMounted(() => {
  loadExperts()
  // 首次进入根据 tab 加载
  if (activeTab.value === 'pending') loadPending()
  else loadAssigned()
})

function formatTime(val?: string) {
  if (!val) return '-'
  // ✅ 如果你用 dayjs：
  // return dayjs(val).format('YYYY-MM-DD HH:mm:ss')

  // ✅ 不依赖库的写法（后端若已是 yyyy-MM-dd HH:mm:ss 就直接返回）
  return String(val).replace('T', ' ').slice(0, 19)
}

async function loadPending() {
  loading.value = true
  try {
    const res = await getPendingAssignResults({ page: 1, pageSize: 50 })
    tableData.value = res?.data?.list || []
  } catch (e) {
    console.error(e)
    ElMessage.error('加载待分配数据失败')
  } finally {
    loading.value = false
  }
}

async function loadAssigned() {
  loading.value = true
  try {
    const res = await getAssignReviewersList({
      page: assignedPagination.value.page,
      pageSize: assignedPagination.value.pageSize
    })
    assignedTableData.value = res?.data?.list || []
    assignedPagination.value.total = res?.data?.total || 0
  } catch (e) {
    console.error(e)
    ElMessage.error('加载已分配数据失败')
  } finally {
    loading.value = false
  }
}

async function loadExperts() {
  expertsLoading.value = true
  try {
    const res = await getExpertUsers()
    experts.value = res?.data || []
  } catch (error) {
    console.error('加载专家列表失败:', error)
    ElMessage.error('加载专家列表失败')
  } finally {
    expertsLoading.value = false
  }
}

// tab 切换
function handleTabChange() {
  if (activeTab.value === 'pending') {
    loadPending()
  } else {
    assignedPagination.value.page = 1
    loadAssigned()
  }
}

// ✅ 分页事件（已分配）
function handleAssignedSizeChange(size: number) {
  assignedPagination.value.pageSize = size
  assignedPagination.value.page = 1
  loadAssigned()
}
function handleAssignedCurrentChange(page: number) {
  assignedPagination.value.page = page
  loadAssigned()
}

function assignExpert(row: any) {
  currentResult.value = row
  selectedExperts.value = []
  assignDialog.value = true
}

async function confirmAssign() {
  if (!currentResult.value) {
    ElMessage.warning('未选择成果')
    return
  }
  if (!selectedExperts.value.length) {
    ElMessage.warning('请选择专家')
    return
  }

  assignLoading.value = true
  try {
    const selectedExpertNames = selectedExperts.value.map((id: any) => {
      const expert = experts.value.find(e => e.id === id)
      return expert ? expert.name : `专家${id}`
    })

    await assignReviewers(currentResult.value.id, {
      reviewerIds: selectedExperts.value,
      reviewerNames: selectedExpertNames
    })

    ElMessage.success('分配成功')
    assignDialog.value = false

    // ✅ 分配成功后刷新当前页的数据，更符合用户心智
    if (activeTab.value === 'pending') {
      loadPending()
    } else {
      loadAssigned()
    }
  } catch (error) {
    console.error('分配失败:', error)
    ElMessage.error('分配失败')
  } finally {
    assignLoading.value = false
  }
}
</script>

<style scoped>
.review-assign {
  padding: 8px;
}

.card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title {
  font-size: 16px;
  font-weight: 600;
}

.table :deep(.el-table__cell) {
  padding: 12px 0;
}
.table :deep(.el-table__header .el-table__cell) {
  padding: 14px 0;
}
.table :deep(.el-table__body tr:hover > td) {
  background-color: #f7faff !important;
}

.time-text {
  font-variant-numeric: tabular-nums;
  color: #606266;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
