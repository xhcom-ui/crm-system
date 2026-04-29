<template>
  <div class="designer-container">
    <!-- 顶部面包屑 -->
    <el-page-header @back="goBack" :title="workflow.name || '工作流设计器'" />

    <!-- 工作流基本信息 -->
    <el-card class="info-card" shadow="never">
      <template #header><strong>基本信息</strong></template>
      <el-form :model="workflow" label-width="100px" inline>
        <el-form-item label="名称">
          <el-input v-model="workflow.name" placeholder="工作流名称" style="width:200px" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="workflow.type" style="width:140px">
            <el-option label="线索分配" :value="1" /><el-option label="合同审批" :value="2" />
            <el-option label="工单流转" :value="3" /><el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="workflow.priority" :min="0" :max="99" style="width:120px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="workflow.statusBool" active-text="启用" inactive-text="禁用" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveWorkflow">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 流程步骤设计区 -->
    <el-card class="steps-card" shadow="never">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <strong>流程步骤 ({{ nodes.length }} 步)</strong>
          <el-button type="primary" size="small" @click="handleAddNode">+ 添加步骤</el-button>
        </div>
      </template>

      <div v-if="nodes.length === 0" class="empty-hint">
        <el-icon :size="48"><Setting /></el-icon>
        <p>暂无步骤，点击上方按钮开始设计流程</p>
      </div>

      <div v-else class="flow-chain">
        <!-- 开始节点 -->
        <div class="chain-node start-node">
          <el-tag type="success" size="large" round>开始</el-tag>
        </div>

        <template v-for="(node, index) in nodes" :key="node.id">
          <!-- 连接箭头 -->
          <div class="chain-arrow">
            <div class="arrow-line" />
            <el-icon><ArrowDown /></el-icon>
          </div>

          <!-- 步骤卡片 -->
          <div class="step-card" :class="{ 'step-active': index === 0 }">
            <div class="step-badge">{{ node.stepOrder || index + 1 }}</div>
            <div class="step-body">
              <div class="step-header">
                <span class="step-name">{{ node.stepName || '未命名步骤' }}</span>
                <el-tag :type="stepTypeTag(node.stepType)" size="small">
                  {{ stepTypeLabel(node.stepType) }}
                </el-tag>
              </div>
              <div class="step-meta">
                <el-icon><User /></el-icon>
                <span>{{ assigneeLabel(node) }}</span>
              </div>
            </div>
            <div class="step-actions">
              <el-tooltip content="上移">
                <el-button :disabled="index === 0" link size="small" @click="handleReorder(node, 'up')">
                  <el-icon><Top /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="下移">
                <el-button :disabled="index === nodes.length - 1" link size="small" @click="handleReorder(node, 'down')">
                  <el-icon><Bottom /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="编辑">
                <el-button link type="primary" size="small" @click="handleEditNode(node)"><el-icon><Edit /></el-icon></el-button>
              </el-tooltip>
              <el-tooltip content="删除">
                <el-button link type="danger" size="small" @click="handleDeleteNode(node)"><el-icon><Delete /></el-icon></el-button>
              </el-tooltip>
            </div>
          </div>
        </template>

        <!-- 结束箭头 -->
        <div class="chain-arrow">
          <div class="arrow-line" />
          <el-icon><ArrowDown /></el-icon>
        </div>

        <!-- 结束节点 -->
        <div class="chain-node end-node">
          <el-tag type="danger" size="large" round>结束</el-tag>
        </div>
      </div>
    </el-card>

    <!-- 步骤编辑对话框 -->
    <el-dialog v-model="nodeDialogVisible" :title="nodeDialogTitle" width="520px" @close="resetNodeForm">
      <el-form ref="nodeFormRef" :model="nodeForm" :rules="nodeRules" label-width="100px">
        <el-form-item label="步骤名称" prop="stepName">
          <el-input v-model="nodeForm.stepName" placeholder="例如：主管审批" />
        </el-form-item>
        <el-form-item label="步骤类型" prop="stepType">
          <el-select v-model="nodeForm.stepType" style="width:100%">
            <el-option label="审批" :value="1" /><el-option label="通知" :value="2" />
            <el-option label="自动操作" :value="3" /><el-option label="条件分支" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理人类型" prop="assigneeType">
          <el-select v-model="nodeForm.assigneeType" style="width:100%">
            <el-option label="指定人" :value="1" /><el-option label="角色" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理人" prop="assigneeValue">
          <el-select
            v-if="nodeForm.assigneeType === 1"
            v-model="nodeForm.assigneeValue"
            filterable
            clearable
            :loading="assigneeLoading"
            placeholder="请选择处理人"
            style="width:100%"
          >
            <el-option
              v-for="user in userOptions"
              :key="user.id"
              :label="userLabel(user)"
              :value="String(user.id)"
            />
          </el-select>
          <el-select
            v-else
            v-model="nodeForm.assigneeValue"
            filterable
            clearable
            :loading="assigneeLoading"
            placeholder="请选择处理角色"
            style="width:100%"
          >
            <el-option
              v-for="role in roleOptions"
              :key="role.id"
              :label="roleLabel(role)"
              :value="role.roleKey"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="步骤配置">
          <el-input v-model="nodeForm.stepConfig" type="textarea" :rows="3" placeholder='JSON格式，如 {"timeout":24,"autoApprove":false}' />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="nodeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitNode" :loading="nodeSubmitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getWorkflowById, updateWorkflow } from '@/api/workflow'
