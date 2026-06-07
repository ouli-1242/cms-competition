<script setup lang="ts">
/**
 * 老师工作台布局
 * - 橙色渐变侧边栏
 * - 仅显示老师权限内的菜单
 */
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const menus = [
  { path: '/teacher', label: '控制台', icon: 'Grid' },
  { path: '/teacher/audit', label: '报名审核', icon: 'CircleCheck' },
  { path: '/teacher/stats', label: '数据统计', icon: 'TrendCharts' },
  { path: '/student/profile', label: '个人中心', icon: 'User' }
]

const roleText = computed(() => {
  const map: any = { ADMIN: '管理员', TEACHER: '老师', STUDENT: '学生' }
  return map[userStore.user?.role] || '用户'
})

const userDisplay = computed(() => {
  return userStore.user?.realName || userStore.user?.username || '老师'
})

function logout() {
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="teacher-layout">
    <!-- 橙色渐变侧边栏 -->
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
          :class="{ active: m.path === '/teacher' ? route.path === '/teacher' : route.path.startsWith(m.path) }"
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

      <main class="main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.teacher-layout {
  min-height: 100vh;
  display: flex;
  background: $bg-page;
}

.sider {
  width: 220px;
  background: linear-gradient(180deg, #f6ad55 0%, #ed8936 100%);
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
