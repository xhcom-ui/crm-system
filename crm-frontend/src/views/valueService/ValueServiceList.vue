<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="query">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="服务/客户/负责人" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="16">
      <el-col v-for="item in cards" :key="item.label" :xs="12" :md="6">
        <div class="metric-card">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <small>{{ item.hint }}</small>
        </div>
      </el-col>
    </el-row>
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>增值服务列表</span>
          <el-button type="primary" @click="openDialog">新增服务</el-button>
        </div>
      </template>
      <el-table :data="rows" v-loading="loading" border stripe>
        <el-table-column prop="name" label="服务名称" min-width="180" />
        <el-table-column prop="customerName" label="客户" min-width="180" />
        <el-table-column prop="ownerName" label="负责人" width="120" />
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="expireDate" label="到期日期" width="140" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">{{ row.status === 1 ? '服务中' : '即将到期' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增增值服务" width="560px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="服务名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="客户名称" prop="customerName"><el-input v-model="form.customerName" /></el-form-item>
        <el-form-item label="负责人"><el-input v-model="form.ownerName" /></el-form-item>
        <el-form-item label="金额"><el-input-number v-model="form.amount" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="到期日期"><el-date-picker v-model="form.expireDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="服务中" :value="1" />
            <el-option label="即将到期" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { addValueService, getValueServicePage, getValueServiceSummary } from '@/api/valueService'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const query = reactive({ keyword: '' })
const pagination = reactive({ current: 1, size: 10, total: 0 })
const form = reactive({ name: '', customerName: '', ownerName: '', amount: 0, expireDate: '', status: 1, remark: '' })
const rules = {
  name: [{ required: true, message: '请输入服务名称', trigger: 'blur' }],
  customerName: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
}
const cards = ref([
  { label: '服务中', value: 28, hint: '当前有效服务' },
  { label: '即将到期', value: 6, hint: '30 天内到期' },
  { label: '本月续费', value: '¥86,000', hint: '已确认金额' },
  { label: '待回访', value: 9, hint: '需客户成功跟进' },
])

const rows = ref([
  { name: '高级客户成功包', customerName: '星河科技有限公司', ownerName: '张三', amount: 28000, expireDate: '2026-06-30', status: 1 },
  { name: '数据迁移服务', customerName: '云启贸易', ownerName: '李四', amount: 12000, expireDate: '2026-05-12', status: 2 },
  { name: '营销顾问服务', customerName: '远山教育', ownerName: '王五', amount: 36000, expireDate: '2026-09-01', status: 1 },
])

async function loadData() {
  loading.value = true
  try {
    const [pageRes, summaryRes] = await Promise.allSettled([
      getValueServicePage({ current: pagination.current, size: pagination.size, keyword: query.keyword }),
      getValueServiceSummary(),
    ])
    if (pageRes.status === 'fulfilled') {
      rows.value = pageRes.value.data?.records || rows.value
      pagination.total = pageRes.value.data?.total || rows.value.length
    }
    if (summaryRes.status === 'fulfilled') cards.value = summaryRes.value.data || cards.value
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  loadData()
}

function handleReset() {
  query.keyword = ''
  handleSearch()
}

function openDialog() {
  resetForm()
  dialogVisible.value = true
}

async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    await addValueService(form)
    ElMessage.success('增值服务已新增')
    dialogVisible.value = false
    pagination.current = 1
    await loadData()
  } finally {
    submitLoading.value = false
  }
}

function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, { name: '', customerName: '', ownerName: '', amount: 0, expireDate: '', status: 1, remark: '' })
}

onMounted(() => loadData())
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
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
</style>
