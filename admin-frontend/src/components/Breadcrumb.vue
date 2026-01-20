<template>
  <el-breadcrumb separator="/" class="breadcrumb-container">
    <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
    <el-breadcrumb-item v-for="item in breadcrumbList" :key="item.path" :to="item.path ? { path: item.path } : undefined">
      {{ item.title }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// 路由标题映射
const routeTitleMap = {
  '/dashboard': '首页',
  '/attractions': '景点列表',
  '/attractions/create': '创建景点',
  '/attractions/edit': '编辑景点',
  '/hotels': '酒店列表',
  '/hotels/create': '创建酒店',
  '/hotels/edit': '编辑酒店',
  '/products': '商品列表',
  '/products/create': '创建商品',
  '/products/edit': '编辑商品',
  '/categories': '分类管理',
  '/coupons': '优惠券管理',
  '/coupons/create': '创建优惠券',
  '/coupons/edit': '编辑优惠券',
  '/orders': '订单管理',
  '/users': '用户管理',
  '/miniprogram': '小程序管理',
  '/system/admins': '管理员管理',
  '/system/roles': '角色权限',
  '/system/logs': '操作日志',
}

const breadcrumbList = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  const list = []
  
  matched.forEach((item, index) => {
    const path = item.path
    const title = item.meta.title || routeTitleMap[path] || '未知'
    
    // 如果是最后一个，不需要链接
    if (index === matched.length - 1) {
      list.push({ title, path: null })
    } else {
      list.push({ title, path })
    }
  })
  
  // 处理动态路由（如编辑页面）
  if (route.params.id && route.meta.title) {
    const lastItem = list[list.length - 1]
    if (lastItem) {
      lastItem.title = route.meta.title
    }
  }
  
  return list
})
</script>

<style scoped>
.breadcrumb-container {
  margin-bottom: 16px;
}
</style>
