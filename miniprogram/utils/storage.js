/**
 * 存储工具类
 * 封装微信小程序本地存储操作
 */

const STORAGE_KEYS = require('./constants').STORAGE_KEYS;

/**
 * 存储数据
 * @param {string} key 存储键
 * @param {any} value 存储值
 * @param {boolean} sync 是否同步存储，默认false（异步）
 */
function setStorage(key, value, sync = false) {
  try {
    if (sync) {
      wx.setStorageSync(key, value);
    } else {
      wx.setStorage({
        key,
        data: value,
      });
    }
  } catch (error) {
    console.error('存储数据失败:', error);
    throw error;
  }
}

/**
 * 获取存储数据
 * @param {string} key 存储键
 * @param {any} defaultValue 默认值
 * @param {boolean} sync 是否同步获取，默认false（异步）
 * @returns {Promise<any>|any} 存储值
 */
function getStorage(key, defaultValue = null, sync = false) {
  try {
    if (sync) {
      const value = wx.getStorageSync(key);
      return value !== '' ? value : defaultValue;
    } else {
      return new Promise((resolve, reject) => {
        wx.getStorage({
          key,
          success: (res) => {
            resolve(res.data);
          },
          fail: (err) => {
            if (err.errMsg.includes('not found')) {
              resolve(defaultValue);
            } else {
              reject(err);
            }
          },
        });
      });
    }
  } catch (error) {
    console.error('获取存储数据失败:', error);
    return defaultValue;
  }
}

/**
 * 删除存储数据
 * @param {string} key 存储键
 * @param {boolean} sync 是否同步删除，默认false（异步）
 */
function removeStorage(key, sync = false) {
  try {
    if (sync) {
      wx.removeStorageSync(key);
    } else {
      wx.removeStorage({
        key,
      });
    }
  } catch (error) {
    console.error('删除存储数据失败:', error);
    throw error;
  }
}

/**
 * 清空所有存储数据
 * @param {boolean} sync 是否同步清空，默认false（异步）
 */
function clearStorage(sync = false) {
  try {
    if (sync) {
      wx.clearStorageSync();
    } else {
      wx.clearStorage();
    }
  } catch (error) {
    console.error('清空存储数据失败:', error);
    throw error;
  }
}

/**
 * 获取存储信息
 * @returns {Promise<Object>} 存储信息
 */
function getStorageInfo() {
  return new Promise((resolve, reject) => {
    wx.getStorageInfo({
      success: (res) => {
        resolve(res);
      },
      fail: (err) => {
        reject(err);
      },
    });
  });
}

module.exports = {
  setStorage,
  getStorage,
  removeStorage,
  clearStorage,
  getStorageInfo,
  // 便捷方法
  setToken: (token) => setStorage(STORAGE_KEYS.TOKEN, token, true),
  getToken: () => getStorage(STORAGE_KEYS.TOKEN, null, true),
  removeToken: () => removeStorage(STORAGE_KEYS.TOKEN, true),
  setSessionId: (sessionId) => setStorage(STORAGE_KEYS.SESSION_ID, sessionId, true),
  getSessionId: () => getStorage(STORAGE_KEYS.SESSION_ID, null, true),
  removeSessionId: () => removeStorage(STORAGE_KEYS.SESSION_ID, true),
  setUserInfo: (userInfo) => setStorage(STORAGE_KEYS.USER_INFO, userInfo, true),
  getUserInfo: () => getStorage(STORAGE_KEYS.USER_INFO, null, true),
  removeUserInfo: () => removeStorage(STORAGE_KEYS.USER_INFO, true),
  setCartCount: (count) => setStorage(STORAGE_KEYS.CART_COUNT, count, true),
  getCartCount: () => getStorage(STORAGE_KEYS.CART_COUNT, 0, true),
  getSearchHistory: () => getStorage(STORAGE_KEYS.SEARCH_HISTORY, [], true),
  setSearchHistory: (history) => setStorage(STORAGE_KEYS.SEARCH_HISTORY, history, true),
};