import { getWorkflowNodes, addWorkflowNode, updateWorkflowNode, deleteWorkflowNode, reorderWorkflowNode } from '@/api/workflow'
import { getAllRoles, getUserList } from '@/api/system'

const route = useRoute()
const router = useRouter()
const workflowId = route.params.id

const workflow = reactive({ name: '', type: 1, priority: 0, status: 1, statusBool: true, description: '' })
const nodes = ref([])

// 步骤编辑
const nodeDialogVisible = ref(false)
const nodeDialogTitle = ref('新增步骤')
const nodeSubmitLoading = ref(false)
const nodeFormRef = ref(null)
const isEditNode = ref(false)
const editNodeId = ref(null)
const nodeForm = reactive({ workflowId: Number(workflowId), stepName: '', stepType: 1, assigneeType: 1, assigneeValue: '', stepConfig: '' })
const nodeRules = {
  stepName: [{ required: true, message: '请输入步骤名称', trigger: 'blur' }],
  assigneeValue: [{ required: true, message: '请选择处理人', trigger: 'change' }],
}
const assigneeLoading = ref(false)
const userOptions = ref([])
const roleOptions = ref([])

function stepTypeLabel(v) { const m = { 1: '审批', 2: '通知', 3: '自动操作', 4: '条件分支' }; return m[v] || '未知' }
function stepTypeTag(v) { const m = { 1: 'warning', 2: 'info', 3: 'success', 4: 'primary' }; return m[v] || 'info' }
function assigneeLabel(n) {
  if (n.assigneeType === 2) {
    const role = roleOptions.value.find(item => item.roleKey === n.assigneeValue)
    return `角色: ${role ? role.roleName : (n.assigneeValue || '未设置')}`
  }
  const user = userOptions.value.find(item => String(item.id) === String(n.assigneeValue))
  return `用户: ${user ? userLabel(user) : (n.assigneeValue || '未设置')}`
}
function userLabel(user) { return `${user.realName || user.username} (${user.username})` }
function roleLabel(role) { return `${role.roleName} (${role.roleKey})` }

function goBack() { router.push('/workflow') }

async function loadWorkflow() {
  try {
    const res = await getWorkflowById(workflowId)
    const d = res.data
    Object.assign(workflow, d, { statusBool: d.status === 1 })
  } catch { /* handled */ }
}

async function saveWorkflow() {
  try {
    await updateWorkflow(workflowId, { ...workflow, status: workflow.statusBool ? 1 : 0, statusBool: undefined })
    ElMessage.success('保存成功')
  } catch { /* handled */ }
}

async function loadNodes() {
  try {
    const res = await getWorkflowNodes(workflowId)
    nodes.value = res.data || []
  } catch { /* handled */ }
}

