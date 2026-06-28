<script setup lang="ts">
/**
 * 登录页 - 实现原型图 3 个状态
 * 状态 1: 默认态（空表单）
 * 状态 2: 校验态（红框 + 红字提示 + 顶部错误 banner）
 * 状态 3: 账号锁定态（弹窗）
 */
import { ref, reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, View, Hide, Warning } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'


const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// ====== 表单数据 ======
const form = reactive({
  username: '',
  password: ''
})

// ====== 校验状态 ======
const errors = reactive({
  username: '',
  password: ''
})
const showTopBanner = ref(false)
const topBannerText = ref('')

// ====== 密码可见性 ======
const passwordVisible = ref(false)

// ====== 加载与锁定 ======
const loading = ref(false)
const lockDialogVisible = ref(false)
const MAX_FAIL = 5
const LOCK_MINUTES = 15
let lockTimer: number | null = null
const lockCountdown = ref(0)

// ====== 校验函数 ======
function validate(): boolean {
  let ok = true
  errors.username = ''
  errors.password = ''

  if (!form.username.trim()) {
    errors.username = '账号不能为空'
    ok = false
  } else if (form.username.length < 3) {
    errors.username = '账号长度至少 3 位'
    ok = false
  }

  if (!form.password) {
    errors.password = '密码不能为空'
    ok = false
  } else if (form.password.length < 6) {
    errors.password = '密码长度至少 6 位'
    ok = false
  }

  return ok
}

// ====== 显示顶部错误 banner（带 shake 动效） ======
function showErrorBanner(text: string) {
  topBannerText.value = text
  showTopBanner.value = true
  // 触发动画
  setTimeout(() => {
    showTopBanner.value = false
  }, 4000)
}

// ====== 提交登录 ======
async function handleLogin() {
  if (!validate()) return
  if (lockDialogVisible.value) return

  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || ''
    if (redirect) {
      router.push(redirect)
    } else {
      router.push('/')
    }
  } catch (err: any) {
    const msg = err?.message || '账号或密码错误，请重新输入'
    if (msg.includes('登录失败次数过多')) {
      showLockDialog()
    } else {
      showErrorBanner(msg)
    }
  } finally {
    loading.value = false
  }
}

// ====== 锁定逻辑 ======
function showLockDialog() {
  lockDialogVisible.value = true
  lockCountdown.value = LOCK_MINUTES * 60
  if (lockTimer) clearInterval(lockTimer)
  lockTimer = window.setInterval(() => {
    lockCountdown.value--
    if (lockCountdown.value <= 0) {
      clearInterval(lockTimer!)
      lockTimer = null
      lockDialogVisible.value = false
      ElMessage.info('账号已解锁，请重新登录')
    }
  }, 1000)
}

const lockMinutes = computed(() => Math.floor(lockCountdown.value / 60))
const lockSeconds = computed(() => lockCountdown.value % 60)
const lockCountdownText = computed(
  () => `${String(lockMinutes.value).padStart(2, '0')}:${String(lockSeconds.value).padStart(2, '0')}`
)

function dismissLock() {
  lockDialogVisible.value = false
  if (lockTimer) {
    clearInterval(lockTimer)
    lockTimer = null
  }
}

function goRegister() {
  router.push('/register')
}

function goForgot() {
  ElMessage.info('请联系系统管理员重置密码')
}
</script>

