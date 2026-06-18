<script setup lang="ts">
/**
 * 我的团队列表
 * - 展示用户加入/创建的团队
 * - 快捷入口：创建团队 / 加入团队
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getMyTeams, quitTeam } from '@/api/team'
import { getCompetitionDetail } from '@/api/public'

const router = useRouter()
const userStore = useUserStore()

const teams = ref<any[]>([])
const loading = ref(false)
const compTitleMap = ref<Record<number, string>>({})

function getCompTitle(id: number) {
  return compTitleMap.value[id] || `竞赛 #${id}`
}

async function loadTeams() {
  loading.value = true
  try {
    const res: any = await getMyTeams()
    const list = Array.isArray(res) ? res : []
    const roleMap: Record<number, string> = { 1: 'CAPTAIN', 0: 'MEMBER' }

    // 批量获取竞赛名称
    const ids = [...new Set(list.map((i: any) => i.team?.competitionId).filter(Boolean))] as number[]
    const fetches = ids
      .filter((id) => !compTitleMap.value[id])
      .map((id) =>
        getCompetitionDetail(id)
          .then((c: any) => { if (c) compTitleMap.value[id] = c.title })
          .catch(() => {})
      )
    await Promise.all(fetches)

    teams.value = list.map((item: any) => {
      const t = item.team
      const isPending = item.memberStatus === 0
      const teamStatusMap: Record<number, string> = { 0: '待提交', 1: '审核中', 2: '已通过', 3: '已拒绝' }
      return {
        id: t.id,
        title: t.teamName || '团队',
        category: t.competitionId ? getCompTitle(t.competitionId) : '',
        role: roleMap[item.myRole] || 'MEMBER',
        currentCount: item.members?.length || 0,
        maxMembers: t.maxSize || 5,
        inviteCode: t.inviteCode || '',
        status: t.status,
        isPending,
        statusText: isPending ? '待审核' : (teamStatusMap[t.status] || '未知')
      }
    })
  } catch (e) {
    teams.value = []
  } finally {
    loading.value = false
  }
}

function roleText(r: string) {
  return r === 'CAPTAIN' ? '队长' : '队员'
}

function statusType(s: string) {
  const m: any = { '审核中': 'warning', '已通过': 'success', '已拒绝': 'danger', '已提交': 'primary', '待审核': 'warning' }
  return m[s] || 'info'
}

function goCreate() {
  router.push({ name: 'TeamCreate' })
}

function goJoin() {
  router.push({ name: 'TeamJoin' })
}

function goDetail(t: any) {
  if (t.role === 'CAPTAIN') {
    router.push(`/student-center/team-manage/${t.id}`)
  } else {
    router.push(`/student-center/team-detail/${t.id}`)
  }
}

async function handleQuit(t: any) {
  try {
    await ElMessageBox.confirm(`确认退出团队「${t.title}」？`, '提示', {
      type: 'warning',
      confirmButtonText: '确认退出',
      cancelButtonText: '再想想'
    })
    await quitTeam(t.id)
    ElMessage.success('已退出团队')
    teams.value = teams.value.filter((x) => x.id !== t.id)
  } catch {}
}

onMounted(() => {
  loadTeams()
})
</script>

<template>
  <div class="teams-page">
    <div class="page-header">
      <h1 class="page-title">我的团队</h1>
      <div class="user-mini">
        <div class="avatar">{{ userStore.user?.realName?.[0] || 'U' }}</div>
        <span class="user-name">{{ userStore.user?.realName || '用户' }}</span>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="action-bar">
      <button class="btn-primary" @click="goCreate">
        <span class="icon">+</span>创建团队
      </button>
      <button class="btn-secondary" @click="goJoin">
        <span class="icon">→</span>加入团队
      </button>
    </div>

    <!-- 团队列表 -->
    <div v-if="loading" class="loading">
      <el-skeleton :rows="6" animated />
    </div>

    <div v-else-if="teams.length > 0" class="team-list">
      <div v-for="t in teams" :key="t.id" class="team-card">
        <div class="card-top" @click="goDetail(t)">
          <div class="card-main">
            <h3 class="team-title">{{ t.title }}</h3>
            <div class="team-meta">
              <el-tag size="small" effect="plain" round>{{ t.category }}</el-tag>
              <el-tag size="small" effect="plain" type="info" round>{{ t.currentCount }}/{{ t.maxMembers }}人</el-tag>
              <el-tag size="small" effect="plain" :type="t.role === 'CAPTAIN' ? 'warning' : 'info'" round>
                {{ roleText(t.role) }}
              </el-tag>
              <el-tag size="small" :type="statusType(t.statusText) as any" effect="light" round>
                {{ t.statusText }}
              </el-tag>
            </div>
          </div>
        </div>
        <div class="card-actions">
          <template v-if="t.isPending">
            <span class="pending-hint">等待队长审核</span>
          </template>
          <template v-else>
            <button class="btn-link" @click="goDetail(t)">查看详情</button>
            <button v-if="t.role === 'MEMBER'" class="btn-quit" @click="handleQuit(t)">退出团队</button>
          </template>
        </div>
      </div>
    </div>

    <div v-else class="empty">
      <el-empty description="暂无团队" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.teams-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: $space-4 $space-6 $space-8;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $space-5;
}

.page-title {
  margin: 0;
  font-size: $font-size-xl;
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

.action-bar {
  display: flex;
  gap: $space-3;
  margin-bottom: $space-5;
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 6px;
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

  .icon {
    font-size: 18px;
    font-weight: $font-weight-bold;
  }

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 6px 16px rgba(43, 108, 176, 0.35);
  }
}

.btn-secondary {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 40px;
  padding: 0 $space-5;
  border: 1px solid $primary;
  background: $bg-card;
  color: $primary;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  border-radius: $radius-base;
  cursor: pointer;
  transition: all $transition-fast;

  .icon {
    font-size: 18px;
    font-weight: $font-weight-bold;
  }

  &:hover {
    background: $primary-50;
  }
}

.loading {
  background: $bg-card;
  border-radius: $radius-md;
  padding: $space-6;
}

.team-list {
  display: flex;
  flex-direction: column;
  gap: $space-3;
}

.team-card {
  background: $bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;
  padding: $space-4 $space-5;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all $transition-base;

  &:hover {
    box-shadow: $shadow-md;
    transform: translateY(-1px);
  }
}

.card-top {
  flex: 1;
  cursor: pointer;
}

.team-title {
  margin: 0 0 $space-2;
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.team-meta {
  display: flex;
  gap: $space-2;
  flex-wrap: wrap;
}

.card-actions {
  display: flex;
  gap: $space-2;
}

.btn-link {
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

.btn-quit {
  height: 32px;
  padding: 0 $space-4;
  border: 1px solid $danger;
  background: transparent;
  color: $danger;
  font-size: $font-size-sm;
  border-radius: $radius-base;
  cursor: pointer;
  transition: all $transition-fast;
  &:hover {
    background: $danger;
    color: #fff;
  }
}

.pending-hint {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.empty {
  background: $bg-card;
  border-radius: $radius-md;
  padding: $space-10;
}

@media (max-width: 768px) {
  .teams-page {
    padding: $space-3 $space-4;
  }
  .team-card {
    flex-direction: column;
    align-items: flex-start;
    gap: $space-3;
  }
  .card-actions {
    width: 100%;
    justify-content: flex-end;
  }
  .user-mini .user-name {
    display: none;
  }
}
</style>
