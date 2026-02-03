<template>
  <div class="search-results">
    <el-card class="search-panel">
      <div class="panel-header">
        <div>
          <div class="panel-title">成果检索</div>
          <div class="panel-desc">组合关键词、类型、年份、项目等条件精准定位成果</div>
        </div>
        <el-tag effect="plain" type="info" class="panel-total">匹配 {{ pagination.total }} 条</el-tag>
      </div>

      <el-form :model="searchForm" label-width="90px" class="search-form">
        <el-row :gutter="18" class="form-grid">
          <el-col :xs="24" :md="12" :lg="12">
            <el-form-item label="成果名称">
              <el-input
              v-model="searchForm.title"
              placeholder="输入成果名称/标题"
              clearable
              @keyup.enter="handleSearch"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12" :lg="12">
            <el-form-item label="关键词">
              <el-input v-model="searchForm.keyword" placeholder="搜索关键词" clearable @keyup.enter="handleSearch"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12" :lg="6">
            <el-form-item label="成果类型">
              <el-select
                v-model="searchForm.type"
                placeholder="全部类型"
                clearable
                filterable
                :loading="typeLoading"
              >
              <!-- 如果你希望“全部类型”也作为选项显示，可以保留这行；不需要就删掉 -->
                <el-option label="全部类型" :value="''" />
                <el-option
                  v-for="t in resultTypes"
                  :key="t.type_code"
                  :label="t.type_name"
                  :value="t.type_code"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12" :lg="6">
            <el-form-item label="作者">
              <el-input v-model="searchForm.author" placeholder="作者姓名" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12" :lg="8">
            <el-form-item label="年份范围">
              <el-date-picker
                v-model="searchForm.yearRange"
                type="yearrange"
                range-separator="至"
                start-placeholder="开始年份"
                end-placeholder="结束年份"
                value-format="YYYY"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12" :lg="8">
            <el-form-item label="项目">
              <el-select v-model="searchForm.projectId" placeholder="全部项目" clearable filterable>
                <el-option :label="'全部项目'" :value="''" />
                <el-option
                  v-for="project in projects"
                  :key="project.id"
                  :label="getProjectLabel(project)"
                  :value="project.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <div class="search-actions">
          <div class="search-hint">
            <el-icon class="hint-icon">
              <InfoFilled />
            </el-icon>
            支持输入多个关键词，以空格分隔可提高命中率
          </div>
          <div class="action-buttons">
            <el-button round plain :icon="RefreshIcon" @click="handleReset">重置</el-button>
            <el-button round type="primary" :icon="SearchIcon" @click="handleSearch">查询</el-button>
            <el-button
              v-if="canExport"
              round
              type="success"
              plain
              class="export-btn"
              :icon="DownloadIcon"
              :loading="exporting"
              :disabled="loading"
              @click="handleExport"
            >
              导出
            </el-button>
          </div>
        </div>
      </el-form>
    </el-card>

    <el-card style="margin-top: 20px">
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="title" label="成果名称" min-width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">
              {{ row.title }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="120" />
        <el-table-column prop="authors" label="作者" width="200">
          <template #default="{ row }">
            {{ row.authors?.join(', ') }}
          </template>
        </el-table-column>
        <el-table-column prop="year" label="年份" width="100" />
        <el-table-column prop="projectName" label="所属项目" min-width="180">
          <template #default="{ row }">
            <span v-if="row.projectName">{{ row.projectName }} ({{ row.projectCode }})</span>
            <span v-else class="text-muted">无所属/其他</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          layout="total, prev, pager, next"
          @current-change="handleSearch"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { InfoFilled, Search as SearchIcon, RefreshRight as RefreshIcon, Download as DownloadIcon } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getVisibleResults, getResults,getMyResults, exportResults } from '@/api/result'
import { getProjects } from '@/api/project'
import { getResultTypes } from '@/api/result'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const tableData = ref([])
const projects = ref([])
const exporting = ref(false)
const canExport = computed(() => userStore.isAdmin)

const resultTypes = ref<AchievementType[]>([])
const typeLoading = ref(false)

