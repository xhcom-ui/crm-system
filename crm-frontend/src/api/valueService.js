import request from '@/utils/request'

export function getValueServicePage(params) {
  return request({ url: '/service/value-added/page', method: 'get', params })
}

export function getValueServiceSummary() {
  return request({ url: '/service/value-added/summary', method: 'get' })
}

export function addValueService(data) {
  return request({ url: '/service/value-added', method: 'post', data })
}
