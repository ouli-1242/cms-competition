<script setup lang="ts">
/**
 * 报名审核页（教师）
 * - Tab 切换：个人报名 / 团队报名
 * - 筛选：竞赛下拉 + 状态下拉 + 姓名搜索（仅个人）
 * - 审核弹窗：通过/拒绝 + 审核意见
 * - 分页
 */
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getTeacherCompetitions,
  getTeacherRegistrations,
  teacherAuditRegistration,
  getTeacherTeamRegistrations,
  teacherAuditTeamRegistration
} from '@/api/registration'

// ====== 状态 ======
const activeTab = ref('individual')
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)

// 竞赛标题映射 id → title
const compTitleMap = ref<Record<number, string>>({})
const competitionOptions = ref<any[]>([])

// 分页
const pagination = reactive({
  pageNum: 1,
  pageSize: 10
})

// 筛选
const filter = reactive({
  competitionId: '' as string | number,
  status: '' as string | number,
  studentName: '',
  teamName: ''
})

// 审核弹窗
const auditVisible = ref(false)
const auditTarget = ref<any>(null)
const auditForm = reactive({ pass: true, remark: '' })
const auditErrors = reactive({ remark: '' })
const submitting = ref(false)

// ====== 数据加载 ======

/** 加载竞赛列表（用于下拉 + 标题映射） */
async function loadCompetitions() {
  try {
    const res: any = await getTeacherCompetitions({ pageNum: 1, pageSize: 100 })
    const records = res?.records || res?.data?.records || []
    competitionOptions.value = records
    const map: Record<number, string> = {}
    for (const c of records) {
      map[c.id] = c.title || c.name || `竞赛#${c.id}`
    }
    compTitleMap.value = map
  } catch {
    competitionOptions.value = []
    compTitleMap.value = {}
  }
}

/** 加载报名列表 */
async function loadData() {
  loading.value = true
  try {
    const params: any = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      competitionId: filter.competitionId ? Number(filter.competitionId) : undefined,
      status: filter.status !== '' ? Number(filter.status) : undefined
    }

    let res: any
    if (activeTab.value === 'individual') {
      params.studentName = filter.studentName || undefined
      res = await getTeacherRegistrations(params)
    } else {
      params.teamName = filter.teamName || undefined
      res = await getTeacherTeamRegistrations(params)
    }

    list.value = res?.records || res?.data?.records || []
    total.value = res?.total ?? res?.data?.total ?? 0
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// ====== 操作 ======

function handleSearch() {
  pagination.pageNum = 1
  loadData()
}

function handleReset() {
  filter.competitionId = ''
  filter.status = ''
  filter.studentName = ''
  filter.teamName = ''
  pagination.pageNum = 1
  loadData()
}

function handlePageChange(page: number) {
  pagination.pageNum = page
  loadData()
}

function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadData()
}

function openAudit(row: any) {
  auditTarget.value = row
  auditForm.pass = true
  auditForm.remark = ''
  auditErrors.remark = ''
  auditVisible.value = true
}

function validateAudit(): boolean {
  auditErrors.remark = ''
  if (!auditForm.remark.trim()) {
    auditErrors.remark = '请填写审核意见'
    return false
  }
  if (auditForm.remark.length > 200) {
    auditErrors.remark = '审核意见不能超过 200 字'
    return false
  }
  return true
}

async function handleAuditSubmit() {
  if (!validateAudit()) return
  if (!auditTarget.value) return
  submitting.value = true
  try {
    const id = auditTarget.value.id
    if (activeTab.value === 'individual') {
      await teacherAuditRegistration(id, auditForm.pass, auditForm.remark)
    } else {
      await teacherAuditTeamRegistration(id, auditForm.pass, auditForm.remark)
    }
    ElMessage.success('审核完成')
    auditVisible.value = false
    loadData()
  } catch {
    ElMessage.error('审核失败，请重试')
  } finally {
    submitting.value = false
  }
}

// ====== 批量审核 ======
const selectedRows = ref<any[]>([])

function handleSelectionChange(rows: any[]) {
  selectedRows.value = rows
}

