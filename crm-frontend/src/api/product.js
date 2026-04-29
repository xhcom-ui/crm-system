import request from '@/utils/request'

export function getProductPage(params) {
  return request({ url: '/sales/product/page', method: 'get', params })
}

export function addProduct(data) {
  return request({ url: '/sales/product', method: 'post', data })
}
