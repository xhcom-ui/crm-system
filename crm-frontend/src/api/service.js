import request from '@/utils/request'

export function getTicketPage(params) {
  return request({ url: '/service/ticket/page', method: 'get', params })
}

export function getTicketById(id) {
  return request({ url: `/service/ticket/${id}`, method: 'get' })
}

export function getTicketDetail(id) {
  return request({ url: `/service/ticket/${id}/detail`, method: 'get' })
}

export function addTicket(data) {
  return request({ url: '/service/ticket', method: 'post', data })
}

export function updateTicket(id, data) {
  return request({ url: `/service/ticket/${id}`, method: 'put', data })
}

export function deleteTicket(id) {
  return request({ url: `/service/ticket/${id}`, method: 'delete' })
}

// 自动分配工单
export function assignTicket(id) {
  return request({ url: `/service/ticket/${id}/assign`, method: 'post' })
}

// 处理人负载统计
export function getAssigneeLoad() {
  return request({ url: '/service/ticket/assignee-load', method: 'get' })
}

export function getKnowledgeOverview() {
  return request({ url: '/service/knowledge/overview', method: 'get' })
}

export function addKnowledgeArticle(data) {
  return request({ url: '/service/knowledge/articles', method: 'post', data })
}
