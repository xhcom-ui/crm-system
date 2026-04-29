<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="header-bar">
          <span>操作日志</span>
          <el-button type="danger" size="small" @click="handleClean" :disabled="tableData.length === 0" v-if="userStore.hasPermission('system:operlog:del')">
            清空日志
          </el-button>
        </div>
      </template>

      <!-- 搜索 -->
      <el-form :inline="true" :model="searchForm" style="margin-bottom: 16px">
        <el-form-item label="模块">
          <el-input v-model="searchForm.title" placeholder="操作模块" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="searchForm.operName" placeholder="操作人" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="操作模块" width="130" />
        <el-table-column prop="operType" label="操作类型" width="100" />
        <el-table-column prop="operName" label="操作人" width="100" />
        <el-table-column prop="requestUrl" label="请求URL" min-width="220" show-overflow-tooltip />
        <el-table-column prop="requestMethod" label="方式" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costTime" label="耗时(ms)" width="90" />
        <el-table-column prop="createTime" label="操作时间" width="180" />
      </el-table>
      <el-pagination
        v-model:current-page="pagination.current" v-model:page-size="pagination.size"
        :total="pagination.total" :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchData" @current-change="fetchData"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getOperLogPage, cleanOperLog } from '@/api/system'

const userStore = useUserStore()

const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({ title: '', operName: '' })
const pagination = reactive({ current: 1, size: 10, total: 0 })

async function fetchData() {
  loading.value = true
  try {
    const res = await getOperLogPage({
      current: pagination.current,
      size: pagination.size,
      title: searchForm.title || undefined,
      operName: searchForm.operName || undefined,
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally { loading.value = false }
}

function handleSearch() { pagination.current = 1; fetchData() }
function handleReset() { searchForm.title = ''; searchForm.operName = ''; handleSearch() }

async function handleClean() {
  await ElMessageBox.confirm('确认清空所有操作日志吗？此操作不可恢复。', '提示', { type: 'warning' })
  await cleanOperLog()
  ElMessage.success('日志已清空')
  fetchData()
}

onMounted(() => fetchData())
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.header-bar { display: flex; justify-content: space-between; align-items: center; }
</style>
