import axios from 'axios'
import { ElMessage } from 'element-plus'

let redirectingToLogin = false

function isLoginRequest(config = {}) {
  return String(config.url || '').includes('/auth/login')
}

function redirectToLogin(message) {
  if (redirectingToLogin || window.location.hash === '#/login') return

  redirectingToLogin = true
  localStorage.removeItem('token')
  sessionStorage.removeItem('token')
  if (message) ElMessage.error(message)

  const current = window.location.hash.replace(/^#/, '') || '/'
  const redirect = current && current !== '/login' ? `?redirect=${encodeURIComponent(current)}` : ''
  window.location.hash = `#/login${redirect}`

  setTimeout(() => {
    redirectingToLogin = false
  }, 800)
}

// 创建 Axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 请求拦截器 - Token 注入
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token')
    if (token) {
      config.headers.Authorization = token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 统一错误处理
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      const message = res.message || '请求失败，请重新登录'
      if (!isLoginRequest(response.config)) {
        redirectToLogin(message)
      } else {
        ElMessage.error(message)
      }
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '接口请求异常，请重新登录'
    if (!isLoginRequest(error.config)) {
      redirectToLogin(message)
    } else {
      ElMessage.error(message)
    }
    return Promise.reject(error)
  }
)

export default request
