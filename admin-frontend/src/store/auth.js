import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, logout } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(getToken())
  const userInfo = ref(null)

  // 登录
  const userLogin = async (loginForm) => {
    try {
      const response = await login(loginForm)
      if (response.data && response.data.token) {
        token.value = response.data.token
        userInfo.value = {
          userId: response.data.userId,
          username: response.data.username,
          realName: response.data.realName,
        }
        // 记住我功能由登录页面控制，这里使用默认值
        setToken(response.data.token, loginForm.remember || false)
        return Promise.resolve(response)
      } else {
        return Promise.reject(new Error('登录失败'))
      }
    } catch (error) {
      return Promise.reject(error)
    }
  }

  // 登出
  const userLogout = async () => {
    try {
      await logout()
    } catch (error) {
      console.error('登出失败:', error)
    } finally {
      token.value = ''
      userInfo.value = null
      removeToken()
    }
  }

  // 设置用户信息
  const setUserInfo = (info) => {
    userInfo.value = info
  }

  return {
    token,
    userInfo,
    userLogin,
    userLogout,
    setUserInfo,
  }
})
