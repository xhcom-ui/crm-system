<template>
  <div class="page-container">
    <div class="page-heading">
      <div>
        <h1>工作流审批实例追踪</h1>
        <p>追踪流程实例状态、当前节点、执行结果和异常实例。</p>
      </div>
      <el-button :icon="'ArrowLeft'" @click="router.push('/workflow')">返回工作流</el-button>
    </div>

    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="工作流 ID">
          <el-input v-model="query.workflowId" placeholder="可选" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="14">
        <el-card shadow="never">
          <template #header><span>实例列表</span></template>
          <el-table :data="instances" v-loading="loading" border stripe @row-click="selectRow">
            <el-table-column prop="id" label="实例 ID" width="100" />
            <el-table-column prop="workflowName" label="工作流" min-width="180" />
            <el-table-column prop="currentNodeName" label="当前节点" min-width="150" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="updatedAt" label="更新时间" width="160" />
          </el-table>
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="fetchData"
            @current-change="fetchData"
          />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="10">
        <el-card shadow="never" class="trace-card">
          <template #header><span>执行轨迹</span></template>
          <div v-if="selected">
            <h3>{{ selected.workflowName }}</h3>
            <p class="muted">实例 {{ selected.id }} · {{ statusLabel(selected.status) }}</p>
            <div class="action-row">
              <el-button size="small" type="success" @click="handleAction('approve')">通过</el-button>
              <el-button size="small" type="warning" @click="handleAction('transfer')">转交</el-button>
              <el-button size="small" type="danger" @click="handleAction('reject')">驳回</el-button>
              <el-button size="small" @click="handleAction('retry')">重试</el-button>
            </div>
            <el-steps :active="activeStep" direction="vertical" finish-status="success">
              <el-step v-for="step in traceSteps" :key="step.title" :title="step.title" :description="step.desc" />
            </el-steps>
            <el-divider />
            <pre class="result-box">{{ selected.result || '暂无执行结果' }}</pre>
          </div>
          <el-empty v-else description="选择左侧实例查看轨迹" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  approveWorkflowInstance,
  getWorkflowInstances,
  getWorkflowInstanceTrace,
  rejectWorkflowInstance,
  retryWorkflowInstance,
  transferWorkflowInstance,
} from '@/api/workflow'

const router = useRouter()
const loading = ref(false)
const instances = ref([])
const selected = ref(null)
const query = reactive({ workflowId: '' })
const pagination = reactive({ current: 1, size: 10, total: 0 })

const fallbackInstances = [
  { id: 1001, workflowId: 1, workflowName: '线索自动分配', currentNodeOrder: 2, currentNodeName: '销售接收', status: 1, updatedAt: '今天 10:30', result: '{ action: 线索分配, triggered: true }' },
  { id: 1002, workflowId: 2, workflowName: '合同审批', currentNodeOrder: 3, currentNodeName: '财务复核', status: 1, updatedAt: '今天 09:45', result: '{ approver: 财务, status: processing }' },
  { id: 1003, workflowId: 3, workflowName: '工单升级', currentNodeOrder: 4, currentNodeName: '主管确认', status: 2, updatedAt: '昨天 17:20', result: '{ status: completed }' },
]

const traceData = ref(null)

const traceSteps = computed(() => {
  if (traceData.value?.steps?.length) return traceData.value.steps
  const current = selected.value?.currentNodeOrder || 1
  return [
    { title: '触发流程', desc: '创建流程实例并写入上下文' },
    { title: '条件判断', desc: '根据业务类型匹配审批路径' },
    { title: selected.value?.currentNodeName || '当前节点', desc: '当前待处理节点' },
    { title: '完成归档', desc: '写入结果并通知相关人员' },
  ].map((item, index) => index + 1 < current ? { ...item, desc: `${item.desc}，已完成` } : item)
})

const activeStep = computed(() => traceData.value?.activeStep ?? Math.max((selected.value?.currentNodeOrder || 1) - 1, 0))

function statusLabel(v) {
  const map = { 1: '进行中', 2: '已完成', 3: '已取消', 4: '失败' }
  return map[v] || '未知'
}

function statusType(v) {
  const map = { 1: 'warning', 2: 'success', 3: 'info', 4: 'danger' }
  return map[v] || ''
}

async function selectRow(row) {
  selected.value = row
  traceData.value = null
  try {
    const res = await getWorkflowInstanceTrace(row.id)
    traceData.value = res.data || null
  } catch {
    traceData.value = null
  }
}

function reset() {
  query.workflowId = ''
  pagination.current = 1
  fetchData()
}

async function fetchData() {
  loading.value = true
  try {
    const params = { current: pagination.current, size: pagination.size }
    if (query.workflowId) params.workflowId = query.workflowId
    const res = await getWorkflowInstances(params)
    instances.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch {
    instances.value = fallbackInstances
    pagination.total = fallbackInstances.length
  } finally {
    if (instances.value[0]) await selectRow(instances.value[0])
    else selected.value = null
    loading.value = false
  }
}

async function handleAction(action) {
  if (!selected.value) return
  const actions = {
    approve: approveWorkflowInstance,
    reject: rejectWorkflowInstance,
    transfer: transferWorkflowInstance,
    retry: retryWorkflowInstance,
  }
  await actions[action](selected.value.id, { reason: actionLabel(action) })
  ElMessage.success(`${actionLabel(action)}成功`)
  await fetchData()
}

function actionLabel(action) {
  return { approve: '审批通过', reject: '审批驳回', transfer: '节点转交', retry: '实例重试' }[action] || '处理'
}

onMounted(() => fetchData())
</script>

<style scoped>
.page-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.page-heading h1 {
  margin: 0;
  font-size: 24px;
}

.page-heading p,
.muted {
  margin: 6px 0 0;
  color: #64748b;
}

.trace-card {
  min-height: 500px;
}

.action-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 14px 0;
}

.result-box {
  min-height: 92px;
  padding: 12px;
  overflow: auto;
  border-radius: 8px;
  background: #0f172a;
  color: #dbeafe;
  white-space: pre-wrap;
}

@media (max-width: 768px) {
  .page-heading {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
