<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="query">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="产品名称/编码" clearable @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 140px">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div class="toolbar">
        <strong>产品列表</strong>
        <el-button type="primary" @click="openDialog">新增产品</el-button>
      </div>
      <el-table :data="rows" v-loading="loading" border stripe>
        <el-table-column prop="code" label="产品编码" width="140" />
        <el-table-column prop="name" label="产品名称" min-width="180" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="price" label="标准价" width="120">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
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

    <el-dialog v-model="dialogVisible" title="新增产品" width="560px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="产品名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="产品编码" prop="code"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="分类"><el-input v-model="form.category" /></el-form-item>
        <el-form-item label="标准价"><el-input-number v-model="form.price" :min="0" style="width: 100%" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" style="width: 100%" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="上架" inactive-text="下架" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addRow">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { addProduct, getProductPage } from '@/api/product'

const dialogVisible = ref(false)
const loading = ref(false)
const formRef = ref(null)
const query = reactive({ keyword: '', status: '' })
const form = reactive({ code: '', name: '', category: '', price: 0, stock: 0, status: 1 })
const pagination = reactive({ current: 1, size: 10, total: 0 })
const rules = {
  code: [{ required: true, message: '请输入产品编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
}
const rows = ref([
  { code: 'CRM-SaaS-001', name: 'CRM 标准版', category: '软件订阅', price: 9800, stock: 999, status: 1 },
  { code: 'CRM-AUTO-002', name: '营销自动化插件', category: '增购模块', price: 3600, stock: 999, status: 1 },
  { code: 'CRM-SVC-003', name: '数据迁移服务包', category: '交付服务', price: 5200, stock: 20, status: 0 },
])

async function loadData() {
  loading.value = true
  try {
    const res = await getProductPage({ current: pagination.current, size: pagination.size, ...query })
    rows.value = res.data?.records || rows.value
    pagination.total = res.data?.total || rows.value.length
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  Object.assign(query, { keyword: '', status: '' })
  pagination.current = 1
  loadData()
}

function openDialog() {
  resetForm()
  dialogVisible.value = true
}

async function addRow() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  await addProduct(form)
  ElMessage.success('产品已添加')
  dialogVisible.value = false
  pagination.current = 1
  loadData()
}

function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, { code: '', name: '', category: '', price: 0, stock: 0, status: 1 })
}

onMounted(() => loadData())
</script>
