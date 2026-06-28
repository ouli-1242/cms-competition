<script setup lang="ts">
/**
 * 资料修改审批页（管理员）
 * - 列表：申请人 / 字段 / 旧值 → 新值 / 状态 / 操作
 * - 筛选：状态下拉
 * - 审核弹窗
 */
import { ref, reactive, onMounted } from 'vue'
import { statusText, statusType } from '@/utils/status'
import { ElMessage } from 'element-plus'
import { getProfileChanges, reviewProfileChange } from '@/api/admin'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

const filter = reactive({ status: '' })

// 审核弹窗
const auditVisible = ref(false)
const auditTarget = ref<any>(null)
const auditForm = reactive({ result: 'APPROVE', remark: '' })
const submitting = ref(false)

const fieldLabel: Record<string, string> = {
  username: '学号',
  email: '邮箱'
}

async function loadData() {
  loading.value = true
  try {
    const res: any = await getProfileChanges({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      status: filter.status !== '' ? Number(filter.status) : undefined
    })
    list.value = res?.records || []
    total.value = res?.total || 0
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  loadData()
}

function handleReset() {
  filter.status = ''
  currentPage.value = 1
  loadData()
}

function openAudit(row: any) {
  auditTarget.value = row
  auditForm.result = 'APPROVE'
  auditForm.remark = ''
  auditVisible.value = true
}

async function handleAuditSubmit() {
  if (!auditTarget.value) return
  submitting.value = true
  try {
    await reviewProfileChange(
      auditTarget.value.id,
      auditForm.result === 'APPROVE',
      auditForm.remark || undefined
    )
    ElMessage.success('审核完成')
    auditVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

function handlePageChange(page: number) {
  currentPage.value = page
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">资料修改审批</h2>
    </div>

    <!-- 筛选 -->
    <div class="filter-bar">
      <div class="filter-item">
        <select v-model="filter.status" class="form-input form-select" @change="handleSearch">
          <option value="">全部状态</option>
          <option value="0">待审核</option>
          <option value="1">已通过</option>
          <option value="2">已拒绝</option>
        </select>
      </div>
      <div class="filter-actions">
        <button class="btn-search" @click="handleSearch">搜索</button>
        <button class="btn-reset" @click="handleReset">重置</button>
      </div>
    </div>

    <!-- 表格 -->
    <el-table
      v-loading="loading"
      :data="list"
      stripe
      class="data-table"
      :header-cell-style="{ background: '#f7fafc', color: '#4a5568', fontWeight: 600 }"
    >
      <el-table-column label="申请人" width="120" align="center">
        <template #default="{ row }">
          <span class="user-name">{{ row.userRealName || row.userUsername || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="修改字段" width="100" align="center">
        <template #default="{ row }">
          <el-tag size="small" effect="plain" round>{{ fieldLabel[row.fieldName] || row.fieldName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="当前值" min-width="140">
        <template #default="{ row }">
          <span class="value-text">{{ row.oldValue || '（空）' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="申请改为" min-width="140">
        <template #default="{ row }">
          <span class="value-text new-value">{{ row.newValue }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status) as any" effect="light" round>
            {{ statusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="申请时间" width="180" align="center" prop="createTime" />
      <el-table-column label="操作" width="160" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 0"
            type="primary"
            size="small"
            plain
            round
            @click="openAudit(row)"
          >审核</el-button>
          <el-button v-else type="info" size="small" disabled round>已处理</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无修改申请" />
      </template>
    </el-table>

    <div v-if="total > pageSize" class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 审核弹窗 -->
    <el-dialog
      v-model="auditVisible"
      title="审批资料修改"
      width="480px"
      :close-on-click-modal="false"
      align-center
    >
      <div class="audit-body" v-if="auditTarget">
        <div class="audit-info">
          <div class="info-row">
            <span class="info-label">申请人：</span>
            <span>{{ auditTarget.userRealName || auditTarget.userUsername }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">修改字段：</span>
            <span>{{ fieldLabel[auditTarget.fieldName] || auditTarget.fieldName }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">当前值：</span>
            <span>{{ auditTarget.oldValue || '（空）' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">新值：</span>
            <span class="new-value">{{ auditTarget.newValue }}</span>
          </div>
        </div>
        <div class="form-item">
          <label class="form-label">审批结果</label>
          <select v-model="auditForm.result" class="form-input form-select">
            <option value="APPROVE">通过</option>
            <option value="REJECT">拒绝</option>
          </select>
        </div>
        <div class="form-item">
          <label class="form-label">备注（可选）</label>
          <textarea
            v-model="auditForm.remark"
            class="form-input form-textarea"
            placeholder="填写审批备注"
            maxlength="200"
            rows="3"
          ></textarea>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="btn-ok gray" @click="auditVisible = false">取消</button>
          <button class="btn-ok primary" :disabled="submitting" @click="handleAuditSubmit">
            {{ submitting ? '提交中...' : '确认' }}
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
}

.filter-actions {
  display: flex;
  gap: $space-2;
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
  min-height: 80px;
  padding: $space-2 $space-3;
  resize: vertical;
  line-height: 1.6;
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
  box-shadow: 0 2px 8px rgba(43, 108, 176, 0.2);
  transition: all $transition-base;
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, transparent 0%, rgba(255,255,255,0.12) 50%, transparent 100%);
    transform: translateX(-100%);
    transition: transform 0.5s ease;
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 14px rgba(43, 108, 176, 0.35);

    &::after {
      transform: translateX(100%);
    }
  }

  &:active {
    transform: translateY(0) scale(0.97);
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
  &:hover { background: $bg-page; border-color: $primary; color: $primary; }
}

// ===== 表格 =====
.data-table {
  border-radius: $radius-md;
  overflow: hidden;
  box-shadow: $shadow-sm;
  background: $bg-card;
}

.user-name {
  font-weight: $font-weight-medium;
  color: $text-primary;
}

.value-text {
  font-size: $font-size-sm;
  color: $text-regular;

  &.new-value {
    color: $primary;
    font-weight: $font-weight-medium;
  }
}

.pagination {
  display: flex;
  justify-content: center;
  padding: $space-4 0;
}

// ===== 审核弹窗 =====
.audit-body {
  display: flex;
  flex-direction: column;
  gap: $space-4;
  padding: $space-3 0;
}

.audit-info {
  background: $bg-page;
  border-radius: $radius-base;
  padding: $space-3 $space-4;
}

.info-row {
  display: flex;
  align-items: center;
  padding: $space-1 0;
  font-size: $font-size-sm;

  .info-label {
    width: 80px;
    color: $text-secondary;
    flex-shrink: 0;
  }

  .new-value {
    color: $primary;
    font-weight: $font-weight-medium;
  }
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

  &:hover { background: $bg-page; border-color: $primary; color: $primary; }

  &.primary {
    background: linear-gradient(135deg, $primary, $primary-hover);
    color: #fff;
    border: none;
    box-shadow: 0 4px 12px rgba(43, 108, 176, 0.25);
    &:hover:not(:disabled) { transform: translateY(-1px); color: #fff; }
    &:disabled { background: $bg-disabled; color: $text-disabled; cursor: not-allowed; }
  }
  &.gray {
    background: #e2e8f0;
    border-color: #cbd5e0;
    &:hover { background: #cbd5e0; }
  }
}
</style>
