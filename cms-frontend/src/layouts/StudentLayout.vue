<script setup lang="ts">
/**
 * 学生布局 - 左侧侧边栏
 */
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const menus = [
  { path: '/', label: '返回首页', icon: 'HomeFilled' },
  { path: '/student/profile', label: '个人中心', icon: 'User' },
  { path: '/student-center/my-registrations', label: '我的报名', icon: 'Document' },
  { path: '/student-center/my-teams', label: '我的团队', icon: 'UserFilled' }
]

/** 前缀匹配：子页面也能正确高亮父菜单 */
const activeMenu = computed(() => {
  const path = route.path
  if (path === '/' || path === '') return '/'
  if (path.startsWith('/student/profile')) return '/student/profile'
  if (path.startsWith('/student-center/my-teams') || path.includes('team')) return '/student-center/my-teams'
  if (path.startsWith('/student-center')) return '/student-center/my-registrations'
  return path
})

function logout() {
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="layout">
    <aside class="sider">
      <div class="logo">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="#fff">
            <path d="M12 3L1 9l4 2.18v6L12 21l7-3.82v-6l2-1.09V17h2V9L12 3zm6.82 6L12 12.72 5.18 9 12 5.28 18.82 9zM17 15.99l-5 2.73-5-2.73v-3.72L12 15l5-2.73v3.72z"/>
          </svg>
        </div>
        <span class="logo-text">竞赛报名系统</span>
      </div>
      <el-menu :default-active="activeMenu" router class="menu">
        <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">
          <el-icon><component :is="m.icon" /></el-icon>
          <span>{{ m.label }}</span>
        </el-menu-item>
      </el-menu>
    </aside>
    <div class="content">
      <header class="topbar">
        <div></div>
        <el-button text @click="logout">退出</el-button>
      </header>
      <main class="main"><router-view /></main>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.layout {
  min-height: 100vh;
  display: flex;
  background: $bg-page;
}

.sider {
  width: 220px;
  background: $bg-card;
  border-right: 1px solid $border-light;
  box-shadow: 1px 0 8px rgba(0, 0, 0, 0.03);
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  height: 100vh;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  gap: $space-2;
  padding: 0 $space-5;
  border-bottom: 1px solid $border-light;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  background: linear-gradient(135deg, #fafbfc, $bg-card);
}
.logo-icon {
  width: 32px;
  height: 32px;
  border-radius: $radius-md;
  background: linear-gradient(135deg, $primary, $primary-hover);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  box-shadow: 0 2px 8px rgba(43, 108, 176, 0.25);
  transition: transform $transition-base;

  .logo:hover & {
    transform: rotate(-10deg) scale(1.05);
  }
}

.menu {
  border-right: none !important;
  padding-top: $space-4;
  flex: 1;

  .el-menu-item {
    margin: 2px $space-2;
    border-radius: $radius-md;
    transition: all $transition-base;

    &:hover {
      background: linear-gradient(135deg, $primary-50, #e8f0fe);
      color: $primary;
    }

    &.is-active {
      background: linear-gradient(135deg, $primary-50, #e0efff);
      color: $primary;
      font-weight: $font-weight-semibold;
      box-shadow: 0 1px 4px rgba(43, 108, 176, 0.12);
      border-radius: $radius-md;
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
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(16px) saturate(180%);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  border-bottom: 1px solid rgba(237, 242, 247, 0.8);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 $space-6;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.user-info {
  display: flex;
  align-items: center;
  gap: $space-2;
  font-size: $font-size-sm;
  color: $text-regular;
}

.main {
  flex: 1;
  padding: $space-6;
  overflow-y: auto;
}
</style>
