<script setup lang="ts">
/**
 * 数据统计页（管理员）
 * - 顶部统计卡片
 * - 报名人数柱状图（按竞赛）
 * - 审核状态占比环形图
 * - 报名明细表格 + 导出
 */
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Trophy, Document, User, UserFilled, CircleCheck, TrendCharts } from '@element-plus/icons-vue'
import { getAdminStats, getCompetitions, getCompetitionStats } from '@/api/admin'
import * as echarts from 'echarts'

const barRef = ref<HTMLDivElement | null>(null)
const pieRef = ref<HTMLDivElement | null>(null)
let barChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

// 总览统计
const overview = ref({
  competitionCount: 0,
  totalCount: 0,
  individualCount: 0,
  teamCount: 0,
  passedCount: 0,
  pendingCount: 0,
  rejectedCount: 0,
  passRate: 0
})

// 竞赛明细
interface CompetitionDetail {
  id: number
  title: string
  total: number
  approved: number
  pending: number
  rejected: number
}
const detailList = ref<CompetitionDetail[]>([])
const exportVisible = ref(false)

// 统计卡片数据
const statCards = computed(() => [
  {
    label: '竞赛总数',
    value: overview.value.competitionCount,
    icon: Trophy,
    color: '#2b6cb0',
    bg: 'linear-gradient(135deg, #ebf4fb 0%, #d6e8ff 100%)'
  },
  {
    label: '报名总数',
    value: overview.value.totalCount,
    icon: Document,
    color: '#6b46c1',
    bg: 'linear-gradient(135deg, #f5f0ff 0%, #e9d8fd 100%)'
  },
  {
    label: '个人报名',
    value: overview.value.individualCount,
    icon: User,
    color: '#2f855a',
    bg: 'linear-gradient(135deg, #f0faf4 0%, #c6f6d5 100%)'
  },
  {
    label: '团队报名',
    value: overview.value.teamCount,
    icon: UserFilled,
    color: '#c05621',
    bg: 'linear-gradient(135deg, #fff5e6 0%, #feebc8 100%)'
  },
  {
    label: '已通过',
    value: overview.value.passedCount,
    icon: CircleCheck,
    color: '#276749',
    bg: 'linear-gradient(135deg, #f0faf4 0%, #c6f6d5 100%)'
  },
  {
    label: '通过率',
    value: overview.value.passRate,
    suffix: '%',
    icon: TrendCharts,
    color: '#9b2c2c',
    bg: 'linear-gradient(135deg, #fef5f5 0%, #fed7d7 100%)'
  }
])

async function loadData() {
  try {
    const data: any = await getAdminStats()
    overview.value = data
  } catch {
    ElMessage.error('获取总览统计失败')
  }

  let competitions: any[] = []
  try {
    const res: any = await getCompetitions({ pageNum: 1, pageSize: 100 })
    competitions = res?.records || []
  } catch {
    ElMessage.error('获取竞赛列表失败')
  }

  const details: CompetitionDetail[] = []
  for (const comp of competitions) {
    try {
      const stat: any = await getCompetitionStats(comp.id)
      details.push({
        id: comp.id,
        title: comp.title || '未命名竞赛',
        total: stat?.totalCount ?? 0,
        approved: stat?.passedCount ?? 0,
        pending: stat?.pendingCount ?? 0,
        rejected: stat?.rejectedCount ?? 0
      })
    } catch {
      details.push({
        id: comp.id,
        title: comp.title || '未命名竞赛',
        total: 0,
        approved: 0,
        pending: 0,
        rejected: 0
      })
    }
  }
  detailList.value = details

  setTimeout(() => {
    initBarChart()
    initPieChart()
  }, 100)
}

