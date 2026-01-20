import { useAuthStore } from '@/store/auth'

/**
 * 检查用户是否有指定权限
 * @param {string|Array} permission - 权限代码或权限代码数组
 * @returns {boolean}
 */
export function hasPermission(permission) {
  const authStore = useAuthStore()
  const userInfo = authStore.userInfo

  if (!userInfo) {
    return false
  }

  // 超级管理员拥有所有权限
  if (userInfo.roleCode === 'SUPER_ADMIN') {
    return true
  }

  // 获取用户权限列表（从用户信息中获取）
  const permissions = userInfo.permissions || []

  if (Array.isArray(permission)) {
    // 多个权限，需要全部拥有
    return permission.every(perm => permissions.includes(perm))
  } else {
    // 单个权限
    return permissions.includes(permission)
  }
}

/**
 * 检查用户是否有指定角色
 * @param {string|Array} role - 角色代码或角色代码数组
 * @returns {boolean}
 */
export function hasRole(role) {
  const authStore = useAuthStore()
  const userInfo = authStore.userInfo

  if (!userInfo) {
    return false
  }

  const userRole = userInfo.roleCode || userInfo.role?.code

  if (Array.isArray(role)) {
    return role.includes(userRole)
  } else {
    return userRole === role
  }
}

/**
 * 权限指令（用于v-has指令）
 */
export const permissionDirective = {
  mounted(el, binding) {
    const { value } = binding
    if (value) {
      const hasPerm = hasPermission(value)
      if (!hasPerm) {
        el.style.display = 'none'
        // 或者直接移除元素
        // el.parentNode && el.parentNode.removeChild(el)
      }
    }
  },
  updated(el, binding) {
    const { value } = binding
    if (value) {
      const hasPerm = hasPermission(value)
      if (hasPerm) {
        el.style.display = ''
      } else {
        el.style.display = 'none'
      }
    }
  },
}
