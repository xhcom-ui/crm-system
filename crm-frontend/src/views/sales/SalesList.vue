<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="商机名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd" v-if="userStore.hasPermission('sales:add')">新增商机</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="商机名称" min-width="180" />
        <el-table-column prop="amount" label="预计金额" width="140">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column label="阶段" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ stageLabel(row.stage) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)" v-if="userStore.hasPermission('sales:edit')">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)" v-if="userStore.hasPermission('sales:del')">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchData"
        @current-change="fetchData"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="商机名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商机名称" />
        </el-form-item>
        <el-form-item label="预计金额" prop="amount">
          <el-input-number v-model="form.amount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="销售阶段" prop="stage">
          <el-select v-model="form.stage" style="width: 100%">
            <el-option label="初步接触" :value="1" />
            <el-option label="需求分析" :value="2" />
            <el-option label="方案报价" :value="3" />
            <el-option label="谈判" :value="4" />
            <el-option label="赢单" :value="5" />
            <el-option label="输单" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getOpportunityPage, addOpportunity, updateOpportunity, deleteOpportunity } from '@/api/sales'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref([])
const formRef = ref(null)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const editId = ref(null)

const searchForm = reactive({ keyword: '' })
const pagination = reactive({ current: 1, size: 10, total: 0 })
const form = reactive({ name: '', amount: 0, stage: 1, remark: '' })
const rules = {
  name: [{ required: true, message: '请输入商机名称', trigger: 'blur' }],
}

const dialogTitle = computed(() => (isEdit.value ? '编辑商机' : '新增商机'))

function stageLabel(val) {
  const map = { 1: '初步接触', 2: '需求分析', 3: '方案报价', 4: '谈判', 5: '赢单', 6: '输单' }
  return map[val] || '未知'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getOpportunityPage({ current: pagination.current, size: pagination.size, keyword: searchForm.keyword })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally { loading.value = false }
}

function handleSearch() { pagination.current = 1; fetchData() }
function handleReset() { searchForm.keyword = ''; handleSearch() }
function handleAdd() { isEdit.value = false; editId.value = null; resetForm(); dialogVisible.value = true }

function handleEdit(row) {
  isEdit.value = true; editId.value = row.id
  Object.assign(form, row); dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (isEdit.value) { await updateOpportunity(editId.value, form); ElMessage.success('更新成功') }
    else { await addOpportunity(form); ElMessage.success('新增成功') }
    dialogVisible.value = false; fetchData()
  } finally { submitLoading.value = false }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除商机 "${row.name}" 吗？`, '提示', { type: 'warning' })
  await deleteOpportunity(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, { name: '', amount: 0, stage: 1, remark: '' })
}

onMounted(() => fetchData())
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.toolbar { margin-bottom: 16px; }
</style>
