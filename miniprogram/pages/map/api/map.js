/**
 * 地图相关 API
 */

const request = require('../../../utils/request');

/**
 * 获取地图列表
 */
function getMapList() {
  return request.get('/miniprogram/maps', {}, {
    needAuth: false,
  });
}

module.exports = {
  getMapList,
};
