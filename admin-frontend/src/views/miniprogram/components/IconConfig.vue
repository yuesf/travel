<template>
  <div class="icon-config-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>Icon图标配置</span>
        </div>
      </template>
      <IconManager ref="iconManagerRef" v-model="icons" @change="handleIconChange" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import IconManager from '@/components/IconManager.vue'
import { getConfigByType } from '@/api/miniprogram'

const icons = ref([])
const iconManagerRef = ref(null)

// Icon图标变化
const handleIconChange = () => {
  // Icon图标变化处理
}

onMounted(() => {
  // 组件挂载时的初始化逻辑
})
</script>

<style scoped>
.icon-config-container {
  padding: 0;
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
