<script setup lang="ts">
/**
 * 后台控制台（数据概览）
 * - 管理员 / 老师 复用
 * - 3 张统计卡片（来自真实 API）
 * - 汇总柱状图（展示聚合数据，非时间序列）
 */
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { getAdminStats } from '@/api/admin'
import { getTeacherStatistics } from '@/api/registration'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

const userStore = useUserStore()

// 角色判定
const isAdmin = computed(() => userStore.user?.role === 'ADMIN')

// API 返回的统计数据
const apiStats = ref<{
  competitionCount: number
  totalCount: number
  individualCount: number
  teamCount: number
  passedCount: number
  pendingCount: number
  passRate: number
}>({
  competitionCount: 0,
  totalCount: 0,
  individualCount: 0,
  teamCount: 0,
  passedCount: 0,
  pendingCount: 0,
  passRate: 0
})

// 管理员视角卡片
const adminCards = computed(() => [
  { label: '竞赛总数', value: apiStats.value.competitionCount, color: '#2b6cb0', bg: '#eaf2fb' },
  { label: '报名总数', value: apiStats.value.totalCount, color: '#f6ad55', bg: '#fff5e6' },
  { label: '已通过', value: apiStats.value.passedCount, color: '#48bb78', bg: '#f0faf4' },
  { label: '待审核', value: apiStats.value.pendingCount, color: '#e53e3e', bg: '#fef5f5' }
])

// 老师视角卡片
const teacherCards = computed(() => [
  { label: '指导竞赛数', value: apiStats.value.competitionCount, color: '#2b6cb0', bg: '#eaf2fb' },
  { label: '报名总数', value: apiStats.value.totalCount, color: '#f6ad55', bg: '#fff5e6' },
  { label: '待审核', value: apiStats.value.pendingCount, color: '#e53e3e', bg: '#fef5f5' },
  { label: '通过率', value: apiStats.value.passRate + '%', color: '#48bb78', bg: '#f0faf4' }
])

const stats = computed(() => (isAdmin.value ? adminCards.value : teacherCards.value))

// ECharts
const chartRef = ref<HTMLDivElement | null>(null)
let chartInstance: echarts.ECharts | null = null

function initChart() {
  if (!chartRef.value) return
  chartInstance = echarts.init(chartRef.value)
  const s = apiStats.value
  // 展示聚合数据柱状图（非时间序列趋势），各项含义见 x 轴标签
  const option = {
    title: {
      text: '报名数据汇总',
      left: 'left',
      textStyle: { fontSize: 14, color: '#1a202c' }
    },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ['个人报名', '团队报名', '已通过', '报名总数'],
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisLabel: { color: '#4a5568' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#edf2f7', type: 'dashed' } },
      axisLabel: { color: '#4a5568' }
    },
    series: [{
      data: [s.individualCount, s.teamCount, s.passedCount, s.totalCount],
      type: 'bar',
      barWidth: 32,
      itemStyle: {
        borderRadius: [8, 8, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#4a90e2' },
          { offset: 1, color: '#2b6cb0' }
        ])
      }
    }]
  }
  chartInstance.setOption(option)
}

function resizeChart() {
  chartInstance?.resize()
}

onMounted(async () => {
  try {
    const data = isAdmin.value
      ? await getAdminStats()
      : await getTeacherStatistics()
    apiStats.value = data as any
  } catch {
    ElMessage.error('获取统计数据失败')
  }
  // 等 DOM 渲染完成后再初始化图表
  setTimeout(initChart, 100)
  window.addEventListener('resize', resizeChart)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeChart)
  chartInstance?.dispose()
})
</script>

<template>
  <div class="dashboard">
    <h2 class="page-title">控制台 - 数据概览</h2>

    <!-- 统计卡片 -->
    <div class="stat-grid">
      <div v-for="(s, i) in stats" :key="i" class="stat-card" :style="{ borderColor: s.color }">
        <p class="stat-label" :style="{ color: s.color }">{{ s.label }}</p>
        <p class="stat-value" :style="{ color: s.color }">{{ s.value }}</p>
      </div>
    </div>

    <!-- 图表 -->
    <div class="chart-card">
      <div ref="chartRef" class="chart"></div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.dashboard {
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
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $space-4;
}

.stat-card {
  background: $bg-card;
  border-radius: $radius-md;
  padding: $space-5 $space-6;
  text-align: center;
  border-top: 3px solid;
  box-shadow: $shadow-sm;
  transition: all $transition-base;

  &:hover {
    transform: translateY(-2px);
    box-shadow: $shadow-md;
  }
}

.stat-label {
  margin: 0 0 $space-3;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
}

.stat-value {
  margin: 0;
  font-size: 36px;
  font-weight: $font-weight-bold;
  line-height: 1;
}

// ===== 图表 =====
.chart-card {
  background: $bg-card;
  border-radius: $radius-md;
  padding: $space-5;
  box-shadow: $shadow-sm;
}

.chart {
  width: 100%;
  height: 360px;
}

// ===== 响应式 =====
@media (max-width: 768px) {
  .stat-grid {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
