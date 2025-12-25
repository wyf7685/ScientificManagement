<template>
  <component
    :is="fieldComponent"
    v-model="modelValue"
    v-bind="componentProps"
  >
    <!-- Select 组件的选项需要特殊处理 -->
    <template v-if="field.type === 'select'">
      <el-option
        v-for="opt in field.options"
        :key="opt"
        :label="opt"
        :value="opt"
      />
    </template>
  </component>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getFieldComponent, getFieldDefaultProps } from '@/config/dynamicFields'

interface DynamicField {
  id: string
  name: string
  label: string
  type: string
  required?: boolean
  helpText?: string
  options?: string[]
  [key: string]: any
}

interface Props {
  field: DynamicField
  modelValue: any
}

interface Emits {
  (e: 'update:modelValue', value: any): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 根据字段类型获取对应组件（从配置文件）
const fieldComponent = computed(() => {
  return getFieldComponent(props.field.type)
})

// 双向绑定的值
const modelValue = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 根据字段类型返回组件所需的 props
const componentProps = computed(() => {
  // 获取该字段类型的默认属性
  const defaultProps = getFieldDefaultProps(props.field.type)
  
  // 基础属性
  const baseProps: Record<string, any> = {
    placeholder: props.field.helpText || `请输入${props.field.label}`
  }
  
  // 特殊处理某些字段类型
  const specialProps: Record<string, any> = {}
  
  if (props.field.type === 'checkbox') {
    specialProps.label = props.field.helpText || props.field.label
    delete baseProps.placeholder // checkbox 不需要 placeholder
  }
  
  // 合并所有属性：默认属性 + 基础属性 + 特殊属性
  return {
    ...defaultProps,
    ...baseProps,
    ...specialProps
  }
})
</script>

<style scoped>
/* 组件样式继承父级 */
</style>

