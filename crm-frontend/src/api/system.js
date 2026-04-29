import request from '@/utils/request'

/**
 * 系统管理 API - 用户/角色/菜单/部门
 */

// ==================== 用户管理 ====================

export function getUserList(params) {
  return request({ url: '/system/user', method: 'get', params })
}

export function getUserById(id) {
  return request({ url: `/system/user/${id}`, method: 'get' })
}

export function addUser(data) {
  return request({ url: '/system/user', method: 'post', data })
}

export function updateUser(id, data) {
  return request({ url: `/system/user/${id}`, method: 'put', data })
}

export function deleteUser(id) {
  return request({ url: `/system/user/${id}`, method: 'delete' })
}

export function getUserRoleIds(id) {
  return request({ url: `/system/user/${id}/roles`, method: 'get' })
}

export function setUserRoles(id, roleIds) {
  return request({ url: `/system/user/${id}/roles`, method: 'put', data: { roleIds } })
}

// ==================== 角色管理 ====================

export function getRoleList(params) {
  return request({ url: '/system/role', method: 'get', params })
}

export function getAllRoles() {
  return request({ url: '/system/role/all', method: 'get' })
}

export function getRoleById(id) {
  return request({ url: `/system/role/${id}`, method: 'get' })
}

export function addRole(data) {
  return request({ url: '/system/role', method: 'post', data })
}

export function updateRole(id, data) {
  return request({ url: `/system/role/${id}`, method: 'put', data })
}

export function deleteRole(id) {
  return request({ url: `/system/role/${id}`, method: 'delete' })
}

export function getRoleMenuIds(id) {
  return request({ url: `/system/role/${id}/menus`, method: 'get' })
}

export function setRoleMenus(id, menuIds) {
  return request({ url: `/system/role/${id}/menus`, method: 'put', data: { menuIds } })
}

// ==================== 菜单管理 ====================

export function getMenuTree() {
  return request({ url: '/system/menu/tree', method: 'get' })
}

export function getMenuById(id) {
  return request({ url: `/system/menu/${id}`, method: 'get' })
}

export function addMenu(data) {
  return request({ url: '/system/menu', method: 'post', data })
}

export function updateMenu(id, data) {
  return request({ url: `/system/menu/${id}`, method: 'put', data })
}

export function deleteMenu(id) {
  return request({ url: `/system/menu/${id}`, method: 'delete' })
}

// ==================== 部门管理 ====================

export function getDeptTree() {
  return request({ url: '/system/dept/tree', method: 'get' })
}

export function getDeptById(id) {
  return request({ url: `/system/dept/${id}`, method: 'get' })
}

export function addDept(data) {
  return request({ url: '/system/dept', method: 'post', data })
}

export function updateDept(id, data) {
  return request({ url: `/system/dept/${id}`, method: 'put', data })
}

export function deleteDept(id) {
  return request({ url: `/system/dept/${id}`, method: 'delete' })
}

// ==================== 路由菜单 ====================

export function getRoutes() {
  return request({ url: '/auth/routes', method: 'get' })
}

// ==================== 密码修改 ====================

export function updatePassword(data) {
  return request({ url: '/auth/password', method: 'put', data })
}

// ==================== 操作日志 ====================

export function getOperLogPage(params) {
  return request({ url: '/system/oper-log/page', method: 'get', params })
}

export function cleanOperLog() {
  return request({ url: '/system/oper-log/clean', method: 'delete' })
}

// ==================== 通知管理 ====================

export function sendNotification(data) {
  return request({ url: '/system/notification/send', method: 'post', data })
}

export function getNotificationPage(params) {
  return request({ url: '/system/notification/page', method: 'get', params })
}

export function getNotificationChannels() {
  return request({ url: '/system/notification/channels', method: 'get' })
}

export function updateNotificationChannel(id, data) {
  return request({ url: `/system/notification/channels/${id}`, method: 'put', data })
}

export function getNotificationPushLogs(id) {
  return request({ url: `/system/notification/${id}/push-logs`, method: 'get' })
}
