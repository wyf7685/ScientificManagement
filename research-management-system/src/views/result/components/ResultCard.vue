<template>
  <div class="result-card" @click="handleClick">
    <div class="card-header">
      <div class="type-icon" :style="{ background: typeColor }">
        <component :is="typeIcon" class="icon" />
      </div>
      <el-tag :type="typeTagType" size="small" class="type-tag">
        {{ typeLabel }}
      </el-tag>
    </div>

    <div class="card-body">
      <h3 class="title" :title="result.title">
        {{ result.title }}
      </h3>

      <div class="authors">
        <el-icon class="author-icon"><User /></el-icon>
        <span class="author-text">{{ authorsText }}</span>
      </div>

      <div class="meta-info">
        <div class="meta-item">
          <el-icon><Calendar /></el-icon>
          <span>{{ formatDate(result.createdAt) }}</span>
        </div>
        <div class="meta-item">
          <el-icon><View /></el-icon>
          <span>{{ result.viewCount || 0 }}</span>
        </div>
      </div>
    </div>

    <div class="card-footer">
      <el-button type="primary" link size="small" :icon="View">
        查看详情
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Document, TrophyBase, Monitor, User, Calendar, View } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

interface Props {
  result: {
    id: string
    title: string
    type: string
    authors: string[]
    createdAt: string
    viewCount?: number
  }
}

const props = defineProps<Props>()
const emit = defineEmits(['click'])

// 成果类型配置
const typeConfig: Record<string, any> = {
  paper: {
    label: '学术论文',
    icon: Document,
    color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    tagType: 'primary'
  },
  patent: {
    label: '发明专利',
    icon: TrophyBase,
    color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    tagType: 'danger'
  },
  software: {
    label: '软件著作权',
    icon: Monitor,
    color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    tagType: 'success'
  }
}

const typeLabel = computed(() => {
  return typeConfig[props.result.type]?.label || '其他'
})

const typeIcon = computed(() => {
  return typeConfig[props.result.type]?.icon || Document
})

const typeColor = computed(() => {
  return typeConfig[props.result.type]?.color || 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)'
})

const typeTagType = computed(() => {
  return typeConfig[props.result.type]?.tagType || 'info'
})

const authorsText = computed(() => {
  const authors = props.result.authors || []
  if (authors.length === 0) return '未知作者'
  if (authors.length <= 3) return authors.join(', ')
  return `${authors.slice(0, 3).join(', ')} 等${authors.length}人`
})

function formatDate(date: string) {
  if (!date) return ''
  const now = dayjs()
  const target = dayjs(date)
  const diffDays = now.diff(target, 'day')
  
  if (diffDays < 7) {
    return target.fromNow()
  } else if (diffDays < 365) {
    return target.format('MM-DD')
  } else {
    return target.format('YYYY-MM-DD')
  }
}

function handleClick() {
  emit('click', props.result)
}
</script>

<style scoped>
.result-card {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  height: 280px;
}

.result-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  border-color: #1d5bff;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.type-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.type-icon .icon {
  font-size: 24px;
}

.type-tag {
  font-weight: 500;
}

.card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: hidden;
}

.title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
  min-height: 48px;
}

.authors {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #6b7280;
  font-size: 14px;
}

.author-icon {
  font-size: 16px;
  flex-shrink: 0;
}

.author-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meta-info {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: auto;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #9ca3af;
  font-size: 13px;
}

.meta-item .el-icon {
  font-size: 14px;
}

.card-footer {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
  display: flex;
  justify-content: flex-end;
}
</style>