const batchVisible = ref(false)
const batchForm = reactive({ pass: true, remark: '' })
const batchErrors = reactive({ remark: '' })
const batchSubmitting = ref(false)

function openBatchAudit() {
  const pendingRows = selectedRows.value.filter((r: any) => r.status === 0)
  if (pendingRows.length === 0) {
    ElMessage.warning('请选择待审核的记录')
    return
  }
  batchForm.pass = true
  batchForm.remark = ''
  batchErrors.remark = ''
  batchVisible.value = true
}

async function handleBatchSubmit() {
  if (!batchForm.remark.trim()) {
    batchErrors.remark = '请填写审核意见'
    return
  }
  const pendingRows = selectedRows.value.filter((r: any) => r.status === 0)
  if (pendingRows.length === 0) return
  batchSubmitting.value = true
  let success = 0
  let fail = 0
  try {
    for (const row of pendingRows) {
      try {
        if (activeTab.value === 'individual') {
          await teacherAuditRegistration(row.id, batchForm.pass, batchForm.remark)
        } else {
          await teacherAuditTeamRegistration(row.id, batchForm.pass, batchForm.remark)
        }
        success++
      } catch {
        fail++
      }
    }
    ElMessage.success(`批量审核完成：${success} 条成功${fail > 0 ? '，' + fail + ' 条失败' : ''}`)
    batchVisible.value = false
    selectedRows.value = []
    loadData()
  } finally {
    batchSubmitting.value = false
  }
}

// ====== 导出 ======
function handleExport() {
  if (list.value.length === 0) {
    ElMessage.warning('当前无数据可导出')
    return
  }
  import('xlsx').then((XLSX) => {
    const rows = list.value.map((r: any) => {
      if (activeTab.value === 'individual') {
        return {
          '报名人': r.realName || r.studentName || '-',
          '竞赛名称': r.competitionTitle || getCompTitle(r.competitionId),
          '报名时间': r.registerTime || r.createTime || '-',
          '状态': statusText(r.status)
        }
      } else {
        return {
          '团队名': r.teamName || `团队#${r.teamId || r.id}`,
          '竞赛名称': r.competitionTitle || getCompTitle(r.competitionId),
          '报名时间': r.registerTime || r.createTime || '-',
          '状态': statusText(r.status)
        }
      }
    })
    const ws = XLSX.utils.json_to_sheet(rows)
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, activeTab.value === 'individual' ? '个人报名' : '团队报名')
    XLSX.writeFile(wb, `报名名单_${activeTab.value === 'individual' ? '个人' : '团队'}_${Date.now()}.xlsx`)
    ElMessage.success('导出成功')
  }).catch(() => {
    ElMessage.error('导出失败，请重试')
  })
}

// ====== 工具函数 ======

function statusType(s: number) {
  return s === 0 ? 'warning' : s === 1 ? 'success' : 'danger'
}

function statusText(s: number) {
  return s === 0 ? '待审核' : s === 1 ? '已通过' : '已拒绝'
}

function getCompTitle(id: number) {
  return compTitleMap.value[id] || `竞赛#${id}`
}

// ====== 监听 & 初始化 ======

watch(activeTab, () => {
  pagination.pageNum = 1
  filter.competitionId = ''
  filter.status = ''
  filter.studentName = ''
  filter.teamName = ''
  selectedRows.value = []
  loadData()
})

onMounted(() => {
  loadCompetitions()
  loadData()
})
</script>

