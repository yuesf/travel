/**
 * URL处理工具
 * 用于处理图片、视频等媒体资源的URL，解决小程序无法访问localhost的问题
 * 注意：后端统一返回签名URL，前端不再需要OSS URL处理逻辑
 */

const { API_BASE_URL } = require('./constants');

/**
 * 从API_BASE_URL中提取基础域名
 * 例如：http://localhost:8080/api/v1 -> http://localhost:8080
 */
function getBaseUrl() {
  try {
    // 如果API_BASE_URL包含协议，提取基础部分
    if (API_BASE_URL.startsWith('http://') || API_BASE_URL.startsWith('https://')) {
      // 找到第三个斜杠的位置（协议://域名:端口）
      const match = API_BASE_URL.match(/^(https?:\/\/[^\/]+)/);
      if (match) {
        return match[1];
      }
    }
    // 如果无法解析，返回默认值
    return 'http://localhost:8080';
  } catch (error) {
    console.error('解析API_BASE_URL失败:', error);
    return 'http://localhost:8080';
  }
}

/**
 * 规范化URL，将localhost替换为可访问的地址
 * 在开发环境中，如果URL包含localhost，尝试替换为本机IP或保持原样
 * 注意：后端统一返回签名URL，直接使用即可
 * 
 * @param {string} url 原始URL
 * @returns {string} 规范化后的URL
 */
function normalizeUrl(url) {
  if (!url || typeof url !== 'string' || url.trim() === '') {
    return '';
  }

  // 如果URL已经是完整路径且不包含localhost，直接返回（后端统一返回签名URL）
  if (url.startsWith('http') && !url.includes('localhost')) {
    return url;
  }

  // 如果URL包含localhost，尝试替换
  if (url.includes('localhost')) {
    const baseUrl = getBaseUrl();
    
    // 优先检查是否有配置的替换地址（可以通过app.globalData设置）
    // 这在开发环境中特别有用，可以设置为本机IP地址，如：http://192.168.1.100:8080
    try {
      const app = getApp();
      if (app && app.globalData && app.globalData.mediaBaseUrl) {
        const mediaBaseUrl = app.globalData.mediaBaseUrl;
        // 提取路径部分（例如：/uploads/common/video/...）
        const pathMatch = url.match(/\/uploads\/.*$/);
        if (pathMatch) {
          return mediaBaseUrl.replace(/\/$/, '') + pathMatch[0];
        }
      }
    } catch (e) {
      // getApp() 可能在某些情况下失败，忽略错误
      console.debug('无法获取app实例:', e);
    }
    
    // 如果没有配置mediaBaseUrl，使用API_BASE_URL的基础部分
    // 提取路径部分（例如：/uploads/common/video/...）
    const pathMatch = url.match(/\/uploads\/.*$/);
    if (pathMatch) {
      // 使用baseUrl，即使它也是localhost
      // 在微信开发者工具中，如果设置了"不校验合法域名"，localhost也可以访问
      return baseUrl.replace(/\/$/, '') + pathMatch[0];
    }
    
    // 如果无法提取路径，尝试直接替换localhost部分
    // 这种情况应该很少见，但为了兼容性还是处理一下
    return url.replace(/http:\/\/localhost:\d+/, baseUrl);
  }

  // 如果是相对路径，需要添加基础URL
  if (url.startsWith('/uploads/')) {
    const baseUrl = getBaseUrl();
    return baseUrl.replace(/\/$/, '') + url;
  }

  // 其他情况直接返回
  return url;
}

/**
 * 批量规范化URL数组
 * @param {string[]} urls URL数组
 * @returns {string[]} 规范化后的URL数组
 */
function normalizeUrls(urls) {
  if (!Array.isArray(urls)) {
    return [];
  }
  return urls.map(url => normalizeUrl(url));
}

module.exports = {
  normalizeUrl,
  normalizeUrls,
  getBaseUrl,
};
