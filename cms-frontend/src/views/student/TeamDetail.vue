<script setup lang="ts">
/**
 * 团队信息查看页
 * - 展示团队信息 + 指导老师
 * - 展示团队成员 + 状态
 * - 队长可邀请/撤销指导老师
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getTeamDetail, searchTeachers, inviteTeacher, cancelAdvisorInvite } from '@/api/team'
import { getCompetitionDetail } from '@/api/public'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const team = ref<any>(null)
const advisor = ref<any>(null)
const pendingInvite = ref<any>(null)
const loading = ref(false)

// 邀请指导老师
const showInvitePanel = ref(false)
const searchKeyword = ref('')
const teacherList = ref<any[]>([])
const selectedTeacher = ref<any>(null)
const inviteRemark = ref('')
const inviting = ref(false)
const searching = ref(false)

const isCaptain = computed(() => {
  return team.value?.captainId === userStore.user?.id
})

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
          compTitle = comp?.title || ''
        } catch {}
      }
      const members = (res.members || []).map((m: any) => ({
        realName: m.realName || m.username || '成员',
        username: m.username || '',
        role: m.userId === t.captainId ? 'CAPTAIN' : 'MEMBER',
        status: m.status === 1 ? '已确认' : '待审核'
      }))
      const statusMap: Record<number, string> = { 0: '待提交', 1: '审核中', 2: '已通过', 3: '已拒绝' }
      team.value = {
        id: t.id,
        title: t.teamName || '团队',
        category: compTitle,
        currentCount: members.length,
        maxMembers: t.maxSize || 5,
        inviteCode: t.inviteCode || '',
        status: t.status,
        statusText: statusMap[t.status] || '未知',
        captainId: t.captainId,
        members
      }
      advisor.value = res.advisor || null
      pendingInvite.value = res.pendingInvite || null
    } else {
      team.value = null
    }
  } catch (e) {
    team.value = null
  } finally {
    loading.value = false
  }
}

function statusType(status: number | string): string {
  const map: any = { '审核中': 'warning', '已通过': 'success', '已拒绝': 'danger', '已提交': 'primary' }
  return map[status as string] || 'info'
}

async function handleSearchTeachers() {
  searching.value = true
  try {
    const res: any = await searchTeachers(searchKeyword.value || undefined)
    teacherList.value = res || []
  } catch {
    teacherList.value = []
  } finally {
    searching.value = false
  }
}

function selectTeacher(t: any) {
  selectedTeacher.value = t
}

async function handleInvite() {
  if (!selectedTeacher.value || !team.value) return
  inviting.value = true
  try {
    await inviteTeacher(team.value.id, selectedTeacher.value.id, inviteRemark.value || undefined)
    ElMessage.success('邀请已发送')
    showInvitePanel.value = false
    selectedTeacher.value = null
    inviteRemark.value = ''
    searchKeyword.value = ''
    teacherList.value = []
    await loadTeam()
  } catch {
    // error handled by interceptor
  } finally {
    inviting.value = false
  }
}

async function handleCancelInvite() {
  if (!pendingInvite.value) return
  try {
    await ElMessageBox.confirm('确定撤销对该老师的邀请吗？', '撤销邀请', { type: 'warning' })
    await cancelAdvisorInvite(pendingInvite.value.id)
    ElMessage.success('已撤销')
    pendingInvite.value = null
  } catch {
    // cancelled
  }
}

onMounted(() => {
  loadTeam()
})
</script>

<template>
  <div class="detail-page">
    <div class="page-header">
      <button class="back-btn" @click="router.back()">
        <span>&larr;</span>
      </button>
      <h1 class="page-title">高校学科竞赛报名管理系统</h1>
      <div class="user-mini">
        <div class="avatar">{{ userStore.user?.realName?.[0] || 'U' }}</div>
        <span class="user-name">{{ userStore.user?.realName || '用户' }}</span>
      </div>
    </div>

    <div class="breadcrumb">
      <span @click="router.push('/student/profile')">个人中心</span>
      <span class="sep">&rsaquo;</span>
      <span @click="router.push('/student-center/my-teams')">我的团队</span>
      <span class="sep">&rsaquo;</span>
      <span class="current">团队详情</span>
    </div>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="6" animated />
    </div>

    <template v-else-if="team">
      <div class="info-card">
        <h2 class="team-title">{{ team.title }}</h2>
        <div class="team-meta">
          <el-tag size="small" effect="plain" round>比赛：{{ team.category }}</el-tag>
          <el-tag size="small" effect="plain" type="info" round>{{ team.currentCount }}/{{ team.maxMembers }}人</el-tag>
        </div>

        <div class="invite-row">
          <div class="invite-left">
            <p class="invite-label">团队邀请码（仅供内部使用）</p>
            <div class="invite-code">
              <span class="code">{{ team.inviteCode }}</span>
            </div>
          </div>
          <div class="status-right">
            <p class="status-label">报名状态：</p>
            <el-tag :type="statusType(team.statusText) as any" effect="light" round>
              {{ team.statusText }}
            </el-tag>
          </div>
        </div>
      </div>

      <!-- 指导老师 -->
      <div class="section">
        <h3 class="section-title">指导老师</h3>

        <!-- 已有指导老师 -->
        <div v-if="advisor" class="advisor-info">
          <div class="advisor-avatar teacher">
            {{ advisor.teacherName?.[0] || '师' }}
          </div>
          <div class="advisor-detail">
            <div class="advisor-name">{{ advisor.teacherName }}</div>
            <div class="advisor-college">{{ advisor.teacherCollege || '教师' }}</div>
          </div>
          <el-tag type="success" size="small" effect="plain" round>已确认</el-tag>
        </div>

        <!-- 待审核的邀请 -->
        <div v-else-if="pendingInvite" class="advisor-info">
          <div class="advisor-avatar pending">
            {{ pendingInvite.teacherName?.[0] || '师' }}
          </div>
          <div class="advisor-detail">
            <div class="advisor-name">{{ pendingInvite.teacherName }}</div>
            <div class="advisor-college">等待老师确认邀请</div>
          </div>
          <div class="advisor-actions" v-if="isCaptain">
            <el-tag type="warning" size="small" effect="plain" round>待确认</el-tag>
            <button class="cancel-btn" @click="handleCancelInvite">撤销</button>
          </div>
          <el-tag v-else type="warning" size="small" effect="plain" round>待确认</el-tag>
        </div>

        <!-- 无指导老师 -->
        <div v-else class="no-advisor">
          <p class="no-advisor-text">暂无指导老师</p>
          <button v-if="isCaptain && !showInvitePanel" class="invite-btn" @click="showInvitePanel = true; handleSearchTeachers()">
            邀请指导老师
          </button>
        </div>

        <!-- 邀请面板（仅队长） -->
        <div v-if="showInvitePanel && isCaptain" class="invite-panel">
          <div class="search-row">
            <input
              v-model="searchKeyword"
              class="search-input"
              placeholder="搜索教师姓名或学院"
              @keyup.enter="handleSearchTeachers"
            />
            <button class="search-btn" @click="handleSearchTeachers" :disabled="searching">
              {{ searching ? '搜索中...' : '搜索' }}
            </button>
          </div>

          <div v-if="teacherList.length > 0" class="teacher-list">
            <div
              v-for="t in teacherList"
              :key="t.id"
              class="teacher-item"
              :class="{ selected: selectedTeacher?.id === t.id }"
              @click="selectTeacher(t)"
            >
              <div class="teacher-avatar">{{ t.realName?.[0] || '师' }}</div>
              <div class="teacher-info">
                <div class="teacher-name">{{ t.realName }}</div>
                <div class="teacher-college">{{ t.college || '教师' }}</div>
              </div>
              <span v-if="selectedTeacher?.id === t.id" class="check-mark">&#10003;</span>
            </div>
          </div>
          <p v-else-if="!searching" class="no-result">未找到教师，请尝试其他关键词</p>

          <div v-if="selectedTeacher" class="remark-row">
            <textarea
              v-model="inviteRemark"
              class="remark-input"
              placeholder="邀请备注（可选）"
              maxlength="200"
              rows="2"
            ></textarea>
          </div>

          <div class="invite-actions">
            <button class="confirm-btn" :disabled="!selectedTeacher || inviting" @click="handleInvite">
              {{ inviting ? '发送中...' : '发送邀请' }}
            </button>
            <button class="cancel-panel-btn" @click="showInvitePanel = false; selectedTeacher = null; teacherList = []">
              取消
            </button>
          </div>
        </div>
      </div>

      <div class="section">
        <h3 class="section-title">团队成员</h3>
        <div class="member-list">
          <div v-for="(m, i) in team.members" :key="i" class="member-item">
            <div class="member-avatar" :class="{ captain: m.role === 'CAPTAIN' }">
              {{ m.realName[0] }}
            </div>
            <div class="member-info">
              <div class="member-name">
                <span v-if="m.role === 'CAPTAIN'" class="captain-tag">队长</span>
                {{ m.realName }}
              </div>
              <div class="member-id">学号 {{ m.username }}</div>
            </div>
            <el-tag size="small" type="success" effect="plain" round>{{ m.status }}</el-tag>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.detail-page {
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
  margin-bottom: $space-5;
}

.invite-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: $space-4;
  padding-top: $space-4;
  border-top: 1px dashed $border-light;
}

.invite-label {
  margin: 0 0 $space-1;
  font-size: $font-size-xs;
  color: $text-secondary;
}

.invite-code {
  display: inline-flex;
  align-items: center;
  padding: $space-2 $space-5;
  background: $primary-50;
  border-radius: $radius-base;
}

.code {
  font-family: 'Consolas', monospace;
  font-size: 22px;
  font-weight: $font-weight-semibold;
  color: $primary;
  letter-spacing: 3px;
}

.status-right {
  text-align: right;
}

.status-label {
  margin: 0 0 $space-1;
  font-size: $font-size-xs;
  color: $text-secondary;
}

.section {
  background: $bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;
  padding: $space-5;
  margin-bottom: $space-4;
}

.section-title {
  margin: 0 0 $space-3;
  padding-bottom: $space-3;
  border-bottom: 1px solid $border-light;
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

// 指导老师
.advisor-info {
  display: flex;
  align-items: center;
  gap: $space-3;
  padding: $space-3 0;
}

.advisor-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-size-lg;
  font-weight: $font-weight-medium;
  flex-shrink: 0;
  &.teacher {
    background: linear-gradient(135deg, #48bb78, #2f855a);
    color: #fff;
  }
  &.pending {
    background: linear-gradient(135deg, #f6ad55, #ed8936);
    color: #fff;
  }
}

.advisor-detail {
  flex: 1;
}

.advisor-name {
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $text-primary;
  margin-bottom: 2px;
}

.advisor-college {
  font-size: $font-size-xs;
  color: $text-secondary;
}

.advisor-actions {
  display: flex;
  align-items: center;
  gap: $space-2;
}

.cancel-btn {
  border: 1px solid $danger;
  color: $danger;
  background: transparent;
  padding: 4px 12px;
  border-radius: $radius-sm;
  font-size: $font-size-xs;
  cursor: pointer;
  transition: all $transition-fast;
  &:hover {
    background: $danger;
    color: #fff;
  }
}

.no-advisor {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $space-2 0;
}

.no-advisor-text {
  margin: 0;
  font-size: $font-size-sm;
  color: $text-secondary;
}

.invite-btn {
  background: $primary;
  color: #fff;
  border: none;
  padding: $space-2 $space-4;
  border-radius: $radius-base;
  font-size: $font-size-sm;
  cursor: pointer;
  transition: background $transition-fast;
  &:hover {
    background: $primary-hover;
  }
}

.invite-panel {
  margin-top: $space-4;
  padding-top: $space-4;
  border-top: 1px dashed $border-light;
}

.search-row {
  display: flex;
  gap: $space-2;
  margin-bottom: $space-3;
}

.search-input {
  flex: 1;
  padding: $space-2 $space-3;
  border: 1px solid $border-light;
  border-radius: $radius-base;
  font-size: $font-size-sm;
  outline: none;
  transition: border-color $transition-fast;
  &:focus {
    border-color: $primary;
  }
}

.search-btn {
  background: $primary;
  color: #fff;
  border: none;
  padding: $space-2 $space-4;
  border-radius: $radius-base;
  font-size: $font-size-sm;
  cursor: pointer;
  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
  &:hover:not(:disabled) {
    background: $primary-hover;
  }
}

.teacher-list {
  max-height: 240px;
  overflow-y: auto;
  margin-bottom: $space-3;
}

.teacher-item {
  display: flex;
  align-items: center;
  gap: $space-3;
  padding: $space-2 $space-3;
  border: 1px solid $border-light;
  border-radius: $radius-base;
  margin-bottom: $space-2;
  cursor: pointer;
  transition: all $transition-fast;
  &:hover {
    border-color: $primary;
    background: $primary-50;
  }
  &.selected {
    border-color: $primary;
    background: $primary-50;
  }
}

.teacher-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #48bb78, #2f855a);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  flex-shrink: 0;
}

.teacher-info {
  flex: 1;
}

.teacher-name {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $text-primary;
}

.teacher-college {
  font-size: $font-size-xs;
  color: $text-secondary;
}

.check-mark {
  color: $primary;
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
}

.no-result {
  font-size: $font-size-sm;
  color: $text-placeholder;
  text-align: center;
  padding: $space-3 0;
  margin: 0;
}

.remark-row {
  margin-bottom: $space-3;
}

.remark-input {
  width: 100%;
  padding: $space-2 $space-3;
  border: 1px solid $border-light;
  border-radius: $radius-base;
  font-size: $font-size-sm;
  resize: vertical;
  outline: none;
  font-family: inherit;
  &:focus {
    border-color: $primary;
  }
}

.invite-actions {
  display: flex;
  gap: $space-2;
}

.confirm-btn {
  background: $primary;
  color: #fff;
  border: none;
  padding: $space-2 $space-5;
  border-radius: $radius-base;
  font-size: $font-size-sm;
  cursor: pointer;
  transition: background $transition-fast;
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
  &:hover:not(:disabled) {
    background: $primary-hover;
  }
}

.cancel-panel-btn {
  background: transparent;
  color: $text-secondary;
  border: 1px solid $border-light;
  padding: $space-2 $space-5;
  border-radius: $radius-base;
  font-size: $font-size-sm;
  cursor: pointer;
  transition: all $transition-fast;
  &:hover {
    border-color: $text-secondary;
    color: $text-primary;
  }
}

// 团队成员
.member-list {
  display: flex;
  flex-direction: column;
}

.member-item {
  display: flex;
  align-items: center;
  gap: $space-3;
  padding: $space-3 0;
  border-bottom: 1px solid $border-light;
  &:last-child {
    border-bottom: none;
  }
}

.member-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, $primary, $primary-hover);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-size-md;
  font-weight: $font-weight-medium;
  flex-shrink: 0;
  &.captain {
    background: linear-gradient(135deg, #f6ad55, #ed8936);
  }
}

.member-info {
  flex: 1;
}

.member-name {
  font-size: $font-size-base;
  color: $text-primary;
  font-weight: $font-weight-medium;
  margin-bottom: 2px;
  display: flex;
  align-items: center;
  gap: $space-1;
}

.captain-tag {
  font-size: $font-size-xs;
  color: $warning;
  background: #fff5e6;
  padding: 2px 6px;
  border-radius: $radius-sm;
}

.member-tag {
  font-size: $font-size-sm;
}

.member-id {
  font-size: $font-size-xs;
  color: $text-secondary;
}

@media (max-width: 768px) {
  .detail-page {
    padding: $space-3 $space-4;
  }
  .invite-row {
    flex-direction: column;
    align-items: flex-start;
  }
  .status-right {
    text-align: left;
  }
  .user-mini .user-name {
    display: none;
  }
  .advisor-info {
    flex-wrap: wrap;
  }
}
</style>
