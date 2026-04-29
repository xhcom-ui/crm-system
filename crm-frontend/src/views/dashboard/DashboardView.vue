<template>
  <div class="dashboard">
    <section class="overview-band">
      <div>
        <p class="eyebrow">经营概览</p>
        <h1>CRM 工作台</h1>
        <p class="overview-copy">聚合客户、商机、线索和工单状态，优先处理高价值线索和待响应事项。</p>
      </div>
      <div class="overview-actions">
        <el-button type="primary" :icon="Plus" @click="router.push('/customer')">新增客户</el-button>
        <el-button :icon="Search" @click="router.push('/leads')">查看线索</el-button>
      </div>
    </section>

    <el-row :gutter="16" class="stat-cards">
      <el-col v-for="item in statCards" :key="item.key" :xs="12" :sm="12" :md="6">
        <button class="stat-card" :class="item.tone" @click="router.push(item.path)">
          <span class="stat-icon"><el-icon :size="22"><component :is="item.icon" /></el-icon></span>
          <span class="stat-info">
            <span class="stat-label">{{ item.label }}</span>
            <strong class="stat-value" v-loading="loading[item.key]">{{ item.value }}</strong>
            <span class="stat-hint">{{ item.hint }}</span>
          </span>
        </button>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="content-row">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="card-header">
              <span>销售漏斗</span>
              <el-tag size="small" type="info">{{ stats.opportunities }} 个商机</el-tag>
            </div>
          </template>
          <v-chart :option="funnelOption" class="chart chart-large" autoresize />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="card-header">
              <span>关键待办</span>
              <el-button link type="primary" @click="router.push('/service')">处理工单</el-button>
            </div>
          </template>
          <div class="todo-list">
            <div v-for="todo in todoItems" :key="todo.title" class="todo-item">
              <span class="todo-mark" :class="todo.type"></span>
              <div>
                <strong>{{ todo.title }}</strong>
                <p>{{ todo.desc }}</p>
              </div>
              <el-tag :type="todo.tagType" size="small">{{ todo.count }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="content-row">
      <el-col :xs="24" :md="8">
        <el-card shadow="never" class="panel-card">
          <template #header><span>赢单率</span></template>
          <v-chart :option="winRateOption" class="chart chart-small" autoresize />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card shadow="never" class="panel-card">
          <template #header><span>线索来源</span></template>
          <v-chart :option="leadSourceOption" class="chart chart-small" autoresize />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card shadow="never" class="panel-card">
          <template #header><span>最近动态</span></template>
          <el-timeline class="activity-list">
            <el-timeline-item
              v-for="item in activities"
              :key="item.content"
              :timestamp="item.time"
              :type="item.type"
            >
              {{ item.content }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="content-row">
      <el-col :xs="24" :md="14">
        <el-card shadow="never" class="panel-card">
          <template #header><span>工单处理趋势</span></template>
          <v-chart :option="ticketTrendOption" class="chart chart-medium" autoresize />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="10">
        <el-card shadow="never" class="panel-card quick-card">
          <template #header><span>常用入口</span></template>
          <div class="quick-grid">
            <button v-for="action in quickActions" :key="action.path" class="quick-action" @click="router.push(action.path)">
              <el-icon :size="22"><component :is="action.icon" /></el-icon>
              <span>{{ action.label }}</span>
            </button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, PieChart, GaugeChart, LineChart, FunnelChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import { getSalesFunnel, getWinRate, getCustomerTotal, getLeadTotal, getTicketTotal } from '@/api/dashboard'
import { Plus, Search } from '@element-plus/icons-vue'

use([CanvasRenderer, BarChart, PieChart, GaugeChart, LineChart, FunnelChart,
  TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const router = useRouter()

const stats = reactive({
  customers: 0,
  opportunities: 0,
  leads: 0,
  tickets: 0,
})

const loading = reactive({ customers: false, opportunities: false, leads: false, tickets: false })

const funnelData = ref([])
const winRateData = ref({ won: 0, lost: 0, winRate: 0 })
const leadSourceData = ref([
  { value: 32, name: '官网' },
  { value: 24, name: '广告' },
  { value: 18, name: '推荐' },
  { value: 12, name: '社交媒体' },
])

const statCards = computed(() => [
  { key: 'customers', label: '客户总数', value: stats.customers, hint: '沉淀客户资产', icon: 'User', path: '/customer', tone: 'blue' },
  { key: 'opportunities', label: '商机总数', value: stats.opportunities, hint: '推进销售阶段', icon: 'TrendCharts', path: '/sales', tone: 'green' },
  { key: 'leads', label: '线索总数', value: stats.leads, hint: '关注高分线索', icon: 'Search', path: '/leads', tone: 'amber' },
  { key: 'tickets', label: '待处理工单', value: stats.tickets, hint: '优先响应客户', icon: 'Service', path: '/service', tone: 'red' },
])

const todoItems = computed(() => [
  { title: '高价值线索跟进', desc: '评分 80 分以上线索建议当天触达', count: Math.max(Math.round(stats.leads * 0.18), 0), type: 'hot', tagType: 'warning' },
  { title: '待响应工单', desc: '投诉与紧急工单需要优先分配', count: stats.tickets, type: 'risk', tagType: 'danger' },
  { title: '商机阶段复盘', desc: '检查停留在报价和谈判阶段的商机', count: Math.max(Math.round(stats.opportunities * 0.28), 0), type: 'sales', tagType: 'success' },
])

const activities = computed(() => [
  { content: `新增客户 ${stats.customers || 0} 条数据已同步`, time: '刚刚', type: 'primary' },
  { content: `销售漏斗当前有 ${stats.opportunities || 0} 个商机`, time: '10 分钟前', type: 'success' },
  { content: `待处理工单 ${stats.tickets || 0} 个`, time: '30 分钟前', type: stats.tickets > 0 ? 'warning' : 'info' },
  { content: '工作流自动化规则保持运行', time: '今天', type: 'info' },
])

const quickActions = [
  { label: '客户管理', icon: 'User', path: '/customer' },
  { label: '销售商机', icon: 'TrendCharts', path: '/sales' },
  { label: '营销活动', icon: 'Promotion', path: '/marketing' },
  { label: '服务工单', icon: 'Service', path: '/service' },
  { label: '线索池', icon: 'Search', path: '/leads' },
  { label: '工作流', icon: 'SetUp', path: '/workflow' },
]

const funnelOption = computed(() => ({
  color: ['#2563eb', '#0f766e', '#f59e0b', '#dc2626', '#16a34a', '#64748b'],
  tooltip: { trigger: 'item', formatter: '{b}: {c} 个' },
  series: [{
    type: 'funnel',
    left: '6%',
    top: 18,
    bottom: 18,
    width: '88%',
    sort: 'descending',
    gap: 4,
    minSize: '24%',
    maxSize: '92%',
    label: { show: true, position: 'inside', fontSize: 13, color: '#fff' },
    itemStyle: { borderColor: '#fff', borderWidth: 2 },
    data: normalizedFunnelData.value,
  }],
}))

const normalizedFunnelData = computed(() => {
  const data = funnelData.value.map(item => ({ name: item.name, value: item.count }))
  return data.length ? data : [
    { name: '初步接触', value: 48 },
    { name: '需求分析', value: 32 },
    { name: '方案报价', value: 21 },
    { name: '谈判', value: 13 },
    { name: '赢单', value: 7 },
  ]
})

const leadSourceOption = computed(() => ({
  color: ['#2563eb', '#0f766e', '#f59e0b', '#dc2626', '#7c3aed'],
  tooltip: { trigger: 'item' },
  legend: { bottom: 0, itemWidth: 10, itemHeight: 10 },
  series: [{
    type: 'pie',
    radius: ['46%', '70%'],
    center: ['50%', '43%'],
    label: { formatter: '{b}\n{c}' },
    data: leadSourceData.value,
  }],
}))

const winRateOption = computed(() => ({
  series: [{
    type: 'gauge',
    startAngle: 210,
    endAngle: -30,
    center: ['50%', '52%'],
    radius: '86%',
    min: 0,
    max: 100,
    axisLine: { lineStyle: { width: 18, color: [[0.35, '#dc2626'], [0.65, '#f59e0b'], [1, '#16a34a']] } },
    pointer: { length: '58%', width: 5 },
    axisTick: { distance: -24, length: 6 },
    splitLine: { distance: -28, length: 12 },
    axisLabel: { distance: 8, fontSize: 10 },
    detail: { valueAnimation: true, formatter: '{value}%', fontSize: 28, offsetCenter: [0, '72%'], color: '#111827' },
    data: [{ value: Math.round(winRateData.value.winRate || 0), name: '赢单率' }],
  }],
}))

const ticketTrendOption = computed(() => ({
  color: ['#2563eb', '#16a34a'],
  tooltip: { trigger: 'axis' },
  legend: { top: 0 },
  grid: { left: 8, right: 16, bottom: 8, top: 42, outerBoundsMode: 'same', outerBoundsContain: 'axisLabel' },
  xAxis: { type: 'category', boundaryGap: false, data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
  yAxis: { type: 'value', splitLine: { lineStyle: { color: '#eef2f7' } } },
  series: [
    { name: '新建', type: 'line', data: [5, 8, 6, 9, 7, 3, 2], smooth: true, areaStyle: { opacity: 0.08 } },
    { name: '已解决', type: 'line', data: [3, 6, 5, 7, 6, 2, 1], smooth: true, areaStyle: { opacity: 0.08 } },
  ],
}))

onMounted(async () => {
  await Promise.allSettled([
    loadFunnelData(),
    loadWinRateData(),
    loadStatTotals(),
  ])
})

async function loadFunnelData() {
  try {
    loading.opportunities = true
    const res = await getSalesFunnel()
    if (res?.data) {
      funnelData.value = res.data.stages || []
      stats.opportunities = res.data.totalCount || 0
    }
  } catch (e) {
    console.warn('漏斗数据加载失败')
  } finally {
    loading.opportunities = false
  }
}

async function loadWinRateData() {
  try {
    const res = await getWinRate()
    if (res?.data) winRateData.value = res.data
  } catch (e) {
    console.warn('赢单率加载失败')
  }
}

async function loadStatTotals() {
  await Promise.allSettled([
    loadTotal('customers', getCustomerTotal),
    loadTotal('leads', getLeadTotal),
    loadTotal('tickets', getTicketTotal),
  ])
}

async function loadTotal(key, loader) {
  try {
    loading[key] = true
    const res = await loader()
    if (res?.data) stats[key] = res.data.total || 0
  } catch (e) {
    console.warn(`${key} 数据加载失败`)
  } finally {
    loading[key] = false
  }
}
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.overview-band {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  min-height: 148px;
  padding: 28px;
  color: #fff;
  background:
    linear-gradient(135deg, rgba(15, 23, 42, 0.92), rgba(15, 118, 110, 0.82)),
    url('https://images.unsplash.com/photo-1551434678-e076c223a692?auto=format&fit=crop&w=1600&q=80') center/cover;
  border-radius: 8px;
}

.eyebrow {
  margin: 0 0 8px;
  font-size: 13px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.76);
}

.overview-band h1 {
  margin: 0;
  font-size: 30px;
  line-height: 1.2;
}

.overview-copy {
  max-width: 580px;
  margin-top: 10px;
  color: rgba(255, 255, 255, 0.82);
}

.overview-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.stat-cards,
.content-row {
  row-gap: 16px;
}

.stat-card {
  width: 100%;
  min-height: 116px;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  color: #111827;
  cursor: pointer;
  text-align: left;
  transition: transform 0.2s, border-color 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  border-color: #cbd5e1;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
}

.stat-icon {
  width: 48px;
  height: 48px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  border-radius: 8px;
  color: #fff;
}

.stat-card.blue .stat-icon { background: #2563eb; }
.stat-card.green .stat-icon { background: #0f766e; }
.stat-card.amber .stat-icon { background: #f59e0b; }
.stat-card.red .stat-icon { background: #dc2626; }

.stat-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
}

.stat-value {
  min-height: 36px;
  font-size: 30px;
  line-height: 1.2;
}

.stat-hint {
  font-size: 12px;
  color: #94a3b8;
}

.panel-card {
  height: 100%;
  border-radius: 8px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-weight: 600;
}

.chart {
  width: 100%;
}

.chart-large { height: 360px; }
.chart-medium { height: 300px; }
.chart-small { height: 260px; }

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.todo-item {
  display: grid;
  grid-template-columns: 10px 1fr auto;
  align-items: center;
  gap: 12px;
  min-height: 70px;
  padding: 12px;
  border: 1px solid #edf2f7;
  border-radius: 8px;
  background: #f8fafc;
}

.todo-item p {
  margin: 5px 0 0;
  font-size: 12px;
  color: #64748b;
}

.todo-mark {
  width: 10px;
  height: 42px;
  border-radius: 999px;
}

.todo-mark.hot { background: #f59e0b; }
.todo-mark.risk { background: #dc2626; }
.todo-mark.sales { background: #16a34a; }

.activity-list {
  padding: 6px 0 0 2px;
}

.quick-card :deep(.el-card__body) {
  height: calc(100% - 57px);
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  height: 100%;
}

.quick-action {
  min-height: 78px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 8px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  color: #334155;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s, color 0.2s;
}

.quick-action:hover {
  border-color: #2563eb;
  background: #eff6ff;
  color: #2563eb;
}

@media (max-width: 768px) {
  .overview-band {
    align-items: flex-start;
    flex-direction: column;
    padding: 22px;
  }

  .overview-band h1 {
    font-size: 24px;
  }

  .overview-actions {
    width: 100%;
  }

  .overview-actions .el-button {
    flex: 1;
  }

  .stat-card {
    min-height: 112px;
    padding: 14px;
  }

  .stat-icon {
    width: 40px;
    height: 40px;
  }

  .stat-value {
    font-size: 24px;
  }
}
</style>
