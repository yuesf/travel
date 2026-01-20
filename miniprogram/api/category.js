/**
 * 分类API
 * 获取分类列表、景点列表、酒店列表、商品列表等
 */

const request = require('../utils/request');

/**
 * 获取分类列表
 * @returns {Promise} 分类列表
 */
function getCategories() {
  return request.get('/miniprogram/categories', {}, {
    needAuth: false,
  });
}

/**
 * 获取景点列表（支持分类筛选）
 * @param {Object} params 查询参数
 * @param {number} params.categoryId 分类ID（可选）
 * @param {string} params.keyword 关键词（可选）
 * @param {number} params.page 页码，默认1
 * @param {number} params.pageSize 每页数量，默认10
 * @returns {Promise} 景点列表
 */
function getAttractions(params = {}) {
  return request.get('/miniprogram/attractions', params, {
    needAuth: false,
  });
}

/**
 * 获取酒店列表（支持分类筛选）
 * @param {Object} params 查询参数
 * @param {number} params.categoryId 分类ID（可选）
 * @param {string} params.keyword 关键词（可选）
 * @param {number} params.page 页码，默认1
 * @param {number} params.pageSize 每页数量，默认10
 * @returns {Promise} 酒店列表
 */
function getHotels(params = {}) {
  return request.get('/miniprogram/hotels', params, {
    needAuth: false,
  });
}

/**
 * 获取商品列表（支持分类筛选）
 * @param {Object} params 查询参数
 * @param {number} params.categoryId 分类ID（可选）
 * @param {string} params.keyword 关键词（可选）
 * @param {number} params.page 页码，默认1
 * @param {number} params.pageSize 每页数量，默认10
 * @returns {Promise} 商品列表
 */
function getProducts(params = {}) {
  return request.get('/miniprogram/products', params, {
    needAuth: false,
  });
}

module.exports = {
  getCategories,
  getAttractions,
  getHotels,
  getProducts,
};
