import request from '@/utils/request'

export function getSalesOrderPage(params) {
  return request({ url: '/sales/order/page', method: 'get', params })
}

export function addSalesOrder(data) {
  return request({ url: '/sales/order', method: 'post', data })
}

export function getSalesOrderDetail(id) {
  return request({ url: `/sales/order/${id}`, method: 'get' })
}

export function importSalesOrders(file) {
  const data = new FormData()
  data.append('file', file)
  return request({
    url: '/sales/order/import',
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 60000,
  })
}

export function getSupportedPlatforms() {
  return request({ url: '/sales/platform/supported', method: 'get' })
}

export function getPlatformConfigs(params) {
  return request({ url: '/sales/platform/configs', method: 'get', params })
}

export function addPlatformConfig(data) {
  return request({ url: '/sales/platform/config', method: 'post', data })
}

export function updatePlatformConfig(id, data) {
  return request({ url: `/sales/platform/config/${id}`, method: 'put', data })
}

export function syncPlatformData(id, syncType = 'ALL') {
  return request({ url: `/sales/platform/config/${id}/sync`, method: 'post', data: { syncType }, timeout: 60000 })
}

export function getPlatformRefunds(params) {
  return request({ url: '/sales/platform/refunds', method: 'get', params })
}

export function getPlatformSyncLogs(params) {
  return request({ url: '/sales/platform/sync-logs', method: 'get', params })
}
