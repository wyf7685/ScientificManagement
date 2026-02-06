<template>
  <div class="demand-insights">
    <div class="page-header">
      <div>
        <h2>需求洞察与智能匹配</h2>
        <p class="subtitle">汇总外部企业痛点，智能关联研究院成果，辅助科技成果转化</p>
      </div>
      <el-button :loading="listLoading" @click="handleSearch">
        <el-icon><Refresh /></el-icon> 刷新
      </el-button>
    </div>

    <el-card class="filter-card glass">
      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="关键词">
          <el-input
            v-model="filters.keyword"
            placeholder="需求标题、摘要、行业"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="行业">
          <el-select v-model="filters.industry" placeholder="全部" clearable>
            <el-option v-for="opt in industryOptions" :key="opt" :label="opt" :value="opt" />
          </el-select>
        </el-form-item>
        <el-form-item label="地域">
          <el-select v-model="filters.region" placeholder="全部" clearable>
            <el-option v-for="opt in regionOptions" :key="opt" :label="opt" :value="opt" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable>
            <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源类型">
          <el-select v-model="filters.sourceCategory" placeholder="全部" clearable>
            <el-option v-for="opt in sourceCategoryOptions" :key="opt" :label="opt" :value="opt" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="listLoading" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card glass">
      <el-table
        :data="demandList"
        :loading="listLoading"
        @row-click="handleRowClick"
        height="560"
        border
        highlight-current-row
      >
        <el-table-column prop="title" label="需求标题" min-width="240">
          <template #default="{ row }">
            <div class="title-cell">
              <div class="title-text">{{ row.title }}</div>
              <div class="title-tags">
                <el-tag v-for="tag in row.tags || []" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
              </div>
            </div>
            <div class="meta-line">
              <el-tag size="small" type="info" effect="plain">
                {{ row.sourceCategory || '来源未知' }}
              </el-tag>
              <span class="muted">{{ row.sourceSite }}</span>
              <span class="dot">·</span>
              <span class="muted">{{ row.industry || '未知行业' }}</span>
              <span class="dot">·</span>
              <span class="muted">{{ row.region || '未知地域' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="capturedAt" label="抓取时间" width="120" />
        <el-table-column prop="confidence" label="可信度" width="120">
          <template #default="{ row }">
            <el-progress
              :percentage="Math.round((row.confidence || 0) * 100)"
              :stroke-width="10"
              :color="progressColor(row.confidence || 0)"
              :format="(p) => `${p}%`"
            />
          </template>
        </el-table-column>
        <el-table-column label="匹配度" width="120">
          <template #default="{ row }">
            <div class="score-pill" :class="scoreClass(row.bestMatchScore)">
              {{ formatScore(row.bestMatchScore) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="plain">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click.stop="handleRowClick(row)">查看匹配</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-footer">
        <el-pagination
          background
          layout="prev, pager, next, jumper, total"
          :total="pagination.total"
          :page-size="pagination.pageSize"
          :current-page="pagination.page"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-drawer
      v-model="drawerVisible"
      title="需求详情与匹配"
      size="640px"
      destroy-on-close
    >
      <el-skeleton :loading="detailLoading" animated :rows="6">
        <template #default>
          <div class="drawer-header">
            <div>
              <div class="drawer-title">{{ selectedDemand?.title }}</div>
              <div class="drawer-sub">
                <el-tag size="small" type="info" effect="plain">
                  {{ selectedDemand?.sourceCategory || '来源未知' }}
                </el-tag>
                <span>{{ selectedDemand?.sourceSite }} / {{ selectedDemand?.capturedAt }}</span>
                <span class="dot">·</span>
                <span>{{ selectedDemand?.industry || '未知行业' }} / {{ selectedDemand?.region || '未知地域' }}</span>
              </div>
            </div>
            <div class="drawer-actions">
              <el-tag :type="statusType(selectedDemand?.status)">{{ statusLabel(selectedDemand?.status) }}</el-tag>
              <el-button size="small" :loading="rematching" @click="handleRematch">
                <el-icon><Refresh /></el-icon>
                重新匹配
              </el-button>
            </div>
          </div>

          <div class="section">
            <div class="section-title">需求摘要</div>
            <p class="section-body">{{ selectedDemand?.summary }}</p>
            <p v-if="selectedDemand?.llmSummary" class="llm-summary">
              智能摘要：{{ selectedDemand.llmSummary }}
            </p>
            <div class="keyword-line">
              <el-tag
                v-for="kw in selectedDemand?.keywords || []"
                :key="kw"
                size="small"
                effect="plain"
              >
                {{ kw }}
              </el-tag>
            </div>
            <el-link
              v-if="selectedDemand?.sourceUrl"
              :href="selectedDemand.sourceUrl"
              target="_blank"
              type="primary"
            >
              查看来源
            </el-link>
          </div>

          <div class="section">
            <div class="section-title">
              智能匹配结果
              <span class="muted">（按匹配度排序）</span>
            </div>
            <el-empty
              v-if="matches.length === 0"
              description="暂无匹配结果，尝试重新匹配或降低阈值"
            />
            <div v-else class="match-list">
              <div v-for="item in matches" :key="item.resultId" class="match-card">
                <div class="match-header">
                  <div>
                    <div class="match-title">{{ item.resultTitle }}</div>
                    <div class="match-sub">
                      <span>{{ item.resultType }}</span>
                      <span class="dot">·</span>
                      <span>{{ item.owner || '负责人待定' }}</span>
                      <span class="dot">·</span>
                      <span>{{ item.updatedAt ? formatDateTime(item.updatedAt) : '更新时间未知' }}</span>
                    </div>
                  </div>
                  <div class="score-pill" :class="scoreClass(item.matchScore)">
                    {{ formatScore(item.matchScore) }}
                  </div>
                </div>
                <div class="reason-block">
                  <div class="reason-title">匹配理由</div>
                  <p class="reason-text">{{ item.reason || '模型未返回详细理由' }}</p>
                  <div v-if="item.sourceSnippet" class="snippet">
                    <span class="snippet-label">原文片段</span>
                    <p class="snippet-text">{{ item.sourceSnippet }}</p>
                  </div>
                </div>
                <div class="card-actions">
                  <el-button type="primary" link @click="goResultDetail(item.resultId)">查看成果</el-button>
                </div>
              </div>
            </div>
          </div>
        </template>
      </el-skeleton>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh } from '@element-plus/icons-vue'
import { getDemands, getDemandDetail, rematchDemand } from '@/api/demand'
import { DemandItem, DemandMatch } from '@/types'
import { useRequest, useAsyncAction } from '@/composables/useErrorHandler'
import { AppError, ErrorType } from '@/utils/errorHandler'
import { formatDateTime } from '@/utils/date'

const router = useRouter()

const filters = reactive({
  keyword: '',
  industry: '',
  region: '',
  sourceCategory: '',
  status: ''
})

const industryOptions = ['医疗健康', '能源储能', '轨道交通', '制造业', '信息技术']
const regionOptions = ['北京', '上海', '深圳', '杭州', '广州', '全国']
const sourceCategoryOptions = [
  '政府/园区平台',
  '招投标网站',
  '企业官网/年报',
  '垂直行业论坛/新闻'
]
const statusOptions = [
  { label: '未匹配', value: 'unmatched' },
  { label: '已匹配', value: 'matched' },
  { label: '跟进中', value: 'in_follow_up' }
]

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const demandList = ref<DemandItem[]>([])
const drawerVisible = ref(false)
const detailLoading = ref(false)
const selectedDemand = ref<DemandItem | null>(null)
const matches = ref<DemandMatch[]>([])

const { loading: listLoading, execute: loadDemands } = useRequest(
  async () => {
    const res = await getDemands({
      ...filters,
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    pagination.total = res?.data?.total || 0
    return res?.data?.list || []
  },
  {
    immediate: false,
    onSuccess: (list) => {
      demandList.value = list || []
    }
  }
)

function handleSearch() {
  pagination.page = 1
  loadDemands()
}

function handleReset() {
  filters.keyword = ''
  filters.industry = ''
  filters.region = ''
  filters.sourceCategory = ''
  filters.status = ''
  handleSearch()
}

function handlePageChange(page: number) {
  pagination.page = page
  loadDemands()
}

function progressColor(value: number) {
  if (value >= 0.8) return '#22c55e'
  if (value >= 0.6) return '#a855f7'
  return '#3b82f6'
}

function scoreClass(score?: number) {
  if (!score && score !== 0) return 'score-neutral'
  if (score >= 0.8) return 'score-high'
  if (score >= 0.6) return 'score-mid'
  return 'score-low'
}

function formatScore(score?: number) {
  if (score === undefined || score === null) return '--'
  return `${Math.round(score * 100)}%`
}

function statusType(status?: string) {
  if (status === 'matched') return 'success'
  if (status === 'in_follow_up') return 'warning'
  return 'info'
}

function statusLabel(status?: string) {
  if (status === 'matched') return '已匹配'
  if (status === 'in_follow_up') return '跟进中'
  if (status === 'unmatched') return '未匹配'
  return status || '未知'
}

async function handleRowClick(row: DemandItem) {
  await loadDetail(row.id)
}

async function loadDetail(id: string) {
  detailLoading.value = true
  try {
    const res = await getDemandDetail(id)
    selectedDemand.value = res?.data || null
    matches.value = res?.data?.matches || []
    drawerVisible.value = true
  } catch (error) {
    console.error(error)
  } finally {
    detailLoading.value = false
  }
}

const { executing: rematching, execute: runRematch } = useAsyncAction(
  async () => {
    const id = selectedDemand.value?.id
    if (!id) {
      throw new AppError('缺少需求 ID', ErrorType.VALIDATION, 'MISSING_DEMAND_ID')
    }
    await rematchDemand(id)
    await loadDetail(id)
  },
  { successMessage: '已重新匹配，结果更新中' }
)

async function handleRematch() {
  await runRematch()
}

function goResultDetail(resultId: string) {
  router.push(`/results/${resultId}`)
}

onMounted(() => {
  loadDemands()
})
</script>

<style scoped>
.demand-insights {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h2 {
  margin: 0 0 6px;
}

.subtitle {
  margin: 0;
  color: #6b7280;
}

.filter-card {
  border: 1px solid #eef2f7;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.table-card {
  border: 1px solid #eef2f7;
}

.table-footer {
  display: flex;
  justify-content: flex-end;
  padding: 12px 0 0;
}

.title-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.title-text {
  font-weight: 700;
  color: #111827;
}

.title-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.meta-line {
  color: #9ca3af;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.muted {
  color: #9ca3af;
}

.dot {
  color: #d1d5db;
}

.score-pill {
  padding: 4px 10px;
  border-radius: 12px;
  font-weight: 700;
  text-align: center;
  min-width: 64px;
}

.score-high {
  background: #ecfdf3;
  color: #15803d;
}

.score-mid {
  background: #f5f3ff;
  color: #6b21a8;
}

.score-low {
  background: #eff6ff;
  color: #1d4ed8;
}

.score-neutral {
  background: #f3f4f6;
  color: #6b7280;
}

.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.drawer-title {
  font-size: 18px;
  font-weight: 700;
}

.drawer-sub {
  color: #6b7280;
  font-size: 13px;
  display: flex;
  gap: 6px;
  align-items: center;
}

.drawer-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.section {
  margin-top: 16px;
  padding: 12px;
  border: 1px solid #eef2f7;
  border-radius: 12px;
  background: #f9fafb;
}

.section-title {
  font-weight: 700;
  margin-bottom: 8px;
  display: flex;
  gap: 6px;
  align-items: center;
}

.section-body {
  color: #1f2937;
  line-height: 1.6;
  margin: 0 0 8px;
}

.llm-summary {
  background: #eef2ff;
  padding: 8px;
  border-radius: 8px;
  color: #4b5563;
}

.keyword-line {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin: 8px 0;
}

.match-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.match-card {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 12px;
  background: #fff;
  box-shadow: 0 6px 16px rgba(17, 24, 39, 0.08);
}

.match-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
}

.match-title {
  font-weight: 700;
  margin-bottom: 4px;
}

.match-sub {
  color: #6b7280;
  font-size: 13px;
  display: flex;
  gap: 6px;
  align-items: center;
}

.reason-block {
  margin-top: 8px;
}

.reason-title {
  font-weight: 600;
  margin-bottom: 4px;
}

.reason-text {
  color: #1f2937;
  margin: 0 0 8px;
  line-height: 1.5;
}

.snippet {
  background: #f9fafb;
  border: 1px dashed #d1d5db;
  border-radius: 8px;
  padding: 8px;
}

.snippet-label {
  font-size: 12px;
  color: #6b7280;
}

.snippet-text {
  margin: 4px 0 0;
  color: #374151;
  line-height: 1.5;
}

.card-actions {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}
</style>
