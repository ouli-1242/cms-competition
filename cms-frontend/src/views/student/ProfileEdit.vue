<script setup lang="ts">
/**
 * 学生个人资料编辑页
 * - 上传头像
 * - 编辑姓名、学院、手机号（直接保存）
 * - 学号、邮箱可编辑但需管理员审批
 */
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserInfo, updateUserInfo, submitProfileChange } from '@/api/auth'
import { uploadFile } from '@/api/upload'
import { useUserStore } from '@/stores/user'


const userStore = useUserStore()

const router = useRouter()

const form = reactive({
  realName: '',
  college: '',
  phone: '',
  username: '',
  email: '',
  avatar: ''
})

// 记录原始值用于对比
const original = reactive({
  username: '',
  email: ''
})

const errors = reactive({
  realName: '',
  college: '',
  phone: ''
})

const saving = ref(false)
const successDialogVisible = ref(false)
const successMessage = ref('')

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
    if (data) {
      form.realName = data.realName || data.nickname || ''
      form.college = data.college || ''
      form.phone = data.phone || ''
      form.username = data.username || ''
      form.email = data.email || ''
      form.avatar = data.avatar || ''
      original.username = form.username
      original.email = form.email
    }
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

  const messages: string[] = []

  try {
    // 1. 直接保存常规字段
    await updateUserInfo({
      realName: form.realName,
      college: form.college,
      phone: form.phone,
      avatar: form.avatar || undefined
    })
    messages.push('基本资料已保存')

    // 2. 学号变更 → 提交审批
    if (form.username !== original.username) {
      if (!form.username.trim()) {
        messages.push('学号不能为空，已跳过学号修改')
      } else {
        try {
          await submitProfileChange('username', form.username.trim())
          messages.push('学号修改已提交审批')
        } catch {
          messages.push('学号修改提交失败')
        }
      }
    }

    // 3. 邮箱变更 → 提交审批
    if (form.email !== original.email) {
      try {
        await submitProfileChange('email', form.email.trim())
        messages.push('邮箱修改已提交审批')
      } catch {
        messages.push('邮箱修改提交失败')
      }
    }

    await userStore.fetchInfo()
    successMessage.value = messages.join('；')
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

const uploading = ref(false)
async function onFileChange(e: Event) {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('头像不能超过 5MB')
    return
  }
  uploading.value = true
  try {
    const url = await uploadFile(file, 'avatar')
    form.avatar = url
    ElMessage.success('头像上传成功')
  } finally {
    uploading.value = false
  }
}

onMounted(() => {
  loadUser()
})
</script>

<template>
  <div class="edit-page">
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
            class="form-input"
            placeholder="请输入学号"
          />
          <p class="hint-text">修改学号需管理员审批后生效</p>
        </div>

        <div class="form-item">
          <label class="form-label">邮箱</label>
          <input
            v-model="form.email"
            type="email"
            class="form-input"
            placeholder="请输入邮箱"
          />
          <p class="hint-text">修改邮箱需管理员审批后生效</p>
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

    <!-- 修改成功弹窗 -->
    <el-dialog
      v-model="successDialogVisible"
      width="420px"
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
            <p class="alert-title">保存完成</p>
            <p class="alert-desc">{{ successMessage }}</p>
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

.hint-text {
  margin: 0;
  font-size: $font-size-xs;
  color: $warning;
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
