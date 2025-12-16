<template>
  <div class="system-settings">
    <div class="page-header">
      <div>
        <h2>系统设置 · 爬虫数据源</h2>
        <p class="subtitle">集中配置需求爬虫的数据源、抓取频率与通知策略，确保数据持续更新</p>
      </div>
      <el-button :loading="sourcesLoading" @click="loadSources">
        <el-icon style="margin-right: 4px"><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never" class="glass">
          <template #header>
            <div class="card-header">
              <div>
                <div class="card-title">数据源列表</div>
                <div class="card-desc">管理外部网站、API、RSS 等数据源的抓取配置</div>
              </div>
              <el-button type="primary" @click="openCreate">
                <el-icon style="margin-right: 4px"><Plus /></el-icon>
                新增数据源
              </el-button>
            </div>
          </template>

          <el-table
            :data="sourceList"
            :loading="sourcesLoading"
            border
            empty-text="暂无配置的数据源"
          >
            <el-table-column label="名称 / 标签" min-width="220">
              <template #default="{ row }">
                <div class="name-line">
                  <span class="source-name">{{ row.name }}</span>
                  <el-tag size="small" effect="plain">{{ typeLabel(row.type) }}</el-tag>
                </div>
                <div class="tag-line">
                  <el-tag
                    v-for="tag in row.tags || []"
                    :key="tag"
                    size="small"
                    effect="plain"
                  >
                    {{ tag }}
                  </el-tag>
                </div>
                <div class="muted">
                  {{ row.baseUrl }}
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="industry" label="行业" width="120" />
            <el-table-column prop="region" label="地域" width="100" />
            <el-table-column label="抓取频率" width="120">
              <template #default="{ row }">
                {{ formatFrequency(row.frequencyHours) }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="140">
              <template #default="{ row }">
                <div class="status-cell">
                  <el-tag :type="statusTagType(row.status)" effect="plain">
                    {{ statusLabel(row.status) }}
                  </el-tag>
                  <span class="status-time">
                    上次成功：{{ row.lastSuccessAt || '—' }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="启用" width="100" align="center">
              <template #default="{ row }">
                <el-switch v-model="row.enabled" @change="handleToggle(row)" />
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" width="200">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="openEdit(row)">
                  编辑
                </el-button>
                <el-button link type="primary" size="small" :loading="testingId === row.id" @click="handleTest(row)">
                  测试连接
                </el-button>
                <el-button link type="danger" size="small" @click="handleDelete(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="glass settings-card">
          <template #header>
            <div class="card-title">全局策略</div>
          </template>
          <el-form
            ref="settingsFormRef"
            :model="settings"
            label-width="120px"
            :disabled="settingsLoading"
          >
            <el-form-item label="默认频率 (小时)">
              <el-input-number
                v-model="settings.defaultFrequencyHours"
                :min="1"
                :max="168"
              />
            </el-form-item>
            <el-form-item label="失败重试次数">
              <el-input-number v-model="settings.retryLimit" :min="0" :max="10" />
            </el-form-item>
            <el-form-item label="自动标签">
              <el-switch v-model="settings.autoTagging" />
            </el-form-item>
            <el-form-item label="去重阈值">
              <el-slider
                v-model="settings.deduplicateThreshold"
                :min="0.5"
                :max="0.95"
                :step="0.01"
                show-input
              />
            </el-form-item>
            <el-form-item label="通知邮箱">
              <el-select
                v-model="settings.notifyEmails"
                multiple
                filterable
                allow-create
                default-first-option
                placeholder="输入邮箱后回车添加"
              />
            </el-form-item>
            <el-form-item label="Webhook 地址">
              <el-input
                v-model="settings.notifyWebhook"
                placeholder="如需推送到钉钉/飞书，请填写回调地址"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="savingSettings" @click="handleSaveSettings">
                保存设置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <el-drawer
      v-model="drawerVisible"
      :title="drawerTitle"
      size="520px"
      destroy-on-close
    >
      <el-form ref="sourceFormRef" :model="sourceForm" :rules="formRules" label-width="120px">
        <el-form-item label="数据源名称" prop="name">
          <el-input v-model="sourceForm.name" placeholder="例如：政府采购网" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="sourceForm.type" placeholder="选择类型">
            <el-option v-for="opt in typeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="行业">
          <el-input v-model="sourceForm.industry" placeholder="可选，如 生物医药" />
        </el-form-item>
        <el-form-item label="地域">
          <el-input v-model="sourceForm.region" placeholder="可选，如 长三角" />
        </el-form-item>
        <el-form-item label="基础 URL" prop="baseUrl">
          <el-input v-model="sourceForm.baseUrl" placeholder="https://example.com..." />
        </el-form-item>
        <el-form-item label="认证方式" prop="authType">
          <el-select v-model="sourceForm.authType">
            <el-option v-for="opt in authOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="sourceForm.authType === 'api_key'" label="API Key">
          <el-input v-model="sourceForm.credentials.apiKey" placeholder="填写 API Key" />
        </el-form-item>
        <el-form-item v-if="sourceForm.authType === 'cookie'" label="Cookie">
          <el-input
            v-model="sourceForm.credentials.cookie"
            type="textarea"
            :rows="2"
            placeholder="k1=v1; k2=v2"
          />
        </el-form-item>
        <el-form-item v-if="sourceForm.authType === 'basic'" label="用户名/密码">
          <div class="basic-auth">
            <el-input v-model="sourceForm.credentials.username" placeholder="用户名" />
            <el-input v-model="sourceForm.credentials.password" placeholder="密码" type="password" />
          </div>
        </el-form-item>
        <el-form-item label="抓取频率 (小时)" prop="frequencyHours">
          <el-input-number v-model="sourceForm.frequencyHours" :min="1" :max="168" />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="sourceForm.priority">
            <el-option v-for="opt in priorityOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-select
            v-model="sourceForm.tags"
            multiple
            allow-create
            filterable
            default-first-option
            placeholder="按回车新增标签"
          />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="sourceForm.enabled" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="sourceForm.description"
            type="textarea"
            :rows="3"
            placeholder="记录数据源特殊要求、字段映射等"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="drawer-footer">
          <el-button @click="drawerVisible = false">取消</el-button>
          <el-button type="primary" :loading="savingSource" @click="handleSubmit">
            保存
          </el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import {
  type CrawlerDataSource,
  type CrawlerSettings,
  getCrawlerSources,
  createCrawlerSource,
  updateCrawlerSource,
  deleteCrawlerSource,
  testCrawlerSource,
  getCrawlerSettings,
  updateCrawlerSettings
} from '@/api/system'
import { useRequest } from '@/composables/useErrorHandler'

const sourceList = ref<CrawlerDataSource[]>([])
const drawerVisible = ref(false)
const savingSource = ref(false)
const editingId = ref<string | null>(null)
const testingId = ref<string | null>(null)
const sourceFormRef = ref<FormInstance>()

const defaultSource: CrawlerDataSource = {
  name: '',
  type: 'html',
  industry: '',
  region: '',
  baseUrl: '',
  authType: 'none',
  credentials: {},
  frequencyHours: 6,
  priority: 'medium',
  tags: [],
  enabled: true,
  description: ''
}

const sourceForm = reactive<CrawlerDataSource>({ ...defaultSource })

const formRules: FormRules = {
  name: [{ required: true, message: '请输入数据源名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  baseUrl: [{ required: true, message: '请输入基础 URL', trigger: 'blur' }],
  authType: [{ required: true, message: '请选择认证方式', trigger: 'change' }],
  frequencyHours: [{ required: true, message: '请输入频率', trigger: 'blur' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }]
}

const typeOptions = [
  { label: '网页解析', value: 'html' },
  { label: 'RSS/Atom', value: 'rss' },
  { label: '开放 API', value: 'api' },
  { label: '文件/FTP', value: 'file' }
]

const authOptions = [
  { label: '无需认证', value: 'none' },
  { label: 'API Key', value: 'api_key' },
  { label: 'Cookie', value: 'cookie' },
  { label: 'Basic Auth', value: 'basic' }
]

const priorityOptions = [
  { label: '高', value: 'high' },
  { label: '中', value: 'medium' },
  { label: '低', value: 'low' }
]

const settingsFormRef = ref<FormInstance>()
const settings = reactive<CrawlerSettings>({
  defaultFrequencyHours: 8,
  retryLimit: 3,
  autoTagging: true,
  deduplicateThreshold: 0.8,
  notifyEmails: [],
  notifyWebhook: ''
})
const savingSettings = ref(false)

const { loading: sourcesLoading, execute: fetchSources } = useRequest(async () => {
  const res = await getCrawlerSources()
  return res?.data
}, {
  immediate: false,
  onSuccess: (data) => {
    if (Array.isArray(data)) {
      sourceList.value = data
    } else {
      sourceList.value = data?.list || []
    }
  }
})

const { loading: settingsLoading, execute: fetchSettings } = useRequest(async () => {
  const res = await getCrawlerSettings()
  return res?.data
}, {
  immediate: false,
  onSuccess: (data) => {
    if (!data) return
    Object.assign(settings, {
      ...settings,
      ...data,
      notifyEmails: data.notifyEmails || []
    })
  }
})

const drawerTitle = computed(() => (editingId.value ? '编辑数据源' : '新增数据源'))

function resetForm() {
  Object.assign(sourceForm, JSON.parse(JSON.stringify(defaultSource)))
}

function openCreate() {
  editingId.value = null
  resetForm()
  drawerVisible.value = true
}

function openEdit(row: CrawlerDataSource) {
  editingId.value = row.id || null
  Object.assign(sourceForm, JSON.parse(JSON.stringify(row)))
  if (!sourceForm.credentials) {
    sourceForm.credentials = {}
  }
  drawerVisible.value = true
}

async function handleSubmit() {
  if (!sourceFormRef.value) return
  const valid = await sourceFormRef.value.validate().catch(() => false)
  if (!valid) return
  savingSource.value = true
  try {
    const payload = JSON.parse(JSON.stringify(sourceForm))
    if (editingId.value) {
      await updateCrawlerSource(editingId.value, payload)
      ElMessage.success('数据源已更新')
    } else {
      await createCrawlerSource(payload)
      ElMessage.success('数据源已创建')
    }
    drawerVisible.value = false
    await loadSources()
  } finally {
    savingSource.value = false
  }
}

async function handleDelete(row: CrawlerDataSource) {
  if (!row.id) return
  try {
    await ElMessageBox.confirm(`确定删除数据源「${row.name}」吗？`, '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteCrawlerSource(row.id)
    ElMessage.success('已删除')
    await loadSources()
  } catch {
    // 用户取消或请求失败
  }
}

async function handleToggle(row: CrawlerDataSource) {
  if (!row.id) return
  try {
    await updateCrawlerSource(row.id, { enabled: row.enabled })
    ElMessage.success('状态已更新')
  } catch {
    row.enabled = !row.enabled
  }
}

async function handleTest(row: CrawlerDataSource) {
  if (!row.id) return
  testingId.value = row.id
  try {
    await testCrawlerSource(row.id)
    ElMessage.success('连接成功')
    await loadSources()
  } catch (error) {
    console.error(error)
  } finally {
    testingId.value = null
  }
}

async function loadSources() {
  await fetchSources()
}

async function loadSettings() {
  await fetchSettings()
}

function formatFrequency(hours?: number) {
  if (!hours) return '--'
  if (hours < 1) return '实时'
  if (hours < 24) return `每 ${hours} 小时`
  return `每 ${Math.round(hours / 24)} 天`
}

function typeLabel(type?: string) {
  const found = typeOptions.find((item) => item.value === type)
  return found?.label || '其他'
}

function statusLabel(status?: string) {
  switch (status) {
    case 'healthy':
      return '运行正常'
    case 'warning':
      return '部分异常'
    case 'error':
      return '连接失败'
    default:
      return '未执行'
  }
}

function statusTagType(status?: string) {
  if (status === 'healthy') return 'success'
  if (status === 'warning') return 'warning'
  if (status === 'error') return 'danger'
  return 'info'
}

async function handleSaveSettings() {
  savingSettings.value = true
  try {
    await updateCrawlerSettings(settings)
    ElMessage.success('系统设置已更新')
  } finally {
    savingSettings.value = false
  }
}

onMounted(() => {
  loadSources()
  loadSettings()
})
</script>

<style scoped>
.system-settings {
  display: flex;
  flex-direction: column;
  gap: 16px;
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-weight: 700;
}

.card-desc {
  font-size: 12px;
  color: #9ca3af;
}

.name-line {
  display: flex;
  gap: 8px;
  align-items: center;
}

.source-name {
  font-weight: 600;
  color: #111827;
}

.tag-line {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin: 4px 0;
}

.muted {
  color: #9ca3af;
  font-size: 12px;
}

.status-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.status-time {
  color: #9ca3af;
  font-size: 12px;
}

.settings-card {
  height: 100%;
}

.glass {
  border: 1px solid #eef2f7;
}

.drawer-footer {
  text-align: right;
}

.basic-auth {
  display: flex;
  gap: 8px;
}
</style>
