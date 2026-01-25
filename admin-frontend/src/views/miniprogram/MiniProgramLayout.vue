<template>
  <div class="miniprogram-layout">
    <router-view />
  </div>
</template>

<script setup>
// 简化布局，直接显示内容，不需要中间菜单
</script>

<style scoped>
.miniprogram-layout {
  height: 100%;
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
