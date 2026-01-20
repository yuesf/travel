<template>
  <div class="banner-manager">
    <div class="banner-header">
      <el-button type="primary" @click="handleAdd" :icon="Plus">添加轮播图</el-button>
    </div>

    <!-- 轮播图列表 -->
    <el-table
      :data="bannerList"
      v-loading="loading"
      stripe
      border
      row-key="id"
      class="banner-table"
    >
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column label="排序" width="120" align="center">
        <template #default="{ row, $index }">
          <el-button
            link
            type="primary"
            size="small"
            :icon="ArrowUp"
            :disabled="$index === 0"
            @click="handleMoveUp($index)"
            title="上移"
          />
          <el-button
            link
            type="primary"
            size="small"
            :icon="ArrowDown"
            :disabled="$index === bannerList.length - 1"
            @click="handleMoveDown($index)"
            title="下移"
          />
        </template>
      </el-table-column>
      <el-table-column label="类型" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getBannerType(row) === 'video' ? 'warning' : 'success'">
            {{ getBannerType(row) === 'video' ? '视频' : '图片' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="预览" width="150">
        <template #default="{ row }">
          <div v-if="getBannerType(row) === 'video'" class="video-preview-cell">
            <video
              :src="getBannerVideo(row)"
              style="width: 120px; height: 80px; border-radius: 4px; object-fit: cover;"
              controls
            >
              您的浏览器不支持视频播放
            </video>
          </div>
          <el-image
            v-else
            :src="getBannerImage(row)"
            fit="cover"
            style="width: 120px; height: 80px; border-radius: 4px;"
            :preview-src-list="[getBannerImage(row)]"
            preview-teleported
            :lazy="false"
          >
            <template #error>
              <div class="image-slot">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
        </template>
      </el-table-column>
      <el-table-column label="标题" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">
          {{ getBannerTitle(row) }}
        </template>
      </el-table-column>
      <el-table-column label="跳转链接" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          {{ getBannerLink(row) }}
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row, $index }">
          <el-button link type="primary" size="small" @click="handleEdit(row, $index)">
            编辑
          </el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row, $index)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入轮播图标题" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="formData.type">
            <el-radio value="image">图片</el-radio>
            <el-radio value="video">视频</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item 
          v-if="formData.type === 'image'" 
          label="图片" 
          prop="image"
        >
          <ImageUpload
            v-model="formData.image"
            :limit="1"
            :max-size="5"
          />
        </el-form-item>
        <el-form-item 
          v-if="formData.type === 'video'" 
          label="视频" 
          prop="video"
        >
          <VideoUpload
            v-model="formData.video"
            :limit="1"
            :max-size="50"
          />
        </el-form-item>
        <el-form-item label="跳转链接" prop="link">
          <el-input v-model="formData.link" placeholder="请输入跳转链接（可选）" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ArrowUp, ArrowDown, Picture } from '@element-plus/icons-vue'
import ImageUpload from './ImageUpload.vue'
import VideoUpload from './VideoUpload.vue'
import {
  getConfigByType,
  createConfig,
  updateConfig,
  deleteConfig,
} from '@/api/miniprogram'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['update:modelValue', 'change'])

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('添加轮播图')
const submitting = ref(false)
const formRef = ref(null)
const bannerList = ref([])
const editingIndex = ref(-1)

// 表单数据
const formData = reactive({
  title: '',
  type: 'image', // 'image' 或 'video'
  image: '',
  video: '',
  link: '',
  sort: 0,
  status: 1,
})

// 表单验证规则
const formRules = {
  title: [{ required: true, message: '请输入轮播图标题', trigger: 'blur' }],
  image: [
    {
      validator: (rule, value, callback) => {
        if (formData.type === 'image' && !value) {
          callback(new Error('请上传图片'))
        } else {
          callback()
        }
      },
      trigger: 'change',
    },
  ],
  video: [
    {
      validator: (rule, value, callback) => {
        if (formData.type === 'video' && !value) {
          callback(new Error('请上传视频'))
        } else {
          callback()
        }
      },
      trigger: 'change',
    },
  ],
}

// 获取轮播图类型
const getBannerType = (row) => {
  try {
    const configValue = typeof row.configValue === 'string' 
      ? JSON.parse(row.configValue) 
      : row.configValue
    return configValue?.type || 'image'
  } catch (e) {
    return 'image'
  }
}

// 获取轮播图图片
const getBannerImage = (row) => {
  try {
    const configValue = typeof row.configValue === 'string' 
      ? JSON.parse(row.configValue) 
      : row.configValue
    return configValue?.image || ''
  } catch (e) {
    return ''
  }
}

// 获取轮播图视频
const getBannerVideo = (row) => {
  try {
    const configValue = typeof row.configValue === 'string' 
      ? JSON.parse(row.configValue) 
      : row.configValue
    return configValue?.video || ''
  } catch (e) {
    return ''
  }
}

// 加载轮播图列表
const loadBanners = async () => {
  loading.value = true
  try {
    const res = await getConfigByType('BANNER', { status: null })
    if (res.data) {
      bannerList.value = res.data.sort((a, b) => (a.sort || 0) - (b.sort || 0))
      emit('update:modelValue', bannerList.value)
      emit('change', bannerList.value)
    }
  } catch (error) {
    console.error('加载轮播图列表失败:', error)
    ElMessage.error('加载轮播图列表失败')
  } finally {
    loading.value = false
  }
}

