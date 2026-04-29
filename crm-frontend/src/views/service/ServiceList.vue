<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="工单标题/编号" clearable @keyup.enter="handleSearch" />
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
          <el-button type="primary" @click="handleAdd" v-if="userStore.hasPermission('service:add')">创建工单</el-button>
        </div>
        <el-button :icon="'Collection'" @click="router.push('/service/knowledge')">SLA 与知识库</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="ticketNo" label="工单编号" width="160" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="priorityType(row.priority)" size="small">{{ priorityLabel(row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <el-button link type="success" size="small" @click="handleAssign(row)">分配</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)" v-if="userStore.hasPermission('service:edit')">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)" v-if="userStore.hasPermission('service:del')">删除</el-button>
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
        <el-form-item label="工单标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入工单标题" />
        </el-form-item>
        <el-form-item label="工单类型" prop="type">
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="咨询" :value="1" /><el-option label="投诉" :value="2" />
            <el-option label="建议" :value="3" /><el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="form.priority" style="width: 100%">
            <el-option label="低" :value="1" /><el-option label="中" :value="2" />
            <el-option label="高" :value="3" /><el-option label="紧急" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="待处理" :value="1" /><el-option label="处理中" :value="2" />
            <el-option label="已解决" :value="3" /><el-option label="已关闭" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="工单内容">
          <el-input v-model="form.content" type="textarea" :rows="4" />
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
import { getTicketPage, addTicket, updateTicket, deleteTicket, assignTicket } from '@/api/service'

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
const form = reactive({ title: '', type: 1, priority: 1, status: 1, content: '' })
const rules = { title: [{ required: true, message: '请输入工单标题', trigger: 'blur' }] }

const dialogTitle = computed(() => (isEdit.value ? '编辑工单' : '创建工单'))

function priorityLabel(v) { const m = { 1: '低', 2: '中', 3: '高', 4: '紧急' }; return m[v] || '未知' }
function priorityType(v) { const m = { 1: 'info', 2: 'primary', 3: 'warning', 4: 'danger' }; return m[v] || 'info' }
function statusLabel(v) { const m = { 1: '待处理', 2: '处理中', 3: '已解决', 4: '已关闭' }; return m[v] || '未知' }
function statusType(v) { const m = { 1: 'danger', 2: 'warning', 3: 'success', 4: 'info' }; return m[v] || 'info' }

async function fetchData() {
  loading.value = true
  try {
    const res = await getTicketPage({ current: pagination.current, size: pagination.size, keyword: searchForm.keyword })
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
    if (isEdit.value) { await updateTicket(editId.value, form); ElMessage.success('更新成功') }
    else { await addTicket(form); ElMessage.success('创建成功') }
    dialogVisible.value = false; fetchData()
  } finally { submitLoading.value = false }
}

async function handleAssign(row) {
  try {
    const res = await assignTicket(row.id)
    const { oldAssignee, newAssignee, status } = res.data || {}
    ElMessage.success(`工单已分配${newAssignee ? '给 ID:' + newAssignee : ''}，状态: ${status}`)
    fetchData()
  } catch { /* error handled by interceptor */ }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除工单 "${row.title}" 吗？`, '提示', { type: 'warning' })
  await deleteTicket(row.id); ElMessage.success('删除成功'); fetchData()
}

function resetForm() { formRef.value?.resetFields(); Object.assign(form, { title: '', type: 1, priority: 1, status: 1, content: '' }) }
onMounted(() => fetchData())
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.toolbar { margin-bottom: 16px; }
</style>
