<script setup lang="ts">
/**
 * 我的竞赛页（教师）
 * - 展示教师指导的竞赛列表
 * - 关键词搜索 + 分页
 */
import { ref, reactive, onMounted } from 'vue'
import { getTeacherCompetitions, getTeacherCompetitionDetail } from '@/api/registration'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)

const pagination = reactive({
  pageNum: 1,
  pageSize: 10
})

const keyword = ref('')

async function loadData() {
  loading.value = true
  try {
    const res: any = await getTeacherCompetitions({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: keyword.value || undefined
    })
    list.value = res?.records || res?.data?.records || []
    total.value = res?.total ?? res?.data?.total ?? 0
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.pageNum = 1
  loadData()
}

function handleReset() {
  keyword.value = ''
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

function statusType(s: number) {
  return s === 1 ? 'success' : 'info'
}

function statusText(s: number) {
  return s === 1 ? '进行中' : '未开始'
}

// 详情弹窗
const detailVisible = ref(false)
const detailData = ref<any>(null)
const detailLoading = ref(false)

async function openDetail(row: any) {
  detailData.value = null
  detailVisible.value = true
  detailLoading.value = true
  try {
    detailData.value = await getTeacherCompetitionDetail(row.id)
  } catch {
    detailData.value = row
  } finally {
    detailLoading.value = false
  }
}

function fmtTime(t: string) {
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
      <h2 class="page-title">我的竞赛</h2>
    </div>

    <!-- 搜索栏 -->
    <div class="filter-bar">
      <div class="filter-item filter-search">
        <input
          v-model="keyword"
          type="text"
          class="form-input"
          placeholder="搜索竞赛名称"
          @keyup.enter="handleSearch"
        />
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
      class="data-table clickable-rows"
      :header-cell-style="{ background: '#f7fafc', color: '#4a5568', fontWeight: 600 }"
      @row-click="openDetail"
    >
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="title" label="竞赛名称" min-width="240">
        <template #default="{ row }">
          <span class="comp-name">{{ row.title || row.name || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="类别" width="140" align="center">
        <template #default="{ row }">
          <span class="category-text">{{ row.category || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status) as any" effect="light" round>
            {{ statusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" align="center">
        <template #default="{ row }">
          <span class="time-text">{{ row.createTime || '-' }}</span>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无竞赛" />
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

    <!-- 竞赛详情弹窗 -->
    <el-dialog v-model="detailVisible" title="竞赛详情" width="560px" :show-close="true" align-center>
      <div v-loading="detailLoading" class="detail-body">
        <template v-if="detailData">
          <div class="detail-title">{{ detailData.title }}</div>
          <div class="detail-tags">
            <el-tag effect="plain" round>{{ detailData.category || '-' }}</el-tag>
            <el-tag :type="detailData.type === 2 ? 'warning' : 'info'" effect="plain" round>
              {{ detailData.type === 2 ? '团队赛' : '个人赛' }}
            </el-tag>
            <el-tag :type="statusType(detailData.status) as any" effect="light" round>
              {{ statusText(detailData.status) }}
            </el-tag>
          </div>
          <div class="detail-desc">{{ detailData.description || '暂无介绍' }}</div>
          <div class="detail-grid">
            <div class="detail-row"><span class="detail-label">报名开始</span><span>{{ fmtTime(detailData.registerStart) }}</span></div>
            <div class="detail-row"><span class="detail-label">报名截止</span><span>{{ fmtTime(detailData.registerEnd) }}</span></div>
            <div class="detail-row"><span class="detail-label">竞赛开始</span><span>{{ fmtTime(detailData.compStart) }}</span></div>
            <div class="detail-row"><span class="detail-label">竞赛截止</span><span>{{ fmtTime(detailData.compEnd) }}</span></div>
            <div class="detail-row"><span class="detail-label">人数上限</span><span>{{ detailData.maxTeamSize || '-' }}</span></div>
            <div class="detail-row"><span class="detail-label">创建时间</span><span>{{ fmtTime(detailData.createTime) }}</span></div>
          </div>
        </template>
      </div>
      <template #footer>
        <div class="dialog-footer center">
          <button class="btn-close" @click="detailVisible = false">关闭</button>
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
  transition: box-shadow $transition-base;

  &:focus-within {
    box-shadow: $shadow-md;
  }
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

// ===== 表格 =====
.data-table {
  border-radius: $radius-md;
  overflow: hidden;
  box-shadow: $shadow-sm;
  background: $bg-card;
}

.comp-name {
  font-size: $font-size-base;
  color: $text-primary;
  font-weight: $font-weight-medium;
}

.category-text {
  font-size: $font-size-sm;
  color: $text-regular;
}

.time-text {
  font-size: $font-size-sm;
  color: $text-secondary;
}

// ===== 可点击行 =====
:deep(.clickable-rows) {
  .el-table__body tr {
    cursor: pointer;
    transition: background $transition-fast;
    &:hover td {
      background: $primary-50 !important;
    }
  }
}

// ===== 详情弹窗 =====
:deep(.el-dialog__header) {
  padding: $space-5 $space-6 0;
  margin-right: 0;
}

.detail-body {
  display: flex;
  flex-direction: column;
  gap: $space-4;
  min-height: 120px;
  padding: $space-2 0;
}

.detail-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.detail-tags {
  display: flex;
  gap: $space-2;
  flex-wrap: wrap;
}

.detail-desc {
  font-size: $font-size-sm;
  color: $text-regular;
  line-height: 1.7;
  background: $bg-page;
  border-radius: $radius-base;
  padding: $space-3 $space-4;
}

.detail-grid {
  display: flex;
  flex-direction: column;
  gap: $space-2;
  background: $bg-page;
  border-radius: $radius-base;
  padding: $space-3 $space-4;
}

.detail-row {
  display: flex;
  align-items: center;
  font-size: $font-size-sm;
  padding: $space-1 0;
}

.detail-label {
  width: 80px;
  color: $text-secondary;
  flex-shrink: 0;
}

.dialog-footer {
  display: flex;
  justify-content: center;
  padding-top: $space-3;
  &.center { justify-content: center; }
}

.btn-close {
  height: 36px;
  padding: 0 $space-6;
  border: 1px solid $border-base;
  background: $bg-card;
  color: $text-regular;
  font-size: $font-size-sm;
  border-radius: $radius-base;
  cursor: pointer;
  min-width: 80px;
  &:hover { background: $bg-page; border-color: $primary; color: $primary; }
}

// ===== 分页 =====
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding: $space-2 0;
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
