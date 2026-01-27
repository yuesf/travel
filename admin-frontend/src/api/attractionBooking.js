import request from '@/utils/request'

/**
 * 查询可订日期列表
 */
export function getBookingDateList(attractionId, params) {
  return request({
    url: `/admin/attractions/${attractionId}/booking-dates`,
    method: 'get',
    params,
  })
}

/**
 * 根据ID查询可订日期详情
 */
export function getBookingDateById(attractionId, id) {
  return request({
    url: `/admin/attractions/${attractionId}/booking-dates/${id}`,
    method: 'get',
  })
}

/**
 * 创建可订日期
 */
export function createBookingDate(attractionId, data) {
  return request({
    url: `/admin/attractions/${attractionId}/booking-dates`,
    method: 'post',
    data,
  })
}

/**
 * 批量设置可订日期
 */
export function batchCreateBookingDate(attractionId, data) {
  return request({
    url: `/admin/attractions/${attractionId}/booking-dates/batch`,
    method: 'post',
    data,
  })
}

/**
 * 更新可订日期
 */
export function updateBookingDate(attractionId, id, data) {
  return request({
    url: `/admin/attractions/${attractionId}/booking-dates/${id}`,
    method: 'put',
    data,
  })
}

/**
 * 删除可订日期
 */
export function deleteBookingDate(attractionId, id) {
  return request({
    url: `/admin/attractions/${attractionId}/booking-dates/${id}`,
    method: 'delete',
  })
}

/**
 * 查询金顶时间段列表
 */
export function getGoldenSummitTimeSlotList(attractionId, params) {
  return request({
    url: `/admin/attractions/${attractionId}/golden-summit-time-slots`,
    method: 'get',
    params,
  })
}

/**
 * 根据ID查询金顶时间段详情
 */
export function getGoldenSummitTimeSlotById(attractionId, id) {
  return request({
    url: `/admin/attractions/${attractionId}/golden-summit-time-slots/${id}`,
    method: 'get',
  })
}

/**
 * 创建金顶时间段
 */
export function createGoldenSummitTimeSlot(attractionId, data) {
  return request({
    url: `/admin/attractions/${attractionId}/golden-summit-time-slots`,
    method: 'post',
    data,
  })
}

/**
 * 更新金顶时间段
 */
export function updateGoldenSummitTimeSlot(attractionId, id, data) {
  return request({
    url: `/admin/attractions/${attractionId}/golden-summit-time-slots/${id}`,
    method: 'put',
    data,
  })
}

/**
 * 删除金顶时间段
 */
export function deleteGoldenSummitTimeSlot(attractionId, id) {
  return request({
    url: `/admin/attractions/${attractionId}/golden-summit-time-slots/${id}`,
    method: 'delete',
  })
}

/**
 * 查询票种分类列表
 */
export function getTicketCategoryList(attractionId) {
  return request({
    url: `/admin/attractions/${attractionId}/ticket-categories`,
    method: 'get',
  })
}

/**
 * 根据ID查询票种分类详情
 */
export function getTicketCategoryById(attractionId, id) {
  return request({
    url: `/admin/attractions/${attractionId}/ticket-categories/${id}`,
    method: 'get',
  })
}

/**
 * 创建票种分类
 */
export function createTicketCategory(attractionId, data) {
  return request({
    url: `/admin/attractions/${attractionId}/ticket-categories`,
    method: 'post',
    data,
  })
}

/**
 * 更新票种分类
 */
export function updateTicketCategory(attractionId, id, data) {
  return request({
    url: `/admin/attractions/${attractionId}/ticket-categories/${id}`,
    method: 'put',
    data,
  })
}

/**
 * 删除票种分类
 */
export function deleteTicketCategory(attractionId, id) {
  return request({
    url: `/admin/attractions/${attractionId}/ticket-categories/${id}`,
    method: 'delete',
  })
}

/**
 * 查询票种列表
 */
export function getTicketList(attractionId, params) {
  return request({
    url: `/admin/attractions/${attractionId}/tickets`,
    method: 'get',
    params,
  })
}

/**
 * 根据ID查询票种详情
 */
export function getTicketById(attractionId, id) {
  return request({
    url: `/admin/attractions/${attractionId}/tickets/${id}`,
    method: 'get',
  })
}

/**
 * 创建票种
 */
export function createTicket(attractionId, data) {
  return request({
    url: `/admin/attractions/${attractionId}/tickets`,
    method: 'post',
    data,
  })
}

/**
 * 更新票种
 */
export function updateTicket(attractionId, id, data) {
  return request({
    url: `/admin/attractions/${attractionId}/tickets/${id}`,
    method: 'put',
    data,
  })
}

/**
 * 删除票种
 */
export function deleteTicket(attractionId, id) {
  return request({
    url: `/admin/attractions/${attractionId}/tickets/${id}`,
    method: 'delete',
  })
}
