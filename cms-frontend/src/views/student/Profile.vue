<script setup lang="ts">
/**
 * 学生个人中心首页（StudentLayout 带侧边栏）
 * - 用户信息卡片（来自 userStore）
 * - 统计：我的报名数 / 待审核数
 * - 资料列表 + 编辑资料入口
 * - 安全设置
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Edit, Document, Clock, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getMyRegistrations, getMyTeamRegistrations } from '@/api/registration'

const router = useRouter()
const userStore = useUserStore()

// ====== 用户信息（从 store 取） ======
const userInfo = computed(() => ({
  username: userStore.user?.username || '',
  realName: userStore.user?.realName || userStore.user?.nickname || '用户',
  college: userStore.user?.college || '',
  phone: userStore.user?.phone || '',
  email: (userStore.user as any)?.email || '',
  role: userStore.user?.role || 'STUDENT',
  avatar: userStore.user?.avatar || ''
}))

// ====== 统计数据 ======
const stats = ref({ registrations: 0, pending: 0 })

// ====== 加载真实数据 ======
async function loadStats() {
  // 个人报名
  let indTotal = 0, indPending = 0
  try {
    const res: any = await getMyRegistrations({ pageNum: 1, pageSize: 1 })
    if (res) indTotal = res.total || 0
  } catch { console.error('[Profile] 操作失败') }
  try {
    const res: any = await getMyRegistrations({ pageNum: 1, pageSize: 1, status: 0 })
    if (res) indPending = res.total || 0
  } catch { console.error('[Profile] 操作失败') }
  // 团队报名
  let teamTotal = 0, teamPending = 0
  try {
    const res: any = await getMyTeamRegistrations({ pageNum: 1, pageSize: 1 })
    if (res) teamTotal = res.total || 0
  } catch { console.error('[Profile] 操作失败') }
  try {
    const res: any = await getMyTeamRegistrations({ pageNum: 1, pageSize: 1, status: 0 })
    if (res) teamPending = res.total || 0
  } catch { console.error('[Profile] 操作失败') }
  stats.value.registrations = indTotal + teamTotal
  stats.value.pending = indPending + teamPending
}

async function loadUser() {
  if (!userStore.user && userStore.token) {
    try {
      await userStore.fetchInfo()
    } catch (e) {
      // 静默
    }
  }
}

onMounted(() => {
  loadUser()
  loadStats()
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
    <!-- 用户信息卡 -->
    <div class="user-card">
      <div class="user-header">
        <div class="user-avatar">
          <img v-if="userInfo.avatar" :src="userInfo.avatar" class="avatar-img" />
          <span v-else>{{ userInfo.realName[0] || 'U' }}</span>
        </div>
        <div class="user-info">
          <h2 class="user-name">{{ userInfo.realName }}</h2>
          <p class="user-college">{{ userInfo.college || '未设置学院' }} · 学号 {{ userInfo.username }}</p>
        </div>
      </div>
    </div>

    <!-- 统计 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon icon-blue"><el-icon><Document /></el-icon></div>
        <div>
          <p class="stat-label">我的报名数</p>
          <p class="stat-value">{{ stats.registrations }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-orange"><el-icon><Clock /></el-icon></div>
        <div>
          <p class="stat-label">待审核数</p>
          <p class="stat-value">{{ stats.pending }}</p>
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
        <el-col :xs="24" :sm="8">
          <div class="info-row">
            <span class="info-key">姓名</span>
            <span class="info-value">{{ userInfo.realName }}</span>
          </div>
          <div class="info-row">
            <span class="info-key">手机号</span>
            <span class="info-value">{{ userInfo.phone || '未设置' }}</span>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="info-row">
            <span class="info-key">学号</span>
            <span class="info-value">{{ userInfo.username }}</span>
          </div>
          <div class="info-row">
            <span class="info-key">邮箱</span>
            <span class="info-value">{{ userInfo.email || '未设置' }}</span>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="info-row">
            <span class="info-key">学院</span>
            <span class="info-value">{{ userInfo.college || '未设置' }}</span>
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
        <span class="action-icon"><el-icon><Lock /></el-icon></span>
        <div class="action-content">
          <p class="action-name">修改密码</p>
          <p class="action-desc">定期修改密码可提升账号安全性</p>
        </div>
        <span class="action-arrow">›</span>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.profile-page {
  max-width: 960px;
  margin: 0 auto;
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
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
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
  .stats-row {
    grid-template-columns: 1fr;
  }
}
</style>
