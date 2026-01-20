<template>
  <div class="error-container">
    <div class="error-content">
      <div class="error-image">
        <img src="https://element-plus.org/images/element-plus-logo.svg" alt="403" />
      </div>
      <div class="error-info">
        <h1 class="error-code">403</h1>
        <h2 class="error-title">无权限访问</h2>
        <p class="error-description">抱歉，您没有权限访问该页面</p>
        <el-button type="primary" @click="goBack">返回上一页</el-button>
        <el-button @click="goHome">返回首页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.go(-1)
}

const goHome = () => {
  router.push('/dashboard')
}
</script>

<style scoped>
.error-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}

.error-content {
  text-align: center;
}

.error-image {
  margin-bottom: 40px;
}

.error-image img {
  width: 200px;
  height: 200px;
}

.error-code {
  font-size: 72px;
  font-weight: bold;
  color: #409eff;
  margin: 0;
}

.error-title {
  font-size: 24px;
  color: #303133;
  margin: 20px 0;
}

.error-description {
  font-size: 16px;
  color: #606266;
  margin-bottom: 40px;
}
</style>
