import { ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'

/**
 * 路由守卫
 */
export function setupRouterGuards(router) {
  // 全局前置守卫
  router.beforeEach(async (to, from, next) => {
    const token = getToken()

    // 设置页面标题
    if (to.meta && to.meta.title) {
      document.title = `${to.meta.title} - 旅游平台后台管理系统`
    } else {
      document.title = '旅游平台后台管理系统'
    }

    // 不需要登录的页面（白名单）
    const whiteList = ['/login']
    
    if (whiteList.includes(to.path)) {
      // 已登录用户访问登录页，跳转到首页
      if (token) {
        next({ path: '/dashboard', replace: true })
      } else {
        next()
      }
      return
    }

    // 需要登录的页面
    if (!token) {
      // 未登录，跳转到登录页，并保存要访问的路径
      next({
        path: '/login',
        query: { redirect: to.fullPath },
        replace: true,
      })
      return
    }

    // 已登录，检查权限
    if (to.meta && to.meta.requiresAuth) {
      // TODO: 实现权限检查逻辑
      // import { hasPermission } from '@/utils/permission'
      // if (to.meta.permission && !hasPermission(to.meta.permission)) {
      //   ElMessage.warning('无权限访问该页面')
      //   next({ path: '/403', replace: true })
      //   return
      // }
    }

    next()
  })

  // 全局后置守卫
  router.afterEach((to, from) => {
    // 可以在这里做一些页面统计等操作
  })

  // 全局错误处理
  router.onError((error) => {
    console.error('路由错误:', error)
    console.error('错误堆栈:', error.stack)
    console.error('错误消息:', error.message)
    ElMessage.error(`页面加载失败: ${error.message || '未知错误'}，请查看控制台获取详细信息`)
  })
}
