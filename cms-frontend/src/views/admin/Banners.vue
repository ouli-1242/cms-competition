<script setup lang="ts">
/**
 * 内容维护页（管理员）
 * - 轮播图管理
 * - 热门竞赛推荐
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getBanners, createBanner, deleteBanner } from '@/api/admin'

const loading = ref(false)
const bannerList = ref<any[]>([])

// 添加轮播图弹窗
const addVisible = ref(false)
const submitting = ref(false)
const bannerForm = reactive({
  imageUrl: '',
  link: ''
})
const bannerErrors = reactive({ imageUrl: '', link: '' })

// 删除确认
const deleteVisible = ref(false)
const deleteTarget = ref<any>(null)

// 保存成功弹窗
const saveVisible = ref(false)

// 热门竞赛推荐
const hotPicks = reactive({
  pinned: [] as number[],
  autoRecommend: false
})

const competitions = ref<any[]>([])

// 占位数据
const placeholderBanners = [
  { id: 1, imageUrl: 'https://via.placeholder.com/100x40?text=Banner1', link: 'https://www.baidu.com' }
]
const placeholderComps = [
  { id: 1, title: '程序设计竞赛' },
  { id: 2, title: '英语演讲比赛' },
  { id: 3, title: '软件设计竞赛' }
]

async function loadBanners() {
  loading.value = true
  try {
    const res: any = await getBanners()
    bannerList.value = res || []
  } catch (e) {
    bannerList.value = placeholderBanners
  } finally {
    loading.value = false
  }
}

function openAdd() {
  bannerForm.imageUrl = ''
  bannerForm.link = ''
  bannerErrors.imageUrl = ''
  bannerErrors.link = ''
  addVisible.value = true
}

const fileInput = ref<HTMLInputElement | null>(null)
function triggerUpload() {
  fileInput.value?.click()
}
function onFileChange(e: Event) {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (file) bannerForm.imageUrl = file.name
}

function validateBanner(): boolean {
  bannerErrors.imageUrl = ''
  bannerErrors.link = ''
  let ok = true
  if (!bannerForm.imageUrl) {
    bannerErrors.imageUrl = '请选择文件'
    ok = false
  }
  if (!bannerForm.link.trim()) {
    bannerErrors.link = '请输入跳转链接'
    ok = false
  } else if (!/^https?:\/\/.+/.test(bannerForm.link.trim())) {
    bannerErrors.link = '请输入合法的链接（http/https 开头）'
    ok = false
  }
  return ok
}

async function handleAddSave() {
  if (!validateBanner()) return
  submitting.value = true
  try {
    try {
      await createBanner({ imageUrl: bannerForm.imageUrl, link: bannerForm.link })
    } catch (e) {}
    bannerList.value.push({
      id: Date.now(),
      imageUrl: bannerForm.imageUrl,
      link: bannerForm.link
    })
    ElMessage.success('添加成功')
    addVisible.value = false
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
  bannerList.value = bannerList.value.filter((x) => x.id !== deleteTarget.value.id)
  ElMessage.success('删除成功')
  deleteVisible.value = false
  deleteTarget.value = null
}

function togglePinned(id: number) {
  const i = hotPicks.pinned.indexOf(id)
  if (i >= 0) {
    hotPicks.pinned.splice(i, 1)
  } else {
    hotPicks.pinned.push(id)
  }
}

function isPinned(id: number): boolean {
  return hotPicks.pinned.includes(id)
}

function saveHotPicks() {
  saveVisible.value = true
}

function openLink(link: string) {
  window.open(link, '_blank')
}

onMounted(() => {
  loadBanners()
  competitions.value = placeholderComps
})
</script>

<template>
  <div class="page">
    <h2 class="page-title">内容维护</h2>

    <!-- 轮播图管理 -->
    <div class="section">
      <div class="section-header">
        <h3 class="section-title">轮播图管理</h3>
        <button class="btn-add" @click="openAdd">
          <span>+</span>添加轮播图
        </button>
      </div>

      <el-table
        v-loading="loading"
        :data="bannerList"
        stripe
        class="data-table"
        :header-cell-style="{ background: '#f7fafc', color: '#4a5568', fontWeight: 600 }"
      >
        <el-table-column label="图片" width="140" align="center">
          <template #default="{ row }">
            <div class="img-placeholder">
              <span v-if="!row.imageUrl.startsWith('http')">📷</span>
              <img v-else :src="row.imageUrl" alt="banner" class="img-thumb" />
            </div>
          </template>
        </el-table-column>
        <el-table-column label="链接" min-width="200">
          <template #default="{ row }">
            <a :href="row.link" target="_blank" class="link-text">{{ row.link }}</a>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="warning" size="small" plain round @click="openLink(row.link)">跳转</el-button>
            <el-button type="danger" size="small" plain round @click="openDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无轮播图" />
        </template>
      </el-table>
    </div>

    <!-- 热门竞赛推荐 -->
    <div class="section">
      <h3 class="section-title">热门竞赛推荐</h3>
      <div class="hot-list">
        <label
          v-for="c in competitions"
          :key="c.id"
          class="hot-item"
        >
          <input
            type="checkbox"
            :checked="isPinned(c.id)"
            @change="togglePinned(c.id)"
            class="hot-checkbox"
          />
          <span class="hot-text">{{ c.title }}</span>
          <el-tag v-if="isPinned(c.id)" size="small" type="danger" effect="light" round class="top-tag">置顶</el-tag>
        </label>
        <label class="hot-item">
          <input
            v-model="hotPicks.autoRecommend"
            type="checkbox"
            class="hot-checkbox"
          />
          <span class="hot-text">自动推荐热门竞赛</span>
        </label>
      </div>
      <div class="hot-actions">
        <button class="btn-save" @click="saveHotPicks">
          保存（前台首页立即生效）
        </button>
      </div>
    </div>

    <!-- 添加轮播图 弹窗 -->
    <el-dialog
      v-model="addVisible"
      title="添加轮播图"
      width="560px"
      :close-on-click-modal="false"
      align-center
    >
      <div class="form-body">
        <div class="form-item">
          <label class="form-label">选择文件</label>
          <div class="upload-row">
            <button class="btn-upload" @click="triggerUpload">
              <span>📁</span>选择文件
            </button>
            <span class="file-info">{{ bannerForm.imageUrl || '未选择文件' }}</span>
          </div>
          <p v-if="bannerErrors.imageUrl" class="error-text">{{ bannerErrors.imageUrl }}</p>
          <input
            ref="fileInput"
            type="file"
            accept="image/*"
            style="display: none"
            @change="onFileChange"
          />
        </div>
        <div class="form-item">
          <label class="form-label">跳转链接</label>
          <input
            v-model="bannerForm.link"
            type="text"
            class="form-input"
            :class="{ 'is-error': bannerErrors.link }"
            placeholder="跳转链接"
          />
          <p v-if="bannerErrors.link" class="error-text">{{ bannerErrors.link }}</p>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="btn-ok gray" @click="addVisible = false">取消</button>
          <button class="btn-ok primary" :disabled="submitting" @click="handleAddSave">
            {{ submitting ? '保存中...' : '保存' }}
          </button>
        </div>
      </template>
    </el-dialog>

    <!-- 删除确认 弹窗 -->
    <el-dialog
      v-model="deleteVisible"
      width="440px"
      :show-close="true"
      align-center
    >
      <template #header>
        <div class="dialog-header"><h3>此页面显示</h3></div>
      </template>
      <div class="dialog-body">
        <p class="dialog-text">确定删除该轮播图吗？删除后无法恢复!</p>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="btn-ok gray" @click="deleteVisible = false">取消</button>
          <button class="btn-ok primary" @click="confirmDelete">确定</button>
        </div>
      </template>
    </el-dialog>

    <!-- 保存成功 弹窗 -->
    <el-dialog
      v-model="saveVisible"
      width="440px"
      :show-close="true"
      align-center
    >
      <template #header>
        <div class="dialog-header"><h3>此页面显示</h3></div>
      </template>
      <div class="dialog-body">
        <p class="dialog-text">内容保存成功，前台首页已生效!</p>
      </div>
      <template #footer>
        <div class="dialog-footer center">
          <button class="btn-ok primary" @click="saveVisible = false">确定</button>
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
  gap: $space-5;
}

.page-title {
  margin: 0 0 $space-2;
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.section {
  background: $bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;
  padding: $space-5;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $space-4;
}

.section-title {
  margin: 0;
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.btn-add {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 32px;
  padding: 0 $space-4;
  border: none;
  background: linear-gradient(135deg, $primary, $primary-hover);
  color: #fff;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  border-radius: $radius-base;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(43, 108, 176, 0.2);
  transition: all $transition-fast;

  span {
    font-size: 16px;
    font-weight: $font-weight-bold;
  }

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(43, 108, 176, 0.3);
  }
}

// ===== 表格 =====
.data-table {
  border-radius: $radius-base;
  overflow: hidden;
}

.img-placeholder {
  width: 80px;
  height: 32px;
  background: $bg-page;
  border: 1px solid $border-base;
  border-radius: $radius-sm;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: $text-placeholder;
  margin: 0 auto;
}

.img-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.link-text {
  color: $primary;
  font-size: $font-size-sm;
  text-decoration: none;
  &:hover {
    text-decoration: underline;
  }
}

// ===== 热门竞赛 =====
.hot-list {
  display: flex;
  flex-direction: column;
  gap: $space-3;
  padding: $space-3 0;
}

.hot-item {
  display: flex;
  align-items: center;
  gap: $space-3;
  cursor: pointer;
  font-size: $font-size-base;
  color: $text-primary;
  user-select: none;

  &:hover .hot-text {
    color: $primary;
  }
}

.hot-checkbox {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: $primary;
}

.hot-text {
  transition: color $transition-fast;
}

.top-tag {
  margin-left: 6px;
}

.hot-actions {
  margin-top: $space-4;
}

.btn-save {
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
  transition: all $transition-fast;
  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 6px 16px rgba(43, 108, 176, 0.35);
  }
}

// ===== 表单弹窗 =====
.form-body {
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

.upload-row {
  display: flex;
  align-items: center;
  gap: $space-3;
}

.btn-upload {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 36px;
  padding: 0 $space-4;
  border: 1.5px solid $border-base;
  background: $bg-card;
  color: $text-regular;
  font-size: $font-size-sm;
  border-radius: $radius-base;
  cursor: pointer;
  transition: all $transition-fast;
  &:hover {
    border-color: $primary;
    color: $primary;
    background: $primary-50;
  }
}

.file-info {
  font-size: $font-size-sm;
  color: $text-secondary;
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

  &:focus {
    border-color: $primary;
    box-shadow: 0 0 0 3px rgba(43, 108, 176, 0.08);
  }
  &.is-error {
    border-color: $danger;
    background: #fef5f5;
  }
}

.error-text {
  margin: 0;
  font-size: $font-size-xs;
  color: $danger;
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
</style>
