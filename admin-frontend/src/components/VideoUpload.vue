<template>
  <div class="video-upload-container">
    <el-upload
      v-if="!disableUpload"
      :action="uploadUrl"
      :headers="uploadHeaders"
      :file-list="fileList"
      :on-success="handleSuccess"
      :on-error="handleError"
      :on-progress="handleProgress"
      :before-upload="beforeUpload"
      :limit="limit"
      :on-exceed="handleExceed"
      :disabled="disabled"
      :on-remove="handleRemove"
      accept="video/mp4"
      :show-file-list="true"
    >
      <el-button type="primary" :icon="Upload">上传视频</el-button>
      <template #tip>
        <div class="el-upload__tip">
          只能上传MP4格式的视频，文件大小不超过{{ maxSize }}MB
        </div>
      </template>
    </el-upload>
    
    <!-- 当禁用上传时，只显示已选择的视频 -->
    <div v-else-if="videoUrl" class="video-display">
      <div class="video-item">
        <video
          :src="videoUrl"
          controls
          style="width: 100%; max-width: 500px;"
        >
          您的浏览器不支持视频播放
        </video>
        <el-button
          type="danger"
          size="small"
          style="margin-top: 8px;"
          @click="handleRemove"
        >
          删除
        </el-button>
      </div>
    </div>

    <!-- 上传进度条 -->
    <div v-if="uploadProgress > 0 && uploadProgress < 100" class="upload-progress">
      <el-progress
        :percentage="uploadProgress"
        :status="uploadProgress === 100 ? 'success' : undefined"
        :stroke-width="8"
      />
      <div class="progress-text">上传中... {{ uploadProgress }}%</div>
    </div>

    <!-- 视频预览 -->
    <div v-if="videoUrl" class="video-preview">
      <video
        :src="videoUrl"
        controls
        style="width: 100%; max-width: 500px; margin-top: 16px;"
      >
        您的浏览器不支持视频播放
      </video>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { getToken } from '@/utils/auth'
import { API_BASE_URL } from '@/config/api'

const props = defineProps({
  modelValue: {
    type: String,
    default: '',
  },
  limit: {
    type: Number,
    default: 1,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  maxSize: {
    type: Number,
    default: 50, // MB
  },
  // 是否禁用上传（只显示和删除，不允许上传新文件）
  disableUpload: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:modelValue', 'change'])

// 上传视频地址：基于统一的 API_BASE_URL 拼接
// 开发环境：/api/v1/common/file/upload/video
// 生产环境：/travel/api/v1/common/file/upload/video
const uploadUrl = computed(() => {
  const baseUrl = API_BASE_URL.replace(/\/$/, '')
  return `${baseUrl}/common/file/upload/video`
})

const uploadHeaders = computed(() => {
  const token = getToken()
  return {
    Authorization: `Bearer ${token}`,
  }
})

const fileList = ref([])
const videoUrl = ref('')
const uploadProgress = ref(0)

// 同步外部值到内部
watch(
  () => props.modelValue,
  (newVal) => {
    videoUrl.value = newVal || ''
    if (newVal) {
      fileList.value = [{
        uid: 'video-1',
        name: 'video.mp4',
        url: newVal,
        status: 'success',
      }]
    } else {
      fileList.value = []
    }
  },
  { immediate: true }
)

// 上传前检查
const beforeUpload = (file) => {
  const isVideo = file.type.startsWith('video/')
  const isValidType = file.type === 'video/mp4'
  const isLtMaxSize = file.size / 1024 / 1024 < props.maxSize

  if (!isVideo || !isValidType) {
    ElMessage.error('只能上传MP4格式的视频文件')
    return false
  }

  if (!isLtMaxSize) {
    ElMessage.error(`视频大小不能超过 ${props.maxSize}MB`)
    return false
  }

  // 重置进度
  uploadProgress.value = 0
  return true
}

// 上传进度
const handleProgress = (event, file) => {
  uploadProgress.value = Math.round((event.loaded / event.total) * 100)
}

// 上传成功
const handleSuccess = (response, file) => {
  uploadProgress.value = 100
  if (response && response.code === 200) {
    const url = response.data?.url || response.data
    videoUrl.value = url
    file.url = url
    file.status = 'success'
    emit('update:modelValue', url)
    emit('change', url)
    ElMessage.success('上传成功')
    // 延迟重置进度条
    setTimeout(() => {
      uploadProgress.value = 0
    }, 1000)
  } else {
    file.status = 'error'
    uploadProgress.value = 0
    ElMessage.error(response?.message || '上传失败')
  }
}

// 上传失败
const handleError = (error, file) => {
  file.status = 'error'
  uploadProgress.value = 0
  console.error('上传失败:', error)
  ElMessage.error('上传失败，请重试')
}

// 移除文件
const handleRemove = () => {
  videoUrl.value = ''
  fileList.value = []
  uploadProgress.value = 0
  emit('update:modelValue', '')
  emit('change', '')
}

// 超出限制
const handleExceed = () => {
  ElMessage.warning(`最多只能上传 ${props.limit} 个视频`)
}
</script>

<style scoped>
.video-upload-container {
  width: 100%;
}

.upload-progress {
  margin-top: 16px;
}

.progress-text {
  margin-top: 8px;
  text-align: center;
  color: #606266;
  font-size: 14px;
}

.video-preview {
  margin-top: 16px;
}

:deep(.el-upload__tip) {
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
}
</style>
