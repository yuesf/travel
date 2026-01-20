/**
 * API 配置
 * 从环境变量读取 API 基础路径，支持开发和生产环境分离
 */

/**
 * 获取 API 基础路径
 * 优先使用环境变量，如果未设置则根据模式使用默认值
 */
const getApiBaseUrl = () => {
  // 优先使用环境变量
  if (import.meta.env.VITE_API_BASE_URL) {
    return import.meta.env.VITE_API_BASE_URL
  }
  
  // 根据模式使用默认值
  const mode = import.meta.env.MODE
  if (mode === 'production') {
    return '/travel/api/v1'
  }
  
  // 开发环境或其他模式
  return '/api/v1'
}

// 导出 API 基础路径常量
export const API_BASE_URL = getApiBaseUrl()

// 导出函数以便动态获取（如果需要）
export { getApiBaseUrl }
