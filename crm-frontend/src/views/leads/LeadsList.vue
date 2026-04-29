<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="线索名称/联系方式" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd" v-if="userStore.hasPermission('leads:add')">新增线索</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="线索名称" min-width="180" />
        <el-table-column prop="contactInfo" label="联系方式" width="150" />
        <el-table-column label="来源" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ sourceLabel(row.source) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="评分" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button link type="success" size="small" @click="handleScore(row)">评分</el-button>
            <el-button link type="warning" size="small" @click="handleConvert(row)">转化</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)" v-if="userStore.hasPermission('leads:edit')">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)" v-if="userStore.hasPermission('leads:del')">删除</el-button>
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
        <el-form-item label="线索名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入线索名称" />
        </el-form-item>
        <el-form-item label="联系方式" prop="contactInfo">
          <el-input v-model="form.contactInfo" placeholder="请输入联系方式" />
        </el-form-item>
        <el-form-item label="来源" prop="source">
          <el-select v-model="form.source" style="width: 100%">
            <el-option label="官网" :value="1" /><el-option label="广告" :value="2" />
            <el-option label="推荐" :value="3" /><el-option label="社交媒体" :value="4" />
            <el-option label="其他" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="评分" prop="score">
          <el-input-number v-model="form.score" :min="0" :max="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="新建" :value="1" /><el-option label="已分配" :value="2" />
            <el-option label="已转化" :value="3" /><el-option label="已关闭" :value="4" />
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
import { getLeadPage, addLead, updateLead, deleteLead, scoreLead, convertLead } from '@/api/leads'

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
const form = reactive({ name: '', contactInfo: '', source: 1, score: 0, status: 1, remark: '' })
const rules = { name: [{ required: true, message: '请输入线索名称', trigger: 'blur' }] }

const dialogTitle = computed(() => (isEdit.value ? '编辑线索' : '新增线索'))

function sourceLabel(v) { const m = { 1: '官网', 2: '广告', 3: '推荐', 4: '社交媒体', 5: '其他' }; return m[v] || '未知' }
function statusLabel(v) { const m = { 1: '新建', 2: '已分配', 3: '已转化', 4: '已关闭' }; return m[v] || '未知' }
function statusType(v) { const m = { 1: 'info', 2: 'warning', 3: 'success', 4: 'primary' }; return m[v] || 'info' }

async function fetchData() {
  loading.value = true
  try {
    const res = await getLeadPage({ current: pagination.current, size: pagination.size, keyword: searchForm.keyword })
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
    if (isEdit.value) { await updateLead(editId.value, form); ElMessage.success('更新成功') }
    else { await addLead(form); ElMessage.success('新增成功') }
    dialogVisible.value = false; fetchData()
  } finally { submitLoading.value = false }
}

async function handleScore(row) {
  try {
    const res = await scoreLead(row.id)
    const { oldScore, newScore, isHot } = res.data
    ElMessage.success(`评分完成：${oldScore} → ${newScore}${isHot ? ' 🔥热门线索' : ''}`)
    fetchData()
  } catch { /* error handled by interceptor */ }
}

async function handleConvert(row) {
  await ElMessageBox.confirm(`确认将线索 "${row.name}" 转化为商机吗？`, '线索转化', { type: 'info' })
  try {
    await convertLead(row.id)
    ElMessage.success('转化成功')
    fetchData()
  } catch { /* error handled by interceptor */ }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除线索 "${row.name}" 吗？`, '提示', { type: 'warning' })
  await deleteLead(row.id); ElMessage.success('删除成功'); fetchData()
}

function resetForm() { formRef.value?.resetFields(); Object.assign(form, { name: '', contactInfo: '', source: 1, score: 0, status: 1, remark: '' }) }
onMounted(() => fetchData())
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.toolbar { margin-bottom: 16px; }
</style>
