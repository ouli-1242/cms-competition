<script setup lang="ts">
/**
 * 团队管理页（队长视角）
 * - 显示团队邀请码
 * - 管理团队成员
 * - 处理加入申请
 */
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { kickMember, reviewMember, getTeamDetail, dissolveTeam, searchTeachers, inviteTeacher, cancelAdvisorInvite } from '@/api/team'
import { getCompetitionDetail } from '@/api/public'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const team = ref<any>(null)
const loading = ref(false)

// 邀请指导老师
const advisor = ref<any>(null)
const pendingInvite = ref<any>(null)
const showInvitePanel = ref(false)
const searchKeyword = ref('')
const teacherList = ref<any[]>([])
const selectedTeacher = ref<any>(null)
const inviteRemark = ref('')
const inviting = ref(false)
const searching = ref(false)

async function loadTeam() {
  loading.value = true
  try {
    const id = Number(route.params.id)
    const res: any = await getTeamDetail(id)
    if (res && res.team) {
      // 获取竞赛名称
      let compTitle = `竞赛 #${res.team.competitionId}`
      if (res.team.competitionId) {
        try {
          const comp: any = await getCompetitionDetail(res.team.competitionId)
          if (comp) compTitle = comp.title
        } catch {}
      }
      const allMembers = (res.members || []).map((m: any) => ({
          memberId: m.id,
          id: m.userId,
          realName: m.realName || m.username || '成员',
          username: m.username || '',
          status: m.status,
          role: m.userId === res.team.captainId ? 'CAPTAIN' : 'MEMBER',
          avatar: (m.realName || m.username || '?')[0]
        }))
      team.value = {
        id: res.team.id,
        title: res.team.teamName || '团队',
        category: res.team.competitionId ? compTitle : '',
        currentCount: allMembers.filter((m: any) => m.status === 1).length,
        maxMembers: res.team.maxSize || 5,
        inviteCode: res.team.inviteCode || '',
        members: allMembers.filter((m: any) => m.status === 1),
        applications: allMembers.filter((m: any) => m.status === 0)
      }
      advisor.value = res.advisor || null
      pendingInvite.value = res.pendingInvite || null
    } else {
      team.value = null
      ElMessage.error('团队不存在')
    }
  } catch (e) {
    team.value = null
  } finally {
    loading.value = false
  }
}

function copyCode() {
  if (team.value?.inviteCode) {
    navigator.clipboard?.writeText(team.value.inviteCode)
    ElMessage.success('邀请码已复制')
  }
}

async function handleRemoveMember(member: any) {
  if (member.role === 'CAPTAIN') {
    ElMessage.warning('队长不能被移除')
    return
  }
  try {
    await ElMessageBox.confirm(`确认移除成员「${member.realName}」？`, '提示', {
      type: 'warning',
      confirmButtonText: '确认移除',
      cancelButtonText: '取消'
    })
    await kickMember(team.value.id, member.id)
    team.value.members = team.value.members.filter((m: any) => m.id !== member.id)
    team.value.currentCount--
    ElMessage.success('已移除')
  } catch {}
}

async function handleAudit(app: any, approve: boolean) {
  try {
    await reviewMember(app.memberId, approve)
    team.value.applications = team.value.applications.filter((a: any) => a.memberId !== app.memberId)
    if (approve) {
      team.value.members.push({ ...app, role: 'MEMBER' })
      team.value.currentCount++
    }
    ElMessage.success(approve ? '已通过' : '已拒绝')
  } catch (e) {}
}

