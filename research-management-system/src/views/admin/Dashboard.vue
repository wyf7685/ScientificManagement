<template>
  <div class="admin-dashboard">
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6" v-for="stat in stats" :key="stat.key">
        <div class="stat-card" :style="{ background: stat.color }">
          <div class="stat-icon">
            <component :is="stat.icon" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-section">
      <el-col :xs="24" :md="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>成果分布</span>
              <div class="card-actions">
                <el-radio-group v-model="distributionDimension" size="small">
                  <el-radio-button label="type">类型</el-radio-button>
                  <el-radio-button label="indexLevel">收录级别</el-radio-button>
                  <el-radio-button label="department">部门</el-radio-button>
                  <el-radio-button label="team">团队</el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>
          <div ref="distributionChartRef" class="chart-container large"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>堆叠趋势 + 引用</span>
              <div class="card-actions">
                <el-radio-group v-model="trendDimension" size="small">
                  <el-radio-button label="type">类型</el-radio-button>
                  <el-radio-button label="indexLevel">收录级别</el-radio-button>
                  <el-radio-button label="department">部门</el-radio-button>
                  <el-radio-button label="team">团队</el-radio-button>
                </el-radio-group>
                <el-radio-group v-model="trendRange" size="small" class="range-radio">
                  <el-radio-button label="3y">近3年</el-radio-button>
                  <el-radio-button label="5y">近5年</el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>
          <div ref="stackedChartRef" class="chart-container large"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-section">
      <el-col :span="24">
        <el-card class="recent-results">
          <template #header>
            <div class="card-header">
              <span>最新入库成果</span>
              <el-button type="primary" size="small" link @click="$router.push('/admin/results')">
                查看全部
              </el-button>
            </div>
          </template>
          <el-table :data="recentResults" v-loading="loading" size="default">
            <el-table-column prop="title" label="成果名称" min-width="240" show-overflow-tooltip />
            <el-table-column prop="type" label="类型" width="120" />
            <el-table-column prop="author" label="作者" width="140" show-overflow-tooltip />
            <el-table-column prop="department" label="所属部门" width="160" show-overflow-tooltip />
            <el-table-column prop="createdAt" label="入库时间" width="160" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { Document, Tickets, TrophyBase, TrendCharts } from '@element-plus/icons-vue'
import { getStatistics, getResults, getAdvancedDistribution, getStackedTrend, getTypePie } from '@/api/result'
import * as echarts from 'echarts'

const loading = ref(false)
const statistics = ref(null)
const recentResults = ref([])

const distributionDimension = ref('type')
const trendDimension = ref('type')
const trendRange = ref('5y')

const distributionData = ref<any[]>([])
const indexLevelDistribution = ref<any[]>([])
const distributionEmpty = ref(false)
const stackedTimeline = ref<string[]>([])
const stackedSeries = ref<any[]>([])
const citationSeries = ref<number[]>([])

const distributionChartRef = ref(null)
const stackedChartRef = ref(null)
const distributionChartInstance = ref<echarts.ECharts | null>(null)
const stackedChartInstance = ref<echarts.ECharts | null>(null)

const colorPalette = ['#1d5bff', '#4c7eff', '#00c892', '#ff9d3c', '#7c3aed', '#0ea5e9', '#f97316']

