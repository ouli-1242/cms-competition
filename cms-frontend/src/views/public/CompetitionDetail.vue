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
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// ====== 数据 ======
const competition = ref<any>(null)
const loading = ref(false)

// ====== 当前用户报名状态（mock） ======
// 实际应通过后端查询
const myRegStatus = ref<'not_started' | 'registering' | 'registered' | 'draft' | 'ended' | 'login_required'>(
  'registering'
)
const isTeamCompetition = ref(true) // 是否团队赛

// ====== 占位数据 ======
const placeholder: any = {
  id: 1,
  title: '2026 全国大学生算法编程挑战赛',
  organizer: '中国计算机学会',
  category: '程序设计',
  registerStart: '2026-02-15',
  registerEnd: '2026-05-31',
  contestStart: '2026-06-15',
  contestEnd: '2026-06-20',
  teamType: 'team',
  teamSize: '3-5',
  phases: '校赛 + 全国决赛',
  budget: '0',
  registered: 1286,
  teams: 0,
  status: 1, // 1: 报名中
  cover: '',
  description: '本次算法编程挑战赛旨在激发大学生对算法学习的兴趣，提升解决实际问题的能力。竞赛采用团队报名形式，队员需要具备扎实的编程基础和良好的团队协作精神。比赛分预选赛、复赛和决赛三个阶段，分别考察基础算法、数据结构应用和综合工程能力。',
  attachments: [
    { name: '竞赛章程.pdf', size: '2.4 MB' },
    { name: '报名表模板.pdf', size: '0.8 MB' }
  ]
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
  // 未登录
  if (!userStore.isLoggedIn) {
    return { code: 'login_required', label: '登录后报名', disabled: false, color: 'primary' }
  }

  const c = competition.value
  if (!c) return { code: 'loading', label: '加载中', disabled: true, color: 'default' }

  const now = Date.now()
  const start = new Date(c.registerStart).getTime()
  const end = new Date(c.registerEnd).getTime()
  const contestEnd = new Date(c.contestEnd).getTime()

  // 已截止
  if (now > contestEnd) {
    return { code: 'ended', label: '已截止', disabled: true, color: 'default' }
  }
  // 报名截止但比赛未结束
  if (now > end) {
    return { code: 'ended', label: '报名已截止', disabled: true, color: 'default' }
  }
  // 未开始
  if (now < start) {
    return { code: 'not_started', label: '报名未开始', disabled: true, color: 'default' }
  }
  // 已报名
  if (myRegStatus.value === 'registered') {
    return { code: 'registered', label: '我的报名', disabled: true, color: 'success' }
  }
  // 草稿
  if (myRegStatus.value === 'draft') {
    return { code: 'draft', label: '草稿', disabled: true, color: 'warning' }
  }
  // 报名中
  if (isTeamCompetition.value) {
    return { code: 'registering_team', label: '团队报名', disabled: false, color: 'primary' }
  }
  return { code: 'registering_individual', label: '个人报名', disabled: false, color: 'primary' }
})

// ====== 操作 ======
function onAction() {
  if (regState.value.disabled) return
  if (regState.value.code === 'login_required') {
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  if (regState.value.code === 'registering_team') {
    // 跳转到创建/加入团队
    router.push({ path: '/student/my-teams', query: { competitionId: competition.value?.id } })
    return
  }
  if (regState.value.code === 'registering_individual') {
    // 个人赛直接报名
    ElMessage.success('个人报名已提交')
    myRegStatus.value = 'registered'
    return
  }
}

function downloadAttachment(att: any) {
  ElMessage.success(`下载：${att.name}`)
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
      <button class="header-icon" @click="router.push('/')">
        <span>🏠</span>
      </button>
    </div>

    <!-- 封面 -->
    <div v-if="competition" class="cover">
      <div class="cover-bg">
        <div class="cover-circle cover-circle-1"></div>
        <div class="cover-circle cover-circle-2"></div>
        <div class="cover-overlay"></div>
      </div>
    </div>

    <!-- 标题区 -->
    <div v-if="competition" class="title-block">
      <div class="title-row">
        <h1 class="title">{{ competition.title }}</h1>
        <el-tag
          v-if="competition.status === 1"
          type="warning"
          effect="light"
          round
          class="status-tag"
        >
          报名中
        </el-tag>
        <el-tag v-else-if="competition.status === 2" type="info" effect="light" round class="status-tag">
          已截止
        </el-tag>
        <el-tag v-else type="warning" effect="light" round class="status-tag">
          即将开始
        </el-tag>
      </div>
      <p class="organizer">📍 主办方：{{ competition.organizer }}</p>
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
              <p class="info-value">{{ competition.registerStart }} 至 {{ competition.registerEnd }}</p>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="info-item">
            <el-icon class="info-icon"><Calendar /></el-icon>
            <div>
              <p class="info-label">比赛时间</p>
              <p class="info-value">{{ competition.contestStart }} 至 {{ competition.contestEnd }}</p>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="info-item">
            <el-icon class="info-icon"><User /></el-icon>
            <div>
              <p class="info-label">参赛形式</p>
              <p class="info-value">{{ isTeamCompetition ? '团体赛' : '个人赛' }}（{{ competition.teamSize }}人）</p>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="info-item">
            <el-icon class="info-icon"><Trophy /></el-icon>
            <div>
              <p class="info-label">比赛阶段</p>
              <p class="info-value">{{ competition.phases }}</p>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
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
              <p class="info-value">{{ competition.registered }} 人</p>
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
    <div v-if="competition" class="section">
      <h3 class="section-title">
        <span class="bar"></span>附件下载
      </h3>
      <div class="attachment-list">
        <div
          v-for="(att, idx) in competition.attachments"
          :key="idx"
          class="attachment-item"
          @click="downloadAttachment(att)"
        >
          <el-icon class="att-icon" :size="20" color="#e53e3e"><Document /></el-icon>
          <span class="att-name">{{ att.name }}</span>
          <span class="att-size">{{ att.size }}</span>
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
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.detail-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: $space-4 $space-6 100px;
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
.cover {
  position: relative;
  height: 240px;
  border-radius: $radius-md;
  overflow: hidden;
  margin-bottom: $space-4;
}

.cover-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #1a202c 0%, #2d3748 50%, #b91c1c 100%);
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
.section {
  background: $bg-card;
  border-radius: $radius-md;
  padding: $space-5;
  margin-bottom: $space-4;
  box-shadow: $shadow-sm;
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
  height: 80px;
}

.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: $bg-card;
  border-top: 1px solid $border-light;
  padding: $space-3 $space-6;
  display: flex;
  justify-content: center;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.04);
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
  transition: all $transition-fast;
}

.btn-primary {
  background: linear-gradient(135deg, $primary, $primary-hover);
  color: #fff;
  box-shadow: 0 4px 12px rgba(43, 108, 176, 0.3);

  &:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 6px 16px rgba(43, 108, 176, 0.4);
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

// ===== 响应式 =====
@media (max-width: 768px) {
  .detail-page {
    padding: $space-3 $space-4 100px;
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
