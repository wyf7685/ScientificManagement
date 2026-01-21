<template>
  <div class="expert-reviews">
    <el-card>
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="待审核" name="pending">
          <el-table :data="tableData" v-loading="loading">
            <el-table-column prop="title" label="成果名称" min-width="200" />
            <el-table-column prop="type" label="类型" width="120" />
            <el-table-column label="所属项目" width="200">
              <template #default="{ row }">
                <template v-if="row.projectName">
                  <span>{{ row.projectName }}</span>
                  <span class="text-muted"> ({{ row.projectCode }})</span>
                </template>
                <span v-else class="text-muted">无所属/其他</span>
              </template>
            </el-table-column>
            <el-table-column prop="createdBy" label="提交人" width="120" />
            <el-table-column prop="createdAt" label="提交时间" width="180" />
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="handleReview(row)">
                  审核
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="已审核" name="reviewed">
          <el-table :data="tableData" v-loading="loading">
            <el-table-column prop="title" label="成果名称" min-width="200" />
            <el-table-column prop="type" label="类型" width="120" />
            <el-table-column label="所属项目" width="200">
              <template #default="{ row }">
                <template v-if="row.projectName">
                  <span>{{ row.projectName }}</span>
                  <span class="text-muted"> ({{ row.projectCode }})</span>
                </template>
                <span v-else class="text-muted">无所属/其他</span>
              </template>
            </el-table-column>
            <el-table-column prop="reviewResult" label="审核结果" width="100">
              <template #default="{ row }">
                <el-tag :type="row.reviewResult === 'approve' ? 'success' : 'danger'">
                  {{ row.reviewResult === 'approve' ? '通过' : '驳回' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="reviewedAt" label="审核时间" width="180" />
          </el-table>
        </el-tab-pane>
      </el-tabs>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          :total="pagination.total"
          layout="total, prev, pager, next"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 审核对话框 -->
    <el-dialog v-model="reviewDialog" title="审核成果" width="900px">
      <div v-if="currentResult">
        <!-- 顶部操作栏 -->
        <div class="dialog-header-actions">
          <el-button type="primary" link @click="viewResultDetail">
            <el-icon><View /></el-icon>
            查看完整详情
          </el-button>
        </div>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="标题" :span="2">
            {{ currentResult.title }}
          </el-descriptions-item>
          <el-descriptions-item label="类型">
            {{ currentResult.type }}
          </el-descriptions-item>
          <el-descriptions-item label="年份">
            {{ currentResult.year }}
          </el-descriptions-item>
          <el-descriptions-item label="所属项目" :span="2">
            <template v-if="currentResult.projectName">
              {{ currentResult.projectName }} ({{ currentResult.projectCode }})
            </template>
            <span v-else class="text-muted">无所属/其他</span>
          </el-descriptions-item>
          <el-descriptions-item label="提交人">
            {{ currentResult.createdBy }}
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">
            {{ currentResult.createdAt }}
          </el-descriptions-item>
          <el-descriptions-item label="作者" :span="2">
            {{ currentResult.authors?.join(', ') || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="关键词" :span="2">
            <el-tag v-for="kw in currentResult.keywords" :key="kw" style="margin-right: 8px">
              {{ kw }}
            </el-tag>
            <span v-if="!currentResult.keywords?.length" class="text-muted">-</span>
          </el-descriptions-item>
          <el-descriptions-item label="摘要" :span="2">
            <div class="abstract-content">
              {{ currentResult.abstract || '-' }}
            </div>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 附件列表 -->
        <div v-if="currentResult.attachments?.length" class="attachments-section">
          <el-divider content-position="left">
            <span style="font-weight: 600;">附件列表</span>
          </el-divider>
          <div class="attachment-list">
            <div v-for="file in currentResult.attachments" :key="file.id" class="attachment-item">
              <el-icon><Document /></el-icon>
              <span class="filename">{{ file.name }}</span>
              <span class="filesize">{{ formatFileSize(file.size) }}</span>
              <el-button type="primary" link @click="downloadFile(file)">
                <el-icon><Download /></el-icon>
                下载
              </el-button>
            </div>
          </div>
        </div>

        <el-divider />

        <el-form :model="reviewForm" label-width="100px">
          <el-form-item label="审核意见" required>
            <el-input
              v-model="reviewForm.comment"
              type="textarea"
              :rows="4"
              placeholder="请输入审核意见"
            />
          </el-form-item>
          <el-form-item label="审核结果" required>
            <el-radio-group v-model="reviewForm.action">
              <el-radio label="approve">通过</el-radio>
              <el-radio label="reject">驳回</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="reviewDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitReview">
          提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { View, Document, Download } from '@element-plus/icons-vue'
import { getReviewBacklog, getReviewHistory, reviewResult } from '@/api/result'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('pending')
const tableData = ref([])
const reviewDialog = ref(false)
const currentResult = ref(null)
const submitting = ref(false)

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const reviewForm = reactive({
  action: 'approve',
  comment: ''
})

onMounted(() => {
  loadData()
})

function handleTabChange() {
  pagination.page = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    const res = activeTab.value === 'pending'
      ? await getReviewBacklog(params)
      : await getReviewHistory(params)
    const { data } = res || {}
    tableData.value = data?.list || []
    pagination.total = data?.total || tableData.value.length
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function handleReview(row) {
  currentResult.value = row
  reviewForm.action = 'approve'
  reviewForm.comment = ''
  reviewDialog.value = true
}

async function submitReview() {
  if (!reviewForm.comment) {
    ElMessage.warning('请输入审核意见')
    return
  }

  if (!currentResult.value?.id) {
    ElMessage.error('未找到要审核的成果')
    return
  }

  submitting.value = true
  try {
    await reviewResult(currentResult.value.id, reviewForm)
    ElMessage.success('审核成功')
    reviewDialog.value = false
    loadData()
  } catch (error) {
    ElMessage.error('审核失败')
  } finally {
    submitting.value = false
  }
}

function viewResultDetail() {
  if (!currentResult.value?.id) {
    ElMessage.warning('无法查看详情')
    return
  }
  const routeUrl = router.resolve({
    path: `/results/${currentResult.value.id}`
  })
  window.open(routeUrl.href, '_blank')
}

function formatFileSize(bytes?: number) {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

function downloadFile(file: any) {
  if (file?.url) {
    window.open(file.url, '_blank')
  }
}
</script>

<style scoped>
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.text-muted {
  color: #909399;
  font-size: 13px;
}

.dialog-header-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.abstract-content {
  line-height: 1.6;
  color: #606266;
  white-space: pre-wrap;
  word-break: break-word;
}

.attachments-section {
  margin-top: 20px;
}

.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.attachment-item:hover {
  background: #e8eaf0;
}

.attachment-item .el-icon {
  font-size: 18px;
  color: #409eff;
}

.filename {
  flex: 1;
  font-size: 14px;
  color: #303133;
}

.filesize {
  font-size: 13px;
  color: #909399;
  min-width: 80px;
  text-align: right;
}
</style>
