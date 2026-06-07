<script setup lang="ts">
/**
 * 数据统计页（管理员 / 老师）
 * - 报名人数柱状图
 * - 审核状态占比环形图
 * - 报名明细表格 + 导出
 */
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const barRef = ref<HTMLDivElement | null>(null)
const pieRef = ref<HTMLDivElement | null>(null)
let barChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

// 占位数据
const barData = [
  { name: '程序设计', value: 56 },
  { name: '英语演讲', value: 32 },
  { name: '文艺比赛', value: 18 },
  { name: '科技创新', value: 24 }
]

const pieData = [
  { name: '待审核', value: 30, color: '#f6ad55' },
  { name: '已通过', value: 124, color: '#48bb78' },
  { name: '已驳回', value: 18, color: '#e53e3e' }
]

// 报名明细
const detailList = ref<any[]>([
  { id: 1, title: '程序设计竞赛', total: 56, approved: 50, rejected: 6 }
])

const exportVisible = ref(false)

function initBarChart() {
  if (!barRef.value) return
  barChart = echarts.init(barRef.value)
  barChart.setOption({
    title: {
      text: '报名人数统计',
      left: 'left',
      textStyle: { fontSize: 14, color: '#1a202c', fontWeight: 600 }
    },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: barData.map((d) => d.name),
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisLabel: { color: '#4a5568', fontSize: 12 }
    },
    yAxis: {
      type: 'value',
      max: 60,
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#edf2f7', type: 'dashed' } },
      axisLabel: { color: '#4a5568' }
    },
    series: [{
      name: '报名人数',
      data: barData.map((d) => d.value),
      type: 'bar',
      barWidth: 50,
      itemStyle: {
        borderRadius: [4, 4, 0, 0],
        color: '#2b6cb0'
      },
      label: {
        show: true,
        position: 'top',
        color: '#1a202c',
        fontSize: 12,
        fontWeight: 600
      }
    }]
  })
}

function initPieChart() {
  if (!pieRef.value) return
  pieChart = echarts.init(pieRef.value)
  pieChart.setOption({
    title: {
      text: '审核状态占比',
      left: 'left',
      textStyle: { fontSize: 14, color: '#1a202c', fontWeight: 600 }
    },
    tooltip: { trigger: 'item' },
    legend: {
      top: 30,
      right: 0,
      orient: 'horizontal',
      textStyle: { color: '#4a5568' }
    },
    series: [{
      name: '审核状态',
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['50%', '60%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 4,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{b}\n{c}'
      },
      data: pieData.map((d) => ({ name: d.name, value: d.value, itemStyle: { color: d.color } }))
    }]
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
  setTimeout(() => {
    initBarChart()
    initPieChart()
  }, 100)
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

    <!-- 图表区域 -->
    <div class="charts-row">
      <div class="chart-card">
        <div ref="barRef" class="chart"></div>
      </div>
      <div class="chart-card">
        <div ref="pieRef" class="chart pie-chart"></div>
      </div>
    </div>

    <!-- 报名明细 -->
    <div class="detail-card">
      <div class="detail-header">
        <h3 class="detail-title">报名明细</h3>
        <button class="btn-export" @click="handleExport">
          <span>📥</span>导出报名明细
        </button>
      </div>

      <el-table
        :data="detailList"
        stripe
        class="detail-table"
        :header-cell-style="{ background: '#f7fafc', color: '#4a5568', fontWeight: 600 }"
      >
        <el-table-column prop="title" label="竞赛" min-width="200">
          <template #default="{ row }">
            <span class="comp-name">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="total" label="总报名" width="120" align="center">
          <template #default="{ row }">
            <span class="num-text">{{ row.total }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="approved" label="通过" width="120" align="center">
          <template #default="{ row }">
            <span class="num-text approved">{{ row.approved }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="rejected" label="拒绝" width="120" align="center">
          <template #default="{ row }">
            <span class="num-text rejected">{{ row.rejected }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 导出成功 弹窗 -->
    <el-dialog
      v-model="exportVisible"
      width="440px"
      :show-close="true"
      align-center
    >
      <template #header>
        <div class="dialog-header"><h3>此页面显示</h3></div>
      </template>
      <div class="dialog-body">
        <p class="dialog-text">报名明细导出成功!</p>
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
  margin: 0 0 $space-2;
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $space-4;
}

.chart-card {
  background: $bg-card;
  border-radius: $radius-md;
  padding: $space-4 $space-5;
  box-shadow: $shadow-sm;
}

.chart {
  width: 100%;
  height: 360px;
}

.pie-chart {
  height: 380px;
}

.detail-card {
  background: $bg-card;
  border-radius: $radius-md;
  padding: $space-4 $space-5;
  box-shadow: $shadow-sm;
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
  gap: 4px;
  height: 32px;
  padding: 0 $space-4;
  border: none;
  background: #48bb78;
  color: #fff;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  border-radius: $radius-base;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(72, 187, 120, 0.2);
  transition: all $transition-fast;

  &:hover {
    background: #38a169;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(72, 187, 120, 0.3);
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

.num-text {
  font-family: 'Consolas', monospace;
  font-weight: $font-weight-semibold;
  color: $text-regular;

  &.approved {
    color: $success;
  }
  &.rejected {
    color: $danger;
  }
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
  }
}

@media (max-width: 1024px) {
  .charts-row {
    grid-template-columns: 1fr;
  }
}
</style>
