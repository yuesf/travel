<template>
  <el-aside :width="isCollapse ? '64px' : '240px'" class="sidebar-container">
    <el-menu
      :default-active="activeMenu"
      :collapse="isCollapse"
      :collapse-transition="false"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409eff"
      class="sidebar-menu"
      @select="handleMenuSelect"
    >
      <el-menu-item index="/dashboard">
        <el-icon><Odometer /></el-icon>
        <template #title>首页</template>
      </el-menu-item>

      <el-menu-item index="/miniprogram">
        <el-icon><Grid /></el-icon>
        <template #title>小程序管理</template>
      </el-menu-item>

      <el-sub-menu index="products">
        <template #title>
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </template>
        <el-menu-item index="/products">商品列表</el-menu-item>
        <el-menu-item index="/categories">分类管理</el-menu-item>
        <el-menu-item index="/products/create">创建商品</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="articles">
        <template #title>
          <el-icon><Document /></el-icon>
          <span>文章管理</span>
        </template>
        <el-menu-item index="/articles">文章列表</el-menu-item>
        <el-menu-item index="/articles/create">创建文章</el-menu-item>
        <el-menu-item index="/articles/categories">文章分类</el-menu-item>
        <el-menu-item index="/articles/tags">文章标签</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="attractions">
        <template #title>
          <el-icon><Location /></el-icon>
          <span>景点管理</span>
        </template>
        <el-menu-item index="/attractions">景点列表</el-menu-item>
        <el-menu-item index="/attractions/create">创建景点</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="hotels">
        <template #title>
          <el-icon><OfficeBuilding /></el-icon>
          <span>酒店管理</span>
        </template>
        <el-menu-item index="/hotels">酒店列表</el-menu-item>
        <el-menu-item index="/hotels/create">创建酒店</el-menu-item>
      </el-sub-menu>

      <el-menu-item index="/coupons">
        <el-icon><Ticket /></el-icon>
        <template #title>优惠券管理</template>
      </el-menu-item>

      <el-menu-item index="/orders">
        <el-icon><Document /></el-icon>
        <template #title>订单管理</template>
      </el-menu-item>

      <el-menu-item index="/users">
        <el-icon><User /></el-icon>
        <template #title>用户管理</template>
      </el-menu-item>

      <el-sub-menu index="system">
        <template #title>
          <el-icon><Setting /></el-icon>
          <span>系统管理</span>
        </template>
        <el-menu-item index="/system/payment-config">支付配置</el-menu-item>
        <el-menu-item index="/system/merchant-config">商家配置</el-menu-item>
        <el-menu-item index="/system/admins">管理员管理</el-menu-item>
        <el-menu-item index="/system/roles">角色权限</el-menu-item>
        <el-menu-item index="/system/logs">操作日志</el-menu-item>
      </el-sub-menu>
    </el-menu>
  </el-aside>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Odometer,
  Location,
  OfficeBuilding,
  Goods,
  Ticket,
  Document,
  User,
  Grid,
  Setting,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)

// 处理菜单选择事件，使用 Vue Router 进行导航
const handleMenuSelect = (index) => {
  // 如果点击的是当前路由，不进行导航
  if (route.path === index) {
    return
  }
  // 使用 router.push 进行导航，避免页面刷新
  router.push(index).catch((err) => {
    // 忽略重复导航的错误
    if (err.name !== 'NavigationDuplicated') {
      console.error('路由导航错误:', err)
    }
  })
}

const activeMenu = computed(() => {
  const { path } = route
  if (path.startsWith('/attractions')) {
    if (path === '/attractions' || path === '/attractions/create' || path.includes('/attractions/edit')) {
      return path
    }
    return '/attractions'
  }
  if (path.startsWith('/hotels')) {
    if (path === '/hotels' || path === '/hotels/create' || path.includes('/hotels/edit')) {
      return path
    }
    return '/hotels'
  }
  if (path.startsWith('/products')) {
    if (path === '/products' || path === '/products/create' || path.includes('/products/edit')) {
      return path
    }
    return '/products'
  }
  if (path.startsWith('/categories')) {
    return '/categories'
  }
  if (path.startsWith('/articles')) {
    if (path === '/articles' || path === '/articles/create' || 
        path.includes('/articles/edit') || path.includes('/articles/') ||
        path === '/articles/categories' || path === '/articles/tags') {
      // 排除编辑和创建路径，其他都返回文章列表路径
      if (path.includes('/articles/edit') || path === '/articles/create') {
        return path
      }
      // 详情页面也返回列表路径以激活菜单
      return '/articles'
    }
    return '/articles'
  }
  if (path.startsWith('/system')) {
    return path
  }
  return path
})
</script>

<style scoped>
.sidebar-container {
  background-color: #304156;
  transition: width 0.28s;
  overflow: hidden;
}

.sidebar-menu {
  border-right: none;
  height: 100%;
  overflow-y: auto;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 240px;
}
</style>
