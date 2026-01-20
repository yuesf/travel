/**
 * 用户API
 * 用户信息管理相关接口
 */

const request = require('../utils/request');

/**
 * 获取用户信息
 * @returns {Promise} 用户信息
 */
function getUserInfo() {
  return request.get('/miniprogram/user/info');
}

/**
 * 更新用户信息
 * @param {Object} userInfo 用户信息
 * @returns {Promise} 更新结果
 */
function updateUserInfo(userInfo) {
  return request.put('/miniprogram/user/info', userInfo);
}

/**
 * 获取用户收藏的文章列表
 * @param {Object} params 查询参数
 * @param {number} params.page 页码，默认1
 * @param {number} params.pageSize 每页数量，默认10
 * @returns {Promise} 收藏的文章列表
 */
function getFavoriteArticles(params = {}) {
  return request.get('/miniprogram/user/favorites/articles', params);
}

module.exports = {
  getUserInfo,
  updateUserInfo,
  getFavoriteArticles,
};
