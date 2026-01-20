import request from '@/utils/request'

/**
 * 获取首页统计数据
 */
export function getDashboardStats() {
  return request({
    url: '/admin/dashboard/stats',
    method: 'get',
  })
}

/**
 * 获取订单趋势数据
 */
export function getOrderTrend(params) {
  return request({
    url: '/admin/dashboard/order-trend',
    method: 'get',
    params,
  })
}

/**
 * 获取销售统计数据
 */
export function getSalesStats(params) {
  return request({
    url: '/admin/dashboard/sales-stats',
    method: 'get',
    params,
  })
}

/**
 * 清除所有缓存
 */
export function clearAllCache() {
  return request({
    url: '/admin/dashboard/cache/clear-all',
    method: 'post',
  })
}

/**
 * 清除指定类型的缓存
 */
export function clearCache(cacheType) {
  return request({
    url: '/admin/dashboard/cache/clear',
    method: 'post',
    params: { cacheType },
  })
}

/**
 * 获取缓存统计信息
 */
export function getCacheStats() {
  return request({
    url: '/admin/dashboard/cache/stats',
    method: 'get',
  })
}