// 上移
const handleMoveUp = async (index) => {
  if (index === 0) return
  
  const item = bannerList.value[index]
  const prevItem = bannerList.value[index - 1]
  
  // 交换排序值
  const tempSort = item.sort
  item.sort = prevItem.sort
  prevItem.sort = tempSort
  
  // 更新列表顺序
  bannerList.value.splice(index - 1, 2, item, prevItem)
  
  // 更新数据库
  try {
    await Promise.all([
      updateConfig(item.id, { sort: item.sort }),
      updateConfig(prevItem.id, { sort: prevItem.sort }),
    ])
    ElMessage.success('排序更新成功')
    emit('update:modelValue', bannerList.value)
    emit('change', bannerList.value)
  } catch (error) {
    console.error('更新排序失败:', error)
    ElMessage.error('更新排序失败')
    await loadBanners()
  }
}

// 下移
const handleMoveDown = async (index) => {
  if (index === bannerList.value.length - 1) return
  
  const item = bannerList.value[index]
  const nextItem = bannerList.value[index + 1]
  
  // 交换排序值
  const tempSort = item.sort
  item.sort = nextItem.sort
  nextItem.sort = tempSort
  
  // 更新列表顺序
  bannerList.value.splice(index, 2, nextItem, item)
  
  // 更新数据库
  try {
    await Promise.all([
      updateConfig(item.id, { sort: item.sort }),
      updateConfig(nextItem.id, { sort: nextItem.sort }),
    ])
    ElMessage.success('排序更新成功')
    emit('update:modelValue', bannerList.value)
    emit('change', bannerList.value)
  } catch (error) {
    console.error('更新排序失败:', error)
    ElMessage.error('更新排序失败')
    await loadBanners()
  }
}

// 添加
const handleAdd = () => {
  editingIndex.value = -1
  dialogTitle.value = '添加轮播图'
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row, index) => {
  editingIndex.value = index
  dialogTitle.value = '编辑轮播图'
  
  try {
    const configValue = typeof row.configValue === 'string'
      ? JSON.parse(row.configValue)
      : row.configValue
    
    formData.title = configValue?.title || ''
    formData.type = configValue?.type || 'image'
    formData.image = configValue?.image || ''
    formData.video = configValue?.video || ''
    formData.link = configValue?.link || ''
    formData.sort = row.sort || 0
    formData.status = row.status !== undefined ? row.status : 1
  } catch (e) {
    console.error('解析配置值失败:', e)
  }
  
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row, index) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除轮播图"${getBannerTitle(row)}"吗？`,
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
      await loadBanners()
    } catch (error) {
      console.error('删除轮播图失败:', error)
      ElMessage.error('删除轮播图失败')
    } finally {
      loading.value = false
    }
  } catch (error) {
    // 用户取消
  }
}

// 获取轮播图标题
const getBannerTitle = (row) => {
  try {
    const configValue = typeof row.configValue === 'string'
      ? JSON.parse(row.configValue)
      : row.configValue
    return configValue?.title || '未命名'
  } catch (e) {
    return '未命名'
  }
}

// 获取轮播图跳转链接
const getBannerLink = (row) => {
  try {
    const configValue = typeof row.configValue === 'string'
      ? JSON.parse(row.configValue)
      : row.configValue
    return configValue?.link || '-'
  } catch (e) {
    return '-'
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    const configValue = {
      title: formData.title,
      type: formData.type,
      image: formData.type === 'image' ? formData.image : '',
      video: formData.type === 'video' ? formData.video : '',
      link: formData.link || '',
    }

    if (editingIndex.value >= 0) {
      // 编辑
      const row = bannerList.value[editingIndex.value]
      await updateConfig(row.id, {
        configValue: JSON.stringify(configValue),
        sort: formData.sort,
        status: formData.status,
      })
      ElMessage.success('更新成功')
    } else {
      // 添加
      const maxSort = bannerList.value.length > 0
        ? Math.max(...bannerList.value.map(item => item.sort || 0))
        : 0
      
      await createConfig({
        configKey: `BANNER_${Date.now()}`,
        configType: 'BANNER',
        configValue: JSON.stringify(configValue),
        description: `轮播图：${formData.title}`,
        sort: formData.sort || maxSort + 1,
        status: formData.status,
      })
      ElMessage.success('添加成功')
    }

    dialogVisible.value = false
    await loadBanners()
  } catch (error) {
    if (error !== false) {
      console.error('提交失败:', error)
      ElMessage.error('提交失败')
    }
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  formData.title = ''
  formData.type = 'image'
  formData.image = ''
  formData.video = ''
  formData.link = ''
  formData.sort = bannerList.value.length > 0
    ? Math.max(...bannerList.value.map(item => item.sort || 0)) + 1
    : 1
  formData.status = 1
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 监听类型变化，清空对应的上传内容
watch(() => formData.type, (newType) => {
  if (newType === 'image') {
    formData.video = ''
  } else {
    formData.image = ''
  }
  if (formRef.value) {
    formRef.value.clearValidate(['image', 'video'])
  }
})

// 对话框关闭
const handleDialogClose = () => {
  resetForm()
  editingIndex.value = -1
}

onMounted(() => {
  loadBanners()
})

// 暴露方法供父组件调用
defineExpose({
  loadBanners,
})
</script>

<style scoped>
.banner-manager {
  width: 100%;
}

.banner-header {
  margin-bottom: 16px;
}

.banner-table {
  width: 100%;
}

.video-preview-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-preview-cell video {
  cursor: pointer;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 30px;
}

</style>
