<script setup lang="ts">
/**
 * 创建团队页
 * - 选择竞赛（团队赛）
 * - 输入团队名称
 * - 搜索并邀请学生（多选）
 * - 搜索并邀请指导老师（单选）
 * - 创建后展示邀请码
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCompetitions } from '@/api/public'
import { createTeam, searchStudents, searchTeachers } from '@/api/team'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const competitions = ref<any[]>([])
const selectedCompetitionId = ref<number | null>(null)
const teamName = ref('')
const error = ref('')

// 创建成功的弹窗
const successVisible = ref(false)
const createdTeam = ref<any>(null)

// ===== 学生搜索（多选） =====
const studentKeyword = ref('')
const studentResults = ref<any[]>([])
const selectedStudents = ref<any[]>([])   // {id, realName, college, avatar}
const searchingStudents = ref(false)

async function doSearchStudents() {
  if (!studentKeyword.value.trim()) return
  searchingStudents.value = true
  try {
    const res: any = await searchStudents(studentKeyword.value.trim())
    // 排除自己
    const myId = userStore.user?.id
    studentResults.value = (res || []).filter((s: any) => s.id !== myId)
  } catch {
    studentResults.value = []
  } finally {
    searchingStudents.value = false
  }
}

function toggleStudent(s: any) {
  const idx = selectedStudents.value.findIndex(x => x.id === s.id)
  if (idx >= 0) {
    selectedStudents.value.splice(idx, 1)
  } else {
    selectedStudents.value.push(s)
  }
}

function isSelectedStudent(id: number) {
  return selectedStudents.value.some(x => x.id === id)
}

function removeStudent(id: number) {
  selectedStudents.value = selectedStudents.value.filter(x => x.id !== id)
}

// ===== 教师搜索（单选） =====
const teacherKeyword = ref('')
const teacherResults = ref<any[]>([])
const selectedTeacher = ref<any>(null)     // {id, realName, college, avatar}
const searchingTeachers = ref(false)

async function doSearchTeachers() {
  if (!teacherKeyword.value.trim()) return
  searchingTeachers.value = true
  try {
    const res: any = await searchTeachers(teacherKeyword.value.trim())
    teacherResults.value = res || []
  } catch {
    teacherResults.value = []
  } finally {
    searchingTeachers.value = false
  }
}

function selectTeacher(t: any) {
  selectedTeacher.value = selectedTeacher.value?.id === t.id ? null : t
}

function clearTeacher() {
  selectedTeacher.value = null
}

// ===== 竞赛加载 =====
async function loadCompetitions() {
  loading.value = true
  try {
    const res: any = await getCompetitions({ pageNum: 1, pageSize: 50, type: 2, registrationStatus: 1 })
    competitions.value = res?.records || []
  } catch (e) {
    competitions.value = []
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  if (!userStore.isLoggedIn) {
    router.push({ name: 'Login' })
    return
  }
  error.value = ''
  if (!selectedCompetitionId.value) {
    error.value = '请选择竞赛'
    return
  }
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
      competitionId: selectedCompetitionId.value,
      teamName: teamName.value,
      studentIds: selectedStudents.value.map(s => s.id),
      teacherId: selectedTeacher.value?.id
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
    } else if (e?.message) {
      error.value = e.message
    } else {
      error.value = '创建失败，请稍后重试'
    }
  } finally {
    submitting.value = false
  }
}

function onSuccessClose() {
  successVisible.value = false
  const teamId = createdTeam.value?.id || createdTeam.value?.teamId
  if (teamId) {
    router.push(`/student-center/team-manage/${teamId}`)
  } else {
    router.push('/student-center/my-teams')
  }
}

function copyInviteCode() {
  if (createdTeam.value?.inviteCode) {
    navigator.clipboard?.writeText(createdTeam.value.inviteCode)
  }
}

onMounted(() => {
  loadCompetitions()
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
            <label class="form-label">
              <span class="required">*</span>选择竞赛
            </label>
            <select
              v-model="selectedCompetitionId"
              class="form-input"
              :class="{ 'is-error': error === '请选择竞赛' }"
            >
              <option :value="null" disabled>请选择团队赛竞赛</option>
              <option v-for="c in competitions" :key="c.id" :value="c.id">
                {{ c.title }}
              </option>
            </select>
            <p v-if="competitions.length === 0" class="form-tip">暂无可报名的团队赛竞赛</p>
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

        <!-- 邀请队员（多选） -->
        <div class="form-section">
          <h3 class="section-title">邀请队员 <span class="section-sub">（可选）</span></h3>
          <div class="search-bar">
            <input
              v-model="studentKeyword"
              type="text"
              class="form-input search-input"
              placeholder="输入姓名或学院搜索学生"
              @keyup.enter="doSearchStudents"
            />
            <button class="btn-search" @click="doSearchStudents" :disabled="searchingStudents">
              {{ searchingStudents ? '搜索中...' : '搜索' }}
            </button>
          </div>

          <!-- 搜索结果列表 -->
          <div v-if="studentResults.length > 0" class="search-results">
            <div
              v-for="s in studentResults"
              :key="s.id"
              class="result-item"
              :class="{ selected: isSelectedStudent(s.id) }"
              @click="toggleStudent(s)"
            >
              <div class="result-avatar">
                <img v-if="s.avatar" :src="s.avatar" class="avatar-img" />
                <span v-else>{{ s.realName?.[0] || 'U' }}</span>
              </div>
              <div class="result-info">
                <span class="result-name">{{ s.realName }}</span>
                <span class="result-college">{{ s.college || '未知学院' }}</span>
              </div>
              <div class="result-check">
                <span v-if="isSelectedStudent(s.id)" class="check-mark">✓</span>
              </div>
            </div>
          </div>
          <p v-else-if="studentKeyword && !searchingStudents" class="form-tip">
            未找到匹配的学生，请尝试其他关键词
          </p>

          <!-- 已选队员标签 -->
          <div v-if="selectedStudents.length > 0" class="selected-tags">
            <span class="tags-label">已选择 {{ selectedStudents.length }} 人：</span>
            <div class="tag-list">
              <span v-for="s in selectedStudents" :key="s.id" class="tag-item">
                {{ s.realName }}
                <button class="tag-remove" @click="removeStudent(s.id)">×</button>
              </span>
            </div>
          </div>
        </div>

        <!-- 邀请指导老师（单选） -->
        <div class="form-section">
          <h3 class="section-title">邀请指导老师 <span class="section-sub">（可选）</span></h3>
          <div class="search-bar">
            <input
              v-model="teacherKeyword"
              type="text"
              class="form-input search-input"
              placeholder="输入姓名或学院搜索教师"
              @keyup.enter="doSearchTeachers"
            />
            <button class="btn-search" @click="doSearchTeachers" :disabled="searchingTeachers">
              {{ searchingTeachers ? '搜索中...' : '搜索' }}
            </button>
          </div>

          <!-- 搜索结果列表 -->
          <div v-if="teacherResults.length > 0" class="search-results">
            <div
              v-for="t in teacherResults"
              :key="t.id"
              class="result-item"
              :class="{ selected: selectedTeacher?.id === t.id }"
              @click="selectTeacher(t)"
            >
              <div class="result-avatar">
                <img v-if="t.avatar" :src="t.avatar" class="avatar-img" />
                <span v-else>{{ t.realName?.[0] || 'U' }}</span>
              </div>
              <div class="result-info">
                <span class="result-name">{{ t.realName }}</span>
                <span class="result-college">{{ t.college || '未知学院' }}</span>
              </div>
              <div class="result-check">
                <span v-if="selectedTeacher?.id === t.id" class="check-mark">✓</span>
              </div>
            </div>
          </div>
          <p v-else-if="teacherKeyword && !searchingTeachers" class="form-tip">
            未找到匹配的教师，请尝试其他关键词
          </p>

          <!-- 已选教师 -->
          <div v-if="selectedTeacher" class="selected-tags">
            <span class="tags-label">已选择：</span>
            <div class="tag-list">
              <span class="tag-item">
                {{ selectedTeacher.realName }}
                <button class="tag-remove" @click="clearTeacher()">×</button>
              </span>
            </div>
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
          <p class="tip">6 位邀请码有效期 {{ createdTeam?.expireDays || 5 }} 天</p>
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

// ===== 搜索区域 =====
.section-sub {
  font-weight: $font-weight-regular;
  font-size: $font-size-xs;
  color: $text-secondary;
}

.search-bar {
  display: flex;
  gap: $space-2;
  margin-bottom: $space-3;
}

.search-input {
  flex: 1;
}

.btn-search {
  height: 42px;
  padding: 0 $space-4;
  border: 1px solid $border-base;
  background: $bg-card;
  color: $primary;
  font-size: $font-size-sm;
  border-radius: $radius-base;
  cursor: pointer;
  white-space: nowrap;
  transition: all $transition-fast;

  &:hover:not(:disabled) {
    background: $primary-50;
    border-color: $primary;
  }
  &:disabled {
    color: $text-disabled;
    cursor: not-allowed;
  }
}

.search-results {
  border: 1px solid $border-light;
  border-radius: $radius-base;
  max-height: 220px;
  overflow-y: auto;
  margin-bottom: $space-3;
}

.result-item {
  display: flex;
  align-items: center;
  gap: $space-3;
  padding: $space-2 $space-3;
  cursor: pointer;
  transition: background $transition-fast;

  &:hover {
    background: $primary-50;
  }

  &.selected {
    background: $primary-50;
  }

  & + .result-item {
    border-top: 1px solid $border-light;
  }
}

.result-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, $primary, $primary-hover);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  flex-shrink: 0;
  overflow: hidden;
}

.result-avatar .avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.result-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.result-name {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $text-primary;
}

.result-college {
  font-size: $font-size-xs;
  color: $text-secondary;
}

.result-check {
  flex-shrink: 0;
  width: 20px;
  text-align: center;
}

.check-mark {
  color: $primary;
  font-weight: $font-weight-bold;
  font-size: $font-size-md;
}

// ===== 已选标签 =====
.selected-tags {
  display: flex;
  align-items: center;
  gap: $space-2;
  flex-wrap: wrap;
  padding: $space-2 0;
}

.tags-label {
  font-size: $font-size-xs;
  color: $text-secondary;
  flex-shrink: 0;
}

.tag-list {
  display: flex;
  gap: $space-2;
  flex-wrap: wrap;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px $space-2 2px $space-3;
  background: $primary-50;
  color: $primary;
  border-radius: $radius-full;
  font-size: $font-size-xs;
  font-weight: $font-weight-medium;
}

.tag-remove {
  border: none;
  background: transparent;
  color: $primary;
  cursor: pointer;
  font-size: 14px;
  line-height: 1;
  padding: 0 2px;
  opacity: 0.6;
  transition: opacity $transition-fast;

  &:hover {
    opacity: 1;
  }
}
</style>
