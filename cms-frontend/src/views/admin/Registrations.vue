<script setup lang="ts">
/**
 * 报名管理页（管理员）
 * - 个人报名 / 团队报名 Tab 切换
 * - 详情 + 审核合二为一弹窗
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRegistrations, auditRegistration, getTeamRegistrations, auditTeamRegistration, getCompetitions } from '@/api/admin'
import { statusText, statusType } from '@/utils/status'

const loading = ref(false)
const list = ref<any[]>([])

const filter = reactive({ competitionId: '', status: '', keyword: '' })
const activeTab = ref<'individual' | 'team'>('individual')
const competitionOptions = ref<any[]>([])

// 个人报名 详情+审核 合一弹窗
const indDialogVisible = ref(false)
const indDialogData = ref<any>(null)
const indAuditForm = reactive({ result: 'APPROVE', remark: '' })
const indAuditError = ref('')
const indSubmitting = ref(false)

// 团队报名 详情+审核 合一弹窗
const teamDialogVisible = ref(false)
const teamDialogData = ref<any>(null)
const teamAuditForm = reactive({ result: 'APPROVE', remark: '' })
const teamAuditError = ref('')
const teamSubmitting = ref(false)

// 团队分页
const teamList = ref<any[]>([])
const teamLoading = ref(false)
const teamTotal = ref(0)
const teamPage = ref(1)

// 导出弹窗
const exportVisible = ref(false)

// ====== 个人报名 ======
async function loadData() {
  loading.value = true
  try {
    const res: any = await getRegistrations({
      pageNum: 1, pageSize: 100,
      competitionId: filter.competitionId ? Number(filter.competitionId) : undefined,
      status: filter.status !== '' ? Number(filter.status) : undefined,
      studentName: filter.keyword || undefined
    })
    list.value = res?.records || []
  } catch { list.value = [] } finally { loading.value = false }
}

function openIndDialog(row: any) {
  indDialogData.value = row
  indAuditForm.result = 'APPROVE'
  indAuditForm.remark = ''
  indAuditError.value = ''
  indDialogVisible.value = true
}

async function handleIndAudit() {
  if (!indAuditForm.remark.trim()) { indAuditError.value = '请填写审核意见'; return }
  if (indAuditForm.remark.length > 200) { indAuditError.value = '审核意见不能超过 200 字'; return }
  if (!indDialogData.value) return
  indSubmitting.value = true
  try {
    await auditRegistration(indDialogData.value.id, indAuditForm.result === 'APPROVE', indAuditForm.remark)
    const target = list.value.find((x: any) => x.id === indDialogData.value.id)
    if (target) { target.status = indAuditForm.result === 'APPROVE' ? 1 : 2 }
    ElMessage.success('审核完成')
    indDialogVisible.value = false
  } finally { indSubmitting.value = false }
}


// ====== 团队报名 ======
async function loadTeamData() {
  teamLoading.value = true
  try {
    const res: any = await getTeamRegistrations({
      pageNum: teamPage.value, pageSize: 100,
      competitionId: filter.competitionId ? Number(filter.competitionId) : undefined,
      status: filter.status !== '' ? Number(filter.status) : undefined
    })
    teamList.value = res?.records || []
    teamTotal.value = res?.total || 0
  } catch { teamList.value = []; teamTotal.value = 0 } finally { teamLoading.value = false }
}

function openTeamDialog(row: any) {
  teamDialogData.value = row
  teamAuditForm.result = 'APPROVE'
  teamAuditForm.remark = ''
  teamAuditError.value = ''
  teamDialogVisible.value = true
}


async function handleTeamAudit() {
  if (!teamAuditForm.remark.trim()) { teamAuditError.value = '请填写审核意见'; return }
  if (teamAuditForm.remark.length > 200) { teamAuditError.value = '审核意见不能超过 200 字'; return }
  if (!teamDialogData.value) return
  teamSubmitting.value = true
  try {
    await auditTeamRegistration(teamDialogData.value.id, teamAuditForm.result === 'APPROVE', teamAuditForm.remark)
    const target = teamList.value.find((x: any) => x.id === teamDialogData.value.id)
    if (target) { target.status = teamAuditForm.result === 'APPROVE' ? 1 : 2 }
    ElMessage.success('审核完成')
    teamDialogVisible.value = false
  } finally { teamSubmitting.value = false }
}

// ====== 通用 ======

function handleSearch() {
  if (activeTab.value === 'individual') loadData()
  else { teamPage.value = 1; loadTeamData() }
}

function handleReset() {
  filter.competitionId = ''; filter.status = ''; filter.keyword = ''
  handleSearch()
}

function handleTabChange(tab: 'individual' | 'team') {
  activeTab.value = tab
  filter.competitionId = ''
  loadCompetitions(tab === 'individual' ? 1 : 2)
  handleSearch()
}

async function loadCompetitions(type?: number) {
  try {
    const res: any = await getCompetitions({ pageNum: 1, pageSize: 200, type })
    competitionOptions.value = res?.records || []
  } catch { competitionOptions.value = [] }
}

function handleExport() { exportVisible.value = true }

onMounted(() => { loadCompetitions(1); loadData() })
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">报名管理</h2>
      <button class="btn-export" @click="handleExport">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
        报名导出
      </button>
    </div>

    <div class="tab-bar">
      <button class="tab-item" :class="{ active: activeTab === 'individual' }" @click="handleTabChange('individual')">个人报名</button>
      <button class="tab-item" :class="{ active: activeTab === 'team' }" @click="handleTabChange('team')">团队报名</button>
    </div>

    <div class="filter-bar">
      <div class="filter-item">
        <select v-model="filter.competitionId" class="form-input form-select" @change="handleSearch">
          <option value="">全部竞赛</option>
          <option v-for="c in competitionOptions" :key="c.id" :value="String(c.id)">{{ c.title }}</option>
        </select>
      </div>
      <div class="filter-item">
        <select v-model="filter.status" class="form-input form-select" @change="handleSearch">
          <option value="">全部状态</option>
          <option value="0">待审核</option>
          <option value="1">已通过</option>
          <option value="2">已拒绝</option>
        </select>
      </div>
      <div class="filter-item filter-search">
        <input v-model="filter.keyword" type="text" class="form-input" placeholder="搜索姓名" @keyup.enter="handleSearch" />
      </div>
      <div class="filter-actions">
        <button class="btn-search" @click="handleSearch">搜索</button>
        <button class="btn-reset" @click="handleReset">重置</button>
      </div>
    </div>

    <!-- 个人报名表格 -->
    <el-table
      v-show="activeTab === 'individual'"
      v-loading="loading"
      :data="list"
      stripe
      class="data-table"
      :header-cell-style="{ background: '#f7fafc', color: '#4a5568', fontWeight: 600 }"
    >
      <el-table-column prop="studentName" label="报名人" width="120" align="center">
        <template #default="{ row }"><span class="user-name">{{ row.studentName || '-' }}</span></template>
      </el-table-column>
      <el-table-column prop="competitionName" label="竞赛名称" min-width="200">
        <template #default="{ row }"><span class="comp-name">{{ row.competitionName || '-' }}</span></template>
      </el-table-column>
      <el-table-column prop="college" label="学院" width="140" align="center">
        <template #default="{ row }">{{ row.college || '-' }}</template>
      </el-table-column>
      <el-table-column prop="registerTime" label="报名时间" width="180" align="center">
        <template #default="{ row }">{{ row.registerTime || '-' }}</template>
      </el-table-column>
      <el-table-column label="审核状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status) as any" effect="light" round>{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" plain round @click="openIndDialog(row)">
            {{ row.status === 0 ? '审核' : '查看' }}
          </el-button>
        </template>
      </el-table-column>
      <template #empty><el-empty description="暂无报名" /></template>
    </el-table>

    <!-- 团队报名表格 -->
    <el-table
      v-show="activeTab === 'team'"
      v-loading="teamLoading"
      :data="teamList"
      stripe
      class="data-table"
      :header-cell-style="{ background: '#f7fafc', color: '#4a5568', fontWeight: 600 }"
    >
      <el-table-column prop="teamName" label="团队名称" width="140" align="center">
        <template #default="{ row }"><span class="user-name">{{ row.teamName || '-' }}</span></template>
      </el-table-column>
      <el-table-column prop="competitionName" label="竞赛名称" min-width="200">
        <template #default="{ row }"><span class="comp-name">{{ row.competitionName || '-' }}</span></template>
      </el-table-column>
      <el-table-column prop="captainName" label="队长" width="100" align="center">
        <template #default="{ row }">{{ row.captainName || '-' }}</template>
      </el-table-column>
      <el-table-column label="成员" width="90" align="center">
        <template #default="{ row }">{{ row.memberCount || 0 }}{{ row.maxSize ? '/' + row.maxSize : '' }}</template>
      </el-table-column>
      <el-table-column prop="registerTime" label="报名时间" width="180" align="center">
        <template #default="{ row }">{{ row.registerTime || '-' }}</template>
      </el-table-column>
      <el-table-column label="审核状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status) as any" effect="light" round>{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" plain round @click="openTeamDialog(row)">
            {{ row.status === 0 ? '审核' : '查看' }}
          </el-button>
        </template>
      </el-table-column>
      <template #empty><el-empty description="暂无团队报名" /></template>
    </el-table>

    <!-- 个人报名 详情+审核 合一弹窗 -->
    <el-dialog v-model="indDialogVisible" width="540px" :close-on-click-modal="false" align-center>
      <template #header>
        <div class="dialog-header">
          <h3>报名详情</h3>
          <el-tag v-if="indDialogData" :type="statusType(indDialogData.status) as any" effect="light" round>
            {{ statusText(indDialogData.status) }}
          </el-tag>
        </div>
      </template>
      <div class="detail-body" v-if="indDialogData">
        <div class="detail-row"><span class="detail-label">报名人</span><span class="detail-value">{{ indDialogData.studentName || '-' }}</span></div>
        <div class="detail-row"><span class="detail-label">学院</span><span class="detail-value">{{ indDialogData.college || '-' }}</span></div>
        <div class="detail-row"><span class="detail-label">竞赛</span><span class="detail-value">{{ indDialogData.competitionName || '-' }}</span></div>
        <div class="detail-row"><span class="detail-label">报名时间</span><span class="detail-value">{{ indDialogData.registerTime || '-' }}</span></div>
        <div class="detail-row" v-if="indDialogData.description"><span class="detail-label">说明</span><span class="detail-value">{{ indDialogData.description }}</span></div>
        <div class="detail-row" v-if="indDialogData.attachment"><span class="detail-label">附件</span><span class="detail-value"><a :href="indDialogData.attachment" target="_blank" class="link-text">查看附件</a></span></div>
        <div class="detail-row" v-if="indDialogData.reviewRemark"><span class="detail-label">审核意见</span><span class="detail-value">{{ indDialogData.reviewRemark }}</span></div>

        <!-- 审核表单（仅待审核时显示） -->
        <div v-if="indDialogData.status === 0" class="audit-section">
          <div class="audit-divider"></div>
          <div class="form-item">
            <label class="form-label">审核结果</label>
            <select v-model="indAuditForm.result" class="form-input form-select">
              <option value="APPROVE">通过</option>
              <option value="REJECT">拒绝</option>
            </select>
          </div>
          <div class="form-item">
            <label class="form-label">审核意见</label>
            <textarea v-model="indAuditForm.remark" class="form-input form-textarea" :class="{ 'is-error': indAuditError }" placeholder="请填写审核意见" maxlength="200" rows="3"></textarea>
            <p v-if="indAuditError" class="error-text">{{ indAuditError }}</p>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer" :class="{ center: indDialogData?.status !== 0 }">
          <button class="btn-ok gray" @click="indDialogVisible = false">关闭</button>
          <button v-if="indDialogData?.status === 0" class="btn-ok primary" :disabled="indSubmitting" @click="handleIndAudit">
            {{ indSubmitting ? '提交中...' : '确认提交' }}
          </button>
        </div>
      </template>
    </el-dialog>

    <!-- 团队报名 详情+审核 合一弹窗 -->
    <el-dialog v-model="teamDialogVisible" width="540px" :close-on-click-modal="false" align-center>
      <template #header>
        <div class="dialog-header">
          <h3>团队报名详情</h3>
          <el-tag v-if="teamDialogData" :type="statusType(teamDialogData.status) as any" effect="light" round>
            {{ statusText(teamDialogData.status) }}
          </el-tag>
        </div>
      </template>
      <div class="detail-body" v-if="teamDialogData">
        <div class="detail-row"><span class="detail-label">团队名称</span><span class="detail-value">{{ teamDialogData.teamName || '-' }}</span></div>
        <div class="detail-row"><span class="detail-label">竞赛</span><span class="detail-value">{{ teamDialogData.competitionName || '-' }}</span></div>
        <div class="detail-row"><span class="detail-label">队长</span><span class="detail-value">{{ teamDialogData.captainName || '-' }}</span></div>
        <div class="detail-row"><span class="detail-label">成员数</span><span class="detail-value">{{ teamDialogData.memberCount || 0 }}{{ teamDialogData.maxSize ? ' / ' + teamDialogData.maxSize : '' }}</span></div>
        <div class="detail-row"><span class="detail-label">报名时间</span><span class="detail-value">{{ teamDialogData.registerTime || '-' }}</span></div>
        <div class="detail-row" v-if="teamDialogData.description"><span class="detail-label">说明</span><span class="detail-value">{{ teamDialogData.description }}</span></div>
        <div class="detail-row" v-if="teamDialogData.attachment"><span class="detail-label">附件</span><span class="detail-value"><a :href="teamDialogData.attachment" target="_blank" class="link-text">查看附件</a></span></div>
        <div class="detail-row" v-if="teamDialogData.reviewRemark"><span class="detail-label">审核意见</span><span class="detail-value">{{ teamDialogData.reviewRemark }}</span></div>

        <div v-if="teamDialogData.status === 0" class="audit-section">
          <div class="audit-divider"></div>
          <div class="form-item">
            <label class="form-label">审核结果</label>
            <select v-model="teamAuditForm.result" class="form-input form-select">
              <option value="APPROVE">通过</option>
              <option value="REJECT">拒绝</option>
            </select>
          </div>
          <div class="form-item">
            <label class="form-label">审核意见</label>
            <textarea v-model="teamAuditForm.remark" class="form-input form-textarea" :class="{ 'is-error': teamAuditError }" placeholder="请填写审核意见" maxlength="200" rows="3"></textarea>
            <p v-if="teamAuditError" class="error-text">{{ teamAuditError }}</p>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer" :class="{ center: teamDialogData?.status !== 0 }">
          <button class="btn-ok gray" @click="teamDialogVisible = false">关闭</button>
          <button v-if="teamDialogData?.status === 0" class="btn-ok primary" :disabled="teamSubmitting" @click="handleTeamAudit">
            {{ teamSubmitting ? '提交中...' : '确认提交' }}
          </button>
        </div>
      </template>
    </el-dialog>

    <!-- 导出弹窗 -->
    <el-dialog v-model="exportVisible" width="400px" :show-close="true" align-center>
      <template #header><div class="dialog-header"><h3>导出成功</h3></div></template>
      <div class="dialog-body"><p class="dialog-text">报名数据导出成功！</p></div>
      <template #footer><div class="dialog-footer center"><button class="btn-ok primary" @click="exportVisible = false">确定</button></div></template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.page { display: flex; flex-direction: column; gap: $space-4; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-title { margin: 0; font-size: $font-size-xl; font-weight: $font-weight-semibold; color: $text-primary; }

.btn-export {
  display: inline-flex; align-items: center; gap: 6px; height: 36px; padding: 0 $space-5;
  border: none; background: linear-gradient(135deg, $primary, $primary-hover); color: #fff;
  font-size: $font-size-sm; font-weight: $font-weight-medium; border-radius: $radius-base;
  cursor: pointer; box-shadow: 0 4px 12px rgba(43, 108, 176, 0.25); transition: all $transition-fast;
  &:hover { transform: translateY(-1px); box-shadow: 0 6px 16px rgba(43, 108, 176, 0.35); }
  svg { flex-shrink: 0; }
}

.tab-bar { display: flex; border-bottom: 2px solid $border-light; }
.tab-item {
  padding: $space-2 $space-5; border: none; background: transparent; color: $text-secondary;
  font-size: $font-size-sm; font-weight: $font-weight-medium; cursor: pointer; position: relative; transition: all $transition-fast;
  &::after { content: ''; position: absolute; left: 0; bottom: -2px; width: 100%; height: 2px; background: transparent; transition: background $transition-fast; }
  &:hover { color: $primary; }
  &.active { color: $primary; font-weight: $font-weight-semibold; &::after { background: $primary; } }
}

.filter-bar {
  display: flex; gap: $space-3; align-items: center; padding: $space-3 $space-4;
  background: $bg-card; border-radius: $radius-md; box-shadow: $shadow-sm;
}
.filter-item { flex: 0 0 200px; &.filter-search { flex: 1 1 auto; } }
.filter-actions { display: flex; gap: $space-2; flex-shrink: 0; }

.form-input {
  height: 36px; padding: 0 $space-3; border: 1.5px solid $border-base; background: $bg-card;
  border-radius: $radius-base; font-size: $font-size-sm; color: $text-primary; outline: none;
  transition: all $transition-fast; font-family: inherit; width: 100%;
  &:focus { border-color: $primary; box-shadow: 0 0 0 3px rgba(43, 108, 176, 0.08); }
  &.is-error { border-color: $danger; background: #fef5f5; }
}
.form-select {
  appearance: none;
  background-image: url("data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 12 12'%3E%3Cpath fill='%23a0aec0' d='M6 8.5L1.5 4h9z'/%3E%3C/svg%3E");
  background-repeat: no-repeat; background-position: right 12px center; background-size: 12px; padding-right: 32px; cursor: pointer;
}
.form-textarea { height: auto; min-height: 80px; padding: $space-2 $space-3; resize: vertical; line-height: 1.6; font-family: inherit; }

.btn-search {
  height: 36px; padding: 0 $space-5; border: none; background: linear-gradient(135deg, $primary, $primary-hover);
  color: #fff; font-size: $font-size-sm; font-weight: $font-weight-medium; border-radius: $radius-base;
  cursor: pointer; transition: all $transition-fast;
  &:hover { transform: translateY(-1px); }
}
.btn-reset {
  height: 36px; padding: 0 $space-5; border: 1px solid $border-base; background: $bg-card;
  color: $text-regular; font-size: $font-size-sm; border-radius: $radius-base; cursor: pointer; transition: all $transition-fast;
  &:hover { background: $bg-page; border-color: $primary; color: $primary; }
}

.data-table { border-radius: $radius-md; overflow: hidden; box-shadow: $shadow-sm; background: $bg-card; }
.user-name, .comp-name { font-size: $font-size-base; color: $text-primary; font-weight: $font-weight-medium; }

// ===== 合一弹窗 =====
.dialog-header {
  display: flex; align-items: center; gap: $space-3;
  h3 { margin: 0; font-size: $font-size-md; font-weight: $font-weight-semibold; color: $text-primary; }
}

.detail-body { padding: $space-2 0; }
.detail-row {
  display: flex; align-items: flex-start; padding: $space-2 0; font-size: $font-size-sm; border-bottom: 1px dashed $border-light;
  &:last-child { border-bottom: none; }
}
.detail-label { width: 80px; color: $text-secondary; flex-shrink: 0; }
.detail-value { color: $text-primary; flex: 1; }
.link-text { color: $primary; text-decoration: none; &:hover { text-decoration: underline; } }

.audit-section { padding-top: $space-2; }
.audit-divider { height: 1px; background: $border-base; margin: $space-3 0 $space-4; }

.form-item { display: flex; flex-direction: column; gap: $space-1; margin-bottom: $space-3; }
.form-label { font-size: $font-size-sm; font-weight: $font-weight-medium; color: $text-regular; }
.error-text { margin: 0; font-size: $font-size-xs; color: $danger; }

:deep(.el-dialog__header) { padding: $space-5 $space-6 0; margin-right: 0; }
.dialog-body { padding: $space-3 0; }
.dialog-text { margin: 0; font-size: $font-size-sm; color: $text-regular; line-height: 1.7; }
.dialog-footer {
  display: flex; justify-content: flex-end; gap: $space-3; padding-top: $space-3;
  &.center { justify-content: center; }
}

.btn-ok {
  height: 36px; padding: 0 $space-5; border: 1px solid $border-base; background: $bg-card;
  color: $text-regular; font-size: $font-size-sm; border-radius: $radius-base; cursor: pointer;
  transition: all $transition-fast; min-width: 80px;
  &:hover { background: $bg-page; border-color: $primary; color: $primary; }
  &.primary {
    background: linear-gradient(135deg, $primary, $primary-hover); color: #fff; border: none;
    box-shadow: 0 4px 12px rgba(43, 108, 176, 0.25);
    &:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 6px 16px rgba(43, 108, 176, 0.35); color: #fff; }
    &:disabled { background: $bg-disabled; color: $text-disabled; cursor: not-allowed; box-shadow: none; }
  }
  &.gray { background: #e2e8f0; border-color: #cbd5e0; color: $text-regular; &:hover { background: #cbd5e0; border-color: #a0aec0; color: $text-primary; } }
}

@media (max-width: 768px) {
  .filter-bar { flex-wrap: wrap; }
  .filter-item { flex: 1 1 calc(50% - $space-2); }
}
</style>
