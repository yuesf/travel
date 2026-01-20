/**
 * 地图配置
 * 高德地图API Key配置
 * 请在此处配置您的高德地图API Key
 * 获取方式：https://lbs.amap.com/api/javascript-api/guide/abc/prepare
 */
export const AMAP_API_KEY = import.meta.env.VITE_AMAP_API_KEY || ''

// 如果没有配置API Key，可以使用测试Key（功能受限）
// 建议在生产环境中配置真实的API Key
export const getAMapApiKey = () => {
  return AMAP_API_KEY || ''
}
