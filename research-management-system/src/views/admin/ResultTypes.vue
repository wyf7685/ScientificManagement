<template>
 <div class="result-types">
  <el-card>
   <template #header>
    <div class="card-header">
     <span>成果类型配置</span>
     <el-button type="primary" @click="addType">新增类型</el-button>
    </div>
   </template>

   <el-table :data="types" v-loading="loading">
        <el-table-column prop="name" label="类型名称" width="150" />
        <el-table-column prop="code" label="类型代码" width="120" />
    <el-table-column prop="description" label="描述" min-width="200" />
    <el-table-column prop="enabled" label="状态" width="100">
     <template #default="{ row }">
      <el-tag :type="row.enabled ? 'success' : 'info'">
       {{ row.enabled ? '启用' : '停用' }}
      </el-tag>
     </template>
    </el-table-column>
    <el-table-column label="操作" width="200">
     <template #default="{ row }">
      <el-button type="primary" link size="small" @click="openBaseInfoDialog(row)">
       编辑
      </el-button>
      <el-button type="primary" link size="small" @click="openFieldsConfigDialog(row)">
       配置字段
      </el-button>
     </template>
    </el-table-column>
   </el-table>
  </el-card>

  <el-dialog
   v-model="dialogVisible"
   :title="dialogTitle"
   :width="dialogWidth"
   :fullscreen="currentDialogComponent === DynamicFieldsConfig"
   :destroy-on-close="true"
  >
   <component
    :is="currentDialogComponent"
    v-if="dialogVisible"
    :initial-data="currentRow"
    :is-edit="isEdit"
    @close="closeDialog(false)"
    @save-success="handleSaveSuccess"
   />
  </el-dialog>
 </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, markRaw } from 'vue'
import { ElMessage } from 'element-plus'
import { getResultTypes } from '@/api/result'

import ResultTypeBaseInfoForm from './ResultTypeBaseInfoForm.vue'
import ResultTypeDynamicFieldsConfig from './ResultTypeDynamicFieldsConfig.vue'

const BaseInfoForm = markRaw(ResultTypeBaseInfoForm)
const DynamicFieldsConfig = markRaw(ResultTypeDynamicFieldsConfig)

// --- 状态管理 ---
const loading = ref(false)
const types = ref<any[]>([]) // 使用 any[] 避免 TS 报错，但实际开发中应定义 Type
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentRow = ref<any>({})
const currentDialogComponent = ref<typeof BaseInfoForm | typeof DynamicFieldsConfig | null>(null)

// --- 计算属性 (保持不变) ---
const dialogTitle = computed(() => {
 if (currentDialogComponent.value === BaseInfoForm) {
  return isEdit.value ? '编辑类型基础信息' : '新增成果类型'
 }
 if (currentDialogComponent.value === DynamicFieldsConfig) {
  return `配置【${currentRow.value.name}】的动态字段`
 }
 return ''
})

const dialogWidth = computed(() => {
 if (currentDialogComponent.value === BaseInfoForm) {
  return '600px'
 }
 if (currentDialogComponent.value === DynamicFieldsConfig) {
  return '1400px'
 }
 return '600px'
})

// --- 生命周期钩子 (保持不变) ---
onMounted(async () => {
 await loadTypes()
})

// --- 业务函数 (核心修改在这里) ---
async function loadTypes() {
 loading.value = true
 try {
  const response = await getResultTypes()
  const list = response?.data || []

  if (Array.isArray(list)) {
    types.value = list.map((item: any) => ({
      name: item.type_name || item.typeName || item.name,
      code: item.type_code || item.typeCode || item.code,
      description: item.description,
      documentId: item.documentId,
      enabled: item.is_delete === 0 || item.enabled === true,
      id: item.id
    }))
  } else {
    console.error('成果类型数据格式错误:', response)
    types.value = []
  }

 } catch (error) {
  ElMessage.error('加载成果类型失败')
    // 如果有模拟数据，可以在 catch 块中作为备用
    // types.value = [ { id: 1, name: '论文', code: 'paper', description: '记录学术论文信息', enabled: true }, /* ... */ ];
 } finally {
  loading.value = false
 }
}

// ... (以下函数保持不变)

function closeDialog(needReload = false) {
 dialogVisible.value = false
 currentRow.value = {}
 currentDialogComponent.value = null
 if (needReload) {
  loadTypes()
 }
}

function handleSaveSuccess() {
  ElMessage.success('保存成功')
  closeDialog(true) // 保存成功后关闭弹窗并重新加载列表
}

function addType() {
 isEdit.value = false
 // 注意：新增时确保传入的字段名是前端期望的 name/code
 currentRow.value = { id: '', name: '', code: '', description: '', enabled: true } 
 currentDialogComponent.value = BaseInfoForm
 dialogVisible.value = true
}

// 打开编辑基础信息弹窗
function openBaseInfoDialog(row: any) {
 isEdit.value = true
  // 拷贝行数据，保持 name/code 格式不变
 currentRow.value = { ...row } 
 currentDialogComponent.value = BaseInfoForm
 dialogVisible.value = true
}

// 打开配置动态字段弹窗
function openFieldsConfigDialog(row: any) {
 isEdit.value = true 
  // 拷贝行数据，确保 documentId 随 currentRow 传递给 ResultTypeDynamicFieldsConfig
 currentRow.value = { ...row } 
 currentDialogComponent.value = DynamicFieldsConfig
 dialogVisible.value = true
}
</script>

<style scoped>
.card-header {
 display: flex;
 justify-content: space-between;
 align-items: center;
}
</style>
