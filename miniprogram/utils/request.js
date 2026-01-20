/**
 * 网络请求封装
 * 统一处理请求拦截、响应拦截、错误处理、请求重试
 */

const { API_BASE_URL, API_TIMEOUT, RETRY_CONFIG } = require('./constants');
const auth = require('./auth');
const authCheck = require('./auth-check');

/**
 * 显示加载提示
 */
function showLoading(title = '加载中...') {
  wx.showLoading({
    title,
    mask: true,
  });
}

/**
 * 隐藏加载提示
 */
function hideLoading() {
  wx.hideLoading();
}

/**
 * 显示错误提示
 * @param {string} message 错误信息
 */
function showError(message) {
  wx.showToast({
    title: message,
    icon: 'none',
    duration: 2000,
  });
}

/**
 * 请求重试
 * @param {Function} requestFn 请求函数
 * @param {number} retries 剩余重试次数
 * @param {number} delay 延迟时间（毫秒）
 * @returns {Promise} 请求Promise
 */
function retryRequest(requestFn, retries = RETRY_CONFIG.MAX_RETRIES, delay = RETRY_CONFIG.RETRY_DELAY) {
  return requestFn().catch((error) => {
    // 网络错误且还有重试次数时，进行重试
    if (retries > 0 && (error.errMsg?.includes('fail') || error.errMsg?.includes('timeout'))) {
      return new Promise((resolve) => {
        setTimeout(() => {
          resolve(retryRequest(requestFn, retries - 1, delay * 2));
        }, delay);
      });
    }
    throw error;
  });
}

/**
 * 跳转到登录页面
 */
function redirectToLogin() {
  const pages = getCurrentPages();
  const currentPage = pages[pages.length - 1];
  
  // 如果当前不在登录页，则跳转
  if (currentPage && currentPage.route !== 'pages/mine/login') {
    wx.reLaunch({
      url: '/pages/mine/login',
    });
  }
}

/**
 * 执行实际的网络请求
 */
function performRequest(options) {
  const {
    url,
    method = 'GET',
    data = {},
    header = {},
    showLoading: showLoadingFlag = true,
    showError: showErrorFlag = true,
    timeout = API_TIMEOUT,
  } = options;

  // 构建完整URL
  const fullUrl = url.startsWith('http') ? url : `${API_BASE_URL}${url}`;

  // 构建请求头
  const requestHeader = {
    'Content-Type': 'application/json',
    ...header,
  };

  // 添加Session ID到请求头
  const sessionId = auth.getSessionId();
  if (sessionId && typeof sessionId === 'string' && sessionId.trim().length > 0) {
    requestHeader['X-Session-Id'] = sessionId.trim();
  }

  // 创建请求函数
  const requestFn = () => {
    return new Promise((resolve, reject) => {
      wx.request({
        url: fullUrl,
        method,
        data,
        header: requestHeader,
        timeout,
        success: (res) => {
          // 隐藏加载提示
          if (showLoadingFlag) {
            hideLoading();
          }

          const { statusCode, data: responseData } = res;

          // HTTP状态码检查
          if (statusCode >= 200 && statusCode < 300) {
            // 业务状态码检查
            if (responseData.code === 200) {
              resolve(responseData.data);
            } else {
              // 业务错误
              const errorMessage = responseData.message || '请求失败';

              // 401: Session无效或过期
              if (responseData.code === 401) {
                // 清除登录状态缓存
                authCheck.clearAuthCache();
                auth.logout();
                // 跳转到登录页
                redirectToLogin();
              }

              if (showErrorFlag) {
                showError(errorMessage);
              }

              reject(new Error(errorMessage));
            }
          } else {
            // HTTP错误
            let errorMessage = '请求失败';
            switch (statusCode) {
              case 401:
                errorMessage = '未授权，请先登录';
                // 清除登录状态缓存
                authCheck.clearAuthCache();
                auth.logout();
                redirectToLogin();
                break;
              case 403:
                errorMessage = '无权限访问';
                break;
              case 404:
                errorMessage = '请求的资源不存在';
                break;
              case 500:
                errorMessage = '服务器内部错误';
                break;
              default:
                errorMessage = `请求失败: ${statusCode}`;
            }

            if (showErrorFlag) {
              showError(errorMessage);
            }

            reject(new Error(errorMessage));
          }
        },
        fail: (err) => {
          // 隐藏加载提示
          if (showLoadingFlag) {
            hideLoading();
          }

          let errorMessage = '网络错误，请检查网络连接';
          if (err.errMsg?.includes('timeout')) {
            errorMessage = '请求超时，请稍后重试';
          } else if (err.errMsg?.includes('fail')) {
            errorMessage = '网络连接失败，请检查网络';
          }

          if (showErrorFlag) {
            showError(errorMessage);
          }

          reject(new Error(errorMessage));
        },
      });
    });
  };

  // 执行请求（带重试）
  return retryRequest(requestFn);
}

