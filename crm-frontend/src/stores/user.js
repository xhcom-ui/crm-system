import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi, getUserInfo } from '@/api/auth'

/**
 * 用户状态管理
 */
export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)
  const roles = ref([])
  const permissions = ref([])

  function setToken(val) {
    token.value = val
    localStorage.setItem('token', val)
  }

  /**
   * 是否拥有某个权限
   */
  function hasPermission(perm) {
    return permissions.value.includes(perm) || roles.value.includes('admin')
  }

  /**
   * 登录
   */
  async function login(username, password) {
    const res = await loginApi(username, password)
    const newToken = res.data.token
    setToken(newToken)
    return newToken
  }

  /**
   * 获取用户信息（含角色与权限）
   */
  async function fetchUserInfo() {
    const res = await getUserInfo()
    // 新版返回 { user, roles, permissions }
    const data = res.data
    userInfo.value = data.user || data // 兼容旧版
    roles.value = data.roles || []
    permissions.value = data.permissions || []
    return userInfo.value
  }

  /**
   * 登出
   */
  async function logout() {
    try {
      await logoutApi()
    } catch (e) {
      // 忽略登出接口错误
    }
    token.value = ''
    userInfo.value = null
    roles.value = []
    permissions.value = []
    localStorage.removeItem('token')
  }

  return { token, userInfo, roles, permissions, login, fetchUserInfo, logout, setToken, hasPermission }
})
