import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from './auth'
import router from '@/router'
import { API_BASE_URL } from '@/config/api'

// 创建axios实例
const service = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 添加Token到请求头
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data

    // 如果返回的状态码不是200，说明接口有问题
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')

      // 401: Token无效或过期
      if (res.code === 401) {
        removeToken()
        router.push('/login')
      }

      // 403: 无权限
      if (res.code === 403) {
        ElMessage.error('无权限访问')
      }

      return Promise.reject(new Error(res.message || '请求失败'))
    } else {
      return res
    }
  },
  (error) => {
    console.error('响应错误:', error)
    
    if (error.response) {
      const { status, data } = error.response

      switch (status) {
        case 401:
          ElMessage.error('未授权，请先登录')
          removeToken()
          router.push('/login')
          break
        case 403:
          ElMessage.error('无权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 502:
          // 502 网关错误，可能是后端服务不可用，静默处理避免影响用户体验
          // 只在控制台输出警告，不显示错误提示
          console.warn('网关错误 (502):', error.config?.url || '未知接口')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(data?.message || `请求失败: ${status}`)
      }
    } else if (error.request) {
      // 检查是否是连接被拒绝（后端服务未启动）
      if (error.code === 'ECONNREFUSED' || error.message?.includes('ECONNREFUSED')) {
        ElMessage.error('无法连接到后端服务，请确保后端服务已启动（端口 8080）')
      } else {
        ElMessage.error('网络错误，请检查网络连接')
      }
    } else {
      ElMessage.error(error.message || '请求失败')
    }

    return Promise.reject(error)
  }
)

export default service
