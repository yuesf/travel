<template>
  <div class="image-upload-container">
    <el-upload
      ref="uploadRef"
      :action="uploadUrl"
      :headers="uploadHeaders"
      list-type="picture-card"
      :file-list="fileList"
      :on-preview="handlePreview"
      :on-remove="handleRemove"
      :on-success="handleSuccess"
      :on-error="handleError"
      :before-upload="beforeUpload"
      :limit="limit"
      :on-exceed="handleExceed"
      :disabled="disabled"
      accept="image/jpeg,image/jpg,image/png,image/webp"
    >
      <el-icon><Plus /></el-icon>
    </el-upload>

    <!-- 图片预览对话框 -->
    <el-dialog v-model="previewVisible" title="图片预览" width="800px">
      <img 
        :src="previewImageUrl" 
        style="width: 100%;" 
        alt="预览"
        @error="handleImageError"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import Sortable from 'sortablejs'
import { getToken } from '@/utils/auth'

const props = defineProps({
  modelValue: {
    type: [Array, String],
    default: () => [],
  },
  limit: {
    type: Number,
    default: 9,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  maxSize: {
    type: Number,
    default: 5, // MB
  },
})

const emit = defineEmits(['update:modelValue', 'change'])

const uploadUrl = computed(() => {
  return '/api/v1/common/file/upload/image'
})

const uploadHeaders = computed(() => {
  const token = getToken()
  return {
    Authorization: `Bearer ${token}`,
  }
})

const uploadRef = ref(null)
const fileList = ref([])
const previewVisible = ref(false)
const previewImageUrl = ref('')
let sortableInstance = null

// 同步外部值到内部列表
watch(
  () => props.modelValue,
  (newVal) => {
    if (props.limit === 1) {
      // 单文件模式：modelValue是字符串
      if (newVal && typeof newVal === 'string' && newVal.trim() !== '') {
        fileList.value = [{
          uid: 'image-1',
          name: 'image.jpg',
          url: newVal,
          status: 'success',
        }]
      } else if (Array.isArray(newVal) && newVal.length > 0) {
        // 兼容数组格式
        fileList.value = [{
          uid: 'image-1',
          name: 'image.jpg',
          url: newVal[0],
          status: 'success',
        }]
      } else {
        fileList.value = []
      }
    } else {
      // 多文件模式：modelValue是数组
      if (Array.isArray(newVal)) {
        fileList.value = newVal
          .filter(url => url && typeof url === 'string' && url.trim() !== '')
          .map((url, index) => ({
            uid: `image-${index}`,
            name: `image-${index}.jpg`,
            url: url,
            status: 'success',
          }))
      } else if (typeof newVal === 'string' && newVal.trim() !== '') {
        // 兼容字符串格式（单个图片URL）
        fileList.value = [{
          uid: 'image-0',
          name: 'image.jpg',
          url: newVal,
          status: 'success',
        }]
      } else {
        fileList.value = []
      }
    }
  },
  { immediate: true, deep: true }
)

// 同步内部列表到外部
const updateValue = () => {
  const urls = fileList.value
    .filter((file) => file.status === 'success')
    .map((file) => file.url || file.response?.data?.url || file.response?.data)
    .filter(Boolean)

  if (props.limit === 1) {
    // 单文件模式：返回字符串
    const value = urls.length > 0 ? urls[0] : ''
    emit('update:modelValue', value)
    emit('change', value)
  } else {
    // 多文件模式：返回数组（确保始终是数组）
    const value = Array.isArray(urls) ? urls : []
    emit('update:modelValue', value)
    emit('change', value)
  }
}

// 上传前检查
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isValidType = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'].includes(file.type)
  const isLtMaxSize = file.size / 1024 / 1024 < props.maxSize

  if (!isImage || !isValidType) {
    ElMessage.error('只能上传图片文件（JPG、PNG、WebP格式）')
    return false
  }

  if (!isLtMaxSize) {
    ElMessage.error(`图片大小不能超过 ${props.maxSize}MB`)
    return false
  }

  return true
}

// 上传成功
const handleSuccess = (response, file) => {
  console.log('上传成功响应:', response)
  if (response && response.code === 200) {
    const url = response.data?.url || response.data
    // 确保URL是字符串
    const imageUrl = typeof url === 'string' ? url : ''
    
    // 更新file对象
    file.url = imageUrl
    file.status = 'success'
    
    // 确保fileList中有这个文件
    const existingIndex = fileList.value.findIndex(item => item.uid === file.uid)
    if (existingIndex > -1) {
      fileList.value[existingIndex] = file
    } else {
      fileList.value.push(file)
    }
    
    // 如果是单文件模式，移除其他文件
    if (props.limit === 1 && fileList.value.length > 1) {
      fileList.value = [file]
    }
    
    updateValue()
    ElMessage.success('上传成功')
  } else {
    file.status = 'error'
    ElMessage.error(response?.message || '上传失败')
  }
}

// 上传失败
const handleError = (error, file) => {
  file.status = 'error'
  console.error('上传失败:', error)
  ElMessage.error('上传失败，请重试')
}

