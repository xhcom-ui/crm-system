import request from '@/utils/request'

// 客户联系人 API
export function getContactPage(params) {
  return request({ url: '/customer/contact/page', method: 'get', params })
}

export function getContactById(id) {
  return request({ url: `/customer/contact/${id}`, method: 'get' })
}

export function getCustomerInsight(id) {
  return request({ url: `/customer/insight/${id}`, method: 'get' })
}

export function addCustomerInteraction(data) {
  return request({ url: '/customer/interaction', method: 'post', data })
}

export function addContact(data) {
  return request({ url: '/customer/contact', method: 'post', data })
}

export function updateContact(id, data) {
  return request({ url: `/customer/contact/${id}`, method: 'put', data })
}

export function deleteContact(id) {
  return request({ url: `/customer/contact/${id}`, method: 'delete' })
}
