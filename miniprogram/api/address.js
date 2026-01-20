/**
 * 收货地址API
 */

const request = require('../utils/request');

/**
 * 获取收货地址列表
 */
function getAddressList() {
  return request.get('/miniprogram/addresses');
}

/**
 * 获取收货地址详情
 */
function getAddressById(id) {
  return request.get(`/miniprogram/addresses/${id}`);
}

/**
 * 创建收货地址
 */
function createAddress(data) {
  return request.post('/miniprogram/addresses', data);
}

/**
 * 更新收货地址
 */
function updateAddress(id, data) {
  return request.put(`/miniprogram/addresses/${id}`, data);
}

/**
 * 删除收货地址
 */
function deleteAddress(id) {
  return request.delete(`/miniprogram/addresses/${id}`);
}

/**
 * 设置默认收货地址
 */
function setDefaultAddress(id) {
  return request.put(`/miniprogram/addresses/${id}/default`);
}

module.exports = {
  getAddressList,
  getAddressById,
  createAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress,
};
