<script setup lang="ts">
/**
 * 加入团队页
 * - 输入 6 位邀请码
 * - 提交申请，等待队长审核
 */
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { joinTeam } from '@/api/team'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const inviteCode = ref('')
const submitting = ref(false)
const error = ref('')

const successVisible = ref(false)

async function handleJoin() {
  if (!userStore.isLoggedIn) {
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  error.value = ''
  if (!inviteCode.value.trim()) {
    error.value = '请输入邀请码'
    return
  }
  if (!/^[A-Z0-9]{6}$/.test(inviteCode.value.trim().toUpperCase())) {
    error.value = '邀请码格式不正确（6 位字母数字）'
    return
  }
  submitting.value = true
  try {
    await joinTeam(inviteCode.value.trim().toUpperCase())
    successVisible.value = true
  } catch (e: any) {
    if (e?.message?.includes('不存在') || e?.code === 'INVITE_NOT_FOUND') {
      error.value = '邀请码无效，请检查后重新输入'
    } else if (e?.message?.includes('过期')) {
      error.value = '邀请码已过期'
    } else if (e?.message?.includes('已加入') || e?.code === 'ALREADY_JOINED') {
      error.value = '您已加入该团队'
    } else {
      error.value = e?.message || '加入失败，请稍后重试'
    }
  } finally {
    submitting.value = false
  }
}

function onSuccessClose() {
  successVisible.value = false
  router.push('/student-center/my-teams')
}

// 自动转大写 + 限制 6 位
function onInput() {
  inviteCode.value = inviteCode.value.toUpperCase().replace(/[^A-Z0-9]/g, '').slice(0, 6)
}
</script>

<template>
  <div class="join-team-page">
    <!-- 顶部 -->
    <div class="page-header">
      <button class="back-btn" @click="router.back()">
        <span>←</span>
      </button>
      <h1 class="page-title">高校学科竞赛报名管理系统</h1>
      <div class="user-mini" v-if="userStore.isLoggedIn">
        <div class="avatar">{{ userStore.user?.realName?.[0] || 'U' }}</div>
        <span class="user-name">{{ userStore.user?.realName || '用户' }}</span>
      </div>
      <div v-else class="placeholder"></div>
    </div>

    <!-- 面包屑 -->
    <div class="breadcrumb">
      <span @click="router.push('/student/profile')">个人中心</span>
      <span class="sep">›</span>
      <span @click="router.push('/student-center/my-teams')">我的团队</span>
      <span class="sep">›</span>
      <span class="current">加入团队</span>
    </div>

    <!-- 卡片 -->
    <div class="join-card">
      <h2 class="card-title">加入团队</h2>

      <div class="form-section">
        <h3 class="section-title">团队邀请码</h3>
          <div class="form-item">
            <label class="form-label">
              <span class="required">*</span>邀请码
            </label>
            <input
              v-model="inviteCode"
              type="text"
              class="form-input code-input"
              :class="{ 'is-error': !!error, 'is-success': !error && inviteCode.length === 6 }"
              placeholder="请输入6位邀请码"
              maxlength="6"
              @input="onInput"
            />
            <p v-if="error" class="error-text">
              <span class="error-icon">⚠</span>{{ error }}
            </p>
            <p v-else-if="inviteCode.length === 6" class="form-tip success-tip">✓ 邀请码格式正确</p>
            <p v-else class="form-tip">向队长获取团队 6 位邀请码（仅含大写字母与数字）</p>
          </div>
        </div>

        <div class="form-actions">
          <button class="btn-cancel" @click="router.back()">取消</button>
          <button class="btn-primary" :disabled="submitting" @click="handleJoin">
            {{ submitting ? '提交中...' : '申请加入' }}
          </button>
        </div>
    </div>

    <!-- 申请已提交弹窗 -->
    <el-dialog
      v-model="successVisible"
      width="420px"
      :show-close="false"
      align-center
    >
      <template #header>
        <div class="dialog-header center">
          <h3>申请已提交</h3>
        </div>
      </template>
      <div class="dialog-body">
        <p class="dialog-text">提交后，队长需通过您的申请。<br/>您可以在「我的团队」中查看进度。</p>
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

.join-team-page {
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
  transition: all $transition-base;

  &:hover {
    background: linear-gradient(135deg, $primary-50, $primary);
    color: #fff;
    transform: scale(1.08);
    box-shadow: 0 2px 8px rgba(43, 108, 176, 0.25);
  }

  &:active {
    transform: scale(0.95);
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
  transition: all $transition-base;

  &:hover {
    box-shadow: $shadow-md;
    transform: translateY(-1px);
  }
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
.join-card {
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
  &.is-success {
    border-color: $success;
    background: #f0faf4;
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

.code-input {
  font-family: 'Consolas', monospace;
  font-size: 20px;
  letter-spacing: 4px;
  text-align: center;
  text-transform: uppercase;
}

.form-tip {
  margin: 4px 0 0;
  font-size: $font-size-xs;
  color: $text-secondary;

  &.success-tip {
    color: $success;
  }
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
  text-align: center;
}

.dialog-text {
  margin: 0;
  font-size: $font-size-sm;
  color: $text-regular;
  line-height: 1.8;
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
  .join-team-page {
    padding: $space-3 $space-4;
  }
  .user-mini .user-name {
    display: none;
  }
}
</style>
