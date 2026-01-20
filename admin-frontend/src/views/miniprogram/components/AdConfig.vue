<template>
  <div class="ad-config-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>广告位列表</span>
          <el-button type="primary" size="small" @click="handleAddAd" :icon="Plus">
            添加广告位
          </el-button>
        </div>
      </template>
      <el-table
        :data="adList"
        v-loading="loading"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="广告图片" width="150">
          <template #default="{ row }">
            <el-image
              :src="getAdImage(row)"
              fit="cover"
              style="width: 120px; height: 80px; border-radius: 4px;"
              :preview-src-list="[getAdImage(row)]"
              preview-teleported
            />
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="configKey" label="配置键" min-width="150" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEditAd(row)">
              编辑
            </el-button>
            <el-button link type="danger" size="small" @click="handleDeleteAd(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑广告位对话框 -->
    <el-dialog
      v-model="adDialogVisible"
      :title="adDialogTitle"
      width="600px"
      @close="handleAdDialogClose"
    >
      <el-form
        ref="adFormRef"
        :model="adFormData"
        :rules="adFormRules"
        label-width="100px"
      >
        <el-form-item label="广告图片" prop="image">
          <ImageUpload
            v-model="adFormData.image"
            :limit="1"
            :max-size="5"
          />
        </el-form-item>
        <el-form-item label="跳转链接" prop="link">
          <el-input v-model="adFormData.link" placeholder="请输入跳转链接（可选）" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="adFormData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入广告描述"
          />
        </el-form-item>
        <el-form-item label="配置键" prop="configKey">
          <el-input
            v-model="adFormData.configKey"
            placeholder="请输入配置键（唯一标识）"
            :disabled="editingAdId !== null"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="adFormData.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="adFormData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitAd" :loading="adSubmitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import ImageUpload from '@/components/ImageUpload.vue'
import {
  getConfigByType,
  createConfig,
  updateConfig,
  deleteConfig,
} from '@/api/miniprogram'

const loading = ref(false)
const adList = ref([])
const adDialogVisible = ref(false)
const adDialogTitle = ref('添加广告位')
const adSubmitting = ref(false)
const adFormRef = ref(null)
const editingAdId = ref(null)

// 广告表单数据
const adFormData = reactive({
  image: '',
  link: '',
  description: '',
  configKey: '',
  sort: 0,
  status: 1,
})

// 广告表单验证规则
const adFormRules = {
  image: [{ required: true, message: '请上传广告图片', trigger: 'change' }],
  configKey: [{ required: true, message: '请输入配置键', trigger: 'blur' }],
}

// 加载广告位列表
const loadAdList = async () => {
  loading.value = true
  try {
    const res = await getConfigByType('AD', { status: null })
    if (res.data) {
      adList.value = res.data.sort((a, b) => (a.sort || 0) - (b.sort || 0))
    }
  } catch (error) {
    console.error('加载广告位列表失败:', error)
    ElMessage.error('加载广告位列表失败')
  } finally {
    loading.value = false
  }
}

// 获取广告图片
const getAdImage = (row) => {
  try {
    const configValue = typeof row.configValue === 'string'
      ? JSON.parse(row.configValue)
      : row.configValue
    return configValue?.image || ''
  } catch (e) {
    return ''
  }
}

// 添加广告位
const handleAddAd = () => {
  editingAdId.value = null
  adDialogTitle.value = '添加广告位'
  resetAdForm()
  adDialogVisible.value = true
}

// 编辑广告位
const handleEditAd = (row) => {
  editingAdId.value = row.id
  adDialogTitle.value = '编辑广告位'
  
  try {
    const configValue = typeof row.configValue === 'string'
      ? JSON.parse(row.configValue)
      : row.configValue
    
    adFormData.image = configValue?.image || ''
    adFormData.link = configValue?.link || ''
    adFormData.description = row.description || ''
    adFormData.configKey = row.configKey || ''
    adFormData.sort = row.sort || 0
    adFormData.status = row.status !== undefined ? row.status : 1
  } catch (e) {
    console.error('解析广告配置失败:', e)
  }
  
  adDialogVisible.value = true
}

// 删除广告位
const handleDeleteAd = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除广告位"${row.description || row.configKey}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    loading.value = true
    try {
      await deleteConfig(row.id)
      ElMessage.success('删除成功')
      await loadAdList()
    } catch (error) {
      console.error('删除广告位失败:', error)
      ElMessage.error('删除广告位失败')
    } finally {
      loading.value = false
    }
  } catch (error) {
    // 用户取消
  }
}

// 提交广告表单
const handleSubmitAd = async () => {
  if (!adFormRef.value) return

  try {
    await adFormRef.value.validate()
    adSubmitting.value = true

    const configValue = {
      image: adFormData.image,
      link: adFormData.link || '',
    }

    if (editingAdId.value !== null) {
      // 编辑
      await updateConfig(editingAdId.value, {
        configValue: JSON.stringify(configValue),
        description: adFormData.description,
        sort: adFormData.sort,
        status: adFormData.status,
      })
      ElMessage.success('更新成功')
    } else {
      // 添加
      await createConfig({
        configKey: adFormData.configKey,
        configType: 'AD',
        configValue: JSON.stringify(configValue),
        description: adFormData.description,
        sort: adFormData.sort,
        status: adFormData.status,
      })
      ElMessage.success('添加成功')
    }

    adDialogVisible.value = false
    await loadAdList()
  } catch (error) {
    if (error !== false) {
      console.error('提交失败:', error)
      ElMessage.error('提交失败')
    }
  } finally {
    adSubmitting.value = false
  }
}

// 重置广告表单
const resetAdForm = () => {
  adFormData.image = ''
  adFormData.link = ''
  adFormData.description = ''
  adFormData.configKey = ''
  adFormData.sort = adList.value.length > 0
    ? Math.max(...adList.value.map(item => item.sort || 0)) + 1
    : 1
  adFormData.status = 1
  if (adFormRef.value) {
    adFormRef.value.clearValidate()
  }
}

// 广告对话框关闭
const handleAdDialogClose = () => {
  resetAdForm()
  editingAdId.value = null
}

onMounted(() => {
  loadAdList()
})
</script>

<style scoped>
.ad-config-container {
  padding: 0;
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
