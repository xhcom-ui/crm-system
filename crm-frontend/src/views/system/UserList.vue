<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="openDialog()" v-if="userStore.hasPermission('system:user:add')">新增用户</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status===1?'success':'danger'" size="small">
              {{ row.status===1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)" v-if="userStore.hasPermission('system:user:edit')">编辑</el-button>
            <el-button size="small" type="warning" @click="assignRoles(row)" v-if="userStore.hasPermission('system:user:edit')">分配角色</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)" v-if="userStore.hasPermission('system:user:del')">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="current" :page-size="size"
        :total="total" layout="total, prev, pager, next"
        @current-change="fetchData" style="margin-top:16px; justify-content:flex-end"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" :placeholder="isEdit?'不填则不修改':''" show-password />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="正常" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 分配角色对话框 -->
    <el-dialog v-model="roleDialogVisible" title="分配角色" width="450px">
      <el-checkbox-group v-model="selectedRoleIds">
        <el-checkbox v-for="r in allRoles" :key="r.id" :label="r.id" :value="r.id">
          {{ r.roleName }} <span style="color:#999;font-size:12px">{{ r.roleKey }}</span>
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveRoles">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  getUserList, addUser, updateUser, deleteUser,
  getUserRoleIds, setUserRoles, getAllRoles
} from '@/api/system'

const userStore = useUserStore()

const loading = ref(false)
const tableData = ref([])
const current = ref(1)
const size = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({ username: '', password: '', realName: '', email: '', phone: '', status: 1 })

const roleDialogVisible = ref(false)
const currentUserId = ref(null)
const selectedRoleIds = ref([])
const allRoles = ref([])

function fetchData() {
  loading.value = true
  getUserList({ current: current.value, size: size.value }).then(res => {
    tableData.value = res.data.records
    total.value = res.data.total
  }).finally(() => loading.value = false)
}

function openDialog(row) {
  if (row) {
    isEdit.value = true
    form.value = { ...row, password: '' }
  } else {
    isEdit.value = false
    form.value = { username: '', password: '', realName: '', email: '', phone: '', status: 1 }
  }
  dialogVisible.value = true
}

function handleSave() {
  if (isEdit.value) {
    updateUser(form.value.id, form.value).then(() => {
      ElMessage.success('更新成功')
      dialogVisible.value = false
      fetchData()
    })
  } else {
    if (!form.value.password) {
      ElMessage.warning('请输入密码')
      return
    }
    addUser(form.value).then(() => {
      ElMessage.success('新增成功')
      dialogVisible.value = false
      fetchData()
    })
  }
}

function handleDelete(id) {
  ElMessageBox.confirm('确认删除该用户？', '提示', { type: 'warning' }).then(() => {
    deleteUser(id).then(() => {
      ElMessage.success('删除成功')
      fetchData()
    })
  }).catch(() => {})
}

function assignRoles(row) {
  currentUserId.value = row.id
  Promise.all([
    getAllRoles(),
    getUserRoleIds(row.id)
  ]).then(([rolesRes, roleIdsRes]) => {
    allRoles.value = rolesRes.data
    selectedRoleIds.value = roleIdsRes.data
    roleDialogVisible.value = true
  })
}

function handleSaveRoles() {
  setUserRoles(currentUserId.value, selectedRoleIds.value).then(() => {
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
  })
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
