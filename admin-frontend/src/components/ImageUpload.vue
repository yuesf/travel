<template>
  <div class="image-upload-container">
    <div class="upload-actions" v-if="!disableUpload">
      <el-button type="primary" @click="handleSelectFromLibrary" style="margin-bottom: 10px">
        从文件库选择
      </el-button>
    </div>
    
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
      <el-icon v-if="!disableUpload"><Plus /></el-icon>
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

    <!-- 文件库选择器 -->
    <FileSelector
      v-model="fileSelectorVisible"
      file-type="image"
      :multiple="true"
      :max-select="limit"
      @select="handleFileSelect"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import Sortable from 'sortablejs'
import { getToken } from '@/utils/auth'
import { API_BASE_URL } from '@/config/api'
import FileSelector from './FileSelector.vue'
// OSS bucket已改为"私有写公有读"模式，不再需要获取签名URL

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
  compress: {
    type: Boolean,
    default: false,
  },
  compressSize: {
    type: Object,
    default: () => ({ width: 32, height: 32 }),
  },
  // 是否禁用上传（只显示和删除，不允许上传新文件）
  disableUpload: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:modelValue', 'change'])

// 上传图片地址：基于统一的 API_BASE_URL 拼接
// 开发环境：/api/v1/common/file/upload/image（通过 Vite 代理到 http://127.0.0.1）
// 生产环境：/travel/api/v1/common/file/upload/image（Nginx 转发到 https://yuesf.cn/travel/）
const uploadUrl = computed(() => {
  const baseUrl = API_BASE_URL.replace(/\/$/, '')
  return `${baseUrl}/common/file/upload/image`
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
const fileSelectorVisible = ref(false)
let sortableInstance = null
const isRemoving = ref(false) // 标记是否正在删除，防止 watch 重新同步
// 注意：OSS bucket已改为"私有写公有读"模式，后端直接返回公开URL，前端直接使用即可

// 同步外部值到内部列表
watch(
  () => props.modelValue,
  (newVal) => {
    // 如果正在删除，跳过同步，避免删除操作被覆盖
    if (isRemoving.value) {
      return
    }
    
    if (props.limit === 1) {
      // 单文件模式：modelValue是字符串
      if (newVal && typeof newVal === 'string' && newVal.trim() !== '') {
        const fileItem = {
          uid: 'image-1',
          name: 'image.jpg',
          url: newVal,
          status: 'success',
        }
        fileList.value = [fileItem]
      } else if (Array.isArray(newVal) && newVal.length > 0) {
        // 兼容数组格式
        const fileItem = {
          uid: 'image-1',
          name: 'image.jpg',
          url: newVal[0],
          status: 'success',
        }
        fileList.value = [fileItem]
      } else {
        fileList.value = []
      }
    } else {
      // 多文件模式：modelValue是数组
      if (Array.isArray(newVal)) {
        const urls = newVal.filter(url => url && typeof url === 'string' && url.trim() !== '')
        fileList.value = urls.map((url, index) => {
          const fileItem = {
            uid: `image-${index}`,
            name: `image-${index}.jpg`,
            url: url,
            status: 'success',
          }
          return fileItem
        })
      } else if (typeof newVal === 'string' && newVal.trim() !== '') {
        // 兼容字符串格式（单个图片URL）
        const fileItem = {
          uid: 'image-0',
          name: 'image.jpg',
          url: newVal,
          status: 'success',
        }
        fileList.value = [fileItem]
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
    .map((file) => {
      // 优先使用原始URL（如果存在），用于保存到数据库
      // 如果没有原始URL，则使用url（兼容旧数据）
      return file.originalUrl || file.url || file.response?.data?.url || file.response?.data
    })
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

// 压缩图片
const compressImage = (file, maxWidth = 32, maxHeight = 32, quality = 0.8) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = (e) => {
      const img = new Image()
      img.src = e.target.result
      img.onload = () => {
        const canvas = document.createElement('canvas')
        const ctx = canvas.getContext('2d')
        
        // 计算压缩后的尺寸（保持宽高比）
        let width = img.width
        let height = img.height
        if (width > height) {
          if (width > maxWidth) {
            height = (height * maxWidth) / width
            width = maxWidth
          }
        } else {
          if (height > maxHeight) {
            width = (width * maxHeight) / height
            height = maxHeight
          }
        }
        
        // 设置画布尺寸为目标尺寸
        canvas.width = maxWidth
        canvas.height = maxHeight
        
        // 居中绘制（如果原图不是正方形）
        const x = (maxWidth - width) / 2
        const y = (maxHeight - height) / 2
        
        // 填充白色背景（可选，如果需要）
        ctx.fillStyle = '#FFFFFF'
        ctx.fillRect(0, 0, maxWidth, maxHeight)
        
        // 绘制图片
        ctx.drawImage(img, x, y, width, height)
        
        // 转换为 Blob
        canvas.toBlob(
          (blob) => {
            if (blob) {
              // 将 Blob 转换为 File 对象
              const compressedFile = new File([blob], file.name, {
                type: 'image/jpeg',
                lastModified: Date.now(),
              })
              resolve(compressedFile)
            } else {
              reject(new Error('图片压缩失败'))
            }
          },
          'image/jpeg',
          quality
        )
      }
      img.onerror = () => {
        reject(new Error('图片加载失败'))
      }
    }
    reader.onerror = () => {
      reject(new Error('文件读取失败'))
    }
  })
}

// 上传前检查
const beforeUpload = async (file) => {
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

  // 如果需要压缩，先压缩图片
  if (props.compress) {
    try {
      const compressedFile = await compressImage(
        file,
        props.compressSize.width,
        props.compressSize.height,
        0.8
      )
      // 替换原文件为压缩后的文件
      // 注意：不能直接修改 File 对象的 uid，需要在返回前创建一个新对象
      // Element Plus 会自动处理 uid，所以直接返回压缩后的文件即可
      return compressedFile
    } catch (error) {
      console.error('图片压缩失败:', error)
      ElMessage.warning('图片压缩失败，将使用原图上传')
      return true
    }
  }

  return true
}

// 上传成功
const handleSuccess = (response, file) => {
  console.log('上传成功响应:', response)
  if (response && response.code === 200) {
    const url = response.data?.url || response.data
    // 确保URL是字符串，后端直接返回公开URL
    const fileUrl = typeof url === 'string' ? url : ''
    
    // OSS bucket已改为"私有写公有读"模式，后端直接返回公开URL
    // 直接使用返回的URL，无需提取原始URL
    const fileItem = {
      uid: file.uid,
      name: file.name,
      url: fileUrl, // 直接使用公开URL
      status: 'success',
      response: response,
    }
    
    // 确保fileList中有这个文件
    const existingIndex = fileList.value.findIndex(item => item.uid === file.uid)
    if (existingIndex > -1) {
      // 使用新对象替换，避免修改只读属性
      fileList.value[existingIndex] = fileItem
    } else {
      fileList.value.push(fileItem)
    }
    
    // 如果是单文件模式，移除其他文件
    if (props.limit === 1 && fileList.value.length > 1) {
      fileList.value = [fileItem]
    }
    
    // 更新值并触发验证
    updateValue()
    
    // 延迟触发验证，确保值已更新
    nextTick(() => {
      // 触发 change 事件以触发表单验证
      // 直接使用公开URL保存到数据库
      emit('change', props.limit === 1 ? fileUrl : [fileUrl])
    })
    
    ElMessage.success('上传成功')
  } else {
    // 创建错误状态的文件对象
    const fileItem = {
      uid: file.uid,
      name: file.name,
      status: 'error',
      response: response,
    }
    const existingIndex = fileList.value.findIndex(item => item.uid === file.uid)
    if (existingIndex > -1) {
      fileList.value[existingIndex] = fileItem
    }
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
  return new Promise((resolve) => {
    // 设置删除标志，防止 watch 重新同步
    isRemoving.value = true
    
    const index = fileList.value.findIndex((item) => item.uid === file.uid)
    if (index > -1) {
      fileList.value.splice(index, 1)
      updateValue()
    }
    
    // 等待 DOM 更新完成后再重置标志
    nextTick(() => {
      // 延迟重置标志，确保父组件的更新已经完成
      setTimeout(() => {
        isRemoving.value = false
        resolve(true)
      }, 100)
    })
  })
}

// 预览图片
const handlePreview = async (file) => {
  // 后端统一返回签名URL，直接使用即可
  const url = file.url || file.response?.data?.url || file.response?.data || ''
  previewImageUrl.value = url
  previewVisible.value = true
}

// 图片加载错误
const handleImageError = async (e) => {
  const currentUrl = previewImageUrl.value
  console.error('图片加载失败:', currentUrl)
  // 后端统一返回签名URL，如果加载失败可能是URL过期或其他原因
  ElMessage.error('图片加载失败，请刷新页面重试')
}

// 超出限制
const handleExceed = () => {
  ElMessage.warning(`最多只能上传 ${props.limit} 张图片`)
}

// 从文件库选择
const handleSelectFromLibrary = () => {
  fileSelectorVisible.value = true
}

// 处理文件库选择的文件
const handleFileSelect = (files) => {
  if (!files || files.length === 0) {
    return
  }

  // 获取当前已有的图片URL列表
  const currentUrls = Array.isArray(props.modelValue) 
    ? [...props.modelValue] 
    : (props.modelValue ? [props.modelValue] : [])

  // 添加新选择的文件URL
  const newUrls = files.map(file => file.fileUrl || file.previewUrl || file.url).filter(Boolean)
  
  // 合并并去重
  const allUrls = [...new Set([...currentUrls, ...newUrls])]
  
  // 检查是否超过限制
  if (allUrls.length > props.limit) {
    ElMessage.warning(`最多只能选择 ${props.limit} 张图片，已自动截取前 ${props.limit} 张`)
    allUrls.splice(props.limit)
  }

  // 更新值
  if (props.limit === 1) {
    emit('update:modelValue', allUrls[0] || '')
    emit('change', allUrls[0] || '')
  } else {
    emit('update:modelValue', allUrls)
    emit('change', allUrls)
  }

  ElMessage.success(`已添加 ${newUrls.length} 张图片`)
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
  // 如果禁用了上传，不处理上传按钮
  if (props.disableUpload) {
    return
  }
  
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

/* 确保删除按钮在非禁用状态下可见 */
:deep(.el-upload-list--picture-card .el-upload-list__item:not(.is-disabled) .el-upload-list__item-actions) {
  opacity: 0;
  transition: opacity var(--el-transition-duration);
}

:deep(.el-upload-list--picture-card .el-upload-list__item:not(.is-disabled):hover .el-upload-list__item-actions) {
  opacity: 1;
}

/* 当禁用上传时，隐藏上传按钮 */
:deep(.el-upload--picture-card:not(:has(.el-icon--plus))) {
  display: none !important;
}
</style>
