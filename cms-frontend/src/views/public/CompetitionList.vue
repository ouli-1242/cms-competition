<script setup lang="ts">
/**
 * 竞赛列表页
 * - 顶部返回栏（页面标题在 PublicLayout 中已显示）
 * - 搜索筛选：关键词 + 类别 + 状态
 * - 卡片网格（3 列 / 2 列 / 1 列 响应式）
 * - 分页
 */
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getCompetitions } from '@/api/public'
import type { Competition, PageResult } from '@/api/public'
import { competitionStatusText, competitionStatusType } from '@/utils/status'
import { coverStyle } from '@/utils/cover'


const router = useRouter()
const route = useRoute()

// ====== 筛选条件（从 query 初始化） ======
const filters = ref({
  keyword: (route.query.keyword as string) || '',
  category: (route.query.category as string) || '',
  type: route.query.type !== undefined ? Number(route.query.type) : '',
  status: route.query.status !== undefined ? Number(route.query.status) : ''
})

// ====== 数据 ======
const list = ref<Competition[]>([])
const total = ref(0)
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)

// ====== 筛选选项 ======
const categoryOptions = ref<{ label: string; value: string }[]>([{ label: '全部', value: '' }])

// 动态加载类别列表
async function loadCategories() {
  try {
    const res: any = await getCompetitions({ pageNum: 1, pageSize: 100 })
    const categories = new Set<string>()
    ;(res?.records || []).forEach((c: Competition) => {
      if (c.category) categories.add(c.category)
    })
    categoryOptions.value = [
      { label: '全部', value: '' },
      ...Array.from(categories).map(c => ({ label: c, value: c }))
    ]
  } catch { /* 忽略 */ }
}
const statusOptions = [
  { label: '全部', value: '' },
  { label: '报名中', value: 1 },
  { label: '即将开始', value: 0 },
  { label: '已截止', value: 2 }
]
const typeOptions = [
  { label: '全部', value: '' },
  { label: '个人赛', value: 1 },
  { label: '团队赛', value: 2 }
]

