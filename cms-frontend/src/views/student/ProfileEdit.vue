<script setup lang="ts">
/**
 * 学生个人资料编辑页
 * - 上传头像
 * - 编辑姓名、学院、手机号
 * - 学号、邮箱只读
 */
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserInfo, updateUserInfo } from '@/api/auth'

const router = useRouter()

const form = reactive({
  realName: '张明远',
  college: '计算机科学与技术学院',
  phone: '13812347899',
  username: '2024001234',
  email: 'zhangmingyuan@example.com',
  avatar: ''
})

const errors = reactive({
  realName: '',
  college: '',
  phone: ''
})

const saving = ref(false)
const successDialogVisible = ref(false)

function onSuccessConfirm() {
  successDialogVisible.value = false
  router.push('/student/profile')
}

const collegeOptions = [
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

async function loadUser() {
  try {
    const data: any = await getUserInfo()
    if (data) Object.assign(form, data)
  } catch (e) {
    // 静默
  }
}

function validate() {
  errors.realName = ''
  errors.college = ''
  errors.phone = ''

  if (!form.realName.trim()) {
    errors.realName = '姓名不能为空'
    return false
  }
  if (form.realName.length < 2) {
    errors.realName = '姓名至少 2 个字'
    return false
  }
  if (!form.college) {
    errors.college = '请选择学院'
    return false
  }
  if (!form.phone) {
    errors.phone = '手机号不能为空'
    return false
  }
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    errors.phone = '请输入正确的手机号'
    return false
  }
  return true
}

async function handleSave() {
  if (!validate()) return
  saving.value = true
  try {
    await updateUserInfo({
      realName: form.realName,
      college: form.college,
      phone: form.phone
    })
    // ElMessage.success('保存成功')
    successDialogVisible.value = true
  } catch (e) {
    // 错误已拦截
  } finally {
    saving.value = false
  }
}

function handleCancel() {
  router.back()
}

// ====== 头像上传 ======
const fileInput = ref<HTMLInputElement | null>(null)

function triggerUpload() {
  fileInput.value?.click()
}

function onFileChange(e: Event) {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  // 校验
  if (!/^image\/(jpg|jpeg|png)$/.test(file.type)) {
    ElMessage.error('请上传 JPG/PNG 格式')
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 2MB')
    return
  }

  // 预览
  const reader = new FileReader()
  reader.onload = (ev) => {
    form.avatar = ev.target?.result as string
  }
  reader.readAsDataURL(file)
}

onMounted(() => {
  loadUser()
})
</script>

<template>
  <div class="edit-page">
    <!-- 顶部返回栏 -->
    <div class="page-header">
      <button class="back-btn" @click="router.back()">
        <span>←</span>
      </button>
      <h1 class="page-title">校园活动管理平台</h1>
      <div class="user-mini">
        <div class="avatar">{{ form.realName[0] }}</div>
        <span class="user-name">{{ form.realName }}</span>
      </div>
    </div>

    <!-- 返回链接 -->
    <div class="back-link" @click="router.push('/student/profile')">
      <span class="back-icon">←</span>返回个人中心
    </div>

    <!-- 编辑卡 -->
    <div class="edit-card">
      <h2 class="card-title">编辑个人资料</h2>

      <!-- 头像区 -->
      <div class="avatar-section">
        <div class="avatar-preview">
          <img v-if="form.avatar" :src="form.avatar" alt="avatar" />
          <span v-else>{{ form.realName[0] }}</span>
        </div>
        <div class="avatar-info">
          <button class="btn-upload" @click="triggerUpload">上传头像</button>
          <p class="avatar-tip">支持 JPG、PNG 格式，大小不超过 2MB</p>
          <input
            ref="fileInput"
            type="file"
            accept="image/jpeg,image/png"
            style="display: none"
            @change="onFileChange"
          />
        </div>
      </div>

      <!-- 表单 -->
      <div class="form-section">
        <div class="form-item">
          <label class="form-label">姓名</label>
          <input
            v-model="form.realName"
            type="text"
            class="form-input"
            :class="{ 'is-error': errors.realName }"
            placeholder="请输入姓名"
            maxlength="20"
          />
          <p v-if="errors.realName" class="error-text">{{ errors.realName }}</p>
        </div>

        <div class="form-item">
          <label class="form-label">学院</label>
          <select
            v-model="form.college"
            class="form-input form-select"
            :class="{ 'is-error': errors.college }"
          >
            <option value="" disabled>请选择学院</option>
            <option v-for="c in collegeOptions" :key="c" :value="c">{{ c }}</option>
          </select>
          <p v-if="errors.college" class="error-text">{{ errors.college }}</p>
        </div>

        <div class="form-item">
          <label class="form-label">手机号</label>
          <input
            v-model="form.phone"
            type="tel"
            class="form-input"
            :class="{ 'is-error': errors.phone }"
            placeholder="请输入手机号"
            maxlength="11"
          />
          <p v-if="errors.phone" class="error-text">{{ errors.phone }}</p>
        </div>

        <div class="form-item">
          <label class="form-label">学号</label>
          <input
            v-model="form.username"
            type="text"
            class="form-input disabled"
            disabled
            readonly
          />
        </div>

        <div class="form-item">
          <label class="form-label">邮箱</label>
          <input
            v-model="form.email"
            type="email"
            class="form-input disabled"
            disabled
            readonly
          />
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <button class="btn-cancel" @click="handleCancel">取消</button>
        <button class="btn-save" :disabled="saving" @click="handleSave">
          {{ saving ? '保存中...' : '保存修改' }}
        </button>
      </div>
    </div>

    <!-- 修改成功弹窗（状态 8） -->
    <el-dialog
      v-model="successDialogVisible"
      width="400px"
      :show-close="true"
      align-center
    >
      <template #header>
        <div class="dialog-header">
          <h3>提示</h3>
        </div>
      </template>
      <div class="dialog-body">
        <div class="alert alert-success">
          <div class="alert-icon">✓</div>
          <div>
            <p class="alert-title">修改成功</p>
            <p class="alert-desc">个人资料已更新保存。</p>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer center">
          <button class="btn-ok" @click="onSuccessConfirm">确定</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.edit-page {
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

// ===== 返回链接 =====
.back-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin: 0 0 $space-3;
  font-size: $font-size-sm;
  color: $text-secondary;
  cursor: pointer;
  transition: color $transition-fast;

  &:hover {
    color: $primary;
  }
}

