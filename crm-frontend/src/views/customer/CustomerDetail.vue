<template>
  <div class="page-container">
    <div class="detail-header">
      <el-button :icon="'ArrowLeft'" @click="router.back()">返回</el-button>
      <div>
        <h1>{{ customer.name || '客户详情' }}</h1>
        <p>{{ customer.company || '未填写公司' }} · {{ levelLabel(customer.level) }} · {{ sourceLabel(customer.source) }}</p>
      </div>
      <el-button type="primary" :icon="'Edit'" @click="router.push('/customer')">编辑客户</el-button>
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="profile-card">
          <template #header><span>客户画像</span></template>
          <div class="avatar-block">
            <el-avatar :size="64">{{ avatarText }}</el-avatar>
            <div>
              <strong>{{ customer.name || '-' }}</strong>
              <p>{{ customer.position || '未填写职位' }}</p>
            </div>
          </div>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="手机号">{{ customer.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ customer.email || '-' }}</el-descriptions-item>
            <el-descriptions-item label="客户来源">{{ sourceLabel(customer.source) }}</el-descriptions-item>
            <el-descriptions-item label="备注">{{ customer.remark || '暂无备注' }}</el-descriptions-item>
          </el-descriptions>
          <div class="tag-list">
            <el-tag v-for="tag in profile.tags" :key="tag" effect="plain">{{ tag }}</el-tag>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="16">
        <el-card shadow="never">
          <template #header><span>转化归因</span></template>
          <el-row :gutter="12" class="metric-row">
            <el-col v-for="item in attributionMetrics" :key="item.label" :xs="12" :md="6">
              <div class="metric-tile">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
                <small>{{ item.hint }}</small>
              </div>
            </el-col>
          </el-row>
          <div class="attribution-bars">
            <div v-for="item in attribution" :key="item.channel" class="bar-row">
              <span>{{ item.channel }}</span>
              <el-progress :percentage="item.weight" :stroke-width="10" :show-text="false" />
              <strong>{{ item.weight }}%</strong>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="timeline-card">
          <template #header>
            <div class="card-header">
              <span>互动时间线</span>
              <div class="header-actions">
                <el-tag size="small" type="info">{{ interactions.length }} 条记录</el-tag>
                <el-button link type="primary" @click="dialogVisible = true">新增互动</el-button>
              </div>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="item in interactions"
              :key="item.time + item.title"
              :timestamp="item.time"
              :type="item.type"
            >
              <div class="timeline-content">
                <strong>{{ item.title }}</strong>
                <p>{{ item.desc }}</p>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" title="新增互动记录" width="560px" @close="resetInteractionForm">
      <el-form ref="formRef" :model="interactionForm" :rules="rules" label-width="96px">
        <el-form-item label="互动类型" prop="interactionType">
          <el-select v-model="interactionForm.interactionType" style="width: 100%">
            <el-option label="电话" value="call" />
            <el-option label="邮件" value="email" />
            <el-option label="工单" value="ticket" />
            <el-option label="会议" value="meeting" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="interactionForm.title" placeholder="请输入互动标题" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="interactionForm.description" type="textarea" :rows="4" placeholder="请输入互动内容" />
        </el-form-item>
        <el-form-item label="时间线类型">
          <el-select v-model="interactionForm.timelineType" style="width: 100%">
            <el-option label="普通" value="primary" />
            <el-option label="成功" value="success" />
            <el-option label="提醒" value="warning" />
            <el-option label="信息" value="info" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitInteraction">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addCustomerInteraction, getCustomerInsight } from '@/api/customer'

const route = useRoute()
const router = useRouter()

const customer = reactive({})
const formRef = ref(null)
const dialogVisible = ref(false)
const submitLoading = ref(false)

const profile = reactive({ tags: ['高意向', '企业客户', '关注售后', '邮件活跃'] })

const interactions = reactive([
  { time: '今天 10:20', title: '销售电话跟进', desc: '确认采购预算，客户希望下周收到正式报价。', type: 'primary' },
  { time: '昨天 16:40', title: '营销邮件打开', desc: '打开产品升级邮件并点击案例链接。', type: 'success' },
  { time: '本周一 11:05', title: '服务工单创建', desc: '咨询数据导入规则，已由客服处理。', type: 'warning' },
  { time: '上周三 09:30', title: '官网表单提交', desc: '来源为官网，关注 CRM 自动化和工作流能力。', type: 'info' },
])

