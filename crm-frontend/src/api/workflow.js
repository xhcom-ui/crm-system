import request from '@/utils/request'

export function getWorkflowPage(params) {
  return request({ url: '/workflow/workflow/page', method: 'get', params })
}

export function getWorkflowById(id) {
  return request({ url: `/workflow/workflow/${id}`, method: 'get' })
}

export function addWorkflow(data) {
  return request({ url: '/workflow/workflow', method: 'post', data })
}

export function updateWorkflow(id, data) {
  return request({ url: `/workflow/workflow/${id}`, method: 'put', data })
}

export function deleteWorkflow(id) {
  return request({ url: `/workflow/workflow/${id}`, method: 'delete' })
}

// 触发工作流
export function triggerWorkflow(id) {
  return request({ url: `/workflow/workflow/${id}/trigger`, method: 'post' })
}

// 查询执行实例列表
export function getWorkflowInstances(params) {
  return request({ url: '/workflow/workflow/instances', method: 'get', params })
}

// 查询单个实例详情
export function getWorkflowInstanceById(id) {
  return request({ url: `/workflow/workflow/instances/${id}`, method: 'get' })
}

export function getWorkflowInstanceTrace(id) {
  return request({ url: `/workflow/workflow/instances/${id}/trace`, method: 'get' })
}

export function approveWorkflowInstance(id, data) {
  return request({ url: `/workflow/workflow/instances/${id}/approve`, method: 'post', data })
}

export function rejectWorkflowInstance(id, data) {
  return request({ url: `/workflow/workflow/instances/${id}/reject`, method: 'post', data })
}

export function transferWorkflowInstance(id, data) {
  return request({ url: `/workflow/workflow/instances/${id}/transfer`, method: 'post', data })
}

export function retryWorkflowInstance(id, data) {
  return request({ url: `/workflow/workflow/instances/${id}/retry`, method: 'post', data })
}

// ========== 工作流节点 API ==========

// 获取工作流节点列表
export function getWorkflowNodes(workflowId) {
  return request({ url: `/workflow/node/list/${workflowId}`, method: 'get' })
}

// 新增节点
export function addWorkflowNode(data) {
  return request({ url: '/workflow/node', method: 'post', data })
}

// 更新节点
export function updateWorkflowNode(id, data) {
  return request({ url: `/workflow/node/${id}`, method: 'put', data })
}

// 删除节点
export function deleteWorkflowNode(id) {
  return request({ url: `/workflow/node/${id}`, method: 'delete' })
}

// 调整节点顺序
export function reorderWorkflowNode(id, direction) {
  return request({ url: `/workflow/node/${id}/reorder`, method: 'put', params: { direction } })
}
