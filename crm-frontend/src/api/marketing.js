import request from '@/utils/request'

export function getCampaignPage(params) {
  return request({ url: '/marketing/campaign/page', method: 'get', params })
}

export function getCampaignById(id) {
  return request({ url: `/marketing/campaign/${id}`, method: 'get' })
}

export function addCampaign(data) {
  return request({ url: '/marketing/campaign', method: 'post', data })
}

export function updateCampaign(id, data) {
  return request({ url: `/marketing/campaign/${id}`, method: 'put', data })
}

export function deleteCampaign(id) {
  return request({ url: `/marketing/campaign/${id}`, method: 'delete' })
}

// 营销活动统计
export function getCampaignStats() {
  return request({ url: '/marketing/campaign/stats', method: 'get' })
}

export function getCampaignReport() {
  return request({ url: '/marketing/campaign/report', method: 'get' })
}

export function addCampaignPerformance(data) {
  return request({ url: '/marketing/campaign/performance', method: 'post', data })
}

// 根据客户标签匹配活动
export function matchCampaigns(tags) {
  return request({ url: '/marketing/campaign/match', method: 'post', params: { tags } })
}
