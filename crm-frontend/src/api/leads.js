import request from '@/utils/request'

export function getLeadPage(params) {
  return request({ url: '/leads/lead/page', method: 'get', params })
}

export function getLeadById(id) {
  return request({ url: `/leads/lead/${id}`, method: 'get' })
}

export function addLead(data) {
  return request({ url: '/leads/lead', method: 'post', data })
}

export function updateLead(id, data) {
  return request({ url: `/leads/lead/${id}`, method: 'put', data })
}

export function deleteLead(id) {
  return request({ url: `/leads/lead/${id}`, method: 'delete' })
}

// 线索评分
export function scoreLead(id) {
  return request({ url: `/leads/lead/${id}/score`, method: 'post' })
}

// 线索转化为商机
export function convertLead(id) {
  return request({ url: `/leads/lead/${id}/convert`, method: 'post' })
}
