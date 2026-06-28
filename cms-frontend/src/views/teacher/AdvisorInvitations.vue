<script setup lang="ts">
/**
 * 指导邀请管理（教师）
 * - Tab：邀请列表 / 我的团队
 * - 邀请列表：查看/接受/拒绝指导邀请
 * - 我的团队：已接受指导的团队列表
 */
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAdvisorInvitations,
  acceptAdvisorInvitation,
  rejectAdvisorInvitation,
  getAdvisedTeams
} from '@/api/registration'

const activeTab = ref('invitations')
const loading = ref(false)

// 邀请列表
const invitations = ref<any[]>([])
const filterStatus = ref<string | number>('')

// 我的团队
const teams = ref<any[]>([])

async function loadInvitations() {
  loading.value = true
  try {
    const status = filterStatus.value !== '' ? Number(filterStatus.value) : undefined
    const res: any = await getAdvisorInvitations(status)
    invitations.value = res || []
  } catch {
    invitations.value = []
  } finally {
    loading.value = false
  }
}


async function loadTeams() {
  loading.value = true
  try {
    const res: any = await getAdvisedTeams()
    teams.value = res || []
  } catch {
    teams.value = []
  } finally {
    loading.value = false
  }
}

function loadData() {
  if (activeTab.value === 'invitations') loadInvitations()
  else loadTeams()
}

async function handleAccept(inv: any) {
  try {
    await ElMessageBox.confirm(`确定接受「${inv.teamName}」的指导邀请吗？`, '接受邀请', { type: 'info' })
    await acceptAdvisorInvitation(inv.id)
    ElMessage.success('已接受')
    loadInvitations()
  } catch {
    // cancelled
  }
}

async function handleReject(inv: any) {
  try {
    await ElMessageBox.confirm(`确定拒绝「${inv.teamName}」的指导邀请吗？`, '拒绝邀请', { type: 'warning' })
    await rejectAdvisorInvitation(inv.id)
    ElMessage.success('已拒绝')
    loadInvitations()
  } catch {
    // cancelled
  }
}

function handleSearch() {
  loadInvitations()
}

function handleReset() {
  filterStatus.value = ''
  loadInvitations()
}

function statusText(s: number) {
  return s === 0 ? '待审核' : s === 1 ? '已接受' : '已拒绝'
}

function statusType(s: number) {
  return s === 0 ? 'warning' : s === 1 ? 'success' : 'danger'
}

function teamStatusText(s: number) {
  const map: Record<number, string> = { 0: '组建中', 1: '审核中', 2: '已通过' }
  return map[s] || '未知'
}

function teamStatusType(s: number) {
  return s === 0 ? 'info' : s === 1 ? 'warning' : 'success'
}

