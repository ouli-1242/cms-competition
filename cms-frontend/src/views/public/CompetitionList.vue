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
import { getCompetitions } from '@/api/public'
import type { Competition, PageResult } from '@/api/public'

const router = useRouter()
const route = useRoute()

// ====== 筛选条件（从 query 初始化） ======
const filters = ref({
  keyword: (route.query.keyword as string) || '',
  category: (route.query.category as string) || '',
  status: route.query.status !== undefined ? Number(route.query.status) : ''
})

// ====== 数据 ======
const list = ref<Competition[]>([])
const total = ref(0)
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)

// ====== 筛选选项 ======
const categoryOptions = [
  { label: '全部', value: '' },
  { label: '数学建模', value: '数学建模' },
  { label: '程序设计', value: '程序设计' },
  { label: '电子设计', value: '电子设计' },
  { label: '挑战杯', value: '挑战杯' },
  { label: '互联网+', value: '互联网+' },
  { label: '机械创新', value: '机械创新' },
  { label: '英语竞赛', value: '英语竞赛' }
]
const statusOptions = [
  { label: '全部', value: '' },
  { label: '报名中', value: 1 },
  { label: '即将开始', value: 0 },
  { label: '已截止', value: 2 }
]

// ====== 占位数据 ======
const placeholder: Competition[] = [
  { id: 1, title: '2026 全国大学生算法编程挑战赛', category: '程序设计', organizer: '中国计算机学会', registerStart: '2026-02-15', registerEnd: '2026-05-31', contestTime: '2026-06-15', maxTeamSize: 3, status: 1, cover: '', description: '' },
  { id: 2, title: '2026 ACM 国际大学生程序设计竞赛', category: '程序设计', organizer: 'ACM 中国', registerStart: '2026-03-01', registerEnd: '2026-05-15', contestTime: '2026-06-20', maxTeamSize: 3, status: 1, cover: '', description: '' },
  { id: 3, title: '2026 全国大学生电子设计竞赛', category: '电子设计', organizer: '教育部', registerStart: '2026-04-01', registerEnd: '2026-05-20', contestTime: '2026-07-15', maxTeamSize: 3, status: 1, cover: '', description: '' },
  { id: 4, title: '"挑战杯"全国大学生创业计划竞赛', category: '挑战杯', organizer: '团中央', registerStart: '2026-03-15', registerEnd: '2026-06-30', contestTime: '2026-09-10', maxTeamSize: 8, status: 1, cover: '', description: '' },
  { id: 5, title: '2026 全国大学生英语竞赛', category: '英语竞赛', organizer: '外语教学协会', registerStart: '2026-02-01', registerEnd: '2026-04-30', contestTime: '2026-05-25', maxTeamSize: 1, status: 1, cover: '', description: '' },
  { id: 6, title: '2026 中国机器人大赛', category: '机械创新', organizer: '中国自动化学会', registerStart: '2026-04-01', registerEnd: '2026-06-15', contestTime: '2026-08-10', maxTeamSize: 5, status: 1, cover: '', description: '' },
  { id: 7, title: '2026 全国大学生物联网设计竞赛', category: '电子设计', organizer: '教育部', registerStart: '2026-03-01', registerEnd: '2026-05-31', contestTime: '2026-07-20', maxTeamSize: 3, status: 1, cover: '', description: '' },
  { id: 8, title: '"互联网+"大学生创新创业大赛', category: '互联网+', organizer: '教育部', registerStart: '2026-04-15', registerEnd: '2026-07-15', contestTime: '2026-10-20', maxTeamSize: 5, status: 1, cover: '', description: '' },
  { id: 9, title: '2026 全国大学生广告艺术大赛', category: '文化创意', organizer: '教育部', registerStart: '2026-03-01', registerEnd: '2026-06-15', contestTime: '2026-09-05', maxTeamSize: 5, status: 1, cover: '', description: '' },
  { id: 10, title: '2026 全国大学生物理竞赛', category: '物理', organizer: '物理学会', registerStart: '2026-02-15', registerEnd: '2026-05-10', contestTime: '2026-08-15', maxTeamSize: 3, status: 1, cover: '', description: '' },
  { id: 11, title: '2026 全国大学生化学竞赛', category: '化学', organizer: '化学学会', registerStart: '2026-03-01', registerEnd: '2026-05-20', contestTime: '2026-07-25', maxTeamSize: 3, status: 1, cover: '', description: '' },
  { id: 12, title: '2026 全国大学生生物竞赛', category: '生物', organizer: '生物学会', registerStart: '2026-04-01', registerEnd: '2026-06-10', contestTime: '2026-08-20', maxTeamSize: 3, status: 1, cover: '', description: '' },
  { id: 13, title: '2026 全国大学生市场调查与分析大赛', category: '经管', organizer: '教育部', registerStart: '2026-02-15', registerEnd: '2026-05-15', contestTime: '2026-07-30', maxTeamSize: 5, status: 1, cover: '', description: '' },
  { id: 14, title: '2026 全国大学生交通科技大赛', category: '交通', organizer: '交通部', registerStart: '2026-03-10', registerEnd: '2026-06-05', contestTime: '2026-07-15', maxTeamSize: 5, status: 1, cover: '', description: '' },
  { id: 15, title: '2026 全国大学生工程训练综合能力竞赛', category: '机械创新', organizer: '教育部', registerStart: '2026-03-20', registerEnd: '2026-06-20', contestTime: '2026-08-25', maxTeamSize: 4, status: 1, cover: '', description: '' },
  { id: 16, title: '2026 全国大学生节能减排社会实践与科技竞赛', category: '能源', organizer: '教育部', registerStart: '2026-04-01', registerEnd: '2026-06-30', contestTime: '2026-08-30', maxTeamSize: 5, status: 1, cover: '', description: '' }
]

