<template>
  <div class="page-container">
    <div class="page-heading">
      <div>
        <h1>营销活动效果报表</h1>
        <p>查看触达、打开、点击、转化和投入产出表现。</p>
      </div>
      <div class="heading-actions">
        <el-button type="primary" @click="dialogVisible = true">录入效果</el-button>
        <el-button :icon="'ArrowLeft'" @click="router.push('/marketing')">返回活动</el-button>
      </div>
    </div>

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
          <template #header><span>渠道转化趋势</span></template>
          <v-chart :option="trendOption" class="chart" autoresize />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="10">
        <el-card shadow="never">
          <template #header><span>渠道贡献</span></template>
          <v-chart :option="channelOption" class="chart" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <template #header><span>活动排行</span></template>
      <el-table :data="campaignRows" border stripe>
        <el-table-column prop="name" label="活动" min-width="220" />
        <el-table-column prop="sent" label="触达" width="100" />
        <el-table-column prop="openRate" label="打开率" width="100" />
        <el-table-column prop="clickRate" label="点击率" width="100" />
        <el-table-column prop="conversion" label="转化" width="100" />
        <el-table-column prop="roi" label="ROI" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '增长' ? 'success' : 'warning'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="录入营销效果" width="640px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="活动名称" prop="campaignName">
          <el-input v-model="form.campaignName" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="渠道" prop="channel">
          <el-select v-model="form.channel" style="width: 100%">
            <el-option label="邮件" value="邮件" />
            <el-option label="短信" value="短信" />
            <el-option label="站内推送" value="站内推送" />
            <el-option label="人工跟进" value="人工跟进" />
          </el-select>
        </el-form-item>
        <el-form-item label="统计月份" prop="statMonth">
          <el-input v-model="form.statMonth" placeholder="例如：6月" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :xs="24" :sm="12">
            <el-form-item label="触达数">
              <el-input-number v-model="form.sentCount" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="打开数">
              <el-input-number v-model="form.openCount" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="点击数">
              <el-input-number v-model="form.clickCount" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="转化数">
              <el-input-number v-model="form.conversionCount" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="成本">
              <el-input-number v-model="form.costAmount" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="收入">
              <el-input-number v-model="form.revenueAmount" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitPerformance">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { addCampaignPerformance, getCampaignReport } from '@/api/marketing'

use([CanvasRenderer, BarChart, LineChart, PieChart, GridComponent, LegendComponent, TooltipComponent])

const router = useRouter()
const report = ref({})
const formRef = ref(null)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const form = reactive({
  campaignName: '',
  channel: '邮件',
  statMonth: '6月',
  sentCount: 0,
  openCount: 0,
  clickCount: 0,
  conversionCount: 0,
  costAmount: 0,
  revenueAmount: 0,
})

const rules = {
  campaignName: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  channel: [{ required: true, message: '请选择渠道', trigger: 'change' }],
  statMonth: [{ required: true, message: '请输入统计月份', trigger: 'blur' }],
}

const metrics = computed(() => [
  ...(report.value.metrics || []),
  ...fallbackMetrics,
].slice(0, 4))

const fallbackMetrics = [
  { label: '活动总数', value: 12, hint: '含草稿与运行中' },
  { label: '总触达', value: '36,820', hint: '邮件/短信/推送' },
  { label: '平均转化率', value: '8.6%', hint: '较上期 +1.4%' },
  { label: '营销 ROI', value: '3.7', hint: '收入 / 成本' },
]

const campaignRows = computed(() => report.value.campaignRows || [
  { name: '春季续费唤醒', sent: 9820, openRate: '42%', clickRate: '16%', conversion: 386, roi: '4.8', status: '增长' },
  { name: '高价值客户升级包', sent: 3260, openRate: '55%', clickRate: '24%', conversion: 168, roi: '6.1', status: '增长' },
  { name: '沉睡线索再营销', sent: 12400, openRate: '28%', clickRate: '7%', conversion: 92, roi: '1.9', status: '观察' },
  { name: '工单满意度回访', sent: 6140, openRate: '48%', clickRate: '18%', conversion: 134, roi: '3.2', status: '增长' },
])

const trendRows = computed(() => report.value.trend || [
  { month: '1月', sent: 4200, clicked: 560, converted: 68 },
  { month: '2月', sent: 5300, clicked: 740, converted: 92 },
  { month: '3月', sent: 6100, clicked: 880, converted: 118 },
  { month: '4月', sent: 5800, clicked: 810, converted: 104 },
  { month: '5月', sent: 7200, clicked: 1120, converted: 146 },
  { month: '6月', sent: 8220, clicked: 1380, converted: 186 },
])

const channelRows = computed(() => report.value.channels || [
  { name: '邮件', value: 42 },
  { name: '短信', value: 23 },
  { name: '站内推送', value: 21 },
  { name: '人工跟进', value: 14 },
])

const trendOption = computed(() => ({
  color: ['#2563eb', '#0f766e', '#f59e0b'],
  tooltip: { trigger: 'axis' },
  legend: { top: 0 },
  grid: { left: 10, right: 16, top: 42, bottom: 10, outerBoundsMode: 'same', outerBoundsContain: 'axisLabel' },
  xAxis: { type: 'category', data: trendRows.value.map(item => item.month) },
  yAxis: { type: 'value' },
  series: [
    { name: '触达', type: 'bar', data: trendRows.value.map(item => item.sent) },
    { name: '点击', type: 'line', smooth: true, data: trendRows.value.map(item => item.clicked) },
    { name: '转化', type: 'line', smooth: true, data: trendRows.value.map(item => item.converted) },
  ],
}))

const channelOption = computed(() => ({
  color: ['#2563eb', '#0f766e', '#f59e0b', '#dc2626'],
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [{
    type: 'pie',
    radius: ['45%', '68%'],
    center: ['50%', '42%'],
    data: channelRows.value,
  }],
}))

async function loadReport() {
  try {
    const res = await getCampaignReport()
    report.value = res.data || {}
  } catch {
    report.value = {}
  }
}

async function submitPerformance() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    await addCampaignPerformance(form)
    ElMessage.success('营销效果已保存')
    dialogVisible.value = false
    await loadReport()
  } finally {
    submitLoading.value = false
  }
}

function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, {
    campaignName: '',
    channel: '邮件',
    statMonth: '6月',
    sentCount: 0,
    openCount: 0,
    clickCount: 0,
    conversionCount: 0,
    costAmount: 0,
    revenueAmount: 0,
  })
}

onMounted(() => loadReport())
</script>

<style scoped>
.page-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.heading-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-heading h1 {
  margin: 0;
  font-size: 24px;
}

.page-heading p {
  margin: 6px 0 0;
  color: #64748b;
}

.metric-card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-height: 112px;
  padding: 18px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.metric-card span,
.metric-card small {
  color: #64748b;
}

.metric-card strong {
  font-size: 28px;
}

.chart {
  width: 100%;
  height: 320px;
}

@media (max-width: 768px) {
  .page-heading,
  .heading-actions {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
