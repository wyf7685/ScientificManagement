<template>
  <div class="dynamic-fields-config-container" v-loading="loading">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>类型基础信息</span>
            </div>
          </template>
          <el-descriptions :column="1" border>
  <el-descriptions-item label="类型名称">{{ initialData.name }}</el-descriptions-item>

  <el-descriptions-item label="类型代码">{{ initialData.code }}</el-descriptions-item>

  <el-descriptions-item label="ID标识">
    <el-tag size="small">{{ initialData.documentId }}</el-tag>
  </el-descriptions-item>
</el-descriptions>
        </el-card>
      </el-col>

      <el-col :span="18">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>动态字段配置</span>
              <el-button type="primary" link @click="addField">+ 新增字段</el-button>
            </div>
          </template>

          <el-table :data="fields" row-key="tempKey" border>
            <el-table-column label="字段编码 (field_code)" prop="field_code" width="160">
                <template #default="{ row }">
                    <el-input 
                      v-model="row.field_code" 
                      size="small" 
                      placeholder="如：paper_title" 
                      :disabled="!!row.documentId" 
                    />
                </template>
            </el-table-column>
            
            <el-table-column label="字段名称" prop="field_name" width="160">
                <template #default="{ row }">
                    <el-input v-model="row.field_name" size="small" placeholder="如：论文标题" />
                </template>
            </el-table-column>
            
            <el-table-column label="字段类型" prop="field_type" width="120">
                <template #default="{ row }">
                    <el-select v-model="row.field_type" size="small">
                        <el-option v-for="t in FIELD_TYPES" :key="t" :label="t" :value="t" />
                    </el-select>
                </template>
            </el-table-column>
            
            <el-table-column label="必填" width="80" align="center">
                <template #default="{ row }">
                    <el-checkbox v-model="row.required_bool" />
                </template>
            </el-table-column>
            
            <el-table-column label="说明" prop="description">
                <template #default="{ row }">
                    <el-input v-model="row.description" size="small" />
                </template>
            </el-table-column>
            
            <el-table-column label="操作" width="100" align="center">
                <template #default="{ row, $index }">
                    <el-popconfirm title="确定删除该字段吗？" @confirm="handleDeleteField(row, $index)">
                        <template #reference>
                            <el-button link type="danger" size="small">删除</el-button>
                        </template>
                    </el-popconfirm>
                </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
    
    <div class="dialog-footer">
      <el-button @click="$emit('close')">关闭</el-button>
      <el-button type="primary" :loading="saving" @click="saveAllFields">保存配置</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  getFieldDefsByType, 
  createFieldDef, 
  updateFieldDef, 
  deleteFieldDef,
  updateResultTypeFieldOrder,
  type AchievementFieldDef 
} from '@/api/result'

const props = defineProps({
  initialData: { type: Object, required: true }, // 这里面包含了当前正在编辑的 AchievementType 的 documentId
})

const emit = defineEmits(['close'])

const FIELD_TYPES = ["TEXT", "NUMBER", "BOOLEAN", "DATE", "MEDIA", "JSON", "EMAIL", "RICHTEXT","DATETIME"]
const loading = ref(false)
const saving = ref(false)

// 前端临时数据结构，混合了 API 数据和 UI 状态（如 required_bool）
const fields = ref<any[]>([])
let tempIdCounter = 0

onMounted(() => {
  fetchFields()
})

// 1. 获取字段列表
async function fetchFields() {
 if (!props.initialData.documentId) return
 
 loading.value = true
 try {
  const res = await getFieldDefsByType(props.initialData.documentId)
  
    // 【关键修正】：根据你提供的 JSON 结构，数据列表在响应体的 res.data 属性中
    // Strapi 响应体是 { data: [ 字段列表 ], meta: {...} }
  // res 是整个 JSON 响应体
  const rawList = res.data || [] // 访问 res.data 即可获取到 [ {字段} ] 数组
  
  // 数据转换：后端(0/1) -> 前端(Boolean)
  
  fields.value = rawList.map((item: any) => ({
   documentId: item.documentId,
   field_code: item.field_code,
   field_name: item.field_name,
   field_type: item.field_type,
   description: item.description,
   is_required: item.is_required,  // 后端原始值
   required_bool: item.is_required === 1, // UI 绑定值
   tempKey: `server-${item.documentId}`  // 表格渲染用的唯一Key
  }))
 } catch (error) {
  console.error(error)
  ElMessage.error('字段加载失败')
 } finally {
  loading.value = false
 }
}

// 2. 本地添加一行（此时并未保存到后端）
function addField() {
  fields.value.push({
    documentId: null, // 标记为新数据
    field_code: '',
    field_name: '',
    field_type: 'TEXT',
    is_required: 0,
    required_bool: false,
    description: '',
    tempKey: `local-${tempIdCounter++}`
  })
}

// 3. 删除字段
async function handleDeleteField(row: any, index: number) {
  // Case A: 刚刚点新增出来的，还没存库，直接删数组
  if (!row.documentId) {
    fields.value.splice(index, 1)
    return
  }
  
  // Case B: 数据库里有的，走逻辑删除 API
  try {
    loading.value = true
    await deleteFieldDef(row.documentId)
    ElMessage.success('字段已删除')
    fields.value.splice(index, 1)
  } catch (error) {
    console.error(error)
    ElMessage.error('删除失败')
  } finally {
    loading.value = false
  }
}

// 4. 保存所有配置 (新增 + 更新)
async function saveAllFields() {
  saving.value = true
  try {
    const orderedDocIds: string[] = []

    for (const field of fields.value) {
      // 构造符合 Interface 定义的数据包
      const payload: AchievementFieldDef = {
        field_code: field.field_code,
        field_name: field.field_name,
        field_type: field.field_type,
        is_required: field.required_bool ? 1 : 0, // Boolean 转 Int
        description: field.description,
        is_delete: 0
      }

      if (field.documentId) {
        // === 更新 ===
        // 只需要更新字段本身的属性，不需要重复发 achievement_type
        await updateFieldDef(field.documentId, payload)
        orderedDocIds.push(field.documentId)
      } else {
        // === 新增 ===
        // 关键点：如果是新增，必须告诉后端这个字段属于哪个 Type
        payload.achievement_type = props.initialData.documentId

        const res = await createFieldDef(payload)
        const newDocId =
          res?.data?.documentId ||
          res?.data?.attributes?.documentId ||
          res?.data?.attributes?.document_id ||
          res?.data?.document_id
        const newId = res?.data?.id

        if (newDocId) {
          field.documentId = newDocId
          orderedDocIds.push(newDocId)
        } else if (newId != null) {
          orderedDocIds.push(String(newId))
        }
      }
    }

    if (props.initialData.documentId && orderedDocIds.length > 0) {
      await updateResultTypeFieldOrder(props.initialData.documentId, orderedDocIds)
    }

    ElMessage.success('配置已保存')
    await fetchFields() // 重新拉取，确保拿到最新的 documentId 和顺序
  } catch (error) {
    console.error(error)
    ElMessage.error('保存失败，请检查网络或控制台')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.dialog-footer { text-align: right; padding-top: 20px; border-top: 1px solid var(--el-border-color-lighter); margin-top: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.dynamic-fields-config-container { padding-bottom: 0px; }
</style>
