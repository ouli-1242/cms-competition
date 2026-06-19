<script setup lang="ts">
/**
 * 用户管理页（管理员）
 * - 用户列表：姓名 / 学号 / 角色 / 学院 / 手机号 / 邮箱 / 注册时间
 * - 筛选：关键词搜索 + 角色下拉
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUsers, createTeacher } from '@/api/admin'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

const filter = reactive({ keyword: '', role: '' })

const roleLabel: Record<string, string> = {
  STUDENT: '学生',
  TEACHER: '教师',
  ADMIN: '管理员'
}

const roleType: Record<string, string> = {
  STUDENT: 'primary',
  TEACHER: 'success',
  ADMIN: 'danger'
}

async function loadData() {
  loading.value = true
  try {
    const res: any = await getUsers({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      keyword: filter.keyword || undefined,
      role: filter.role || undefined
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
  filter.keyword = ''
  filter.role = ''
  currentPage.value = 1
  loadData()
}

function handlePageChange(page: number) {
  currentPage.value = page
  loadData()
}

function formatTime(t: string) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 16)
}

// 详情弹窗
const detailVisible = ref(false)
const detailData = ref<any>(null)

function openDetail(row: any) {
  detailData.value = row
  detailVisible.value = true
}

// 创建教师弹窗
const createVisible = ref(false)
const createLoading = ref(false)
const createForm = reactive({
  username: '',
  realName: '',
  college: '',
  phone: '',
  email: ''
})

function openCreate() {
  createForm.username = ''
  createForm.realName = ''
  createForm.college = ''
  createForm.phone = ''
  createForm.email = ''
  createVisible.value = true
}

async function handleCreate() {
  if (!createForm.username.trim()) {
    ElMessage.warning('请输入账号')
    return
  }
  createLoading.value = true
  try {
    await createTeacher({
      username: createForm.username.trim(),
      realName: createForm.realName || undefined,
      college: createForm.college || undefined,
      phone: createForm.phone || undefined,
      email: createForm.email || undefined
    })
    ElMessage.success('创建成功，默认密码：123456')
    createVisible.value = false
    loadData()
  } catch (e: any) {
    ElMessage.error(e?.message || '创建失败')
  } finally {
    createLoading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">用户管理</h2>
      <button class="btn-create" @click="openCreate">创建教师</button>
    </div>

    <!-- 筛选 -->
    <div class="filter-bar">
      <div class="filter-item">
        <select v-model="filter.role" class="form-input form-select" @change="handleSearch">
          <option value="">全部角色</option>
          <option value="STUDENT">学生</option>
          <option value="TEACHER">教师</option>
          <option value="ADMIN">管理员</option>
        </select>
      </div>
      <div class="filter-item filter-search">
        <input
          v-model="filter.keyword"
          type="text"
          class="form-input"
          placeholder="搜索姓名、学号、手机号"
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
      <el-table-column label="姓名" width="120" align="center">
        <template #default="{ row }">
          <span class="user-name">{{ row.realName || row.nickname || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="学号/账号" width="130" align="center" prop="username" />
      <el-table-column label="角色" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="(roleType[row.role] as any) || 'info'" size="small" effect="plain" round>
            {{ roleLabel[row.role] || row.role }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="学院" min-width="160" prop="college">
        <template #default="{ row }">
          {{ row.college || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="手机号" width="130" align="center" prop="phone">
        <template #default="{ row }">
          {{ row.phone || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="邮箱" min-width="160" prop="email">
        <template #default="{ row }">
          {{ row.email || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="注册时间" width="160" align="center">
        <template #default="{ row }">
          {{ formatTime(row.createTime) }}
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无用户" />
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

    <!-- 用户详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="用户详情"
      width="520px"
      :show-close="true"
      align-center
    >
      <div class="detail-body" v-if="detailData">
        <div class="detail-avatar">
          <img v-if="detailData.avatar" :src="detailData.avatar" alt="avatar" />
          <span v-else>{{ (detailData.realName || detailData.username || '?')[0] }}</span>
        </div>
        <div class="detail-grid">
          <div class="detail-row">
            <span class="detail-label">姓名</span>
            <span class="detail-value">{{ detailData.realName || detailData.nickname || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">学号/账号</span>
            <span class="detail-value">{{ detailData.username || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">角色</span>
            <span class="detail-value">
              <el-tag :type="(roleType[detailData.role] as any) || 'info'" size="small" effect="plain" round>
                {{ roleLabel[detailData.role] || detailData.role }}
              </el-tag>
            </span>
          </div>
          <div class="detail-row">
            <span class="detail-label">学院</span>
            <span class="detail-value">{{ detailData.college || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">手机号</span>
            <span class="detail-value">{{ detailData.phone || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">邮箱</span>
            <span class="detail-value">{{ detailData.email || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">注册时间</span>
            <span class="detail-value">{{ formatTime(detailData.createTime) }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer center">
          <button class="btn-ok" @click="detailVisible = false">关闭</button>
        </div>
      </template>
    </el-dialog>

    <!-- 创建教师弹窗 -->
    <el-dialog
      v-model="createVisible"
      title="创建教师账号"
      width="480px"
      :show-close="true"
      align-center
    >
      <div class="create-form">
        <div class="form-row">
          <label class="form-label">账号 <span class="required">*</span></label>
          <input v-model="createForm.username" class="form-input" placeholder="登录账号（必填）" />
        </div>
        <div class="form-row">
          <label class="form-label">姓名</label>
          <input v-model="createForm.realName" class="form-input" placeholder="真实姓名" />
        </div>
        <div class="form-row">
          <label class="form-label">学院</label>
          <input v-model="createForm.college" class="form-input" placeholder="所属学院" />
        </div>
        <div class="form-row">
          <label class="form-label">手机号</label>
          <input v-model="createForm.phone" class="form-input" placeholder="手机号" />
        </div>
        <div class="form-row">
          <label class="form-label">邮箱</label>
          <input v-model="createForm.email" class="form-input" placeholder="邮箱地址" />
        </div>
        <p class="form-tip">默认密码：123456，创建后教师可自行修改</p>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="createVisible = false">取消</button>
          <button class="btn-confirm" :disabled="createLoading" @click="handleCreate">
            {{ createLoading ? '创建中...' : '确认创建' }}
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
  flex: 0 0 180px;
  &.filter-search { flex: 1 1 auto; }
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
  &:hover { transform: translateY(-1px); }
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

.pagination {
  display: flex;
  justify-content: center;
  padding: $space-4 0;
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
  align-items: center;
  gap: $space-5;
  padding: $space-3 0;
}

.detail-avatar {
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
  overflow: hidden;
  flex-shrink: 0;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.detail-grid {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: $space-2;
  background: $bg-page;
  border-radius: $radius-base;
  padding: $space-4 $space-5;
}

.detail-row {
  display: flex;
  align-items: center;
  padding: $space-1 0;
  font-size: $font-size-sm;
}

.detail-label {
  width: 90px;
  color: $text-secondary;
  flex-shrink: 0;
}

.detail-value {
  color: $text-primary;
}

.dialog-footer {
  display: flex;
  justify-content: center;
  padding-top: $space-3;
}

.btn-ok {
  height: 36px;
  padding: 0 $space-6;
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
}

// ===== 创建教师按钮 =====
.btn-create {
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
  &:hover { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(43, 108, 176, 0.3); }
}

// ===== 创建表单弹窗 =====
.create-form {
  display: flex;
  flex-direction: column;
  gap: $space-3;
  padding: $space-2 0;
}

.form-row {
  display: flex;
  align-items: center;
  gap: $space-3;
}

.form-label {
  width: 70px;
  font-size: $font-size-sm;
  color: $text-regular;
  flex-shrink: 0;
  text-align: right;
}

.required {
  color: #e53e3e;
  margin-left: 2px;
}

.form-tip {
  margin: $space-1 0 0 70px;
  font-size: 12px;
  color: $text-secondary;
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: $space-3;
  padding-top: $space-3;
}

.btn-cancel {
  height: 36px;
  padding: 0 $space-6;
  border: 1px solid $border-base;
  background: $bg-card;
  color: $text-regular;
  font-size: $font-size-sm;
  border-radius: $radius-base;
  cursor: pointer;
  transition: all $transition-fast;
  &:hover { background: $bg-page; border-color: $primary; color: $primary; }
}

.btn-confirm {
  height: 36px;
  padding: 0 $space-6;
  border: none;
  background: linear-gradient(135deg, $primary, $primary-hover);
  color: #fff;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  border-radius: $radius-base;
  cursor: pointer;
  transition: all $transition-fast;
  &:hover { transform: translateY(-1px); }
  &:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }
}
</style>
