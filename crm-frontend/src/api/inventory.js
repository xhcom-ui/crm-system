import request from '@/utils/request'

export function getInventoryOverview() {
  return request({ url: '/sales/inventory/overview', method: 'get' })
}

export function getInventoryStocks(params) {
  return request({ url: '/sales/inventory/stocks', method: 'get', params })
}

export function getPreviousInventory(params) {
  return request({ url: '/sales/inventory/previous', method: 'get', params })
}

export function getInventoryMovements(params) {
  return request({ url: '/sales/inventory/movements', method: 'get', params })
}

export function getPurchasingSalesStock(params) {
  return request({ url: '/sales/inventory/purchasing-sales-stock', method: 'get', params })
}

export function addInventoryMovement(data) {
  return request({ url: '/sales/inventory/movement', method: 'post', data })
}
