<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="工作流名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-button type="primary" @click="handleAdd" v-if="userStore.hasPermission('workflow:add')">新增工作流</el-button>
        </div>
        <el-button :icon="'Connection'" @click="router.push('/workflow/instances')">实例追踪</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="工作流名称" min-width="200" />
        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ typeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <el-button link type="success" size="small" @click="handleDesign(row)">设计</el-button>
            <el-button link type="warning" size="small" @click="handleTrigger(row)">触发</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)" v-if="userStore.hasPermission('workflow:edit')">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)" v-if="userStore.hasPermission('workflow:del')">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current" v-model:page-size="pagination.size"
        :total="pagination.total" :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchData" @current-change="fetchData"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="工作流名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入工作流名称" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="线索分配" :value="1" /><el-option label="合同审批" :value="2" />
            <el-option label="工单流转" :value="3" /><el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input-number v-model="form.priority" :min="0" :max="99" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getWorkflowPage, addWorkflow, updateWorkflow, deleteWorkflow, triggerWorkflow } from '@/api/workflow'

const userStore = useUserStore()
const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const formRef = ref(null)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const editId = ref(null)

const searchForm = reactive({ keyword: '' })
const pagination = reactive({ current: 1, size: 10, total: 0 })
const form = reactive({ name: '', type: 1, priority: 0, status: 1, description: '' })
const rules = { name: [{ required: true, message: '请输入工作流名称', trigger: 'blur' }] }

const dialogTitle = computed(() => (isEdit.value ? '编辑工作流' : '新增工作流'))

function typeLabel(v) { const m = { 1: '线索分配', 2: '合同审批', 3: '工单流转', 4: '其他' }; return m[v] || '未知' }

async function fetchData() {
  loading.value = true
  try {
    const res = await getWorkflowPage({ current: pagination.current, size: pagination.size, keyword: searchForm.keyword })
    tableData.value = res.data.records; pagination.total = res.data.total
  } finally { loading.value = false }
}

function handleSearch() { pagination.current = 1; fetchData() }
function handleReset() { searchForm.keyword = ''; handleSearch() }
function handleAdd() { isEdit.value = false; editId.value = null; resetForm(); dialogVisible.value = true }
function handleEdit(row) { isEdit.value = true; editId.value = row.id; Object.assign(form, row); dialogVisible.value = true }

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (isEdit.value) { await updateWorkflow(editId.value, form); ElMessage.success('更新成功') }
    else { await addWorkflow(form); ElMessage.success('新增成功') }
    dialogVisible.value = false; fetchData()
  } finally { submitLoading.value = false }
}

function handleDesign(row) {
  router.push(`/workflow/${row.id}/design`)
}

async function handleTrigger(row) {
  try {
    const res = await triggerWorkflow(row.id)
    const data = res.data || {}
    ElMessage.success(`工作流已触发 (实例 ${data.instanceId})，当前节点: ${data.currentNode}`)
    fetchData()
  } catch { /* error handled by interceptor */ }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除工作流 "${row.name}" 吗？`, '提示', { type: 'warning' })
  await deleteWorkflow(row.id); ElMessage.success('删除成功'); fetchData()
}

function resetForm() { formRef.value?.resetFields(); Object.assign(form, { name: '', type: 1, priority: 0, status: 1, description: '' }) }
onMounted(() => fetchData())
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.toolbar { margin-bottom: 16px; }
</style>
