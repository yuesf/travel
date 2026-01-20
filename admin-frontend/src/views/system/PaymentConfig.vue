<template>
  <div class="payment-config-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>支付配置</span>
          <el-alert
            type="warning"
            :closable="false"
            show-icon
            style="margin-left: 10px;"
          >
            <template #default>
              <span style="font-size: 12px;">配置修改后立即生效，请谨慎操作</span>
            </template>
          </el-alert>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="140px"
        class="payment-config-form"
      >
        <el-form-item label="小程序AppID" prop="appId">
          <el-input
            v-model="formData.appId"
            placeholder="请输入小程序AppID（必须以wx开头）"
            :disabled="isEdit"
            style="width: 400px"
          />
          <div class="form-tip">
            <el-icon><InfoFilled /></el-icon>
            <span>AppID必须与小程序实际AppID一致，否则支付会失败</span>
          </div>
        </el-form-item>

        <el-form-item label="商户号" prop="mchId">
          <el-input
            v-model="formData.mchId"
            placeholder="请输入微信支付商户号"
            style="width: 400px"
          />
        </el-form-item>

        <el-form-item label="API密钥" prop="apiKey">
          <el-input
            v-model="formData.apiKey"
            type="password"
            placeholder="请输入API密钥"
            show-password
            style="width: 400px"
          />
          <div class="form-tip" v-if="isEdit && formData.apiKey && formData.apiKey.startsWith('****')">
            <el-icon><InfoFilled /></el-icon>
            <span>如需更新API密钥，请输入新密钥；否则保持原值</span>
          </div>
        </el-form-item>

        <el-form-item label="支付回调地址" prop="notifyUrl">
          <el-input
            v-model="formData.notifyUrl"
            placeholder="请输入支付回调地址（完整URL）"
            style="width: 500px"
          />
          <div class="form-tip">
            <el-icon><InfoFilled /></el-icon>
            <span>支付成功后微信会调用此地址通知支付结果</span>
          </div>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
          <div class="form-tip">
            <el-icon><WarningFilled /></el-icon>
            <span>禁用后，小程序将无法使用支付功能</span>
          </div>
        </el-form-item>

        <el-form-item label="描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入配置描述（可选）"
            style="width: 500px"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEdit ? '更新配置' : '创建配置' }}
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
import { InfoFilled, WarningFilled } from '@element-plus/icons-vue'
import {
  getPaymentConfig,
  createPaymentConfig,
  updatePaymentConfig,
} from '@/api/paymentConfig'

const formRef = ref(null)
const submitting = ref(false)
const isEdit = ref(false)
const configId = ref(null)

const formData = reactive({
  appId: '',
  mchId: '',
  apiKey: '',
  notifyUrl: '',
  status: 1,
  description: '',
})

const formRules = {
  appId: [
    { required: true, message: '请输入AppID', trigger: 'blur' },
    { pattern: /^wx[a-zA-Z0-9]+$/, message: 'AppID格式不正确，必须以wx开头', trigger: 'blur' },
  ],
  mchId: [
    { required: true, message: '请输入商户号', trigger: 'blur' },
  ],
  apiKey: [
    { required: true, message: '请输入API密钥', trigger: 'blur' },
  ],
  notifyUrl: [
    { required: true, message: '请输入支付回调地址', trigger: 'blur' },
    { type: 'url', message: '请输入有效的URL地址', trigger: 'blur' },
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' },
  ],
}

// 加载配置
const loadConfig = async () => {
  try {
    const res = await getPaymentConfig()
    if (res.code === 200 && res.data) {
      isEdit.value = true
      configId.value = res.data.id
      Object.assign(formData, {
        appId: res.data.appId,
        mchId: res.data.mchId,
        apiKey: res.data.apiKey || '', // 如果是部分隐藏的值，保持原值
        notifyUrl: res.data.notifyUrl,
        status: res.data.status,
        description: res.data.description || '',
      })
    } else {
      isEdit.value = false
      configId.value = null
    }
  } catch (error) {
    console.error('加载配置失败:', error)
    isEdit.value = false
    configId.value = null
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    submitting.value = true
    
    const data = {
      appId: formData.appId,
      mchId: formData.mchId,
      apiKey: formData.apiKey,
      notifyUrl: formData.notifyUrl,
      status: formData.status,
      description: formData.description,
    }
    
    if (isEdit.value) {
      // 如果API密钥以****开头，说明是部分隐藏的值，不更新
      if (data.apiKey && data.apiKey.startsWith('****')) {
        delete data.apiKey
      }
      await updatePaymentConfig(configId.value, data)
      ElMessage.success('更新配置成功')
    } else {
      await createPaymentConfig(data)
      ElMessage.success('创建配置成功')
    }
    
    // 重新加载配置
    await loadConfig()
  } catch (error) {
    if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error(isEdit.value ? '更新配置失败' : '创建配置失败')
    }
  } finally {
    submitting.value = false
  }
}

// 重置表单
const handleReset = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  if (isEdit.value) {
    loadConfig()
  } else {
    Object.assign(formData, {
      appId: '',
      mchId: '',
      apiKey: '',
      notifyUrl: '',
      status: 1,
      description: '',
    })
  }
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.payment-config-container {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.payment-config-form {
  padding: 20px 0;
}

.form-tip {
  margin-top: 4px;
  color: #909399;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.form-tip .el-icon {
  font-size: 14px;
}
</style>
