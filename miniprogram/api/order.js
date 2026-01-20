/**
 * 订单API
 * 订单的创建、查询、取消、退款、支付等操作
 */

const request = require('../utils/request');

/**
 * 创建订单
 * @param {Object} data 订单数据
 * @param {string} data.orderType 订单类型（ATTRACTION/HOTEL/PRODUCT）
 * @param {Array} data.items 订单项列表
 * @param {number} data.couponId 优惠券ID（可选）
 * @param {string} data.contactName 联系人姓名
 * @param {string} data.contactPhone 联系电话
 * @param {string} data.remark 备注（可选）
 * @returns {Promise} 订单信息
 */
function createOrder(data) {
  return request.post('/miniprogram/orders', data);
}

/**
 * 获取订单统计数量
 * @param {Object} options 请求选项（可选）
 * @param {boolean} options.showLoading 是否显示加载提示
 * @param {boolean} options.showError 是否显示错误提示
 * @returns {Promise} 订单统计数量
 */
function getOrderStatistics(options = {}) {
  return request.get('/miniprogram/orders/statistics', {}, options);
}

/**
 * 获取订单列表
 * @param {Object} params 查询参数
 * @param {string} params.status 订单状态（可选）
 * @param {number} params.page 页码，默认1
 * @param {number} params.pageSize 每页数量，默认10
 * @returns {Promise} 订单列表
 */
function getOrderList(params = {}) {
  return request.get('/miniprogram/orders', params);
}

/**
 * 获取订单详情
 * @param {number} id 订单ID
 * @returns {Promise} 订单详情
 */
function getOrderDetail(id) {
  return request.get(`/miniprogram/orders/${id}`);
}

/**
 * 取消订单
 * @param {number} id 订单ID
 * @returns {Promise} 取消结果
 */
function cancelOrder(id) {
  return request.post(`/miniprogram/orders/${id}/cancel`);
}

/**
 * 申请退款
 * @param {number} id 订单ID
 * @param {string} reason 退款原因（可选）
 * @returns {Promise} 退款申请结果
 */
function applyRefund(id, reason) {
  return request.post(`/miniprogram/orders/${id}/refund`, { reason });
}

/**
 * 支付订单
 * @param {number} id 订单ID
 * @param {Object} data 支付数据
 * @param {string} data.payType 支付方式（WECHAT）
 * @returns {Promise} 支付结果
 */
function payOrder(id, data) {
  return request.post(`/miniprogram/orders/${id}/pay`, data);
}

module.exports = {
  createOrder,
  getOrderStatistics,
  getOrderList,
  getOrderDetail,
  cancelOrder,
  applyRefund,
  payOrder,
};