/**
 * 统一请求方法
 * @param {Object} options 请求配置
 * @param {string} options.url 请求地址
 * @param {string} options.method 请求方法，默认GET
 * @param {Object} options.data 请求数据
 * @param {Object} options.header 请求头
 * @param {boolean} options.showLoading 是否显示加载提示，默认true
 * @param {boolean} options.showError 是否显示错误提示，默认true
 * @param {boolean} options.needAuth 是否需要Token认证，默认true
 * @param {number} options.timeout 请求超时时间，默认API_TIMEOUT
 * @returns {Promise} 请求Promise
 */
function request(options = {}) {
  const {
    url,
    method = 'GET',
    data = {},
    header = {},
    showLoading: showLoadingFlag = true,
    showError: showErrorFlag = true,
    needAuth = true,
    timeout = API_TIMEOUT,
  } = options;

  // 显示加载提示
  if (showLoadingFlag) {
    showLoading();
  }

  // 如果需要认证，先检查登录状态
  if (needAuth) {
    // 先检查本地是否有 sessionId（快速检查）
    if (!auth.isLoggedIn()) {
      // 隐藏加载提示
      if (showLoadingFlag) {
        hideLoading();
      }
      
      const errorMessage = '未授权，请先登录';
      
      if (showErrorFlag) {
        showError(errorMessage);
      }
      
      // 跳转到登录页
      redirectToLogin();
      
      return Promise.reject(new Error(errorMessage));
    }
    
    // 检查登录状态是否有效（带缓存）
    return authCheck.checkAuthStatus()
      .then((isValid) => {
        if (!isValid) {
          // 隐藏加载提示
          if (showLoadingFlag) {
            hideLoading();
          }
          
          const errorMessage = '登录已过期，请重新登录';
          
          if (showErrorFlag) {
            showError(errorMessage);
          }
          
          // 跳转到登录页
          redirectToLogin();
          
          return Promise.reject(new Error(errorMessage));
        }
        
        // 登录状态有效，继续发起请求
        return performRequest(options);
      })
      .catch((error) => {
        // 隐藏加载提示
        if (showLoadingFlag) {
          hideLoading();
        }
        
        // 如果是我们主动拒绝的请求，直接返回错误
        if (error.message && (
          error.message.includes('未授权') || 
          error.message.includes('登录已过期')
        )) {
          if (showErrorFlag) {
            showError(error.message);
          }
          return Promise.reject(error);
        }
        
        // 其他错误（如网络错误），继续发起请求（让后端处理）
        return performRequest(options);
      });
  }
  
  // 不需要认证，直接发起请求
  return performRequest(options);
}

/**
 * GET请求
 * @param {string} url 请求地址
 * @param {Object} params 请求参数
 * @param {Object} options 其他选项
 * @returns {Promise} 请求Promise
 */
function get(url, params = {}, options = {}) {
  // 将参数拼接到URL
  let fullUrl = url;
  const paramKeys = Object.keys(params);
  if (paramKeys.length > 0) {
    const queryString = paramKeys
      .map((key) => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
      .join('&');
    fullUrl += (url.includes('?') ? '&' : '?') + queryString;
  }

  return request({
    url: fullUrl,
    method: 'GET',
    ...options,
  });
}

/**
 * POST请求
 * @param {string} url 请求地址
 * @param {Object} data 请求数据
 * @param {Object} options 其他选项
 * @returns {Promise} 请求Promise
 */
function post(url, data = {}, options = {}) {
  return request({
    url,
    method: 'POST',
    data,
    ...options,
  });
}

/**
 * PUT请求
 * @param {string} url 请求地址
 * @param {Object} data 请求数据
 * @param {Object} options 其他选项
 * @returns {Promise} 请求Promise
 */
function put(url, data = {}, options = {}) {
  return request({
    url,
    method: 'PUT',
    data,
    ...options,
  });
}

/**
 * DELETE请求
 * @param {string} url 请求地址
 * @param {Object} options 其他选项
 * @returns {Promise} 请求Promise
 */
function del(url, options = {}) {
  return request({
    url,
    method: 'DELETE',
    ...options,
  });
}

module.exports = {
  request,
  get,
  post,
  put,
  delete: del,
};
