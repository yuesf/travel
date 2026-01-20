/**
 * 认证API
 * 用户登录、登出、获取用户信息等
 * 使用微信session机制
 */

const request = require('../utils/request');

/**
 * 微信登录
 * @param {Object} data 登录数据
 * @param {string} data.code 微信登录code
 * @returns {Promise} 登录结果，包含sessionId和用户信息
 */
function wechatLogin(data) {
  return request.post('/miniprogram/auth/login', data, {
    needAuth: false,
  });
}

/**
 * 登出
 * @returns {Promise} 登出结果
 */
function logout() {
  return request.post('/miniprogram/auth/logout');
}

/**
 * 刷新session
 * @param {Object} data 登录数据
 * @param {string} data.code 微信登录code
 * @returns {Promise} 登录结果，包含新的sessionId
 */
function refreshSession(data) {
  return request.post('/miniprogram/auth/refresh', data, {
    needAuth: false,
  });
}

/**
 * 获取用户信息
 * @param {Object} options 请求选项
 * @returns {Promise} 用户信息
 */
function getUserInfo(options = {}) {
  return request.get('/miniprogram/auth/info', {}, options);
}

/**
 * 更新用户信息
 * @param {Object} userInfo 用户信息
 * @returns {Promise} 更新结果
 */
function updateUserInfo(userInfo) {
  return request.put('/miniprogram/auth/info', userInfo);
}

module.exports = {
  wechatLogin,
  logout,
  refreshSession,
  getUserInfo,
  updateUserInfo,
};
