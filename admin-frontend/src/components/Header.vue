<template>
  <div class="header-container">
    <div class="header-left">
      <div class="logo">
        <span class="logo-text">旅游平台</span>
      </div>
    </div>
    <div class="header-right">
      <el-dropdown @command="handleCommand" class="user-dropdown">
        <span class="user-info">
          <el-avatar :size="32" :src="userAvatar" class="avatar">
            <el-icon><User /></el-icon>
          </el-avatar>
          <span class="username">{{ userInfo?.realName || userInfo?.username || '管理员' }}</span>
          <el-icon class="el-icon--right"><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人信息</el-dropdown-item>
            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

const userInfo = computed(() => authStore.userInfo)
const userAvatar = computed(() => userInfo.value?.avatar || '')

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
      await authStore.userLogout()
      ElMessage.success('退出成功')
      router.push('/login')
    } catch (error) {
      // 用户取消
    }
  } else if (command === 'profile') {
    // 跳转到个人信息页面（后续实现）
    ElMessage.info('个人信息功能待实现')
  }
}
</script>

<style scoped>
.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background-color: #409eff;
  color: white;
  height: 60px;
  line-height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.logo-text {
  color: white;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  color: white;
  cursor: pointer;
}

.avatar {
  margin-right: 8px;
  background-color: rgba(255, 255, 255, 0.3);
}

.username {
  margin-right: 8px;
  font-size: 14px;
}
</style>
