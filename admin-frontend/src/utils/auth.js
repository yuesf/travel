const TOKEN_KEY = 'admin_token'
const REMEMBER_KEY = 'remember_me'

/**
 * 获取Token
 */
export function getToken() {
  const remember = localStorage.getItem(REMEMBER_KEY) === 'true'
  if (remember) {
    return localStorage.getItem(TOKEN_KEY) || ''
  } else {
    return sessionStorage.getItem(TOKEN_KEY) || ''
  }
}

/**
 * 设置Token
 */
export function setToken(token, remember = false) {
  localStorage.setItem(REMEMBER_KEY, remember.toString())
  if (remember) {
    localStorage.setItem(TOKEN_KEY, token)
    sessionStorage.removeItem(TOKEN_KEY)
  } else {
    sessionStorage.setItem(TOKEN_KEY, token)
    localStorage.removeItem(TOKEN_KEY)
  }
}

/**
 * 设置Token存储方式
 * @param {string} token - Token值
 * @param {boolean} remember - 是否记住
 */
export function setTokenStorage(token, remember = false) {
  setToken(token, remember)
}

/**
 * 删除Token
 */
export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REMEMBER_KEY)
}
