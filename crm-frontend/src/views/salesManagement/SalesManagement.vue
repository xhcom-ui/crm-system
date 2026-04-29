<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="query">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="订单/客户/产品/平台订单号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="'Search'" @click="handleSearch">查询</el-button>
          <el-button :icon="'RefreshLeft'" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>销售订单与平台数据</span>
          <div class="header-actions">
            <el-upload :show-file-list="false" accept=".xls,.xlsx" :before-upload="handleImport">
              <el-button :icon="'Upload'">Excel 导入</el-button>
            </el-upload>
            <el-button type="primary" :icon="'Plus'" @click="openDialog">新增订单</el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="订单列表" name="orders">
          <el-table :data="rows" v-loading="loading" border stripe @row-dblclick="openDetail">
            <el-table-column prop="orderNo" label="订单编号" width="150" />
            <el-table-column prop="platform" label="平台" width="100" />
            <el-table-column prop="platformOrderNo" label="平台订单号" min-width="150" show-overflow-tooltip />
            <el-table-column prop="customerName" label="客户" min-width="160" />
            <el-table-column prop="productName" label="产品/服务" min-width="170" />
            <el-table-column prop="amount" label="金额" width="120"><template #default="{ row }">¥{{ row.amount || 0 }}</template></el-table-column>
            <el-table-column prop="refundAmount" label="退款" width="110"><template #default="{ row }">¥{{ row.refundAmount || 0 }}</template></el-table-column>
            <el-table-column prop="ownerName" label="销售" width="100" />
            <el-table-column prop="signDate" label="日期" width="120" />
            <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag></template></el-table-column>
            <el-table-column label="操作" width="90" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="openDetail(row)">详情</el-button></template></el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="loadOrders"
            @current-change="loadOrders"
          />
        </el-tab-pane>

        <el-tab-pane label="平台配置" name="platforms">
          <div class="toolbar">
            <div class="platform-tags">
              <el-tag v-for="item in supportedPlatforms" :key="item.code" effect="plain">{{ item.name }}</el-tag>
            </div>
            <el-button type="primary" :icon="'Plus'" @click="openPlatformDialog()">新增平台配置</el-button>
          </div>
          <el-table :data="platformRows" v-loading="platformLoading" border stripe>
            <el-table-column prop="platformName" label="平台" width="100" />
            <el-table-column prop="shopName" label="店铺" min-width="150" />
            <el-table-column prop="apiEndpoint" label="接口地址" min-width="220" show-overflow-tooltip />
            <el-table-column prop="lastSyncTime" label="最近抽取" width="170" />
            <el-table-column label="状态" width="90"><template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag></template></el-table-column>
            <el-table-column label="操作" width="260" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openPlatformDialog(row)">编辑</el-button>
                <el-button link type="success" @click="handleSync(row, 'ORDER')">抽订单</el-button>
                <el-button link type="warning" @click="handleSync(row, 'REFUND')">抽退款</el-button>
                <el-button link @click="handleSync(row, 'ALL')">全量抽取</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="退款数据" name="refunds">
          <el-table :data="refundRows" v-loading="refundLoading" border stripe>
            <el-table-column prop="refundNo" label="退款编号" min-width="150" />
            <el-table-column prop="orderNo" label="订单编号" min-width="140" />
            <el-table-column prop="platformName" label="平台" width="100" />
            <el-table-column prop="shopName" label="店铺" min-width="140" />
            <el-table-column prop="amount" label="退款金额" width="120" />
            <el-table-column prop="reason" label="原因" min-width="160" />
            <el-table-column prop="refundTime" label="退款时间" width="170" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="抽取日志" name="logs">
          <el-table :data="syncLogs" v-loading="logLoading" border stripe>
            <el-table-column prop="platformName" label="平台" width="100" />
            <el-table-column prop="syncType" label="类型" width="100" />
            <el-table-column prop="orderCount" label="订单" width="80" />
            <el-table-column prop="refundCount" label="退款" width="80" />
            <el-table-column prop="message" label="结果" min-width="220" />
            <el-table-column prop="startedAt" label="开始时间" width="170" />
            <el-table-column prop="finishedAt" label="结束时间" width="170" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增销售订单" width="680px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="106px" class="form-grid">
        <el-form-item label="订单编号" prop="orderNo"><el-input v-model="form.orderNo" /></el-form-item>
        <el-form-item label="来源平台"><el-input v-model="form.platform" placeholder="淘宝/京东/抖音..." /></el-form-item>
        <el-form-item label="平台订单号"><el-input v-model="form.platformOrderNo" /></el-form-item>
        <el-form-item label="客户名称" prop="customerName"><el-input v-model="form.customerName" /></el-form-item>
        <el-form-item label="产品/服务" prop="productName"><el-input v-model="form.productName" /></el-form-item>
        <el-form-item label="合同金额"><el-input-number v-model="form.amount" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="退款金额"><el-input-number v-model="form.refundAmount" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="销售"><el-input v-model="form.ownerName" /></el-form-item>
        <el-form-item label="签约日期"><el-date-picker v-model="form.signDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="状态"><el-select v-model="form.status" style="width: 100%"><el-option label="已签约" :value="1" /><el-option label="回款中" :value="2" /><el-option label="已完成" :value="3" /></el-select></el-form-item>
        <el-form-item label="备注" class="full"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" :loading="submitLoading" @click="submitForm">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="platformDialog" title="平台配置" width="680px" @close="resetPlatformForm">
      <el-form :model="platformForm" label-width="106px" class="form-grid">
        <el-form-item label="平台"><el-select v-model="platformForm.platformCode" style="width: 100%" @change="syncPlatformName"><el-option v-for="item in supportedPlatforms" :key="item.code" :label="item.name" :value="item.code" /></el-select></el-form-item>
        <el-form-item label="平台名称"><el-input v-model="platformForm.platformName" /></el-form-item>
        <el-form-item label="店铺名称"><el-input v-model="platformForm.shopName" /></el-form-item>
        <el-form-item label="AppKey"><el-input v-model="platformForm.appKey" /></el-form-item>
        <el-form-item label="AppSecret"><el-input v-model="platformForm.appSecret" show-password /></el-form-item>
        <el-form-item label="AccessToken"><el-input v-model="platformForm.accessToken" show-password /></el-form-item>
        <el-form-item label="接口地址" class="full"><el-input v-model="platformForm.apiEndpoint" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="platformForm.status" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="备注" class="full"><el-input v-model="platformForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="platformDialog = false">取消</el-button><el-button type="primary" @click="savePlatform">保存</el-button></template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="销售订单详情" size="520px">
      <el-descriptions v-if="detail.order" :column="1" border>
        <el-descriptions-item label="订单编号">{{ detail.order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="平台">{{ detail.order.platform || '-' }}</el-descriptions-item>
        <el-descriptions-item label="平台订单号">{{ detail.order.platformOrderNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ detail.order.customerName }}</el-descriptions-item>
        <el-descriptions-item label="产品/服务">{{ detail.order.productName }}</el-descriptions-item>
        <el-descriptions-item label="金额">¥{{ detail.order.amount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="退款金额">¥{{ detail.order.refundAmount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ detail.order.paidTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="抽取时间">{{ detail.order.extractedAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ detail.order.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <h3 class="drawer-title">关联退款</h3>
      <el-table :data="detail.refunds || []" border size="small">
        <el-table-column prop="refundNo" label="退款编号" min-width="140" />
        <el-table-column prop="amount" label="金额" width="90" />
        <el-table-column prop="reason" label="原因" min-width="120" />
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addPlatformConfig,
  addSalesOrder,
  getPlatformConfigs,
  getPlatformRefunds,
  getPlatformSyncLogs,
  getSalesOrderDetail,
  getSalesOrderPage,
  getSupportedPlatforms,
  importSalesOrders,
  syncPlatformData,
  updatePlatformConfig,
} from '@/api/salesManagement'

const activeTab = ref('orders')
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const platformDialog = ref(false)
const platformLoading = ref(false)
const refundLoading = ref(false)
const logLoading = ref(false)
const formRef = ref(null)
const query = reactive({ keyword: '' })
const pagination = reactive({ current: 1, size: 10, total: 0 })
const rows = ref([])
const supportedPlatforms = ref([])
const platformRows = ref([])
const refundRows = ref([])
const syncLogs = ref([])
const detail = ref({})

const form = reactive({ orderNo: '', platform: '', platformOrderNo: '', customerName: '', productName: '', amount: 0, refundAmount: 0, ownerName: '', signDate: '', status: 1, remark: '' })
const platformForm = reactive({ id: null, platformCode: 'taobao', platformName: '淘宝', shopName: '', appKey: '', appSecret: '', accessToken: '', apiEndpoint: '', status: 1, remark: '' })
const rules = {
  orderNo: [{ required: true, message: '请输入订单编号', trigger: 'blur' }],
  customerName: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
  productName: [{ required: true, message: '请输入产品/服务', trigger: 'blur' }],
}

function statusType(status) { return { 1: 'success', 2: 'warning', 3: 'info' }[status] || '' }
function statusLabel(status) { return { 1: '已签约', 2: '回款中', 3: '已完成' }[status] || '未知' }

async function loadOrders() {
  loading.value = true
  try {
    const res = await getSalesOrderPage({ current: pagination.current, size: pagination.size, keyword: query.keyword })
    rows.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function loadPlatforms() {
  platformLoading.value = true
  try {
    const [supported, configs] = await Promise.all([getSupportedPlatforms(), getPlatformConfigs({ current: 1, size: 50 })])
    supportedPlatforms.value = supported.data || []
    platformRows.value = configs.data?.records || []
  } finally {
    platformLoading.value = false
  }
}

async function loadRefunds() {
  refundLoading.value = true
  try { refundRows.value = (await getPlatformRefunds({ current: 1, size: 50 })).data?.records || [] } finally { refundLoading.value = false }
}

async function loadLogs() {
  logLoading.value = true
  try { syncLogs.value = (await getPlatformSyncLogs({ current: 1, size: 50 })).data?.records || [] } finally { logLoading.value = false }
}

function handleTabChange(tab) {
  if (tab === 'platforms') loadPlatforms()
  if (tab === 'refunds') loadRefunds()
  if (tab === 'logs') loadLogs()
}

function handleSearch() { pagination.current = 1; loadOrders() }
function handleReset() { query.keyword = ''; handleSearch() }
function openDialog() { resetForm(); dialogVisible.value = true }

async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    await addSalesOrder(form)
    ElMessage.success('销售订单已新增')
    dialogVisible.value = false
    await loadOrders()
  } finally {
    submitLoading.value = false
  }
}

async function handleImport(file) {
  const res = await importSalesOrders(file)
  const data = res.data || {}
  const msg = `共 ${data.total || 0} 行，成功 ${data.success || 0} 行，失败 ${data.failed || 0} 行`
  if (data.errors?.length) {
    await ElMessageBox.alert(data.errors.slice(0, 8).join('\n'), msg, { type: 'warning' })
  } else {
    ElMessage.success(msg)
  }
  await loadOrders()
  return false
}

async function openDetail(row) {
  const res = await getSalesOrderDetail(row.id)
  detail.value = res.data || {}
  detailVisible.value = true
}

function openPlatformDialog(row) {
  resetPlatformForm()
  if (row) Object.assign(platformForm, row)
  platformDialog.value = true
}

function syncPlatformName(code) {
  const item = supportedPlatforms.value.find(p => p.code === code)
  if (item) platformForm.platformName = item.name
}

async function savePlatform() {
  if (platformForm.id) await updatePlatformConfig(platformForm.id, platformForm)
  else await addPlatformConfig(platformForm)
  ElMessage.success('平台配置已保存')
  platformDialog.value = false
  await loadPlatforms()
}

async function handleSync(row, type) {
  const res = await syncPlatformData(row.id, type)
  ElMessage.success(res.data?.message || '抽取完成')
  await Promise.all([loadOrders(), loadPlatforms(), loadRefunds(), loadLogs()])
}

function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, { orderNo: '', platform: '', platformOrderNo: '', customerName: '', productName: '', amount: 0, refundAmount: 0, ownerName: '', signDate: '', status: 1, remark: '' })
}

function resetPlatformForm() {
  Object.assign(platformForm, { id: null, platformCode: 'taobao', platformName: '淘宝', shopName: '', appKey: '', appSecret: '', accessToken: '', apiEndpoint: '', status: 1, remark: '' })
}

onMounted(() => Promise.all([loadOrders(), loadPlatforms()]))
</script>

<style scoped>
.card-header, .header-actions, .toolbar, .platform-tags {
  display: flex;
  align-items: center;
  gap: 12px;
}
.card-header { justify-content: space-between; }
.header-actions { flex-wrap: wrap; }
.toolbar { justify-content: space-between; margin-bottom: 12px; }
.platform-tags { flex-wrap: wrap; }
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 12px;
}
.form-grid .full { grid-column: 1 / -1; }
.drawer-title { margin: 20px 0 10px; font-size: 15px; }
@media (max-width: 900px) {
  .card-header, .toolbar { align-items: stretch; flex-direction: column; }
  .form-grid { grid-template-columns: 1fr; }
}
</style>
