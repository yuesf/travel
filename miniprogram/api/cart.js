/**
 * 购物车API
 * 购物车的增删改查操作
 */

const request = require('../utils/request');

/**
 * 获取购物车列表
 * @returns {Promise} 购物车列表
 */
function getCartList() {
  return request.get('/miniprogram/cart');
}

/**
 * 添加商品到购物车
 * @param {Object} data 购物车数据
 * @param {string} data.productType 商品类型（ATTRACTION/HOTEL/PRODUCT）
 * @param {number} data.productId 商品ID
 * @param {number} data.quantity 数量
 * @param {number} data.roomId 房型ID（酒店类型时必填）
 * @param {string} data.checkInDate 入住日期（酒店类型时必填）
 * @param {string} data.checkOutDate 退房日期（酒店类型时必填）
 * @returns {Promise} 购物车项
 */
function addToCart(data) {
  return request.post('/miniprogram/cart', data);
}

/**
 * 更新购物车商品数量
 * @param {number} id 购物车ID
 * @param {Object} data 更新数据
 * @param {number} data.quantity 数量
 * @returns {Promise} 更新后的购物车项
 */
function updateCart(id, data) {
  return request.put(`/miniprogram/cart/${id}`, data);
}

/**
 * 删除购物车商品
 * @param {number} id 购物车ID
 * @returns {Promise} 删除结果
 */
function deleteCart(id) {
  return request.delete(`/miniprogram/cart/${id}`);
}

module.exports = {
  getCartList,
  addToCart,
  updateCart,
  deleteCart,
};
