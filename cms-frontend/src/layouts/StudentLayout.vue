<script setup lang="ts">
/**
 * 学生布局 - 左侧侧边栏
 */
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const menus = [
  { path: '/student/profile', label: '个人中心', icon: 'User' },
  { path: '/student/my-registrations', label: '我的报名', icon: 'Document' },
  { path: '/student/my-teams', label: '我的团队', icon: 'UserFilled' }
]

function logout() {
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="layout">
    <aside class="sider">
      <div class="logo">
        <span class="logo-icon">🏆</span>
        <span class="logo-text">竞赛报名系统</span>
      </div>
      <el-menu :default-active="route.path" router class="menu">
        <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">
          <el-icon><component :is="m.icon" /></el-icon>
          <span>{{ m.label }}</span>
        </el-menu-item>
      </el-menu>
    </aside>
    <div class="content">
      <header class="topbar">
        <div class="user-info">
          <el-avatar :size="32">{{ userStore.user?.realName?.[0] || 'U' }}</el-avatar>
          <span>{{ userStore.user?.realName || userStore.user?.username }}（学生）</span>
        </div>
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
}

.menu {
  border-right: none;
  padding-top: $space-4;
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
