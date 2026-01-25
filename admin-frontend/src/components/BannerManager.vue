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
            :preview-src-list="getPreviewImageList(row)"
            preview-teleported
            :lazy="false"
            @error="() => handleBannerImageError(row, $event)"
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
          <div style="display: flex; align-items: flex-start; gap: 12px;">
            <ImageUpload
              v-model="formData.image"
              :limit="1"
              :max-size="5"
              :disable-upload="true"
            />
            <el-button 
              type="info" 
              :icon="Folder"
              @click="handleSelectFromFileList('image')"
            >
              从文件库选择
            </el-button>
          </div>
        </el-form-item>
        <el-form-item 
          v-if="formData.type === 'video'" 
          label="视频" 
          prop="video"
        >
          <div style="display: flex; align-items: flex-start; gap: 12px;">
            <VideoUpload
              v-model="formData.video"
              :limit="1"
              :max-size="50"
              :disable-upload="true"
            />
            <el-button 
              type="info" 
              :icon="Folder"
              @click="handleSelectFromFileList('video')"
            >
              从文件库选择
            </el-button>
          </div>
        </el-form-item>
        <!-- 新的链接配置 -->
        <LinkTypeSelector v-model="linkConfig" />
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

    <!-- 文件选择器对话框 -->
    <FileSelector
      v-model="fileSelectorVisible"
      :file-type="fileSelectorType"
      :multiple="false"
      :max-select="1"
      @select="handleFileSelect"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ArrowUp, ArrowDown, Picture, Folder } from '@element-plus/icons-vue'
import ImageUpload from './ImageUpload.vue'
import VideoUpload from './VideoUpload.vue'
import LinkTypeSelector from './LinkTypeSelector.vue'
import FileSelector from './FileSelector.vue'
import {
  getConfigByType,
  createConfig,
  updateConfig,
  deleteConfig,
} from '@/api/miniprogram'
import { getSignedUrlByUrl } from '@/api/file'

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
const fileSelectorVisible = ref(false)
const fileSelectorType = ref('image') // 'image' 或 'video'

// 表单数据
const formData = reactive({
  title: '',
  type: 'image', // 'image' 或 'video'
  image: '',
  video: '',
  link: '', // 保留以兼容旧数据
  linkType: 'NONE',
  linkValue: '',
  linkDisplay: '',
  sort: 0,
  status: 1,
})

