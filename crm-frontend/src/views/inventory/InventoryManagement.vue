<template>
  <div class="page-container ops-page">
    <el-card shadow="never">
      <div class="hero-head">
        <div>
          <h2>库存与进销存</h2>
          <p>当前库存、上期库存、库存流水、采购销售库存汇总。</p>
        </div>
        <el-button type="primary" :icon="'Plus'" @click="movementDialog = true">新增流水</el-button>
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
        <el-tab-pane label="当前库存" name="stocks">
          <div class="toolbar">
            <el-input v-model="query.keyword" :prefix-icon="'Search'" placeholder="产品编码/名称" clearable @keyup.enter="loadStocks" />
            <el-button type="primary" :icon="'Refresh'" @click="loadStocks">刷新</el-button>
          </div>
          <el-table :data="stocks" v-loading="loading" border stripe>
            <el-table-column prop="warehouseName" label="仓库" width="130" />
            <el-table-column prop="productCode" label="编码" width="140" />
            <el-table-column prop="productName" label="产品" min-width="170" />
            <el-table-column prop="openingQty" label="期初" width="80" />
            <el-table-column prop="inboundQty" label="入库" width="80" />
            <el-table-column prop="outboundQty" label="出库" width="80" />
            <el-table-column prop="availableQty" label="可用" width="80" />
            <el-table-column prop="closingQty" label="期末" width="80" />
            <el-table-column prop="stockAmount" label="库存金额" width="120" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="上期库存" name="previous">
          <el-table :data="previousRows" border stripe>
            <el-table-column prop="period" label="期间" width="100" />
            <el-table-column prop="productCode" label="编码" width="140" />
            <el-table-column prop="productName" label="产品" min-width="170" />
            <el-table-column prop="openingQty" label="期初" width="90" />
            <el-table-column prop="inboundQty" label="入库" width="90" />
            <el-table-column prop="outboundQty" label="出库" width="90" />
            <el-table-column prop="closingQty" label="期末" width="90" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="进销存报表" name="report">
          <el-table :data="reportRows" border stripe>
            <el-table-column prop="period" label="期间" width="100" />
            <el-table-column prop="productName" label="产品" min-width="170" />
            <el-table-column prop="openingQty" label="期初" width="90" />
            <el-table-column prop="purchaseQty" label="采购" width="90" />
            <el-table-column prop="salesQty" label="销售" width="90" />
            <el-table-column prop="closingQty" label="期末" width="90" />
            <el-table-column prop="stockAmount" label="库存金额" width="120" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="库存流水" name="movements">
          <el-table :data="movements" border stripe>
            <el-table-column prop="movementNo" label="流水号" min-width="160" />
            <el-table-column prop="movementType" label="类型" width="100" />
            <el-table-column prop="productName" label="产品" min-width="170" />
            <el-table-column prop="quantity" label="数量" width="90" />
            <el-table-column prop="amount" label="金额" width="120" />
            <el-table-column prop="bizNo" label="业务单号" min-width="150" />
            <el-table-column prop="movementTime" label="时间" width="180" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="待补能力" name="missing">
          <div class="capability-actions">
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
          <el-table :data="capabilityRows" border stripe class="capability-table">
            <el-table-column prop="name" label="能力" width="150" />
            <el-table-column prop="desc" label="补充说明" min-width="260" />
            <el-table-column prop="status" label="状态" width="110">
              <template #default="{ row }"><el-tag type="warning" size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="movementDialog" title="新增库存流水" width="620px" @close="resetMovement">
      <el-form :model="movementForm" label-width="100px" class="dialog-grid">
        <el-form-item label="产品编码"><el-input v-model="movementForm.productCode" /></el-form-item>
        <el-form-item label="产品名称"><el-input v-model="movementForm.productName" /></el-form-item>
        <el-form-item label="仓库"><el-input v-model="movementForm.warehouseCode" /></el-form-item>
        <el-form-item label="类型"><el-select v-model="movementForm.movementType" style="width: 100%"><el-option label="采购入库" value="PURCHASE_IN" /><el-option label="销售出库" value="SALE_OUT" /><el-option label="盘点调整" value="ADJUST" /></el-select></el-form-item>
        <el-form-item label="数量"><el-input-number v-model="movementForm.quantity" style="width: 100%" /></el-form-item>
        <el-form-item label="单价"><el-input-number v-model="movementForm.unitCost" :min="0" style="width: 100%" /></el-form-item>
        <el-form-item label="业务单号"><el-input v-model="movementForm.bizNo" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="movementDialog = false">取消</el-button><el-button type="primary" @click="saveMovement">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="capabilityDialog" :title="selectedCapability.name" width="560px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="能力说明">{{ selectedCapability.desc }}</el-descriptions-item>
        <el-descriptions-item label="建议接口">{{ selectedCapability.api }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">{{ selectedCapability.status }}</el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button type="primary" @click="capabilityDialog = false">知道了</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { addInventoryMovement, getInventoryMovements, getInventoryOverview, getInventoryStocks, getPreviousInventory, getPurchasingSalesStock } from '@/api/inventory'

const activeTab = ref('stocks')
const loading = ref(false)
const movementDialog = ref(false)
const capabilityDialog = ref(false)
const query = reactive({ keyword: '' })
const metrics = ref([])
const missingCapabilities = ref([])
const capabilityRows = ref([])
const selectedCapability = ref({})
const stocks = ref([])
const previousRows = ref([])
const reportRows = ref([])
const movements = ref([])
const movementForm = reactive({ productCode: '', productName: '', warehouseCode: 'MAIN', movementType: 'PURCHASE_IN', quantity: 1, unitCost: 0, bizNo: '' })

async function loadOverview() {
  const res = await getInventoryOverview()
  metrics.value = res.data?.metrics || []
  missingCapabilities.value = res.data?.missingCapabilities || []
  capabilityRows.value = (res.data?.capabilities || []).length
    ? res.data.capabilities
    : missingCapabilities.value.map(name => ({ name, desc: capabilityDesc(name), api: `/sales/inventory/capability/${name}`, status: '待建设' }))
}
async function loadStocks() {
  loading.value = true
  try {
    const res = await getInventoryStocks({ current: 1, size: 50, ...query })
    stocks.value = res.data?.records || []
  } finally {
    loading.value = false
  }
}
async function loadPrevious() { previousRows.value = (await getPreviousInventory()).data || [] }
async function loadReport() { reportRows.value = (await getPurchasingSalesStock()).data || [] }
async function loadMovements() { movements.value = (await getInventoryMovements({ current: 1, size: 50 })).data?.records || [] }
async function saveMovement() {
  await addInventoryMovement(movementForm)
  ElMessage.success('库存流水已保存')
  movementDialog.value = false
  await Promise.all([loadOverview(), loadMovements()])
}
function resetMovement() { Object.assign(movementForm, { productCode: '', productName: '', warehouseCode: 'MAIN', movementType: 'PURCHASE_IN', quantity: 1, unitCost: 0, bizNo: '' }) }
function capabilityDesc(name) {
  return {
    采购审批: '采购申请、审批流、入库联动和供应商报价比价。',
    批次效期管理: '按批次号、生产日期、有效期管理库存先进先出。',
    盘点差异处理: '盘点任务、实盘数量、差异审批和自动生成调整流水。',
    多仓调拨: '仓库间调拨申请、出库、在途、入库闭环。',
    供应商协同: '供应商档案、交期、对账和补货协同。',
  }[name] || '库存进销存扩展能力。'
}
function openCapability(name) {
  selectedCapability.value = capabilityRows.value.find(item => item.name === name) || { name, desc: capabilityDesc(name), api: `/sales/inventory/capability/${name}`, status: '待建设' }
  capabilityDialog.value = true
}

watch(activeTab, tab => {
  if (tab === 'previous') loadPrevious()
  if (tab === 'report') loadReport()
  if (tab === 'movements') loadMovements()
})
onMounted(() => Promise.all([loadOverview(), loadStocks(), loadPrevious(), loadReport(), loadMovements()]))
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
.toolbar .el-input { max-width: 280px; }
.capability-actions { display: flex; flex-wrap: wrap; gap: 10px; }
.capability-table { margin-top: 14px; }
.dialog-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); column-gap: 12px; }
@media (max-width: 900px) { .metric-grid, .dialog-grid { grid-template-columns: 1fr; } .hero-head, .toolbar { align-items: stretch; flex-direction: column; } }
</style>
