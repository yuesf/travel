<template>
  <div class="oss-config-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>阿里云OSS配置</span>
          <el-button
            v-if="hasConfig"
            type="danger"
            size="small"
            @click="handleDelete"
            :loading="deleting"
          >
            删除配置
          </el-button>
        </div>
      </template>

      <el-alert
        title="配置说明"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      >
        <p>1. 配置阿里云OSS后，图片和视频将自动上传到OSS存储，提升访问速度</p>
        <p>2. 如未配置或配置未启用，系统将自动使用本地存储</p>
        <p>3. Access Key Secret将加密存储，保证安全性</p>
        <p>4. 配置后请先测试连接，确保配置正确</p>
      </el-alert>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="160px"
        class="oss-config-form"
      >
        <el-form-item label="OSS Endpoint" prop="endpoint">
          <el-input
            v-model="formData.endpoint"
            placeholder="如：oss-cn-hangzhou.aliyuncs.com"
            style="width: 500px"
          />
          <div class="form-item-tip">
            请输入OSS的Endpoint地址，不包含https://前缀
          </div>
        </el-form-item>

        <el-form-item label="Access Key ID" prop="accessKeyId">
          <el-input
            v-model="formData.accessKeyId"
            placeholder="请输入Access Key ID"
            style="width: 500px"
          />
        </el-form-item>

        <el-form-item label="Access Key Secret" prop="accessKeySecret">
          <el-input
            v-model="formData.accessKeySecret"
            type="password"
            placeholder="请输入Access Key Secret"
            style="width: 500px"
            show-password
          />
          <div class="form-item-tip" v-if="hasConfig">
            如不修改密钥，请留空
          </div>
        </el-form-item>

        <el-form-item label="Bucket名称" prop="bucketName">
          <el-input
            v-model="formData.bucketName"
            placeholder="请输入Bucket名称"
            style="width: 500px"
          />
        </el-form-item>

        <el-form-item label="是否启用">
          <el-switch
            v-model="formData.enabled"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            保存配置
          </el-button>
          <el-button @click="handleTest" :loading="testing">
            测试连接
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getOssConfig,
  saveOrUpdateOssConfig,
  testOssConnection,
  deleteOssConfig,
} from '@/api/oss'

const formRef = ref(null)
const submitting = ref(false)
const testing = ref(false)
const deleting = ref(false)
const hasConfig = ref(false)

const formData = reactive({
  endpoint: '',
  accessKeyId: '',
  accessKeySecret: '',
  bucketName: '',
  enabled: true,
})

const formRules = {
  endpoint: [
    { required: true, message: '请输入OSS Endpoint', trigger: 'blur' },
  ],
  accessKeyId: [
    { required: true, message: '请输入Access Key ID', trigger: 'blur' },
  ],
  accessKeySecret: [
    {
      validator: (rule, value, callback) => {
        if (!hasConfig.value && !value) {
          callback(new Error('请输入Access Key Secret'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  bucketName: [
    { required: true, message: '请输入Bucket名称', trigger: 'blur' },
  ],
}

// 加载配置
const loadConfig = async () => {
  try {
    const res = await getOssConfig()
    if (res.data) {
      hasConfig.value = true
      Object.assign(formData, {
        endpoint: res.data.endpoint || '',
        accessKeyId: res.data.accessKeyId || '',
        accessKeySecret: '', // 不显示已有密钥
        bucketName: res.data.bucketName || '',
        enabled: res.data.enabled ?? true,
      })
    }
  } catch (error) {
    console.error('加载OSS配置失败:', error)
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    const data = {
      endpoint: formData.endpoint,
      accessKeyId: formData.accessKeyId,
      bucketName: formData.bucketName,
      enabled: formData.enabled,
    }

    // 只有输入了新密钥才传递
    if (formData.accessKeySecret) {
      data.accessKeySecret = formData.accessKeySecret
    }

    await saveOrUpdateOssConfig(data)
    ElMessage.success('保存配置成功')
    
    // 重新加载配置
    await loadConfig()
    
    // 清空密码输入框
    formData.accessKeySecret = ''
  } catch (error) {
    if (error !== false) {
      console.error('保存配置失败:', error)
      ElMessage.error(error.message || '保存配置失败')
    }
  } finally {
    submitting.value = false
  }
}

// 测试连接
const handleTest = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    testing.value = true

    const data = {
      endpoint: formData.endpoint,
      accessKeyId: formData.accessKeyId,
      accessKeySecret: formData.accessKeySecret || '********',
      bucketName: formData.bucketName,
      enabled: formData.enabled,
    }

    await testOssConnection(data)
    ElMessage.success('OSS连接测试成功')
  } catch (error) {
    if (error !== false) {
      console.error('测试连接失败:', error)
      ElMessage.error(error.message || 'OSS连接测试失败，请检查配置')
    }
  } finally {
    testing.value = false
  }
}

// 重置表单
const handleReset = () => {
  if (hasConfig.value) {
    loadConfig()
  } else {
    formRef.value?.resetFields()
  }
}

// 删除配置
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(
      '删除配置后，系统将自动使用本地存储。确定要删除OSS配置吗？',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    deleting.value = true
    await deleteOssConfig()
    ElMessage.success('删除配置成功')
    
    // 重置表单
    hasConfig.value = false
    formRef.value?.resetFields()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除配置失败:', error)
      ElMessage.error('删除配置失败')
    }
  } finally {
    deleting.value = false
  }
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.oss-config-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.oss-config-form {
  margin-top: 20px;
}

.form-item-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>
