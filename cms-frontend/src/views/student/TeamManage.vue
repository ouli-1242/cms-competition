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
import { getTeamDetail, removeMember, auditJoinRequest } from '@/api/team'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const team = ref<any>(null)
const loading = ref(false)

async function loadTeam() {
  loading.value = true
  try {
    const id = Number(route.params.id || 1)
    const data = await getTeamDetail(id)
    team.value = data
  } catch (e) {
    // 占位数据
    team.value = {
      id: 1,
      title: '全国大学生数学建模竞赛',
      category: '校级赛',
      minMembers: 2,
      maxMembers: 3,
      currentCount: 2,
      inviteCode: 'A3B7K9',
      members: [
        { id: 101, realName: '张三', username: '2024001', role: 'CAPTAIN', avatar: '张' },
        { id: 102, realName: '李四', username: '2024002', role: 'MEMBER', avatar: '李' }
      ],
      applications: [
        { id: 201, realName: '王五', username: '2024003', avatar: '王' }
      ]
    }
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
    try {
      await removeMember({ teamId: team.value.id, userId: member.id })
    } catch (e) {}
    team.value.members = team.value.members.filter((m: any) => m.id !== member.id)
    team.value.currentCount--
    ElMessage.success('已移除')
  } catch {}
}

async function handleAudit(app: any, approve: boolean) {
  try {
    await auditJoinRequest({ requestId: app.id, approve })
    team.value.applications = team.value.applications.filter((a: any) => a.id !== app.id)
    if (approve) {
      team.value.members.push({ ...app, role: 'MEMBER' })
      team.value.currentCount++
    }
    ElMessage.success(approve ? '已通过' : '已拒绝')
  } catch (e) {}
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
        <div class="avatar">{{ userStore.realName?.[0] || 'U' }}</div>
        <span class="user-name">{{ userStore.realName || '用户' }}</span>
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
              <el-tag size="small" effect="plain" round>📋 {{ team.category }}</el-tag>
              <el-tag size="small" effect="plain" type="info" round>👥 {{ team.currentCount }}/{{ team.maxMembers }}人</el-tag>
            </div>
          </div>
          <div class="invite-box">
            <p class="invite-label">团队邀请码</p>
            <div class="invite-code" @click="copyCode">
              <span class="code">{{ team.inviteCode }}</span>
              <span class="copy-icon">📋</span>
            </div>
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
                <span v-if="m.role === 'CAPTAIN'" class="captain-tag">👑 队长</span>
                <span v-else>👤</span>
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
