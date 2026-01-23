/**
 * 优惠券API
 * 优惠券的查询、使用等操作
 */

const request = require('../../../utils/request');

/**
 * 获取用户优惠券列表
 * @param {Object} params 查询参数
 * @param {number} params.status 优惠券状态（0-未使用，1-已使用，2-已过期）
 * @param {number} params.page 页码，默认1
 * @param {number} params.pageSize 每页数量，默认10
 * @returns {Promise} 优惠券列表
 */
function getCouponList(params = {}) {
  return request.get('/miniprogram/coupons', params);
}

module.exports = {
  getCouponList,
};
