<script setup lang="ts">
/**
 * 老师工作台布局
 * - 橙色渐变侧边栏
 * - 仅显示老师权限内的菜单
 */
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import NotificationBell from '@/components/NotificationBell.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const menus = [
  { path: '/teacher', label: '控制台', icon: 'Grid' },
  { path: '/teacher/competitions', label: '我的竞赛', icon: 'Trophy' },
  { path: '/teacher/audit', label: '报名审核', icon: 'CircleCheck' },
  { path: '/teacher/advisor', label: '指导邀请', icon: 'UserFilled' },
  { path: '/teacher/stats', label: '数据统计', icon: 'TrendCharts' }
]

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
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="#fff">
            <path d="M12 3L1 9l4 2.18v6L12 21l7-3.82v-6l2-1.09V17h2V9L12 3zm6.82 6L12 12.72 5.18 9 12 5.28 18.82 9zM17 15.99l-5 2.73-5-2.73v-3.72L12 15l5-2.73v3.72z"/>
          </svg>
        </div>
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
        <el-button class="role-switch" type="primary" plain @click="router.push('/')">
          <el-icon><HomeFilled /></el-icon>
          返回首页
        </el-button>

        <div class="user-area">
          <NotificationBell />
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
  background: linear-gradient(180deg, #0d4b4e 0%, #1a7a7c 40%, #1e8a8c 100%);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  position: sticky;
  top: 0;
  height: 100vh;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.06);
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
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
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
  margin: 0 $space-2;
  border-radius: $radius-md;
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  font-size: $font-size-base;
  transition: all $transition-base;
  position: relative;
  cursor: pointer;

  .menu-icon {
    font-size: 18px;
    transition: transform $transition-base;
  }

  &:hover {
    background: rgba(255, 255, 255, 0.12);
    color: #fff;

    .menu-icon {
      transform: scale(1.1);
    }
  }

  &.active {
    background: rgba(255, 255, 255, 0.18);
    color: #fff;
    font-weight: $font-weight-semibold;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);

    .menu-icon {
      transform: scale(1.1);
    }

    &::before {
      content: '';
      position: absolute;
      left: -8px;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 20px;
      background: #fff;
      border-radius: 2px;
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
