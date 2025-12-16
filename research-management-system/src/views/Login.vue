<template>
  <div class="login-page">
    <div class="login-hero">
      <div class="hero-badge">LabNexus</div>
      <h1 class="hero-title">欢迎回来，科研人</h1>
      <p class="hero-sub">统一成果 · 高效协同 · 智能洞察</p>
      <div class="hero-stats">
        <div class="stat glass">
          <div class="stat-label">在研项目</div>
          <div class="stat-value">12</div>
          <div class="stat-desc">3 个即将结题</div>
        </div>
        <div class="stat glass">
          <div class="stat-label">待处理事项</div>
          <div class="stat-value">5</div>
          <div class="stat-desc">设备采购与审批</div>
        </div>
      </div>
    </div>

    <div class="login-card glass">
      <div class="card-header">
        <div>
          <div class="card-title">科研成果管理系统</div>
          <div class="card-sub">登录以查看你的进展和任务</div>
        </div>
        <div class="quick-accounts">
          <span class="quick-label">一键填充</span>
          <el-tag
            v-for="acc in quickAccounts"
            :key="acc.username"
            type="info"
            effect="plain"
            class="quick-tag"
            @click="fillAccount(acc)"
          >
            {{ acc.label }}
          </el-tag>
        </div>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入账号"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <div class="form-extra">
          <el-checkbox v-model="loginForm.remember">记住登录状态</el-checkbox>
          <span class="link">忘记密码？联系管理员</span>
        </div>
        <el-button
          type="primary"
          size="large"
          :loading="loading"
          class="login-button"
          native-type="submit"
        >
          登录
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '@/api/auth'
import { useFormSubmit } from '@/composables/useErrorHandler'
import { AppError, ErrorType } from '@/utils/errorHandler'
import { UserRole } from '@/types'

type LoginForm = {
  username: string
  password: string
  remember: boolean
}

type LoginResponse = {
  token: string
  user: Record<string, any>
}

type QuickAccount = {
  label: string
  username: string
  password: string
}

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance | null>(null)
const quickAccounts: QuickAccount[] = [
  { label: '管理员', username: 'admin', password: 'admin123' },
  { label: '专家', username: 'expert', password: 'expert123' },
  { label: '科研用户', username: 'researcher', password: 'researcher123' }
]

const loginForm = reactive<LoginForm>({
  username: '',
  password: '',
  remember: false
})

const rules: FormRules<LoginForm> = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 使用 useFormSubmit Hook 处理表单提交
const { submitting: loading, submit } = useFormSubmit<LoginResponse>(
  async (formData: LoginForm) => {
    const res = await login({
      username: formData.username.trim(),
      password: formData.password
    })
    const { data } = res || {}

    if (!data?.token || !data?.user) {
      throw new AppError(
        '登录响应异常，请稍后重试',
        ErrorType.BUSINESS,
        'LOGIN_RESPONSE_ERROR'
      )
    }

    return data
  },
  {
    validate: async () => {
      if (!loginFormRef.value) return false
      return await loginFormRef.value.validate()
    },
    onSuccess: async (data) => {
      // 先保存登录信息（保存失败则不跳转）
      const ok = userStore.login(data.token, data.user, loginForm.remember)
      if (!ok) return

      ElMessage.success('登录成功')

      const role = data.user?.role || UserRole.RESEARCHER
      const redirect = resolveRedirect(route.query.redirect, role)

      // 等待状态更新完成后再跳转
      await nextTick()
      router.replace(redirect)
    },
    successMessage: undefined // 已经在 onSuccess 中处理
  }
)

async function handleLogin() {
  if (loading.value) return
  await submit({ ...loginForm })
}

// 根据角色获取默认路由 - 使用传入的 role 参数,避免响应式延迟
function getDefaultRouteByRole(role: string) {
  if (role === UserRole.ADMIN || role === UserRole.MANAGER) {
    return '/admin/dashboard'
  } else if (role === UserRole.EXPERT) {
    return '/expert/reviews'
  }
  return '/dashboard'
}

function resolveRedirect(rawRedirect: unknown, role: string) {
  const fallback = getDefaultRouteByRole(role)
  if (typeof rawRedirect !== 'string' || rawRedirect === '/' || rawRedirect === '/login') {
    return fallback
  }

  const resolved = router.resolve(rawRedirect)
  if (!resolved.matched.length) {
    return fallback
  }

  const requiredRoles = (resolved.meta.roles ||
    resolved.matched[resolved.matched.length - 1]?.meta.roles) as string[] | undefined

  if (Array.isArray(requiredRoles) && requiredRoles.length > 0 && !requiredRoles.includes(role)) {
    return fallback
  }

  return resolved.fullPath
}

function fillAccount(acc: QuickAccount) {
  loginForm.username = acc.username
  loginForm.password = acc.password

  // 清理上一次的校验提示
  nextTick(() => {
    loginFormRef.value?.clearValidate()
  })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr 440px;
  gap: 32px;
  align-items: center;
  padding: 60px;
  background: radial-gradient(circle at 20% 20%, rgba(29, 91, 255, 0.12), transparent 30%),
    radial-gradient(circle at 80% 10%, rgba(15, 69, 216, 0.12), transparent 28%),
    #f7f9fc;
}

.login-hero {
  padding: 32px;
  color: #0f172a;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  padding: 8px 12px;
  background: #e5edff;
  color: #1d5bff;
  border-radius: 12px;
  font-weight: 700;
  margin-bottom: 16px;
}

.hero-title {
  font-size: 34px;
  font-weight: 800;
  margin: 0 0 8px;
}

.hero-sub {
  color: #6b7280;
  margin-bottom: 18px;
  font-size: 16px;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 14px;
}

.stat {
  padding: 14px 16px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid #eef2f7;
}

.stat-label {
  color: #6b7280;
  font-size: 13px;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
}

.stat-desc {
  font-size: 12px;
  color: #6b7280;
}

.login-card {
  background: #fff;
  padding: 28px;
  border-radius: 18px;
  box-shadow: 0 16px 40px rgba(17, 24, 39, 0.12);
  border: 1px solid #eef2f7;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 18px;
  gap: 12px;
}

.card-title {
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
}

.card-sub {
  color: #6b7280;
  margin-top: 4px;
  font-size: 13px;
}

.quick-accounts {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.quick-label {
  font-size: 12px;
  color: #6b7280;
}

.quick-tag {
  cursor: pointer;
}

.login-form {
  margin-top: 8px;
}

.form-extra {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 8px 0 16px;
  color: #6b7280;
  font-size: 13px;
}

.link {
  color: #1d5bff;
  cursor: pointer;
}

.login-button {
  width: 100%;
  height: 44px;
  border-radius: 12px;
}

.glass {
  backdrop-filter: blur(10px);
}

@media (max-width: 1024px) {
  .login-page {
    grid-template-columns: 1fr;
    padding: 32px 20px;
  }

  .login-hero {
    order: 2;
  }

  .login-card {
    order: 1;
  }
}
</style>
