<template>
  <el-container class="main-layout">
    <!-- 顶部导航 -->
    <el-header class="header glass">
      <div class="header-left">
        <div class="brand">
          <div class="brand-logo">Lab</div>
          <div class="brand-text">
            <div class="brand-title">Nexus</div>
            <!-- <div class="brand-sub">{{ userStore.userInfo?.department || '科研机构' }}</div> -->
          </div>
        </div>
        <div class="search-bar">
          <el-input
            v-model="keyword"
            placeholder="搜索科研项目、文献或人员"
            :prefix-icon="Search"
            clearable
            size="large"
          />
          <div class="status-dot">
            <span class="dot"></span>
            <span class="text">系统运行正常</span>
          </div>
        </div>
      </div>
      <div class="header-right">
        <el-button round size="large" @click="showMessages">
          <Bell class="header-icon" />
        </el-button>
        <el-button type="primary" round size="large" :icon="Plus" @click="router.push('/results/create')">
          新建成果
        </el-button>
        <el-dropdown @command="handleCommand">
          <div class="user-info pill">
            <!-- <el-avatar :src="userStore.userInfo?.avatar" :size="32">
              {{ userStore.userInfo?.name?.charAt(0) }}
            </el-avatar> -->
            <div class="user-meta">
              <span class="username">{{ userStore.userInfo?.name }}</span>
              <span class="role">{{ userStore.userInfo?.roles.join('/') }}</span>
            </div>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人信息</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container class="main-container">
      <!-- 左侧导航 -->
      <el-aside width="240px" class="sidebar glass">
        <el-menu
          :default-active="activeMenu"
          :router="true"
          class="sidebar-menu"
        >
          <template v-for="item in menuItems" :key="item.path">
            <el-menu-item
              v-if="!item.children && hasPermission(item.roles)"
              :index="item.path"
              class="menu-pill"
            >
              <el-icon class="menu-icon" size="18">
                <component :is="item.icon" />
              </el-icon>
              <span>{{ item.title }}</span>
            </el-menu-item>

            <el-sub-menu
              v-else-if="item.children && hasPermission(item.roles)"
              :index="item.path"
              class="menu-pill"
            >
              <template #title>
                <el-icon class="menu-icon" size="18">
                  <component :is="item.icon" />
                </el-icon>
                <span>{{ item.title }}</span>
              </template>
              <el-menu-item
                v-for="child in item.children"
                :key="child.path"
                :index="child.path"
              >
                {{ child.title }}
              </el-menu-item>
            </el-sub-menu>
          </template>
        </el-menu>

        <div class="sidebar-footer">
          <div
            v-if="showSystemSettingsCard"
            class="footer-card"
            @click="goSystemSettings"
          >
            <div class="footer-title">系统设置</div>
            <div class="footer-desc">通知、偏好、主题</div>
          </div>
          <div class="profile-card">
            <!-- <el-avatar :src="userStore.userInfo?.avatar" :size="40">
              {{ userStore.userInfo?.name?.charAt(0) }}
            </el-avatar> -->
            <div class="profile-meta">
              <div class="profile-name">{{ userStore.userInfo?.name || '用户' }}</div>
              <!-- <div class="profile-role">{{ userStore.userInfo?.department || '实验室' }}</div> -->
            </div>
          </div>
        </div>
      </el-aside>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <div class="breadcrumb-container glass">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute.meta.title">
              {{ currentRoute.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="content-wrapper">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </el-main>
    </el-container>

    <!-- 消息抽屉 -->
    <el-drawer v-model="messageDrawer" title="消息中心" size="400px">
      <div class="message-list">
        <el-empty v-if="messages.length === 0" description="暂无消息" />
        <div v-else>
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="message-item"
            :class="{ unread: !msg.read }"
            @click="handleMessageClick(msg)"
          >
            <div class="message-title">{{ msg.title }}</div>
            <div class="message-time">{{ msg.time }}</div>
          </div>
        </div>
      </div>
    </el-drawer>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Bell,
  House,
  Plus,
  Document,
  Search,
  Checked,
  DataAnalysis,
  Setting,
  User,
  List,
  FolderOpened
} from '@element-plus/icons-vue'
import { UserRole } from '@/types'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const keyword = ref('')

const currentRoute = computed(() => route)
const activeMenu = computed(() => route.path)
const showSystemSettingsCard = computed(() => userStore.isAdmin)

// 消息相关
const messageDrawer = ref(false)
const unreadCount = ref(0)
const messages = ref([])

// 菜单配置
const menuItems = computed(() => {
  const items: Array<{
    path: string
    title: string
    icon?: any
    roles?: string[]
    children?: Array<{ path: string; title: string }>
  }> = []

  // 管理员菜单结构（优先级最高）
  if (userStore.isAdmin) {
    // 顶部：创建成果物
    items.push({
      path: '/results/create',
      title: '创建成果物',
      icon: Plus,
      roles: [UserRole.ADMIN]
    })

    // 顶部：科研看台
    items.push({
      path: '/admin/dashboard',
      title: '科研看台',
      icon: DataAnalysis,
      roles: [UserRole.ADMIN]
    })

    // 1. 成果物
    items.push({
      path: '/results',
      title: '成果物',
      icon: Document,
      roles: [UserRole.ADMIN],
      children: [
        { path: '/results/list', title: '科研成果' },
        { path: '/results/search', title: '成果检索' },
        { path: '/admin/interim-results', title: '过程成果' }
      ]
    })

    // 2. 审核与权限（流程类）
    items.push({
      path: '/review',
      title: '审核与权限',
      icon: Checked,
      roles: [UserRole.ADMIN],
      children: [
        { path: '/expert/reviews', title: '成果审核' },
        { path: '/admin/access-requests', title: '权限审核' },
        { path: '/admin/review-assign', title: '审核分配' }
      ]
    })

    // 3. 科技助手
    items.push({
      path: '/insights',
      title: '科技助手',
      icon: DataAnalysis,
      roles: [UserRole.ADMIN],
      children: [
        { path: '/insights/demands', title: '需求洞察' },
        { path: '/admin/research-insights', title: '研究洞察' }
      ]
    })

    // 5. 系统配置（低频维护）
    const systemConfigChildren = [
      { path: '/admin/system-settings', title: '系统设置' }
    ]

    // 只有管理员可以访问成果类型配置
    if (userStore.isAdmin) {
      systemConfigChildren.push({ path: '/admin/result-types', title: '成果类型配置' })
    }

    items.push({
      path: '/system',
      title: '系统配置',
      icon: Setting,
      roles: [UserRole.ADMIN],
      children: systemConfigChildren
    })
  } else if (userStore.isExpert) {
    // 专家菜单结构
    items.push({
      path: '/results/create',
      title: '创建成果物',
      icon: Plus,
      roles: [UserRole.RESEARCHER, UserRole.EXPERT, UserRole.ADMIN]
    })
    items.push({
      path: '/results/list',
      title: '科研成果',
      icon: List,
      roles: [UserRole.RESEARCHER, UserRole.EXPERT, UserRole.ADMIN]
    })
    items.push({
      path: '/results/search',
      title: '成果检索',
      icon: Search,
      roles: [UserRole.RESEARCHER, UserRole.EXPERT, UserRole.ADMIN]
    })
    items.push({
      path: '/insights/demands',
      title: '需求洞察',
      icon: DataAnalysis,
      roles: [UserRole.RESEARCHER, UserRole.EXPERT, UserRole.ADMIN]
    })
    items.push({
      path: '/expert/reviews',
      title: '成果审核',
      icon: Checked,
      roles: [UserRole.EXPERT, UserRole.ADMIN]
    })
  } else {
    // 普通用户菜单结构
    items.push({
      path: '/dashboard',
      title: '个人概览',
      icon: House,
      roles: [UserRole.RESEARCHER, UserRole.EXPERT, UserRole.ADMIN]
    })
    items.push({
      path: '/results/create',
      title: '创建成果物',
      icon: Plus,
      roles: [UserRole.RESEARCHER, UserRole.EXPERT, UserRole.ADMIN]
    })
    items.push({
      path: '/results/my',
      title: '个人成果物',
      icon: Document,
      roles: [UserRole.RESEARCHER, UserRole.EXPERT, UserRole.ADMIN]
    })
    items.push({
      path: '/results/list',
      title: '科研成果',
      icon: List,
      roles: [UserRole.RESEARCHER, UserRole.EXPERT, UserRole.ADMIN]
    })
    items.push({
      path: '/results/search',
      title: '成果检索',
      icon: Search,
      roles: [UserRole.RESEARCHER, UserRole.EXPERT, UserRole.ADMIN]
    })
    items.push({
      path: '/insights/demands',
      title: '需求洞察',
      icon: DataAnalysis,
      roles: [UserRole.RESEARCHER, UserRole.EXPERT, UserRole.ADMIN]
    })
  }

  return items
})

// 权限检查
function hasPermission(roles) {
  if (!roles || roles.length === 0) return true
  return userStore.hasRole(roles)
}

// 显示消息
function showMessages() {
  messageDrawer.value = true
}

// 处理消息点击
function handleMessageClick(msg) {
  // TODO: 跳转到相关页面
  console.log('Message clicked:', msg)
}

function goSystemSettings() {
  router.push('/admin/system-settings')
}

// 处理用户菜单命令
async function handleCommand(command) {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      userStore.logout()
      redirectToLoginPortal()
      ElMessage.success('已退出登录')
    } catch {
      // 取消操作
    }
  } else if (command === 'profile') {
    ElMessage.info('个人信息功能开发中')
  }
}

