<script setup lang="ts">
/**
 * 提交团队报名页
 * - 显示团队信息
 * - 上传附件（团队名单等）
 * - 提交后等待审核
 */
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { submitTeamRegistration, getTeamDetail } from '@/api/team'
import { uploadFile } from '@/api/upload'
import { getCompetitionDetail } from '@/api/public'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const team = ref<any>(null)
const loading = ref(false)
const submitting = ref(false)

const files = ref<{ name: string; size: number; file: File }[]>([])

const successVisible = ref(false)

async function loadTeam() {
  loading.value = true
  try {
    const id = Number(route.params.id)
    const res: any = await getTeamDetail(id)
    if (res && res.team) {
      const t = res.team
      let compTitle = ''
      if (t.competitionId) {
        try {
          const comp: any = await getCompetitionDetail(t.competitionId)
          compTitle = comp?.title || `竞赛 #${t.competitionId}`
        } catch {
          compTitle = `竞赛 #${t.competitionId}`
        }
      }
      team.value = {
        id: t.id,
        title: t.teamName || '团队',
        category: compTitle,
        currentCount: res.members?.length || 0,
        maxMembers: t.maxSize || 5,
        members: (res.members || []).map((m: any) => ({
          realName: m.realName || m.username || '成员',
          role: m.userId === t.captainId ? 'CAPTAIN' : 'MEMBER'
        }))
      }
    } else {
      team.value = null
    }
  } catch (e) {
    team.value = null
  } finally {
    loading.value = false
  }
}

const fileInput = ref<HTMLInputElement | null>(null)

function triggerUpload() {
  fileInput.value?.click()
}

function onFileChange(e: Event) {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return
  // 限制：zip/pdf/docx，单个 10MB
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
  files.value = [{ name: file.name, size: file.size, file }]
}

function removeFile() {
  files.value = []
  if (fileInput.value) fileInput.value.value = ''
}

function formatSize(bytes: number) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

async function handleSubmit() {
  if (submitting.value) return
  if (files.value.length === 0) {
    ElMessage.warning('请上传团队材料')
    return
  }
  submitting.value = true
  try {
    // 先上传文件
    const uploadedUrls: string[] = []
    for (const f of files.value) {
      const url = await uploadFile(f.file, 'attachment')
      uploadedUrls.push(url)
    }
    await submitTeamRegistration(
      team.value.id,
      undefined,
      uploadedUrls.join(',')
    )
    successVisible.value = true
  } finally {
    submitting.value = false
  }
}

function onSuccessClose() {
  successVisible.value = false
  router.push('/student-center/my-registrations')
}

onMounted(() => {
  loadTeam()
})
</script>

<template>
  <div class="submit-page">
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
      <span @click="router.push('/student-center/my-teams')">我的团队</span>
      <span class="sep">›</span>
      <span class="current">提交报名</span>
    </div>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="6" animated />
    </div>

    <template v-else-if="team">
      <div class="info-card">
        <h2 class="team-title">{{ team.title }}</h2>
        <div class="team-meta">
          <el-tag size="small" effect="plain" round>{{ team.category }}</el-tag>
          <el-tag size="small" effect="plain" type="info" round>{{ team.currentCount }}/{{ team.maxMembers }}人</el-tag>
          <el-tag size="small" effect="plain" type="warning" round>待提交</el-tag>
        </div>
      </div>

      <div class="upload-card">
        <h3 class="upload-title">附件上传</h3>
        <p class="upload-tip">支持 PDF / DOC / DOCX / ZIP，单文件不超过 10MB</p>

        <div v-if="files.length === 0" class="upload-zone" @click="triggerUpload">
          <div class="upload-icon">↑</div>
          <p class="upload-text">点击或拖拽文件到此处上传</p>
        </div>
        <div v-else class="file-list">
          <div v-for="(f, i) in files" :key="i" class="file-item">
            <div class="file-icon">F</div>
            <div class="file-info">
              <p class="file-name">{{ f.name }}</p>
              <p class="file-size">{{ formatSize(f.size) }}</p>
            </div>
            <button class="btn-remove" @click="removeFile">删除</button>
          </div>
        </div>

        <input
          ref="fileInput"
          type="file"
          accept=".pdf,.doc,.docx,.zip"
          style="display: none"
          @change="onFileChange"
        />
      </div>

      <div class="bottom-actions">
        <button class="btn-cancel" @click="router.back()">取消</button>
        <button class="btn-primary" :disabled="submitting || files.length === 0" @click="handleSubmit">
          {{ submitting ? '提交中...' : '提交报名' }}
        </button>
      </div>
    </template>

    <!-- 提交成功弹窗 -->
    <el-dialog
      v-model="successVisible"
      width="420px"
      :show-close="false"
      align-center
    >
      <template #header>
        <div class="dialog-header center">
          <h3>提交成功</h3>
        </div>
      </template>
      <div class="dialog-body">
        <p class="dialog-text">团队所有信息提交成功<br/>请耐心等待审核结果</p>
      </div>
      <template #footer>
        <div class="dialog-footer center">
          <button class="btn-ok primary" @click="onSuccessClose">我知道了</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.submit-page {
  max-width: 1280px;
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

.team-title {
  margin: 0 0 $space-3;
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.team-meta {
  display: flex;
  gap: $space-2;
  flex-wrap: wrap;
}

.upload-card {
  background: $bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;
  padding: $space-5;
  margin-bottom: $space-4;
}

.upload-title {
  margin: 0 0 $space-1;
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.upload-tip {
  margin: 0 0 $space-4;
  font-size: $font-size-xs;
  color: $text-secondary;
}

.upload-zone {
  height: 160px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: $bg-page;
  border: 2px dashed $border-base;
  border-radius: $radius-md;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover {
    background: $primary-50;
    border-color: $primary;
  }
}

.upload-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: $primary-50;
  color: $primary;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: $font-weight-bold;
  margin-bottom: $space-2;
}

.upload-text {
  margin: 0;
  font-size: $font-size-sm;
  color: $text-secondary;
}

.file-list {
  display: flex;
  flex-direction: column;
  gap: $space-2;
}

.file-item {
  display: flex;
  align-items: center;
  gap: $space-3;
  padding: $space-3;
  background: $success-bg;
  border: 1px solid $success-light;
  border-radius: $radius-base;
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
  margin: 0 0 2px;
  font-size: $font-size-sm;
  color: $text-primary;
  font-weight: $font-weight-medium;
}

.file-size {
  margin: 0;
  font-size: $font-size-xs;
  color: $text-secondary;
}

.btn-remove {
  padding: 4px 10px;
  border: 1px solid $danger;
  background: transparent;
  color: $danger;
  font-size: 14px;
  border-radius: $radius-sm;
  cursor: pointer;
  transition: all $transition-fast;
  &:hover {
    background: $danger;
    color: #fff;
  }
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
  justify-content: center;
  padding-top: $space-3;
}

.btn-ok {
  &.primary {
    min-width: 120px;
    height: 40px;
    padding: 0 $space-6;
    border: none;
    background: linear-gradient(135deg, $primary, $primary-hover);
    color: #fff;
    font-size: $font-size-base;
    border-radius: $radius-base;
    cursor: pointer;
    box-shadow: 0 4px 12px rgba(43, 108, 176, 0.25);
    transition: all $transition-fast;
    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 6px 16px rgba(43, 108, 176, 0.35);
    }
  }
}

@media (max-width: 768px) {
  .submit-page {
    padding: $space-3 $space-4;
  }
  .user-mini .user-name {
    display: none;
  }
}
</style>
