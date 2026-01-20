import request from '@/utils/request'

/**
 * 分页查询景点列表
 */
export function getAttractionList(params) {
  return request({
    url: '/admin/attractions',
    method: 'get',
    params,
  })
}

/**
 * 根据ID查询景点详情
 */
export function getAttractionById(id) {
  return request({
    url: `/admin/attractions/${id}`,
    method: 'get',
  })
}

/**
 * 创建景点
 */
export function createAttraction(data) {
  return request({
    url: '/admin/attractions',
    method: 'post',
    data,
  })
}

/**
 * 更新景点
 */
export function updateAttraction(id, data) {
  return request({
    url: `/admin/attractions/${id}`,
    method: 'put',
    data,
  })
}

/**
 * 删除景点（软删除）
 */
export function deleteAttraction(id) {
  return request({
    url: `/admin/attractions/${id}`,
    method: 'delete',
  })
}
