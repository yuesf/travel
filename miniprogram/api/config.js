/**
 * 配置 API
 * 小程序配置相关接口
 */

const request = require('../utils/request');

/**
 * 获取 Logo 配置
 * @returns {Promise} Logo 配置
 */
function getLogoConfig() {
  return request.get('/miniprogram/config/logo', {}, {
    showLoading: false,
    showError: false,
    needAuth: false, // Logo 配置是公开信息，无需认证
  });
}

module.exports = {
  getLogoConfig,
};
