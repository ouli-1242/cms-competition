<script setup lang="ts">
/**
 * 创建团队页
 * - 选择竞赛
 * - 输入团队名称
 * - 创建后展示邀请码
 */
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getCompetitionDetail } from '@/api/public'
import { createTeam } from '@/api/team'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const competition = ref<any>(null)
const teamName = ref('')
const error = ref('')

// 创建成功的弹窗
const successVisible = ref(false)
const createdTeam = ref<any>(null)

async function loadCompetition() {
  loading.value = true
  try {
    const data = await getCompetitionDetail(Number(route.query.competitionId))
    competition.value = data
  } catch (e) {
    competition.value = {
      id: Number(route.query.competitionId),
      title: '高校大学生创新创业挑战赛',
      type: '团体赛',
      category: 'A类',
      minMembers: 3,
      maxMembers: 5
    }
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  if (!userStore.isLoggedIn) {
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  error.value = ''
  if (!teamName.value.trim()) {
    error.value = '请输入团队名称'
    return
  }
  if (teamName.value.length < 2 || teamName.value.length > 20) {
    error.value = '团队名称长度应为 2-20 个字'
    return
  }
  submitting.value = true
  try {
    const res: any = await createTeam({
      competitionId: competition.value.id,
      teamName: teamName.value
    })
    createdTeam.value = res || {
      teamName: teamName.value,
      inviteCode: 'A3B7K9',
      expireDays: 5
    }
    successVisible.value = true
  } catch (e: any) {
    if (e?.message?.includes('已存在') || e?.code === 'TEAM_EXISTS') {
      error.value = '团队名重复或格式错误，请重新创建'
    } else {
      error.value = '创建失败，请稍后重试'
    }
  } finally {
    submitting.value = false
  }
}

function onSuccessClose() {
  successVisible.value = false
  router.push('/student-center/team-manage')
}

function copyInviteCode() {
  if (createdTeam.value?.inviteCode) {
    navigator.clipboard?.writeText(createdTeam.value.inviteCode)
  }
}

onMounted(() => {
  loadCompetition()
})
</script>

<template>
  <div class="create-team-page">
    <!-- 顶部返回栏 -->
    <div class="page-header">
      <button class="back-btn" @click="router.back()">
        <span>←</span>
      </button>
      <h1 class="page-title">高校学科竞赛报名管理系统</h1>
      <div class="user-mini" v-if="userStore.isLoggedIn">
        <div class="avatar">{{ userStore.realName?.[0] || 'U' }}</div>
        <span class="user-name">{{ userStore.realName || '用户' }}</span>
      </div>
      <div v-else class="placeholder"></div>
    </div>

    <!-- 面包屑 -->
    <div class="breadcrumb">
      <span @click="router.push('/student/profile')">个人中心</span>
      <span class="sep">›</span>
      <span @click="router.push('/student-center/my-teams')">我的团队</span>
      <span class="sep">›</span>
      <span class="current">创建团队</span>
    </div>

    <!-- 主卡片 -->
    <div class="create-card">
      <h2 class="card-title">创建团队</h2>

      <div v-if="loading" class="loading">
        <el-skeleton :rows="3" animated />
      </div>

      <template v-else>
        <!-- 竞赛信息 -->
        <div class="form-section">
          <h3 class="section-title">竞赛信息</h3>
          <div class="form-item">
            <label class="form-label">选择竞赛</label>
            <div class="form-input readonly">
              <span class="value">✅ {{ competition?.title || '高校大学生创新创业挑战赛' }}</span>
            </div>
            <p class="form-tip">{{ competition?.type || '团体赛' }} · {{ competition?.category || 'A类' }}</p>
          </div>
        </div>

        <!-- 团队信息 -->
        <div class="form-section">
          <h3 class="section-title">团队信息</h3>
          <div class="form-item">
            <label class="form-label">
              <span class="required">*</span>团队名称
            </label>
            <input
              v-model="teamName"
              type="text"
              class="form-input"
              :class="{ 'is-error': !!error }"
              placeholder="请输入团队名称（2-20字）"
              maxlength="20"
            />
            <p v-if="error" class="error-text">
              <span class="error-icon">⚠</span>{{ error }}
            </p>
            <p v-else class="form-tip">创建后团队名称不可修改，请谨慎填写</p>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="form-actions">
          <button class="btn-cancel" @click="router.back()">取消</button>
          <button class="btn-primary" :disabled="submitting" @click="handleCreate">
            {{ submitting ? '创建中...' : '确认创建' }}
          </button>
        </div>
      </template>
    </div>

    <!-- 创建成功弹窗 -->
    <el-dialog
      v-model="successVisible"
      width="420px"
      :show-close="false"
      align-center
    >
      <template #header>
        <div class="dialog-header center">
          <h3>创建成功</h3>
        </div>
      </template>
      <div class="dialog-body">
        <div class="success-info">
          <div class="info-row">
            <span class="info-label">团队名称：</span>
            <span class="info-value">{{ createdTeam?.teamName }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">邀请码：</span>
            <span class="info-value code-value" @click="copyInviteCode">
              {{ createdTeam?.inviteCode }}
            </span>
          </div>
          <p class="tip">📌 6 位邀请码有效期 {{ createdTeam?.expireDays || 5 }} 天</p>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer center">
          <button class="btn-ok primary" @click="onSuccessClose">知道了</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.create-team-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: $space-4 $space-6 $space-8;
}

// ===== 顶部栏 =====
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $space-2 0 $space-3;
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

.placeholder {
  width: 36px;
  height: 36px;
}

// ===== 面包屑 =====
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 $space-4;
  font-size: $font-size-sm;
  color: $text-secondary;

  span {
    cursor: pointer;
    transition: color $transition-fast;

    &:hover {
      color: $primary;
    }

    &.current {
      color: $primary;
      cursor: default;
    }

    &.sep {
      color: $text-placeholder;
      cursor: default;
    }
  }
}

// ===== 卡片 =====
.create-card {
  max-width: 720px;
  margin: 0 auto;
  background: $bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;
  padding: $space-6;
}

.card-title {
  margin: 0 0 $space-5;
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  padding-bottom: $space-3;
  border-bottom: 1px solid $border-light;
}

.loading {
  padding: $space-4 0;
}

.form-section {
  margin-bottom: $space-5;

  &:last-of-type {
    margin-bottom: $space-4;
  }
}

.section-title {
  margin: 0 0 $space-3;
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: $space-1;
}

.form-label {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $text-regular;
}

.required {
  color: $danger;
  margin-right: 2px;
}

.form-input {
  height: 42px;
  padding: 0 $space-3;
  border: 1.5px solid $border-base;
  background: $bg-card;
  border-radius: $radius-base;
  font-size: $font-size-base;
  color: $text-primary;
  outline: none;
  transition: all $transition-fast;
  font-family: inherit;

  &:focus {
    border-color: $primary;
    box-shadow: 0 0 0 3px rgba(43, 108, 176, 0.08);
  }

  &.is-error {
    border-color: $danger;
    background: #fef5f5;
  }

  &.readonly {
    background: $bg-page;
    cursor: default;
    display: flex;
    align-items: center;
    color: $text-regular;
  }
}

.value {
  display: block;
  font-size: $font-size-base;
  color: $text-primary;
}

.form-tip {
  margin: 4px 0 0;
  font-size: $font-size-xs;
  color: $text-secondary;
}

.error-text {
  display: flex;
  align-items: center;
  gap: 4px;
  margin: 4px 0 0;
  font-size: $font-size-xs;
  color: $danger;
}

.error-icon {
  font-size: 12px;
}

// ===== 操作按钮 =====
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: $space-3;
  margin-top: $space-5;
  padding-top: $space-5;
  border-top: 1px solid $border-light;
}

