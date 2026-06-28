<script setup lang="ts">
/**
 * 注册页 - 实现原型图 3 个状态
 * 状态 1: 默认态（空表单，2 列布局）
 * 状态 2: 校验态（红框 + 红字提示 + 绿色对号）
 * 状态 3: 注册成功态（绿色对号弹窗）
 */
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
// Element Plus 静态 import 以确保 tree-shaking 与类型
import {
  User,
  Lock,
  View,
  Hide,
  Phone,
  Message,
  Check,
  ArrowDown
} from '@element-plus/icons-vue'
import { register } from '@/api/auth'


const router = useRouter()

// ====== 表单数据 ======
const form = reactive({
  username: '',
  realName: '',
  college: '',
  phone: '',
  email: '',
  password: '',
  confirmPassword: ''
})

// ====== 校验状态 ======
const errors = reactive({
  username: '',
  realName: '',
  college: '',
  phone: '',
  email: '',
  password: '',
  confirmPassword: ''
})

// 已校验通过标记（绿色对号）
const validated = reactive({
  username: false,
  realName: false,
  college: false,
  phone: false,
  email: false,
  password: false,
  confirmPassword: false
})

// 学院下拉选项
const colleges = [
  '计算机科学与技术学院',
  '软件学院',
  '信息与通信工程学院',
  '电子工程学院',
  '自动化学院',
  '经济管理学院',
  '外国语学院',
  '数学与统计学院',
  '物理学院',
  '机械与动力工程学院'
]

// ====== UI 状态 ======
const loading = ref(false)
const successDialogVisible = ref(false)
const passwordVisible = ref(false)
const confirmPasswordVisible = ref(false)
const submitting = ref(false)

// ====== 校验函数 ======
const phoneReg = /^1[3-9]\d{9}$/
const emailReg = /^[\w.+-]+@[\w-]+\.[\w.-]+$/

async function validateField(field: keyof typeof form): Promise<boolean> {
  errors[field] = ''
  validated[field] = false
  const v = (form[field] || '').toString().trim()

  switch (field) {
    case 'username':
      if (!v) {
        errors.username = '学号不能为空'
        return false
      }
      if (!/^\d{6,12}$/.test(v)) {
        errors.username = '请输入 6-12 位数字学号'
        return false
      }
      break
    case 'realName':
      if (!v) {
        errors.realName = '姓名不能为空'
        return false
      }
      if (v.length < 2) {
        errors.realName = '姓名至少 2 个字'
        return false
      }
      break
    case 'college':
      if (!v) {
        errors.college = '请选择学院'
        return false
      }
      break
    case 'phone':
      if (!v) {
        errors.phone = '手机号不能为空'
        return false
      }
      if (!phoneReg.test(v)) {
        errors.phone = '请输入正确的手机号'
        return false
      }
      break
    case 'email':
      if (!v) {
        errors.email = '邮箱不能为空'
        return false
      }
      if (!emailReg.test(v)) {
        errors.email = '请输入正确的邮箱'
        return false
      }
      break
    case 'password':
      if (!v) {
        errors.password = '密码不能为空'
        return false
      }
      if (v.length < 6 || v.length > 20) {
        errors.password = '密码长度为 6-20 位'
        return false
      }
      break
    case 'confirmPassword':
      if (!v) {
        errors.confirmPassword = '请再次输入密码'
        return false
      }
      if (v !== form.password) {
        errors.confirmPassword = '两次输入的密码不匹配'
        return false
      }
      break
  }
  validated[field] = true
  return true
}

async function validateAll(): Promise<boolean> {
  const fields: (keyof typeof form)[] = [
    'username', 'realName', 'college', 'phone', 'email', 'password', 'confirmPassword'
  ]
  const results = await Promise.all(fields.map((f) => validateField(f)))
  return results.every(Boolean)
}

