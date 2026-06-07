<script setup lang="ts">
/**
 * 学生个人中心首页
 * - 用户信息卡片
 * - Tab：个人资料 / 报名记录 / 我的团队
 * - 统计：我的报名数 / 组建的团队
 * - 资料列表 + 编辑资料入口
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Edit } from '@element-plus/icons-vue'
import { getUserInfo } from '@/api/auth'

const router = useRouter()

const activeTab = ref('profile')

// ====== 用户信息 ======
const userInfo = ref({
  username: '2024001234',
  realName: '张明远',
  college: '计算机科学与技术学院',
  phone: '138****7899',
  email: 'zhangmingyuan@example.com',
  role: 'STUDENT',
  avatar: ''
})

// ====== 统计数据 ======
const stats = ref({
  registrations: 12,
  teams: 3
})

// ====== 加载用户信息 ======
async function loadUser() {
  try {
    const data: any = await getUserInfo()
    if (data) {
      userInfo.value = {
        ...userInfo.value,
        ...data,
        phone: data.phone || '138****7899'
      }
    }
  } catch (e) {
    // 静默使用占位
  }
}

onMounted(() => {
  loadUser()
})

function goEdit() {
  router.push('/student/profile/edit')
}

function goChangePwd() {
  router.push('/student/profile/password')
}
</script>

<template>
  <div class="profile-page">
    <!-- 顶部返回栏 -->
    <div class="page-header">
      <button class="back-btn" @click="router.push('/')">
        <span>←</span>
      </button>
      <h1 class="page-title">校园活动管理平台</h1>
      <div class="user-mini">
        <div class="avatar">{{ userInfo.realName[0] }}</div>
        <span class="user-name">{{ userInfo.realName }}</span>
      </div>
    </div>

    <!-- 用户信息卡 -->
    <div class="user-card">
      <div class="user-header">
        <div class="user-avatar">
          {{ userInfo.realName[0] }}
        </div>
        <div class="user-info">
          <h2 class="user-name">{{ userInfo.realName }}</h2>
          <p class="user-college">🏛 {{ userInfo.college }} · 学号 {{ userInfo.username }}</p>
        </div>
      </div>

      <!-- Tab 栏 -->
      <div class="tab-bar">
        <div
          class="tab-item"
          :class="{ active: activeTab === 'profile' }"
          @click="activeTab = 'profile'"
        >
          <span class="tab-icon">👤</span>个人资料
        </div>
        <div
          class="tab-item"
          :class="{ active: activeTab === 'registrations' }"
          @click="activeTab = 'registrations'"
        >
          <span class="tab-icon">📋</span>报名记录
        </div>
        <div
          class="tab-item"
          :class="{ active: activeTab === 'teams' }"
          @click="activeTab = 'teams'"
        >
          <span class="tab-icon">👥</span>我的团队
        </div>
      </div>
    </div>

    <!-- 个人资料 Tab -->
    <template v-if="activeTab === 'profile'">
      <!-- 统计 -->
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-icon icon-blue">📋</div>
          <div>
            <p class="stat-label">我的报名数</p>
            <p class="stat-value">{{ stats.registrations }}</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon icon-orange">👥</div>
          <div>
            <p class="stat-label">组建的团队</p>
            <p class="stat-value">{{ stats.teams }}</p>
          </div>
        </div>
      </div>

      <!-- 资料卡 -->
      <div class="section">
        <div class="section-header">
          <h3 class="section-title">个人资料</h3>
          <button class="btn-edit" @click="goEdit">
            <el-icon><Edit /></el-icon>编辑资料
          </button>
        </div>
        <el-row :gutter="24">
          <el-col :xs="24" :sm="12">
            <div class="info-row">
              <span class="info-key">姓名</span>
              <span class="info-value">{{ userInfo.realName }}</span>
            </div>
            <div class="info-row">
              <span class="info-key">学院</span>
              <span class="info-value">{{ userInfo.college }}</span>
            </div>
            <div class="info-row">
              <span class="info-key">学号</span>
              <span class="info-value">{{ userInfo.username }}</span>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12">
            <div class="info-row">
              <span class="info-key">手机号</span>
              <span class="info-value">{{ userInfo.phone }}</span>
            </div>
            <div class="info-row">
              <span class="info-key">邮箱</span>
              <span class="info-value">{{ userInfo.email }}</span>
            </div>
            <div class="info-row">
              <span class="info-key">角色</span>
              <el-tag size="small" effect="plain" round>学生</el-tag>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 安全设置 -->
      <div class="section">
        <h3 class="section-title">安全设置</h3>
        <div class="action-row" @click="goChangePwd">
          <span class="action-icon">🔒</span>
          <div class="action-content">
            <p class="action-name">修改密码</p>
            <p class="action-desc">定期修改密码可提升账号安全性</p>
          </div>
          <span class="action-arrow">›</span>
        </div>
      </div>
    </template>

    <!-- 报名记录 Tab -->
    <template v-else-if="activeTab === 'registrations'">
      <div class="section">
        <el-empty description="暂无报名记录">
          <el-button type="primary" @click="router.push('/competitions')">去报名</el-button>
        </el-empty>
      </div>
    </template>

    <!-- 我的团队 Tab -->
    <template v-else>
      <div class="section">
        <el-empty description="暂无团队">
          <el-button type="primary" @click="router.push('/student-center/my-teams')">去创建</el-button>
        </el-empty>
      </div>
    </template>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.profile-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: $space-4 $space-6 $space-8;
}

// ===== 顶部返回栏 =====
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $space-2 0 $space-4;
}

.back-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: $bg-card;
  border-radius: 50%;
  cursor: pointer;
  font-size: 18px;
  color: $text-regular;
  box-shadow: $shadow-sm;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all $transition-fast;

  &:hover {
    background: $primary-50;
    color: $primary;
  }
}

.page-title {
  margin: 0;
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.user-mini {
  display: flex;
  align-items: center;
  gap: $space-2;
  padding: $space-1 $space-3;
  background: $bg-card;
  border-radius: $radius-full;
  box-shadow: $shadow-sm;
}

.user-mini .avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, $primary, $primary-hover);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
}

.user-mini .user-name {
  font-size: $font-size-sm;
  color: $text-primary;
}

// ===== 用户卡 =====
.user-card {
  background: $bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;
  padding: $space-5;
  margin-bottom: $space-4;
}

.user-header {
  display: flex;
  align-items: center;
  gap: $space-4;
  margin-bottom: $space-5;
}

.user-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, $primary, $primary-hover);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: $font-weight-semibold;
  box-shadow: 0 4px 12px rgba(43, 108, 176, 0.3);
}

.user-info {
  flex: 1;
}

.user-info .user-name {
  margin: 0 0 $space-1;
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.user-college {
  margin: 0;
  font-size: $font-size-sm;
  color: $text-secondary;
}

// ===== Tab =====
.tab-bar {
  display: flex;
  border-top: 1px solid $border-light;
  margin: 0 (-$space-5) (-$space-5);
}

.tab-item {
  flex: 1;
  padding: $space-4;
  text-align: center;
  cursor: pointer;
  color: $text-secondary;
  font-size: $font-size-sm;
  border-top: 2px solid transparent;
  transition: all $transition-fast;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $space-1;

  &:hover {
    color: $primary;
  }

  &.active {
    color: $primary;
    border-top-color: $primary;
    font-weight: $font-weight-medium;
  }
}

.tab-icon {
  font-size: 16px;
}

// ===== 统计 =====
.stats-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $space-3;
  margin-bottom: $space-4;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: $space-3;
  background: $bg-card;
  border-radius: $radius-md;
  padding: $space-4 $space-5;
  box-shadow: $shadow-sm;
  transition: all $transition-base;

  &:hover {
    transform: translateY(-2px);
    box-shadow: $shadow-md;
  }
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: $radius-base;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.icon-blue {
  background: $primary-50;
  color: $primary;
}

.icon-orange {
  background: #fff5e6;
  color: $warning;
}

.stat-label {
  margin: 0 0 2px;
  font-size: $font-size-xs;
  color: $text-secondary;
}

.stat-value {
  margin: 0;
  font-size: 24px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

// ===== 通用 Section =====
.section {
  background: $bg-card;
  border-radius: $radius-md;
  padding: $space-5;
  margin-bottom: $space-4;
  box-shadow: $shadow-sm;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $space-4;
}

.section-title {
  margin: 0;
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.btn-edit {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border: 1px solid $primary;
  background: transparent;
  color: $primary;
  border-radius: $radius-base;
  font-size: $font-size-sm;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover {
    background: $primary-50;
  }
}

// ===== 资料行 =====
.info-row {
  display: flex;
  align-items: center;
  padding: $space-3 0;
  border-bottom: 1px dashed $border-light;

  &:last-child {
    border-bottom: none;
  }
}

.info-key {
  width: 80px;
  font-size: $font-size-sm;
  color: $text-secondary;
  flex-shrink: 0;
}

.info-value {
  flex: 1;
  font-size: $font-size-base;
  color: $text-primary;
}

// ===== 安全设置 =====
.action-row {
  display: flex;
  align-items: center;
  gap: $space-3;
  padding: $space-3 0;
  cursor: pointer;
  transition: opacity $transition-fast;

  &:hover {
    opacity: 0.7;
  }
}

.action-icon {
  width: 40px;
  height: 40px;
  border-radius: $radius-base;
  background: $primary-50;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.action-content {
  flex: 1;
}

.action-name {
  margin: 0 0 2px;
  font-size: $font-size-base;
  color: $text-primary;
  font-weight: $font-weight-medium;
}

.action-desc {
  margin: 0;
  font-size: $font-size-xs;
  color: $text-secondary;
}

.action-arrow {
  font-size: 20px;
  color: $text-placeholder;
}

// ===== 响应式 =====
@media (max-width: 768px) {
  .profile-page {
    padding: $space-3 $space-4;
  }
  .stats-row {
    grid-template-columns: 1fr;
  }
  .user-mini .user-name {
    display: none;
  }
}
</style>
