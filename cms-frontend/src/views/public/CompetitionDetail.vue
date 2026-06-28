<script setup lang="ts">
/**
 * 竞赛详情页
 * - 状态 1：未登录 - 显示"登录后报名"按钮
 * - 状态 2：报名中 + 未报名 - 显示"个人报名"或"团队报名"按钮
 * - 状态 3：已报名 - 显示"我的报名"禁用按钮
 * - 状态 4：草稿 - 显示"草稿"禁用按钮
 * - 状态 5：已截止 - 禁用按钮
 * - 状态 6：未开始 - 禁用按钮
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Document,
  Download,
  Calendar,
  User,
  Trophy,
  Money,
  Position,
  Clock
} from '@element-plus/icons-vue'
import { getCompetitionDetail } from '@/api/public'
import { registerIndividual } from '@/api/registration'
import { createTeam, joinTeam } from '@/api/team'
import { uploadFile } from '@/api/upload'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// ====== 数据 ======
const competition = ref<any>(null)
const loading = ref(false)

// ====== 是否团队赛（type: 1=个人, 2=团队） ======
const isTeamCompetition = computed(() => competition.value?.type === 2)

// ====== 报名弹窗 ======
const regDialogVisible = ref(false)
const regForm = ref({ description: '', attachment: '' })
const regSubmitting = ref(false)
const regSuccess = ref(false)

// ====== 团队报名弹窗 ======
const teamDialogVisible = ref(false)
const teamActiveTab = ref<'create' | 'join'>('create')
const teamForm = ref({ teamName: '', slogan: '', maxSize: 5 })
const joinCode = ref('')
const teamSubmitting = ref(false)
const teamSuccess = ref(false)
const teamSuccessMsg = ref('')

// ====== 报名确认 ======
const agreed = ref(false)

// ====== 附件上传 ======
const uploadRef = ref<HTMLInputElement | null>(null)
const uploading = ref(false)
const uploadedFile = ref<{ name: string; url: string } | null>(null)

// ====== 工具函数 ======
function formatDate(d: string): string {
  if (!d) return '-'
  return d.replace('T', ' ').replace(/:\d{2}$/, '')
}

function triggerUpload() {
  uploadRef.value?.click()
}

async function onFileChange(e: Event) {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 10MB')
    target.value = ''
    return
  }
  uploading.value = true
  try {
    const url = await uploadFile(file, 'attachment')
    uploadedFile.value = { name: file.name, url }
    regForm.value.attachment = url
  } catch {
    ElMessage.error('文件上传失败')
  } finally {
    uploading.value = false
    target.value = ''
  }
}

function removeUploadedFile() {
  uploadedFile.value = null
  regForm.value.attachment = ''
}

// ====== 占位数据 ======
const placeholder: any = {
  id: 1,
  title: '2026 全国大学生算法编程挑战赛',
  category: '程序设计',
  type: 1,
  registerStart: '2026-02-15',
  registerEnd: '2026-05-31',
  compStart: '2026-06-15',
  compEnd: '2026-06-20',
  maxTeamSize: 5,
  minTeamSize: 3,
  status: 1, // 1: 报名中
  cover: '',
  description: '本次算法编程挑战赛旨在激发大学生对算法学习的兴趣，提升解决实际问题的能力。竞赛采用团队报名形式，队员需要具备扎实的编程基础和良好的团队协作精神。比赛分预选赛、复赛和决赛三个阶段，分别考察基础算法、数据结构应用和综合工程能力。',
  attachment: '',
  registrationCount: 42
}

// ====== 加载 ======
async function loadData() {
  loading.value = true
  try {
    const data = await getCompetitionDetail(Number(route.params.id))
    competition.value = data || placeholder
  } catch (e) {
    competition.value = placeholder
  } finally {
    loading.value = false
  }
}

// ====== 报名状态判定 ======
const regState = computed(() => {
  const c = competition.value
  if (!c) return { code: 'loading', label: '加载中', disabled: true, color: 'default' }

  const now = Date.now()
  const start = new Date(c.registerStart).getTime()
  const end = new Date(c.registerEnd).getTime()

  // 报名截止（不受登录状态影响）
  if (now > end) {
    return { code: 'ended', label: '报名已截止', disabled: true, color: 'default' }
  }
  // 未开始（不受登录状态影响）
  if (now < start) {
    return { code: 'not_started', label: '报名未开始', disabled: true, color: 'default' }
  }

  // 报名中 - 未登录提示登录
  if (!userStore.isLoggedIn) {
    return { code: 'login_required', label: '登录后报名', disabled: false, color: 'primary' }
  }

  // 报名中 - 已登录
  if (isTeamCompetition.value) {
    return { code: 'registering_team', label: '团队报名', disabled: false, color: 'primary' }
  }
  return { code: 'registering_individual', label: '个人报名', disabled: false, color: 'primary' }
})

// ====== 基于日期的状态标签（不受登录状态影响） ======
const statusDisplay = computed(() => {
  const c = competition.value
  if (!c) return { label: '', type: 'info' as const }

  const now = Date.now()
  const start = new Date(c.registerStart).getTime()
  const end = new Date(c.registerEnd).getTime()

  if (now > end) {
    return { label: '已截止', type: 'info' as const }
  }
  if (now < start) {
    return { label: '即将开始', type: 'warning' as const }
  }
  return { label: '报名中', type: 'warning' as const }
})

// ====== 操作 ======
function onAction() {
  if (regState.value.disabled) return
  if (regState.value.code === 'login_required') {
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  if (regState.value.code === 'registering_team') {
    // 团队赛弹出报名窗口
    teamForm.value = { teamName: '', slogan: '', maxSize: competition.value?.maxTeamSize || 5 }
    joinCode.value = ''
    teamSuccess.value = false
    teamActiveTab.value = 'create'
    teamDialogVisible.value = true
    return
  }
  if (regState.value.code === 'registering_individual') {
    // 个人赛弹出报名窗口
    regForm.value = { description: '', attachment: '' }
    agreed.value = false
    uploadedFile.value = null
    regSuccess.value = false
    regDialogVisible.value = true
    return
  }
}

function downloadAttachment(url: string) {
  if (url) window.open(url, '_blank')
}

async function submitRegistration() {
  if (regSubmitting.value) return
  if (!agreed.value) {
    ElMessage.warning('请先确认报名须知')
    return
  }
  regSubmitting.value = true
  try {
    await registerIndividual(
      competition.value.id,
      regForm.value.description || undefined,
      regForm.value.attachment || undefined
    )
    regSuccess.value = true
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    regSubmitting.value = false
  }
}

function goToMyRegistrations() {
  regDialogVisible.value = false
  router.push('/student-center/my-registrations')
}

// ====== 团队操作 ======
async function handleCreateTeam() {
  if (!teamForm.value.teamName.trim()) {
    ElMessage.warning('请输入团队名称')
    return
  }
  teamSubmitting.value = true
  try {
    await createTeam({
      teamName: teamForm.value.teamName,
      competitionId: competition.value.id,
      maxSize: teamForm.value.maxSize,
      slogan: teamForm.value.slogan || undefined
    })
    teamSuccess.value = true
    teamSuccessMsg.value = '团队创建成功！你是队长，可以通过邀请码邀请队员加入。'
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    teamSubmitting.value = false
  }
}


async function handleJoinTeam() {
  if (!joinCode.value.trim()) {
    ElMessage.warning('请输入邀请码')
    return
  }
  teamSubmitting.value = true
  try {
    await joinTeam(joinCode.value)
    teamSuccess.value = true
    teamSuccessMsg.value = '已成功加入团队！等待队长提交报名。'
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    teamSubmitting.value = false
  }
}


function goToMyTeams() {
  teamDialogVisible.value = false
  router.push('/student-center/my-teams')
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="detail-page">
    <!-- 顶部返回栏 -->
    <div class="page-header">
      <button class="back-btn" @click="router.back()">
        <span>←</span>
      </button>
      <h1 class="page-title">竞赛平台</h1>
      <div style="width:36px"></div>
    </div>

    <!-- 封面 -->
    <div v-if="competition" class="cover">
      <img v-if="competition.cover" :src="competition.cover" :alt="competition.title" class="cover-img" />
      <div v-else class="cover-bg">
        <div class="cover-circle cover-circle-1"></div>
        <div class="cover-circle cover-circle-2"></div>
      </div>
      <div class="cover-overlay"></div>
    </div>

    <!-- 标题区 -->
    <div v-if="competition" class="title-block">
      <div class="title-row">
        <h1 class="title">{{ competition.title }}</h1>
        <el-tag
          v-if="statusDisplay.label"
          :type="statusDisplay.type"
          effect="light"
          round
          class="status-tag"
        >
          {{ statusDisplay.label }}
        </el-tag>
      </div>
      <p class="organizer">类别：{{ competition.category }}</p>
    </div>

    <!-- 竞赛信息 -->
    <div v-if="competition" class="section info-section">
      <h3 class="section-title">
        <span class="bar"></span>竞赛信息
      </h3>
      <el-row :gutter="24">
        <el-col :xs="24" :sm="12" :md="8">
          <div class="info-item">
            <el-icon class="info-icon"><Calendar /></el-icon>
            <div>
              <p class="info-label">报名时间</p>
              <p class="info-value">{{ formatDate(competition.registerStart) }} 至 {{ formatDate(competition.registerEnd) }}</p>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="info-item">
            <el-icon class="info-icon"><Calendar /></el-icon>
            <div>
              <p class="info-label">比赛时间</p>
              <p class="info-value">{{ formatDate(competition.compStart) }} 至 {{ formatDate(competition.compEnd) }}</p>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="info-item">
            <el-icon class="info-icon"><User /></el-icon>
            <div>
              <p class="info-label">参赛形式</p>
              <p class="info-value">
                <template v-if="isTeamCompetition">
                  团体赛<template v-if="competition.minTeamSize">（{{ competition.minTeamSize }}-{{ competition.maxTeamSize }}人）</template><template v-else-if="competition.maxTeamSize">（最多{{ competition.maxTeamSize }}人）</template>
                </template>
                <template v-else>个人赛</template>
              </p>
            </div>
          </div>
        </el-col>
        <el-col v-if="competition.phases" :xs="24" :sm="12" :md="8">
          <div class="info-item">
            <el-icon class="info-icon"><Trophy /></el-icon>
            <div>
              <p class="info-label">比赛阶段</p>
              <p class="info-value">{{ competition.phases }}</p>
            </div>
          </div>
        </el-col>
        <el-col v-if="competition.budget" :xs="24" :sm="12" :md="8">
          <div class="info-item">
            <el-icon class="info-icon"><Money /></el-icon>
            <div>
              <p class="info-label">人均预算</p>
              <p class="info-value">{{ competition.budget }} 元</p>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="info-item">
            <el-icon class="info-icon"><Position /></el-icon>
            <div>
              <p class="info-label">已报名</p>
              <p class="info-value">{{ competition.registrationCount || 0 }} {{ isTeamCompetition ? '队' : '人' }}</p>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 竞赛简介 -->
    <div v-if="competition" class="section">
      <h3 class="section-title">
        <span class="bar"></span>竞赛简介
      </h3>
      <div class="description">
        <p>{{ competition.description }}</p>
      </div>
    </div>

    <!-- 附件下载 -->
    <div v-if="competition && competition.attachment" class="section">
      <h3 class="section-title">
        <span class="bar"></span>附件下载
      </h3>
      <div class="attachment-list">
        <div
          class="attachment-item"
          @click="downloadAttachment(competition.attachment)"
        >
          <el-icon class="att-icon" :size="20" color="#e53e3e"><Document /></el-icon>
          <span class="att-name">附件下载</span>
          <el-icon class="att-download" :size="16" color="#a0aec0"><Download /></el-icon>
        </div>
      </div>
    </div>

    <!-- 占位避免被底栏遮挡 -->
    <div class="bottom-spacer"></div>

    <!-- 底部固定操作栏 -->
    <div v-if="competition" class="action-bar" :class="`action-${regState.color}`">
      <button
        class="action-btn"
        :class="`btn-${regState.color}`"
        :disabled="regState.disabled"
        @click="onAction"
      >
        <el-icon v-if="regState.disabled" class="action-icon"><Clock /></el-icon>
        {{ regState.label }}
      </button>
    </div>

    <!-- 报名弹窗 -->
    <el-dialog
      v-model="regDialogVisible"
      width="520px"
      :show-close="true"
      :close-on-click-modal="!regSuccess"
      align-center
    >
      <template #header>
        <div class="dialog-header">
          <h3>竞赛报名</h3>
          <p class="dialog-subtitle">{{ competition?.title }}</p>
        </div>
      </template>

      <!-- 报名表单 -->
      <div v-if="!regSuccess" class="reg-dialog-body">
        <!-- 个人信息 -->
        <div class="reg-user-info">
          <div class="reg-user-avatar">{{ userStore.user?.realName?.[0] || 'U' }}</div>
          <div class="reg-user-detail">
            <span class="reg-user-name">{{ userStore.user?.realName || '-' }}</span>
            <span class="reg-user-meta">{{ userStore.user?.username || '-' }} · {{ userStore.user?.college || userStore.user?.school || '-' }}</span>
          </div>
        </div>

        <div class="reg-info-row">
          <span class="reg-label">竞赛类型</span>
          <span class="reg-value">{{ competition?.category || '-' }}</span>
        </div>
        <div class="reg-info-row">
          <span class="reg-label">参赛形式</span>
          <span class="reg-value">个人赛</span>
        </div>
        <div class="reg-info-row">
          <span class="reg-label">报名时间</span>
          <span class="reg-value">{{ formatDate(competition?.registerStart) }} 至 {{ formatDate(competition?.registerEnd) }}</span>
        </div>
        <div class="reg-info-row">
          <span class="reg-label">比赛时间</span>
          <span class="reg-value">{{ formatDate(competition?.compStart) }} 至 {{ formatDate(competition?.compEnd) }}</span>
        </div>

        <div class="reg-form-group">
          <label class="reg-form-label">报名说明（选填）</label>
          <el-input
            v-model="regForm.description"
            type="textarea"
            :rows="3"
            placeholder="简要描述你的参赛动机或相关经历"
            maxlength="500"
            show-word-limit
          />
        </div>

        <div class="reg-form-group">
          <label class="reg-form-label">附件材料（选填）</label>
          <div v-if="uploadedFile" class="reg-file-item">
            <el-icon :size="16" color="#38a169"><Document /></el-icon>
            <span class="reg-file-name">{{ uploadedFile.name }}</span>
            <button class="reg-file-remove" @click="removeUploadedFile">移除</button>
          </div>
          <div v-else class="reg-upload-zone" @click="triggerUpload">
            <span v-if="uploading" class="reg-upload-loading">上传中...</span>
            <span v-else>点击上传文件（PDF/DOC/ZIP，不超过10MB）</span>
          </div>
          <input
            ref="uploadRef"
            type="file"
            accept=".pdf,.doc,.docx,.zip"
            style="display: none"
            @change="onFileChange"
          />
        </div>

        <!-- 报名须知 -->
        <div class="reg-notice">
          <h4 class="reg-notice-title">报名须知</h4>
          <ul class="reg-notice-list">
            <li>报名提交后将进入审核流程，审核结果可在「我的报名」中查看</li>
            <li>待审核状态下可取消报名，审核通过后不可取消</li>
            <li>请确保个人信息准确，如有变更请在个人中心修改</li>
          </ul>
        </div>

        <!-- 确认勾选 -->
        <label class="reg-agree">
          <input v-model="agreed" type="checkbox" />
          <span>我已确认信息无误并了解参赛规则</span>
        </label>
      </div>

      <!-- 报名成功 -->
      <div v-else class="reg-success">
        <div class="success-icon">✓</div>
        <h4 class="success-title">报名成功</h4>
        <p class="success-desc">你的报名已提交，请等待审核。可在「我的报名」中查看进度。</p>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <template v-if="!regSuccess">
            <el-button @click="regDialogVisible = false">取消</el-button>
            <el-button
              type="primary"
              :loading="regSubmitting"
              :disabled="!agreed"
              @click="submitRegistration"
            >
              {{ regSubmitting ? '提交中...' : '确认报名' }}
            </el-button>
          </template>
          <template v-else>
            <el-button @click="regDialogVisible = false">关闭</el-button>
            <el-button type="primary" @click="goToMyRegistrations">查看我的报名</el-button>
          </template>
        </div>
      </template>
    </el-dialog>

    <!-- 团队报名弹窗 -->
    <el-dialog
      v-model="teamDialogVisible"
      width="520px"
      :show-close="true"
      :close-on-click-modal="!teamSuccess"
      align-center
    >
      <template #header>
        <div class="dialog-header">
          <h3>团队报名</h3>
          <p class="dialog-subtitle">{{ competition?.title }}</p>
        </div>
      </template>

      <!-- 团队报名表单 -->
      <div v-if="!teamSuccess" class="reg-dialog-body">
        <div class="reg-info-row">
          <span class="reg-label">参赛形式</span>
          <span class="reg-value">团体赛<template v-if="competition?.minTeamSize">（{{ competition?.minTeamSize }}-{{ competition?.maxTeamSize }}人）</template><template v-else-if="competition?.maxTeamSize">（最多{{ competition?.maxTeamSize }}人）</template></span>
        </div>
        <div class="reg-info-row">
          <span class="reg-label">报名时间</span>
          <span class="reg-value">{{ formatDate(competition?.registerStart) }} 至 {{ formatDate(competition?.registerEnd) }}</span>
        </div>

        <!-- Tab 切换 -->
        <el-tabs v-model="teamActiveTab" class="team-tabs">
          <el-tab-pane label="创建团队" name="create">
            <div class="reg-form-group">
              <label class="reg-form-label">团队名称</label>
              <el-input v-model="teamForm.teamName" placeholder="输入团队名称" maxlength="30" />
            </div>
            <div class="reg-form-group">
              <label class="reg-form-label">团队口号（选填）</label>
              <el-input v-model="teamForm.slogan" placeholder="一句话介绍你的团队" maxlength="50" />
            </div>
            <div class="reg-form-group">
              <label class="reg-form-label">团队人数</label>
              <el-input-number
                v-model="teamForm.maxSize"
                :min="competition?.minTeamSize || 1"
                :max="competition?.maxTeamSize || 10"
              />
            </div>
          </el-tab-pane>

          <el-tab-pane label="加入团队" name="join">
            <div class="reg-form-group">
              <label class="reg-form-label">邀请码</label>
              <el-input v-model="joinCode" placeholder="输入队长提供的邀请码" />
            </div>
            <p class="team-hint">请从队长处获取邀请码，加入后等待队长提交报名即可。</p>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 成功 -->
      <div v-else class="reg-success">
        <div class="success-icon">✓</div>
        <h4 class="success-title">操作成功</h4>
        <p class="success-desc">{{ teamSuccessMsg }}</p>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <template v-if="!teamSuccess">
            <el-button @click="teamDialogVisible = false">取消</el-button>
            <el-button
              v-if="teamActiveTab === 'create'"
              type="primary"
              :loading="teamSubmitting"
              @click="handleCreateTeam"
            >
              {{ teamSubmitting ? '创建中...' : '创建团队' }}
            </el-button>
            <el-button
              v-else
              type="primary"
              :loading="teamSubmitting"
              @click="handleJoinTeam"
            >
              {{ teamSubmitting ? '加入中...' : '加入团队' }}
            </el-button>
          </template>
          <template v-else>
            <el-button @click="teamDialogVisible = false">关闭</el-button>
            <el-button type="primary" @click="goToMyTeams">查看我的团队</el-button>
          </template>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.detail-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: $space-4 $space-6 150px;
}

// ===== 顶部返回栏 =====
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $space-2 0 $space-3;
}

.back-btn,
.header-icon {
  width: 36px;
  height: 36px;
  border: none;
  background: $bg-card;
  border-radius: 50%;
  cursor: pointer;
  font-size: 16px;
  color: $text-regular;
  box-shadow: $shadow-sm;
  transition: all $transition-fast;
  display: flex;
  align-items: center;
  justify-content: center;

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

// ===== 封面 =====
@keyframes coverFloat {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.02); }
}

.cover {
  position: relative;
  height: 260px;
  border-radius: $radius-lg;
  overflow: hidden;
  margin-bottom: $space-4;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  animation: coverFloat 8s ease-in-out infinite;
}

.cover-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #1a202c 0%, #2d3748 40%, #4299e1 80%, #48bb78 100%);
}

.cover-circle {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(229, 62, 62, 0.4), transparent);
}
.cover-circle-1 {
  width: 400px;
  height: 400px;
  top: -100px;
  right: -100px;
}
.cover-circle-2 {
  width: 300px;
  height: 300px;
  bottom: -150px;
  left: 10%;
  background: radial-gradient(circle, rgba(214, 158, 46, 0.4), transparent);
}

.cover-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, transparent 60%, rgba(0, 0, 0, 0.5));
}

// ===== 标题区 =====
.title-block {
  padding: 0 $space-1;
  margin-bottom: $space-5;
}

.title-row {
  display: flex;
  align-items: center;
  gap: $space-3;
  flex-wrap: wrap;
  margin-bottom: $space-2;
}

.title {
  margin: 0;
  font-size: $font-size-2xl;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.status-tag {
  font-size: $font-size-xs !important;
}

.organizer {
  margin: 0;
  font-size: $font-size-sm;
  color: $text-secondary;
}

// ===== 通用 Section =====
@keyframes sectionFadeIn {
  from { opacity: 0; transform: translateY(12px); }
  to   { opacity: 1; transform: translateY(0); }
}

.section {
  background: $bg-card;
  border-radius: $radius-lg;
  padding: $space-5 $space-6;
  margin-bottom: $space-5;
  box-shadow: $shadow-sm;
  border: 1px solid $border-light;
  animation: sectionFadeIn 0.5s ease both;
  transition: box-shadow $transition-base, transform $transition-base;

  &:hover {
    box-shadow: $shadow-md;
  }

  &:nth-child(3) { animation-delay: 0.05s; }
  &:nth-child(4) { animation-delay: 0.1s; }
  &:nth-child(5) { animation-delay: 0.15s; }
  &:nth-child(6) { animation-delay: 0.2s; }
}

.section-title {
  display: flex;
  align-items: center;
  gap: $space-2;
  margin: 0 0 $space-4;
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.bar {
  display: inline-block;
  width: 4px;
  height: 16px;
  border-radius: 2px;
  background: linear-gradient(180deg, $primary, $primary-hover);
}

// ===== 信息区 =====
.info-section {
  .info-item {
    display: flex;
    align-items: flex-start;
    gap: $space-3;
    margin-bottom: $space-4;
  }

  .info-icon {
    color: $primary;
    font-size: 20px;
    margin-top: 2px;
    flex-shrink: 0;
  }

  .info-label {
    margin: 0 0 4px;
    font-size: $font-size-xs;
    color: $text-secondary;
  }

  .info-value {
    margin: 0;
    font-size: $font-size-base;
    color: $text-primary;
    font-weight: $font-weight-medium;
  }
}

// ===== 简介 =====
.description {
  font-size: $font-size-base;
  line-height: 1.8;
  color: $text-regular;
  text-indent: 2em;

  p {
    margin: 0;
  }
}

// ===== 附件 =====
.attachment-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $space-3;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: $space-2;
  padding: $space-3 $space-4;
  background: $bg-page;
  border: 1px solid $border-light;
  border-radius: $radius-base;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover {
    background: $primary-50;
    border-color: $primary;

    .att-name {
      color: $primary;
    }
  }
}

.att-icon {
  flex-shrink: 0;
}

.att-name {
  flex: 1;
  font-size: $font-size-sm;
  color: $text-primary;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.att-size {
  font-size: $font-size-xs;
  color: $text-placeholder;
  flex-shrink: 0;
}

.att-download {
  flex-shrink: 0;
}

// ===== 底部操作栏 =====
.bottom-spacer {
  height: 130px;
}

.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 101;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-top: 1px solid $border-light;
  padding: $space-3 $space-6;
  display: flex;
  justify-content: center;
  box-shadow: 0 -4px 16px rgba(0, 0, 0, 0.06);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to   { transform: translateY(0); }
}

.action-btn {
  min-width: 240px;
  max-width: 400px;
  width: 50%;
  height: 44px;
  border: none;
  border-radius: $radius-base;
  font-size: $font-size-md;
  font-weight: $font-weight-medium;
  letter-spacing: 2px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $space-2;
  transition: all $transition-base;
}

.btn-primary {
  background: linear-gradient(135deg, $primary, $primary-hover);
  color: #fff;
  box-shadow: 0 4px 12px rgba(43, 108, 176, 0.3);
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
    box-shadow: 0 6px 20px rgba(43, 108, 176, 0.45);

    &::after {
      transform: translateX(100%);
    }
  }

  &:active:not(:disabled) {
    transform: translateY(0) scale(0.98);
  }
}

.btn-success {
  background: $bg-disabled;
  color: $success;
  cursor: not-allowed;
  border: 1px solid $success;
}

.btn-warning {
  background: $bg-disabled;
  color: $warning;
  cursor: not-allowed;
  border: 1px solid $warning;
}

.btn-default {
  background: $bg-disabled;
  color: $text-disabled;
  cursor: not-allowed;
  border: 1px solid $border-base;
}

.action-icon {
  font-size: 16px;
}

// ===== 报名弹窗 =====
.dialog-header {
  h3 {
    margin: 0 0 4px;
    font-size: $font-size-md;
    font-weight: $font-weight-semibold;
    color: $text-primary;
  }
}

.dialog-subtitle {
  margin: 0;
  font-size: $font-size-sm;
  color: $text-secondary;
  font-weight: $font-weight-regular;
}

.reg-dialog-body {
  padding: $space-2 0;
}

.reg-info-row {
  display: flex;
  align-items: center;
  padding: $space-2 0;
  border-bottom: 1px dashed $border-light;

  &:last-of-type {
    margin-bottom: $space-4;
  }
}

.reg-label {
  width: 80px;
  font-size: $font-size-sm;
  color: $text-secondary;
  flex-shrink: 0;
}

.reg-value {
  flex: 1;
  font-size: $font-size-sm;
  color: $text-primary;
  font-weight: $font-weight-medium;
}

.reg-form-group {
  margin-bottom: $space-4;
}

.reg-form-label {
  display: block;
  font-size: $font-size-sm;
  color: $text-primary;
  font-weight: $font-weight-medium;
  margin-bottom: $space-2;
}

// ===== 个人信息 =====
.reg-user-info {
  display: flex;
  align-items: center;
  gap: $space-3;
  padding: $space-3 $space-4;
  background: $bg-page;
  border-radius: $radius-base;
  margin-bottom: $space-4;
}

.reg-user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, $primary, $primary-hover);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-size-sm;
  font-weight: $font-weight-semibold;
  flex-shrink: 0;
}

.reg-user-detail {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.reg-user-name {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $text-primary;
}

.reg-user-meta {
  font-size: $font-size-xs;
  color: $text-secondary;
}

// ===== 文件上传 =====
.reg-upload-zone {
  padding: $space-4;
  border: 1.5px dashed $border-base;
  border-radius: $radius-base;
  text-align: center;
  font-size: $font-size-sm;
  color: $text-secondary;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover {
    border-color: $primary;
    background: $primary-50;
    color: $primary;
  }
}

.reg-upload-loading {
  color: $primary;
}

.reg-file-item {
  display: flex;
  align-items: center;
  gap: $space-2;
  padding: $space-3 $space-4;
  background: $success-bg;
  border: 1px solid rgba(56, 161, 105, 0.2);
  border-radius: $radius-base;
}

.reg-file-name {
  flex: 1;
  font-size: $font-size-sm;
  color: $text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.reg-file-remove {
  padding: 2px 8px;
  border: 1px solid $danger;
  background: transparent;
  color: $danger;
  font-size: $font-size-xs;
  border-radius: $radius-sm;
  cursor: pointer;
  transition: all $transition-fast;
  flex-shrink: 0;

  &:hover {
    background: $danger;
    color: #fff;
  }
}

// ===== 报名须知 =====
.reg-notice {
  background: $bg-page;
  border-radius: $radius-base;
  padding: $space-3 $space-4;
  margin-bottom: $space-4;
}

.reg-notice-title {
  margin: 0 0 $space-2;
  font-size: $font-size-sm;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.reg-notice-list {
  margin: 0;
  padding: 0 0 0 $space-4;
  list-style: none;

  li {
    position: relative;
    padding: 2px 0 2px $space-2;
    font-size: $font-size-xs;
    color: $text-secondary;
    line-height: 1.7;

    &::before {
      content: '·';
      position: absolute;
      left: 0;
      color: $text-placeholder;
    }
  }
}

// ===== 确认勾选 =====
.reg-agree {
  display: flex;
  align-items: center;
  gap: $space-2;
  font-size: $font-size-sm;
  color: $text-regular;
  cursor: pointer;
  user-select: none;

  input[type="checkbox"] {
    width: 16px;
    height: 16px;
    accent-color: $primary;
    cursor: pointer;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: $space-3;
}

// ===== 报名成功 =====
.reg-success {
  text-align: center;
  padding: $space-6 $space-4;
}

.success-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto $space-4;
  border-radius: 50%;
  background: $success;
  color: #fff;
  font-size: 28px;
  font-weight: $font-weight-bold;
  display: flex;
  align-items: center;
  justify-content: center;
}

.success-title {
  margin: 0 0 $space-2;
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.success-desc {
  margin: 0;
  font-size: $font-size-sm;
  color: $text-secondary;
  line-height: 1.6;
}

// ===== 团队弹窗 Tab =====
.team-tabs {
  margin-top: $space-3;
}

.team-hint {
  margin: $space-3 0 0;
  font-size: $font-size-xs;
  color: $text-secondary;
  line-height: 1.6;
}

// ===== 响应式 =====
@media (max-width: 768px) {
  .detail-page {
    padding: $space-3 $space-4 150px;
  }
  .title {
    font-size: $font-size-lg;
  }
  .attachment-list {
    grid-template-columns: 1fr;
  }
  .action-btn {
    width: 80%;
    min-width: 200px;
  }
}
</style>