function initBarChart() {
  if (!barRef.value) return
  barChart = echarts.init(barRef.value)
  barChart.setOption({
    title: {
      text: '各竞赛报名人数',
      left: 16,
      top: 12,
      textStyle: { fontSize: 15, color: '#1a202c', fontWeight: 600 }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(255,255,255,0.96)',
      borderColor: '#e2e8f0',
      borderWidth: 1,
      textStyle: { color: '#2d3748', fontSize: 13 },
      formatter: (params: any) => {
        const p = params[0]
        return `<div style="font-weight:600;margin-bottom:4px">${p.name}</div>
                <span style="color:${p.color}">●</span> 报名人数：<b>${p.value}</b> 人`
      }
    },
    grid: { left: 16, right: 24, bottom: 16, top: 56, containLabel: true },
    xAxis: {
      type: 'category',
      data: detailList.value.map((d) => d.title),
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisTick: { show: false },
      axisLabel: {
        color: '#4a5568',
        fontSize: 11,
        interval: 0,
        rotate: detailList.value.length > 6 ? 35 : 0,
        width: 80,
        overflow: 'truncate',
        ellipsis: '...'
      }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#f0f4f8', type: 'dashed' } },
      axisLabel: { color: '#718096', fontSize: 12 }
    },
    series: [{
      name: '报名人数',
      data: detailList.value.map((d) => d.total),
      type: 'bar',
      barMaxWidth: 40,
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#4299e1' },
          { offset: 1, color: '#2b6cb0' }
        ])
      },
      emphasis: {
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#63b3ed' },
            { offset: 1, color: '#3182ce' }
          ])
        }
      },
      label: {
        show: true,
        position: 'top',
        color: '#2d3748',
        fontSize: 12,
        fontWeight: 600,
        formatter: (p: any) => p.value > 0 ? p.value : ''
      }
    }],
    animationDuration: 800,
    animationEasing: 'cubicOut'
  })
}

function initPieChart() {
  if (!pieRef.value) return
  pieChart = echarts.init(pieRef.value)
  const o = overview.value
  pieChart.setOption({
    title: {
      text: '审核状态分布',
      left: 16,
      top: 12,
      textStyle: { fontSize: 15, color: '#1a202c', fontWeight: 600 }
    },
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255,255,255,0.96)',
      borderColor: '#e2e8f0',
      borderWidth: 1,
      textStyle: { color: '#2d3748', fontSize: 13 },
      formatter: (p: any) => `${p.marker} ${p.name}：<b>${p.value}</b> 人 (${p.percent}%)`
    },
    legend: {
      bottom: 12,
      left: 'center',
      itemWidth: 12,
      itemHeight: 12,
      itemGap: 24,
      textStyle: { color: '#4a5568', fontSize: 13 },
      icon: 'circle'
    },
    series: [{
      name: '审核状态',
      type: 'pie',
      radius: ['48%', '72%'],
      center: ['50%', '48%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 6,
        borderColor: '#fff',
        borderWidth: 3
      },
      label: {
        show: true,
        formatter: '{b}\n{c} 人',
        fontSize: 12,
        color: '#4a5568',
        lineHeight: 18
      },
      labelLine: {
        length: 16,
        length2: 12,
        lineStyle: { color: '#cbd5e0' }
      },
      emphasis: {
        scaleSize: 8,
        label: { fontSize: 14, fontWeight: 600 }
      },
      data: [
        {
          name: '已通过',
          value: o.passedCount,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
              { offset: 0, color: '#68d391' },
              { offset: 1, color: '#38a169' }
            ])
          }
        },
        {
          name: '待审核',
          value: o.pendingCount,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
              { offset: 0, color: '#fbd38d' },
              { offset: 1, color: '#ed8936' }
            ])
          }
        },
        {
          name: '未通过',
          value: o.rejectedCount,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
              { offset: 0, color: '#fc8181' },
              { offset: 1, color: '#e53e3e' }
            ])
          }
        }
      ]
    }],
    animationDuration: 800,
    animationEasing: 'cubicOut'
  })
}

function handleExport() {
  exportVisible.value = true
}

function resizeCharts() {
  barChart?.resize()
  pieChart?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  barChart?.dispose()
  pieChart?.dispose()
})
</script>