// 移除文件
const handleRemove = (file) => {
  const index = fileList.value.findIndex((item) => item.uid === file.uid)
  if (index > -1) {
    fileList.value.splice(index, 1)
    updateValue()
  }
}

// 预览图片
const handlePreview = (file) => {
  const url = file.url || file.response?.data?.url || file.response?.data || ''
  previewImageUrl.value = url
  previewVisible.value = true
}

// 图片加载错误
const handleImageError = (e) => {
  console.error('图片加载失败:', previewImageUrl.value)
  ElMessage.error('图片加载失败')
}

// 超出限制
const handleExceed = () => {
  ElMessage.warning(`最多只能上传 ${props.limit} 张图片`)
}

// 初始化拖拽排序
const initSortable = () => {
  if (props.limit === 1) return // 单文件模式不需要排序
  
  nextTick(() => {
    // 确保上传按钮可点击（在任何情况下都要执行）
    ensureUploadButtonClickable()
    
    // 查找图片列表容器（只包含已上传的图片，不包含上传按钮）
    const uploadListEl = uploadRef.value?.$el?.querySelector('.el-upload-list--picture-card')
    if (!uploadListEl || sortableInstance) {
      return
    }

    // 只在图片列表容器上初始化 Sortable，确保不影响上传按钮
    sortableInstance = Sortable.create(uploadListEl, {
      animation: 300,
      handle: '.el-upload-list__item', // 拖拽句柄
      draggable: '.el-upload-list__item', // 只允许拖拽列表项
      onEnd: (evt) => {
        const { oldIndex, newIndex } = evt
        if (oldIndex !== newIndex) {
          // 更新fileList顺序
          const movedItem = fileList.value.splice(oldIndex, 1)[0]
          fileList.value.splice(newIndex, 0, movedItem)
          updateValue()
        }
        // 拖拽结束后，确保上传按钮仍然可点击
        ensureUploadButtonClickable()
      },
    })
  })
}

// 确保上传按钮可点击
const ensureUploadButtonClickable = () => {
  nextTick(() => {
    if (!uploadRef.value?.$el) return
    
    const uploadBtn = uploadRef.value.$el.querySelector('.el-upload--picture-card')
    if (uploadBtn) {
      // 重置所有可能阻止点击的样式
      uploadBtn.style.pointerEvents = 'auto'
      uploadBtn.style.cursor = 'pointer'
      uploadBtn.style.zIndex = '10'
      
      // 确保内部的 input 元素可点击
      const input = uploadBtn.querySelector('input[type="file"]')
      if (input) {
        input.style.pointerEvents = 'auto'
        input.style.cursor = 'pointer'
      }
    }
  })
}

// 监听fileList变化，重新初始化排序
watch(
  () => fileList.value.length,
  () => {
    if (sortableInstance) {
      sortableInstance.destroy()
      sortableInstance = null
    }
    // 延迟初始化，确保 DOM 更新完成
    nextTick(() => {
      ensureUploadButtonClickable()
      initSortable()
    })
  },
  { flush: 'post' }
)

// 暴露方法供父组件调用，用于在 tab 切换时重新初始化拖拽排序
const refresh = () => {
  // 只重新初始化拖拽排序，不影响文件上传功能
  if (sortableInstance) {
    sortableInstance.destroy()
    sortableInstance = null
  }
  // 延迟初始化，确保 tab 切换后 DOM 已渲染
  nextTick(() => {
    ensureUploadButtonClickable()
    setTimeout(() => {
      ensureUploadButtonClickable()
      initSortable()
    }, 100)
  })
}

defineExpose({
  refresh
})

onMounted(() => {
  // 确保上传按钮可以点击
  nextTick(() => {
    ensureUploadButtonClickable()
    // 延迟初始化 Sortable，确保 el-upload 完全初始化
    // Chrome 浏览器需要更长的延迟
    setTimeout(() => {
      ensureUploadButtonClickable()
      initSortable()
      // 多次确保上传按钮可点击，解决 Chrome 兼容性问题
      setTimeout(() => {
        ensureUploadButtonClickable()
      }, 100)
      setTimeout(() => {
        ensureUploadButtonClickable()
      }, 300)
    }, 300)
  })
})

onBeforeUnmount(() => {
  if (sortableInstance) {
    sortableInstance.destroy()
    sortableInstance = null
  }
})
</script>

<style scoped>
.image-upload-container {
  width: 100%;
}

:deep(.el-upload-list--picture-card) {
  display: flex;
  flex-wrap: wrap;
}

:deep(.el-upload--picture-card) {
  width: 120px;
  height: 120px;
  line-height: 120px;
  cursor: pointer;
  pointer-events: auto !important;
  position: relative;
}

:deep(.el-upload--picture-card input[type="file"]) {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  opacity: 0;
  cursor: pointer;
  z-index: 11;
  pointer-events: auto !important;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 120px;
  height: 120px;
  margin-right: 8px;
  margin-bottom: 8px;
}
</style>
