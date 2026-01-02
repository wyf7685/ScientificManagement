/**
 * 动态字段配置
 * 集中管理后端字段类型到前端组件的映射关系
 */

import { markRaw } from 'vue'
import {
  ElInput,
  ElInputNumber,
  ElDatePicker,
  ElSelect,
  ElCheckbox,
  ElSwitch
} from 'element-plus'

/**
 * 后端字段类型枚举
 */
export const BackendFieldType = {
  TEXT: 'TEXT',
  RICHTEXT: 'RICHTEXT',
  NUMBER: 'NUMBER',
  DATE: 'DATE',
  EMAIL: 'EMAIL',
  BOOLEAN: 'BOOLEAN',
  JSON: 'JSON',
  MEDIA: 'MEDIA'
} as const

/**
 * 前端字段类型枚举
 */
export const FrontendFieldType = {
  TEXT: 'text',
  TEXTAREA: 'textarea',
  NUMBER: 'number',
  DATE: 'date',
  SELECT: 'select',
  CHECKBOX: 'checkbox',
  SWITCH: 'switch'
} as const

/**
 * 后端字段类型 -> 前端字段类型映射表
 * 用于将 Strapi 返回的字段类型转换为前端渲染器能识别的类型
 */
export const BACKEND_TO_FRONTEND_TYPE_MAP: Record<string, string> = {
  [BackendFieldType.TEXT]: FrontendFieldType.TEXT,
  [BackendFieldType.RICHTEXT]: FrontendFieldType.TEXTAREA,
  [BackendFieldType.NUMBER]: FrontendFieldType.NUMBER,
  [BackendFieldType.DATE]: FrontendFieldType.DATE,
  [BackendFieldType.EMAIL]: FrontendFieldType.TEXT,
  [BackendFieldType.BOOLEAN]: FrontendFieldType.CHECKBOX,
  [BackendFieldType.JSON]: FrontendFieldType.TEXTAREA,
  [BackendFieldType.MEDIA]: FrontendFieldType.TEXT
}

/**
 * 前端字段类型 -> Element Plus 组件映射表
 * 使用 markRaw 避免 Vue 响应式系统对组件对象进行处理
 */
export const FIELD_COMPONENT_MAP: Record<string, any> = {
  [FrontendFieldType.TEXT]: markRaw(ElInput),
  [FrontendFieldType.TEXTAREA]: markRaw(ElInput),
  [FrontendFieldType.NUMBER]: markRaw(ElInputNumber),
  [FrontendFieldType.DATE]: markRaw(ElDatePicker),
  [FrontendFieldType.SELECT]: markRaw(ElSelect),
  [FrontendFieldType.CHECKBOX]: markRaw(ElCheckbox),
  [FrontendFieldType.SWITCH]: markRaw(ElSwitch)
}

/**
 * 字段类型的默认组件属性配置
 */
export const FIELD_DEFAULT_PROPS: Record<string, Record<string, any>> = {
  [FrontendFieldType.TEXT]: {},
  
  [FrontendFieldType.TEXTAREA]: {
    type: 'textarea',
    rows: 3
  },
  
  [FrontendFieldType.NUMBER]: {
    controls: true,
    step: 1
  },
  
  [FrontendFieldType.DATE]: {
    type: 'date',
    valueFormat: 'YYYY-MM-DD'
  },
  
  [FrontendFieldType.SELECT]: {
    clearable: true,
    filterable: true
  },
  
  [FrontendFieldType.CHECKBOX]: {},
  
  [FrontendFieldType.SWITCH]: {
    activeText: '是',
    inactiveText: '否'
  }
}

/**
 * 将后端字段类型映射为前端类型
 */
export function mapFieldType(backendType: string): string {
  return BACKEND_TO_FRONTEND_TYPE_MAP[backendType] || FrontendFieldType.TEXT
}

/**
 * 获取字段类型对应的组件
 */
export function getFieldComponent(fieldType: string) {
  return FIELD_COMPONENT_MAP[fieldType] || ElInput
}

/**
 * 获取字段类型的默认属性
 */
export function getFieldDefaultProps(fieldType: string): Record<string, any> {
  return { ...(FIELD_DEFAULT_PROPS[fieldType] || {}) }
}



