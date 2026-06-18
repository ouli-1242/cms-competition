<script setup lang="ts">
/**
 * 密码修改页
 * - 旧密码 / 新密码 / 确认密码
 * - 实时校验 + 强度提示
 * - 提交
 */
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { View, Hide, Lock } from '@element-plus/icons-vue'
import { changePassword } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const errors = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 显示/隐藏
const showOld = ref(false)
const showNew = ref(false)
const showConfirm = ref(false)

const saving = ref(false)

// 密码强度
const strength = computed(() => {
  const p = form.newPassword
  if (!p) return { level: 0, label: '', color: '' }
  let score = 0
  if (p.length >= 6) score++
  if (p.length >= 10) score++
  if (/[a-z]/.test(p) && /[A-Z]/.test(p)) score++
  if (/\d/.test(p)) score++
  if (/[^\w\s]/.test(p)) score++

  if (score <= 2) return { level: 1, label: '弱', color: '#e53e3e' }
  if (score <= 3) return { level: 2, label: '中', color: '#ed8936' }
  return { level: 3, label: '强', color: '#48bb78' }
})

function validate(): boolean {
  errors.oldPassword = ''
  errors.newPassword = ''
  errors.confirmPassword = ''

  let ok = true
  if (!form.oldPassword) {
    errors.oldPassword = '旧密码不能为空'
    ok = false
  }
  if (!form.newPassword) {
    errors.newPassword = '新密码不能为空'
    ok = false
  } else if (form.newPassword.length < 6 || form.newPassword.length > 20) {
    errors.newPassword = '密码长度为 6-20 位'
    ok = false
  } else if (form.newPassword === form.oldPassword) {
    errors.newPassword = '新密码不能与旧密码相同'
    ok = false
  }

  if (!form.confirmPassword) {
    errors.confirmPassword = '请再次输入新密码'
    ok = false
  } else if (form.confirmPassword !== form.newPassword) {
    errors.confirmPassword = '两次输入的新密码不一致，请重新输入'
    ok = false
  }
  return ok
}

async function handleSubmit() {
  if (!validate()) return
  saving.value = true
  try {
    await changePassword(form.oldPassword, form.newPassword)
    ElMessage.success('密码修改成功，请重新登录')
    setTimeout(() => {
      // 退出登录
      userStore.logout()
      router.push('/login')
    }, 1000)
  } catch (e) {
    // 错误已拦截
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="pwd-page">
    <!-- 卡片 -->
    <div class="pwd-card">
      <h2 class="card-title">修改密码</h2>

      <!-- 旧密码 -->
      <div class="form-item">
        <label class="form-label">旧密码</label>
        <div class="input-wrap" :class="{ 'is-error': errors.oldPassword }">
          <el-icon class="input-icon"><Lock /></el-icon>
          <input
            v-model="form.oldPassword"
            :type="showOld ? 'text' : 'password'"
            class="input"
            placeholder="请输入旧密码"
            maxlength="20"
          />
          <el-icon class="toggle-icon" @click="showOld = !showOld">
            <component :is="showOld ? Hide : View" />
          </el-icon>
        </div>
        <p v-if="errors.oldPassword" class="error-text">
          <span class="error-icon">⚠</span>{{ errors.oldPassword }}
        </p>
      </div>

      <!-- 新密码 -->
      <div class="form-item">
        <label class="form-label">新密码</label>
        <div class="input-wrap" :class="{ 'is-error': errors.newPassword }">
          <el-icon class="input-icon"><Lock /></el-icon>
          <input
            v-model="form.newPassword"
            :type="showNew ? 'text' : 'password'"
            class="input"
            placeholder="请输入新密码"
            maxlength="20"
          />
          <el-icon class="toggle-icon" @click="showNew = !showNew">
            <component :is="showNew ? Hide : View" />
          </el-icon>
        </div>
        <!-- 强度 -->
        <div v-if="form.newPassword && !errors.newPassword" class="strength">
          <div class="strength-bar">
            <div
              class="strength-fill"
              :style="{
                width: (strength.level / 3) * 100 + '%',
                background: strength.color
              }"
            ></div>
          </div>
          <span class="strength-text" :style="{ color: strength.color }">
            强度：{{ strength.label }}
          </span>
        </div>
        <p v-if="errors.newPassword" class="error-text">
          <span class="error-icon">⚠</span>{{ errors.newPassword }}
        </p>
      </div>

      <!-- 确认密码 -->
      <div class="form-item">
        <label class="form-label">确认新密码</label>
        <div class="input-wrap" :class="{ 'is-error': errors.confirmPassword }">
          <el-icon class="input-icon"><Lock /></el-icon>
          <input
            v-model="form.confirmPassword"
            :type="showConfirm ? 'text' : 'password'"
            class="input"
            placeholder="请再次输入新密码"
            maxlength="20"
          />
          <el-icon class="toggle-icon" @click="showConfirm = !showConfirm">
            <component :is="showConfirm ? Hide : View" />
          </el-icon>
        </div>
        <p v-if="errors.confirmPassword" class="error-text">
          <span class="error-icon">⚠</span>{{ errors.confirmPassword }}
        </p>
      </div>

      <!-- 提交 -->
      <button class="btn-submit" :disabled="saving" @click="handleSubmit">
        {{ saving ? '修改中...' : '重置密码' }}
      </button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.pwd-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: $space-4 $space-6 $space-8;
}

// ===== 卡片 =====
.pwd-card {
  max-width: 480px;
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
}

// ===== 表单 =====
.form-item {
  margin-bottom: $space-4;
}

.form-label {
  display: block;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $text-regular;
  margin-bottom: $space-2;
}

.input-wrap {
  display: flex;
  align-items: center;
  gap: $space-2;
  height: 44px;
  padding: 0 $space-3;
  border: 1.5px solid $border-base;
  background: $bg-card;
  border-radius: $radius-base;
  transition: all $transition-fast;

  &:focus-within {
    border-color: $primary;
    box-shadow: 0 0 0 3px rgba(43, 108, 176, 0.08);
  }

  &.is-error {
    border-color: $danger;
    background: #fef5f5;
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

.toggle-icon {
  cursor: pointer;
  color: $text-placeholder;
  font-size: 16px;
  transition: color $transition-fast;

  &:hover {
    color: $primary;
  }
}

// ===== 强度条 =====
.strength {
  display: flex;
  align-items: center;
  gap: $space-2;
  margin-top: $space-2;
}

.strength-bar {
  flex: 1;
  height: 4px;
  background: $border-light;
  border-radius: 2px;
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  transition: all $transition-base;
  border-radius: 2px;
}

.strength-text {
  font-size: $font-size-xs;
  font-weight: $font-weight-medium;
  white-space: nowrap;
}

.error-text {
  display: flex;
  align-items: center;
  gap: 4px;
  margin: $space-2 0 0;
  padding: 8px 12px;
  background: #fef5f5;
  border: 1px solid #fed7d7;
  border-radius: $radius-base;
  font-size: $font-size-xs;
  color: $danger;
}

.error-icon {
  font-size: 12px;
}

// ===== 提交按钮 =====
.btn-submit {
  width: 100%;
  height: 44px;
  margin-top: $space-4;
  border: none;
  background: linear-gradient(135deg, $primary, $primary-hover);
  color: #fff;
  font-size: $font-size-md;
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

// ===== 响应式 =====
@media (max-width: 768px) {
  .pwd-page {
    padding: $space-3 $space-4;
  }
}
</style>
