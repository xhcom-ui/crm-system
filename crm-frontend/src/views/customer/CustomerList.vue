<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="姓名/公司/手机号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏 -->
    <el-card class="table-card" shadow="never">
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd" v-if="userStore.hasPermission('customer:add')">新增客户</el-button>
      </div>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="company" label="公司" min-width="180" />
        <el-table-column prop="position" label="职位" width="120" />
        <el-table-column label="来源" width="100">
          <template #default="{ row }">
            <el-tag :type="sourceType(row.source)" size="small">
              {{ sourceLabel(row.source) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="等级" width="100">
          <template #default="{ row }">
            <el-tag :type="levelType(row.level)" size="small">
              {{ levelLabel(row.level) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button link type="success" size="small" @click="handleDetail(row)">详情</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)" v-if="userStore.hasPermission('customer:edit')">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)" v-if="userStore.hasPermission('customer:del')">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="公司名称" prop="company">
          <el-input v-model="form.company" placeholder="请输入公司名称" />
        </el-form-item>
        <el-form-item label="职位" prop="position">
          <el-input v-model="form.position" placeholder="请输入职位" />
        </el-form-item>
        <el-form-item label="客户来源" prop="source">
          <el-select v-model="form.source" placeholder="请选择" style="width: 100%">
            <el-option label="网站" :value="1" />
            <el-option label="推荐" :value="2" />
            <el-option label="展会" :value="3" />
            <el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户等级" prop="level">
          <el-select v-model="form.level" placeholder="请选择" style="width: 100%">
            <el-option label="普通" :value="1" />
            <el-option label="重要" :value="2" />
            <el-option label="VIP" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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
import { getContactPage, addContact, updateContact, deleteContact } from '@/api/customer'

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

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0,
})

const form = reactive({
  name: '',
  phone: '',
  email: '',
  company: '',
  position: '',
  source: 1,
  level: 1,
  remark: '',
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
}

const dialogTitle = computed(() => (isEdit.value ? '编辑客户' : '新增客户'))

function sourceLabel(val) {
  const map = { 1: '网站', 2: '推荐', 3: '展会', 4: '其他' }
  return map[val] || '未知'
}

function sourceType(val) {
  const map = { 1: 'primary', 2: 'success', 3: 'warning', 4: 'info' }
  return map[val] || 'info'
}

function levelLabel(val) {
  const map = { 1: '普通', 2: '重要', 3: 'VIP' }
  return map[val] || '未知'
}

function levelType(val) {
  const map = { 1: 'info', 2: 'warning', 3: 'danger' }
  return map[val] || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getContactPage({
      current: pagination.current,
      size: pagination.size,
      keyword: searchForm.keyword,
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  fetchData()
}

function handleReset() {
  searchForm.keyword = ''
  handleSearch()
}

function handleAdd() {
  isEdit.value = false
  editId.value = null
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  editId.value = row.id
  Object.assign(form, row)
  dialogVisible.value = true
}

function handleDetail(row) {
  router.push(`/customer/${row.id}`)
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateContact(editId.value, form)
      ElMessage.success('更新成功')
    } else {
      await addContact(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除客户 "${row.name}" 吗？`, '提示', {
    type: 'warning',
  })
  await deleteContact(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, {
    name: '', phone: '', email: '', company: '',
    position: '', source: 1, level: 1, remark: '',
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-card {
  border-radius: 4px;
}

.table-card {
  border-radius: 4px;
}

.toolbar {
  margin-bottom: 16px;
}
</style>
