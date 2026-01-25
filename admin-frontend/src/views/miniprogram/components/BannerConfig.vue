<template>
  <div class="banner-config-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>轮播图管理</span>
        </div>
      </template>
      <BannerManager ref="bannerManagerRef" v-model="banners" @change="handleBannerChange" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import BannerManager from '@/components/BannerManager.vue'
import { getConfigByType } from '@/api/miniprogram'

const banners = ref([])
const bannerManagerRef = ref(null)

// 轮播图变化
const handleBannerChange = () => {
  // 轮播图变化处理
}

onMounted(() => {
  // 组件挂载时的初始化逻辑
})
</script>

<style scoped>
.banner-config-container {
  padding: 0;
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