// ====== 提交注册 ======
async function handleRegister() {
  if (submitting.value) return
  submitting.value = true
  try {
    const ok = await validateAll()
    if (!ok) {
      submitting.value = false
      return
    }
    loading.value = true
    await register({
      username: form.username,
      password: form.password,
      realName: form.realName,
      college: form.college,
      phone: form.phone,
      email: form.email
    })
    successDialogVisible.value = true
    // 3 秒后自动跳转
    setTimeout(() => {
      goLogin()
    }, 3000)
  } catch (err: any) {
    // 错误已由 request 拦截器弹窗
  } finally {
    loading.value = false
    submitting.value = false
  }
}

function goLogin() {
  successDialogVisible.value = false
  router.push('/login')
}

// 失焦校验
function onBlur(field: keyof typeof form) {
  if (form[field]) validateField(field)
}

// 输入框 class（错误红/成功绿）
function inputClass(field: keyof typeof form) {
  return {
    'is-error': !!errors[field],
    'is-success': validated[field] && !errors[field]
  }
}

// 错误时输入文字变红
function inputStyle(field: keyof typeof form) {
  if (errors[field]) return { color: '#e53e3e' }
  return {}
}
</script>

<template>
  <div class="register-page">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
    </div>

    <!-- 注册卡片 -->
    <div class="register-card">
      <!-- Logo + 标题 -->
      <div class="logo-wrap">
        <div class="logo-box">
          <!-- 蓝色书本/学士帽图标 -->
          <svg viewBox="0 0 24 24" width="28" height="28" fill="#fff">
            <path d="M12 3L1 9l4 2.18v6L12 21l7-3.82v-6l2-1.09V17h2V9L12 3zm6.82 6L12 12.72 5.18 9 12 5.28 18.82 9zM17 15.99l-5 2.73-5-2.73v-3.72L12 15l5-2.73v3.72z"/>
          </svg>
        </div>
        <h1 class="title">创建新账号</h1>
        <p class="subtitle">填写以下信息完成注册</p>
      </div>

      <!-- 表单 -->
      <form @submit.prevent="handleRegister" class="register-form" novalidate>
        <!-- 学号 + 姓名（双列） -->
        <div class="form-row two-col">
          <div class="form-item">
            <label class="form-label">
              <span class="required">*</span>学号
            </label>
            <div class="input-wrap" :class="inputClass('username')">
              <input
                v-model="form.username"
                type="text"
                class="input"
                :placeholder="errors.username || '请输入学号'"
                :style="inputStyle('username')"
                maxlength="12"
                @blur="onBlur('username')"
              />
              <el-icon v-if="validated.username" class="check-icon"><Check /></el-icon>
            </div>
            <p v-if="errors.username" class="error-text">{{ errors.username }}</p>
          </div>

          <div class="form-item">
            <label class="form-label">
              <span class="required">*</span>姓名
            </label>
            <div class="input-wrap" :class="inputClass('realName')">
              <input
                v-model="form.realName"
                type="text"
                class="input"
                :placeholder="errors.realName || '请输入姓名'"
                :style="inputStyle('realName')"
                @blur="onBlur('realName')"
              />
              <el-icon v-if="validated.realName" class="check-icon"><Check /></el-icon>
            </div>
            <p v-if="errors.realName" class="error-text">{{ errors.realName }}</p>
          </div>
        </div>

        <!-- 学院 -->
        <div class="form-item">
          <label class="form-label">
            <span class="required">*</span>学院
          </label>
          <div class="input-wrap" :class="inputClass('college')">
            <select v-model="form.college" class="input select" @change="onBlur('college')">
              <option value="" disabled hidden>{{ errors.college || '请选择学院' }}</option>
              <option v-for="c in colleges" :key="c" :value="c">{{ c }}</option>
            </select>
            <el-icon class="select-arrow"><ArrowDown /></el-icon>
            <el-icon v-if="validated.college" class="check-icon"><Check /></el-icon>
          </div>
          <p v-if="errors.college" class="error-text">{{ errors.college }}</p>
        </div>

        <!-- 手机号 -->
        <div class="form-item">
          <label class="form-label">
            <span class="required">*</span>手机号
          </label>
          <div class="input-wrap" :class="inputClass('phone')">
            <el-icon class="input-icon"><Phone /></el-icon>
            <input
              v-model="form.phone"
              type="tel"
              class="input"
              :placeholder="errors.phone || '请输入手机号'"
              :style="inputStyle('phone')"
              maxlength="11"
              @blur="onBlur('phone')"
            />
            <el-icon v-if="validated.phone" class="check-icon"><Check /></el-icon>
          </div>
          <p v-if="errors.phone" class="error-text">{{ errors.phone }}</p>
        </div>

        <!-- 邮箱 -->
        <div class="form-item">
          <label class="form-label">
            <span class="required">*</span>邮箱
          </label>
          <div class="input-wrap" :class="inputClass('email')">
            <el-icon class="input-icon"><Message /></el-icon>
            <input
              v-model="form.email"
              type="email"
              class="input"
              :placeholder="errors.email || '请输入邮箱'"
              :style="inputStyle('email')"
              @blur="onBlur('email')"
            />
            <el-icon v-if="validated.email" class="check-icon"><Check /></el-icon>
          </div>
          <p v-if="errors.email" class="error-text">{{ errors.email }}</p>
        </div>

        <!-- 密码 -->
        <div class="form-item">
          <label class="form-label">
            <span class="required">*</span>密码
          </label>
          <div class="input-wrap" :class="inputClass('password')">
            <el-icon class="input-icon"><Lock /></el-icon>
            <input
              v-model="form.password"
              :type="passwordVisible ? 'text' : 'password'"
              class="input"
              :placeholder="errors.password || '请输入密码（6-20位）'"
              :style="inputStyle('password')"
              maxlength="20"
              @blur="onBlur('password')"
            />
            <el-icon
              class="input-toggle"
              @click="passwordVisible = !passwordVisible"
            >
              <component :is="passwordVisible ? Hide : View" />
            </el-icon>
            <el-icon v-if="validated.password" class="check-icon"><Check /></el-icon>
          </div>
          <p v-if="errors.password" class="error-text">{{ errors.password }}</p>
        </div>

        <!-- 确认密码 -->
        <div class="form-item">
          <label class="form-label">
            <span class="required">*</span>确认密码
          </label>
          <div class="input-wrap" :class="inputClass('confirmPassword')">
            <el-icon class="input-icon"><Lock /></el-icon>
            <input
              v-model="form.confirmPassword"
              :type="confirmPasswordVisible ? 'text' : 'password'"
              class="input"
              :placeholder="errors.confirmPassword || '请再次输入密码'"
              :style="inputStyle('confirmPassword')"
              maxlength="20"
              @blur="onBlur('confirmPassword')"
            />
            <el-icon
              class="input-toggle"
              @click="confirmPasswordVisible = !confirmPasswordVisible"
            >
              <component :is="confirmPasswordVisible ? Hide : View" />
            </el-icon>
            <el-icon v-if="validated.confirmPassword" class="check-icon"><Check /></el-icon>
          </div>
          <p v-if="errors.confirmPassword" class="error-text">{{ errors.confirmPassword }}</p>
        </div>

        <!-- 注册按钮 -->
        <button type="submit" class="btn-primary" :disabled="loading">
          {{ loading ? '注册中...' : '注 册' }}
        </button>

        <!-- 底部链接 -->
        <div class="link-row">
          <span class="link-text">已有账号？</span>
          <a class="link" @click.prevent="goLogin">返回登录</a>
        </div>
      </form>
    </div>

    <!-- 注册成功弹窗 -->
    <el-dialog
      v-model="successDialogVisible"
      width="420px"
      :show-close="false"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      align-center
      class="success-dialog"
    >
      <div class="success-content">
        <div class="success-icon-wrap">
          <el-icon :size="36" color="#48bb78"><Check /></el-icon>
        </div>
        <h2 class="success-title">注册成功</h2>
        <p class="success-desc">您的账号已注册成功，即将跳转至登录页</p>
        <button class="btn-primary" @click="goLogin">前往登录</button>
      </div>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.register-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #eaf2fb 0%, #f5f7fa 50%, #fef5e7 100%);
  padding: $space-6 $space-4;
  overflow-x: hidden;
}

