/**
 * 商品/详情API
 * 获取景点详情、酒店详情、商品详情等
 */

const request = require('../utils/request');

/**
 * 获取景点详情
 * @param {number} id 景点ID
 * @returns {Promise} 景点详情
 */
function getAttractionDetail(id) {
  return request.get(`/miniprogram/attractions/${id}`, {}, {
    needAuth: false,
  });
}

/**
 * 获取酒店详情（包含房型列表）
 * @param {number} id 酒店ID
 * @returns {Promise} 酒店详情
 */
function getHotelDetail(id) {
  return request.get(`/miniprogram/hotels/${id}`, {}, {
    needAuth: false,
  });
}

/**
 * 获取商品详情
 * @param {number} id 商品ID
 * @returns {Promise} 商品详情
 */
function getProductDetail(id) {
  return request.get(`/miniprogram/products/${id}`, {}, {
    needAuth: false,
  });
}

module.exports = {
  getAttractionDetail,
  getHotelDetail,
  getProductDetail,
};
