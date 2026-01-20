/**
 * 认证工具类
 * 管理Session ID和用户认证相关操作
 */

const storage = require('./storage');
const authCheck = require('./auth-check');

/**
 * 获取Session ID
 * @returns {string|null} Session ID
 */
function getSessionId() {
  return storage.getSessionId();
}

/**
 * 设置Session ID
 * @param {string} sessionId Session ID
 */
function setSessionId(sessionId) {
  storage.setSessionId(sessionId);
  
  // 设置登录状态缓存为有效
  authCheck.setAuthCacheValid();
}

/**
 * 删除Session ID
 */
function removeSessionId() {
  storage.removeSessionId();
}

/**
 * 检查是否已登录
 * @returns {boolean} 是否已登录
 */
function isLoggedIn() {
  const sessionId = getSessionId();
  // 确保 Session ID 是非空字符串
  return sessionId && typeof sessionId === 'string' && sessionId.trim().length > 0;
}

/**
 * 获取用户信息
 * @returns {Object|null} 用户信息
 */
function getUserInfo() {
  return storage.getUserInfo();
}

/**
 * 设置用户信息
 * @param {Object} userInfo 用户信息
 */
function setUserInfo(userInfo) {
  storage.setUserInfo(userInfo);
}

/**
 * 删除用户信息
 */
function removeUserInfo() {
  storage.removeUserInfo();
}

/**
 * 退出登录
 * 清除Session ID和用户信息
 */
function logout() {
  removeSessionId();
  removeUserInfo();
  
  // 清除登录状态缓存
  authCheck.clearAuthCache();
}

module.exports = {
  getSessionId,
  setSessionId,
  removeSessionId,
  isLoggedIn,
  getUserInfo,
  setUserInfo,
  removeUserInfo,
  logout,
};