.back-icon {
  font-size: 16px;
}

// ===== 编辑卡 =====
.edit-card {
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
}

// ===== 头像区 =====
.avatar-section {
  display: flex;
  align-items: center;
  gap: $space-4;
  padding-bottom: $space-5;
  margin-bottom: $space-5;
  border-bottom: 1px solid $border-light;
}

.avatar-preview {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, $primary, $primary-hover);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: $font-weight-semibold;
  box-shadow: 0 4px 12px rgba(43, 108, 176, 0.3);
  overflow: hidden;
  flex-shrink: 0;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.avatar-info {
  flex: 1;
}

.btn-upload {
  height: 32px;
  padding: 0 $space-4;
  border: 1px solid $primary;
  background: $primary-50;
  color: $primary;
  font-size: $font-size-sm;
  border-radius: $radius-base;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover {
    background: $primary;
    color: #fff;
  }
}

.avatar-tip {
  margin: $space-2 0 0;
  font-size: $font-size-xs;
  color: $text-secondary;
}

// ===== 表单 =====
.form-section {
  display: flex;
  flex-direction: column;
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

  &.disabled {
    background: $bg-page;
    color: $text-secondary;
    cursor: not-allowed;
  }
}

.form-select {
  appearance: none;
  background-image: url("data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 12 12'%3E%3Cpath fill='%23a0aec0' d='M6 8.5L1.5 4h9z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  background-size: 12px;
  padding-right: 32px;
  cursor: pointer;
}

.error-text {
  margin: 0;
  font-size: $font-size-xs;
  color: $danger;
}

// ===== 操作按钮 =====
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: $space-3;
  margin-top: $space-6;
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

.btn-save {
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

// ===== 响应式 =====
@media (max-width: 768px) {
  .edit-page {
    padding: $space-3 $space-4;
  }
  .avatar-section {
    flex-direction: column;
    align-items: flex-start;
  }
  .user-mini .user-name {
    display: none;
  }
}

// ===== 弹窗样式 =====
:deep(.el-dialog__header) {
  padding: $space-5 $space-6 0;
  margin-right: 0;
}

.dialog-header {
  h3 {
    margin: 0;
    font-size: $font-size-md;
    font-weight: $font-weight-semibold;
    color: $text-primary;
  }
}

.dialog-body {
  padding: $space-3 0 $space-2;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: $space-3;
  padding-top: $space-3;
}

.alert {
  display: flex;
  gap: $space-3;
  padding: $space-4 $space-5;
  border-radius: $radius-base;
  margin-bottom: $space-4;

  &.alert-success {
    background: #e6f7ed;
    color: $success;
  }
}

.alert-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: $font-weight-bold;
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.5);
  margin-top: 2px;
}

.alert-title {
  margin: 0 0 4px;
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
}

.alert-desc {
  margin: 0;
  font-size: $font-size-sm;
  opacity: 0.85;
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
}
</style>
