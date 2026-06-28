<script setup lang="ts">
/**
 * 报名详情页
 * - 展示报名信息（竞赛、时间、状态、附件、说明）
 * - 待审核状态可编辑说明和重新上传附件
 * - 已审核状态展示审核意见
 */
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getRegistrationDetail,
  updateRegistration,
  getTeamRegDetail,
  updateTeamRegistration
} from '@/api/registration'
import { uploadFile } from '@/api/upload'
import { useUserStore } from '@/stores/user'


const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// type: 'individual' | 'team'，从 query 传入
const regType = ref<'individual' | 'team'>((route.query.type as any) || 'individual')
const regId = Number(route.params.id)

const loading = ref(true)
const saving = ref(false)
const editing = ref(false)

const detail = ref<any>(null)

// 编辑表单
const editDescription = ref('')
const editAttachment = ref('')
const editAttachmentName = ref('')

// 文件上传
const fileInput = ref<HTMLInputElement | null>(null)
const uploading = ref(false)

const statusMap: Record<number, { text: string; color: string }> = {
  0: { text: '审核中', color: 'warning' },
  1: { text: '已通过', color: 'success' },
  2: { text: '已拒绝', color: 'danger' }
}

async function loadDetail() {
  loading.value = true
  try {
    const res: any = regType.value === 'team'
      ? await getTeamRegDetail(regId)
      : await getRegistrationDetail(regId)
    detail.value = res
    editDescription.value = res.description || ''
    editAttachment.value = res.attachment || ''
    editAttachmentName.value = res.attachment ? extractFileName(res.attachment) : ''
  } catch {
    ElMessage.error('加载报名详情失败')
  } finally {
    loading.value = false
  }
}

function extractFileName(url: string): string {
  if (!url) return ''
  const parts = url.split('/')
  return parts[parts.length - 1] || url
}

function getStatusInfo(status: number) {
  return statusMap[status] || { text: '未知', color: 'info' }
}

function canEdit(): boolean {
  if (!detail.value) return false
  if (detail.value.status !== 0) return false
  if (regType.value === 'team' && !detail.value.isCaptain) return false
  return true
}

function startEdit() {
  editing.value = true
}

function cancelEdit() {
  editing.value = false
  editDescription.value = detail.value?.description || ''
  editAttachment.value = detail.value?.attachment || ''
  editAttachmentName.value = detail.value?.attachment ? extractFileName(detail.value.attachment) : ''
}

function triggerUpload() {
  fileInput.value?.click()
}

async function onFileChange(e: Event) {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return
  const allowTypes = [
    'application/zip',
    'application/pdf',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'application/msword'
  ]
  if (!allowTypes.includes(file.type) && !/\.(zip|pdf|docx|doc)$/i.test(file.name)) {
    ElMessage.error('仅支持 PDF/DOC/DOCX/ZIP 格式')
    return
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 10MB')
    return
  }
  uploading.value = true
  try {
    const url = await uploadFile(file, 'attachment')
    editAttachment.value = url
    editAttachmentName.value = file.name
    ElMessage.success('文件上传成功')
  } catch {
    ElMessage.error('文件上传失败')
  } finally {
    uploading.value = false
    if (fileInput.value) fileInput.value.value = ''
  }
}

function removeAttachment() {
  editAttachment.value = ''
  editAttachmentName.value = ''
}