<template>
  <div class="page">
    <h2 class="page-title">数据统计</h2>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div
        v-for="card in statCards"
        :key="card.label"
        class="stat-card"
        :style="{ background: card.bg }"
      >
        <div class="stat-icon"><el-icon :size="28"><component :is="card.icon" /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value" :style="{ color: card.color }">
            {{ card.value }}{{ card.suffix || '' }}
          </div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <div class="chart-card bar-card">
        <div ref="barRef" class="chart"></div>
      </div>
      <div class="chart-card pie-card">
        <div ref="pieRef" class="chart"></div>
      </div>
    </div>

    <!-- 报名明细 -->
    <div class="detail-card">
      <div class="detail-header">
        <h3 class="detail-title">报名明细</h3>
        <button class="btn-export" @click="handleExport">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
            <polyline points="7 10 12 15 17 10"/>
            <line x1="12" y1="15" x2="12" y2="3"/>
          </svg>
          导出明细
        </button>
      </div>

      <el-table
        :data="detailList"
        stripe
        class="detail-table"
        :header-cell-style="{ background: '#f7fafc', color: '#4a5568', fontWeight: 600, fontSize: '13px' }"
      >
        <el-table-column prop="title" label="竞赛名称" min-width="240">
          <template #default="{ row }">
            <span class="comp-name">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="total" label="总报名" width="120" align="center">
          <template #default="{ row }">
            <span class="num-badge total">{{ row.total }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="approved" label="已通过" width="120" align="center">
          <template #default="{ row }">
            <span class="num-badge approved">{{ row.approved }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="pending" label="待审核" width="120" align="center">
          <template #default="{ row }">
            <span class="num-badge pending">{{ row.pending }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="rejected" label="未通过" width="120" align="center">
          <template #default="{ row }">
            <span class="num-badge rejected">{{ row.rejected }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 导出成功弹窗 -->
    <el-dialog v-model="exportVisible" width="400px" :show-close="true" align-center>
      <template #header>
        <div class="dialog-header"><h3>导出成功</h3></div>
      </template>
      <div class="dialog-body">
        <p class="dialog-text">报名明细导出成功！</p>
      </div>
      <template #footer>
        <div class="dialog-footer center">
          <button class="btn-ok primary" @click="exportVisible = false">确定</button>
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
  margin: 0 0 $space-1;
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
  display: flex;
  align-items: center;
  gap: $space-3;
  padding: $space-4 $space-5;
  border-radius: $radius-lg;
  transition: transform $transition-fast, box-shadow $transition-fast;

  &:hover {
    transform: translateY(-2px);
    box-shadow: $shadow-md;
  }
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: $radius-md;
  background: rgba(255, 255, 255, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.stat-value {
  font-size: $font-size-2xl;
  font-weight: $font-weight-bold;
  font-family: 'Consolas', 'Monaco', monospace;
  line-height: 1.2;
}

.stat-label {
  font-size: $font-size-xs;
  color: $text-secondary;
  white-space: nowrap;
}

// ===== 图表 =====
.charts-row {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: $space-4;
}

.chart-card {
  background: $bg-card;
  border-radius: $radius-lg;
  padding: $space-3;
  box-shadow: $shadow-base;
  border: 1px solid $border-light;
}

.chart {
  width: 100%;
  height: 380px;
}

// ===== 明细表格 =====
.detail-card {
  background: $bg-card;
  border-radius: $radius-lg;
  padding: $space-5 $space-6;
  box-shadow: $shadow-base;
  border: 1px solid $border-light;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $space-4;
}

.detail-title {
  margin: 0;
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.btn-export {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 34px;
  padding: 0 $space-4;
  border: none;
  background: linear-gradient(135deg, #48bb78, #38a169);
  color: #fff;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  border-radius: $radius-base;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(72, 187, 120, 0.25);
  transition: all $transition-fast;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 14px rgba(72, 187, 120, 0.35);
  }

  svg {
    flex-shrink: 0;
  }
}

.detail-table {
  border-radius: $radius-base;
  overflow: hidden;
}

.comp-name {
  font-size: $font-size-base;
  color: $text-primary;
  font-weight: $font-weight-medium;
}

.num-badge {
  display: inline-block;
  min-width: 32px;
  padding: 2px 10px;
  border-radius: $radius-full;
  font-family: 'Consolas', monospace;
  font-size: $font-size-sm;
  font-weight: $font-weight-semibold;
  text-align: center;

  &.total {
    background: $primary-light;
    color: $primary;
  }
  &.approved {
    background: $success-bg;
    color: $success;
  }
  &.pending {
    background: $warning-bg;
    color: #c05621;
  }
  &.rejected {
    background: $danger-bg;
    color: $danger;
  }
}

// ===== 弹窗 =====
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
  }
}

// ===== 响应式 =====
@media (max-width: 1200px) {
  .stat-cards {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 1024px) {
  .charts-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stat-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
