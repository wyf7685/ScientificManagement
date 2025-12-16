<template>
  <el-container class="main-layout">
    <!-- 顶部导航 -->
    <el-header class="header glass">
      <div class="header-left">
        <div class="brand">
          <div class="brand-logo">Lab</div>
          <div class="brand-text">
            <div class="brand-title">Nexus</div>
            <div class="brand-sub">{{ userStore.userInfo?.department || '科研机构' }}</div>
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
            <el-avatar :src="userStore.userInfo?.avatar" :size="32">
              {{ userStore.userInfo?.name?.charAt(0) }}
            </el-avatar>
            <div class="user-meta">
              <span class="username">{{ userStore.userInfo?.name }}</span>
              <span class="role">{{ userStore.userInfo?.role }}</span>
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
            <el-avatar :src="userStore.userInfo?.avatar" :size="40">
              {{ userStore.userInfo?.name?.charAt(0) }}
            </el-avatar>
            <div class="profile-meta">
              <div class="profile-name">{{ userStore.userInfo?.name || '用户' }}</div>
              <div class="profile-role">{{ userStore.userInfo?.department || '实验室' }}</div>
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
  User
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const keyword = ref('')

const currentRoute = computed(() => route)
const activeMenu = computed(() => route.path)
const showSystemSettingsCard = computed(() => userStore.isAdmin || userStore.isManager)

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
  }> = [
    {
      path: '/dashboard',
      title: '个人概览',
      icon: House,
      roles: ['researcher', 'expert', 'admin', 'manager']
    },
    {
      path: '/results/create',
      title: '创建成果物',
      icon: Plus,
      roles: ['researcher', 'expert', 'admin', 'manager']
    },
    {
      path: '/results/my',
      title: '个人成果物',
      icon: Document,
      roles: ['researcher', 'expert', 'admin', 'manager']
    },
    {
      path: '/results/search',
      title: '成果检索',
      icon: Search,
      roles: ['researcher', 'expert', 'admin', 'manager']
    },
    {
      path: '/insights/demands',
      title: '需求洞察',
      icon: DataAnalysis,
      roles: ['researcher', 'expert', 'admin', 'manager']
    }
  ]

  // 专家菜单
  if (userStore.isExpert) {
    items.push({
      path: '/expert/reviews',
      title: '成果审核',
      icon: Checked,
      roles: ['expert', 'admin']
    })
  }

  // 管理员菜单
  if (userStore.isManager) {
    items.push({
      path: '/admin',
      title: '系统管理',
      icon: Setting,
      roles: ['admin', 'manager'],
      children: [
        { path: '/admin/dashboard', title: '科研看台' },
        { path: '/admin/results', title: '成果管理' },
        { path: '/admin/review-assign', title: '审核分配' },
        { path: '/admin/system-settings', title: '系统设置' }
      ]
    })
  }

  if (userStore.isAdmin) {
    const adminMenu = items.find(item => item.path === '/admin')
    if (adminMenu && adminMenu.children) {
      adminMenu.children.push({ path: '/admin/result-types', title: '成果类型配置' })
    }
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
      router.push('/login')
      ElMessage.success('已退出登录')
    } catch {
      // 取消操作
    }
  } else if (command === 'profile') {
    ElMessage.info('个人信息功能开发中')
  }
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
  padding: 16px;
  display: flex;
  flex-direction: column;
}

.sidebar-menu {
  border-right: none;
  background: transparent;
}

.menu-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-right: 8px;
  font-size: 18px;
}

.menu-pill {
  margin: 6px 0;
  border-radius: 12px !important;
  height: 44px;
  line-height: 44px;
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
