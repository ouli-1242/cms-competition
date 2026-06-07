<script setup lang="ts">
/**
 * 管理员后台布局
 * - 蓝色渐变侧边栏
 * - 顶部栏：切换角色 / 欢迎 / 退出登录
 */
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 管理员菜单
const menus = [
  { path: '/admin', label: '控制台', icon: 'Grid' },
  { path: '/admin/competitions', label: '竞赛管理', icon: 'Calendar' },
  { path: '/admin/registrations', label: '报名管理', icon: 'UserFilled' },
  { path: '/admin/banners', label: '内容维护', icon: 'Picture' },
  { path: '/admin/stats', label: '数据统计', icon: 'TrendCharts' },
  { path: '/student-center/my-teams', label: '个人中心', icon: 'User' }
]

// 顶部信息
const roleText = computed(() => {
  const map: any = { ADMIN: '管理员', TEACHER: '老师', STUDENT: '学生' }
  return map[userStore.user?.role] || '用户'
})

const userDisplay = computed(() => {
  return userStore.user?.realName || userStore.user?.username || '系统管理员'
})

function logout() {
  userStore.logout()
  router.push('/login')
}

// 切换角色
const roleOptions = [
  { label: '管理员', value: 'admin' },
  { label: '老师', value: 'teacher' },
  { label: '学生', value: 'student' }
]

const currentRole = ref('admin')

function switchRole(val: string) {
  if (val === 'admin') {
    router.push('/admin')
  } else if (val === 'teacher') {
    router.push('/teacher')
  } else {
    router.push('/student/profile')
  }
}

function showRoleDialog() {
  // 简化：直接弹出下拉选择，这里用 prompt 模拟
  const role = window.prompt('切换角色：\n1. 管理员\n2. 老师\n3. 学生\n\n请输入数字：', '1')
  if (role === '1') router.push('/admin')
  else if (role === '2') router.push('/teacher')
  else if (role === '3') router.push('/student/profile')
}
</script>

<template>
  <div class="admin-layout">
    <!-- 蓝色渐变侧边栏 -->
    <aside class="sider">
      <div class="logo">
        <span class="logo-icon">🏆</span>
        <span class="logo-text">竞赛管理系统</span>
      </div>

      <nav class="menu">
        <router-link
          v-for="m in menus"
          :key="m.path"
          :to="m.path"
          class="menu-item"
          :class="{ active: m.path === '/admin' ? route.path === '/admin' : route.path.startsWith(m.path) }"
        >
          <el-icon class="menu-icon">
            <component :is="m.icon" />
          </el-icon>
          <span class="menu-label">{{ m.label }}</span>
        </router-link>
      </nav>
    </aside>

    <!-- 内容区 -->
    <div class="content">
      <!-- 顶部栏 -->
      <header class="topbar">
        <el-button class="role-switch" type="primary" plain @click="showRoleDialog">
          <el-icon><Refresh /></el-icon>
          切换角色: {{ roleText }}
        </el-button>

        <div class="user-area">
          <span class="welcome">欢迎: <strong>{{ userDisplay }}</strong></span>
          <el-button class="logout-btn" type="info" plain @click="logout">
            <el-icon><SwitchButton /></el-icon>退出登录
          </el-button>
        </div>
      </header>

      <!-- 主体 -->
      <main class="main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.admin-layout {
  min-height: 100vh;
  display: flex;
  background: $bg-page;
}

// ===== 侧边栏 =====
.sider {
  width: 220px;
  background: linear-gradient(180deg, #4a90e2 0%, #2b6cb0 100%);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  position: sticky;
  top: 0;
  height: 100vh;
}

.logo {
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $space-2;
  color: #fff;
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

.logo-icon {
  font-size: 22px;
}

.menu {
  flex: 1;
  padding: $space-4 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: $space-3;
  padding: $space-3 $space-5;
  color: rgba(255, 255, 255, 0.85);
  text-decoration: none;
  font-size: $font-size-base;
  transition: all $transition-fast;
  position: relative;
  cursor: pointer;

  .menu-icon {
    font-size: 18px;
  }

  &:hover {
    background: rgba(255, 255, 255, 0.1);
    color: #fff;
  }

  &.active {
    background: rgba(0, 0, 0, 0.15);
    color: #fff;
    font-weight: $font-weight-semibold;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 3px;
      background: #fff;
    }
  }
}

// ===== 内容区 =====
.content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.topbar {
  height: 64px;
  background: $bg-card;
  border-bottom: 1px solid $border-light;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 $space-6;
  box-shadow: $shadow-sm;
}

.role-switch {
  font-weight: $font-weight-medium;
}

.user-area {
  display: flex;
  align-items: center;
  gap: $space-4;
}

.welcome {
  font-size: $font-size-sm;
  color: $text-regular;
  strong {
    color: $text-primary;
    font-weight: $font-weight-semibold;
  }
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}

.main {
  flex: 1;
  padding: $space-6;
  overflow-y: auto;
}

// 响应式
@media (max-width: 768px) {
  .sider {
    width: 64px;
  }
  .logo-text,
  .menu-label {
    display: none;
  }
  .menu-item {
    justify-content: center;
    padding: $space-3;
  }
  .welcome {
    display: none;
  }
}
</style>
