import request from '@/utils/request'

/**
 * 获取商家配置
 */
export function getMerchantConfig() {
  return request({
    url: '/admin/merchant-config',
    method: 'get',
  })
}

/**
 * 创建商家配置
 */
export function createMerchantConfig(data) {
  return request({
    url: '/admin/merchant-config',
    method: 'post',
    data,
  })
}

/**
 * 更新商家配置
 */
export function updateMerchantConfig(id, data) {
  return request({
    url: `/admin/merchant-config/${id}`,
    method: 'put',
    data,
  })
}

/**
 * 更新商家配置（使用现有配置ID）
 */
export function updateMerchantConfigDirect(data) {
  return request({
    url: '/admin/merchant-config',
    method: 'put',
    data,
  })
}
