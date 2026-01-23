/**
 * 文章API
 * 文章的查询、点赞、收藏等操作
 */

const request = require('../../../utils/request');

/**
 * 获取文章列表（支持分类、标签筛选，排序，分页）
 * @param {Object} params 查询参数
 * @param {number} params.categoryId 分类ID（可选）
 * @param {number} params.tagId 标签ID（可选）
 * @param {string} params.keyword 关键词（可选）
 * @param {string} params.sortType 排序类型（可选）
 * @param {number} params.page 页码，默认1
 * @param {number} params.pageSize 每页数量，默认10
 * @returns {Promise} 文章列表
 */
function getArticleList(params = {}) {
  return request.get('/miniprogram/articles', params, {
    needAuth: false,
  });
}

/**
 * 获取文章详情
 * @param {number} id 文章ID
 * @returns {Promise} 文章详情
 */
function getArticleDetail(id) {
  // 添加时间戳参数，避免缓存
  const params = {
    _t: Date.now(), // 时间戳，确保每次请求都是新的
  };
  return request.get(`/miniprogram/articles/${id}`, params, {
    needAuth: false,
  });
}

/**
 * 搜索文章
 * @param {Object} params 搜索参数
 * @param {string} params.keyword 搜索关键词
 * @param {number} params.page 页码，默认1
 * @param {number} params.pageSize 每页数量，默认10
 * @returns {Promise} 搜索结果
 */
function searchArticles(params) {
  return request.get('/miniprogram/articles/search', params, {
    needAuth: false,
  });
}

/**
 * 获取推荐文章列表
 * @param {Object} params 查询参数
 * @param {number} params.limit 数量限制，默认10
 * @returns {Promise} 推荐文章列表
 */
function getRecommendArticles(params = {}) {
  return request.get('/miniprogram/articles/recommend', params, {
    needAuth: false,
  });
}

/**
 * 点赞文章
 * @param {number} id 文章ID
 * @returns {Promise} 点赞结果
 */
function likeArticle(id) {
  return request.post(`/miniprogram/articles/${id}/like`);
}

/**
 * 取消点赞
 * @param {number} id 文章ID
 * @returns {Promise} 取消点赞结果
 */
function unlikeArticle(id) {
  return request.delete(`/miniprogram/articles/${id}/like`);
}

/**
 * 收藏文章
 * @param {number} id 文章ID
 * @returns {Promise} 收藏结果
 */
function favoriteArticle(id) {
  return request.post(`/miniprogram/articles/${id}/favorite`);
}

/**
 * 取消收藏
 * @param {number} id 文章ID
 * @returns {Promise} 取消收藏结果
 */
function unfavoriteArticle(id) {
  return request.delete(`/miniprogram/articles/${id}/favorite`);
}

/**
 * 获取相关文章推荐
 * @param {number} id 文章ID
 * @param {Object} params 查询参数
 * @param {number} params.limit 数量限制，默认5
 * @returns {Promise} 相关文章列表
 */
function getRelatedArticles(id, params = {}) {
  return request.get(`/miniprogram/articles/${id}/related`, params, {
    needAuth: false,
  });
}

/**
 * 获取用户收藏的文章列表
 * @param {Object} params 查询参数
 * @param {number} params.page 页码，默认1
 * @param {number} params.pageSize 每页数量，默认10
 * @returns {Promise} 收藏的文章列表
 */
function getFavoriteArticles(params = {}) {
  return request.get('/miniprogram/articles/favorites', params);
}

module.exports = {
  getArticleList,
  getArticleDetail,
  searchArticles,
  getRecommendArticles,
  likeArticle,
  unlikeArticle,
  favoriteArticle,
  unfavoriteArticle,
  getRelatedArticles,
  getFavoriteArticles,
};
