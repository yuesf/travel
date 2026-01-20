<template>
  <div class="miniprogram-layout">
    <el-container>
      <!-- 左侧菜单 -->
      <el-aside width="240px" class="sidebar-container">
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/miniprogram/home">
            <el-icon><HomeFilled /></el-icon>
            <template #title>首页配置</template>
          </el-menu-item>
          <el-menu-item index="/miniprogram/ads">
            <el-icon><Picture /></el-icon>
            <template #title>广告位配置</template>
          </el-menu-item>
          <el-menu-item index="/miniprogram/logo">
            <el-icon><PictureRounded /></el-icon>
            <template #title>Logo配置</template>
          </el-menu-item>
          <el-menu-item index="/miniprogram/statistics">
            <el-icon><DataAnalysis /></el-icon>
            <template #title>数据统计</template>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <!-- 右侧内容区域 -->
      <el-main class="content-main">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  HomeFilled,
  Picture,
  PictureRounded,
  DataAnalysis,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

// 计算当前激活的菜单项
const activeMenu = computed(() => {
  const { path } = route
  // 根据当前路由路径返回对应的菜单索引
  if (path === '/miniprogram' || path === '/miniprogram/home') {
    return '/miniprogram/home'
  }
  return path
})

// 处理菜单选择事件
const handleMenuSelect = (index) => {
  if (route.path !== index) {
    router.push(index)
  }
}
</script>

<style scoped>
.miniprogram-layout {
  height: 100%;
  background-color: #f0f2f5;
}

.sidebar-container {
  background-color: #fff;
  border-right: 1px solid #e4e7ed;
  height: calc(100vh - 60px);
  overflow-y: auto;
}

.sidebar-menu {
  border-right: none;
  height: 100%;
}

.content-main {
  padding: 20px;
  background-color: #f0f2f5;
  overflow-y: auto;
  height: calc(100vh - 60px);
}
</style>
