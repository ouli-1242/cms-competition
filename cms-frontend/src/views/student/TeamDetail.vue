<script setup lang="ts">
/**
 * 团队信息查看页
 * - 展示团队信息
 * - 展示团队成员 + 状态
 */
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getTeamDetail } from '@/api/team'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const team = ref<any>(null)
const loading = ref(false)

async function loadTeam() {
  loading.value = true
  try {
    const id = Number(route.params.id)
    const data = await getTeamDetail(id)
    team.value = data
  } catch (e) {
    team.value = {
      id: Number(route.params.id),
      title: '全国大学生数学建模竞赛',
      category: '校级赛',
      currentCount: 3,
      maxMembers: 3,
      minMembers: 2,
      inviteCode: 'A3B7K9',
      status: 2,
      statusText: '审核中',
      members: [
        { realName: '张三', username: '2024001', role: 'CAPTAIN', status: '已确认' },
        { realName: '李四', username: '2024002', role: 'MEMBER', status: '已确认' },
        { realName: '王五', username: '2024003', role: 'MEMBER', status: '已确认' }
      ]
    }
  } finally {
    loading.value = false
  }
}

function statusType(status: number | string): string {
  const map: any = { '审核中': 'warning', '已通过': 'success', '已拒绝': 'danger', '已提交': 'primary' }
  return map[status as string] || 'info'
}

onMounted(() => {
  loadTeam()
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
        <div class="avatar">{{ userStore.realName?.[0] || 'U' }}</div>
        <span class="user-name">{{ userStore.realName || '用户' }}</span>
      </div>
    </div>

    <div class="breadcrumb">
      <span @click="router.push('/student/profile')">个人中心</span>
      <span class="sep">›</span>
      <span @click="router.push('/student-center/my-teams')">我的团队</span>
      <span class="sep">›</span>
      <span class="current">团队详情</span>
    </div>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="6" animated />
    </div>

    <template v-else-if="team">
      <div class="info-card">
        <h2 class="team-title">{{ team.title }}</h2>
        <div class="team-meta">
          <el-tag size="small" effect="plain" round>📋 比赛形式：{{ team.category }}</el-tag>
          <el-tag size="small" effect="plain" type="info" round>👥 {{ team.currentCount }}/{{ team.maxMembers }}人</el-tag>
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

      <div class="section">
        <h3 class="section-title">团队成员</h3>
        <div class="member-list">
          <div v-for="(m, i) in team.members" :key="i" class="member-item">
            <div class="member-avatar" :class="{ captain: m.role === 'CAPTAIN' }">
              {{ m.realName[0] }}
            </div>
            <div class="member-info">
              <div class="member-name">
                <span v-if="m.role === 'CAPTAIN'" class="captain-tag">👑 队长</span>
                <span v-else class="member-tag">👤</span>
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
}
</style>