<template>
  <div class="page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">报名审核</h2>
    </div>

    <!-- Tab 切换 -->
    <div class="tab-bar">
      <button
        class="tab-item"
        :class="{ active: activeTab === 'individual' }"
        @click="activeTab = 'individual'"
      >
        个人报名
      </button>
      <button
        class="tab-item"
        :class="{ active: activeTab === 'team' }"
        @click="activeTab = 'team'"
      >
        团队报名
      </button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-item">
        <select v-model="filter.competitionId" class="form-input form-select">
          <option value="">全部竞赛</option>
          <option v-for="c in competitionOptions" :key="c.id" :value="c.id">
            {{ c.title || c.name }}
          </option>
        </select>
      </div>
      <div class="filter-item">
        <select v-model="filter.status" class="form-input form-select">
          <option value="">全部状态</option>
          <option value="0">待审核</option>
          <option value="1">已通过</option>
          <option value="2">已拒绝</option>
        </select>
      </div>
      <div v-if="activeTab === 'individual'" class="filter-item filter-search">
        <input
          v-model="filter.studentName"
          type="text"
          class="form-input"
          placeholder="搜索学生姓名"
          @keyup.enter="handleSearch"
        />
      </div>
      <div v-if="activeTab === 'team'" class="filter-item filter-search">
        <input
          v-model="filter.teamName"
          type="text"
          class="form-input"
          placeholder="搜索团队名"
          @keyup.enter="handleSearch"
        />
      </div>
      <div class="filter-actions">
        <button class="btn-search" @click="handleSearch">搜索</button>
        <button class="btn-reset" @click="handleReset">重置</button>
      </div>
    </div>

    <!-- 批量操作栏 -->
    <div class="batch-bar">
      <div class="batch-left">
        <button class="btn-batch btn-batch-pass" @click="openBatchAudit">批量通过/拒绝</button>
        <span v-if="selectedRows.filter(r => r.status === 0).length" class="batch-count">
          已选 {{ selectedRows.filter(r => r.status === 0).length }} 条待审核
        </span>
      </div>
      <button class="btn-export" @click="handleExport">导出名单</button>
    </div>

    <!-- 个人报名表格 -->
    <el-table
      v-if="activeTab === 'individual'"
      v-loading="loading"
      :data="list"
      stripe
      class="data-table"
      :header-cell-style="{ background: '#f7fafc', color: '#4a5568', fontWeight: 600 }"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column prop="realName" label="报名人" width="120" align="center">
        <template #default="{ row }">
          <span class="user-name">{{ row.realName || row.studentName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="competitionId" label="竞赛名称" min-width="200">
        <template #default="{ row }">
          <span class="comp-name">{{ row.competitionTitle || getCompTitle(row.competitionId) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="registerTime" label="报名时间" width="180" align="center">
        <template #default="{ row }">
          <span class="time-text">{{ row.registerTime || row.createTime || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status) as any" effect="light" round>
            {{ statusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 0"
            type="primary"
            size="small"
            plain
            round
            @click="openAudit(row)"
          >
            审核
          </el-button>
          <el-button v-else type="info" size="small" disabled round>已审核</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无报名记录" />
      </template>
    </el-table>

    <!-- 团队报名表格 -->
    <el-table
      v-if="activeTab === 'team'"
      v-loading="loading"
      :data="list"
      stripe
      class="data-table"
      :header-cell-style="{ background: '#f7fafc', color: '#4a5568', fontWeight: 600 }"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column prop="teamName" label="团队名" width="160" align="center">
        <template #default="{ row }">
          <span class="user-name">{{ row.teamName || `团队#${row.teamId || row.id}` }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="competitionId" label="竞赛名称" min-width="200">
        <template #default="{ row }">
          <span class="comp-name">{{ row.competitionTitle || getCompTitle(row.competitionId) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="registerTime" label="报名时间" width="180" align="center">
        <template #default="{ row }">
          <span class="time-text">{{ row.registerTime || row.createTime || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status) as any" effect="light" round>
            {{ statusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 0"
            type="primary"
            size="small"
            plain
            round
            @click="openAudit(row)"
          >
            审核
          </el-button>
          <el-button v-else type="info" size="small" disabled round>已审核</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无团队报名记录" />
      </template>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrap" v-if="total > 0">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <!-- 审核弹窗 -->
    <el-dialog
      v-model="auditVisible"
      title="报名审核"
      width="480px"
      :close-on-click-modal="false"
      align-center
    >
      <div class="audit-body">
        <div class="form-item">
          <label class="form-label">审核结果</label>
          <select v-model="auditForm.pass" class="form-input form-select">
            <option :value="true">通过</option>
            <option :value="false">拒绝</option>
          </select>
        </div>
        <div class="form-item">
          <label class="form-label">审核意见</label>
          <textarea
            v-model="auditForm.remark"
            class="form-input form-textarea"
            :class="{ 'is-error': auditErrors.remark }"
            placeholder="请填写审核意见"
            maxlength="200"
            rows="4"
          ></textarea>
          <p v-if="auditErrors.remark" class="error-text">{{ auditErrors.remark }}</p>
          <p class="char-count">{{ auditForm.remark.length }}/200</p>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="btn-ok gray" @click="auditVisible = false">取消</button>
          <button class="btn-ok primary" :disabled="submitting" @click="handleAuditSubmit">
            {{ submitting ? '提交中...' : '确认提交' }}
          </button>
        </div>
      </template>
    </el-dialog>

    <!-- 批量审核弹窗 -->
    <el-dialog
      v-model="batchVisible"
      :title="`批量审核（${selectedRows.filter(r => r.status === 0).length} 条待审核）`"
      width="480px"
      :close-on-click-modal="false"
      align-center
    >
      <div class="audit-body">
        <div class="form-item">
          <label class="form-label">审核结果</label>
          <select v-model="batchForm.pass" class="form-input form-select">
            <option :value="true">全部通过</option>
            <option :value="false">全部拒绝</option>
          </select>
        </div>
        <div class="form-item">
          <label class="form-label">审核意见</label>
          <textarea
            v-model="batchForm.remark"
            class="form-input form-textarea"
            :class="{ 'is-error': batchErrors.remark }"
            placeholder="请填写审核意见（将应用于所有选中记录）"
            maxlength="200"
            rows="4"
          ></textarea>
          <p v-if="batchErrors.remark" class="error-text">{{ batchErrors.remark }}</p>
          <p class="char-count">{{ batchForm.remark.length }}/200</p>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="btn-ok gray" @click="batchVisible = false">取消</button>
          <button class="btn-ok primary" :disabled="batchSubmitting" @click="handleBatchSubmit">
            {{ batchSubmitting ? '提交中...' : '确认批量提交' }}
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.page {
  display: flex;
  flex-direction: column;
  gap: $space-4;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  margin: 0;
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

// ===== Tab =====
.tab-bar {
  display: flex;
  gap: 0;
  background: $bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;
  overflow: hidden;
}

.tab-item {
  flex: 0 0 auto;
  padding: $space-3 $space-6;
  border: none;
  background: transparent;
  color: $text-secondary;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  cursor: pointer;
  transition: all $transition-fast;
  position: relative;
  font-family: inherit;

  &:hover {
    color: $primary;
    background: $primary-50;
  }

  &.active {
    color: $primary;
    font-weight: $font-weight-semibold;

    &::after {
      content: '';
      position: absolute;
      left: $space-4;
      right: $space-4;
      bottom: 0;
      height: 2px;
      background: $primary;
      border-radius: 1px;
    }
  }
}

// ===== 筛选 =====
.filter-bar {
  display: flex;
  gap: $space-3;
  align-items: center;
  padding: $space-3 $space-4;
  background: $bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;
}

.filter-item {
  flex: 0 0 200px;
  &.filter-search {
    flex: 1 1 auto;
  }
}

.filter-actions {
  display: flex;
  gap: $space-2;
  flex-shrink: 0;
}

.form-input {
  height: 36px;
  padding: 0 $space-3;
  border: 1.5px solid $border-base;
  background: $bg-card;
  border-radius: $radius-base;
  font-size: $font-size-sm;
  color: $text-primary;
  outline: none;
  transition: all $transition-fast;
  font-family: inherit;
  width: 100%;

  &:focus {
    border-color: $primary;
    box-shadow: 0 0 0 3px rgba(43, 108, 176, 0.08);
  }
  &.is-error {
    border-color: $danger;
    background: #fef5f5;
  }
}

.form-select {
  appearance: none;
  background-image: url("data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 12 12'%3E%3Cpath fill='%23a0aec0' d='M6 8.5L1.5 4h9z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  background-size: 12px;
  padding-right: 32px;
  cursor: pointer;
}

.form-textarea {
  height: auto;
  min-height: 100px;
  padding: $space-2 $space-3;
  resize: vertical;
  line-height: 1.6;
  font-family: inherit;
}

.btn-search {
  height: 36px;
  padding: 0 $space-5;
  border: none;
  background: linear-gradient(135deg, $primary, $primary-hover);
  color: #fff;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  border-radius: $radius-base;
  cursor: pointer;
  transition: all $transition-fast;
  &:hover {
    transform: translateY(-1px);
  }
}

.btn-reset {
  height: 36px;
  padding: 0 $space-5;
  border: 1px solid $border-base;
  background: $bg-card;
  color: $text-regular;
  font-size: $font-size-sm;
  border-radius: $radius-base;
  cursor: pointer;
  transition: all $transition-fast;
  &:hover {
    background: $bg-page;
    border-color: $primary;
    color: $primary;
  }
}

// ===== 批量操作栏 =====
.batch-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $space-2 $space-4;
  background: $bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;
}

.batch-left {
  display: flex;
  align-items: center;
  gap: $space-3;
}

.batch-count {
  font-size: $font-size-sm;
  color: $primary;
  font-weight: $font-weight-medium;
}

.btn-batch {
  height: 34px;
  padding: 0 $space-4;
  border: none;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  border-radius: $radius-base;
  cursor: pointer;
  transition: all $transition-fast;
}

.btn-batch-pass {
  background: linear-gradient(135deg, $primary, $primary-hover);
  color: #fff;
  box-shadow: 0 2px 8px rgba(43, 108, 176, 0.2);
  &:hover { transform: translateY(-1px); }
}

.btn-export {
  height: 34px;
  padding: 0 $space-4;
  border: 1px solid $border-base;
  background: $bg-card;
  color: $text-regular;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  border-radius: $radius-base;
  cursor: pointer;
  transition: all $transition-fast;
  &:hover { background: $bg-page; border-color: $primary; color: $primary; }
}

// ===== 表格 =====
.data-table {
  border-radius: $radius-md;
  overflow: hidden;
  box-shadow: $shadow-sm;
  background: $bg-card;
}

.user-name,
.comp-name {
  font-size: $font-size-base;
  color: $text-primary;
  font-weight: $font-weight-medium;
}

.time-text {
  font-size: $font-size-sm;
  color: $text-secondary;
}

// ===== 分页 =====
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding: $space-2 0;
}

// ===== 审核弹窗 =====
.audit-body {
  display: flex;
  flex-direction: column;
  gap: $space-4;
  padding: $space-3 0;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: $space-1;
}

.form-label {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $text-regular;
}

.error-text {
  margin: 0;
  font-size: $font-size-xs;
  color: $danger;
}

.char-count {
  margin: 0;
  font-size: $font-size-xs;
  color: $text-placeholder;
  text-align: right;
}

// ===== 弹窗通用 =====
:deep(.el-dialog__header) {
  padding: $space-5 $space-6 0;
  margin-right: 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: $space-3;
  padding-top: $space-3;
}

.btn-ok {
  height: 36px;
  padding: 0 $space-5;
  border: 1px solid $border-base;
  background: $bg-card;
  color: $text-regular;
  font-size: $font-size-sm;
  border-radius: $radius-base;
  cursor: pointer;
  transition: all $transition-fast;
  min-width: 80px;
  &:hover {
    background: $bg-page;
    border-color: $primary;
    color: $primary;
  }

  &.primary {
    background: linear-gradient(135deg, $primary, $primary-hover);
    color: #fff;
    border: none;
    box-shadow: 0 4px 12px rgba(43, 108, 176, 0.25);
    &:hover:not(:disabled) {
      transform: translateY(-1px);
      box-shadow: 0 6px 16px rgba(43, 108, 176, 0.35);
      color: #fff;
    }
    &:disabled {
      background: $bg-disabled;
      color: $text-disabled;
      cursor: not-allowed;
      box-shadow: none;
    }
  }
  &.gray {
    background: #e2e8f0;
    border-color: #cbd5e0;
    color: $text-regular;
    &:hover {
      background: #cbd5e0;
      border-color: #a0aec0;
      color: $text-primary;
    }
  }
}

@media (max-width: 768px) {
  .filter-bar {
    flex-wrap: wrap;
  }
  .filter-item {
    flex: 1 1 calc(50% - $space-2);
  }
}
</style>
