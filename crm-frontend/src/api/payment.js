import request from '@/utils/request'

export function getPaymentOverview() {
  return request({ url: '/sales/payment/overview', method: 'get' })
}

export function getPaymentOrders(params) {
  return request({ url: '/sales/payment/orders', method: 'get', params })
}

export function getPaymentRefunds(params) {
  return request({ url: '/sales/payment/refunds', method: 'get', params })
}

export function getPaymentChannels() {
  return request({ url: '/sales/payment/channels', method: 'get' })
}

export function unifiedOrder(data) {
  return request({ url: '/sales/payment/unified-order', method: 'post', data })
}

export function refundPayment(data) {
  return request({ url: '/sales/payment/refund', method: 'post', data })
}