// --- 节点 CRUD ---
function handleAddNode() {
  isEditNode.value = false; editNodeId.value = null
  nodeDialogTitle.value = '新增步骤'
  resetNodeForm()
  loadAssigneeOptions()
  nodeDialogVisible.value = true
}

function handleEditNode(node) {
  isEditNode.value = true; editNodeId.value = node.id
  nodeDialogTitle.value = '编辑步骤'
  Object.assign(nodeForm, { ...node, assigneeValue: node.assigneeValue == null ? '' : String(node.assigneeValue) })
  loadAssigneeOptions()
  nodeDialogVisible.value = true
}

async function handleDeleteNode(node) {
  await ElMessageBox.confirm(`确认删除步骤 "${node.stepName}" 吗？`, '提示', { type: 'warning' })
  await deleteWorkflowNode(node.id)
  ElMessage.success('删除成功')
  loadNodes()
}

async function handleReorder(node, direction) {
  try {
    await reorderWorkflowNode(node.id, direction)
    loadNodes()
  } catch { /* handled */ }
}

async function submitNode() {
  const valid = await nodeFormRef.value.validate().catch(() => false)
  if (!valid) return
  nodeSubmitLoading.value = true
  try {
    if (isEditNode.value) {
      await updateWorkflowNode(editNodeId.value, nodeForm)
      ElMessage.success('更新成功')
    } else {
      await addWorkflowNode({ ...nodeForm, workflowId: Number(workflowId) })
      ElMessage.success('添加成功')
    }
    nodeDialogVisible.value = false
    loadNodes()
  } finally { nodeSubmitLoading.value = false }
}

function resetNodeForm() {
  nodeFormRef.value?.resetFields()
  Object.assign(nodeForm, { workflowId: Number(workflowId), stepName: '', stepType: 1, assigneeType: 1, assigneeValue: '', stepConfig: '' })
}

async function loadAssigneeOptions() {
  if (userOptions.value.length && roleOptions.value.length) return
  assigneeLoading.value = true
  try {
    const [userRes, roleRes] = await Promise.allSettled([
      getUserList({ current: 1, size: 200 }),
      getAllRoles(),
    ])
    if (userRes.status === 'fulfilled') userOptions.value = userRes.value.data?.records || []
    if (roleRes.status === 'fulfilled') roleOptions.value = roleRes.value.data || []
  } finally {
    assigneeLoading.value = false
  }
}

onMounted(() => { loadWorkflow(); loadNodes(); loadAssigneeOptions() })
</script>

<style scoped>
.designer-container { display: flex; flex-direction: column; gap: 16px; }

.info-card :deep(.el-form) { display: flex; flex-wrap: wrap; gap: 8px; }

.empty-hint {
  text-align: center; padding: 48px 0; color: #909399;
}
.empty-hint p { margin-top: 12px; font-size: 14px; }

.flow-chain {
  display: flex; flex-direction: column; align-items: center;
  padding: 20px 0;
}

.chain-node { padding: 8px 0; }

.chain-arrow {
  display: flex; flex-direction: column; align-items: center;
  padding: 4px 0; color: #c0c4cc;
}
.chain-arrow .arrow-line {
  width: 2px; height: 16px; background: linear-gradient(to bottom, #409eff, #67c23a);
}

.step-card {
  display: flex; align-items: center; gap: 12px;
  width: 480px; max-width: 100%;
  padding: 14px 16px; border: 2px solid #e4e7ed;
  border-radius: 10px; background: #fff;
  transition: all 0.3s;
}
.step-card:hover { border-color: #409eff; box-shadow: 0 2px 12px rgba(64,158,255,0.15); }
.step-card.step-active { border-color: #409eff; background: #ecf5ff; }

.step-badge {
  flex-shrink: 0;
  width: 36px; height: 36px; border-radius: 50%;
  background: linear-gradient(135deg, #409eff, #67c23a);
  color: #fff; font-weight: 700; font-size: 16px;
  display: flex; align-items: center; justify-content: center;
}

.step-body { flex: 1; min-width: 0; }
.step-header { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.step-name { font-weight: 600; font-size: 15px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.step-meta { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #909399; }

.step-actions { display: flex; flex-direction: row; gap: 2px; flex-shrink: 0; }
</style>
