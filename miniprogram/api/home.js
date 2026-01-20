/**
 * 首页API
 * 获取首页数据、搜索等
 */

const request = require('../utils/request');

/**
 * 获取首页数据
 * @returns {Promise} 首页数据，包含轮播图、推荐内容等
 */
function getHome() {
  return request.get('/miniprogram/home', {}, {
    needAuth: false,
  });
}

/**
 * 搜索景点、酒店、商品
 * @param {Object} params 搜索参数
 * @param {string} params.keyword 搜索关键词
 * @param {string} params.type 搜索类型（ATTRACTION/HOTEL/PRODUCT，可选）
 * @param {number} params.page 页码，默认1
 * @param {number} params.pageSize 每页数量，默认10
 * @returns {Promise} 搜索结果
 */
function search(params) {
  return request.get('/miniprogram/home/search', params, {
    needAuth: false,
  });
}

module.exports = {
  getHome,
  search,
};
