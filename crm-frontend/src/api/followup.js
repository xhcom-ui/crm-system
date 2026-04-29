import request from '@/utils/request'

export function getFollowupPage(params) {
  return request({ url: '/customer/followup/page', method: 'get', params })
}

export function addFollowup(data) {
  return request({ url: '/customer/followup', method: 'post', data })
}