/**
 * 跳转回 login-portal
 */
function redirectToLoginPortal() {
  window.location.href = import.meta.env.VITE_LOGIN_PORTAL_URL
}
</script>

<style scoped>
.main-layout {
  height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: transparent;
  padding: 12px 24px;
  border-bottom: none;
  backdrop-filter: blur(10px);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
  flex: 1;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-logo {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  background: var(--primary-gradient);
  color: #fff;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  letter-spacing: 0.5px;
  box-shadow: 0 12px 24px rgba(13, 62, 255, 0.25);
}

.brand-text {
  display: flex;
  flex-direction: column;
}

.brand-title {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
}

.brand-sub {
  font-size: 12px;
  color: #6b7280;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  max-width: 720px;
  background: #fff;
  border-radius: 16px;
  padding: 10px 14px;
  box-shadow: 0 8px 24px rgba(17, 24, 39, 0.06);
  border: 1px solid #eef2f7;
}

.status-dot {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  background: #f3f6ff;
  color: #1d5bff;
  border-radius: 12px;
  font-size: 12px;
}

.status-dot .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #22c55e;
  box-shadow: 0 0 0 4px rgba(34, 197, 94, 0.18);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.message-badge {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 12px;
  transition: background-color 0.3s;
  background: #f8fafc;
}

.user-info:hover {
  background-color: #f5f6fa;
}

.username {
  font-size: 14px;
  color: #303133;
}

.user-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.role {
  font-size: 12px;
  color: #6b7280;
}

.main-container {
  height: calc(100vh - 60px);
}

.sidebar {
  background: #fff;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 16px;
  display: flex;
  flex-direction: column;
}

/* 美化滚动条 */
.sidebar::-webkit-scrollbar {
  width: 6px;
}

.sidebar::-webkit-scrollbar-thumb {
  background: #e5e7eb;
  border-radius: 3px;
}

.sidebar::-webkit-scrollbar-thumb:hover {
  background: #d1d5db;
}

.sidebar::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar-menu {
  border-right: none;
  background: transparent;
  flex: 1;
}

/* 重置Element Plus默认样式，防止重叠 */
.sidebar-menu .el-menu-item,
.sidebar-menu .el-sub-menu__title {
  display: flex;
  align-items: center;
  padding-left: 20px;
  padding-right: 20px;
}

/* 菜单图标样式 */
.menu-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-right: 8px;
  font-size: 18px;
  flex-shrink: 0;
  width: 18px;
  height: 18px;
}

