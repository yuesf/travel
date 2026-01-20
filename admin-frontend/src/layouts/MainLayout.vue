<template>
  <div class="main-layout">
    <el-container>
      <el-header class="header-wrapper">
        <Header />
      </el-header>
      <el-container>
        <Sidebar />
        <el-main class="main-content">
          <Breadcrumb />
          <div class="content-wrapper">
            <router-view />
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import Header from '@/components/Header.vue'
import Sidebar from '@/components/Sidebar.vue'
import Breadcrumb from '@/components/Breadcrumb.vue'
</script>

<style scoped>
.main-layout {
  height: 100vh;
  overflow: hidden;
}

.header-wrapper {
  padding: 0;
  height: 60px;
  line-height: 60px;
}

.main-content {
  padding: 20px;
  background-color: #f0f2f5;
  overflow-y: auto;
  height: calc(100vh - 60px);
}

.content-wrapper {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
  min-height: calc(100vh - 140px);
  overflow-y: visible;
}
</style>
