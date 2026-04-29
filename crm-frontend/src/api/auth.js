import request from '@/utils/request'

/**
 * 登录
 */
export function login(username, password) {
  return request({
    url: '/auth/login',
    method: 'post',
    data: { username, password },
  })
}

/**
 * 登出
 */
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post',
  })
}

/**
 * 获取当前用户信息
 */
export function getUserInfo() {
  return request({
    url: '/auth/user-info',
    method: 'get',
  })
}

/**
 * 获取当前用户的路由菜单
 */
export function getUserRoutes() {
  return request({
    url: '/auth/routes',
    method: 'get',
  })
}
