<script setup lang="ts">
/**
 * 系统首页
 * - Hero 区域：当前重点竞赛推广
 * - 搜索筛选：类别 / 状态 / 关键词
 * - 热门竞赛
 * - 最新竞赛
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getActiveBanners, getActiveHot, getCompetitions } from '@/api/public'
import type { Banner, Competition } from '@/api/public'

const router = useRouter()

// ====== 数据 ======
const banner = ref<Banner | null>(null)
const hotList = ref<Competition[]>([])
const latestList = ref<Competition[]>([])

// ====== 搜索筛选 ======
const filters = ref({
  category: '',
  status: '',
  keyword: ''
})
const categoryOptions = [
  { label: '全部类别', value: '' },
  { label: '数学建模', value: '数学建模' },
  { label: '程序设计', value: '程序设计' },
  { label: '电子设计', value: '电子设计' },
  { label: '挑战杯', value: '挑战杯' },
  { label: '互联网+', value: '互联网+' },
  { label: '机械创新', value: '机械创新' },
  { label: '英语竞赛', value: '英语竞赛' }
]
const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '报名中', value: 1 },
  { label: '即将开始', value: 0 },
  { label: '已截止', value: 2 }
]

function onSearch() {
  router.push({
    path: '/competitions',
    query: {
      keyword: filters.value.keyword || undefined,
      category: filters.value.category || undefined,
      status: filters.value.status || undefined
    }
  })
}

// ====== 加载数据 ======
async function loadData() {
  try {
    // 加载首张轮播图作为 Hero
    const banners = (await getActiveBanners()) || []
    banner.value = banners[0] || null

    // 热门
    const hotRes: any = await getCompetitions({ page: 1, size: 6, status: 1 })
    hotList.value = hotRes.list || []

    // 最新
    const latestRes: any = await getCompetitions({ page: 1, size: 8 })
    latestList.value = latestRes.list || []
  } catch (e) {
    // 静默失败，使用占位
  }
}

// ====== 占位数据（API 未通时）======
const placeholderHot: Competition[] = [
  { id: 1, title: '全国大学生数学建模竞赛', category: '数学建模', organizer: '教育部', registerStart: '2026-06-01', registerEnd: '2026-06-30', contestTime: '2026-09-15', maxTeamSize: 3, status: 1, cover: '', description: '' },
  { id: 2, title: 'ACM 国际大学生程序设计竞赛', category: '程序设计', organizer: 'ACM 中国', registerStart: '2026-05-01', registerEnd: '2026-07-15', contestTime: '2026-10-20', maxTeamSize: 3, status: 1, cover: '', description: '' },
  { id: 3, title: '全国大学生电子设计竞赛', category: '电子设计', organizer: '教育部', registerStart: '2026-06-15', registerEnd: '2026-08-10', contestTime: '2026-08-30', maxTeamSize: 3, status: 1, cover: '', description: '' },
  { id: 4, title: '全国大学生英语竞赛', category: '英语竞赛', organizer: '外语教学协会', registerStart: '2026-04-01', registerEnd: '2026-05-15', contestTime: '2026-05-25', maxTeamSize: 1, status: 1, cover: '', description: '' },
  { id: 5, title: '"挑战杯"全国大学生创业计划竞赛', category: '挑战杯', organizer: '团中央', registerStart: '2026-07-01', registerEnd: '2026-08-30', contestTime: '2026-11-15', maxTeamSize: 8, status: 1, cover: '', description: '' },
  { id: 6, title: '中国机器人大赛', category: '机械创新', organizer: '中国自动化学会', registerStart: '2026-06-01', registerEnd: '2026-07-31', contestTime: '2026-10-15', maxTeamSize: 5, status: 1, cover: '', description: '' }
]

const placeholderLatest: Competition[] = [
  { id: 7, title: '全国大学生广告艺术大赛', category: '文化创意', organizer: '教育部', registerStart: '2026-06-01', registerEnd: '2026-07-15', contestTime: '2026-09-10', maxTeamSize: 5, status: 1, cover: '', description: '' },
  { id: 8, title: '全国大学生物理竞赛', category: '物理', organizer: '物理学会', registerStart: '2026-05-15', registerEnd: '2026-06-30', contestTime: '2026-08-15', maxTeamSize: 3, status: 1, cover: '', description: '' },
  { id: 9, title: '全国大学生化学竞赛', category: '化学', organizer: '化学学会', registerStart: '2026-05-01', registerEnd: '2026-06-20', contestTime: '2026-07-25', maxTeamSize: 3, status: 1, cover: '', description: '' },
  { id: 10, title: '全国大学生生物竞赛', category: '生物', organizer: '生物学会', registerStart: '2026-06-01', registerEnd: '2026-07-10', contestTime: '2026-08-20', maxTeamSize: 3, status: 1, cover: '', description: '' },
  { id: 11, title: '全国大学生市场调查与分析大赛', category: '经管', organizer: '教育部', registerStart: '2026-04-15', registerEnd: '2026-06-15', contestTime: '2026-07-30', maxTeamSize: 5, status: 1, cover: '', description: '' },
  { id: 12, title: '全国大学生交通科技大赛', category: '交通', organizer: '交通部', registerStart: '2026-05-10', registerEnd: '2026-06-25', contestTime: '2026-07-15', maxTeamSize: 5, status: 1, cover: '', description: '' }
]

onMounted(() => {
  loadData()
})

// 渲染用
const displayHot = ref<Competition[]>([])
const displayLatest = ref<Competition[]>([])
onMounted(() => {
  // 当 API 加载完毕后，替换占位
  setTimeout(() => {
    if (hotList.value.length === 0) displayHot.value = placeholderHot
    else displayHot.value = hotList.value

    if (latestList.value.length === 0) displayLatest.value = placeholderLatest
    else displayLatest.value = latestList.value
  }, 200)
})

// 状态标签
function statusText(s?: number) {
  return s === 1 ? '报名中' : s === 2 ? '已截止' : '即将开始'
}
function statusType(s?: number) {
  return s === 1 ? 'success' : s === 2 ? 'info' : 'warning'
}

// 卡片封面渐变色（按标题首个字 hash 选色）
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
</script>

<template>
  <div class="home">
    <!-- ====== Hero 区域 ====== -->
    <section class="hero">
      <div class="hero-bg">
        <div class="hero-circle hero-circle-1"></div>
        <div class="hero-circle hero-circle-2"></div>
        <div class="hero-circle hero-circle-3"></div>
      </div>
      <div class="hero-content">
        <h1 class="hero-title">
          {{ banner?.title || '2026 全国大学生数学建模竞赛' }}
        </h1>
        <p class="hero-subtitle">
          <span class="dot"></span>
          报名火热进行中（截止日期：2026年6月30日）
          <span class="dot"></span>
        </p>
        <div class="hero-dots">
          <span class="dot active"></span>
          <span class="dot"></span>
          <span class="dot"></span>
          <span class="dot"></span>
        </div>
        <button class="hero-btn" @click="router.push('/competitions/1')">立即报名</button>
      </div>
    </section>

    <!-- ====== 搜索筛选 ====== -->
    <section class="search-bar">
      <div class="search-inner">
        <div class="filter-group">
          <span class="filter-label">竞赛类别</span>
          <el-select v-model="filters.category" placeholder="全部类别" size="default" class="filter-select">
            <el-option
              v-for="opt in categoryOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </div>
        <div class="filter-group">
          <span class="filter-label">报名状态</span>
          <el-select v-model="filters.status" placeholder="全部状态" size="default" class="filter-select">
            <el-option
              v-for="opt in statusOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </div>
        <div class="filter-group filter-search">
          <el-input
            v-model="filters.keyword"
            placeholder="搜索竞赛名称、关键词"
            clearable
            size="default"
          />
        </div>
        <button class="search-btn" @click="onSearch">搜索</button>
      </div>
    </section>

    <!-- ====== 热门竞赛 ====== -->
    <section class="section">
      <div class="section-header">
        <h2 class="section-title">
          <span class="bar bar-orange"></span>
          热门竞赛
        </h2>
        <a class="section-more" @click.prevent="router.push('/competitions')">
          查看更多 →
        </a>
      </div>
      <el-row :gutter="16">
        <el-col
          v-for="c in displayHot"
          :key="c.id"
          :xs="12"
          :sm="8"
          :md="8"
          :lg="8"
        >
          <div class="comp-card" @click="router.push(`/competitions/${c.id}`)">
            <div class="comp-cover" :style="coverStyle(c.title)">
              <span class="cover-text">{{ c.title.slice(0, 2) }}</span>
            </div>
            <div class="comp-body">
              <h3 class="comp-title">{{ c.title }}</h3>
              <div class="comp-tags">
                <el-tag size="small" effect="plain" round class="cat-tag">{{ c.category }}</el-tag>
                <el-tag size="small" :type="statusType(c.status)" effect="plain" round class="status-tag">
                  {{ statusText(c.status) }}
                </el-tag>
              </div>
              <div class="comp-footer">
                <span class="comp-people">👥 {{ 1024 + c.id * 8 }}人</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </section>

    <!-- ====== 最新竞赛 ====== -->
    <section class="section">
      <div class="section-header">
        <h2 class="section-title">
          <span class="bar bar-green"></span>
          最新竞赛
        </h2>
        <a class="section-more" @click.prevent="router.push('/competitions')">
          查看更多 →
        </a>
      </div>
      <el-row :gutter="16">
        <el-col
          v-for="c in displayLatest"
          :key="c.id"
          :xs="12"
          :sm="8"
          :md="8"
          :lg="8"
        >
          <div class="comp-card" @click="router.push(`/competitions/${c.id}`)">
            <div class="comp-cover" :style="coverStyle(c.title)">
              <span class="cover-text">{{ c.title.slice(0, 2) }}</span>
            </div>
            <div class="comp-body">
              <h3 class="comp-title">{{ c.title }}</h3>
              <div class="comp-tags">
                <el-tag size="small" effect="plain" round class="cat-tag">{{ c.category }}</el-tag>
                <el-tag size="small" :type="statusType(c.status)" effect="plain" round class="status-tag">
                  {{ statusText(c.status) }}
                </el-tag>
              </div>
              <div class="comp-footer">
                <span class="comp-people">👥 {{ 856 + c.id * 11 }}人</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </section>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.home {
  max-width: 1280px;
  margin: 0 auto;
  padding: $space-4 $space-6;
}

// ====== Hero ======
.hero {
  position: relative;
  height: 280px;
  border-radius: $radius-md;
  background: linear-gradient(135deg, #2b6cb0 0%, #3182ce 50%, #4299e1 100%);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-bottom: $space-5;
  box-shadow: 0 8px 24px rgba(43, 108, 176, 0.25);
}

.hero-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.hero-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}
.hero-circle-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  left: -80px;
}
.hero-circle-2 {
  width: 200px;
  height: 200px;
  bottom: -60px;
  right: -40px;
  background: rgba(255, 255, 255, 0.06);
}
.hero-circle-3 {
  width: 150px;
  height: 150px;
  top: 30%;
  right: 20%;
  background: rgba(255, 255, 255, 0.05);
}

.hero-content {
  position: relative;
  text-align: center;
  z-index: 1;
}

.hero-title {
  margin: 0 0 $space-3;
  font-size: 32px;
  font-weight: $font-weight-bold;
  letter-spacing: 2px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.hero-subtitle {
  margin: 0 0 $space-3;
  font-size: $font-size-md;
  display: inline-flex;
  align-items: center;
  gap: $space-2;
  opacity: 0.95;

  .dot {
    width: 4px;
    height: 4px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.5);
  }
}

.hero-dots {
  display: flex;
  justify-content: center;
  gap: $space-2;
  margin-bottom: $space-5;

  .dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.4);
    transition: all $transition-fast;
    cursor: pointer;
    &.active {
      width: 24px;
      border-radius: 3px;
      background: #fff;
    }
    &:hover {
      background: rgba(255, 255, 255, 0.7);
    }
  }
}

.hero-btn {
  padding: 10px 32px;
  background: #fff;
  color: $primary;
  border: none;
  border-radius: $radius-full;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all $transition-fast;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
  }
  &:active {
    transform: scale(0.97);
  }
}

// ====== 搜索筛选 ======
.search-bar {
  background: $bg-card;
  border-radius: $radius-md;
  padding: $space-3 $space-5;
  margin-bottom: $space-6;
  box-shadow: $shadow-sm;
}

.search-inner {
  display: flex;
  align-items: center;
  gap: $space-3;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: $space-2;
  min-width: 180px;
}

.filter-search {
  flex: 1;
  min-width: 220px;
}

.filter-label {
  font-size: $font-size-sm;
  color: $text-secondary;
  white-space: nowrap;
  flex-shrink: 0;
}

.filter-select {
  flex: 1;
  min-width: 120px;
}

.search-btn {
  height: 32px;
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

// ====== 通用 Section ======
.section {
  margin-bottom: $space-8;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $space-4;
}

.section-title {
  display: flex;
  align-items: center;
  gap: $space-2;
  margin: 0;
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.bar {
  display: inline-block;
  width: 4px;
  height: 18px;
  border-radius: 2px;
  background: $primary;
}
.bar-orange {
  background: linear-gradient(180deg, $warning, #ed8936);
}
.bar-green {
  background: linear-gradient(180deg, $success, #38a169);
}

.section-more {
  font-size: $font-size-sm;
  color: $primary;
  cursor: pointer;
  transition: opacity $transition-fast;
  &:hover {
    opacity: 0.7;
  }
}

// ====== 竞赛卡片 ======
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
  height: 120px;
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
    background: linear-gradient(to bottom, transparent 60%, rgba(0, 0, 0, 0.15));
  }
}

.cover-text {
  position: relative;
  z-index: 1;
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

.comp-tags {
  display: flex;
  gap: $space-2;
  margin-bottom: $space-2;
  flex-wrap: wrap;
}

.cat-tag {
  background: $primary-50 !important;
  color: $primary !important;
  border-color: transparent !important;
}

.status-tag {
  font-size: $font-size-xs !important;
}

.comp-footer {
  display: flex;
  justify-content: space-between;
  font-size: $font-size-xs;
  color: $text-secondary;
}

.comp-people {
  display: flex;
  align-items: center;
  gap: 4px;
}

// ====== 响应式 ======
@media (max-width: 768px) {
  .home {
    padding: $space-3 $space-4;
  }
  .hero {
    height: 220px;
  }
  .hero-title {
    font-size: 22px;
  }
  .search-inner {
    flex-direction: column;
    align-items: stretch;
  }
  .filter-group,
  .filter-search {
    width: 100%;
    min-width: 0;
  }
}
</style>
