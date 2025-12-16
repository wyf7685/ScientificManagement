<template>
  <div class="my-results">
    <el-card>
      <!-- 筛选区 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option label="草稿" value="draft" />
            <el-option label="待审核" value="pending" />
            <el-option label="审核中" value="reviewing" />
            <el-option label="已驳回" value="rejected" />
            <el-option label="已发布" value="published" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索标题、作者"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="项目">
          <el-select v-model="searchForm.projectId" placeholder="全部项目" clearable filterable>
            <el-option :label="'全部'" :value="''" />
            <el-option
              v-for="project in projects"
              :key="project.id"
              :label="getProjectLabel(project)"
              :value="project.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="title" label="成果名称" min-width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">
              {{ row.title }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="projectName" label="所属项目" min-width="180">
          <template #default="{ row }">
            <span v-if="row.projectName">{{ row.projectName }} ({{ row.projectCode }})</span>
            <span v-else class="text-muted">无所属/其他</span>
          </template>
        </el-table-column>
        <el-table-column prop="visibility" label="可见范围" width="140">
          <template #default="{ row }">
            {{ getVisibilityText(row.visibility) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="editResult(row)">
              编辑
            </el-button>
            <el-button type="primary" link size="small" @click="changeVisibility(row)">
              修改可见范围
            </el-button>
            <el-button
              v-if="row.status === 'draft'"
              type="danger"
              link
              size="small"
              @click="deleteResult(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </el-card>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailDrawer" title="成果详情" size="60%">
      <div v-if="currentResult">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="标题" :span="2">
            {{ currentResult.title }}
          </el-descriptions-item>
          <el-descriptions-item label="类型">
            {{ currentResult.type }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentResult.status)">
              {{ getStatusText(currentResult.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="作者" :span="2">
            {{ currentResult.authors?.join(', ') }}
          </el-descriptions-item>
          <el-descriptions-item label="年份">
            {{ currentResult.year }}
          </el-descriptions-item>
          <el-descriptions-item label="所属项目" :span="2">
            <span v-if="currentResult.projectName">{{ currentResult.projectName }} ({{ currentResult.projectCode }})</span>
            <span v-else class="text-muted">无所属/其他</span>
          </el-descriptions-item>
          <el-descriptions-item label="可见范围">
            {{ getVisibilityText(currentResult.visibility) }}
          </el-descriptions-item>
          <el-descriptions-item label="摘要" :span="2">
            {{ currentResult.abstract }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getMyResults, deleteResult as deleteResultApi } from '@/api/result'
import { getProjects } from '@/api/project'
import { ResultStatus, ResultVisibility } from '@/types'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const detailDrawer = ref(false)
const currentResult = ref(null)
const projects = ref([])

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

const VISIBILITY_TEXT_MAP = {
  [ResultVisibility.PRIVATE]: '私有',
  [ResultVisibility.INTERNAL_ABSTRACT]: '机构内摘要',
  [ResultVisibility.INTERNAL_FULL]: '机构内全文',
  [ResultVisibility.PUBLIC_ABSTRACT]: '公开摘要'
}

const searchForm = reactive({
  status: '',
  keyword: '',
  projectId: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

onMounted(() => {
  loadProjects()
  handleSearch()
})

async function handleSearch() {
  loading.value = true
  try {
    const res = await getMyResults({
      ...searchForm,
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    const { data } = res || {}
    tableData.value = data?.list || []
    pagination.total = data?.total || 0
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function handleReset() {
  searchForm.status = ''
  searchForm.keyword = ''
  searchForm.projectId = ''
  pagination.page = 1
  handleSearch()
}

function viewDetail(row) {
  currentResult.value = row
  detailDrawer.value = true
}

function editResult(row) {
  router.push(`/results/${row.id}/edit`)
}

function changeVisibility(row) {
  ElMessage.info('修改可见范围功能开发中')
}

async function deleteResult(row) {
  try {
    await ElMessageBox.confirm('确定要删除这条成果吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteResultApi(row.id)
    ElMessage.success('删除成功')
    handleSearch()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

function getStatusType(status) {
  return STATUS_TYPE_MAP[status] || 'info'
}

function getStatusText(status) {
  return STATUS_TEXT_MAP[status] || status
}

function getVisibilityText(visibility) {
  return VISIBILITY_TEXT_MAP[visibility] || visibility
}

function getProjectLabel(project) {
  if (!project) return ''
  return `${project.name} (${project.code})`
}

async function loadProjects() {
  try {
    const res = await getProjects()
    projects.value = res?.data || []
  } catch (error) {
    ElMessage.error('加载项目列表失败')
  }
}
</script>

<style scoped>
.my-results {
  height: 100%;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