async function handleDissolve() {
  try {
    await ElMessageBox.confirm(
      '解散后所有成员将被移出，团队数据不可恢复。确认解散吗？',
      '解散团队',
      { type: 'warning', confirmButtonText: '确认解散', cancelButtonText: '取消' }
    )
    await dissolveTeam(team.value.id)
    ElMessage.success('团队已解散')
    router.push('/student-center/my-teams')
  } catch {}
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
  <div class="manage-page">
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
      <span class="current">团队管理</span>
    </div>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="6" animated />
    </div>

    <template v-else-if="team">
      <!-- 团队信息 -->
      <div class="info-card">
        <div class="info-header">
          <div>
            <h2 class="team-title">{{ team.title }}</h2>
            <div class="team-meta">
              <el-tag size="small" effect="plain" round>{{ team.category }}</el-tag>
              <el-tag size="small" effect="plain" type="info" round>{{ team.currentCount }}/{{ team.maxMembers }}人</el-tag>
            </div>
          </div>
          <div class="invite-box">
            <p class="invite-label">团队邀请码</p>
            <div class="invite-code" @click="copyCode">
              <span class="code">{{ team.inviteCode }}</span>
              <span class="copy-icon">复制</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 指导老师 -->
      <div class="section">
        <div class="section-header">
          <h3 class="section-title">指导老师</h3>
        </div>

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
          <div class="advisor-actions">
            <el-tag type="warning" size="small" effect="plain" round>待确认</el-tag>
            <button class="cancel-btn" @click="handleCancelInvite">撤销</button>
          </div>
        </div>

        <!-- 无指导老师 -->
        <div v-else class="no-advisor">
          <p class="no-advisor-text">暂无指导老师</p>
          <button v-if="!showInvitePanel" class="invite-btn" @click="showInvitePanel = true; handleSearchTeachers()">
            邀请指导老师
          </button>
        </div>

        <!-- 邀请面板 -->
        <div v-if="showInvitePanel" class="invite-panel">
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

      <!-- 团队成员 -->
      <div class="section">
        <div class="section-header">
          <h3 class="section-title">团队成员</h3>
          <span class="section-extra">共 {{ team.members.length }} 人</span>
        </div>
        <div class="member-list">
          <div v-for="m in team.members" :key="m.id" class="member-item">
            <div class="member-avatar" :class="{ captain: m.role === 'CAPTAIN' }">
              {{ m.avatar || m.realName[0] }}
            </div>
            <div class="member-info">
              <div class="member-name">
                <span v-if="m.role === 'CAPTAIN'" class="captain-tag">队长</span>
                {{ m.realName }}
              </div>
              <div class="member-id">学号 {{ m.username }}</div>
            </div>
            <button
              v-if="m.role !== 'CAPTAIN'"
              class="btn-remove"
              @click="handleRemoveMember(m)"
            >
              移除
            </button>
          </div>
        </div>
      </div>

      <!-- 加入申请 -->
      <div v-if="team.applications && team.applications.length > 0" class="section">
        <div class="section-header">
          <h3 class="section-title">加入申请</h3>
          <span class="section-extra">{{ team.applications.length }} 人待审核</span>
        </div>
        <div class="app-list">
          <div v-for="app in team.applications" :key="app.id" class="app-item">
            <div class="app-avatar">{{ app.avatar || app.realName[0] }}</div>
            <div class="app-info">
              <div class="app-name">{{ app.realName }}</div>
              <div class="app-id">学号 {{ app.username }}</div>
            </div>
            <div class="app-actions">
              <button class="btn-reject" @click="handleAudit(app, false)">拒绝</button>
              <button class="btn-approve" @click="handleAudit(app, true)">通过</button>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="empty-hint">
        <el-alert type="info" :closable="false" show-icon>
          <template #title>暂无加入申请</template>
        </el-alert>
      </div>

      <div class="bottom-actions">
        <button class="btn-danger" @click="handleDissolve">解散团队</button>
        <button class="btn-cancel" @click="router.back()">返回</button>
        <button class="btn-primary" @click="router.push(`/student-center/team-submit/${team.id}`)">
          提交团队报名
        </button>
      </div>
    </template>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.manage-page {
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

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: $space-4;
}

.team-title {
  margin: 0 0 $space-2;
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.team-meta {
  display: flex;
  gap: $space-2;
}

.invite-box {
  text-align: right;
}

.invite-label {
  margin: 0 0 $space-1;
  font-size: $font-size-xs;
  color: $text-secondary;
}

.invite-code {
  display: inline-flex;
  align-items: center;
  gap: $space-2;
  padding: $space-2 $space-4;
  background: $primary-50;
  border: 1px dashed $primary;
  border-radius: $radius-base;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover {
    background: $primary;
    .code, .copy-icon {
      color: #fff;
    }
  }
}

.code {
  font-family: 'Consolas', monospace;
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  color: $primary;
  letter-spacing: 2px;
}

.copy-icon {
  font-size: 16px;
  color: $primary;
}

.section {
  background: $bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;
  padding: $space-5;
  margin-bottom: $space-4;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $space-3;
  padding-bottom: $space-3;
  border-bottom: 1px solid $border-light;
  margin: (-$space-5) (-$space-5) $space-3;
  padding: $space-4 $space-5;
}

.section-title {
  margin: 0;
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.section-extra {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.member-list,
.app-list {
  display: flex;
  flex-direction: column;
}

.member-item,
.app-item {
  display: flex;
  align-items: center;
  gap: $space-3;
  padding: $space-3 0;
  border-bottom: 1px solid $border-light;
  &:last-child {
    border-bottom: none;
  }
}

.member-avatar,
.app-avatar {
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

.member-info,
.app-info {
  flex: 1;
}

.member-name,
.app-name {
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

.member-id,
.app-id {
  font-size: $font-size-xs;
  color: $text-secondary;
}

.btn-remove {
  padding: 4px 12px;
  border: 1px solid $danger;
  background: transparent;
  color: $danger;
  font-size: $font-size-xs;
  border-radius: $radius-sm;
  cursor: pointer;
  transition: all $transition-fast;
  &:hover {
    background: $danger;
    color: #fff;
  }
}

.app-actions {
  display: flex;
  gap: $space-2;
}

.btn-reject {
  padding: 6px 14px;
  border: 1px solid $border-base;
  background: $bg-card;
  color: $text-regular;
  font-size: $font-size-sm;
  border-radius: $radius-base;
  cursor: pointer;
  transition: all $transition-fast;
  &:hover {
    border-color: $danger;
    color: $danger;
  }
}

.btn-approve {
  padding: 6px 14px;
  border: none;
  background: linear-gradient(135deg, $primary, $primary-hover);
  color: #fff;
  font-size: $font-size-sm;
  border-radius: $radius-base;
  cursor: pointer;
  transition: all $transition-fast;
  &:hover {
    transform: translateY(-1px);
  }
}

.empty-hint {
  margin-bottom: $space-4;
}

.bottom-actions {
  display: flex;
  justify-content: flex-end;
  gap: $space-3;
  margin-top: $space-4;
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
}

.btn-danger {
  height: 40px;
  padding: 0 $space-5;
  border: 1px solid $danger;
  background: transparent;
  color: $danger;
  font-size: $font-size-base;
  border-radius: $radius-base;
  cursor: pointer;
  transition: all $transition-fast;
  &:hover {
    background: $danger;
    color: #fff;
  }
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

@media (max-width: 768px) {
  .manage-page {
    padding: $space-3 $space-4;
  }
  .info-header {
    flex-direction: column;
  }
  .invite-box {
    text-align: left;
  }
  .user-mini .user-name {
    display: none;
  }
}
</style>
