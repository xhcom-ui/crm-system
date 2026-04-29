<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>部门管理</span>
          <el-button type="primary" @click="openDialog()" v-if="userStore.hasPermission('system:dept:add')">新增根部门</el-button>
        </div>
      </template>

      <el-table
        :data="treeData"
        v-loading="loading"
        row-key="id"
        stripe
        default-expand-all
        class="tree-table dept-tree-table"
        :tree-props="{ children: 'children' }"
        :row-class-name="treeRowClassName"
      >
        <el-table-column prop="deptName" label="部门名称" min-width="260">
          <template #default="{ row }">
            <div class="tree-name-cell" :class="{ 'is-leaf': !row.children?.length }">
              <span class="tree-node-badge">{{ row.children?.length ? '部门' : '子级' }}</span>
              <span class="tree-node-title">{{ row.deptName }}</span>
              <span v-if="row.children?.length" class="tree-node-count">{{ row.children.length }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="orderNum" label="排序" width="80" />
        <el-table-column prop="leader" label="负责人" width="120" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status===1?'success':'danger'" size="small">
              {{ row.status===1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <div class="action-group">
              <el-button size="small" @click="openDialog(row)" v-if="userStore.hasPermission('system:dept:edit')">编辑</el-button>
              <el-button size="small" type="success" @click="openDialog(null, row.id)" v-if="userStore.hasPermission('system:dept:add')">添加子级</el-button>
              <el-button size="small" type="danger" @click="handleDelete(row.id)" v-if="userStore.hasPermission('system:dept:del')">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑部门' : '新增部门'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="上级部门">
          <el-input-number v-model="form.parentId" :min="0" />
        </el-form-item>
        <el-form-item label="部门名称"><el-input v-model="form.deptName" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.orderNum" :min="0" /></el-form-item>
        <el-form-item label="负责人"><el-input v-model="form.leader" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getDeptTree, addDept, updateDept, deleteDept } from '@/api/system'

const userStore = useUserStore()

const loading = ref(false)
const treeData = ref([])

const dialogVisible = ref(false); const isEdit = ref(false)
const form = ref({ parentId:0, deptName:'', orderNum:0, leader:'', phone:'', email:'', status:1 })

function fetchData() {
  loading.value = true
  getDeptTree().then(res => {
    treeData.value = res.data
  }).finally(() => loading.value = false)
}

function openDialog(row, forceParentId) {
  if (row) {
    isEdit.value = true
    form.value = { ...row }
  } else {
    isEdit.value = false
    form.value = { parentId: forceParentId || 0, deptName:'', orderNum:0, leader:'', phone:'', email:'', status:1 }
  }
  dialogVisible.value = true
}

function handleSave() {
  if (isEdit.value) {
    updateDept(form.value.id, form.value).then(() => {
      ElMessage.success('更新成功'); dialogVisible.value = false; fetchData()
    })
  } else {
    addDept(form.value).then(() => {
      ElMessage.success('新增成功'); dialogVisible.value = false; fetchData()
    })
  }
}

function handleDelete(id) {
  ElMessageBox.confirm('确认删除？将同步删除子部门', '提示', { type: 'warning' }).then(() => {
    deleteDept(id).then(() => { ElMessage.success('删除成功'); fetchData() })
  }).catch(() => {})
}

function treeRowClassName({ row, rowIndex }) {
  const level = Number(row._level || row.level || 0)
  return `tree-row tree-row-level-${Math.min(level, 4)} ${row.children?.length ? 'tree-row-parent' : 'tree-row-leaf'} ${rowIndex % 2 ? 'is-odd' : ''}`
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }

.tree-name-cell {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 28px;
}

.tree-name-cell.is-leaf::before {
  content: '';
  width: 18px;
  height: 1px;
  background: #d8dee8;
  margin-right: 2px;
}

.tree-node-title {
  font-weight: 500;
  color: #2f3a4a;
}

.tree-node-badge {
  display: inline-flex;
  align-items: center;
  height: 20px;
  padding: 0 7px;
  border-radius: 999px;
  background: #eef4ff;
  color: #2563eb;
  font-size: 12px;
}

.tree-node-count {
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 999px;
  background: #f1f5f9;
  color: #64748b;
  font-size: 12px;
  line-height: 20px;
  text-align: center;
}

.action-group {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
}

.action-group :deep(.el-button + .el-button) {
  margin-left: 0;
}

:deep(.tree-table .el-table__row.tree-row-leaf td:first-child) {
  background: linear-gradient(90deg, rgba(37, 99, 235, .04), transparent 52%);
}

:deep(.tree-table .el-table__row.tree-row-parent td:first-child) {
  font-weight: 600;
}

:deep(.tree-table .el-table__indent) {
  position: relative;
}

:deep(.tree-table .el-table__indent::after) {
  content: '';
  position: absolute;
  left: 50%;
  top: -22px;
  bottom: -22px;
  width: 1px;
  background: #d8dee8;
}

:deep(.tree-table .el-table__expand-icon) {
  width: 22px;
  height: 22px;
  margin-right: 4px;
  border-radius: 6px;
  color: #64748b;
}

:deep(.tree-table .el-table__expand-icon:hover) {
  background: #eef4ff;
  color: #2563eb;
}
</style>