// ====== 加载数据 ======
async function loadData() {
  loading.value = true
  try {
    const res: any = await getCompetitions({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      keyword: filters.value.keyword || undefined,
      category: filters.value.category || undefined,
      type: filters.value.type !== '' ? Number(filters.value.type) : undefined,
      registrationStatus: filters.value.status !== '' ? Number(filters.value.status) : undefined
    })

    list.value = res?.records || []
    total.value = res?.total || 0
  } catch (e: any) {
    ElMessage.error('加载竞赛列表失败：' + (e?.message || '未知错误'))
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 状态标签 → 使用 src/utils/status.ts 统一函数
const statusText = competitionStatusText
const statusType = competitionStatusType

function onSearch() {
  currentPage.value = 1
  loadData()
  // 同步到 URL
  router.replace({
    query: {
      keyword: filters.value.keyword || undefined,
      category: filters.value.category || undefined,
      type: filters.value.type === '' ? undefined : String(filters.value.type),
      status: filters.value.status === '' ? undefined : String(filters.value.status)
    }
  })
}

function onPageChange(p: number) {
  currentPage.value = p
  loadData()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 监听 query 变化（从首页跳转过来）
watch(
  () => route.query,
  () => {
    filters.value.keyword = (route.query.keyword as string) || ''
    filters.value.category = (route.query.category as string) || ''
    filters.value.type = route.query.type !== undefined ? Number(route.query.type) : ('' as any)
    filters.value.status = route.query.status !== undefined ? Number(route.query.status) : ('' as any)
    currentPage.value = 1
    loadData()
  }
)

onMounted(() => {
  loadCategories()
  loadData()
})
</script>

<template>
  <div class="list-page">
    <!-- 顶部返回栏 -->
    <div class="page-header">
      <button class="back-btn" @click="router.back()">
        <span>←</span>
      </button>
      <h1 class="page-title">竞赛平台</h1>
      <div class="placeholder-icon"></div>
    </div>

    <!-- 搜索筛选 -->
    <div class="search-bar">
      <div class="search-row">
        <div class="search-input-wrap">
          <el-icon class="search-icon"><Search /></el-icon>
          <input
            v-model="filters.keyword"
            type="text"
            class="search-input"
            placeholder="搜索竞赛名称、主办方..."
            @keyup.enter="onSearch"
          />
        </div>
        <button class="search-btn" @click="onSearch">搜索</button>
      </div>
      <div class="filter-row">
        <span class="filter-label">类别：</span>
        <div class="filter-tags">
          <button
            v-for="opt in categoryOptions"
            :key="opt.value"
            class="filter-tag"
            :class="{ active: filters.category === opt.value }"
            @click="filters.category = opt.value; onSearch()"
          >
            {{ opt.label }}
          </button>
        </div>
      </div>
      <div class="filter-row">
        <span class="filter-label">赛制：</span>
        <div class="filter-tags">
          <button
            v-for="opt in typeOptions"
            :key="opt.value"
            class="filter-tag"
            :class="{ active: filters.type === opt.value }"
            @click="filters.type = opt.value as any; onSearch()"
          >
            {{ opt.label }}
          </button>
        </div>
      </div>
      <div class="filter-row">
        <span class="filter-label">状态：</span>
        <div class="filter-tags">
          <button
            v-for="opt in statusOptions"
            :key="opt.value"
            class="filter-tag"
            :class="{ active: filters.status === opt.value }"
            @click="filters.status = opt.value as any; onSearch()"
          >
            {{ opt.label }}
          </button>
        </div>
      </div>
    </div>

    <!-- 标题行 -->
    <div class="result-header">
      <h2>共找到 <span class="count">{{ total }}</span> 个竞赛</h2>
    </div>

    <!-- 卡片网格 -->
    <el-row v-loading="loading" :gutter="16" class="card-grid">
      <el-col
        v-for="c in list"
        :key="c.id"
        :xs="12"
        :sm="12"
        :md="8"
        :lg="8"
      >
        <div class="comp-card" @click="router.push(`/competitions/${c.id}`)">
          <div class="comp-cover" :style="c.cover ? undefined : coverStyle(c.title)">
            <img v-if="c.cover" :src="c.cover" :alt="c.title" class="cover-img" />
            <span v-else class="cover-text">{{ c.title.slice(0, 2) }}</span>
            <el-tag class="status-tag" :type="statusType(c)" effect="dark" round size="small">
              {{ statusText(c) }}
            </el-tag>
          </div>
          <div class="comp-body">
            <h3 class="comp-title">{{ c.title }}</h3>
            <p class="comp-organizer">{{ c.category }}</p>
            <div class="comp-footer">
              <span class="comp-count">{{ c.registrationCount ?? 0 }}{{ c.type === 2 ? '队' : '人' }}参赛</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 空状态 -->
    <el-empty v-if="!loading && list.length === 0" description="没有找到匹配的竞赛" />

    <!-- 分页 -->
    <div v-if="total > pageSize" class="pagination">
      <el-pagination
        background
        layout="prev, pager, next, jumper"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.list-page {
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
  transition: all $transition-fast;
  display: flex;
  align-items: center;
  justify-content: center;

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

.placeholder-icon {
  width: 36px;
  height: 36px;
}

// ===== 搜索筛选 =====
.search-bar {
  background: $bg-card;
  border-radius: $radius-md;
  padding: $space-4;
  margin-bottom: $space-5;
  box-shadow: $shadow-sm;
}

.search-row {
  display: flex;
  gap: $space-3;
  margin-bottom: $space-3;
}

.search-input-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  gap: $space-2;
  height: 40px;
  padding: 0 $space-3;
  background: $bg-page;
  border-radius: $radius-base;
  transition: all $transition-fast;

  &:focus-within {
    background: $bg-card;
    box-shadow: 0 0 0 3px rgba(43, 108, 176, 0.08);
  }
}

.search-icon {
  color: $text-placeholder;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: $font-size-base;
  color: $text-primary;

  &::placeholder {
    color: $text-placeholder;
  }
}

.search-btn {
  height: 40px;
  padding: 0 $space-6;
  background: $primary;
  color: #fff;
  border: none;
  border-radius: $radius-base;
  font-size: $font-size-base;
  cursor: pointer;
  transition: all $transition-fast;
  flex-shrink: 0;

  &:hover {
    background: $primary-hover;
  }
}

.filter-row {
  display: flex;
  align-items: center;
  gap: $space-2;
  margin-top: $space-2;
  flex-wrap: wrap;
}

.filter-label {
  font-size: $font-size-sm;
  color: $text-secondary;
  white-space: nowrap;
  flex-shrink: 0;
  min-width: 40px;
}

.filter-tags {
  display: flex;
  gap: $space-2;
  flex-wrap: wrap;
}

.filter-tag {
  height: 28px;
  padding: 0 12px;
  border: 1px solid $border-base;
  background: $bg-card;
  color: $text-regular;
  border-radius: $radius-full;
  font-size: $font-size-sm;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover {
    border-color: $primary;
    color: $primary;
  }

  &.active {
    background: $primary;
    border-color: $primary;
    color: #fff;
  }
}

// ===== 结果头 =====
.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $space-4;

  h2 {
    margin: 0;
    font-size: $font-size-md;
    font-weight: $font-weight-medium;
    color: $text-regular;
  }

  .count {
    color: $primary;
    font-weight: $font-weight-semibold;
  }
}

// ===== 卡片网格 =====
.card-grid {
  min-height: 200px;
}

.comp-card {
  background: $bg-card;
  border-radius: $radius-md;
  overflow: hidden;
  cursor: pointer;
  transition: all $transition-base;
  box-shadow: $shadow-sm;
  margin-bottom: $space-4;
  border: 1px solid $border-light;

  &:hover {
    transform: translateY(-4px);
    box-shadow: $shadow-md;
    border-color: $primary-100;

    .cover-img {
      transform: scale(1.05);
    }
  }
}

.comp-cover {
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 36px;
  font-weight: $font-weight-bold;
  letter-spacing: 4px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(to bottom, transparent 50%, rgba(0, 0, 0, 0.25));
    z-index: 1;
  }
}

.cover-text {
  position: relative;
  z-index: 1;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  position: absolute;
  inset: 0;
  transition: transform 0.4s ease;
}

.status-tag {
  position: absolute;
  top: $space-2;
  right: $space-2;
  z-index: 2;
  backdrop-filter: blur(6px);
  background: rgba(0, 0, 0, 0.45) !important;
  border: none !important;
  color: #fff !important;
}

.comp-body {
  padding: $space-3 $space-4;
}

.comp-title {
  margin: 0 0 $space-2;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $text-primary;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.comp-organizer {
  margin: 0 0 $space-2;
  font-size: $font-size-xs;
  color: $text-secondary;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.comp-footer {
  display: flex;
  justify-content: space-between;
  font-size: $font-size-xs;
  color: $text-secondary;
}

.comp-count {
  display: flex;
  align-items: center;
  gap: 4px;
}

// ===== 分页 =====
.pagination {
  display: flex;
  justify-content: center;
  margin-top: $space-6;
}

// ===== 响应式 =====
@media (max-width: 768px) {
  .list-page {
    padding: $space-3 $space-4 $space-8;
  }
  .search-bar {
    padding: $space-3;
  }
  .filter-row {
    overflow-x: auto;
    flex-wrap: nowrap;
    .filter-tags {
      flex-wrap: nowrap;
    }
  }
}
</style>
