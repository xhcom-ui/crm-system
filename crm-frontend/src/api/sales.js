import request from '@/utils/request'

export function getOpportunityPage(params) {
  return request({ url: '/sales/opportunity/page', method: 'get', params })
}

export function getOpportunityById(id) {
  return request({ url: `/sales/opportunity/${id}`, method: 'get' })
}

export function addOpportunity(data) {
  return request({ url: '/sales/opportunity', method: 'post', data })
}

export function updateOpportunity(id, data) {
  return request({ url: `/sales/opportunity/${id}`, method: 'put', data })
}

export function deleteOpportunity(id) {
  return request({ url: `/sales/opportunity/${id}`, method: 'delete' })
}

// 销售漏斗数据
export function getSalesFunnel() {
  return request({ url: '/sales/opportunity/funnel', method: 'get' })
}

// 赢单率统计
export function getWinRate() {
  return request({ url: '/sales/opportunity/win-rate', method: 'get' })
}

// 商机详情（含关联客户信息）
export function getOpportunityDetail(id) {
  return request({ url: `/sales/opportunity/${id}/detail`, method: 'get' })
}
