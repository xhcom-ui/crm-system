<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" @click="openDialog()" v-if="userStore.hasPermission('system:role:add')">新增角色</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleKey" label="角色标识" width="150" />
        <el-table-column prop="roleSort" label="排序" width="70" />
        <el-table-column label="数据范围" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ scopeMap[row.dataScope] || '未知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status===1?'success':'danger'" size="small">
              {{ row.status===1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="150" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)" v-if="userStore.hasPermission('system:role:edit')">编辑</el-button>
            <el-button size="small" type="warning" @click="assignMenus(row)" v-if="userStore.hasPermission('system:role:edit')">分配权限</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)" v-if="userStore.hasPermission('system:role:del')">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="current" :page-size="size" :total="total"
        layout="total, prev, pager, next" @current-change="fetchData"
        style="margin-top:16px; justify-content:flex-end"
      />
    </el-card>

    <!-- 新增/编辑 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="角色名称"><el-input v-model="form.roleName" /></el-form-item>
        <el-form-item label="角色标识"><el-input v-model="form.roleKey" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.roleSort" :min="0" /></el-form-item>
        <el-form-item label="数据范围">
          <el-select v-model="form.dataScope">
            <el-option v-for="(v,k) in scopeMap" :key="Number(k)" :label="v" :value="Number(k)" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限 -->
    <el-dialog v-model="menuDialogVisible" title="分配菜单权限" width="450px">
      <el-tree
        ref="menuTreeRef"
        :data="menuTree"
        show-checkbox
        node-key="id"
        :default-checked-keys="checkedMenuIds"
        :props="{ label: 'menuName', children: 'children' }"
      />
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveMenus">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getRoleList, addRole, updateRole, deleteRole, getRoleMenuIds, setRoleMenus, getMenuTree } from '@/api/system'

const userStore = useUserStore()

const loading = ref(false)
const tableData = ref([])
const current = ref(1); const size = ref(10); const total = ref(0)

const scopeMap = { 1: '全部', 2: '自定义', 3: '本部门', 4: '本部门及以下', 5: '仅本人' }

const dialogVisible = ref(false); const isEdit = ref(false)
const form = ref({ roleName:'', roleKey:'', roleSort:0, dataScope:1, status:1, description:'' })

const menuDialogVisible = ref(false); const currentRoleId = ref(null)
const menuTree = ref([]); const checkedMenuIds = ref([]); const menuTreeRef = ref(null)

function fetchData() {
  loading.value = true
  getRoleList({ current: current.value, size: size.value }).then(res => {
    tableData.value = res.data.records
    total.value = res.data.total
  }).finally(() => loading.value = false)
}

function openDialog(row) {
  if (row) {
    isEdit.value = true
    form.value = { ...row }
  } else {
    isEdit.value = false
    form.value = { roleName:'', roleKey:'', roleSort:0, dataScope:1, status:1, description:'' }
  }
  dialogVisible.value = true
}

function handleSave() {
  if (isEdit.value) {
    updateRole(form.value.id, form.value).then(() => {
      ElMessage.success('更新成功'); dialogVisible.value = false; fetchData()
    })
  } else {
    addRole(form.value).then(() => {
      ElMessage.success('新增成功'); dialogVisible.value = false; fetchData()
    })
  }
}

function handleDelete(id) {
  ElMessageBox.confirm('确认删除该角色？', '提示', { type: 'warning' }).then(() => {
    deleteRole(id).then(() => { ElMessage.success('删除成功'); fetchData() })
  }).catch(() => {})
}

function assignMenus(row) {
  currentRoleId.value = row.id
  Promise.all([getMenuTree(), getRoleMenuIds(row.id)]).then(([treeRes, idsRes]) => {
    menuTree.value = treeRes.data
    checkedMenuIds.value = idsRes.data
    menuDialogVisible.value = true
  })
}

function handleSaveMenus() {
  const keys = menuTreeRef.value.getCheckedKeys()
  const halfKeys = menuTreeRef.value.getHalfCheckedKeys()
  const allIds = [...keys, ...halfKeys]
  setRoleMenus(currentRoleId.value, allIds).then(() => {
    ElMessage.success('权限分配成功'); menuDialogVisible.value = false
  })
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