// 链接配置计算属性（用于双向绑定 LinkTypeSelector）
const linkConfig = computed({
  get: () => ({
    linkType: formData.linkType,
    linkValue: formData.linkValue,
    linkDisplay: formData.linkDisplay,
  }),
  set: (value) => {
    formData.linkType = value.linkType
    formData.linkValue = value.linkValue
    formData.linkDisplay = value.linkDisplay
  },
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

/**
 * 判断是否为OSS URL（未签名的）
 */
const isUnsignedOssUrl = (url) => {
  if (!url || typeof url !== 'string') {
    return false
  }
  // 检查是否是OSS URL（包含oss-*.aliyuncs.com等）
  const ossPatterns = [
    /oss-.*\.aliyuncs\.com/i,
    /\.oss\./i,
    /\.qcloud\.com/i,
    /\.amazonaws\.com/i,
    /\.cos\./i
  ]
  
  // 如果是签名URL（包含查询参数），则不是未签名的
  if (url.includes('?') && (url.includes('Expires=') || url.includes('Signature='))) {
    return false
  }
  
  // 检查是否匹配OSS URL模式
  return ossPatterns.some(pattern => pattern.test(url))
}

// 签名URL缓存（避免重复请求）
const signedUrlCache = new Map()

/**
 * 为OSS URL获取签名URL（用于预览）
 */
const getSignedUrlForPreview = async (url) => {
  if (!isUnsignedOssUrl(url)) {
    return url // 不是未签名的OSS URL，直接返回
  }
  
  // 检查缓存
  if (signedUrlCache.has(url)) {
    return signedUrlCache.get(url)
  }
  
  try {
    const response = await getSignedUrlByUrl(url)
    if (response && response.code === 200 && response.data) {
      // 缓存签名URL（1小时后过期）
      signedUrlCache.set(url, response.data)
      setTimeout(() => {
        signedUrlCache.delete(url)
      }, 3600000) // 1小时
      return response.data
    }
  } catch (error) {
    console.warn('获取签名URL失败:', error)
  }
  
  // 如果获取失败，返回原URL（可能会403，但至少不会报错）
  return url
}

// 获取轮播图图片（自动处理OSS URL签名）
const getBannerImage = (row) => {
  try {
    const configValue = typeof row.configValue === 'string' 
      ? JSON.parse(row.configValue) 
      : row.configValue
    const imageUrl = configValue?.image || ''
    
    // 如果是未签名的OSS URL，返回缓存中的签名URL或原URL
    if (imageUrl && isUnsignedOssUrl(imageUrl)) {
      return signedUrlCache.get(imageUrl) || imageUrl
    }
    
    return imageUrl
  } catch (e) {
    return ''
  }
}

// 获取预览用的图片URL列表（带签名URL处理）
const getPreviewImageList = (row) => {
  const imageUrl = getBannerImage(row)
  return imageUrl ? [imageUrl] : []
}

// 处理图片加载错误
const handleBannerImageError = async (row, event) => {
  console.error('图片加载失败:', row)
  
  try {
    const configValue = typeof row.configValue === 'string' 
      ? JSON.parse(row.configValue) 
      : row.configValue
    const imageUrl = configValue?.image || ''
    
    // 如果是未签名的OSS URL，尝试获取签名URL
    if (imageUrl && isUnsignedOssUrl(imageUrl)) {
      const signedUrl = await getSignedUrlForPreview(imageUrl)
      if (signedUrl !== imageUrl) {
        // 更新缓存，触发重新渲染
        signedUrlCache.set(imageUrl, signedUrl)
        // 强制更新列表以触发重新渲染
        bannerList.value = [...bannerList.value]
      }
    }
  } catch (error) {
    console.error('处理图片加载错误失败:', error)
    ElMessage.warning('图片加载失败，请刷新页面重试')
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

// 注意：后端统一返回签名URL，不再需要批量预加载签名URL

// 加载轮播图列表
const loadBanners = async () => {
  loading.value = true
  try {
    const res = await getConfigByType('BANNER', { status: null })
    if (res.data) {
      bannerList.value = res.data.sort((a, b) => (a.sort || 0) - (b.sort || 0))
      emit('update:modelValue', bannerList.value)
      emit('change', bannerList.value)
      
      // 批量预加载所有未签名OSS图片的签名URL
      const imageUrls = bannerList.value
        .map(item => {
          try {
            const configValue = typeof item.configValue === 'string' 
              ? JSON.parse(item.configValue) 
              : item.configValue
            return configValue?.image || ''
          } catch (e) {
            return ''
          }
        })
        .filter(url => url && isUnsignedOssUrl(url))
      
      // 异步获取所有未签名URL的签名URL
      if (imageUrls.length > 0) {
        Promise.all(imageUrls.map(url => getSignedUrlForPreview(url)))
          .then(() => {
            // 获取完成后，强制更新列表以触发重新渲染
            bannerList.value = [...bannerList.value]
          })
          .catch(error => {
            console.warn('批量获取签名URL失败:', error)
          })
      }
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
    
    // 加载链接配置（兼容旧数据）
    if (configValue?.linkType) {
      // 新格式数据
      formData.linkType = configValue.linkType
      formData.linkValue = configValue.linkValue || ''
      formData.linkDisplay = configValue.linkDisplay || ''
    } else if (configValue?.link) {
      // 旧格式数据：有 link 字段，视为外部链接
      formData.linkType = 'EXTERNAL'
      formData.linkValue = configValue.link
      formData.linkDisplay = configValue.link
    } else {
      // 无链接配置
      formData.linkType = 'NONE'
      formData.linkValue = ''
      formData.linkDisplay = ''
    }
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

// 获取轮播图跳转链接配置摘要
const getBannerLink = (row) => {
  try {
    const configValue = typeof row.configValue === 'string'
      ? JSON.parse(row.configValue)
      : row.configValue
    
    // 链接类型标签映射
    const linkTypeLabels = {
      NONE: '无跳转',
      EXTERNAL: '外部链接',
      PRODUCT_CATEGORY: '商品分类',
      PRODUCT: '商品',
      ARTICLE_CATEGORY: '文章分类',
      ARTICLE: '文章',
      ATTRACTION: '景点',
      HOTEL: '酒店',
      MAP: '地图',
    }
    
    // 优先使用新格式
    if (configValue?.linkType) {
      const typeLabel = linkTypeLabels[configValue.linkType] || '未知类型'
      if (configValue.linkType === 'NONE') {
        return typeLabel
      }
      const display = configValue.linkDisplay || configValue.linkValue || '-'
      return `${typeLabel}：${display}`
    }
    
    // 兼容旧格式
    if (configValue?.link) {
      return `外部链接：${configValue.link}`
    }
    
    return '无跳转'
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
      link: formData.link || '', // 保留以兼容旧版本
      linkType: formData.linkType,
      linkValue: formData.linkValue,
      linkDisplay: formData.linkDisplay,
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
  formData.linkType = 'NONE'
  formData.linkValue = ''
  formData.linkDisplay = ''
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

// 打开文件选择器
const handleSelectFromFileList = (type) => {
  fileSelectorType.value = type
  fileSelectorVisible.value = true
}

// 处理文件选择
const handleFileSelect = async (files) => {
  if (files && files.length > 0) {
    const selectedFile = files[0]
    // 优先使用原始URL（fileUrl），用于保存到数据库
    // 如果fileUrl不存在，使用url（兼容旧数据）
    const originalUrl = selectedFile.fileUrl || selectedFile.url || ''
    
    if (!originalUrl) {
      ElMessage.error('选择的文件没有URL')
      return
    }
    
    // 如果是OSS文件，需要获取签名URL用于预览
    let previewUrl = originalUrl
    if (selectedFile.storageType === 'OSS') {
      // 优先使用文件库返回的previewUrl（签名URL）
      if (selectedFile.previewUrl) {
        previewUrl = selectedFile.previewUrl
      } else {
        // 如果没有previewUrl，调用接口获取签名URL
        try {
          const response = await getSignedUrlByUrl(originalUrl)
          if (response && response.code === 200 && response.data) {
            previewUrl = response.data
          }
        } catch (error) {
          console.warn('获取签名URL失败:', error)
          // 如果获取失败，使用原始URL（可能会403，但至少不会报错）
          previewUrl = originalUrl
        }
      }
    }
    
    if (fileSelectorType.value === 'image') {
      // 保存原始URL到formData（用于保存到数据库）
      // ImageUpload组件会自动检测OSS URL并获取签名URL用于预览
      formData.image = originalUrl
      // 触发表单验证
      if (formRef.value) {
        formRef.value.validateField('image')
      }
    } else if (fileSelectorType.value === 'video') {
      // 保存原始URL到formData（用于保存到数据库）
      formData.video = originalUrl
      // 触发表单验证
      if (formRef.value) {
        formRef.value.validateField('video')
      }
    }
    
    ElMessage.success('已选择文件')
  }
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