// ===== 背景 =====
.bg-decoration {
  position: fixed;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
  z-index: 0;
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
  right: -100px;
  animation: blobFloat1 8s ease-in-out infinite alternate;
}
.blob-2 {
  width: 380px;
  height: 380px;
  background: radial-gradient(circle, rgba(246, 173, 85, 0.2), transparent);
  bottom: -80px;
  left: -80px;
  animation: blobFloat2 10s ease-in-out infinite alternate;
}

@keyframes blobFloat1 {
  from { transform: translate(0, 0) scale(1); }
  to { transform: translate(-15px, 15px) scale(1.05); }
}
@keyframes blobFloat2 {
  from { transform: translate(0, 0) scale(1); }
  to { transform: translate(15px, -10px) scale(1.08); }
}

// ===== 注册卡片 =====
.register-card {
  position: relative;
  width: 520px;
  max-width: 100%;
  background: $bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-card;
  padding: $space-8 $space-8 $space-6;
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

// ===== Logo =====
.logo-wrap {
  text-align: center;
  margin-bottom: $space-6;
}
.logo-box {
  width: 52px;
  height: 52px;
  margin: 0 auto $space-3;
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
}
.subtitle {
  margin: 0;
  font-size: $font-size-sm;
  color: $text-secondary;
}

// ===== 表单 =====
.register-form {
  display: flex;
  flex-direction: column;
  gap: $space-4;
}

.form-row.two-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $space-4;
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

.input-wrap {
  display: flex;
  align-items: center;
  gap: $space-2;
  height: 44px;
  padding: 0 $space-3;
  background: $primary-50;
  border: 1.5px solid transparent;
  border-radius: $radius-base;
  transition: all $transition-fast;

  &:focus-within {
    background: $bg-card;
    border-color: $primary;
    box-shadow: 0 0 0 3px rgba(43, 108, 176, 0.08);
  }

  &.is-error {
    background: #fef5f5;
    border-color: $danger;
  }
  &.is-success {
    background: #f0faf4;
    border-color: $success;
  }
}

.input-icon {
  color: $text-placeholder;
  font-size: 16px;
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

.input.select {
  appearance: none;
  cursor: pointer;
  padding-right: $space-2;
  // 红字占位
  &:invalid {
    color: $text-placeholder;
  }
}

.select-arrow {
  color: $text-placeholder;
  font-size: 14px;
  pointer-events: none;
}

.input-toggle {
  cursor: pointer;
  color: $text-placeholder;
  font-size: 16px;
  transition: color $transition-fast;
  &:hover {
    color: $primary;
  }
}

.check-icon {
  color: $success;
  font-size: 18px;
  font-weight: bold;
}

.error-text {
  margin: 0;
  font-size: $font-size-xs;
  color: $danger;
  min-height: 16px;
  line-height: 16px;
}

// ===== 注册按钮 =====
.btn-primary {
  width: 100%;
  height: 46px;
  margin-top: $space-2;
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

// ===== 链接 =====
.link-row {
  text-align: center;
  margin-top: -$space-1;
  font-size: $font-size-sm;
  color: $text-secondary;
}
.link-text {
  color: $text-secondary;
}
.link {
  color: $primary;
  cursor: pointer;
  margin-left: $space-1;
  transition: opacity $transition-fast;
  &:hover {
    opacity: 0.7;
  }
}

// ===== 成功弹窗 =====
.success-content {
  text-align: center;
  padding: $space-4 0 $space-2;
}

.success-icon-wrap {
  width: 72px;
  height: 72px;
  margin: 0 auto $space-5;
  border-radius: 50%;
  background: #e6f7ed;
  display: flex;
  align-items: center;
  justify-content: center;
}

.success-title {
  margin: 0 0 $space-3;
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.success-desc {
  margin: 0 0 $space-6;
  font-size: $font-size-sm;
  color: $text-secondary;
}

:deep(.success-dialog) {
  border-radius: $radius-md;
}
:deep(.el-dialog__header) {
  display: none;
}
:deep(.el-dialog__body) {
  padding: $space-8 $space-6 $space-6;
}

// ===== 响应式 =====
@media (max-width: 600px) {
  .register-card {
    padding: $space-6 $space-5;
  }
  .form-row.two-col {
    grid-template-columns: 1fr;
  }
}
</style>
