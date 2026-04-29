import request from '@/utils/request'

export function getStatsReport() {
  return request({ url: '/sales/stats/report', method: 'get' })
}
