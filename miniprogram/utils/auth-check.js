/**
 * 鉴权检查工具
 * 检查登录状态是否有效，使用缓存机制避免频繁验证
 */

const auth = require('./auth');
const { API_BASE_URL } = require('./constants');

// 模块级缓存变量
let authStatusCache = {
  isValid: false,        // 登录状态是否有效
  checkTime: 0,          // 最后检查时间（时间戳）
  checking: false,        // 是否正在检查中（防止并发）
  checkPromise: null     // 正在进行的检查Promise（防止并发）
};

// 缓存有效期配置（可配置）
// 支持的时间选项：
// - 5分钟：5 * 60 * 1000 = 300000毫秒
// - 30分钟：30 * 60 * 1000 = 1800000毫秒
// - 1小时：60 * 60 * 1000 = 3600000毫秒
// - 1天：24 * 60 * 60 * 1000 = 86400000毫秒
// 默认值：1小时（推荐，平衡性能和安全性）
const CACHE_DURATION = 60 * 60 * 1000; // 1小时

/**
 * 调用后端接口验证登录状态
 * 直接使用 wx.request 避免循环依赖
 * @returns {Promise<boolean>} 返回true表示有效，false表示无效
 */
function verifyAuthStatus() {
  return new Promise((resolve) => {
    const sessionId = auth.getSessionId();
    
    // 如果没有 sessionId，直接返回 false
    if (!sessionId || typeof sessionId !== 'string' || sessionId.trim().length === 0) {
      resolve(false);
      return;
    }
    
    // 构建完整URL
    const url = `${API_BASE_URL}/miniprogram/auth/info`;
    
    // 构建请求头
    const header = {
      'Content-Type': 'application/json',
      'X-Session-Id': sessionId.trim(),
    };
    
    // 发起验证请求（不显示loading和error）
    wx.request({
      url,
      method: 'GET',
      header,
      timeout: 5000, // 5秒超时
      success: (res) => {
        const { statusCode, data: responseData } = res;
        
        // HTTP状态码检查
        if (statusCode >= 200 && statusCode < 300) {
          // 业务状态码检查
          if (responseData && responseData.code === 200) {
            // 接口调用成功，说明登录有效
            resolve(true);
          } else {
            // 401错误或其他业务错误，说明登录无效
            resolve(false);
          }
        } else if (statusCode === 401) {
          // 401未授权，说明登录无效
          resolve(false);
        } else {
          // 其他HTTP错误，返回false
          resolve(false);
        }
      },
      fail: (err) => {
        // 网络错误等异常情况，返回false但不清除本地认证信息
        // 让用户有机会重试
        console.error('登录状态验证请求失败:', err);
        resolve(false);
      },
    });
  });
}

/**
 * 检查登录状态是否有效
 * @param {boolean} forceCheck 是否强制检查（忽略缓存）
 * @returns {Promise<boolean>} 返回true表示登录有效，false表示无效
 */
async function checkAuthStatus(forceCheck = false) {
  // 1. 先检查本地是否有 sessionId
  if (!auth.isLoggedIn()) {
    // 清除缓存
    clearAuthCache();
    return false;
  }
  
  // 2. 检查缓存是否有效
  const now = Date.now();
  if (!forceCheck && authStatusCache.isValid && 
      (now - authStatusCache.checkTime) < CACHE_DURATION) {
    // 缓存有效，直接返回
    return true;
  }
  
  // 3. 如果正在检查中，返回同一个Promise
  if (authStatusCache.checking && authStatusCache.checkPromise) {
    return authStatusCache.checkPromise;
  }
  
  // 4. 发起验证请求
  authStatusCache.checking = true;
  const checkPromise = verifyAuthStatus()
    .then((isValid) => {
      // 更新缓存
      authStatusCache.isValid = isValid;
      authStatusCache.checkTime = Date.now();
      authStatusCache.checking = false;
      authStatusCache.checkPromise = null;
      
      // 如果验证失败，清除本地认证信息
      if (!isValid) {
        auth.logout();
      }
      
      return isValid;
    })
    .catch((error) => {
      // 检查失败，清除缓存
      authStatusCache.checking = false;
      authStatusCache.checkPromise = null;
      clearAuthCache();
      
      // 网络错误等异常情况，返回false
      console.error('登录状态检查失败:', error);
      return false;
    });
  
  authStatusCache.checkPromise = checkPromise;
  return checkPromise;
}

/**
 * 清除登录状态缓存
 */
function clearAuthCache() {
  authStatusCache = {
    isValid: false,
    checkTime: 0,
    checking: false,
    checkPromise: null
  };
}

/**
 * 设置登录状态缓存为有效（登录成功后调用）
 */
function setAuthCacheValid() {
  authStatusCache.isValid = true;
  authStatusCache.checkTime = Date.now();
}

module.exports = {
  checkAuthStatus,
  clearAuthCache,
  setAuthCacheValid,
};
