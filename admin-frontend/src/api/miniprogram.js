import request from '@/utils/request'

/**
 * 获取小程序配置列表
 */
export function getConfigList(params) {
  return request({
    url: '/admin/miniprogram/config',
    method: 'get',
    params,
  })
}

/**
 * 根据配置键获取配置
 */
export function getConfigByKey(configKey) {
  return request({
    url: `/admin/miniprogram/config/${configKey}`,
    method: 'get',
  })
}

/**
 * 根据配置类型获取配置列表
 */
export function getConfigByType(configType, params) {
  return request({
    url: `/admin/miniprogram/config/type/${configType}`,
    method: 'get',
    params,
  })
}

/**
 * 创建小程序配置
 */
export function createConfig(data) {
  return request({
    url: '/admin/miniprogram/config',
    method: 'post',
    data,
  })
}

/**
 * 更新小程序配置
 */
export function updateConfig(id, data) {
  return request({
    url: `/admin/miniprogram/config/${id}`,
    method: 'put',
    data,
  })
}

/**
 * 根据配置键更新配置
 */
export function updateConfigByKey(configKey, data) {
  return request({
    url: `/admin/miniprogram/config/key/${configKey}`,
    method: 'put',
    data,
  })
}

/**
 * 删除小程序配置
 */
export function deleteConfig(id) {
  return request({
    url: `/admin/miniprogram/config/${id}`,
    method: 'delete',
  })
}

/**
 * 根据配置键删除配置
 */
export function deleteConfigByKey(configKey) {
  return request({
    url: `/admin/miniprogram/config/key/${configKey}`,
    method: 'delete',
  })
}

/**
 * 获取小程序统计数据
 */
export function getStatistics(params) {
  return request({
    url: '/admin/miniprogram/statistics',
    method: 'get',
    params,
  })
}
