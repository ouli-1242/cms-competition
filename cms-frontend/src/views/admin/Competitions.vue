<script setup lang="ts">
/**
 * 竞赛管理页（管理员）
 * - 表格展示所有竞赛
 * - 发布 / 编辑 / 上下架 / 删除
 * - 4 个弹窗：发布/编辑、限制提示、删除确认、操作成功
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCompetitions, createCompetition, updateCompetition, deleteCompetition, toggleCompetitionStatus } from '@/api/admin'
import { uploadFile } from '@/api/upload'

const loading = ref(false)
const list = ref<any[]>([])

// 表单对话框
const formVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const form = reactive<any>({
  id: null,
  title: '',
  description: '',
  category: '科技类',
  registerStart: '',
  registerEnd: '',
  compStart: '',
  compEnd: '',
  maxMembers: 100,
  type: 'TEAM',  // INDIVIDUAL/TEAM
  auditEnabled: true,
  cover: ''
})
const errors = reactive<any>({
  title: '',
  maxMembers: ''
})

// 限制弹窗
const restrictVisible = ref(false)

// 删除确认
const deleteVisible = ref(false)
const deleteTarget = ref<any>(null)

// 操作成功弹窗
const successVisible = ref(false)
const successText = ref('')


// ====== 占位数据 ======
const placeholder = [
  { id: 1, title: '程序设计竞赛', category: '科技类', count: 56, status: 1, statusText: '已上架' },
  { id: 2, title: '英语演讲比赛', category: '语言类', count: 0, status: 0, statusText: '已下架' }
]

async function loadData() {
  loading.value = true
  try {
    const res: any = await getCompetitions({ pageNum: 1, pageSize: 100 })
    const records = res?.records || []
    list.value = records.map((r: any) => ({
      ...r,
      statusText: r.status === 1 ? '已上架' : '已下架',
      type: r.type === 2 ? 'TEAM' : 'INDIVIDUAL'
    }))
  } catch (e) {
    list.value = placeholder
  } finally {
    loading.value = false
  }
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    title: '',
    description: '',
    category: '科技类',
    registerStart: new Date().toISOString().slice(0, 16),
    registerEnd: '',
    compStart: '',
    compEnd: '',
    maxMembers: 100,
    type: 'TEAM',
    auditEnabled: true,
    cover: ''
  })
  errors.title = ''
  errors.maxMembers = ''
  formVisible.value = true
}

function openEdit(row: any) {
  isEdit.value = true
  if (row.count > 0) {
    restrictVisible.value = true
    return
  }
  Object.assign(form, {
    ...row,
    description: row.description || '',
    registerStart: row.registerStart || new Date().toISOString().slice(0, 16),
    registerEnd: row.registerEnd || '',
    compStart: row.compStart || '',
    compEnd: row.compEnd || '',
    type: row.type === 2 || row.type === 'TEAM' ? 'TEAM' : 'INDIVIDUAL',
    maxMembers: row.maxTeamSize || row.maxMembers || 100
  })
  formVisible.value = true
}

function validate(): boolean {
  errors.title = ''
  errors.maxMembers = ''
  let ok = true
  if (!form.title.trim()) {
    errors.title = '请输入竞赛名称'
    ok = false
  } else if (form.title.length > 50) {
    errors.title = '名称不能超过 50 字'
    ok = false
  }
  if (!form.maxMembers || form.maxMembers < 1 || form.maxMembers > 10000) {
    errors.maxMembers = '人数上限应在 1-10000 之间'
    ok = false
  }
  return ok
}

async function handleSave() {
  if (!validate()) return
  submitting.value = true
  try {
    const dto: any = {
      title: form.title,
      description: form.description,
      category: form.category,
      type: form.type === 'TEAM' ? 2 : 1,
      maxTeamSize: form.maxMembers,
      minTeamSize: 1,
      cover: form.cover
    }
    if (form.registerStart) dto.registerStart = form.registerStart
    if (form.registerEnd) dto.registerEnd = form.registerEnd
    if (form.compStart) dto.compStart = form.compStart
    if (form.compEnd) dto.compEnd = form.compEnd
    if (isEdit.value) {
      await updateCompetition(form.id, dto)
      ElMessage.success('保存成功')
    } else {
      await createCompetition(dto)
      ElMessage.success('发布成功')
    }
    formVisible.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

function openDelete(row: any) {
  deleteTarget.value = row
  deleteVisible.value = true
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  await deleteCompetition(deleteTarget.value.id)
  ElMessage.success('删除成功')
  list.value = list.value.filter((x) => x.id !== deleteTarget.value.id)
  deleteVisible.value = false
  deleteTarget.value = null
}

async function handleToggle(row: any) {
  await toggleCompetitionStatus(row.id, row.status === 1 ? 0 : 1)
  row.status = row.status === 1 ? 0 : 1
  row.statusText = row.status === 1 ? '已上架' : '已下架'
  successText.value = row.status === 1 ? '已上架，前台显示该竞赛' : '已下架，前台不再显示该竞赛'
  successVisible.value = true
}

function statusType(s: number) {
  return s === 1 ? 'success' : 'info'
}

function typeText(t: string) {
  const map: any = { INDIVIDUAL: '个人', TEAM: '团队' }
  return map[t] || t
}

// 触发文件选择
const fileInput = ref<HTMLInputElement | null>(null)
function triggerUpload() {
  fileInput.value?.click()
}
const uploading = ref(false)
async function onFileChange(e: Event) {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return
  uploading.value = true
  try {
    const url = await uploadFile(file, 'cover')
    form.cover = url
    ElMessage.success('封面上传成功')
  } finally {
    uploading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="page">
    <!-- 顶部栏 -->
    <div class="page-header">
      <h2 class="page-title">竞赛管理</h2>
      <button class="btn-publish" @click="openCreate">
        <span>+</span>发布竞赛
      </button>
    </div>

    <!-- 表格 -->
    <el-table
      v-loading="loading"
      :data="list"
      stripe
      class="data-table"
      :header-cell-style="{ background: '#f7fafc', color: '#4a5568', fontWeight: 600 }"
    >
      <el-table-column prop="id" label="竞赛ID" width="100" align="center" />
      <el-table-column prop="title" label="竞赛名称" min-width="200">
        <template #default="{ row }">
          <span class="comp-name">{{ row.title }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="类型" width="120" align="center">
        <template #default="{ row }">
          <el-tag size="small" effect="plain" round>{{ row.category }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="count" label="报名人数" width="120" align="center">
        <template #default="{ row }">
          <span class="count-text">{{ row.count }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status) as any" effect="light" round>
            {{ row.statusText }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" plain round @click="openEdit(row)">编辑</el-button>
          <el-button
            v-if="row.status === 1"
            type="warning"
            size="small"
            plain
            round
            @click="handleToggle(row)"
          >
            下架
          </el-button>
          <el-button
            v-else
            type="success"
            size="small"
            plain
            round
            @click="handleToggle(row)"
          >
            上架
          </el-button>
          <el-button type="danger" size="small" plain round @click="openDelete(row)">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无竞赛" />
      </template>
    </el-table>

    <!-- 发布/编辑竞赛 弹窗 -->
    <el-dialog
      v-model="formVisible"
      :title="isEdit ? '编辑竞赛' : '发布竞赛'"
      width="720px"
      :close-on-click-modal="false"
      align-center
    >
      <div class="form-grid">
        <div class="form-item">
          <label class="form-label">竞赛名称</label>
          <input
            v-model="form.title"
            type="text"
            class="form-input"
            :class="{ 'is-error': errors.title }"
            placeholder="请输入竞赛名称"
            maxlength="50"
          />
          <p v-if="errors.title" class="error-text">{{ errors.title }}</p>
        </div>
        <div class="form-item">
          <label class="form-label">竞赛类型</label>
          <select v-model="form.category" class="form-input form-select">
            <option value="科技类">科技类</option>
            <option value="语言类">语言类</option>
            <option value="文艺类">文艺类</option>
            <option value="体育类">体育类</option>
            <option value="创业类">创业类</option>
          </select>
        </div>
        <div class="form-item">
          <label class="form-label">报名开始</label>
          <input
            v-model="form.registerStart"
            type="datetime-local"
            class="form-input"
          />
        </div>
        <div class="form-item">
          <label class="form-label">报名截止</label>
          <input
            v-model="form.registerEnd"
            type="datetime-local"
            class="form-input"
          />
        </div>
        <div class="form-item">
          <label class="form-label">竞赛开始</label>
          <input
            v-model="form.compStart"
            type="datetime-local"
            class="form-input"
          />
        </div>
        <div class="form-item">
          <label class="form-label">竞赛截止</label>
          <input
            v-model="form.compEnd"
            type="datetime-local"
            class="form-input"
          />
        </div>
        <div class="form-item">
          <label class="form-label">参赛人数上限</label>
          <input
            v-model.number="form.maxMembers"
            type="number"
            class="form-input"
            :class="{ 'is-error': errors.maxMembers }"
            min="1"
            max="10000"
            placeholder="100"
          />
          <p v-if="errors.maxMembers" class="error-text">{{ errors.maxMembers }}</p>
        </div>
        <div class="form-item">
          <label class="form-label">参赛类型</label>
          <select v-model="form.type" class="form-input form-select">
            <option value="INDIVIDUAL">个人</option>
            <option value="TEAM">团队</option>
          </select>
        </div>
        <div class="form-item">
          <label class="form-label">审核开关</label>
          <select v-model="form.auditEnabled" class="form-input form-select">
            <option :value="true">开启</option>
            <option :value="false">关闭</option>
          </select>
        </div>
        <div class="form-item form-item-full">
          <label class="form-label">竞赛介绍</label>
          <textarea
            v-model="form.description"
            class="form-input form-textarea"
            placeholder="请输入竞赛介绍、参赛规则等信息"
            rows="4"
            maxlength="2000"
          ></textarea>
        </div>
        <div class="form-item form-item-full">
          <label class="form-label">竞赛封面</label>
          <div class="upload-zone" @click="triggerUpload">
            <template v-if="uploading">上传中...</template>
            <template v-else-if="form.cover && form.cover.startsWith('http')">
              <img :src="form.cover" alt="cover" style="max-height: 100%; max-width: 100%; object-fit: contain; border-radius: 4px;" />
            </template>
            <span v-else-if="form.cover" class="file-name">{{ form.cover }}</span>
            <span v-else>选择文件</span>
          </div>
          <input
            ref="fileInput"
            type="file"
            accept="image/*"
            style="display: none"
            @change="onFileChange"
          />
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="formVisible = false">取消</button>
          <button class="btn-save" :disabled="submitting" @click="handleSave">
            {{ submitting ? '保存中...' : '保存' }}
          </button>
        </div>
      </template>
    </el-dialog>

    <!-- 限制提示弹窗 -->
    <el-dialog
      v-model="restrictVisible"
      width="440px"
      :show-close="true"
      align-center
    >
      <template #header>
        <div class="dialog-header"><h3>编辑限制</h3></div>
      </template>
      <div class="dialog-body">
        <p class="dialog-text">该竞赛已有报名，报名时间/人数/参赛类型不可修改!</p>
      </div>
      <template #footer>
        <div class="dialog-footer center">
          <button class="btn-ok primary" @click="restrictVisible = false">确定</button>
        </div>
      </template>
    </el-dialog>

    <!-- 删除确认弹窗 -->
    <el-dialog
      v-model="deleteVisible"
      width="440px"
      :show-close="true"
      align-center
    >
      <template #header>
        <div class="dialog-header"><h3>确认删除</h3></div>
      </template>
      <div class="dialog-body">
        <p class="dialog-text">确定删除该竞赛吗？删除后无法恢复!</p>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="deleteVisible = false">取消</button>
          <button class="btn-ok primary" @click="confirmDelete">确定</button>
        </div>
      </template>
    </el-dialog>

    <!-- 操作成功弹窗 -->
    <el-dialog
      v-model="successVisible"
      width="440px"
      :show-close="true"
      align-center
    >
      <template #header>
        <div class="dialog-header"><h3>操作成功</h3></div>
      </template>
      <div class="dialog-body">
        <p class="dialog-text">{{ successText }}</p>
      </div>
      <template #footer>
        <div class="dialog-footer center">
          <button class="btn-ok primary" @click="successVisible = false">确定</button>
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

.btn-publish {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 36px;
  padding: 0 $space-5;
  border: none;
  background: linear-gradient(135deg, $primary, $primary-hover);
  color: #fff;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  border-radius: $radius-base;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(43, 108, 176, 0.25);
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

  span {
    font-size: 16px;
    font-weight: $font-weight-bold;
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(43, 108, 176, 0.35);

    &::after {
      transform: translateX(100%);
    }
  }

  &:active {
    transform: translateY(0) scale(0.97);
  }
}

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

.count-text {
  font-family: 'Consolas', monospace;
  color: $text-regular;
}

// ===== 表单弹窗 =====
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $space-4 $space-5;
  padding: $space-2 0;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: $space-1;
}

.form-item-full {
  grid-column: 1 / -1;
}

.form-label {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $text-regular;
}

.form-input {
  height: 38px;
  padding: 0 $space-3;
  border: 1.5px solid $border-base;
  background: $bg-card;
  border-radius: $radius-base;
  font-size: $font-size-sm;
  color: $text-primary;
  outline: none;
  transition: all $transition-fast;
  font-family: inherit;

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
  padding: $space-2 $space-3;
  resize: vertical;
  min-height: 80px;
  line-height: 1.6;
}

.error-text {
  margin: 0;
  font-size: $font-size-xs;
  color: $danger;
}

.upload-zone {
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1.5px dashed $border-base;
  border-radius: $radius-base;
  background: $bg-page;
  cursor: pointer;
  color: $text-secondary;
  font-size: $font-size-sm;
  transition: all $transition-fast;

  &:hover {
    border-color: $primary;
    background: $primary-50;
    color: $primary;
  }
}

.file-name {
  color: $primary;
  font-weight: $font-weight-medium;
}

// ===== 弹窗通用 =====
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
  padding: $space-3 0;
}

.dialog-text {
  margin: 0;
  font-size: $font-size-sm;
  color: $text-regular;
  line-height: 1.7;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: $space-3;
  padding-top: $space-3;

  &.center {
    justify-content: center;
  }
}

.btn-cancel {
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

.btn-save {
  height: 36px;
  padding: 0 $space-5;
  border: none;
  background: linear-gradient(135deg, $primary, $primary-hover);
  color: #fff;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  border-radius: $radius-base;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(43, 108, 176, 0.25);
  transition: all $transition-fast;
  &:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 6px 16px rgba(43, 108, 176, 0.35);
  }
  &:disabled {
    background: $bg-disabled;
    color: $text-disabled;
    cursor: not-allowed;
    box-shadow: none;
  }
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
  &:hover {
    background: $bg-page;
    border-color: $primary;
    color: $primary;
  }

  &.primary {
    min-width: 100px;
    background: linear-gradient(135deg, $primary, $primary-hover);
    color: #fff;
    border: none;
    box-shadow: 0 4px 12px rgba(43, 108, 176, 0.25);
    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 6px 16px rgba(43, 108, 176, 0.35);
      color: #fff;
    }
  }
}
</style>
