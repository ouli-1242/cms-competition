<script setup lang="ts">
/**
 * 后台控制台（数据概览）
 * - 管理员 / 老师 复用
 * - 3 张统计卡片
 * - 占位图表（用 ECharts 渐变柱图）
 */
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import * as echarts from 'echarts'

const userStore = useUserStore()

// 角色判定
const isAdmin = computed(() => userStore.user?.role === 'ADMIN')

// 统计卡片（管理员视角）
const adminStats = ref([
  { label: '竞赛总数', value: 46, color: '#2b6cb0', bg: '#eaf2fb' },
  { label: '待审核数', value: 15, color: '#f6ad55', bg: '#fff5e6' },
  { label: '今日报名数', value: 28, color: '#48bb78', bg: '#f0faf4' }
])

// 老师视角
const teacherStats = ref([
  { label: '指导竞赛数', value: 6, color: '#2b6cb0', bg: '#eaf2fb' },
  { label: '待审核报名', value: 12, color: '#f6ad55', bg: '#fff5e6' },
  { label: '已通过报名', value: 38, color: '#48bb78', bg: '#f0faf4' }
])

const stats = computed(() => (isAdmin.value ? adminStats.value : teacherStats.value))

// ECharts
const chartRef = ref<HTMLDivElement | null>(null)
let chartInstance: echarts.ECharts | null = null

function initChart() {
  if (!chartRef.value) return
  chartInstance = echarts.init(chartRef.value)
  const option = {
    title: {
      text: '近 7 日报名趋势',
      left: 'left',
      textStyle: { fontSize: 14, color: '#1a202c' }
    },
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
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
      data: [10, 18, 14, 22, 28, 24, 32],
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
  window.addEventListener('resize', resizeChart)
}

function resizeChart() {
  chartInstance?.resize()
}

onMounted(() => {
  setTimeout(initChart, 100)
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

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <h3 class="section-title">快捷操作</h3>
      <div class="action-grid">
        <template v-if="isAdmin">
          <router-link to="/admin/competitions" class="action-item">
            <span class="action-icon" style="background: #eaf2fb; color: #2b6cb0">📅</span>
            <span class="action-name">发布竞赛</span>
          </router-link>
          <router-link to="/admin/registrations" class="action-item">
            <span class="action-icon" style="background: #fff5e6; color: #f6ad55">📋</span>
            <span class="action-name">审核报名</span>
          </router-link>
          <router-link to="/admin/banners" class="action-item">
            <span class="action-icon" style="background: #f0faf4; color: #48bb78">🖼</span>
            <span class="action-name">维护轮播</span>
          </router-link>
          <router-link to="/admin/stats" class="action-item">
            <span class="action-icon" style="background: #fef5f5; color: #e53e3e">📊</span>
            <span class="action-name">数据大屏</span>
          </router-link>
        </template>
        <template v-else>
          <router-link to="/teacher/audit" class="action-item">
            <span class="action-icon" style="background: #fff5e6; color: #f6ad55">✅</span>
            <span class="action-name">审核报名</span>
          </router-link>
          <router-link to="/teacher/stats" class="action-item">
            <span class="action-icon" style="background: #eaf2fb; color: #2b6cb0">📊</span>
            <span class="action-name">查看统计</span>
          </router-link>
          <router-link to="/student/profile" class="action-item">
            <span class="action-icon" style="background: #f0faf4; color: #48bb78">👤</span>
            <span class="action-name">个人资料</span>
          </router-link>
        </template>
      </div>
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
  grid-template-columns: repeat(3, 1fr);
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

// ===== 快捷操作 =====
.quick-actions {
  background: $bg-card;
  border-radius: $radius-md;
  padding: $space-5;
  box-shadow: $shadow-sm;
}

.section-title {
  margin: 0 0 $space-4;
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $space-3;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $space-2;
  padding: $space-5 $space-3;
  background: $bg-page;
  border-radius: $radius-md;
  text-decoration: none;
  color: $text-primary;
  transition: all $transition-base;

  &:hover {
    background: $primary-50;
    transform: translateY(-2px);
  }
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: $radius-md;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.action-name {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
}

// ===== 响应式 =====
@media (max-width: 768px) {
  .stat-grid,
  .action-grid {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
