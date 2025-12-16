<template>
 <el-form :model="form" ref="formRef" :rules="rules" label-width="100px" style="padding-right: 20px;">
  <el-form-item label="类型名称" prop="name">
   <el-input v-model="form.name" placeholder="如：论文" />
  </el-form-item>
  <el-form-item label="类型代码" prop="code">
   <el-input v-model="form.code" placeholder="如：paper" :disabled="props.isEdit" />
   <div class="el-form-item__extra">系统内部使用的唯一编码，建议全部小写英文。</div>
  </el-form-item>
  <el-form-item label="描述" prop="description">
   <el-input v-model="form.description" type="textarea" :rows="3" />
  </el-form-item>
  <el-form-item label="启用状态" prop="enabled">
   <el-switch v-model="form.enabled" />
  </el-form-item>
 </el-form>

 <div class="dialog-footer">
  <el-button @click="$emit('close')">取消</el-button>
  <el-button type="primary" @click="submitForm">
   {{ props.isEdit ? '保存修改' : '立即创建' }}
  </el-button>
 </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'

// 【新增】导入 API 接口和类型
import { createResultType, updateResultType, type AchievementType } from '@/api/result' 

const props = defineProps({
 initialData: {
  type: Object,
  default: () => ({ id: '', name: '', code: '', description: '', enabled: true })
 },
 isEdit: {
  type: Boolean,
  default: false
 }
})

const emit = defineEmits(['close', 'save-success'])

const formRef = ref<FormInstance>()
// 使用 props.initialData 初始化表单数据
const form = reactive({
 ...props.initialData,
})

const rules = reactive<FormRules>({
 name: [{ required: true, message: '请输入类型名称', trigger: 'blur' }],
 code: [{ required: true, message: '请输入类型代码', trigger: 'blur' }],
})

async function submitForm() {
 if (!formRef.value) return
 const valid = await formRef.value.validate().catch(() => false)
 if (!valid) {
  ElMessage.warning('请检查输入项')
  return
 }

 // 1. 构造发送给 Strapi 的数据体
 const payload: Partial<AchievementType> = {
  type_name: form.name,
  type_code: form.code,
  description: form.description,
  is_delete: form.enabled ? 0 : 1
 }

 try {
  let res
  if (props.isEdit) {
   if (!form.documentId) {
    ElMessage.error('更新失败：缺少 documentId')
    return
   }
   res = await updateResultType(form.documentId, payload)
  } else {
   res = await createResultType(payload)
  }

  if (res) {
   ElMessage.success(props.isEdit ? '修改成功' : '创建成功')
   emit('save-success')
  }
 } catch (error) {
  console.error('API 提交失败:', error)
  ElMessage.error(props.isEdit ? '保存修改失败' : '创建失败，请检查类型代码是否重复或网络连接')
 }
}
</script>

<style scoped>
.dialog-footer {
 text-align: right;
 padding-top: 20px;
 border-top: 1px solid var(--el-border-color-lighter);
 margin-top: 20px;
}
.el-form-item__extra {
    font-size: 12px;
    color: var(--el-text-color-secondary);
}
</style>