<template>
  <div class="login-page">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
    </div>

    <!-- 登录卡片 -->
    <div class="login-card" :class="{ shake: showTopBanner }">
      <!-- Logo + 标题 -->
      <div class="logo-wrap">
        <div class="logo-box">
          <el-icon :size="28" color="#fff"><Lock /></el-icon>
        </div>
        <h1 class="title">高校学科竞赛报名管理系统</h1>
        <p class="subtitle">欢迎回来，请登录您的账号</p>
      </div>

      <!-- 顶部错误 banner -->
      <transition name="banner">
        <div v-if="showTopBanner" class="error-banner">
          <el-icon><Warning /></el-icon>
          <span>{{ topBannerText }}</span>
        </div>
      </transition>

      <!-- 表单 -->
      <el-form @submit.prevent="handleLogin" class="login-form">
        <div class="form-item">
          <label class="form-label">账号</label>
          <div class="input-wrap" :class="{ 'is-error': errors.username }">
            <el-icon class="input-icon"><User /></el-icon>
            <input
              v-model="form.username"
              type="text"
              class="input"
              :placeholder="errors.username || '请输入学号或邮箱'"
              :style="{ color: errors.username ? '#e53e3e' : '' }"
              autocomplete="username"
            />
          </div>
          <p v-if="errors.username" class="error-text">{{ errors.username }}</p>
        </div>

        <div class="form-item">
          <label class="form-label">密码</label>
          <div class="input-wrap" :class="{ 'is-error': errors.password }">
            <el-icon class="input-icon"><Lock /></el-icon>
            <input
              v-model="form.password"
              :type="passwordVisible ? 'text' : 'password'"
              class="input"
              :placeholder="errors.password || '请输入密码'"
              :style="{ color: errors.password ? '#e53e3e' : '' }"
              autocomplete="current-password"
            />
            <el-icon
              class="input-toggle"
              @click="passwordVisible = !passwordVisible"
            >
              <component :is="passwordVisible ? Hide : View" />
            </el-icon>
          </div>
          <p v-if="errors.password" class="error-text">{{ errors.password }}</p>
        </div>

        <!-- 登录按钮 -->
        <button
          type="submit"
          class="btn-primary"
          :disabled="loading || lockDialogVisible"
        >
          {{ loading ? '登录中...' : '登 录' }}
        </button>

        <!-- 链接行 -->
        <div class="link-row">
          <a class="link" @click.prevent="goRegister">立即注册</a>
          <a class="link" @click.prevent="goForgot">忘记密码</a>
        </div>
      </el-form>
    </div>

    <!-- 账号锁定弹窗 -->
    <el-dialog
      v-model="lockDialogVisible"
      width="420px"
      :show-close="false"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      align-center
      class="lock-dialog"
    >
      <div class="lock-content">
        <div class="lock-icon-wrap">
          <el-icon :size="32" color="#f6ad55"><Lock /></el-icon>
        </div>
        <h2 class="lock-title">账号已锁定</h2>
        <p class="lock-desc">您已连续 {{ MAX_FAIL }} 次输入错误密码</p>
        <p class="lock-tips">
          账号已锁定 {{ LOCK_MINUTES }} 分钟（剩余
          <span class="countdown">{{ lockCountdownText }}</span>
          ），请稍后再试
        </p>
        <div class="lock-actions">
          <button class="btn-secondary" @click="dismissLock">取消</button>
          <button class="btn-primary" @click="dismissLock">我知道了</button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.login-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #eaf2fb 0%, #f5f7fa 50%, #fef5e7 100%);
  overflow: hidden;
  padding: $space-6;
}

// ===== 背景装饰 =====
.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
}
.blob-1 {
  width: 480px;
  height: 480px;
  background: radial-gradient(circle, rgba(43, 108, 176, 0.25), transparent);
  top: -120px;
  left: -120px;
  animation: blobFloat1 8s ease-in-out infinite alternate;
}
.blob-2 {
  width: 380px;
  height: 380px;
  background: radial-gradient(circle, rgba(246, 173, 85, 0.2), transparent);
  bottom: -80px;
  right: -80px;
  animation: blobFloat2 10s ease-in-out infinite alternate;
}
.blob-3 {
  width: 280px;
  height: 280px;
  background: radial-gradient(circle, rgba(72, 187, 120, 0.18), transparent);
  top: 35%;
  right: 15%;
  animation: blobFloat3 7s ease-in-out infinite alternate;
}

@keyframes blobFloat1 {
  from { transform: translate(0, 0) scale(1); }
  to { transform: translate(20px, 20px) scale(1.05); }
}
@keyframes blobFloat2 {
  from { transform: translate(0, 0) scale(1); }
  to { transform: translate(-15px, -15px) scale(1.08); }
}
@keyframes blobFloat3 {
  from { transform: translate(0, 0) scale(1); }
  to { transform: translate(-10px, 10px) scale(1.06); }
}

// ===== 登录卡片 =====
.login-card {
  position: relative;
  width: 600px;
  max-width: 100%;
  background: $bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-card;
  padding: $space-12 $space-16;
  z-index: 1;
  animation: cardEnter 0.5s $transition-base both;
}

