<template>
  <div class="results-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <div>
        <h2>科研成果 · Research Achievements</h2>
        <p class="subtitle">浏览机构内的科研成果，发现感兴趣的研究方向</p>
      </div>
    </div>

    <!-- 筛选栏 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="成果类型">
          <el-select v-model="filters.type" placeholder="全部类型" clearable style="width: 140px">
            <el-option label="全部类型" value="" />
            <el-option label="学术论文" value="paper" />
            <el-option label="发明专利" value="patent" />
            <el-option label="软件著作权" value="software" />
          </el-select>
        </el-form-item>
        <el-form-item label="年份范围">
          <el-select v-model="filters.year" placeholder="全部" style="width: 120px">
            <el-option label="全部" value="all" />
            <el-option label="近1年" value="1y" />
            <el-option label="近3年" value="3y" />
            <el-option label="近5年" value="5y" />
            
          </el-select>
        </el-form-item>
        <el-form-item label="排序方式">
          <el-select v-model="filters.sortBy" style="width: 120px">
            <el-option label="最新发布" value="latest" />
            <el-option label="最多浏览" value="hot" />
            <el-option label="最多引用" value="cited" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 成果卡片网格 -->
    <div class="results-container">
      <el-skeleton :loading="loading" :count="8" animated>
        <template #template>
          <div class="results-grid">
            <el-skeleton-item
              v-for="i in 8"
              :key="i"
              variant="rect"
              style="height: 280px; border-radius: 8px"
            />
          </div>
        </template>
        <template #default>
          <div v-if="resultList.length > 0" class="results-grid">
            <ResultCard
              v-for="item in resultList"
              :key="item.id"
              :result="item"
              @click="handleViewDetail(item)"
            />
          </div>
          <el-empty
            v-else
            description="暂无成果数据"
            :image-size="160"
          >
            <el-button type="primary" @click="handleReset">重新加载</el-button>
          </el-empty>
        </template>
      </el-skeleton>

      <!-- 分页 -->
      <div v-if="resultList.length > 0" class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[12, 24, 36, 48]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getVisibleResults, getVisibleResults4Admin } from '@/api/result.ts'
import { useUserStore } from '@/stores/user' 
import ResultCard from './components/ResultCard.vue'

const router = useRouter()
const loading = ref(false)
const resultList = ref<any[]>([])
const userStore = useUserStore()
// 筛选条件
const filters = reactive({
  type: '',
  year: 'all',
  sortBy: 'latest'
})

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 12,
  total: 0
})

onMounted(() => {
  userStore.initUserInfo()
  loadResults()
})

async function loadResults() {
  loading.value = true
  try {
    const api = userStore.isAdmin ? getVisibleResults4Admin : getVisibleResults

    const res = await api({
      ...buildQueryParams(),
      page: pagination.page,
      pageSize: pagination.pageSize
    })

    const { data } = res || {}
    resultList.value = data?.list || (Array.isArray(data) ? data : [])
    pagination.total = data?.total || resultList.value.length
  } catch (error) {
    console.error('加载成果列表失败:', error)
    ElMessage.error('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}


function handleSearch() {
  pagination.page = 1
  loadResults()
}

function handleReset() {
  Object.assign(filters, {
    type: '',
    year: '1y',
    sortBy: 'latest'
  })
  pagination.page = 1
  loadResults()
}

function handlePageChange() {
  loadResults()
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function handleSizeChange() {
  pagination.page = 1
  loadResults()
}

function handleViewDetail(item: any) {
  router.push(`/results/${item.id}`)
}

function buildQueryParams() {
  const params: Record<string, any> = {
    type: filters.type,
    sortBy: filters.sortBy
  }
  const yearRange = resolveYearRange(filters.year)
  if (yearRange) {
    params.yearRange = yearRange
  }
  return params
}

function resolveYearRange(flag: string) {
  if (!flag || flag === 'all') return null
  const years = Number(flag.replace('y', ''))
  if (Number.isNaN(years)) return null
  const currentYear = new Date().getFullYear()
  const start = (currentYear - years + 1).toString()
  const end = currentYear.toString()
  return [start, end]
}
</script>

<style scoped>
.results-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.page-header h2 {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
  color: #111827;
}

.subtitle {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.filter-card {
  border: 1px solid #eef2f7;
}

.filter-form {
  margin: 0;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.results-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.results-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.pagination {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}
</style>
