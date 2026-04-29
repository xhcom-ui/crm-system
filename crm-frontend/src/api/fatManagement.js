import request from '@/utils/request'

export function getFatOverview() {
  return request({ url: '/customer/fat-management/overview', method: 'get' })
}

export function getFatRecordPage(params) {
  return request({ url: '/customer/fat-management/page', method: 'get', params })
}

export function addFatRecord(data) {
  return request({ url: '/customer/fat-management', method: 'post', data })
}

export function updateFatRecord(id, data) {
  return request({ url: `/customer/fat-management/${id}`, method: 'put', data })
}

export function addCustomerInfoItem(moduleType, data) {
  return request({ url: `/customer/fat-management/module/${moduleType}`, method: 'post', data })
}

export function updateCustomerInfoItem(moduleType, id, data) {
  return request({ url: `/customer/fat-management/module/${moduleType}/${id}`, method: 'put', data })
}

export function deleteCustomerInfoItem(moduleType, id) {
  return request({ url: `/customer/fat-management/module/${moduleType}/${id}`, method: 'delete' })
}
