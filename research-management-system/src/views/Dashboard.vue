<template>
  <div class="dashboard">
    <div class="hero glass">
      <div>
        <div class="hero-title">早上好，{{ greetingName }}</div>
        <div class="hero-sub">这里是你今天的科研进展总览。</div>
        <div class="hero-status">
          <span class="dot"></span>
          系统运行正常
        </div>
      </div>
      <el-button type="primary" round @click="router.push('/results/create')">
        新建成果
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :xs="24" :sm="12" :md="6" v-for="stat in stats" :key="stat.key">
        <div class="stat-card glass">
          <div class="stat-icon" :style="{ background: stat.tint }">
            <component :is="stat.icon" />
          </div>
          <div class="stat-content">
            <div class="stat-label">{{ stat.label }}</div>
            <div class="stat-value">{{ stat.value }}</div>
          </div>
          <div class="stat-pill" :style="{ color: stat.accent }">
            {{ stat.tip }}
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16" class="chart-section">
      <el-col :xs="24" :md="12">
        <div class="chart-card glass">
          <div class="card-header">
            <span>成果类型分布</span>
          </div>
          <div ref="typeChartRef" class="chart-container"></div>
        </div>
      </el-col>
      <el-col :xs="24" :md="12">
        <div class="chart-card glass">
          <div class="card-header">
            <span>近年成果产出趋势</span>
          </div>
          <div ref="trendChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 最近成果列表 -->
    <div class="recent-results glass">
      <div class="card-header">
        <span>最近提交的成果</span>
        <el-button type="primary" text @click="router.push('/results/my')">
          查看全部
        </el-button>
      </div>
      <el-table :data="recentResults" v-loading="loading" border>
        <el-table-column prop="title" label="成果名称" min-width="200">
          <template #default="{ row }">
            <el-button type="primary" text @click="viewDetail(row.id)">
              {{ row.title }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" round>
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
      </el-table>
      <el-empty v-if="!loading && recentResults.length === 0" description="暂无成果记录">
        <el-button type="primary" @click="router.push('/results/create')">
          去创建成果物
        </el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Document, Tickets, TrophyBase, TrendCharts } from '@element-plus/icons-vue'
import { getMyStatistics, getMyResults, getTypePie4User } from '@/api/result'
import * as echarts from 'echarts'
import { ResultStatus } from '@/types'

const router = useRouter()
const loading = ref(false)
const statistics = ref(null)
const recentResults = ref([])
const greetingName = ref('同学')

const typeChartRef = ref(null)
const trendChartRef = ref(null)
const typeChartInstance = ref(null)
const trendChartInstance = ref(null)

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

const stats = computed(() => [
  {
    key: 'total',
    label: '总成果数',
    value: statistics.value?.totalResults || 0,
    icon: Document,
    tint: 'rgba(29, 91, 255, 0.12)',
    accent: '#1d5bff',
    tip: '全部成果'
  },
  {
    key: 'paper',
    label: '论文数',
    value: statistics.value?.paperCount || 0,
    icon: Tickets,
    tint: 'rgba(76, 126, 255, 0.12)',
    accent: '#4c7eff',
    tip: '含会议/期刊'
  },
  {
    key: 'patent',
    label: '专利数',
    value: statistics.value?.patentCount || 0,
    icon: TrophyBase,
    tint: 'rgba(0, 200, 146, 0.12)',
    accent: '#00c892',
    tip: '含授权/申请'
  },
  {
    key: 'monthly',
    label: '本月新增',
    value: statistics.value?.monthlyNew || 0,
    icon: TrendCharts,
    tint: 'rgba(255, 163, 64, 0.12)',
    accent: '#ff9d3c',
    tip: '近期提交'
  }
])

onMounted(async () => {
  await loadData()
  initCharts()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  disposeCharts()
})