/* 确保子菜单标题中的图标正确显示 */
.el-sub-menu__title .menu-icon {
  margin-right: 8px;
}

/* 确保文字不会被图标覆盖 */
.el-sub-menu__title span {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.el-menu-item span {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.menu-pill {
  margin: 6px 0;
  border-radius: 12px !important;
}

/* 普通菜单项样式 */
.el-menu-item.menu-pill {
  height: 44px;
  line-height: 44px;
}

/* 子菜单容器样式 */
.el-sub-menu.menu-pill {
  border-radius: 12px !important;
  overflow: hidden;
}

/* 子菜单展开后的内容区域 */
.el-sub-menu.menu-pill .el-menu {
  background: transparent !important;
  padding: 4px 0;
}

/* 子菜单标题样式 */
.el-sub-menu.menu-pill .el-sub-menu__title {
  height: 44px;
  line-height: 44px;
  border-radius: 12px !important;
  padding-left: 20px !important;
  padding-right: 20px !important;
  display: flex;
  align-items: center;
}

/* 子菜单展开图标 */
.el-sub-menu.menu-pill .el-sub-menu__icon-arrow {
  position: absolute;
  right: 20px;
  margin-left: auto;
}

/* 子菜单标题hover效果 */
.el-sub-menu.menu-pill .el-sub-menu__title:hover {
  background: #f5f7fb !important;
}

/* 子菜单项样式 */
.el-sub-menu.menu-pill .el-menu-item {
  height: 40px;
  line-height: 40px;
  padding-left: 48px !important;
  border-radius: 8px;
  margin: 4px 12px;
}

/* 子菜单项hover效果 */
.el-sub-menu.menu-pill .el-menu-item:hover {
  background: #f5f7fb !important;
}

.el-menu-item.is-active {
  background: #eef3ff !important;
  color: var(--primary-color);
  font-weight: 600;
}

.sidebar-footer {
  margin-top: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.footer-card {
  padding: 12px;
  border-radius: 12px;
  background: #f5f7fb;
  color: #4b5563;
  border: 1px solid #eef2f7;
}

.footer-title {
  font-weight: 600;
  color: #111827;
  margin-bottom: 4px;
}

.footer-desc {
  font-size: 12px;
}

.profile-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid #eef2f7;
  box-shadow: 0 10px 24px rgba(17, 24, 39, 0.06);
}

.profile-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.profile-name {
  font-weight: 600;
  color: #0f172a;
}

.profile-role {
  font-size: 12px;
  color: #6b7280;
}

.main-content {
  background: #f5f6fa;
  padding: 0;
  overflow-y: auto;
}

.breadcrumb-container {
  background: #fff;
  padding: 12px 24px;
  border-bottom: 1px solid #eef2f7;
}

.content-wrapper {
  padding: 20px;
}

.glass {
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 10px 30px rgba(17, 24, 39, 0.08);
  border: 1px solid #eef2f7;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.message-list {
  padding: 0;
}

.message-item {
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
  cursor: pointer;
  transition: background-color 0.3s;
}

.message-item:hover {
  background-color: #f5f6fa;
}

.message-item.unread {
  background-color: #ecf5ff;
}

.message-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 8px;
}

.message-time {
  font-size: 12px;
  color: #909399;
}
</style>
