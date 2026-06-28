<script setup lang="ts">
/**
 * 公共布局 - 顶部 Logo + 搜索 + 登录/注册入口 + 左侧可缩放导航栏 + 内容区
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search, HomeFilled, Trophy, User, Document, UserFilled, TrendCharts, Setting } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import NotificationBell from '@/components/NotificationBell.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

onMounted(() => {
  if (userStore.isLoggedIn) {
    userStore.fetchInfo()
  }
})

const collapsed = ref(false)

// 搜索关键词
const searchKeyword = ref('')
function onSearch() {
  if (!searchKeyword.value.trim()) return
  router.push({ path: '/competitions', query: { keyword: searchKeyword.value.trim() } })
}

function logout() {
  userStore.logout()
  router.push('/login')
}

// 左侧导航（根据登录状态动态显示）
const sideMenus = computed(() => {
  const menus = [
    { path: '/', label: '首页', icon: 'HomeFilled' },
    { path: '/competitions', label: '竞赛库', icon: 'Trophy' }
  ]
  if (userStore.isLoggedIn) {
    if (userStore.isStudent) {
      menus.push(
        { path: '/student/profile', label: '个人中心', icon: 'User' },
        { path: '/student-center/my-registrations', label: '我的报名', icon: 'Document' },
        { path: '/student-center/my-teams', label: '我的团队', icon: 'UserFilled' }
      )
    } else if (userStore.isTeacher) {
      menus.push({ path: '/teacher', label: '工作台', icon: 'TrendCharts' })
    } else if (userStore.isAdmin) {
      menus.push({ path: '/admin', label: '后台管理', icon: 'Setting' })
    }
  }
  return menus
})

function isActive(t: { path: string }) {
  if (t.path === '/') return route.path === '/'
  return route.path === t.path || route.path.startsWith(t.path + '/')
}
</script>

<template>
  <div class="layout">
    <!-- 顶部导航栏 -->
    <header class="topbar">
      <div class="topbar-inner">
        <!-- Logo -->
        <div class="logo" @click="router.push('/')">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="#fff">
              <path d="M12 3L1 9l4 2.18v6L12 21l7-3.82v-6l2-1.09V17h2V9L12 3zm6.82 6L12 12.72 5.18 9 12 5.28 18.82 9zM17 15.99l-5 2.73-5-2.73v-3.72L12 15l5-2.73v3.72z"/>
            </svg>
          </div>
          <span class="logo-text">高校学科竞赛报名管理系统</span>
        </div>

        <!-- 搜索框 -->
        <div class="search-wrap">
          <el-icon class="search-icon"><Search /></el-icon>
          <input
            v-model="searchKeyword"
            type="text"
            class="search-input"
            placeholder="搜索竞赛名称、关键词"
            @keyup.enter="onSearch"
          />
        </div>

        <!-- 操作区 -->
        <div class="actions">
          <template v-if="userStore.isLoggedIn">
            <NotificationBell />
            <el-dropdown>
              <div class="user-trigger">
                <div class="avatar">
                  <img v-if="userStore.user?.avatar" :src="userStore.user.avatar" class="avatar-img" />
                  <span v-else>{{ userStore.user?.realName?.[0] || userStore.user?.username?.[0] || 'U' }}</span>
                </div>
                <span class="user-name">{{ userStore.user?.realName || userStore.user?.username }}</span>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-if="userStore.isStudent" @click="router.push('/student/profile')">
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isTeacher" @click="router.push('/teacher/audit')">
                    审核台
                  </el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isAdmin" @click="router.push('/admin/competitions')">
                    后台管理
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <button class="btn-ghost" @click="router.push('/login')">登录</button>
            <button class="btn-solid" @click="router.push('/register')">注册</button>
          </template>
        </div>
      </div>
    </header>

    <div class="body-wrap">
      <!-- 左侧导航栏 -->
      <aside class="sidebar" :class="{ collapsed }">
        <router-link
          v-for="t in sideMenus"
          :key="t.path"
          :to="t.path"
          class="side-item"
          :class="{ active: isActive(t) }"
        >
          <el-icon class="side-icon"><component :is="t.icon" /></el-icon>
          <span v-show="!collapsed" class="side-label">{{ t.label }}</span>
        </router-link>
        <button class="collapse-btn" @click="collapsed = !collapsed">
          <span>{{ collapsed ? '»' : '«' }}</span>
        </button>
      </aside>

      <!-- 内容区 -->
      <main class="main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

// ===== 顶部栏 =====
.topbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid $border-light;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.topbar-inner {
  max-width: 100%;
  padding: 0 $space-6;
  height: 64px;
  display: flex;
  align-items: center;
  gap: $space-5;
}

.logo {
  display: flex;
  align-items: center;
  gap: $space-2;
  cursor: pointer;
  flex-shrink: 0;
}

.logo-icon {
  width: 32px;
  height: 32px;
  border-radius: $radius-md;
  background: linear-gradient(135deg, $primary, $primary-hover);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(43, 108, 176, 0.25);
}

.logo-text {
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  white-space: nowrap;
}

// ===== 搜索框 =====
.search-wrap {
  flex: 1;
  max-width: 480px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: $space-2;
  height: 38px;
  padding: 0 $space-3;
  background: $bg-page;
  border: 1px solid transparent;
  border-radius: $radius-full;
  transition: all $transition-fast;

  &:focus-within {
    background: $bg-card;
    border-color: $primary;
    box-shadow: 0 0 0 3px rgba(43, 108, 176, 0.08);
  }
}

.search-icon {
  color: $text-placeholder;
  font-size: 16px;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: $font-size-base;
  color: $text-primary;

  &::placeholder {
    color: $text-placeholder;
  }
}

// ===== 操作 =====
.actions {
  display: flex;
  align-items: center;
  gap: $space-3;
  flex-shrink: 0;
}

.btn-ghost {
  height: 36px;
  padding: 0 $space-4;
  border: none;
  background: transparent;
  color: $primary;
  font-size: $font-size-base;
  border-radius: $radius-base;
  cursor: pointer;
  transition: background $transition-fast;
  &:hover {
    background: $primary-50;
  }
}

.btn-solid {
  height: 36px;
  padding: 0 $space-4;
  border: none;
  background: linear-gradient(135deg, $primary, $primary-hover);
  color: #fff;
  font-size: $font-size-base;
  border-radius: $radius-base;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(43, 108, 176, 0.2);
  transition: all $transition-base;
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, transparent 0%, rgba(255,255,255,0.12) 50%, transparent 100%);
    transform: translateX(-100%);
    transition: transform 0.5s ease;
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(43, 108, 176, 0.35);

    &::after {
      transform: translateX(100%);
    }
  }
  &:active {
    transform: translateY(0) scale(0.97);
  }
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: $space-2;
  padding: $space-1 $space-3;
  border-radius: $radius-base;
  cursor: pointer;
  transition: background $transition-fast;
  &:hover {
    background: $primary-50;
  }
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, $primary, $primary-hover);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.user-name {
  font-size: $font-size-sm;
  color: $text-primary;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

// ===== 主体 =====
.body-wrap {
  flex: 1;
  display: flex;
}

// ===== 左侧导航 =====
.sidebar {
  width: 160px;
  min-height: calc(100vh - 64px);
  background: $bg-card;
  border-right: 1px solid $border-light;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $space-4 0;
  gap: $space-2;
  flex-shrink: 0;
  transition: width 0.25s ease;
  position: sticky;
  top: 64px;
  height: calc(100vh - 64px);
  overflow: hidden;
}

.sidebar.collapsed {
  width: 56px;
}

.side-item {
  width: calc(100% - 16px);
  display: flex;
  align-items: center;
  gap: $space-2;
  padding: $space-2 $space-3;
  border-radius: $radius-md;
  color: $text-regular;
  text-decoration: none;
  font-size: $font-size-sm;
  transition: all $transition-base;
  white-space: nowrap;
  overflow: hidden;
  position: relative;

  .side-icon {
    transition: transform $transition-base, color $transition-base;
  }

  &:hover {
    background: linear-gradient(135deg, $primary-50, #e8f0fe);
    color: $primary;

    .side-icon {
      transform: translateX(3px);
    }
  }

  &.active {
    background: linear-gradient(135deg, $primary-50, #e0efff);
    color: $primary;
    font-weight: $font-weight-semibold;
    box-shadow: 0 1px 4px rgba(43, 108, 176, 0.12);

    .side-icon {
      transform: scale(1.1);
    }

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 60%;
      border-radius: 0 2px 2px 0;
      background: $primary;
    }
  }
}

.side-icon {
  font-size: 18px;
  flex-shrink: 0;
  width: 18px;
  height: 18px;
}

.side-label {
  overflow: hidden;
  text-overflow: ellipsis;
}

.collapse-btn {
  margin-top: auto;
  width: 32px;
  height: 32px;
  border: 1px solid $border-base;
  background: $bg-card;
  border-radius: $radius-base;
  cursor: pointer;
  color: $text-secondary;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all $transition-fast;
  flex-shrink: 0;

  &:hover {
    background: $primary-50;
    color: $primary;
    border-color: $primary;
  }
}

// ===== 主内容 =====
.main {
  flex: 1;
  min-width: 0;
}

// ===== 响应式 =====
@media (max-width: 768px) {
  .topbar-inner {
    padding: 0 $space-4;
    gap: $space-3;
  }
  .logo-text {
    display: none;
  }
  .search-wrap {
    max-width: none;
  }
  .user-name {
    display: none;
  }
  .sidebar {
    width: 56px;
  }
  .side-label {
    display: none;
  }
  .collapse-btn {
    display: none;
  }
}
</style>
