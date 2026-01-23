/**
 * 并行请求工具函数
 * 用于优化多个独立接口的并行执行
 */

/**
 * 并行执行多个请求
 * @param {Array<Function>} requestFns 请求函数数组，每个函数返回 Promise
 * @param {Object} options 选项
 * @param {boolean} options.failFast 是否快速失败（任一请求失败立即返回），默认 false
 * @param {Function} options.onProgress 进度回调函数 (completed, total) => void
 * @returns {Promise<Array>} 返回所有请求的结果数组
 * 
 * @example
 * const [userInfo, orderList] = await parallelRequest([
 *   () => userApi.getUserInfo(),
 *   () => orderApi.getOrderList(),
 * ]);
 */
function parallelRequest(requestFns, options = {}) {
  const { failFast = false, onProgress } = options;

  if (!Array.isArray(requestFns) || requestFns.length === 0) {
    return Promise.resolve([]);
  }

  // 如果只有一个请求，直接执行
  if (requestFns.length === 1) {
    return requestFns[0]().then(result => [result]);
  }

  // 使用 Promise.allSettled 确保所有请求都执行完成
  // 即使某个请求失败，其他请求也会继续执行
  const promises = requestFns.map((fn, index) => {
    return Promise.resolve(fn())
      .then(result => {
        if (onProgress) {
          onProgress(index + 1, requestFns.length);
        }
        return { status: 'fulfilled', value: result, index };
      })
      .catch(error => {
        if (onProgress) {
          onProgress(index + 1, requestFns.length);
        }
        if (failFast) {
          throw error;
        }
        return { status: 'rejected', reason: error, index };
      });
  });

  if (failFast) {
    // 快速失败模式：使用 Promise.all，任一失败立即返回
    return Promise.all(promises).then(results => {
      return results.map(r => r.value);
    });
  } else {
    // 默认模式：使用 Promise.allSettled，所有请求都执行完成
    return Promise.allSettled(promises).then(results => {
      return results.map((result, index) => {
        if (result.status === 'fulfilled') {
          const innerResult = result.value;
          if (innerResult.status === 'fulfilled') {
            return innerResult.value;
          } else {
            // 请求失败，返回 null 或抛出错误
            console.error(`请求 ${index} 失败:`, innerResult.reason);
            return null;
          }
        } else {
          console.error(`请求 ${index} 执行失败:`, result.reason);
          return null;
        }
      });
    });
  }
}

/**
 * 并行执行多个请求，返回结果对象
 * @param {Object} requestMap 请求映射对象，key 为结果对象的属性名，value 为请求函数
 * @param {Object} options 选项
 * @returns {Promise<Object>} 返回结果对象，key 对应请求的 key
 * 
 * @example
 * const { userInfo, orderList, couponList } = await parallelRequestMap({
 *   userInfo: () => userApi.getUserInfo(),
 *   orderList: () => orderApi.getOrderList(),
 *   couponList: () => couponApi.getCouponList(),
 * });
 */
function parallelRequestMap(requestMap, options = {}) {
  const keys = Object.keys(requestMap);
  const requestFns = keys.map(key => requestMap[key]);

  return parallelRequest(requestFns, options).then(results => {
    const resultMap = {};
    keys.forEach((key, index) => {
      resultMap[key] = results[index];
    });
    return resultMap;
  });
}

/**
 * 批量执行请求，支持并发控制
 * @param {Array<Function>} requestFns 请求函数数组
 * @param {number} concurrency 并发数，默认 3
 * @returns {Promise<Array>} 返回所有请求的结果数组
 * 
 * @example
 * const results = await batchRequest(requestFns, 3); // 最多同时执行 3 个请求
 */
function batchRequest(requestFns, concurrency = 3) {
  if (!Array.isArray(requestFns) || requestFns.length === 0) {
    return Promise.resolve([]);
  }

  const results = [];
  let index = 0;

  function executeNext() {
    if (index >= requestFns.length) {
      return Promise.resolve();
    }

    const currentIndex = index++;
    return Promise.resolve(requestFns[currentIndex]())
      .then(result => {
        results[currentIndex] = result;
        return executeNext();
      })
      .catch(error => {
        results[currentIndex] = { error };
        return executeNext();
      });
  }

  // 创建并发执行的任务
  const tasks = [];
  const actualConcurrency = Math.min(concurrency, requestFns.length);
  for (let i = 0; i < actualConcurrency; i++) {
    tasks.push(executeNext());
  }

  return Promise.all(tasks).then(() => results);
}

module.exports = {
  parallelRequest,
  parallelRequestMap,
  batchRequest,
};