const stats = ref([
  {
    key: 'total',
    label: '总成果数',
    value: 0,
    icon: Document,
    color: 'var(--primary-gradient)'
  },
  {
    key: 'paper',
    label: '论文数',
    value: 0,
    icon: Tickets,
    color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  {
    key: 'patent',
    label: '专利数',
    value: 0,
    icon: TrophyBase,
    color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  },
  {
    key: 'monthly',
    label: '本月新增',
    value: 0,
    icon: TrendCharts,
    color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
  }
])

onMounted(async () => {
  await Promise.all([
    loadSummary(),
    loadDistribution(),
    loadStackedTrend()
  ])
  await nextTick()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  disposeCharts()
})

watch(distributionDimension, () => loadDistribution())
watch([trendDimension, trendRange], () => loadStackedTrend())

async function loadSummary() {
  loading.value = true
  try {
    const [statsRes, resultsRes] = await Promise.all([
      getStatistics(),
      getResults({ page: 1, pageSize: 10 })
    ])
    const statsData = statsRes?.data || {}
    const resultsData = resultsRes?.data || {}
    
    statistics.value = statsData
    recentResults.value = resultsData.list || []

    if (stats.value[0]) stats.value[0].value = statsData.totalResults
    if (stats.value[1]) stats.value[1].value = statsData.paperCount
    if (stats.value[2]) stats.value[2].value = statsData.patentCount
    if (stats.value[3]) stats.value[3].value = statsData.monthlyNew
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

async function loadDistribution() {
  try {
    if (distributionDimension.value === 'type') {
      const res = await getTypePie()
      const list = res?.data || []
      const aggregated = new Map<string, { name: string; value: number }>()
      list.forEach((item: any) => {
        const rawCode = item.typeCode ? item.typeCode.toString().trim() : ''
        const rawName = item.typeName ? item.typeName.toString().trim() : ''
        const key = (rawCode || rawName || '未命名').replace(/\s+/g, ' ').toUpperCase()
        const displayName = rawName || rawCode || '未命名'
        const value = Number(item.count || 0)
        if (!key || !Number.isFinite(value) || value <= 0) return

        if (!aggregated.has(key)) {
          aggregated.set(key, { name: displayName, value })
          return
        }
        const current = aggregated.get(key)!
        current.value += value
        if (!current.name && displayName) {
          current.name = displayName
        }
      })
      distributionData.value = Array.from(aggregated.values())
      distributionEmpty.value = distributionData.value.length === 0
      indexLevelDistribution.value = []
    } else {
      const res = await getAdvancedDistribution({ dimension: distributionDimension.value })
      distributionData.value = res?.data?.items || []
      indexLevelDistribution.value = res?.data?.indexLevelItems || []
      distributionEmpty.value = distributionData.value.length === 0
    }
    renderDistributionChart()
  } catch (error) {
    console.error('加载分布数据失败:', error)
    distributionEmpty.value = true
  }
}

async function loadStackedTrend() {
  try {
    const res = await getStackedTrend({
      dimension: trendDimension.value,
      range: trendRange.value
    })
    stackedTimeline.value = res?.data?.timeline || []
    stackedSeries.value = res?.data?.stacks || []
    citationSeries.value = res?.data?.citations || []
    renderStackedChart()
  } catch (error) {
    console.error('加载趋势数据失败:', error)
  }
}

function renderDistributionChart() {
  if (!distributionChartRef.value) return
  if (!distributionChartInstance.value) {
    distributionChartInstance.value = echarts.init(distributionChartRef.value)
  }
  distributionChartInstance.value.clear()

  const hasData = !distributionEmpty.value && distributionData.value.length > 0
  distributionChartInstance.value.setOption(
    {
      color: colorPalette,
      tooltip: { trigger: 'item' },
      legend: { bottom: 10, show: hasData },
      graphic: hasData
        ? []
        : [
            {
              type: 'text',
              left: 'center',
              top: 'middle',
              style: {
                text: '暂无数据',
                fill: '#94a3b8',
                fontSize: 14
              }
            }
          ],
      series: hasData
        ? [
            {
              type: 'pie',
              radius: ['46%', '74%'],
              label: { show: false },
              itemStyle: { borderColor: '#fff', borderWidth: 2 },
              emphasis: {
                label: { show: true, fontWeight: 'bold', fontSize: 14 }
              },
              data: distributionData.value
            }
          ]
        : []
    },
    true
  )
}

function renderStackedChart() {
  if (!stackedChartRef.value) return
  if (!stackedChartInstance.value) {
    stackedChartInstance.value = echarts.init(stackedChartRef.value)
  }

  const barSeries = (stackedSeries.value || []).map((item, index) => ({
    name: item.name,
    type: 'bar',
    stack: 'total',
    barMaxWidth: 38,
    emphasis: { focus: 'series' },
    itemStyle: { color: colorPalette[index % colorPalette.length] },
    data: item.data || []
  }))

  const lineSeries = {
    name: '引用',
    type: 'line',
    yAxisIndex: 1,
    smooth: true,
    symbol: 'circle',
    symbolSize: 8,
    itemStyle: { color: '#ff6b00' },
    lineStyle: { color: '#ff6b00', width: 3 },
    data: citationSeries.value || []
  }

  stackedChartInstance.value.setOption(
    {
      color: colorPalette,
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      legend: { bottom: 10 },
      grid: { left: '3%', right: '4%', bottom: 55, containLabel: true },
      xAxis: { type: 'category', data: stackedTimeline.value },
      yAxis: [
        { type: 'value', name: '数量' },
        { type: 'value', name: '引用', splitLine: { show: false } }
      ],
      series: [...barSeries, lineSeries]
    },
    true
  )
}

function handleResize() {
  distributionChartInstance.value?.resize()
  stackedChartInstance.value?.resize()
}

function disposeCharts() {
  distributionChartInstance.value?.dispose()
  stackedChartInstance.value?.dispose()
  distributionChartInstance.value = null
  stackedChartInstance.value = null
}
</script>

<style scoped>
.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  padding: 24px;
  border-radius: 8px;
  color: white;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: transform 0.3s;
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-icon {
  font-size: 48px;
  opacity: 0.8;
}

.stat-value {
  font-size: 32px;
  font-weight: 600;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.chart-section {
  margin-bottom: 20px;
}

.chart-container {
  height: 300px;
}

.chart-container.large {
  height: 340px;
}

.chart-card {
  min-height: 360px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.range-radio {
  margin-left: 6px;
}

.recent-results {
  min-height: 400px;
}
</style>
