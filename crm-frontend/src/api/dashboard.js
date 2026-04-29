import request from '@/utils/request'

// 销售漏斗数据
export function getSalesFunnel() {
  return request({ url: '/sales/opportunity/funnel', method: 'get' })
}

// 赢单率
export function getWinRate() {
  return request({ url: '/sales/opportunity/win-rate', method: 'get' })
}

// 营销活动统计
export function getCampaignStats() {
  return request({ url: '/marketing/campaign/stats', method: 'get' })
}

// 客户总数（通过分页接口获取 total）
export function getCustomerTotal() {
  return request({ url: '/customer/contact/page', method: 'get', params: { current: 1, size: 1 } })
}

// 线索总数
export function getLeadTotal() {
  return request({ url: '/leads/lead/page', method: 'get', params: { current: 1, size: 1 } })
}

// 待处理工单总数
export function getTicketTotal() {
  return request({ url: '/service/ticket/page', method: 'get', params: { current: 1, size: 1 } })
}
