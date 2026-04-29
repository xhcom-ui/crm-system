<template>
  <div class="page-container ops-page">
    <el-card shadow="never">
      <div class="hero-head">
        <div>
          <h2>支付管理</h2>
          <p>统一下单、支付回调、退款、渠道配置、转账和对账状态。</p>
        </div>
        <el-button type="primary" :icon="'Plus'" @click="orderDialog = true">统一下单</el-button>
      </div>
      <div class="metric-grid">
        <div v-for="item in metrics" :key="item.label" class="metric-tile">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <small>{{ item.hint }}</small>
        </div>
      </div>
    </el-card>

    <el-card shadow="never">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="支付订单" name="orders">
          <div class="toolbar">
            <el-input v-model="query.keyword" :prefix-icon="'Search'" placeholder="支付单/商户单/客户" clearable @keyup.enter="loadOrders" />
            <el-button type="primary" :icon="'Refresh'" @click="loadOrders">刷新</el-button>
          </div>
          <el-table :data="orders" v-loading="loading" border stripe>
            <el-table-column prop="paymentNo" label="支付订单号" min-width="170" />
            <el-table-column prop="merchantOrderNo" label="商户订单号" min-width="150" />
            <el-table-column prop="channel" label="渠道" width="90" />
            <el-table-column prop="payMethod" label="方式" width="120" />
            <el-table-column prop="amount" label="金额" width="110"><template #default="{ row }">¥{{ row.amount }}</template></el-table-column>
            <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag size="small" :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag></template></el-table-column>
            <el-table-column prop="reconcileStatus" label="对账" width="110" />
            <el-table-column label="操作" width="90"><template #default="{ row }"><el-button link type="primary" @click="openRefund(row)">退款</el-button></template></el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="退款管理" name="refunds">
          <el-table :data="refunds" border stripe>
            <el-table-column prop="refundNo" label="退款单号" min-width="170" />
            <el-table-column prop="paymentNo" label="支付单号" min-width="170" />
            <el-table-column prop="amount" label="金额" width="110" />
            <el-table-column prop="reason" label="原因" min-width="160" />
            <el-table-column prop="successTime" label="成功时间" width="180" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="渠道配置" name="channels">
          <el-table :data="channels" border stripe>
            <el-table-column prop="channelCode" label="渠道编码" width="130" />
            <el-table-column prop="channelName" label="渠道名称" min-width="140" />
            <el-table-column prop="payMethod" label="支付方式" width="130" />
            <el-table-column prop="feeRate" label="费率" width="90" />
            <el-table-column prop="configStatus" label="配置状态" width="120" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="待补能力" name="missing">
          <div class="payment-flow">
            <template v-for="(item, index) in flows" :key="item">
              <div class="flow-node">
                <el-icon><CircleCheck /></el-icon>
                <span>{{ item }}</span>
              </div>
              <div v-if="index < flows.length - 1" class="flow-arrow"></div>
            </template>
          </div>
          <div class="gap-line">
            <el-button
              v-for="item in missingCapabilities"
              :key="item"
              type="warning"
              plain
              @click="openCapability(item)"
            >
              {{ item }}
            </el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="orderDialog" title="统一下单" width="560px" @close="resetOrder">
      <el-form :model="orderForm" label-width="100px">
        <el-form-item label="商户单号"><el-input v-model="orderForm.merchantOrderNo" /></el-form-item>
        <el-form-item label="客户"><el-input v-model="orderForm.customerName" /></el-form-item>
        <el-form-item label="渠道"><el-input v-model="orderForm.channel" /></el-form-item>
        <el-form-item label="方式"><el-input v-model="orderForm.payMethod" /></el-form-item>
        <el-form-item label="金额"><el-input-number v-model="orderForm.amount" :min="0" style="width: 100%" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="orderDialog = false">取消</el-button><el-button type="primary" @click="saveOrder">提交</el-button></template>
    </el-dialog>

    <el-dialog v-model="refundDialog" title="发起退款" width="520px">
      <el-form :model="refundForm" label-width="90px">
        <el-form-item label="支付单号"><el-input v-model="refundForm.paymentNo" disabled /></el-form-item>
        <el-form-item label="退款金额"><el-input-number v-model="refundForm.amount" :min="0" style="width: 100%" /></el-form-item>
        <el-form-item label="原因"><el-input v-model="refundForm.reason" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="refundDialog = false">取消</el-button><el-button type="primary" @click="saveRefund">提交</el-button></template>
    </el-dialog>

    <el-dialog v-model="capabilityDialog" :title="selectedCapability.name" width="560px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="能力说明">{{ selectedCapability.desc }}</el-descriptions-item>
        <el-descriptions-item label="建议接口">{{ selectedCapability.api }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">{{ selectedCapability.status }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="capabilityDialog = false">知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheck } from '@element-plus/icons-vue'
import { getPaymentChannels, getPaymentOrders, getPaymentOverview, getPaymentRefunds, refundPayment, unifiedOrder } from '@/api/payment'

const activeTab = ref('orders')
const loading = ref(false)
const orderDialog = ref(false)
const refundDialog = ref(false)
const capabilityDialog = ref(false)
const query = reactive({ keyword: '' })
const metrics = ref([])
const flows = ref([])
const missingCapabilities = ref([])
const capabilityRows = ref([])
const selectedCapability = ref({})
const orders = ref([])
const refunds = ref([])
const channels = ref([])
const orderForm = reactive({ merchantOrderNo: '', customerName: '', channel: '微信', payMethod: 'WX_NATIVE', amount: 0 })
const refundForm = reactive({ paymentNo: '', amount: 0, reason: '客户申请退款' })

function statusLabel(status) { return { 1: '待支付', 2: '成功', 3: '已关闭', 4: '失败' }[status] || '未知' }
function statusType(status) { return { 1: 'warning', 2: 'success', 3: 'info', 4: 'danger' }[status] || '' }

async function loadOverview() {
  const res = await getPaymentOverview()
  metrics.value = res.data?.metrics || []
  flows.value = res.data?.flows || []
  missingCapabilities.value = res.data?.missingCapabilities || []
  capabilityRows.value = (res.data?.capabilities || []).length
    ? res.data.capabilities
    : missingCapabilities.value.map(name => ({ name, desc: capabilityDesc(name), api: `/sales/payment/capability/${name}`, status: '待建设' }))
}
async function loadOrders() {
  loading.value = true
  try {
    const res = await getPaymentOrders({ current: 1, size: 50, ...query })
    orders.value = res.data?.records || []
  } finally {
    loading.value = false
  }
}
async function loadRefunds() { refunds.value = (await getPaymentRefunds()).data || [] }
async function loadChannels() { channels.value = (await getPaymentChannels()).data || [] }
async function saveOrder() {
  await unifiedOrder(orderForm)
  ElMessage.success('统一下单已提交')
  orderDialog.value = false
  await Promise.all([loadOverview(), loadOrders()])
}
function openRefund(row) {
  Object.assign(refundForm, { paymentNo: row.paymentNo, amount: row.refundableAmount || row.amount || 0, reason: '客户申请退款' })
  refundDialog.value = true
}
async function saveRefund() {
  await refundPayment(refundForm)
  ElMessage.success('退款已提交')
  refundDialog.value = false
  await Promise.all([loadOverview(), loadRefunds()])
}
function resetOrder() { Object.assign(orderForm, { merchantOrderNo: '', customerName: '', channel: '微信', payMethod: 'WX_NATIVE', amount: 0 }) }
function capabilityDesc(name) {
  return {
    支付签名验签: '统一封装各渠道签名、验签、证书轮换和回调报文校验。',
    渠道账单文件解析: '解析微信、支付宝、银行卡等渠道账单文件，生成标准对账明细。',
    分账规则: '按商户、门店、销售、服务商等维度配置分账比例和结算周期。',
    风控限额: '按用户、商户、渠道、单笔、日累计配置支付和退款限额。',
    幂等请求表: '记录下单、退款、转账、回调请求幂等键，防止重复处理。',
  }[name] || '支付引擎扩展能力。'
}
function openCapability(name) {
  selectedCapability.value = capabilityRows.value.find(item => item.name === name) || { name, desc: capabilityDesc(name), api: `/sales/payment/capability/${name}`, status: '待建设' }
  capabilityDialog.value = true
}

watch(activeTab, tab => {
  if (tab === 'refunds') loadRefunds()
  if (tab === 'channels') loadChannels()
})
onMounted(() => Promise.all([loadOverview(), loadOrders(), loadRefunds(), loadChannels()]))
</script>

<style scoped>
.ops-page { display: flex; flex-direction: column; gap: 16px; }
.hero-head, .toolbar { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.hero-head h2 { margin: 0 0 6px; font-size: 22px; }
.hero-head p { margin: 0; color: var(--el-text-color-secondary); }
.metric-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; margin-top: 16px; }
.metric-tile { border: 1px solid var(--el-border-color-lighter); border-radius: 8px; padding: 14px; background: #fff; }
.metric-tile span { display: block; color: var(--el-text-color-secondary); font-size: 13px; }
.metric-tile strong { display: block; margin-top: 8px; font-size: 22px; }
.metric-tile small { color: var(--el-text-color-placeholder); }
.toolbar .el-input { max-width: 300px; }
.payment-flow {
  display: grid;
  grid-template-columns: repeat(13, max-content);
  align-items: center;
  gap: 18px;
  padding: 14px 22px;
  background: #f4f6f9;
  overflow-x: auto;
}
.flow-node {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #67c23a;
  font-weight: 600;
  white-space: nowrap;
}
.flow-node .el-icon { font-size: 17px; }
.flow-arrow {
  width: 28px;
  height: 28px;
  border-top: 1px solid #b6beca;
  border-right: 1px solid #b6beca;
  transform: rotate(45deg);
}
.gap-line { margin-top: 16px; display: flex; flex-wrap: wrap; gap: 10px; }
@media (max-width: 900px) { .metric-grid { grid-template-columns: 1fr; } .hero-head, .toolbar { align-items: stretch; flex-direction: column; } .payment-flow { grid-template-columns: 1fr; gap: 10px; } .flow-arrow { display: none; } }
</style>
