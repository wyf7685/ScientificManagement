<template>
  <div class="research-insights">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h2>研究洞察 · Research Insights</h2>
        <p class="subtitle">通过关键词分析洞察科研热点与学科关联，助力科研决策</p>
      </div>
    </div>

    <!-- 筛选区域 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="时间范围">
          <el-radio-group v-model="keywordRange" size="default">
            <el-radio-button label="1y">近1年</el-radio-button>
            <el-radio-button label="3y">近3年</el-radio-button>
            <el-radio-button label="all">全部</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Refresh" @click="loadKeywordCloud">
            刷新数据
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 词云图谱展示区域 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="chart-card" v-loading="loading">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon class="header-icon" :size="20">
                  <TrendCharts />
                </el-icon>
                <span class="header-title">研究热点词云 / 关联图谱</span>
              </div>
              <div class="header-tip">
                <el-tag size="small" type="info">节点大小代表热度，可拖拽缩放</el-tag>
              </div>
            </div>
          </template>
          
          <!-- 图表容器 -->
          <div ref="keywordChartRef" class="chart-container"></div>
          
          <!-- 空态提示 -->
          <el-empty 
            v-if="!loading && (!keywordGraph.nodes || keywordGraph.nodes.length === 0)"
            description="暂无关键词数据"
            :image-size="120"
          >
            <el-button type="primary" @click="loadKeywordCloud">重新加载</el-button>
          </el-empty>
        </el-card>
      </el-col>
    </el-row>

    <!-- 说明卡片 -->
    <el-card class="info-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon :size="18"><InfoFilled /></el-icon>
          <span>使用说明</span>
        </div>
      </template>
      <div class="info-content">
        <el-row :gutter="16">
          <el-col :span="8">
            <div class="info-item">
              <el-icon class="info-icon" color="#409EFF"><CircleCheck /></el-icon>
              <div>
                <h4>节点大小</h4>
                <p>节点越大表示该关键词出现频率越高，是当前时间范围内的研究热点</p>
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <el-icon class="info-icon" color="#67C23A"><Connection /></el-icon>
              <div>
                <h4>连线关系</h4>
                <p>连线表示关键词之间存在共现关系，揭示学科交叉与研究关联</p>
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <el-icon class="info-icon" color="#E6A23C"><Pointer /></el-icon>
              <div>
                <h4>交互操作</h4>
                <p>支持鼠标拖拽、滚轮缩放，点击节点可高亮显示相关联的关键词</p>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { Refresh, TrendCharts, InfoFilled, CircleCheck, Connection, Pointer } from '@element-plus/icons-vue'
import { getKeywordCloud } from '@/api/result'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const keywordRange = ref('1y')
const keywordGraph = ref<{ nodes: any[]; links: any[] }>({ nodes: [], links: [] })
const keywordChartRef = ref(null)
const keywordChartInstance = ref<echarts.ECharts | null>(null)

const colorPalette = ['#1d5bff', '#4c7eff', '#00c892', '#ff9d3c', '#7c3aed', '#0ea5e9', '#f97316']

onMounted(async () => {
  await loadKeywordCloud()
  await nextTick()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  disposeChart()
})

watch(keywordRange, () => loadKeywordCloud())

async function loadKeywordCloud() {
  loading.value = true
  try {
    const res = await getKeywordCloud({ range: keywordRange.value })
    keywordGraph.value = res?.data || { nodes: [], links: [] }
    await nextTick()
    renderKeywordChart()
    ElMessage.success('数据加载成功')
  } catch (error) {
    console.error('加载关键词数据失败:', error)
    ElMessage.error('加载数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function renderKeywordChart() {
  if (!keywordChartRef.value) return
  if (!keywordChartInstance.value) {
    keywordChartInstance.value = echarts.init(keywordChartRef.value)
  }

  const nodes = (keywordGraph.value.nodes || []).map((item, index) => ({
    ...item,
    symbolSize: Math.max(14, Math.min(42, item.value)),
    itemStyle: { color: colorPalette[index % colorPalette.length] }
  }))

  const categories = Array.from(new Set(nodes.map((item) => item.category).filter(Boolean))).map(
    (name) => ({ name })
  )

  keywordChartInstance.value.setOption(
    {
      tooltip: {
        formatter: (params: any) => `${params.data.name}<br/>热度: ${params.data.value}`
      },
      legend: { 
        data: categories.map((item) => item.name), 
        top: 10,
        textStyle: { fontSize: 12 }
      },
      series: [
        {
          type: 'graph',
          layout: 'force',
          roam: true,
          force: { repulsion: 90, edgeLength: [50, 140] },
          data: nodes,
          links: keywordGraph.value.links || [],
          categories,
          label: { show: true, formatter: '{b}', color: '#0f172a', fontSize: 12 },
          lineStyle: { color: '#cbd5e1', opacity: 0.6 },
          emphasis: { focus: 'adjacency', lineStyle: { width: 3 } }
        }
      ]
    },
    true
  )
}

function handleResize() {
  keywordChartInstance.value?.resize()
}

function disposeChart() {
  keywordChartInstance.value?.dispose()
  keywordChartInstance.value = null
}
</script>

<style scoped>
.research-insights {
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

.chart-card {
  min-height: 600px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  color: #1d5bff;
}

.header-title {
  font-size: 16px;
  color: #111827;
}

.header-tip {
  display: flex;
  align-items: center;
  gap: 8px;
}

.chart-container {
  width: 100%;
  height: 500px;
}

.info-card {
  border: 1px solid #eef2f7;
  background: linear-gradient(135deg, #f5f7fa 0%, #f9fafb 100%);
}

.info-content {
  padding: 8px 0;
}

.info-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.info-icon {
  flex-shrink: 0;
  margin-top: 2px;
}

.info-item h4 {
  margin: 0 0 6px;
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.info-item p {
  margin: 0;
  font-size: 13px;
  color: #6b7280;
  line-height: 1.6;
}
</style>


