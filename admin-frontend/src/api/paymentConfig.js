import request from '@/utils/request'

/**
 * 获取启用的支付配置
 */
export function getPaymentConfig() {
  return request({
    url: '/admin/payment-config',
    method: 'get',
  })
}

/**
 * 根据ID获取支付配置
 */
export function getPaymentConfigById(id) {
  return request({
    url: `/admin/payment-config/${id}`,
    method: 'get',
  })
}

/**
 * 创建支付配置
 */
export function createPaymentConfig(data) {
  return request({
    url: '/admin/payment-config',
    method: 'post',
    data,
  })
}

/**
 * 更新支付配置
 */
export function updatePaymentConfig(id, data) {
  return request({
    url: `/admin/payment-config/${id}`,
    method: 'put',
    data,
  })
}

/**
 * 更新支付配置状态
 */
export function updatePaymentConfigStatus(id, status) {
  return request({
    url: `/admin/payment-config/${id}/status`,
    method: 'put',
    params: { status },
  })
}
