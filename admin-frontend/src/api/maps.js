import request from '@/utils/request'

/**
 * 查询地图列表
 */
export function getMapList(params) {
  return request({
    url: '/admin/maps',
    method: 'get',
    params,
  })
}

/**
 * 根据ID查询地图详情
 */
export function getMapById(id) {
  return request({
    url: `/admin/maps/${id}`,
    method: 'get',
  })
}

/**
 * 创建地图
 */
export function createMap(data) {
  return request({
    url: '/admin/maps',
    method: 'post',
    data,
  })
}

/**
 * 更新地图
 */
export function updateMap(id, data) {
  return request({
    url: `/admin/maps/${id}`,
    method: 'put',
    data,
  })
}

/**
 * 删除地图
 */
export function deleteMap(id) {
  return request({
    url: `/admin/maps/${id}`,
    method: 'delete',
  })
}