function formatTime(t: string) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">指导邀请</h2>
    </div>

    <!-- Tab 切换 -->
    <div class="tab-bar">
      <button class="tab-item" :class="{ active: activeTab === 'invitations' }" @click="activeTab = 'invitations'; loadData()">
        邀请列表
      </button>
      <button class="tab-item" :class="{ active: activeTab === 'teams' }" @click="activeTab = 'teams'; loadData()">
        我的团队
      </button>
    </div>

    <!-- 邀请列表 -->
    <template v-if="activeTab === 'invitations'">
      <div class="filter-bar">
        <div class="filter-item">
          <select v-model="filterStatus" class="form-input form-select" @change="handleSearch">
            <option value="">全部状态</option>
            <option value="0">待审核</option>
            <option value="1">已接受</option>
            <option value="2">已拒绝</option>
          </select>
        </div>
        <div class="filter-actions">
          <button class="btn-search" @click="handleSearch">搜索</button>
          <button class="btn-reset" @click="handleReset">重置</button>
        </div>
      </div>

      <div v-if="loading" class="loading-box">
        <el-skeleton :rows="4" animated />
      </div>

      <div v-else-if="invitations.length === 0" class="empty">
        <p>暂无邀请记录</p>
      </div>

      <div v-else class="card-list">
        <div v-for="inv in invitations" :key="inv.id" class="invite-card">
          <div class="card-header">
            <div class="card-team">{{ inv.teamName }}</div>
            <el-tag :type="statusType(inv.status) as any" size="small" effect="plain" round>
              {{ statusText(inv.status) }}
            </el-tag>
          </div>
          <div class="card-body">
            <div class="card-row">
              <span class="card-label">竞赛：</span>
              <span>{{ inv.competitionName || '-' }}</span>
            </div>
            <div class="card-row">
              <span class="card-label">邀请人：</span>
              <span>{{ inv.inviterName || '-' }}</span>
            </div>
            <div class="card-row">
              <span class="card-label">邀请时间：</span>
              <span>{{ formatTime(inv.inviteTime) }}</span>
            </div>
            <div v-if="inv.remark" class="card-row">
              <span class="card-label">备注：</span>
              <span>{{ inv.remark }}</span>
            </div>
          </div>
          <div v-if="inv.status === 0" class="card-actions">
            <button class="btn-accept" @click="handleAccept(inv)">接受</button>
            <button class="btn-reject" @click="handleReject(inv)">拒绝</button>
          </div>
        </div>
      </div>
    </template>

    <!-- 我的团队 -->
    <template v-if="activeTab === 'teams'">
      <div v-if="loading" class="loading-box">
        <el-skeleton :rows="4" animated />
      </div>

      <div v-else-if="teams.length === 0" class="empty">
        <p>暂无指导的团队</p>
      </div>

      <el-table v-else :data="teams" stripe style="width: 100%">
        <el-table-column prop="teamName" label="团队名称" min-width="140" />
        <el-table-column prop="competitionName" label="所属竞赛" min-width="180" />
        <el-table-column prop="captainName" label="队长" width="100" />
        <el-table-column label="成员" width="80" align="center">
          <template #default="{ row }">
            {{ row.memberCount }}/{{ row.maxSize }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="teamStatusType(row.status) as any" size="small" effect="plain" round>
              {{ teamStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </template>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.page {
  max-width: 1200px;
}

.page-header {
  margin-bottom: $space-4;
}

.page-title {
  margin: 0;
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.tab-bar {
  display: flex;
  background: $bg-card;
  border-radius: $radius-md;
  padding: 4px;
  box-shadow: $shadow-sm;
  margin-bottom: $space-4;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: $space-2 $space-5;
  border: none;
  background: transparent;
  font-size: $font-size-base;
  color: $text-secondary;
  cursor: pointer;
  border-radius: $radius-base;
  transition: all $transition-base;
  &:hover { color: $primary; background: $primary-50; }
  &.active {
    background: $primary;
    color: #fff;
    font-weight: $font-weight-semibold;
    box-shadow: 0 2px 8px rgba(43, 108, 176, 0.3);
  }
}

.filter-bar {
  display: flex;
  gap: $space-3;
  align-items: center;
  margin-bottom: $space-4;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
}

.form-input {
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

.form-select {
  min-width: 140px;
  background: $bg-card;
  cursor: pointer;
}

.filter-actions {
  display: flex;
  gap: $space-2;
}

.btn-search {
  background: $primary;
  color: #fff;
  border: none;
  padding: $space-2 $space-4;
  border-radius: $radius-base;
  font-size: $font-size-sm;
  cursor: pointer;
  &:hover { background: $primary-hover; }
}

.btn-reset {
  background: transparent;
  color: $text-secondary;
  border: 1px solid $border-light;
  padding: $space-2 $space-4;
  border-radius: $radius-base;
  font-size: $font-size-sm;
  cursor: pointer;
  &:hover { border-color: $text-secondary; color: $text-primary; }
}

.loading-box {
  background: $bg-card;
  border-radius: $radius-md;
  padding: $space-5;
}

.empty {
  text-align: center;
  padding: $space-10 0;
  color: $text-placeholder;
  font-size: $font-size-base;
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: $space-3;
}

.invite-card {
  background: $bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;
  padding: $space-4 $space-5;
  border: 1px solid $border-light;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $space-3;
}

.card-team {
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: $space-1;
  margin-bottom: $space-3;
}

.card-row {
  font-size: $font-size-sm;
  color: $text-regular;
}

.card-label {
  color: $text-secondary;
  margin-right: $space-1;
}

.card-actions {
  display: flex;
  gap: $space-2;
  padding-top: $space-3;
  border-top: 1px solid $border-light;
}

.btn-accept {
  background: $success;
  color: #fff;
  border: none;
  padding: $space-2 $space-5;
  border-radius: $radius-base;
  font-size: $font-size-sm;
  cursor: pointer;
  &:hover { opacity: 0.9; }
}

.btn-reject {
  background: transparent;
  color: $danger;
  border: 1px solid $danger;
  padding: $space-2 $space-5;
  border-radius: $radius-base;
  font-size: $font-size-sm;
  cursor: pointer;
  &:hover { background: $danger; color: #fff; }
}
</style>
