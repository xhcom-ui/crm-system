<template>
  <div class="page-container">
    <el-row :gutter="16">
      <el-col v-for="item in metrics" :key="item.label" :xs="12" :md="6">
        <div class="metric-card">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <small>{{ item.hint }}</small>
        </div>
      </el-col>
    </el-row>
    <el-row :gutter="16">
      <el-col :xs="24" :lg="14">
        <el-card shadow="never">
          <template #header><span>销售趋势</span></template>
          <v-chart :option="salesTrendOption" class="chart" autoresize />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="10">
        <el-card shadow="never">
          <template #header><span>业务构成</span></template>
          <v-chart :option="compositionOption" class="chart" autoresize />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { getStatsReport } from '@/api/statistics'

use([CanvasRenderer, BarChart, LineChart, PieChart, GridComponent, LegendComponent, TooltipComponent])

const report = ref({})

const metrics = computed(() => report.value.metrics || [
  { label: '销售额', value: '¥186万', hint: '本月累计' },
  { label: '新增客户', value: 46, hint: '较上月 +12%' },
  { label: '成交订单', value: 28, hint: '合同已签约' },
  { label: '回款率', value: '82%', hint: '目标 85%' },
])

const trendRows = computed(() => report.value.trend || [
  { month: '1月', sales: 82, received: 62 },
  { month: '2月', sales: 96, received: 80 },
  { month: '3月', sales: 118, received: 93 },
  { month: '4月', sales: 126, received: 108 },
  { month: '5月', sales: 151, received: 122 },
  { month: '6月', sales: 186, received: 152 },
])

const compositionRows = computed(() => report.value.composition || [
  { name: '软件订阅', value: 48 },
  { name: '增值服务', value: 24 },
  { name: '营销插件', value: 18 },
  { name: '交付服务', value: 10 },
])

const salesTrendOption = computed(() => ({
  color: ['#2563eb', '#0f766e'],
  tooltip: { trigger: 'axis' },
  legend: { top: 0 },
  grid: { left: 10, right: 16, top: 42, bottom: 10, outerBoundsMode: 'same', outerBoundsContain: 'axisLabel' },
  xAxis: { type: 'category', data: trendRows.value.map(item => item.month) },
  yAxis: { type: 'value' },
  series: [
    { name: '销售额', type: 'bar', data: trendRows.value.map(item => item.sales) },
    { name: '回款额', type: 'line', smooth: true, data: trendRows.value.map(item => item.received) },
  ],
}))

const compositionOption = computed(() => ({
  color: ['#2563eb', '#0f766e', '#f59e0b', '#dc2626'],
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [{
    type: 'pie',
    radius: ['45%', '68%'],
    center: ['50%', '42%'],
    data: compositionRows.value,
  }],
}))

onMounted(async () => {
  try {
    const res = await getStatsReport()
    report.value = res.data || {}
  } catch {
    report.value = {}
  }
})
</script>

<style scoped>
.metric-card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-height: 108px;
  padding: 18px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}
.metric-card span,
.metric-card small { color: #64748b; }
.metric-card strong { font-size: 26px; }
.chart { width: 100%; height: 320px; }
</style>
