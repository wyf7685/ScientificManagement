import { createRouter, createWebHistory } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useUserStore } from '@/stores/user'
import { UserRole } from '@/types'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      redirect: '/dashboard',
      meta: { requiresAuth: true },
      children: [
        // 普通用户路由
        {
          path: '/dashboard',
          name: 'Dashboard',
          component: () => import('@/views/Dashboard.vue'),
          meta: { title: '个人概览', roles: [UserRole.RESEARCHER, UserRole.EXPERT, UserRole.ADMIN, UserRole.MANAGER] }
        },
        {
          path: '/results/create',
          name: 'CreateResult',
          component: () => import('@/views/result/CreateResult.vue'),
          meta: { title: '创建成果物', roles: [UserRole.RESEARCHER, UserRole.EXPERT, UserRole.ADMIN, UserRole.MANAGER] }
        },
        {
          path: '/results/my',
          name: 'MyResults',
          component: () => import('@/views/result/MyResults.vue'),
          meta: { title: '个人成果物', roles: [UserRole.RESEARCHER, UserRole.EXPERT, UserRole.ADMIN, UserRole.MANAGER] }
        },
        {
          path: '/results/list',
          name: 'ResultsList',
          component: () => import('@/views/result/ResultsList.vue'),
          meta: { title: '成果列表', roles: [UserRole.RESEARCHER, UserRole.EXPERT, UserRole.ADMIN, UserRole.MANAGER] }
        },
        {
          path: '/results/search',
          name: 'SearchResults',
          component: () => import('@/views/result/SearchResults.vue'),
          meta: { title: '成果检索', roles: [UserRole.RESEARCHER, UserRole.EXPERT, UserRole.ADMIN, UserRole.MANAGER] }
        },
        {
          path: '/results/:id',
          name: 'ResultDetail',
          component: () => import('@/views/result/ResultDetail.vue'),
          meta: { title: '成果详情', roles: [UserRole.RESEARCHER, UserRole.EXPERT, UserRole.ADMIN, UserRole.MANAGER] }
        },
        {
          path: '/results/:id/edit',
          name: 'EditResult',
          component: () => import('@/views/result/EditResult.vue'),
          meta: { title: '编辑成果', roles: [UserRole.RESEARCHER, UserRole.EXPERT, UserRole.ADMIN, UserRole.MANAGER] }
        },
        {
          path: '/insights/demands',
          name: 'DemandInsights',
          component: () => import('@/views/insights/DemandInsights.vue'),
          meta: { title: '需求洞察', roles: [UserRole.RESEARCHER, UserRole.EXPERT, UserRole.ADMIN, UserRole.MANAGER] }
        },
        // 专家路由
        {
          path: '/expert/reviews',
          name: 'ExpertReviews',
          component: () => import('@/views/expert/Reviews.vue'),
          meta: { title: '成果审核', roles: [UserRole.EXPERT, UserRole.ADMIN] }
        },
        // 管理员路由
        {
          path: '/admin/dashboard',
          name: 'AdminDashboard',
          component: () => import('@/views/admin/Dashboard.vue'),
          meta: { title: '科研看台', roles: [UserRole.ADMIN, UserRole.MANAGER] }
        },
        {
          path: '/admin/results',
          name: 'AdminResults',
          component: () => import('@/views/admin/Results.vue'),
          meta: { title: '成果管理', roles: [UserRole.ADMIN, UserRole.MANAGER] }
        },
        {
          path: '/admin/access-requests',
          name: 'AccessRequests',
          component: () => import('@/views/admin/AccessRequests.vue'),
          meta: { title: '权限审核', roles: [UserRole.ADMIN, UserRole.MANAGER] }
        },
        {
          path: '/admin/review-assign',
          name: 'ReviewAssign',
          component: () => import('@/views/admin/ReviewAssign.vue'),
          meta: { title: '审核分配', roles: [UserRole.ADMIN, UserRole.MANAGER] }
        },
        {
          path: '/admin/result-types',
          name: 'ResultTypes',
          component: () => import('@/views/admin/ResultTypes.vue'),
          meta: { title: '成果类型配置', roles: [UserRole.ADMIN] }
        },
        {
          path: '/admin/system-settings',
          name: 'SystemSettings',
          component: () => import('@/views/admin/SystemSettings.vue'),
          meta: { title: '系统设置', roles: [UserRole.ADMIN, UserRole.MANAGER] }
        },
        {
          path: '/admin/research-insights',
          name: 'ResearchInsights',
          component: () => import('@/views/admin/ResearchInsights.vue'),
          meta: { title: '研究洞察', roles: [UserRole.ADMIN, UserRole.MANAGER] }
        },
        {
          path: '/admin/interim-results',
          name: 'InterimResults',
          component: () => import('@/views/admin/InterimResults.vue'),
          meta: { title: '中期成果物', roles: [UserRole.ADMIN, UserRole.MANAGER] }
        }
      ]
    },
    {
      path: '/403',
      name: 'Forbidden',
      component: () => import('@/views/error/403.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/error/404.vue'),
      meta: { requiresAuth: false }
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const { token, userInfo, isLoggedIn } = storeToRefs(userStore)
  
  // 初始化用户信息 (从 localStorage 恢复)
  if (!userInfo.value && token.value) {
    userStore.initUserInfo()
  }

  // 需要登录的页面
  if (to.meta.requiresAuth !== false) {
    if (!isLoggedIn.value) {
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }

    // 检查角色权限 - 确保 userInfo 已加载
    if (to.meta.roles && Array.isArray(to.meta.roles)) {
      // 等待 userInfo 加载完成
      if (!userInfo.value) {
        console.warn('用户信息未加载,拒绝访问:', to.path)
        next({ path: '/login', query: { redirect: to.fullPath } })
        return
      }

      const hasPermission = userStore.hasRole(to.meta.roles)
      if (!hasPermission) {
        console.warn('权限不足,用户角色:', userInfo.value?.role, '需要角色:', to.meta.roles)
        next('/403')
        return
      }
    }
  }

  // 已登录用户访问登录页，重定向到首页
  // if (to.path === '/login' && isLoggedIn.value) {
  //   next('/')
  //   return
  // }

  next()
})

export default router
