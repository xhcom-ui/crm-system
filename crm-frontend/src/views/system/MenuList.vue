<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>菜单管理</span>
          <el-button type="primary" @click="openDialog()" v-if="userStore.hasPermission('system:menu:add')">新增根菜单</el-button>
        </div>
      </template>

      <el-table
        :data="treeData"
        v-loading="loading"
        row-key="id"
        stripe
        default-expand-all
        class="tree-table menu-tree-table"
        :tree-props="{ children: 'children' }"
        :row-class-name="treeRowClassName"
      >
        <el-table-column prop="menuName" label="菜单名称" min-width="260">
          <template #default="{ row }">
            <div class="tree-name-cell" :class="[`type-${row.menuType}`, { 'is-leaf': !row.children?.length }]">
              <span class="tree-node-icon">{{ typeShort[row.menuType] }}</span>
              <span class="tree-node-title">{{ row.menuName }}</span>
              <span v-if="row.children?.length" class="tree-node-count">{{ row.children.length }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="menuType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="typeColor[row.menuType]" size="small">{{ typeLabel[row.menuType] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路径" min-width="150" show-overflow-tooltip />
        <el-table-column prop="component" label="组件" min-width="180" show-overflow-tooltip />
        <el-table-column prop="perms" label="权限标识" min-width="190" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag v-if="row.perms" class="perm-tag" effect="plain" type="info">{{ row.perms }}</el-tag>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="icon" label="图标" width="100" />
        <el-table-column prop="orderNum" label="排序" width="70" />
        <el-table-column label="状态" width="70">
          <template #default="{ row }">
            <el-tag :type="row.status===1?'success':'info'" size="small">
              {{ row.status===1 ? '显示' : '隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <div class="action-group">
              <el-button size="small" @click="openDialog(row)" v-if="userStore.hasPermission('system:menu:edit')">编辑</el-button>
              <el-button v-if="row.menuType!=='F' && userStore.hasPermission('system:menu:add')" size="small" type="success" @click="openDialog(null, row.id)">添加子级</el-button>
              <el-button size="small" type="danger" @click="handleDelete(row.id)" v-if="userStore.hasPermission('system:menu:del')">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="父级ID"><el-input-number v-model="form.parentId" :min="0" /></el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.menuType">
            <el-radio value="M">目录</el-radio>
            <el-radio value="C">菜单</el-radio>
            <el-radio value="F">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="名称"><el-input v-model="form.menuName" /></el-form-item>
        <el-form-item v-if="form.menuType==='M'||form.menuType==='C'" label="图标"><el-input v-model="form.icon" placeholder="Element Plus 图标名" /></el-form-item>
        <el-form-item v-if="form.menuType==='C'" label="路径"><el-input v-model="form.path" /></el-form-item>
        <el-form-item v-if="form.menuType==='C'" label="组件"><el-input v-model="form.component" /></el-form-item>
        <el-form-item v-if="form.menuType==='F'" label="权限标识"><el-input v-model="form.perms" placeholder="例如: system:user:add" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.orderNum" :min="0" /></el-form-item>
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
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getMenuTree, addMenu, updateMenu, deleteMenu } from '@/api/system'

const userStore = useUserStore()

const loading = ref(false)
const treeData = ref([])

const typeLabel = { M: '目录', C: '菜单', F: '按钮' }
const typeColor = { M: 'warning', C: 'primary', F: 'success' }
const typeShort = { M: '目', C: '菜', F: '钮' }

const dialogVisible = ref(false); const isEdit = ref(false)
const form = ref({ parentId:0, menuType:'M', menuName:'', icon:'', path:'', component:'', perms:'', orderNum:0, status:1 })

const dialogTitle = computed(() => isEdit.value ? '编辑菜单' : '新增菜单')

function fetchData() {
  loading.value = true
  getMenuTree().then(res => {
    treeData.value = res.data
  }).finally(() => loading.value = false)
}

function openDialog(row, forceParentId) {
  if (row) {
    isEdit.value = true
    form.value = { ...row }
  } else {
    isEdit.value = false
    form.value = { parentId: forceParentId || 0, menuType:'M', menuName:'', icon:'', path:'', component:'', perms:'', orderNum:0, status:1 }
  }
  dialogVisible.value = true
}

function handleSave() {
  if (isEdit.value) {
    updateMenu(form.value.id, form.value).then(() => {
      ElMessage.success('更新成功'); dialogVisible.value = false; fetchData()
    })
  } else {
    addMenu(form.value).then(() => {
      ElMessage.success('新增成功'); dialogVisible.value = false; fetchData()
    })
  }
}

function handleDelete(id) {
  ElMessageBox.confirm('确认删除？将同步删除子菜单', '提示', { type: 'warning' }).then(() => {
    deleteMenu(id).then(() => { ElMessage.success('删除成功'); fetchData() })
  }).catch(() => {})
}

function treeRowClassName({ row, rowIndex }) {
  const level = Number(row._level || row.level || 0)
  return `tree-row tree-row-level-${Math.min(level, 4)} tree-row-type-${row.menuType} ${row.children?.length ? 'tree-row-parent' : 'tree-row-leaf'} ${rowIndex % 2 ? 'is-odd' : ''}`
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }

.tree-name-cell {
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

.tree-node-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 7px;
  font-size: 12px;
  font-weight: 700;
  background: #fff7ed;
  color: #d97706;
}

.tree-name-cell.type-C .tree-node-icon {
  background: #eff6ff;
  color: #2563eb;
}

.tree-name-cell.type-F .tree-node-icon {
  background: #f0fdf4;
  color: #16a34a;
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

.perm-tag {
  max-width: 170px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.muted { color: #a8abb2; }

.action-group {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
}

.action-group :deep(.el-button + .el-button) {
  margin-left: 0;
}

:deep(.tree-table .el-table__row.tree-row-type-F td:first-child) {
  background: linear-gradient(90deg, rgba(22, 163, 74, .05), transparent 54%);
}

:deep(.tree-table .el-table__row.tree-row-type-C td:first-child) {
  background: linear-gradient(90deg, rgba(37, 99, 235, .04), transparent 54%);
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
