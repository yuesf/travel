/**
 * 文件相关API
 * 处理文件上传、下载、签名URL获取等
 */

const request = require('../utils/request');

/**
 * 根据URL获取签名URL（用于私有Bucket访问）
 * @param {string} url 文件访问URL
 * @returns {Promise<string>} 签名URL，如果不是OSS URL则返回原URL
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