// ====== 加载数据 ======
async function loadData() {
  loading.value = true
  try {
    const res: any = await getCompetitions({
      page: currentPage.value,
      size: pageSize.value,
      keyword: filters.value.keyword || undefined,
      category: filters.value.category || undefined,
      status: filters.value.status === '' ? undefined : (filters.value.status as number)
    })
    list.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    // API 失败用占位
    list.value = paginate(placeholder, currentPage.value, pageSize.value)
    total.value = placeholder.length
  } finally {
    loading.value = false
  }
}

function paginate<T>(arr: T[], page: number, size: number): T[] {
  const start = (page - 1) * size
  return arr.slice(start, start + size)
}

function onSearch() {
  currentPage.value = 1
  loadData()
  // 同步到 URL
  router.replace({
    query: {
      keyword: filters.value.keyword || undefined,
      category: filters.value.category || undefined,
      status: filters.value.status === '' ? undefined : String(filters.value.status)
    }
  })
}

function onPageChange(p: number) {
  currentPage.value = p
  loadData()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 状态文案/颜色
function statusText(s?: number) {
  return s === 1 ? '报名中' : s === 2 ? '已截止' : '即将开始'
}
function statusType(s?: number) {
  return s === 1 ? 'success' : s === 2 ? 'info' : 'warning'
}

// 卡片封面渐变
function coverStyle(title: string) {
  const colors = [
    ['#4a5568', '#2d3748'],
    ['#2b6cb0', '#2c5282'],
    ['#d69e2e', '#b7791f'],
    ['#9f7aea', '#6b46c1'],
    ['#e53e3e', '#c53030'],
    ['#48bb78', '#2f855a']
  ]
  const idx = (title?.charCodeAt(0) || 0) % colors.length
  return {
    background: `linear-gradient(135deg, ${colors[idx][0]}, ${colors[idx][1]})`
  }
}

// 监听 query 变化（从首页跳转过来）
watch(
  () => route.query,
  () => {
    filters.value.keyword = (route.query.keyword as string) || ''
    filters.value.category = (route.query.category as string) || ''
    filters.value.status = route.query.status !== undefined ? Number(route.query.status) : ('' as any)
    loadData()
  }
)

onMounted(() => {
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
      <div class="sort-tabs">
        <button class="sort-tab active">综合排序</button>
        <button class="sort-tab">人气</button>
      </div>
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
          <div class="comp-cover" :style="coverStyle(c.title)">
            <span class="cover-text">{{ c.title.slice(0, 2) }}</span>
            <el-tag class="status-tag" :type="statusType(c.status)" effect="dark" round size="small">
              {{ statusText(c.status) }}
            </el-tag>
          </div>
          <div class="comp-body">
            <h3 class="comp-title">{{ c.title }}</h3>
            <p class="comp-organizer">🏛 {{ c.organizer }}</p>
            <div class="comp-footer">
              <span class="comp-count">👥 {{ 1024 + c.id * 17 }}人</span>
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

.sort-tabs {
  display: flex;
  gap: $space-3;
}

.sort-tab {
  border: none;
  background: transparent;
  color: $text-secondary;
  font-size: $font-size-sm;
  cursor: pointer;
  padding: $space-1 $space-2;

  &.active {
    color: $primary;
    font-weight: $font-weight-medium;
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
  &:hover {
    transform: translateY(-4px);
    box-shadow: $shadow-md;
  }
}

.comp-cover {
  height: 130px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 36px;
  font-weight: $font-weight-bold;
  letter-spacing: 4px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  position: relative;

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(to bottom, transparent 50%, rgba(0, 0, 0, 0.2));
  }
}

.cover-text {
  position: relative;
  z-index: 1;
}

.status-tag {
  position: absolute;
  top: $space-2;
  right: $space-2;
  z-index: 2;
  backdrop-filter: blur(8px);
  background: rgba(0, 0, 0, 0.4) !important;
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
