/**
 * 路由配置
 */
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册', public: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/PublicLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/public/Home.vue'),
        meta: { title: '首页', public: true }
      },
      {
        path: 'competitions',
        name: 'CompetitionList',
        component: () => import('@/views/public/CompetitionList.vue'),
        meta: { title: '竞赛列表', public: true }
      },
      {
        path: 'competitions/:id',
        name: 'CompetitionDetail',
        component: () => import('@/views/public/CompetitionDetail.vue'),
        meta: { title: '竞赛详情', public: true }
      },
      {
        path: 'guest-required',
        name: 'GuestRequired',
        component: () => import('@/views/shared/GuestPermission.vue'),
        meta: { title: '权限提示', public: true }
      }
    ]
  },
  {
    path: '/student',
    component: () => import('@/layouts/PublicLayout.vue'),
    children: [
      {
        path: 'profile',
        name: 'StudentProfile',
        component: () => import('@/views/student/Profile.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      },
      {
        path: 'profile/edit',
        name: 'StudentProfileEdit',
        component: () => import('@/views/student/ProfileEdit.vue'),
        meta: { title: '编辑个人资料', requiresAuth: true }
      },
      {
        path: 'profile/password',
        name: 'StudentChangePassword',
        component: () => import('@/views/student/ChangePassword.vue'),
        meta: { title: '修改密码', requiresAuth: true }
      }
    ]
  },
  // 学生后台（有侧边栏的页面）
  {
    path: '/student-center',
    component: () => import('@/layouts/StudentLayout.vue'),
    meta: { roles: ['STUDENT'] },
    children: [
      {
        path: 'my-registrations',
        name: 'MyRegistrations',
        component: () => import('@/views/student/MyRegistrations.vue'),
        meta: { title: '我的报名' }
      },
      {
        path: 'my-teams',
        name: 'MyTeams',
        component: () => import('@/views/student/MyTeams.vue'),
        meta: { title: '我的团队' }
      },
      {
        path: 'register-individual/:id',
        name: 'IndividualRegister',
        component: () => import('@/views/student/IndividualRegister.vue'),
        meta: { title: '个人赛报名' }
      },
      {
        path: 'team-create',
        name: 'TeamCreate',
        component: () => import('@/views/student/TeamCreate.vue'),
        meta: { title: '创建团队' }
      },
      {
        path: 'team-join',
        name: 'TeamJoin',
        component: () => import('@/views/student/TeamJoin.vue'),
        meta: { title: '加入团队' }
      },
      {
        path: 'team-manage/:id',
        name: 'TeamManage',
        component: () => import('@/views/student/TeamManage.vue'),
        meta: { title: '团队管理' }
      },
      {
        path: 'team-submit/:id',
        name: 'TeamSubmit',
        component: () => import('@/views/student/TeamSubmit.vue'),
        meta: { title: '提交团队报名' }
      },
      {
        path: 'team-detail/:id',
        name: 'TeamDetail',
        component: () => import('@/views/student/TeamDetail.vue'),
        meta: { title: '团队详情' }
      }
    ]
  },
  {
    path: '/teacher',
    component: () => import('@/layouts/TeacherLayout.vue'),
    meta: { roles: ['TEACHER'] },
    children: [
      {
        path: '',
        name: 'TeacherDashboard',
        component: () => import('@/views/shared/Dashboard.vue'),
        meta: { title: '控制台' }
      },
      {
        path: 'audit',
        name: 'TeacherAudit',
        component: () => import('@/views/teacher/Audit.vue'),
        meta: { title: '报名审核' }
      },
      {
        path: 'stats',
        name: 'TeacherStats',
        component: () => import('@/views/teacher/Stats.vue'),
        meta: { title: '我的统计' }
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { roles: ['ADMIN'] },
    children: [
      {
        path: 'competitions',
        name: 'AdminCompetitions',
        component: () => import('@/views/admin/Competitions.vue'),
        meta: { title: '竞赛管理' }
      },
      {
        path: 'registrations',
        name: 'AdminRegistrations',
        component: () => import('@/views/admin/Registrations.vue'),
        meta: { title: '报名管理' }
      },
      {
        path: 'banners',
        name: 'AdminBanners',
        component: () => import('@/views/admin/Banners.vue'),
        meta: { title: '轮播图管理' }
      },
      {
        path: 'stats',
        name: 'AdminStats',
        component: () => import('@/views/admin/Stats.vue'),
        meta: { title: '数据大屏' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '404', public: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  document.title = (to.meta.title as string)
    ? `${to.meta.title} - 高校学科竞赛报名管理系统`
    : '高校学科竞赛报名管理系统'

  const isPublic = to.meta.public === true
  const requiredRoles = to.meta.roles as string[] | undefined

  // 延迟获取 userStore，避免 Pinia 未初始化时报错
  let userStore: ReturnType<typeof useUserStore> | null = null
  try {
    userStore = useUserStore()
  } catch {
    userStore = null
  }

  const isLoggedIn = userStore?.isLoggedIn ?? false
  const role = userStore?.role ?? 'VISITOR'

  if (isPublic) {
    // 已登录访问登录/注册页则跳到首页
    if (isLoggedIn && (to.name === 'Login' || to.name === 'Register')) {
      return next({ name: 'Home' })
    }
    return next()
  }

  if (!isLoggedIn) {
    return next({ name: 'Login', query: { redirect: to.fullPath } })
  }

  if (requiredRoles && !requiredRoles.includes(role)) {
    return next({ name: 'Home' })
  }

  next()
})

export default router