async function loadData() {
  loading.value = true
  try {
    const [statsRes, resultsRes, typePieRes] = await Promise.all([
      getMyStatistics(),
      getMyResults({ page: 1, pageSize: 5 }),
      getTypePie4User()
    ])
    statistics.value = statsRes?.data || {}
    greetingName.value = statsRes?.data?.owner || '科研者'
    recentResults.value = resultsRes?.data?.list || []

    // 处理真实分布数据
    if (typePieRes?.data) {
      // 映射后端数据结构，保留 typeCode 以便进行稳定的逻辑判断
      const realTypeData = typePieRes.data.map((item: any) => ({
        name: item.typeName || '未命名',
        value: Number(item.count || 0),
        typeCode: item.typeCode || '' // 透传 typeCode
      }))
      
      statistics.value.typeDistribution = realTypeData
      
      // 基于真实数据计算统计指标 (优先使用 typeCode，中文名作为降级兜底)
      const total = realTypeData.reduce((sum: number, item: any) => sum + item.value, 0)
      
      const papers = realTypeData
        .filter((item: any) => {
           const code = String(item.typeCode || '').toUpperCase()
           const name = String(item.name || '')
           return code.includes('PAPER') || name.includes('论文')
        })
        .reduce((sum: number, item: any) => sum + item.value, 0)

      const patents = realTypeData
        .filter((item: any) => {
           const code = String(item.typeCode || '').toUpperCase()
           const name = String(item.name || '')
           return code.includes('PATENT') || name.includes('专利')
        })
        .reduce((sum: number, item: any) => sum + item.value, 0)
        
      statistics.value.totalResults = total
      statistics.value.paperCount = papers
      statistics.value.patentCount = patents
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

function initCharts() {
  disposeCharts()

  if (typeChartRef.value) {
    typeChartInstance.value = echarts.init(typeChartRef.value)
    typeChartInstance.value.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 10 },
      series: [
        {
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: { show: false },
          emphasis: {
            label: { show: true, fontSize: 16, fontWeight: 'bold' }
          },
          data: statistics.value?.typeDistribution || []
        }
      ]
    })
  }

  if (trendChartRef.value) {
    const trendData = statistics.value?.yearlyTrend || []
    trendChartInstance.value = echarts.init(trendChartRef.value)
    trendChartInstance.value.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: trendData.map((item) => item.year)
      },
      yAxis: { type: 'value' },
      series: [
        {
          type: 'line',
          data: trendData.map((item) => item.count),
          smooth: true,
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(102, 126, 234, 0.5)' },
              { offset: 1, color: 'rgba(102, 126, 234, 0.1)' }
            ])
          },
          lineStyle: { color: '#1d5bff' },
          itemStyle: { color: '#1d5bff' }
        }
      ]
    })
  }
}

function handleResize() {
  typeChartInstance.value?.resize()
  trendChartInstance.value?.resize()
}

function disposeCharts() {
  typeChartInstance.value?.dispose()
  trendChartInstance.value?.dispose()
  typeChartInstance.value = null
  trendChartInstance.value = null
}

function getStatusType(status) {
  return STATUS_TYPE_MAP[status] || 'info'
}

function getStatusText(status) {
  return STATUS_TEXT_MAP[status] || status
}

function viewDetail(id) {
  router.push(`/results/${id}`)
}
</script>

<style scoped>
.dashboard {
  padding: 6px;
  background: #f7f9fc;
}

.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px;
  border-radius: 16px;
  margin-bottom: 16px;
}

.hero-title {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
}

.hero-sub {
  color: #6b7280;
  margin-top: 4px;
}

.hero-status {
  margin-top: 8px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border-radius: 12px;
  background: #f3f6ff;
  color: #1d5bff;
  font-size: 12px;
}

.hero-status .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #22c55e;
  box-shadow: 0 0 0 4px rgba(34, 197, 94, 0.15);
}

.stat-cards {
  margin-bottom: 12px;
}

.stat-card {
  padding: 16px;
  border-radius: 14px;
  color: #0f172a;
  display: flex;
  align-items: center;
  gap: 14px;
  transition: transform 0.25s;
  cursor: pointer;
  background: #fff;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-icon {
  font-size: 24px;
  width: 46px;
  height: 46px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 2px;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
}

.stat-pill {
  margin-left: auto;
  font-size: 12px;
  font-weight: 600;
}

.chart-section {
  margin-bottom: 16px;
}

.chart-card {
  border-radius: 16px;
  padding: 16px;
  height: 360px;
}

.chart-container {
  height: 280px;
}

.recent-results {
  margin-bottom: 20px;
  padding: 16px;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 10px 24px rgba(17, 24, 39, 0.06);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-weight: 600;
}
</style>
