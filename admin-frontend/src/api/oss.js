import request from '@/utils/request'

/**
 * 获取OSS配置
 */
export function getOssConfig() {
  return request({
    url: '/admin/oss-config',
    method: 'get',
  })
}

/**
 * 保存或更新OSS配置
 */
export function saveOrUpdateOssConfig(data) {
  return request({
    url: '/admin/oss-config',
    method: 'post',
    data,
  })
}

/**
 * 测试OSS连接
 */
export function testOssConnection(data) {
  return request({
    url: '/admin/oss-config/test',
    method: 'post',
    data,
  })
}

/**
 * 删除OSS配置
 */
export function deleteOssConfig() {
  return request({
    url: '/admin/oss-config',
    method: 'delete',
  })
}