const attribution = reactive([
  { channel: '官网表单', weight: 36 },
  { channel: '邮件营销', weight: 28 },
  { channel: '销售跟进', weight: 24 },
  { channel: '客户推荐', weight: 12 },
])

const insightMetrics = reactive({})
const interactionForm = reactive({
  interactionType: 'call',
  title: '',
  description: '',
  timelineType: 'primary',
})

const rules = {
  title: [{ required: true, message: '请输入互动标题', trigger: 'blur' }],
}

const attributionMetrics = computed(() => [
  { label: '画像评分', value: insightMetrics.profileScore || profileScore.value, hint: '基于等级与互动推算' },
  { label: '转化概率', value: `${insightMetrics.conversionRate || conversionRate.value}%`, hint: '综合渠道贡献' },
  { label: '最近触达', value: insightMetrics.lastTouch || '今天', hint: '电话跟进' },
  { label: '主归因渠道', value: insightMetrics.primaryChannel || attribution[0].channel, hint: `${attribution[0].weight}% 贡献` },
])

const avatarText = computed(() => (customer.name || '客').slice(0, 1))
const profileScore = computed(() => Math.min(60 + Number(customer.level || 1) * 12 + interactions.length * 2, 99))
const conversionRate = computed(() => Math.min(35 + Number(customer.level || 1) * 14 + attribution[0].weight / 2, 96))

function sourceLabel(val) {
  const map = { 1: '网站', 2: '推荐', 3: '展会', 4: '其他' }
  return map[val] || '未知'
}

function levelLabel(val) {
  const map = { 1: '普通客户', 2: '重要客户', 3: 'VIP 客户' }
  return map[val] || '未评级'
}

async function loadInsight() {
  try {
    const res = await getCustomerInsight(route.params.id)
    const data = res.data || {}
    Object.assign(customer, data.customer || {})
    Object.assign(profile, data.profile || {})
    Object.assign(insightMetrics, data.metrics || {})
    if (data.interactions?.length) interactions.splice(0, interactions.length, ...data.interactions)
    if (data.attribution?.length) attribution.splice(0, attribution.length, ...data.attribution)
  } catch {
    Object.assign(customer, {
      id: route.params.id,
      name: '示例客户',
      company: '示例科技有限公司',
      position: '采购负责人',
      phone: '13800000000',
      email: 'customer@example.com',
      source: 1,
      level: 2,
      remark: '接口不可用时展示前端画像样例。',
    })
  }
}

async function submitInteraction() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    await addCustomerInteraction({
      contactId: Number(route.params.id),
      ...interactionForm,
    })
    ElMessage.success('互动记录已保存')
    dialogVisible.value = false
    await loadInsight()
  } finally {
    submitLoading.value = false
  }
}

function resetInteractionForm() {
  formRef.value?.resetFields()
  Object.assign(interactionForm, {
    interactionType: 'call',
    title: '',
    description: '',
    timelineType: 'primary',
  })
}

onMounted(() => loadInsight())
</script>

<style scoped>
.detail-header {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 16px;
}

.detail-header h1 {
  margin: 0;
  font-size: 24px;
}

.detail-header p,
.avatar-block p,
.timeline-content p {
  margin: 4px 0 0;
  color: #64748b;
}

.profile-card {
  height: 100%;
}

.avatar-block {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 18px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
}

.metric-row {
  row-gap: 12px;
}

.metric-tile {
  display: flex;
  flex-direction: column;
  gap: 5px;
  padding: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #f8fafc;
}

.metric-tile span,
.metric-tile small {
  color: #64748b;
}

.metric-tile strong {
  font-size: 24px;
}

.attribution-bars {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-top: 20px;
}

.bar-row {
  display: grid;
  grid-template-columns: 90px 1fr 44px;
  align-items: center;
  gap: 12px;
}

.timeline-card {
  margin-top: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

@media (max-width: 768px) {
  .detail-header {
    grid-template-columns: 1fr;
  }
}
</style>