.btn-cancel {
  height: 40px;
  padding: 0 $space-5;
  border: 1px solid $border-base;
  background: $bg-card;
  color: $text-regular;
  font-size: $font-size-base;
  border-radius: $radius-base;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover {
    background: $bg-page;
    border-color: $primary;
    color: $primary;
  }
}

.btn-primary {
  height: 40px;
  padding: 0 $space-5;
  border: none;
  background: linear-gradient(135deg, $primary, $primary-hover);
  color: #fff;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  border-radius: $radius-base;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(43, 108, 176, 0.25);
  transition: all $transition-fast;

  &:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 6px 16px rgba(43, 108, 176, 0.35);
  }
  &:disabled {
    background: $bg-disabled;
    color: $text-disabled;
    cursor: not-allowed;
    box-shadow: none;
  }
}

// ===== 弹窗 =====
:deep(.el-dialog__header) {
  padding: $space-5 $space-6 0;
  margin-right: 0;
}

.dialog-header {
  &.center {
    text-align: center;
  }
  h3 {
    margin: 0;
    font-size: $font-size-md;
    font-weight: $font-weight-semibold;
    color: $text-primary;
  }
}

.dialog-body {
  padding: $space-3 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: $space-3;
  padding-top: $space-3;

  &.center {
    justify-content: center;
  }
}

.success-info {
  background: $bg-page;
  border-radius: $radius-base;
  padding: $space-4 $space-5;
}

.info-row {
  display: flex;
  padding: $space-1 0;
  font-size: $font-size-sm;

  &:first-child {
    margin-bottom: $space-2;
  }
}

.info-label {
  color: $text-secondary;
  white-space: nowrap;
  flex-shrink: 0;
}

.info-value {
  color: $text-primary;
  flex: 1;
  font-weight: $font-weight-medium;
}

.code-value {
  font-family: 'Consolas', monospace;
  font-size: $font-size-md;
  color: $primary;
  cursor: pointer;
  letter-spacing: 1px;
  user-select: all;

  &:hover {
    text-decoration: underline;
  }
}

.tip {
  margin: $space-3 0 0;
  font-size: $font-size-xs;
  color: $text-secondary;
  text-align: center;
}

.btn-ok {
  height: 40px;
  padding: 0 $space-6;
  border: 1px solid $border-base;
  background: $bg-card;
  color: $text-regular;
  font-size: $font-size-base;
  border-radius: $radius-base;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover {
    background: $bg-page;
    border-color: $primary;
    color: $primary;
  }

  &.primary {
    min-width: 120px;
    background: linear-gradient(135deg, $primary, $primary-hover);
    color: #fff;
    border: none;
    box-shadow: 0 4px 12px rgba(43, 108, 176, 0.25);

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 6px 16px rgba(43, 108, 176, 0.35);
      color: #fff;
    }
  }
}

// ===== 响应式 =====
@media (max-width: 768px) {
  .create-team-page {
    padding: $space-3 $space-4;
  }
  .user-mini .user-name {
    display: none;
  }
}
</style>
