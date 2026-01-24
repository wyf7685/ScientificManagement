<template>
  <div class="interim-results">
    <!-- 顶部操作栏 -->
    <el-card class="operation-bar">
      <div class="bar-content">
        <div class="left">
          <h2>过程成果管理</h2>
          <el-tag type="info">{{ stats.totalResults }} 个成果物 / {{ stats.totalProjects }} 个项目</el-tag>
        </div>
        <div class="right">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索项目或成果物"
            :prefix-icon="Search"
            clearable
            style="width: 300px; margin-right: 12px"
            @change="handleSearch"
          />
          <el-button :icon="Refresh" @click="handleSync">同步数据</el-button>
          <el-button :icon="Download" @click="handleExport">导出列表</el-button>
        </div>
      </div>
    </el-card>

    <!-- 主体区域 -->
    <el-row :gutter="16" class="main-content">
      <!-- 左侧：项目树 -->
      <el-col :span="6">
        <el-card class="tree-card">
          <template #header>
            <div class="card-header">
              <span>项目筛选</span>
            </div>
          </template>
          
          <el-tabs v-model="activeTab" class="tree-tabs">
            <el-tab-pane label="按项目" name="project">
              <el-tree
                ref="projectTreeRef"
                :data="projectTreeData"
                :props="treeProps"
                :filter-node-method="filterNode"
                :highlight-current="true"
                node-key="id"
                @node-click="handleNodeClick"
              >
                <template #default="{ node, data }">
                  <span class="tree-node">
                    <el-icon>
                      <Calendar v-if="data.type === 'year'" />
                      <FolderOpened v-else />
                    </el-icon>
                    <span>{{ node.label }}</span>
                  </span>
                </template>
              </el-tree>
            </el-tab-pane>
            
            <el-tab-pane label="按类型" name="type">
              <el-tree
                :data="typeTreeData"
                :props="treeProps"
                :highlight-current="true"
                node-key="id"
                @node-click="handleNodeClick"
              >
                <template #default="{ node, data }">
                  <span class="tree-node">
                    <el-icon><Document /></el-icon>
                    <span>{{ node.label }}</span>
                  </span>
                </template>
              </el-tree>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>

      <!-- 右侧：成果物列表 -->
      <el-col :span="18">
        <el-card class="list-card">
          <template #header>
            <div class="card-header">
              <span v-if="currentFilter">{{ currentFilter }}</span>
              <span v-else>全部成果物</span>
              <div class="header-actions">
                <el-button 
                  v-if="selectedIds.length > 0"
                  type="primary"
                  :icon="Download"
                  @click="handleBatchDownload"
                >
                  批量下载 ({{ selectedIds.length }})
                </el-button>
              </div>
            </div>
          </template>

          <el-table
            v-loading="loading"
            :data="list"
            @selection-change="handleSelectionChange"
            stripe
          >
            <el-table-column type="selection" width="55" />
            
            <el-table-column label="成果物名称" min-width="250">
              <template #default="{ row }">
                <div class="result-name">
                  <el-icon :size="20" class="file-icon">
                    <component :is="getFileIcon(row.attachments[0]?.name)" />
                  </el-icon>
                  <div class="name-content">
                    <div class="name-text">{{ row.name }}</div>
                    <div class="name-meta">
                      <el-tag size="small" type="info">{{ row.typeLabel }}</el-tag>
                      <span class="meta-text">{{ row.projectPhase }}</span>
                    </div>
                  </div>
                </div>
              </template>
            </el-table-column>

            <el-table-column prop="projectName" label="所属项目" width="200" show-overflow-tooltip />
            
            <el-table-column label="附件" width="120">
              <template #default="{ row }">
                <span>{{ row.attachments.length }} 个文件</span>
              </template>
            </el-table-column>

            <el-table-column prop="submitter" label="提交人" width="120" />
            
            <el-table-column label="提交时间" width="150">
              <template #default="{ row }">
                {{ formatShortDate(row.submittedAt) }}
              </template>
            </el-table-column>

            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="viewDetail(row)">
                  查看详情
                </el-button>
                <el-button
                  v-if="row.source === 'process_system'"
                  type="success"
                  link
                  size="small"
                  @click="jumpToProcessSystem(row)"
                >
                  跳转过程系统
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadData"
            @current-change="loadData"
            style="margin-top: 16px; justify-content: flex-end"
          />
        </el-card>
      </el-col>
    </el-row>

    <!-- 详情抽屉 -->
    <el-drawer
      v-model="detailDrawerVisible"
      title="成果物详情"
      size="600px"
      :destroy-on-close="true"
    >
      <div v-if="currentItem" class="detail-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="成果物名称">
            {{ currentItem.name }}
          </el-descriptions-item>
          <el-descriptions-item label="成果物类型">
            <el-tag>{{ currentItem.typeLabel }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="所属项目">
            {{ currentItem.projectName }}
          </el-descriptions-item>
          <el-descriptions-item label="项目编号">
            {{ currentItem.projectCode }}
          </el-descriptions-item>
          <el-descriptions-item label="项目阶段">
            {{ currentItem.projectPhase }}
          </el-descriptions-item>
          <el-descriptions-item label="提交人">
            {{ currentItem.submitter }} ({{ currentItem.submitterDept }})
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">
            {{ formatDateTime(currentItem.submittedAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="同步时间">
            {{ formatDateTime(currentItem.syncedAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="描述">
            {{ currentItem.description || '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <h3>附件列表</h3>
        <el-table :data="currentItem.attachments" stripe>
          <el-table-column label="文件名" min-width="200">
            <template #default="{ row }">
              <div class="file-item">
                <el-icon :size="18">
                  <component :is="getFileIcon(row.name)" />
                </el-icon>
                <span>{{ row.name }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="大小" width="100">
            <template #default="{ row }">
              {{ formatFileSize(row.size) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="downloadFile(row)">
                下载
              </el-button>
              <el-button 
                v-if="canPreview(row.name)" 
                type="success" 
                link 
                size="small"
                @click="previewFile(row)"
              >
                预览
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="currentItem.tags && currentItem.tags.length > 0" style="margin-top: 16px">
          <h3>标签</h3>
          <el-tag 
            v-for="tag in currentItem.tags" 
            :key="tag"
            style="margin-right: 8px"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Download,
  Calendar,
  FolderOpened,
  Document,
  Picture
} from '@element-plus/icons-vue'
import { 
  getInterimResults, 
  getInterimResultStats,
  syncInterimResults,
  getAttachmentDownloadUrl,
  exportInterimResults
} from '@/api/interim-result'
import type { InterimResult, InterimResultStats, ProjectTreeNode } from '@/types'
import { 
  buildProjectTree, 
  buildTypeTree,
  formatFileSize,
  formatDateTime,
  formatShortDate,
  getFileIconName,
  canPreview as canPreviewFile
} from '@/utils/interim-result'

// ==================== 状态管理 ====================
const loading = ref(false)
const searchKeyword = ref('')
const activeTab = ref('project')
const detailDrawerVisible = ref(false)
const currentItem = ref<InterimResult | null>(null)
const selectedIds = ref<string[]>([])
const currentFilter = ref('')

const stats = ref<InterimResultStats>({
  totalProjects: 0,
  totalResults: 0,
  byType: {},
  byYear: {}
})

const list = ref<InterimResult[]>([])
const allItems = ref<InterimResult[]>([])

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const filterParams = reactive({
  projectId: '',
  type: '',
  year: ''
})

const projectTreeRef = ref()
const treeProps = {
  children: 'children',
  label: 'label'
}

// ==================== 计算属性 ====================
const projectTreeData = computed(() => {
  return buildProjectTree(allItems.value)
})

const typeTreeData = computed(() => {
  return buildTypeTree(allItems.value)
})

// ==================== 生命周期 ====================
onMounted(() => {
  loadData()
  loadStats()
})

watch(searchKeyword, (val) => {
  projectTreeRef.value?.filter(val)
})

// ==================== 业务方法 ====================
async function loadStats() {
  try {
    const res = await getInterimResultStats()
    stats.value = res.data
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await getInterimResults({
      ...filterParams,
      keyword: searchKeyword.value,
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    
    list.value = res.data.list
    pagination.total = res.data.total
    
    // 保存所有数据用于构建树
    if (pagination.page === 1) {
      const allRes = await getInterimResults({ pageSize: 1000 })
      allItems.value = allRes.data.list
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

function filterNode(value: string, data: ProjectTreeNode) {
  if (!value) return true
  return data.label.includes(value)
}

function handleNodeClick(data: ProjectTreeNode) {
  filterParams.projectId = ''
  filterParams.type = ''
  filterParams.year = ''
  
  if (data.type === 'project' && data.projectId) {
    filterParams.projectId = data.projectId
    currentFilter.value = data.label
  } else if (data.type === 'category' && data.categoryType) {
    filterParams.type = data.categoryType
    currentFilter.value = data.label
  } else if (data.type === 'year' && data.year) {
    filterParams.year = data.year
    currentFilter.value = data.label
  } else {
    currentFilter.value = ''
  }
  
  pagination.page = 1
  loadData()
}

function handleSearch() {
  pagination.page = 1
  loadData()
}

async function handleSync() {
  try {
    await ElMessageBox.confirm(
      '确定要从过程系统同步最新数据吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    loading.value = true
    const res = await syncInterimResults()
    ElMessage.success(`同步成功，共 ${res.data.syncCount} 条数据`)
    await loadData()
    await loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('同步失败')
    }
  } finally {
    loading.value = false
  }
}

async function handleExport() {
  try {
    loading.value = true
    await exportInterimResults({
      ...filterParams,
      keyword: searchKeyword.value
    })
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}

function handleSelectionChange(selection: InterimResult[]) {
  selectedIds.value = selection.map(item => item.id)
}

async function handleBatchDownload() {
  ElMessage.info('批量下载功能开发中')
}

function viewDetail(item: InterimResult) {
  currentItem.value = item
  detailDrawerVisible.value = true
}

function jumpToProcessSystem(item: InterimResult) {
  const url = item.sourceUrl || `http://process.example.com/projects/${item.projectId}`
  window.open(url, '_blank')
}

function getFileIcon(filename?: string) {
  if (!filename) return Document
  const iconName = getFileIconName(filename)
  const iconMap: Record<string, any> = {
    Document,
    FolderOpened,
    Picture
  }
  return iconMap[iconName] || Document
}

function canPreview(filename: string) {
  return canPreviewFile(filename)
}

function downloadFile(file: any) {
  const url = getAttachmentDownloadUrl(file.id)
  window.open(url, '_blank')
}

function previewFile(file: any) {
  // 预览功能需要后端支持
  ElMessage.info('预览功能开发中')
}
</script>

<style scoped>
.interim-results {
  padding: 20px;
  background: #f7f9fc;
  min-height: 100vh;
}

.operation-bar {
  margin-bottom: 16px;
}

.operation-bar .bar-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.operation-bar .left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.operation-bar .left h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.operation-bar .right {
  display: flex;
  align-items: center;
}

.main-content .tree-card,
.main-content .list-card {
  min-height: calc(100vh - 200px);
}

.main-content .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.tree-tabs :deep(.el-tabs__content) {
  max-height: calc(100vh - 300px);
  overflow-y: auto;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

:deep(.el-tree) {
  background: transparent;
}

:deep(.el-tree-node__content) {
  height: 36px;
  border-radius: 6px;
}

:deep(.el-tree-node__content:hover) {
  background: #f3f4f6;
}

:deep(.el-tree-node.is-current > .el-tree-node__content) {
  background: #e6f0ff;
  color: #1d5bff;
  font-weight: 500;
}

.result-name {
  display: flex;
  align-items: center;
  gap: 12px;
}

.result-name .file-icon {
  color: #1d5bff;
}

.result-name .name-content {
  flex: 1;
  min-width: 0;
}

.result-name .name-text {
  font-weight: 500;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-name .name-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #6b7280;
}

.result-name .meta-text {
  opacity: 0.8;
}

.detail-content h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}

.detail-content .file-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
