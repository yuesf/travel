<template>
  <div class="merchant-config-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>商家配置</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="140px"
        class="merchant-config-form"
      >
        <el-form-item label="商家名称" prop="merchantName">
          <el-input
            v-model="formData.merchantName"
            placeholder="请输入商家名称"
            style="width: 400px"
          />
        </el-form-item>

        <el-form-item label="联系电话" prop="contactPhone">
          <el-input
            v-model="formData.contactPhone"
            placeholder="请输入联系电话（11位手机号）"
            style="width: 400px"
          />
        </el-form-item>

        <el-form-item label="联系邮箱" prop="contactEmail">
          <el-input
            v-model="formData.contactEmail"
            placeholder="请输入联系邮箱"
            style="width: 400px"
          />
        </el-form-item>

        <el-form-item label="商家地址">
          <el-input
            v-model="formData.address"
            type="textarea"
            :rows="2"
            placeholder="请输入商家地址（可选）"
            style="width: 500px"
          />
        </el-form-item>

        <el-form-item label="商家描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请输入商家描述（可选）"
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
import { ElMessage } from 'element-plus'
import {
  getMerchantConfig,
  createMerchantConfig,
  updateMerchantConfigDirect,
} from '@/api/merchantConfig'

const formRef = ref(null)
const submitting = ref(false)
const isEdit = ref(false)
const configId = ref(null)

const formData = reactive({
  merchantName: '',
  contactPhone: '',
  contactEmail: '',
  address: '',
  description: '',
})

const formRules = {
  merchantName: [
    { required: true, message: '请输入商家名称', trigger: 'blur' },
  ],
  contactPhone: [
    { pattern: /^1[3-9]\d{9}$/, message: '联系电话格式不正确，请输入11位手机号', trigger: 'blur' },
  ],
  contactEmail: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
}

// 加载配置
const loadConfig = async () => {
  try {
    const res = await getMerchantConfig()
    if (res.code === 200 && res.data) {
      isEdit.value = true
      configId.value = res.data.id
      Object.assign(formData, {
        merchantName: res.data.merchantName || '',
        contactPhone: res.data.contactPhone || '',
        contactEmail: res.data.contactEmail || '',
        address: res.data.address || '',
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
      merchantName: formData.merchantName,
      contactPhone: formData.contactPhone || undefined,
      contactEmail: formData.contactEmail || undefined,
      address: formData.address || undefined,
      description: formData.description || undefined,
    }
    
    if (isEdit.value) {
      await updateMerchantConfigDirect(data)
      ElMessage.success('更新配置成功')
    } else {
      await createMerchantConfig(data)
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
      merchantName: '',
      contactPhone: '',
      contactEmail: '',
      address: '',
      description: '',
    })
  }
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.merchant-config-container {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.merchant-config-form {
  padding: 20px 0;
}
</style>
