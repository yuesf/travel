<template>
  <div class="logo-config-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>小程序 Logo 配置</span>
        </div>
      </template>
      <el-form :model="logoForm" label-width="120px">
        <el-form-item label="Logo 图片">
          <ImageUpload
            v-model="logoForm.logoUrl"
            :limit="1"
            :max-size="2"
          />
          <div style="margin-top: 8px; color: #909399; font-size: 12px;">
            支持 PNG、JPG、JPEG 格式，建议尺寸 512x512 像素，最大 2MB
          </div>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSaveLogo" :loading="savingLogo">
            保存配置
          </el-button>
          <el-button @click="handleDeleteLogo" :disabled="!logoForm.logoUrl">
            删除 Logo（恢复默认）
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImageUpload from '@/components/ImageUpload.vue'
import {
  getConfigByKey,
  createConfig,
  updateConfig,
  deleteConfig,
} from '@/api/miniprogram'

const savingLogo = ref(false)

// Logo 配置表单
const logoForm = reactive({
  logoUrl: '',
})

// 加载 Logo 配置
const loadLogoConfig = async () => {
  try {
    const res = await getConfigByKey('MINIPROGRAM_LOGO')
    if (res.data && res.data.configValue) {
      try {
        const configValue = JSON.parse(res.data.configValue)
        logoForm.logoUrl = configValue.logoUrl || ''
      } catch (e) {
        console.error('解析 Logo 配置失败:', e)
      }
    }
  } catch (error) {
    // 配置不存在时，logoUrl 保持为空字符串
    console.log('Logo 配置不存在，使用默认值')
  }
}

// 保存 Logo 配置
const handleSaveLogo = async () => {
  if (!logoForm.logoUrl) {
    ElMessage.warning('请上传 Logo 图片')
    return
  }
  
  savingLogo.value = true
  try {
    const configValue = JSON.stringify({
      logoUrl: logoForm.logoUrl
    })
    
    // 尝试获取现有配置
    let existingConfig = null
    try {
      const res = await getConfigByKey('MINIPROGRAM_LOGO')
      existingConfig = res.data
    } catch (error) {
      // 配置不存在，需要创建
    }
    
    if (existingConfig) {
      // 更新配置
      await updateConfig(existingConfig.id, {
        configValue: configValue,
      })
    } else {
      // 创建配置
      await createConfig({
        configKey: 'MINIPROGRAM_LOGO',
        configType: 'SYSTEM',
        configValue: configValue,
        description: '小程序 Logo 配置',
        sort: 0,
        status: 1,
      })
    }
    
    ElMessage.success('保存成功')
  } catch (error) {
    console.error('保存 Logo 配置失败:', error)
    ElMessage.error('保存失败')
  } finally {
    savingLogo.value = false
  }
}

// 删除 Logo 配置
const handleDeleteLogo = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要删除 Logo 配置吗？删除后将使用默认 Logo。',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    try {
      const res = await getConfigByKey('MINIPROGRAM_LOGO')
      if (res.data) {
        await deleteConfig(res.data.id)
        logoForm.logoUrl = ''
        ElMessage.success('删除成功')
      }
    } catch (error) {
      // 配置不存在
      ElMessage.info('配置不存在')
    }
  } catch (error) {
    // 用户取消
  }
}

onMounted(() => {
  loadLogoConfig()
})
</script>

<style scoped>
.logo-config-container {
  padding: 0;
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