@keyframes cardEnter {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

// ===== Logo + 标题 =====
.logo-wrap {
  text-align: center;
  margin-bottom: $space-8;
}

.logo-box {
  width: 56px;
  height: 56px;
  margin: 0 auto $space-4;
  border-radius: $radius-md;
  background: linear-gradient(135deg, $primary, $primary-hover);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 16px rgba(43, 108, 176, 0.3);
}

.title {
  margin: 0 0 $space-2;
  font-size: $font-size-2xl;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  letter-spacing: 0.5px;
}

.subtitle {
  margin: 0;
  font-size: $font-size-sm;
  color: $text-secondary;
}

// ===== 错误 banner =====
.error-banner {
  display: flex;
  align-items: center;
  gap: $space-2;
  padding: $space-3 $space-4;
  margin-bottom: $space-5;
  background: #fef0f0;
  border: 1px solid #fbcaca;
  border-radius: $radius-base;
  color: $danger;
  font-size: $font-size-sm;
}

.banner-enter-active,
.banner-leave-active {
  transition: all $transition-base;
}
.banner-enter-from,
.banner-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

// ===== 表单 =====
.login-form {
  display: flex;
  flex-direction: column;
  gap: $space-5;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: $space-2;
}

.form-label {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $text-regular;
}

.input-wrap {
  display: flex;
  align-items: center;
  gap: $space-2;
  height: 48px;
  padding: 0 $space-4;
  background: $primary-50;
  border: 1.5px solid transparent;
  border-radius: $radius-base;
  transition: all $transition-fast;

  &:focus-within {
    background: $bg-card;
    border-color: $primary;
    box-shadow: 0 0 0 3px rgba(43, 108, 176, 0.1);
  }

  &.is-error {
    background: #fef5f5;
    border-color: $danger;

    &:focus-within {
      box-shadow: 0 0 0 3px rgba(229, 62, 62, 0.1);
    }
  }
}

.input-icon {
  color: $text-placeholder;
  font-size: 18px;
}

.input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: $font-size-base;
  color: $text-primary;
  height: 100%;

  &::placeholder {
    color: $text-placeholder;
  }
}

.input-toggle {
  cursor: pointer;
  color: $text-placeholder;
  font-size: 18px;
  transition: color $transition-fast;
  &:hover {
    color: $primary;
  }
}

.error-text {
  margin: 0;
  font-size: $font-size-xs;
  color: $danger;
}

// ===== 登录按钮 =====
.btn-primary {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: $radius-base;
  background: linear-gradient(135deg, $primary, $primary-hover);
  color: #fff;
  font-size: $font-size-lg;
  font-weight: $font-weight-medium;
  letter-spacing: 4px;
  cursor: pointer;
  transition: all $transition-base;
  box-shadow: 0 4px 12px rgba(43, 108, 176, 0.25);
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, transparent 0%, rgba(255,255,255,0.15) 50%, transparent 100%);
    transform: translateX(-100%);
    transition: transform 0.6s ease;
  }

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(43, 108, 176, 0.4);

    &::after {
      transform: translateX(100%);
    }
  }
  &:active:not(:disabled) {
    transform: scale(0.98);
  }
  &:disabled {
    background: $bg-disabled;
    color: $text-disabled;
    cursor: not-allowed;
    box-shadow: none;
  }
}

.btn-secondary {
  height: 40px;
  padding: 0 $space-5;
  border: 1.5px solid $border-base;
  border-radius: $radius-base;
  background: $bg-card;
  color: $text-regular;
  font-size: $font-size-base;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover {
    border-color: $primary;
    color: $primary;
  }
}

// ===== 链接 =====
.link-row {
  display: flex;
  justify-content: space-between;
  margin-top: -$space-2;
}

.link {
  font-size: $font-size-sm;
  color: $primary;
  cursor: pointer;
  transition: opacity $transition-fast;
  &:hover {
    opacity: 0.7;
  }
}

// ===== 锁定弹窗 =====
.lock-content {
  text-align: center;
  padding: $space-4 0;
}

.lock-icon-wrap {
  width: 72px;
  height: 72px;
  margin: 0 auto $space-5;
  border-radius: 50%;
  background: #fef5e7;
  display: flex;
  align-items: center;
  justify-content: center;
}

.lock-title {
  margin: 0 0 $space-3;
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.lock-desc {
  margin: 0 0 $space-2;
  font-size: $font-size-base;
  color: $text-regular;
}

.lock-tips {
  margin: 0 0 $space-6;
  font-size: $font-size-sm;
  color: $text-secondary;

  .countdown {
    color: $danger;
    font-weight: $font-weight-semibold;
    font-family: 'Consolas', monospace;
  }
}

.lock-actions {
  display: flex;
  gap: $space-3;
  justify-content: center;

  .btn-primary {
    width: auto;
    padding: 0 $space-8;
    letter-spacing: 0;
  }
}

:deep(.lock-dialog) {
  border-radius: $radius-md;
}
:deep(.el-dialog__header) {
  display: none;
}
:deep(.el-dialog__body) {
  padding: $space-8 $space-6 $space-6;
}
</style>