async function saveEdit() {
  if (saving.value) return
  saving.value = true
  try {
    if (regType.value === 'team') {
      await updateTeamRegistration(regId, editDescription.value, editAttachment.value)
    } else {
      await updateRegistration(regId, editDescription.value, editAttachment.value)
    }
    ElMessage.success('保存成功')
    editing.value = false
    await loadDetail()
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<template>
  <div class="detail-page">
    <div class="page-header">
      <button class="back-btn" @click="router.back()">
        <span>←</span>
      </button>
      <h1 class="page-title">高校学科竞赛报名管理系统</h1>
      <div class="user-mini">
        <div class="avatar">{{ userStore.user?.realName?.[0] || 'U' }}</div>
        <span class="user-name">{{ userStore.user?.realName || '用户' }}</span>
      </div>
    </div>

    <div class="breadcrumb">
      <span @click="router.push('/student/profile')">个人中心</span>
      <span class="sep">›</span>
      <span @click="router.push('/student-center/my-registrations')">我的报名</span>
      <span class="sep">›</span>
      <span class="current">报名详情</span>
    </div>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="6" animated />
    </div>

    <template v-else-if="detail">
      <!-- 基本信息卡片 -->
      <div class="info-card">
        <div class="card-header">
          <h2 class="card-title">
            {{ regType === 'team' ? '团队报名' : '个人报名' }}详情
          </h2>
          <el-tag
            size="small"
            :type="getStatusInfo(detail.status).color as any"
            effect="light"
            round
          >
            {{ getStatusInfo(detail.status).text }}
          </el-tag>
        </div>

        <div class="info-grid">
          <div class="info-row">
            <span class="info-label">竞赛名称</span>
            <span class="info-value">{{ detail.competitionTitle || `竞赛 #${detail.competitionId}` }}</span>
          </div>
          <div v-if="regType === 'team'" class="info-row">
            <span class="info-label">团队名称</span>
            <span class="info-value">{{ detail.teamName || `团队 #${detail.teamId}` }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">报名时间</span>
            <span class="info-value time">{{ detail.registerTime || '-' }}</span>
          </div>
        </div>
      </div>

      <!-- 说明 / 编辑 -->
      <div class="info-card">
        <div class="card-header">
          <h3 class="card-title">报名说明</h3>
          <el-button
            v-if="!editing && canEdit()"
            type="primary"
            size="small"
            plain
            round
            @click="startEdit"
          >
            编辑
          </el-button>
        </div>

        <div v-if="!editing">
          <p class="description-text">{{ detail.description || '无' }}</p>
        </div>
        <div v-else>
          <el-input
            v-model="editDescription"
            type="textarea"
            :rows="4"
            placeholder="请输入报名说明"
            class="edit-textarea"
          />
        </div>
      </div>

      <!-- 附件 / 编辑 -->
      <div class="info-card">
        <div class="card-header">
          <h3 class="card-title">上传附件</h3>
          <el-button
            v-if="!editing && canEdit()"
            type="primary"
            size="small"
            plain
            round
            @click="startEdit"
          >
            编辑
          </el-button>
        </div>

        <div v-if="!editing">
          <div v-if="detail.attachment" class="file-display">
            <div class="file-icon">F</div>
            <div class="file-info">
              <p class="file-name">{{ extractFileName(detail.attachment) }}</p>
            </div>
            <a :href="detail.attachment" target="_blank" class="file-download">下载</a>
          </div>
          <p v-else class="no-file">未上传附件</p>
        </div>
        <div v-else class="edit-upload">
          <div v-if="editAttachmentName" class="file-display current-file">
            <div class="file-icon">F</div>
            <div class="file-info">
              <p class="file-name">{{ editAttachmentName }}</p>
            </div>
            <button class="btn-remove-file" @click="removeAttachment">删除</button>
          </div>
          <div class="upload-actions">
            <button
              class="btn-upload"
              :disabled="uploading"
              @click="triggerUpload"
            >
              {{ uploading ? '上传中...' : (editAttachmentName ? '更换文件' : '选择文件') }}
            </button>
            <span class="upload-tip">支持 PDF/DOC/DOCX/ZIP，不超过 10MB</span>
          </div>
          <input
            ref="fileInput"
            type="file"
            accept=".pdf,.doc,.docx,.zip"
            style="display: none"
            @change="onFileChange"
          />
        </div>
      </div>

      <!-- 审核信息（已审核时显示） -->
      <div v-if="detail.status !== 0" class="info-card review-card">
        <h3 class="card-title">审核信息</h3>
        <div class="info-grid">
          <div class="info-row">
            <span class="info-label">审核结果</span>
            <el-tag
              size="small"
              :type="getStatusInfo(detail.status).color as any"
              effect="light"
              round
            >
              {{ getStatusInfo(detail.status).text }}
            </el-tag>
          </div>
          <div v-if="detail.reviewRemark" class="info-row full">
            <span class="info-label">审核意见</span>
            <span class="info-value">{{ detail.reviewRemark }}</span>
          </div>
          <div v-if="detail.reviewTime" class="info-row">
            <span class="info-label">审核时间</span>
            <span class="info-value time">{{ detail.reviewTime }}</span>
          </div>
        </div>
      </div>

      <!-- 底部操作 -->
      <div class="bottom-actions">
        <button class="btn-cancel" @click="router.back()">返回</button>
        <template v-if="editing">
          <button class="btn-cancel" @click="cancelEdit">取消编辑</button>
          <button
            class="btn-primary"
            :disabled="saving"
            @click="saveEdit"
          >
            {{ saving ? '保存中...' : '保存修改' }}
          </button>
        </template>
      </div>
    </template>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.detail-page {
  max-width: 960px;
  margin: 0 auto;
  padding: $space-4 $space-6 $space-8;
}

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

.loading {
  background: $bg-card;
  border-radius: $radius-md;
  padding: $space-6;
}

.info-card {
  background: $bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;
  padding: $space-5;
  margin-bottom: $space-4;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $space-4;
}

.card-title {
  margin: 0;
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: $space-3;
}

.info-row {
  display: flex;
  align-items: flex-start;
  gap: $space-3;
  font-size: $font-size-sm;

  &.full {
    flex-direction: column;
    gap: $space-1;
  }
}

.info-label {
  color: $text-secondary;
  white-space: nowrap;
  min-width: 80px;
  flex-shrink: 0;
}

.info-value {
  color: $text-primary;
  flex: 1;

  &.time {
    font-family: 'Consolas', monospace;
    color: $text-secondary;
  }
}

.description-text {
  margin: 0;
  font-size: $font-size-sm;
  color: $text-regular;
  line-height: 1.8;
  white-space: pre-wrap;
}

.edit-textarea {
  :deep(textarea) {
    font-size: $font-size-sm;
    line-height: 1.8;
  }
}

.file-display {
  display: flex;
  align-items: center;
  gap: $space-3;
  padding: $space-3;
  background: $success-bg;
  border: 1px solid $success-light;
  border-radius: $radius-base;

  &.current-file {
    margin-bottom: $space-3;
  }
}

.file-icon {
  width: 40px;
  height: 40px;
  border-radius: $radius-base;
  background: $success;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.file-info {
  flex: 1;
}

.file-name {
  margin: 0;
  font-size: $font-size-sm;
  color: $text-primary;
  font-weight: $font-weight-medium;
}

.file-download {
  padding: 4px 12px;
  border: 1px solid $primary;
  color: $primary;
  font-size: $font-size-sm;
  border-radius: $radius-sm;
  text-decoration: none;
  transition: all $transition-fast;
  &:hover {
    background: $primary;
    color: #fff;
  }
}

.no-file {
  margin: 0;
  font-size: $font-size-sm;
  color: $text-placeholder;
}

.edit-upload {
  .upload-actions {
    display: flex;
    align-items: center;
    gap: $space-3;
  }
}

.btn-upload {
  height: 36px;
  padding: 0 $space-4;
  border: 1px solid $primary;
  background: transparent;
  color: $primary;
  font-size: $font-size-sm;
  border-radius: $radius-base;
  cursor: pointer;
  transition: all $transition-fast;
  &:hover:not(:disabled) {
    background: $primary;
    color: #fff;
  }
  &:disabled {
    border-color: $border-base;
    color: $text-disabled;
    cursor: not-allowed;
  }
}

.upload-tip {
  font-size: $font-size-xs;
  color: $text-secondary;
}

.btn-remove-file {
  padding: 4px 10px;
  border: 1px solid $danger;
  background: transparent;
  color: $danger;
  font-size: $font-size-sm;
  border-radius: $radius-sm;
  cursor: pointer;
  transition: all $transition-fast;
  &:hover {
    background: $danger;
    color: #fff;
  }
}

.review-card {
  background: $bg-page;
}

.bottom-actions {
  display: flex;
  justify-content: flex-end;
  gap: $space-3;
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

@media (max-width: 768px) {
  .detail-page {
    padding: $space-3 $space-4;
  }
  .user-mini .user-name {
    display: none;
  }
}
</style>
