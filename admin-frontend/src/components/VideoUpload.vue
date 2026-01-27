<template>
  <div class="video-upload-container">
    <div class="upload-actions" v-if="!disableUpload">
      <el-button type="primary" @click="handleSelectFromLibrary">
        从文件库选择
      </el-button>
    </div>
    
    <!-- 视频预览 -->
    <div v-if="videoUrl" class="video-preview">
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
    
    <div v-else class="empty-state">
      <el-empty description="请从文件库选择视频" :image-size="80" />
    </div>

    <!-- 文件库选择器 -->
    <FileSelector
      v-model="fileSelectorVisible"
      file-type="video"
      :multiple="false"
      :max-select="1"
      @select="handleFileSelect"
    />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import FileSelector from './FileSelector.vue'

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

const videoUrl = ref('')
const fileSelectorVisible = ref(false)

// 同步外部值到内部
watch(
  () => props.modelValue,
  (newVal) => {
    videoUrl.value = newVal || ''
  },
  { immediate: true }
)

// 移除文件
const handleRemove = () => {
  videoUrl.value = ''
  emit('update:modelValue', '')
  emit('change', '')
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

  // 视频只能选择一个
  const selectedFile = files[0]
  const fileUrl = selectedFile.fileUrl || selectedFile.previewUrl || selectedFile.url
  
  if (fileUrl) {
    videoUrl.value = fileUrl
    fileList.value = [{
      uid: 'video-1',
      name: selectedFile.originalName || 'video.mp4',
      url: fileUrl,
      status: 'success',
    }]
    
    emit('update:modelValue', fileUrl)
    emit('change', fileUrl)
    ElMessage.success('已选择视频')
  }
}
</script>

<style scoped>
.video-upload-container {
  width: 100%;
}

.video-preview {
  margin-top: 16px;
}

.video-item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.empty-state {
  margin-top: 20px;
}
</style>
