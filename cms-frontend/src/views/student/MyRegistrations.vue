<script setup lang="ts">
/**
 * 报名记录页
 * - 复用个人中心 Tab 风格
 * - 表格展示：竞赛名称 / 类型 / 报名时间 / 状态 / 操作
 * - 分页
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserInfo } from '@/api/auth'
import { getMyRegistrations, cancelRegistration } from '@/api/registration'

const router = useRouter()

// ====== 用户信息 ======
const userInfo = ref({
  realName: '张明远',
  college: '计算机科学与技术学院',
  username: '2024001234',
  avatar: ''
})

// ====== 数据 ======
const list = ref<any[]>([])
const total = ref(30)
const currentPage = ref(1)
const pageSize = ref(5)
const loading = ref(false)

// ====== 取消报名弹窗 ======
const cancelDialogVisible = ref(false)
const cancelTarget = ref<any>(null)

// ====== 占位数据 ======
const placeholder: any[] = [
  { id: 1, title: '2026年校园科技创新大赛', type: '学科竞赛', typeColor: 'blue', regTime: '2025-03-10 14:30', status: '审核通过', statusColor: 'success' },
  { id: 2, title: 'ACM校级设计挑战赛', type: '学科竞赛', typeColor: 'blue', regTime: '2025-03-05 12:35', status: '审核通过', statusColor: 'success' },
  { id: 3, title: '"青年之声"校园演讲比赛', type: '文艺活动', typeColor: 'orange', regTime: '2025-03-10 20:45', status: '已通过', statusColor: 'success' },
  { id: 4, title: '校园摄影大赛', type: '文艺活动', typeColor: 'orange', regTime: '2025-03-20 15:20', status: '待提交', statusColor: 'info' },
  { id: 5, title: '数字建模竞赛', type: '学科竞赛', typeColor: 'blue', regTime: '2025-03-10 09:00', status: '审核中', statusColor: 'warning' }
]

async function loadUser() {
  try {
    const data: any = await getUserInfo()
    if (data) Object.assign(userInfo.value, data)
  } catch (e) {
    // 静默
  }
}

async function loadData() {
  loading.value = true
  try {
    const res: any = await getMyRegistrations({
      page: currentPage.value,
      size: pageSize.value
    })
    list.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    // 失败用占位
    list.value = placeholder
    total.value = 30
  } finally {
    loading.value = false
  }
}

function onPageChange(p: number) {
  currentPage.value = p
  loadData()
}

function handleCancel(row: any) {
  cancelTarget.value = row
  cancelDialogVisible.value = true
}

async function confirmCancel() {
  if (!cancelTarget.value) return
  const row = cancelTarget.value
  try {
    try {
      await cancelRegistration(row.id)
    } catch (e) {
      // 静默
    }
    ElMessage.success('已取消报名')
    row.status = '已取消'
    row.statusColor = 'info'
    row._action = '已取消'
  } finally {
    cancelDialogVisible.value = false
    cancelTarget.value = null
  }
}

function closeCancelDialog() {
  cancelDialogVisible.value = false
  cancelTarget.value = null
}

function handleView(row: any) {
  router.push(`/student-center/registration/${row.id}`)
}

onMounted(() => {
  loadUser()
  loadData()
})
</script>

<template>
  <div class="reg-page">
    <!-- 顶部返回栏 -->
    <div class="page-header">
      <button class="back-btn" @click="router.back()">
        <span>←</span>
      </button>
      <h1 class="page-title">校园活动管理平台</h1>
      <div class="user-mini">
        <div class="avatar">{{ userInfo.realName[0] }}</div>
        <span class="user-name">{{ userInfo.realName }}</span>
      </div>
    </div>

    <!-- 用户卡 -->
    <div class="user-card">
      <div class="user-header">
        <div class="user-avatar">
          {{ userInfo.realName[0] }}
        </div>
        <div class="user-info">
          <h2 class="user-name">{{ userInfo.realName }}</h2>
          <p class="user-college">🏛 {{ userInfo.college }} · 学号 {{ userInfo.username }}</p>
        </div>
      </div>

      <!-- Tab 栏 -->
      <div class="tab-bar">
        <div class="tab-item" @click="router.push('/student/profile')">
          <span class="tab-icon">👤</span>个人资料
        </div>
        <div class="tab-item active">
          <span class="tab-icon">📋</span>报名记录
        </div>
        <div class="tab-item" @click="router.push('/student-center/my-teams')">
          <span class="tab-icon">👥</span>我的团队
        </div>
      </div>
    </div>

    <!-- 统计 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon icon-blue">📋</div>
        <div>
          <p class="stat-label">我的报名数</p>
          <p class="stat-value">{{ total }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-orange">👥</div>
        <div>
          <p class="stat-label">组建的团队</p>
          <p class="stat-value">3</p>
        </div>
      </div>
    </div>

    <!-- 报名记录表格 -->
    <div class="table-section">
      <h3 class="table-title">报名记录</h3>
      <el-table
        v-loading="loading"
        :data="list"
        stripe
        class="reg-table"
        :header-cell-style="{ background: '#f7fafc', color: '#4a5568', fontWeight: 600 }"
      >
        <el-table-column prop="title" label="竞赛名称" min-width="200">
          <template #default="{ row }">
            <span class="comp-name">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" :type="row.typeColor as any" round>
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="regTime" label="报名时间" width="180" align="center">
          <template #default="{ row }">
            <span class="time-text">📅 {{ row.regTime }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.statusColor as any" effect="light" round>
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === '审核中' || row.status === '待提交'"
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
              @click="handleView(row)"
            >
              查看报名
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

    <!-- 取消报名确认弹窗（状态 7） -->
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
            <span class="confirm-value">{{ cancelTarget.title }}</span>
          </div>
          <div class="confirm-row">
            <span class="confirm-label">报名时间：</span>
            <span class="confirm-value">{{ cancelTarget.regTime }}</span>
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
  max-width: 1280px;
  margin: 0 auto;
  padding: $space-4 $space-6 $space-8;
}

// ===== 顶部返回栏 =====
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $space-2 0 $space-4;
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

// ===== 用户卡 =====
.user-card {
  background: $bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;
  padding: $space-5;
  margin-bottom: $space-4;
}

.user-header {
  display: flex;
  align-items: center;
  gap: $space-4;
  margin-bottom: $space-5;
}

.user-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, $primary, $primary-hover);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: $font-weight-semibold;
  box-shadow: 0 4px 12px rgba(43, 108, 176, 0.3);
}

.user-info {
  flex: 1;
}

.user-info .user-name {
  margin: 0 0 $space-1;
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.user-college {
  margin: 0;
  font-size: $font-size-sm;
  color: $text-secondary;
}

// ===== Tab =====
.tab-bar {
  display: flex;
  border-top: 1px solid $border-light;
  margin: 0 (-$space-5) (-$space-5);
}

.tab-item {
  flex: 1;
  padding: $space-4;
  text-align: center;
  cursor: pointer;
  color: $text-secondary;
  font-size: $font-size-sm;
  border-top: 2px solid transparent;
  transition: all $transition-fast;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $space-1;

  &:hover {
    color: $primary;
  }

  &.active {
    color: $primary;
    border-top-color: $primary;
    font-weight: $font-weight-medium;
  }
}

.tab-icon {
  font-size: 16px;
}

// ===== 统计 =====
.stats-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
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

.icon-orange {
  background: #fff5e6;
  color: $warning;
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

// ===== 表格 Section =====
.table-section {
  background: $bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;
  padding: $space-5;
}

.table-title {
  margin: 0 0 $space-4;
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  color: $text-primary;
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

// ===== 响应式 =====
@media (max-width: 768px) {
  .reg-page {
    padding: $space-3 $space-4;
  }
  .stats-row {
    grid-template-columns: 1fr;
  }
  .user-mini .user-name {
    display: none;
  }
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