const searchForm = reactive({
  title: '',
  keyword: '',
  type: '',
  yearRange: [],
  author: '',
  projectId: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

interface AchievementType {
  id?: number
  documentId?: string
  type_code: string
  type_name: string
  description?: string
  is_delete?: number
}

onMounted(() => {
  initFromQuery()
  loadProjects()
  loadResultTypes()
  handleSearch()
})

async function loadResultTypes() {
  typeLoading.value = true
  try {
    const res = await getResultTypes()
    const list = res?.data ?? []
    // 过滤掉删除的
    resultTypes.value = list.filter((t: AchievementType) => (t.is_delete ?? 0) === 0)
  } catch (e) {
    ElMessage.error('加载成果类型失败')
    resultTypes.value = []
  } finally {
    typeLoading.value = false
  }
}

async function handleSearch() {
  // console.log('title(model)=', JSON.stringify(searchForm.title))
  // console.log('keyword(model)=', JSON.stringify(searchForm.keyword))
  loading.value = true
  try {
    const params = {
      ...getSearchParams(),
      page: pagination.page,
      pageSize: pagination.pageSize
    }

    // 管理员使用管理员接口，普通用户使用用户端检索接口
    const res = userStore.isAdmin
       ? await getResults(params, true)
       : await getMyResults(params)

    const { data } = res || {}
    tableData.value = data?.list || []
    pagination.total = data?.total || 0
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

function handleReset() {
  Object.assign(searchForm, {
    title: '',
    keyword: '',
    type: '',
    yearRange: null,
    author: '',
    projectId: ''
  })
  pagination.page = 1
  handleSearch()
}

function viewDetail(row) {
  router.push(`/results/${row.id}`)
}

async function loadProjects() {
  try {
    const res = await getProjects()
    projects.value = res?.data || []
  } catch (error) {
    ElMessage.error('加载项目列表失败')
  }
}

function getProjectLabel(project) {
  if (!project) return ''
  return `${project.name} (${project.code})`
}

function getSearchParams() {
  const params: Record<string, any> = {
    title: searchForm.title,
    keyword: searchForm.keyword,
    type: searchForm.type,
    author: searchForm.author,
    projectId: searchForm.projectId
  }

  if (Array.isArray(searchForm.yearRange) && searchForm.yearRange.length === 2) {
    params.yearRange = [...searchForm.yearRange]
  }

  return params
}

function initFromQuery() {
  const { projectId, keyword } = route.query
  if (projectId && typeof projectId === 'string') {
    searchForm.projectId = projectId
  }
  if (keyword && typeof keyword === 'string') {
    searchForm.keyword = keyword
  }
}

async function handleExport() {
  if (!canExport.value || exporting.value) return
  exporting.value = true
  try {
    const params = {
      ...getSearchParams(),
      page: 1,
      pageSize: Math.max(pagination.total, pagination.pageSize) || 100
    }
    const blob = await exportResults(params)
    const fileName = `成果检索_${dayjs().format('YYYYMMDD_HHmmss')}.xlsx`
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败', error)
    ElMessage.error('导出失败，请稍后重试')
  } finally {
    exporting.value = false
  }
}
</script>

<style scoped>
.search-panel {
  border: none;
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.08);
  border-radius: 18px;
  padding-bottom: 26px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.panel-title {
  font-size: 20px;
  font-weight: 600;
  color: #0f172a;
}

.panel-desc {
  margin-top: 4px;
  font-size: 13px;
  color: #64748b;
}

.panel-total {
  border-radius: 999px;
  font-size: 13px;
  padding: 4px 12px;
}

.search-form {
  padding-top: 6px;
}

.form-grid :deep(.el-form-item) {
  margin-bottom: 18px;
}

.search-actions {
  margin-top: 12px;
  padding-top: 16px;
  border-top: 1px solid #eef2ff;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
}

.search-hint {
  color: #94a3b8;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.hint-icon {
  color: #1d5bff;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.action-buttons .el-button {
  min-width: 96px;
  padding: 0 18px;
  font-weight: 500;
}

.action-buttons .el-button--primary {
  box-shadow: 0 12px 25px rgba(35, 93, 255, 0.25);
}

.export-btn {
  border-color: rgba(25, 135, 84, 0.35);
  color: #15803d;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
