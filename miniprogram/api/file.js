/**
 * 文件相关API
 * 处理文件上传、下载等
 * 
 * 注意：OSS bucket已改为"私有写公有读"模式，不再需要获取签名URL
 */

const request = require('../utils/request');

/**
 * 根据URL获取公开URL（已废弃：OSS bucket已改为"私有写公有读"模式，不再需要签名URL）
 * 保留此函数用于向后兼容，如果是签名URL则提取基础URL部分
 * @deprecated 请直接使用URL，不再需要获取签名URL
 * @param {string} url 文件访问URL（可能是签名URL或公开URL）
 * @returns {Promise<string>} 公开URL，如果不是OSS URL则返回原URL
 */
function getSignedUrlByUrl(url) {
  return request.get('/common/file/signed-url', { url }, {
    needAuth: false,
    showLoading: false,
    showError: false,
  });
}

module.exports = {
  getSignedUrlByUrl,
};
