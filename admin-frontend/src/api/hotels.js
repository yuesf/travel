import request from '@/utils/request'

/**
 * 分页查询酒店列表
 */
export function getHotelList(params) {
  return request({
    url: '/admin/hotels',
    method: 'get',
    params,
  })
}

/**
 * 根据ID查询酒店详情
 */
export function getHotelById(id) {
  return request({
    url: `/admin/hotels/${id}`,
    method: 'get',
  })
}

/**
 * 创建酒店
 */
export function createHotel(data) {
  return request({
    url: '/admin/hotels',
    method: 'post',
    data,
  })
}

/**
 * 更新酒店
 */
export function updateHotel(id, data) {
  return request({
    url: `/admin/hotels/${id}`,
    method: 'put',
    data,
  })
}

/**
 * 删除酒店（软删除）
 */
export function deleteHotel(id) {
  return request({
    url: `/admin/hotels/${id}`,
    method: 'delete',
  })
}

/**
 * 根据酒店ID查询房型列表
 */
export function getHotelRooms(hotelId) {
  return request({
    url: `/admin/hotels/${hotelId}/rooms`,
    method: 'get',
  })
}

/**
 * 根据ID查询房型详情
 */
export function getHotelRoomById(id) {
  return request({
    url: `/admin/hotels/rooms/${id}`,
    method: 'get',
  })
}

/**
 * 创建房型
 */
export function createHotelRoom(data) {
  return request({
    url: '/admin/hotels/rooms',
    method: 'post',
    data,
  })
}

/**
 * 更新房型
 */
export function updateHotelRoom(id, data) {
  return request({
    url: `/admin/hotels/rooms/${id}`,
    method: 'put',
    data,
  })
}

/**
 * 删除房型
 */
export function deleteHotelRoom(id) {
  return request({
    url: `/admin/hotels/rooms/${id}`,
    method: 'delete',
  })
}
