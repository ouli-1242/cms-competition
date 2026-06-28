<script setup lang="ts">
/**
 * 报名记录页
 * - 表格展示：竞赛 / 报名时间 / 状态 / 操作
 * - 分页
 * - 取消报名
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Document } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getMyRegistrations, cancelRegistration, getMyTeamRegistrations } from '@/api/registration'
import { getCompetitionDetail } from '@/api/public'


const router = useRouter()

// ====== Tab 切换 ======
const activeTab = ref<'individual' | 'team'>('individual')

// ====== 个人报名数据 ======
const list = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

// ====== 团队报名数据 ======
const teamList = ref<any[]>([])
const teamLoading = ref(false)
const teamTotal = ref(0)
const teamPage = ref(1)
const teamPageSize = ref(10)

// ====== 取消报名弹窗 ======
const cancelDialogVisible = ref(false)
const cancelTarget = ref<any>(null)

// ====== 状态映射 ======
const statusMap: Record<number, { text: string; color: string }> = {
  0: { text: '审核中', color: 'warning' },
  1: { text: '已通过', color: 'success' },
  2: { text: '已拒绝', color: 'danger' }
}

// ====== 竞赛名称缓存 ======
const compTitleMap = ref<Record<number, string>>({})

async function loadData() {
  loading.value = true
  try {
    const res: any = await getMyRegistrations({
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    const records = res.records || []
    total.value = res.total || 0

    // 批量获取竞赛名称
    const ids = [...new Set(records.map((r: any) => r.competitionId).filter(Boolean))] as number[]
    const fetches = ids
      .filter((id) => !compTitleMap.value[id])
      .map((id) =>
        getCompetitionDetail(id)
          .then((c: any) => { if (c) compTitleMap.value[id] = c.title })
          .catch(() => {})
      )
    await Promise.all(fetches)

    list.value = records
  } catch (e) {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function getCompTitle(id: number) {
  return compTitleMap.value[id] || `竞赛 #${id}`
}

function onPageChange(p: number) {
  currentPage.value = p
  loadData()
}

// ====== 团队报名数据加载 ======
async function loadTeamData() {
  teamLoading.value = true
  try {
    const res: any = await getMyTeamRegistrations({
      pageNum: teamPage.value,
      pageSize: teamPageSize.value
    })
    const records = res.records || []
    teamTotal.value = res.total || 0

    // 批量获取竞赛名称（复用 compTitleMap）
    const ids = [...new Set(records.map((r: any) => r.competitionId).filter(Boolean))] as number[]
    const fetches = ids
      .filter((id) => !compTitleMap.value[id])
      .map((id) =>
        getCompetitionDetail(id)
          .then((c: any) => { if (c) compTitleMap.value[id] = c.title })
          .catch(() => {})
      )
    await Promise.all(fetches)

    teamList.value = records
  } catch (e) {
    teamList.value = []
    teamTotal.value = 0
  } finally {
    teamLoading.value = false
  }
}

function onTeamPageChange(p: number) {
  teamPage.value = p
  loadTeamData()
}

function onTabChange(tab: 'individual' | 'team') {
  activeTab.value = tab
  if (tab === 'team' && teamList.value.length === 0 && teamTotal.value === 0) {
    loadTeamData()
  }
}

function handleCancel(row: any) {
  cancelTarget.value = row
  cancelDialogVisible.value = true
}

async function confirmCancel() {
  if (!cancelTarget.value) return
  const row = cancelTarget.value
  try {
    await cancelRegistration(row.id)
    ElMessage.success('已取消报名')
    loadData()
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    cancelDialogVisible.value = false
    cancelTarget.value = null
  }
}

function closeCancelDialog() {
  cancelDialogVisible.value = false
  cancelTarget.value = null
}

function handleView(row: any, type: 'individual' | 'team' = 'individual') {
  router.push(`/student-center/registration-detail/${row.id}?type=${type}`)
}

function getStatusInfo(status: number) {
  return statusMap[status] || { text: '未知', color: 'info' }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="reg-page">
    <!-- 标题 -->
    <h2 class="page-title">我的报名</h2>

    <!-- 统计 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon icon-blue"><el-icon><Document /></el-icon></div>
        <div>
          <p class="stat-label">报名总数</p>
          <p class="stat-value">{{ activeTab === 'team' ? teamTotal : total }}</p>
        </div>
      </div>
    </div>

    <!-- Tab 切换 -->
    <div class="tab-bar">
      <div
        class="tab-item"
        :class="{ active: activeTab === 'individual' }"
        @click="onTabChange('individual')"
      >
        个人报名
      </div>
      <div
        class="tab-item"
        :class="{ active: activeTab === 'team' }"
        @click="onTabChange('team')"
      >
        团队报名
      </div>
    </div>

    <!-- 个人报名记录表格 -->
    <div v-if="activeTab === 'individual'" class="table-section">
      <el-table
        v-loading="loading"
        :data="list"
        stripe
        class="reg-table"
        :header-cell-style="{ background: '#f7fafc', color: '#4a5568', fontWeight: 600 }"
      >
        <el-table-column prop="id" label="报名ID" width="90" align="center" />
        <el-table-column prop="competitionId" label="竞赛" min-width="200">
          <template #default="{ row }">
            <span class="comp-name">{{ getCompTitle(row.competitionId) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="registerTime" label="报名时间" width="180" align="center">
          <template #default="{ row }">
            <span class="time-text">{{ row.registerTime || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="getStatusInfo(row.status).color as any" effect="light" round>
              {{ getStatusInfo(row.status).text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="primary"
              size="small"
              plain
              round
              @click="handleView(row, 'individual')"
            >
              查看详情
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="danger"
              size="small"
              plain
              round
              @click="handleCancel(row)"
            >
              取消报名
            </el-button>
            <el-button
              v-else
              type="primary"
              size="small"
              plain
              round
              @click="handleView(row, 'individual')"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无报名记录" />
        </template>
      </el-table>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination">
        <span class="total-text">共 {{ total }} 条</span>
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="onPageChange"
        />
      </div>
    </div>

    <!-- 团队报名记录表格 -->
    <div v-if="activeTab === 'team'" class="table-section">
      <el-table
        v-loading="teamLoading"
        :data="teamList"
        stripe
        class="reg-table"
        :header-cell-style="{ background: '#f7fafc', color: '#4a5568', fontWeight: 600 }"
      >
        <el-table-column prop="teamName" label="团队名称" min-width="140">
          <template #default="{ row }">
            <span class="comp-name">{{ row.teamName || `团队 #${row.teamId || '-'}` }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="competitionId" label="竞赛名称" min-width="200">
          <template #default="{ row }">
            <span class="comp-name">{{ getCompTitle(row.competitionId) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="registerTime" label="报名时间" width="180" align="center">
          <template #default="{ row }">
            <span class="time-text">{{ row.registerTime || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="getStatusInfo(row.status).color as any" effect="light" round>
              {{ getStatusInfo(row.status).text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              plain
              round
              @click="handleView(row, 'team')"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无团队报名记录" />
        </template>
      </el-table>

      <!-- 分页 -->
      <div v-if="teamTotal > 0" class="pagination">
        <span class="total-text">共 {{ teamTotal }} 条</span>
        <el-pagination
          background
          layout="prev, pager, next"
          :total="teamTotal"
          :page-size="teamPageSize"
          :current-page="teamPage"
          @current-change="onTeamPageChange"
        />
      </div>
    </div>

    <!-- 取消报名确认弹窗 -->
    <el-dialog
      v-model="cancelDialogVisible"
      width="480px"
      :show-close="true"
      align-center
    >
      <template #header>
        <div class="dialog-header">
          <h3>取消报名确认</h3>
        </div>
      </template>
      <div class="dialog-body">
        <div class="alert alert-warning">
          <div class="alert-icon">⚠</div>
          <div>
            <p class="alert-title">确定要取消该竞赛的报名吗？</p>
            <p class="alert-desc">取消后如需参加需重新报名，且可能影响审核进度。</p>
          </div>
        </div>
        <div v-if="cancelTarget" class="confirm-info">
          <div class="confirm-row">
            <span class="confirm-label">竞赛名称：</span>
            <span class="confirm-value">{{ getCompTitle(cancelTarget.competitionId) }}</span>
          </div>
          <div class="confirm-row">
            <span class="confirm-label">报名时间：</span>
            <span class="confirm-value">{{ cancelTarget.registerTime || '-' }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="closeCancelDialog">我再想想</button>
          <button class="btn-confirm" @click="confirmCancel">确认取消</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.reg-page {
  max-width: 960px;
  margin: 0 auto;
}

.page-title {
  margin: 0 0 $space-5;
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

// ===== 统计 =====
.stats-row {
  display: grid;
  grid-template-columns: 1fr;
  gap: $space-3;
  margin-bottom: $space-4;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: $space-3;
  background: $bg-card;
  border-radius: $radius-md;
  padding: $space-4 $space-5;
  box-shadow: $shadow-sm;
  transition: all $transition-base;

  &:hover {
    transform: translateY(-2px);
    box-shadow: $shadow-md;
  }
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: $radius-base;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.icon-blue {
  background: $primary-50;
  color: $primary;
}

.stat-label {
  margin: 0 0 2px;
  font-size: $font-size-xs;
  color: $text-secondary;
}

.stat-value {
  margin: 0;
  font-size: 24px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

// ===== Tab 切换 =====
.tab-bar {
  display: flex;
  gap: $space-3;
  margin-bottom: $space-4;
  padding: 4px;
  background: $bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: $space-2 $space-4;
  font-size: $font-size-sm;
  color: $text-secondary;
  cursor: pointer;
  border-radius: $radius-base;
  transition: all $transition-base;
  user-select: none;

  &:hover {
    color: $primary;
    background: $primary-50;
  }

  &.active {
    background: $primary;
    color: #fff;
    font-weight: $font-weight-medium;
    box-shadow: 0 2px 8px rgba(43, 108, 176, 0.3);
  }
}

// ===== 表格 Section =====
.table-section {
  background: $bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;
  padding: $space-5;
}

.reg-table {
  border-radius: $radius-base;
  overflow: hidden;
}

.comp-name {
  font-size: $font-size-base;
  color: $text-primary;
  font-weight: $font-weight-medium;
}

.time-text {
  font-size: $font-size-sm;
  color: $text-secondary;
  font-family: 'Consolas', monospace;
}

// ===== 分页 =====
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: $space-5;
  padding-top: $space-4;
  border-top: 1px solid $border-light;
}

.total-text {
  font-size: $font-size-sm;
  color: $text-secondary;
}

// ===== 弹窗样式 =====
:deep(.el-dialog__header) {
  padding: $space-5 $space-6 0;
  margin-right: 0;
}

.dialog-header {
  h3 {
    margin: 0;
    font-size: $font-size-md;
    font-weight: $font-weight-semibold;
    color: $text-primary;
  }
}

.dialog-body {
  padding: $space-3 0 $space-2;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: $space-3;
  padding-top: $space-3;
}

.alert {
  display: flex;
  gap: $space-3;
  padding: $space-4 $space-5;
  border-radius: $radius-base;
  margin-bottom: $space-4;

  &.alert-warning {
    background: #fff5e6;
    color: $warning;
  }
}

.alert-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: $font-weight-bold;
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.5);
  margin-top: 2px;
}

.alert-title {
  margin: 0 0 4px;
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
}

.alert-desc {
  margin: 0;
  font-size: $font-size-sm;
  opacity: 0.85;
}

.confirm-info {
  background: $bg-page;
  border-radius: $radius-base;
  padding: $space-4 $space-5;
}

.confirm-row {
  display: flex;
  padding: $space-1 0;
  font-size: $font-size-sm;

  &:first-child {
    margin-bottom: $space-2;
  }
}

.confirm-label {
  color: $text-secondary;
  white-space: nowrap;
  flex-shrink: 0;
}

.confirm-value {
  color: $text-primary;
  flex: 1;
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

.btn-confirm {
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
</style>
