<script setup lang="ts">
/**
 * 数据统计页（教师）
 * - 概览统计卡片：指导竞赛数 / 报名总数 / 个人赛 / 团队赛 / 已通过 / 通过率
 * - 柱状图：各竞赛报名人数
 * - 通过率展示卡片
 */
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getTeacherStatistics,
  getTeacherCompetitions,
  getTeacherCompetitionStats
} from '@/api/registration'
import * as echarts from 'echarts'

const barRef = ref<HTMLDivElement | null>(null)
let barChart: echarts.ECharts | null = null

// 总览统计
const overview = ref({
  competitionCount: 0,
  totalCount: 0,
  individualCount: 0,
  teamCount: 0,
  passedCount: 0,
  passRate: 0
})

// 竞赛维度统计
interface CompetitionDetail {
  id: number
  title: string
  total: number
  passed: number
}
const detailList = ref<CompetitionDetail[]>([])

const loading = ref(false)

async function loadData() {
  loading.value = true

  // 1. 总览统计
  try {
    const data: any = await getTeacherStatistics()
    overview.value = {
      competitionCount: data?.competitionCount ?? 0,
      totalCount: data?.totalCount ?? 0,
      individualCount: data?.individualCount ?? 0,
      teamCount: data?.teamCount ?? 0,
      passedCount: data?.passedCount ?? 0,
      passRate: data?.passRate ?? 0
    }
  } catch {
    ElMessage.error('获取统计数据失败')
  }

  // 2. 竞赛列表
  let competitions: any[] = []
  try {
    const res: any = await getTeacherCompetitions({ pageNum: 1, pageSize: 100 })
    competitions = res?.records || []
  } catch {
    ElMessage.error('获取竞赛列表失败')
  }

  // 3. 逐个竞赛的统计明细
  const details: CompetitionDetail[] = []
  for (const comp of competitions) {
    try {
      const stat: any = await getTeacherCompetitionStats(comp.id)
      details.push({
        id: comp.id,
        title: comp.title || '未命名竞赛',
        total: stat?.totalCount ?? 0,
        passed: stat?.passedCount ?? 0
      })
    } catch {
      details.push({
        id: comp.id,
        title: comp.title || '未命名竞赛',
        total: 0,
        passed: 0
      })
    }
  }
  detailList.value = details

  loading.value = false

  // 4. 数据就绪后初始化图表
  setTimeout(() => {
    initBarChart()
  }, 100)
}

function initBarChart() {
  if (!barRef.value) return
  barChart = echarts.init(barRef.value)
  barChart.setOption({
    title: {
      text: '各竞赛报名人数',
      left: 'left',
      textStyle: { fontSize: 14, color: '#1a202c', fontWeight: 600 }
    },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: detailList.value.map((d) => d.title),
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisLabel: {
        color: '#4a5568',
        fontSize: 12,
        interval: 0,
        rotate: detailList.value.length > 5 ? 30 : 0
      }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#edf2f7', type: 'dashed' } },
      axisLabel: { color: '#4a5568' }
    },
    series: [
      {
        name: '报名总数',
        type: 'bar',
        barWidth: 40,
        data: detailList.value.map((d) => Math.floor(d.total)),
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: '#ed8936'
        },
        label: {
          show: true,
          position: 'top',
          color: '#1a202c',
          fontSize: 12,
          fontWeight: 600
        }
      },
      {
        name: '已通过',
        type: 'bar',
        barWidth: 40,
        data: detailList.value.map((d) => Math.floor(d.passed)),
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: '#48bb78'
        },
        label: {
          show: true,
          position: 'top',
          color: '#4a5568',
          fontSize: 12
        }
      }
    ]
  })
}

function resizeCharts() {
  barChart?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  barChart?.dispose()
})
</script>

<template>
  <div class="page" v-loading="loading">
    <h2 class="page-title">数据统计</h2>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-label">指导竞赛数</div>
        <div class="stat-value">{{ Math.floor(overview.competitionCount) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">报名总数</div>
        <div class="stat-value">{{ Math.floor(overview.totalCount) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">个人赛</div>
        <div class="stat-value">{{ Math.floor(overview.individualCount) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">团队赛</div>
        <div class="stat-value">{{ Math.floor(overview.teamCount) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">已通过</div>
        <div class="stat-value success">{{ Math.floor(overview.passedCount) }}</div>
      </div>
      <div class="stat-card highlight">
        <div class="stat-label">通过率</div>
        <div class="stat-value primary">{{ (overview.passRate * 100).toFixed(1) }}%</div>
      </div>
    </div>

    <!-- 柱状图 -->
    <div class="chart-card">
      <div ref="barRef" class="chart"></div>
    </div>

    <!-- 通过率展示卡片 -->
    <div class="summary-card">
      <div class="summary-left">
        <h3 class="summary-title">整体通过率</h3>
        <p class="summary-desc">
          共指导 <strong>{{ Math.floor(overview.competitionCount) }}</strong> 项竞赛，
          累计 <strong>{{ Math.floor(overview.totalCount) }}</strong> 人次报名，
          其中 <strong>{{ Math.floor(overview.passedCount) }}</strong> 人通过审核
        </p>
      </div>
      <div class="summary-right">
        <span class="pass-rate">{{ (overview.passRate * 100).toFixed(1) }}%</span>
      </div>
    </div>
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

// ===== 统计卡片 =====
.stat-cards {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: $space-4;
}

.stat-card {
  background: $bg-card;
  border-radius: $radius-md;
  padding: $space-4 $space-5;
  box-shadow: $shadow-sm;
  text-align: center;
  transition: box-shadow $transition-fast;

  &:hover {
    box-shadow: $shadow-base;
  }

  &.highlight {
    background: linear-gradient(135deg, #fff7ed, #fffaf0);
    border: 1px solid #fbd38d;
  }
}

.stat-label {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin-bottom: $space-2;
}

.stat-value {
  font-size: $font-size-3xl;
  font-weight: $font-weight-bold;
  color: $text-primary;
  font-family: 'Consolas', monospace;

  &.success {
    color: $success;
  }
  &.primary {
    color: #ed8936;
  }
}

// ===== 图表 =====
.chart-card {
  background: $bg-card;
  border-radius: $radius-md;
  padding: $space-4 $space-5;
  box-shadow: $shadow-sm;
}

.chart {
  width: 100%;
  height: 380px;
}

// ===== 通过率卡片 =====
.summary-card {
  background: $bg-card;
  border-radius: $radius-md;
  padding: $space-5 $space-6;
  box-shadow: $shadow-sm;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $space-6;
}

.summary-left {
  flex: 1;
}

.summary-title {
  margin: 0 0 $space-2;
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.summary-desc {
  margin: 0;
  font-size: $font-size-sm;
  color: $text-regular;
  line-height: 1.7;

  strong {
    color: $text-primary;
    font-weight: $font-weight-semibold;
  }
}

.summary-right {
  flex-shrink: 0;
}

.pass-rate {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100px;
  height: 100px;
  border-radius: $radius-full;
  background: linear-gradient(135deg, #f6ad55, #ed8936);
  color: #fff;
  font-size: $font-size-2xl;
  font-weight: $font-weight-bold;
  font-family: 'Consolas', monospace;
  box-shadow: 0 4px 16px rgba(237, 137, 54, 0.3);
}

@media (max-width: 1200px) {
  .stat-cards {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .stat-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  .summary-card {
    flex-direction: column;
    text-align: center;
  }
}
</style>
