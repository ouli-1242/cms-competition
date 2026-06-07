<script setup lang="ts">
/**
 * 公共布局 - 顶部 Logo + 搜索 + 登录/注册入口 + 内容区 + 底部 Tab
 */
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

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

// 底部 Tab
const bottomTabs = [
  { path: '/', label: '首页', icon: '🏠' },
  { path: '/competitions', label: '竞赛库', icon: '🏆' },
  { path: userStore.isLoggedIn ? (userStore.isStudent ? '/student/profile' : userStore.isTeacher ? '/teacher/audit' : '/admin/competitions') : '/login', label: '个人中心', icon: '👤' }
]
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
            <el-dropdown>
              <div class="user-trigger">
                <div class="avatar">{{ userStore.user?.realName?.[0] || userStore.user?.username?.[0] || 'U' }}</div>
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

    <!-- 内容区 -->
    <main class="main">
      <router-view />
    </main>

    <!-- 底部 Tab 栏 -->
    <footer class="bottombar">
      <router-link
        v-for="t in bottomTabs"
        :key="t.path"
        :to="t.path"
        class="tab-item"
        :class="{ active: route.path === t.path || (t.path === '/competitions' && route.path.startsWith('/competitions')) }"
      >
        <span class="tab-icon">{{ t.icon }}</span>
        <span class="tab-label">{{ t.label }}</span>
      </router-link>
    </footer>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding-bottom: 64px; // 为底部 Tab 留位置
}

// ===== 顶部栏 =====
.topbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: $bg-card;
  border-bottom: 1px solid $border-light;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.topbar-inner {
  max-width: 1280px;
  margin: 0 auto;
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
  transition: all $transition-fast;
  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 10px rgba(43, 108, 176, 0.3);
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
}

.user-name {
  font-size: $font-size-sm;
  color: $text-primary;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

// ===== 主内容 =====
.main {
  flex: 1;
}

// ===== 底部 Tab =====
.bottombar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  height: 60px;
  background: $bg-card;
  border-top: 1px solid $border-light;
  display: flex;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.04);
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  color: $text-secondary;
  text-decoration: none;
  transition: color $transition-fast;
  font-size: $font-size-xs;

  &.active {
    color: $primary;
  }
}

.tab-icon {
  font-size: 22px;
}

.tab-label {
  font-size: $font-size-xs;
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
}
</style>
