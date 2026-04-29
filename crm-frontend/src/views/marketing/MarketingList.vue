<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="活动名称" clearable @keyup.enter="handleSearch" />
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
          <el-button type="primary" @click="handleAdd" v-if="userStore.hasPermission('marketing:add')">新增活动</el-button>
        </div>
        <el-button :icon="'DataAnalysis'" @click="router.push('/marketing/report')">效果报表</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="活动名称" min-width="200" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ typeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)" v-if="userStore.hasPermission('marketing:edit')">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)" v-if="userStore.hasPermission('marketing:del')">删除</el-button>
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
        <el-form-item label="活动名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="活动类型" prop="type">
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="邮件" :value="1" />
            <el-option label="短信" :value="2" />
            <el-option label="推送" :value="3" />
            <el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="草稿" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已取消" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动内容">
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
import { getCampaignPage, addCampaign, updateCampaign, deleteCampaign } from '@/api/marketing'

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
const form = reactive({ name: '', type: 1, status: 0, content: '' })
const rules = { name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }] }

const dialogTitle = computed(() => (isEdit.value ? '编辑活动' : '新增活动'))

function typeLabel(v) { const m = { 1: '邮件', 2: '短信', 3: '推送', 4: '其他' }; return m[v] || '未知' }
function statusLabel(v) { const m = { 0: '草稿', 1: '进行中', 2: '已完成', 3: '已取消' }; return m[v] || '未知' }
function statusType(v) { const m = { 0: 'info', 1: 'success', 2: 'primary', 3: 'warning' }; return m[v] || 'info' }

async function fetchData() {
  loading.value = true
  try {
    const res = await getCampaignPage({ current: pagination.current, size: pagination.size, keyword: searchForm.keyword })
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
    if (isEdit.value) { await updateCampaign(editId.value, form); ElMessage.success('更新成功') }
    else { await addCampaign(form); ElMessage.success('新增成功') }
    dialogVisible.value = false; fetchData()
  } finally { submitLoading.value = false }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除活动 "${row.name}" 吗？`, '提示', { type: 'warning' })
  await deleteCampaign(row.id); ElMessage.success('删除成功'); fetchData()
}

function resetForm() { formRef.value?.resetFields(); Object.assign(form, { name: '', type: 1, status: 0, content: '' }) }
onMounted(() => fetchData())
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.toolbar { margin-bottom: 16px; }
</style>
