<template>
  <div class="create-result">
    <el-card>
      <!-- 步骤条 -->
      <el-steps :active="currentStep" align-center finish-status="success">
        <el-step title="选择类型" />
        <el-step title="填写基础信息" />
        <el-step title="上传附件" />
        <el-step title="智能补全" />
        <el-step title="提交" />
      </el-steps>

      <div class="step-content">
        <!-- 步骤1: 选择类型 -->
        <div v-show="currentStep === 0" class="step-panel">
          <h3>选择成果类型</h3>
          <el-select
            v-model="formData.typeId"
            placeholder="请选择成果类型"
            size="large"
            style="width: 100%"
            @change="handleTypeChange"
          >
            <el-option
              v-for="type in resultTypes"
              :key="type.id"
              :label="type.name"
              :value="type.id"
            >
              <span>{{ type.name }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">
                {{ type.description }}
              </span>
            </el-option>
          </el-select>
          <div v-if="selectedType" class="type-info">
            <el-alert :title="selectedType.description" type="info" :closable="false" />
          </div>
        </div>

        <!-- 步骤2: 填写基础信息 -->
        <div v-show="currentStep === 1" class="step-panel" v-loading="loadingFields" element-loading-text="正在加载字段配置...">
          <h3>填写基础信息</h3>
          <el-form
            ref="formRef"
            :model="formData"
            :rules="formRules"
            label-width="120px"
          >
            <el-form-item label="成果标题" prop="title" required>
              <el-input v-model="formData.title" placeholder="请输入成果标题" />
            </el-form-item>
            <el-form-item label="作者" prop="authors" required>
              <el-select
                v-model="formData.authors"
                multiple
                filterable
                allow-create
                placeholder="请选择或输入作者"
                style="width: 100%"
              >
                <el-option :label="userStore.userInfo?.name" :value="userStore.userInfo?.name || ''" />
              </el-select>
            </el-form-item>
            <el-form-item label="所属项目">
              <div class="project-select">
                <el-select
                  v-model="formData.projectId"
                  filterable
                  clearable
                  placeholder="选择所属项目（可留空表示其他/未归属）"
                  style="width: 100%"
                >
                  <el-option :label="'无所属/其他'" :value="''" />
                  <el-option
                    v-for="project in projects"
                    :key="project.id"
                    :label="getProjectLabel(project)"
                    :value="project.id"
                  />
                </el-select>
                <el-button class="create-project-btn" type="primary" link @click="openProjectDialog">
                  新建项目
                </el-button>
              </div>
              <div class="project-hint">未选择时默认为其他/未归属项目，可在此创建并直接选择新项目。</div>
            </el-form-item>
            <el-form-item label="年份" prop="year" required>
              <el-date-picker
                v-model="formData.year"
                type="year"
                placeholder="选择年份"
                value-format="YYYY"
              />
            </el-form-item>
            <el-form-item label="摘要" prop="abstract">
              <el-input
                v-model="formData.abstract"
                type="textarea"
                :rows="4"
                placeholder="请输入成果摘要"
              />
            </el-form-item>
            <el-form-item label="关键词" prop="keywords">
              <el-select
                v-model="formData.keywords"
                multiple
                filterable
                allow-create
                placeholder="请输入关键词，按回车添加"
                style="width: 100%"
              />
            </el-form-item>
            
            <!-- 动态字段 - 使用组件映射模型 -->
            <template v-if="selectedType">
              <el-form-item
                v-for="field in selectedType.fields"
                :key="field.id"
                :label="field.label"
                :prop="`metadata.${field.name}`"
                :required="field.required"
              >
                <DynamicFieldRenderer
                  :field="field"
                  v-model="formData.metadata[field.name]"
                />
              </el-form-item>
            </template>
          </el-form>
        </div>

        <!-- 步骤3: 上传附件 -->
        <div v-show="currentStep === 2" class="step-panel">
          <h3>上传附件与设置可见范围</h3>
          <el-form label-width="120px">
            <el-form-item label="附件上传">
              <el-upload
                v-model:file-list="fileList"
                :auto-upload="false"
                :on-change="handleFileChange"
                drag
                multiple
              >
                <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                <div class="el-upload__text">
                  拖拽文件到此处或 <em>点击上传</em>
                </div>
              </el-upload>
            </el-form-item>
            <el-form-item label="可见范围" required>
              <el-radio-group v-model="formData.visibility">
                <el-radio label="private">私有（仅自己）</el-radio>
                <el-radio label="internal_abstract">机构内摘要可见</el-radio>
                <el-radio label="internal_full">机构内全文可见</el-radio>
                <el-radio label="public_abstract">公开摘要</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </div>

        <!-- 步骤4: 智能补全 -->
        <div v-show="currentStep === 3" class="step-panel">
          <h3>智能补全（可选）</h3>
          <el-form label-width="120px">
            <el-form-item label="补全方式">
              <el-radio-group v-model="autoFillType">
                <el-radio label="doi">DOI</el-radio>
                <el-radio label="arxiv">arXiv ID</el-radio>
                <el-radio label="wanfang">万方标题</el-radio>
                <el-radio label="journalRank">期刊等级</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item :label="getAutoFillLabel()">
              <el-input
                v-model="autoFillValue"
                :placeholder="getAutoFillPlaceholder()"
              >
                <template #append>
                  <el-button
                    :loading="autoFilling"
                    @click="handleAutoFill"
                  >
                    一键补全
                  </el-button>
                </template>
              </el-input>
            </el-form-item>
            <el-alert
              v-if="journalRankItems.length"
              title="期刊等级结果"
              type="success"
              :closable="false"
              class="rank-alert"
            >
              <template #default>
                <div class="rank-result">
                  <div v-for="(item, index) in journalRankItems" :key="index">
                    {{ item }}
                  </div>
                </div>
              </template>
            </el-alert>
          </el-form>
          <el-alert
            title="提示：智能补全会从外部数据库获取元数据，您可以选择性地覆盖当前表单内容"
            type="info"
            :closable="false"
          />
        </div>

        <!-- 步骤5: 确认提交 -->
        <div v-show="currentStep === 4" class="step-panel">
          <h3>确认信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="成果标题">{{ formData.title }}</el-descriptions-item>
            <el-descriptions-item label="成果类型">{{ selectedType?.name }}</el-descriptions-item>
            <el-descriptions-item label="作者">{{ formData.authors.join(', ') }}</el-descriptions-item>
            <el-descriptions-item label="年份">{{ formData.year }}</el-descriptions-item>
            <el-descriptions-item label="可见范围" :span="2">
              {{ getVisibilityText(formData.visibility) }}
            </el-descriptions-item>
            <el-descriptions-item label="附件数量" :span="2">
              {{ fileList.length }} 个文件
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <el-dialog v-model="projectDialogVisible" title="新建项目" width="480px">
        <el-form label-width="100px">
          <el-form-item label="项目名称" required>
            <el-input v-model="projectForm.name" placeholder="请输入项目名称" />
          </el-form-item>
          <el-form-item label="项目编号" required>
            <el-input v-model="projectForm.code" placeholder="请输入项目编号" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="projectDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="creatingProject" @click="handleCreateProject">
            创建并选择
          </el-button>
        </template>
      </el-dialog>

      <!-- 操作按钮 -->
      <div class="step-actions">
        <el-button v-if="currentStep > 0" @click="prevStep">上一步</el-button>
        <el-button v-if="currentStep < 4" type="primary" @click="nextStep">下一步</el-button>
        <el-button v-if="currentStep === 4" type="success" :loading="submitting" @click="handleSubmit">
          提交审核
        </el-button>
        <el-button @click="handleSaveDraft" :loading="saving">保存草稿</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { getResultTypes, getFieldDefsByType, createResult, createResultWithFiles, saveDraft, autoFillMetadata } from '@/api/result'
import { getProjects, createProject } from '@/api/project'
import { ResultVisibility } from '@/types'
import DynamicFieldRenderer from '@/components/DynamicFieldRenderer.vue'
import { mapFieldType } from '@/config/dynamicFields'

const router = useRouter()
const userStore = useUserStore()

const currentStep = ref(0)
const resultTypes = ref([])
const projects = ref([])
const loadingFields = ref(false)
const selectedType = computed(() => {
  const type = resultTypes.value.find(t => t.id === formData.typeId)
  return type || null
})

const formRef = ref()
const formData = reactive({
  typeId: '',
  title: '',
  authors: [userStore.userInfo?.name || ''],
  projectId: '',
  projectName: '',
  projectCode: '',
  year: new Date().getFullYear().toString(),
  abstract: '',
  keywords: [],
  visibility: ResultVisibility.INTERNAL_ABSTRACT,
  metadata: {},
  attachments: []
})

const formRules = {
  title: [{ required: true, message: '请输入成果标题', trigger: 'blur' }],
  authors: [{ required: true, message: '请选择作者', trigger: 'change' }],
  year: [{ required: true, message: '请选择年份', trigger: 'change' }]
}

const fileList = ref([])
const autoFillType = ref('doi')
const autoFillValue = ref('')
const autoFilling = ref(false)
const journalRankItems = ref<string[]>([])
const lastJournalRankAt = ref(0)
const submitting = ref(false)
const saving = ref(false)
const MAX_FILE_SIZE = 20 * 1024 * 1024
const projectDialogVisible = ref(false)
const projectForm = reactive({
  name: '',
  code: ''
})
const creatingProject = ref(false)

onMounted(async () => {
  await Promise.all([loadResultTypes(), loadProjects()])
})

async function loadResultTypes() {
  try {
    const res = await getResultTypes()
    const { data } = res || {}
    // 映射 Strapi 返回的字段到前端模型
    resultTypes.value = (data || [])
      .map((t: any) => ({
        ...t,
        id: t.documentId || t.id,
        name: t.type_name || t.typeName || t.name,
        code: t.type_code || t.typeCode || t.code,
        enabled: t.is_delete === 0 || t.enabled === true
      }))
      .filter((t: any) => t.enabled)
  } catch (error) {
    ElMessage.error('加载成果类型失败')
  }
}

async function loadProjects() {
  try {
    const res = await getProjects()
    projects.value = res?.data || []
  } catch (error) {
    ElMessage.error('加载项目列表失败')
  }
}

// 将后端字段定义转换为前端表单字段格式
function transformFieldDef(field: any) {
  return {
    id: field.field_code,
    name: field.field_code,
    label: field.field_name,
    type: mapFieldType(field.field_type), // 使用配置文件中的映射函数
    documentId: field.documentId,
    backendType: field.field_type,
    required: field.is_required === 1,
    helpText: field.description || `请输入${field.field_name}`,
    options: field.options || [], // 支持选项字段
    order: field.order || 0
  }
}

// 当用户选择成果类型后，动态加载该类型的字段定义
async function handleTypeChange() {
  // 清空旧的元数据
  formData.metadata = {}
  
  if (!formData.typeId) return
  
  // 查找当前选择的类型
  const currentType = resultTypes.value.find(t => t.id === formData.typeId)
  if (!currentType) return
  
  // 如果该类型已经加载过字段，不重复加载
  if (currentType.fields && currentType.fields.length > 0) return
  
  loadingFields.value = true
  try {
    const res = await getFieldDefsByType(formData.typeId)
    const rawFields = res?.data || []
    
    // 转换字段格式并排序
    const transformedFields = rawFields
      .map(transformFieldDef)
      .sort((a, b) => (a.order || 0) - (b.order || 0))
    
    // 将字段定义注入到类型对象中
    currentType.fields = transformedFields
    
    if (transformedFields.length === 0) {
      ElMessage.info('该类型暂未配置动态字段')
    }
  } catch (error) {
    console.error('加载字段定义失败:', error)
    ElMessage.error('加载字段定义失败，请稍后重试')
    // 失败时设置空数组，避免重复请求
    if (currentType) currentType.fields = []
  } finally {
    loadingFields.value = false
  }
}

function validateDynamicFields() {
  if (!selectedType.value) return true
  for (const field of selectedType.value.fields || []) {
    if (!field.required) continue
    const value = formData.metadata[field.name]
    if (value === undefined || value === null || value === '') {
      ElMessage.warning(`请填写${field.label}`)
      return false
    }
  }
  return true
}

async function nextStep() {
  if (currentStep.value === 0 && !formData.typeId) {
    ElMessage.warning('请先选择成果类型')
    return
  }
  
  if (currentStep.value === 1) {
    try {
      await formRef.value?.validate()
    } catch {
      return
    }

    if (!validateDynamicFields()) return
  }
  
  currentStep.value++
}

function prevStep() {
  currentStep.value--
}

async function handleAutoFill() {
  if (!autoFillValue.value) {
    ElMessage.warning('请输入要补全的标识符')
    return
  }

  if (autoFillType.value === 'journalRank' && !canRequestJournalRank()) {
    return
  }
  
  autoFilling.value = true
  try {
    if (autoFillType.value !== 'journalRank') {
      journalRankItems.value = []
    }
    const res = await autoFillMetadata({
      type: autoFillType.value,
      value: autoFillValue.value
    })

    if (autoFillType.value === 'journalRank') {
      journalRankItems.value = formatJournalRank(res?.data)
      if (journalRankItems.value.length === 0) {
        ElMessage.warning('未查询到期刊等级')
        return
      }
      formData.metadata.journalRank = journalRankItems.value.join('；')
      ElMessage.success('期刊等级查询完成')
      return
    }

    // 显示差异对比并让用户选择
    await ElMessageBox.confirm('找到匹配记录，是否覆盖当前表单内容？', '提示', {
      confirmButtonText: '覆盖',
      cancelButtonText: '取消'
    })

    if (res?.data) {
      Object.assign(formData, res.data)
      ElMessage.success('补全成功')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('补全失败，请检查输入或稍后重试')
    }
  } finally {
    autoFilling.value = false
  }
}

function handleFileChange(file, files) {
  if (file?.raw && file.raw.size > MAX_FILE_SIZE) {
    ElMessage.warning('单个附件不能超过 20MB')
    fileList.value = files.filter((item) => item.uid !== file.uid)
    return
  }
  fileList.value = files
}

function getAutoFillLabel() {
  const map = {
    doi: 'DOI',
    arxiv: 'arXiv ID',
    wanfang: '万方标题',
    journalRank: '期刊名称'
  }
  return map[autoFillType.value]
}

function getAutoFillPlaceholder() {
  const map = {
    doi: '例如: 10.1000/xyz123',
    arxiv: '例如: 2301.00001',
    wanfang: '请输入论文标题',
    journalRank: '请输入期刊名称'
  }
  return map[autoFillType.value]
}

function formatJournalRank(data: any) {
  if (!data) return []
  const lines: string[] = []

  const official = data.officialRank?.select || data.officialRank?.all
  if (official && typeof official === 'object') {
    const entries = Object.entries(official)
      .map(([key, value]) => `${key.toUpperCase()}: ${value}`)
      .join('，')
    if (entries) lines.push(`官方数据集：${entries}`)
  }

  const rankInfo = Array.isArray(data.customRank?.rankInfo) ? data.customRank.rankInfo : []
  const rankList = Array.isArray(data.customRank?.rank) ? data.customRank.rank : []
  const rankKeyMap: Record<string, string> = {
    '1': 'oneRankText',
    '2': 'twoRankText',
    '3': 'threeRankText',
    '4': 'fourRankText',
    '5': 'fiveRankText'
  }
  const customLabels: string[] = []
  rankList.forEach((entry: string) => {
    const [uuid, rank] = entry.split('&&&')
    if (!uuid || !rank) return
    const info = rankInfo.find((item: any) => item.uuid === uuid)
    const key = rankKeyMap[rank]
    const label = info?.[key] || rank
    const name = info?.abbName || '自定义'
    customLabels.push(`${name} ${label}`)
  })
  if (customLabels.length) {
    lines.push(`自定义数据集：${customLabels.join('，')}`)
  }

  return lines
}

function canRequestJournalRank() {
  const now = Date.now()
  const minInterval = 500
  if (now - lastJournalRankAt.value < minInterval) {
    ElMessage.warning('请求过于频繁，请稍后重试')
    return false
  }
  lastJournalRankAt.value = now
  return true
}

const VISIBILITY_TEXT = {
  [ResultVisibility.PRIVATE]: '私有（仅自己）',
  [ResultVisibility.INTERNAL_ABSTRACT]: '机构内摘要可见',
  [ResultVisibility.INTERNAL_FULL]: '机构内全文可见',
  [ResultVisibility.PUBLIC_ABSTRACT]: '公开摘要',
  [ResultVisibility.PUBLIC_FULL]: '公开全文'
}

function getVisibilityText(visibility) {
  return VISIBILITY_TEXT[visibility] || visibility
}

function getProjectLabel(project) {
  if (!project) return ''
  return `${project.name} (${project.code})`
}

function openProjectDialog() {
  projectDialogVisible.value = true
  projectForm.name = ''
  projectForm.code = ''
}

async function handleCreateProject() {
  if (!projectForm.name || !projectForm.code) {
    ElMessage.warning('请填写项目名称和编号')
    return
  }
  creatingProject.value = true
  try {
    const res = await createProject({ ...projectForm })
    const newProject = res?.data
    if (newProject) {
      projects.value.unshift(newProject)
      formData.projectId = newProject.id
      formData.projectName = newProject.name
      formData.projectCode = newProject.code
      ElMessage.success('项目创建成功')
    }
    projectDialogVisible.value = false
  } catch (error) {
    ElMessage.error('项目创建失败')
  } finally {
    creatingProject.value = false
  }
}

async function handleSaveDraft() {
  saving.value = true
  try {
    const payload = buildDraftPayload()
    await saveDraft(payload)
    ElMessage.success('草稿已保存')
  } catch (error) {
    ElMessage.error('保存草稿失败')
  } finally {
    saving.value = false
  }
}

async function handleSubmit() {
  submitting.value = true
  try {
    const payload = buildPayload()
    const rawFiles = fileList.value.filter(f => f.raw).map(f => f.raw) as File[]
    
    if (rawFiles.length > 0) {
      await createResultWithFiles(payload, rawFiles)
    } else {
      await createResult(payload)
    }
    
    ElMessage.success('提交成功')
    router.push('/results/my')
  } catch (error) {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

function buildFieldValues() {
  const fields = selectedType.value?.fields || []
  return fields
    .map((field: any) => {
      if (!field.documentId) return null
      const value = formData.metadata[field.name]
      if (value === undefined || value === null || value === '') return null

      const payload: Record<string, any> = {
        achievement_field_def_id: field.documentId
      }

      if (field.type === 'number') {
        payload.numberValue = Number(value)
      } else if (field.type === 'checkbox' || field.type === 'switch') {
        payload.booleanValue = Boolean(value)
      } else if (field.type === 'date') {
        payload.dateValue = value
      } else {
        payload.textValue = value
      }

      return payload
    })
    .filter(Boolean)
}

function buildDraftPayload() {
  const project = projects.value.find((p) => p.id === formData.projectId)
  return {
    ...formData,
    projectName: project?.name || formData.projectName || '',
    projectCode: project?.code || formData.projectCode || '',
    metadata: { ...formData.metadata }
  }
}

function buildPayload() {
  const project = projects.value.find((p) => p.id === formData.projectId)
  return {
    data: {
      title: formData.title,
      year: formData.year,
      authors: formData.authors,
      keywords: formData.keywords,
      summary: formData.abstract,
      achievementStatus: 'PENDING',
      typeDocId: formData.typeId,
      projectCode: project?.code || formData.projectCode || '',
      projectName: project?.name || formData.projectName || '',
      visibilityRange: formData.visibility,
      fields: buildFieldValues()
    }
  }
}
</script>

<style scoped>
.create-result {
  max-width: 1200px;
  margin: 0 auto;
}

.step-content {
  margin: 40px 0;
  min-height: 400px;
}

.step-panel {
  padding: 20px;
}

.step-panel h3 {
  margin-bottom: 24px;
  font-size: 18px;
  color: #303133;
}

.project-select {
  display: flex;
  gap: 8px;
  align-items: center;
}

.create-project-btn {
  white-space: nowrap;
}

.project-hint {
  margin-top: 4px;
  color: #909399;
  font-size: 12px;
}

.type-info {
  margin-top: 20px;
}

.rank-alert {
  margin-top: 12px;
}

.rank-result {
  line-height: 1.6;
}

.step-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 20px;
  border-top: 1px solid #e4e7ed;
}
</style>
