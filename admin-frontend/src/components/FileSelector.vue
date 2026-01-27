<template>
  <el-dialog
    v-model="visible"
    title="选择文件"
    width="1200px"
    :close-on-click-modal="false"
  >
    <!-- 筛选区域 -->
    <el-form :model="searchForm" :inline="true" class="search-form" style="margin-bottom: 20px">
      <el-form-item label="文件类型">
        <el-select v-model="searchForm.fileType" placeholder="全部" style="width: 120px">
          <el-option label="全部" value="all" />
          <el-option label="图片" value="image" />
          <el-option label="视频" value="video" />
        </el-select>
      </el-form-item>
      <el-form-item label="存储类型">
        <el-select v-model="searchForm.storageType" placeholder="全部" style="width: 120px">
          <el-option label="全部" value="all" />
          <el-option label="OSS" value="OSS" />
          <el-option label="本地" value="LOCAL" />
        </el-select>
      </el-form-item>
      <el-form-item label="文件名">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索文件名"
          clearable
          style="width: 200px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button type="success" :icon="Upload" @click="handleUploadClick" :loading="uploading">
          上传
        </el-button>
        <el-tooltip
          content="图片将自动压缩为 WebP 格式以优化文件大小"
          placement="top"
        >
          <el-icon style="margin-left: 8px; color: #909399; cursor: help;">
            <InfoFilled />
          </el-icon>
        </el-tooltip>
      </el-form-item>
    </el-form>

    <!-- 隐藏的上传组件 -->
    <el-upload
      ref="uploadRef"
      :auto-upload="false"
      :show-file-list="false"
      :on-change="handleFileChange"
      :before-upload="beforeUpload"
      multiple
      style="display: none;"
    >
    </el-upload>

    <!-- 上传进度提示 -->
    <el-dialog
      v-model="uploadProgressVisible"
      title="上传进度"
      width="500px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <div class="upload-progress-content">
        <div class="progress-info">
          <span>正在上传 {{ uploadProgress.current }} / {{ uploadProgress.total }} 个文件</span>
          <span class="progress-text">
            成功: {{ uploadProgress.success }} | 失败: {{ uploadProgress.failed }}
          </span>
        </div>
        <el-progress
          :percentage="uploadProgress.percentage"
          :status="uploadProgress.status"
          :stroke-width="8"
        />
        <div v-if="uploadProgress.currentFile" class="current-file">
          当前文件: {{ uploadProgress.currentFile }}
        </div>
      </div>
      <template #footer>
        <el-button
          v-if="uploadProgress.status === 'success' || uploadProgress.status === 'exception'"
          type="primary"
          @click="handleCloseUploadProgress"
        >
          确定
        </el-button>
        <el-button
          v-else
          :disabled="true"
        >
          上传中...
        </el-button>
      </template>
    </el-dialog>

    <!-- 左右分栏布局 -->
    <div class="file-selector-layout">
      <!-- 左侧目录树 -->
      <div class="directory-panel">
        <DirectoryTree
          :selected-directory-id="selectedDirectoryId"
          @select="handleDirectorySelect"
          @refresh="handleDirectoryRefresh"
        />
      </div>
      
      <!-- 右侧文件列表 -->
      <div class="file-panel">
        <!-- 文件网格 -->
        <div v-loading="loading" class="file-grid-container">
      <div
        v-for="file in fileList"
        :key="file.id"
        :data-file-id="file.id"
        class="file-item"
        :class="{ 'selected': isSelected(file) }"
        @click="handleSelectFile(file)"
        @dblclick="handleDoubleClick(file)"
      >
        <div class="file-preview">
          <el-image
            v-if="file.fileType === 'image'"
            :src="getImageUrl(file)"
            fit="cover"
            style="width: 100%; height: 100%; pointer-events: none;"
            @error="(e) => handleImageError(file, e)"
          >
            <template #error>
              <div class="image-error">
                <el-icon :size="40"><Picture /></el-icon>
                <div class="error-text">加载失败</div>
              </div>
            </template>
          </el-image>
          <div v-else-if="file.fileType === 'video'" class="video-icon">
            <el-icon :size="40"><VideoPlay /></el-icon>
          </div>
          <div v-else class="other-icon">
            <el-icon :size="40"><Document /></el-icon>
          </div>
        </div>
        <div class="file-info">
          <div class="file-name" :title="file.originalName">
            {{ file.originalName }}
          </div>
          <div class="file-meta">
            <el-tag v-if="file.storageType === 'OSS'" type="success" size="small">OSS</el-tag>
            <el-tag v-else type="info" size="small">本地</el-tag>
            <span class="file-size">{{ file.fileSizeFormatted }}</span>
          </div>
          <div class="file-hint">双击快速选择</div>
        </div>
        <div v-if="isSelected(file)" class="selected-badge">
          <el-icon><Check /></el-icon>
        </div>
      </div>

      <div v-if="fileList.length === 0" class="empty-state">
        <el-empty description="该目录暂无文件" />
      </div>
        </div>

            <!-- 分页 -->
        <div class="pagination-wrapper" style="margin-top: 20px">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[12, 24, 48, 96]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <span v-if="selectedFiles.length > 0" style="margin-right: auto; color: #606266">
          已选择 {{ selectedFiles.length }} 个文件
        </span>
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleConfirm" :disabled="selectedFiles.length === 0">
          确定
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, nextTick, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { VideoPlay, Document, Check, Picture, Upload, InfoFilled } from '@element-plus/icons-vue'
import { getFileList, getSignedUrl, uploadImage, uploadVideo } from '@/api/file'
import DirectoryTree from './DirectoryTree.vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  fileType: {
    type: String,
    default: 'all', // 'all', 'image', 'video'
  },
  multiple: {
    type: Boolean,
    default: false,
  },
  maxSelect: {
    type: Number,
    default: 9,
  },
})

const emit = defineEmits(['update:modelValue', 'select'])

const visible = ref(false)
const loading = ref(false)
const uploading = ref(false)
const fileList = ref([])
const selectedFiles = ref([])
const uploadRef = ref(null)
const selectedDirectoryId = ref(null)
const selectedDirectory = ref(null)
const uploadProgressVisible = ref(false)
const uploadProgress = reactive({
  total: 0,
  current: 0,
  success: 0,
  failed: 0,
  percentage: 0,
  status: 'success', // 'success' | 'exception' | 'active'
  currentFile: '',
})
const pendingFiles = ref([]) // 待上传的文件列表
let uploadTimer = null // 上传定时器，用于延迟批量上传

const searchForm = reactive({
  fileType: 'all',
  storageType: 'all',
  keyword: '',
})

const pagination = reactive({
  page: 1,
  pageSize: 12,
  total: 0,
})

// 监听外部visible变化
watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
    if (val) {
      // 对话框打开时重置选择和加载数据
      selectedFiles.value = []
      selectedDirectoryId.value = null
      selectedDirectory.value = null
      searchForm.fileType = props.fileType
      loadData()
    }
  },
  { immediate: true }
)

// 监听内部visible变化
watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 加载文件列表
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      page: pagination.page,
      pageSize: pagination.pageSize,
    }
    // 如果选中了目录，添加目录路径到查询参数
    if (selectedDirectory.value && selectedDirectory.value.path) {
      params.module = selectedDirectory.value.path
    }
    const res = await getFileList(params)
    // 后台已经直接返回签名URL，无需额外处理
    fileList.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载文件列表失败:', error)
    ElMessage.error('加载文件列表失败')
  } finally {
    loading.value = false
  }
}

// 目录选择事件
const handleDirectorySelect = (directory) => {
  selectedDirectoryId.value = directory.id
  selectedDirectory.value = directory
  // 重置分页并加载文件
  pagination.page = 1
  loadData()
}

// 目录刷新事件
const handleDirectoryRefresh = () => {
  // 目录刷新后，重新加载文件列表
  loadData()
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    fileType: props.fileType,
    storageType: 'all',
    keyword: '',
  })
  pagination.page = 1
  loadData()
}

// 分页变化
const handlePageChange = (page) => {
  pagination.page = page
  loadData()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.page = 1
  loadData()
}

// 判断文件是否已选中
const isSelected = (file) => {
  return selectedFiles.value.some(f => f.id === file.id)
}

// 选择/取消选择文件
const handleSelectFile = (file) => {
  const index = selectedFiles.value.findIndex(f => f.id === file.id)
  
  if (index > -1) {
    // 已选中，取消选择
    selectedFiles.value.splice(index, 1)
  } else {
    // 未选中，添加选择
    if (props.multiple) {
      // 多选模式
      if (selectedFiles.value.length >= props.maxSelect) {
        ElMessage.warning(`最多只能选择 ${props.maxSelect} 个文件`)
        return
      }
      selectedFiles.value.push(file)
    } else {
      // 单选模式
      selectedFiles.value = [file]
    }
  }
}

// 双击选择文件（直接确认）
const handleDoubleClick = (file) => {
  // 如果是多选模式，双击时只选择当前文件并确认
  if (props.multiple) {
    selectedFiles.value = [file]
  } else {
    // 单选模式，直接选择该文件
    selectedFiles.value = [file]
  }
  
  // 直接确认并关闭对话框
  emit('select', selectedFiles.value)
  visible.value = false
}

// 确认选择
const handleConfirm = () => {
  if (selectedFiles.value.length === 0) {
    ElMessage.warning('请先选择文件')
    return
  }
  
  emit('select', selectedFiles.value)
  visible.value = false
}

// 取消选择
const handleCancel = () => {
  visible.value = false
}

// 获取图片URL（优先使用预览URL，如果没有则使用原始URL）
const getImageUrl = (file) => {
  // 优先使用previewUrl（签名URL），如果没有则使用fileUrl（原始URL）
  return file.previewUrl || file.fileUrl || ''
}

// 处理图片加载错误
const handleImageError = async (file, event) => {
  // OSS bucket已改为"私有写公有读"模式，直接使用公开URL，无需重新获取
  console.warn('图片加载失败:', file.fileUrl || file.previewUrl)
}

// 触发上传按钮点击
const handleUploadClick = () => {
  uploadRef.value?.$el?.querySelector('input[type="file"]')?.click()
}

// 文件选择变化（el-upload 的 on-change 事件）
const handleFileChange = (file, fileList) => {
  // 收集所有选中的文件
  const rawFile = file.raw || file
  if (rawFile && beforeUpload(rawFile)) {
    // 检查是否已存在（通过文件名和大小判断）
    const exists = pendingFiles.value.find(
      f => f.name === rawFile.name && f.size === rawFile.size && f.lastModified === rawFile.lastModified
    )
    if (!exists) {
      pendingFiles.value.push(rawFile)
    }
  }
  
  // 清除之前的定时器
  if (uploadTimer) {
    clearTimeout(uploadTimer)
  }
  
  // 延迟执行批量上传，确保所有文件都已收集完成
  // 当用户选择多个文件时，on-change 会被多次调用
  // 我们等待 300ms 没有新的文件添加后，再开始上传
  uploadTimer = setTimeout(() => {
    if (pendingFiles.value.length > 0) {
      handleBatchUpload()
    }
  }, 300)
}

// 上传前验证
const beforeUpload = (file) => {
  if (!file) {
    return false
  }

  const isImage = file.type.startsWith('image/')
  const isVideo = file.type.startsWith('video/')
  const currentFilterType = searchForm.fileType

  // 验证文件类型
  if (!isImage && !isVideo) {
    ElMessage.error('只能上传图片或视频文件')
    return false
  }

  // 根据当前筛选类型验证
  if (currentFilterType === 'image' && !isImage) {
    ElMessage.warning('当前筛选类型为图片，您上传的是视频，是否继续？')
    // 允许继续，但给出提示
  } else if (currentFilterType === 'video' && !isVideo) {
    ElMessage.warning('当前筛选类型为视频，您上传的是图片，是否继续？')
    // 允许继续，但给出提示
  }

  // 验证文件大小
  const maxSize = isImage ? 5 : 50 // 图片 5MB，视频 50MB
  const fileSizeMB = file.size / 1024 / 1024
  if (fileSizeMB > maxSize) {
    ElMessage.error(`${isImage ? '图片' : '视频'}大小不能超过 ${maxSize}MB`)
    return false
  }

  return true
}

// 批量上传文件
const handleBatchUpload = async () => {
  if (pendingFiles.value.length === 0) {
    return
  }

  const filesToUpload = [...pendingFiles.value]
  pendingFiles.value = []
  
  uploading.value = true
  uploadProgressVisible.value = true
  
  // 初始化进度
  uploadProgress.total = filesToUpload.length
  uploadProgress.current = 0
  uploadProgress.success = 0
  uploadProgress.failed = 0
  uploadProgress.percentage = 0
  uploadProgress.status = 'active'
  uploadProgress.currentFile = ''

  const uploadedFileUrls = []

  // 逐个上传文件
  for (let i = 0; i < filesToUpload.length; i++) {
    const file = filesToUpload[i]
    uploadProgress.current = i + 1
    uploadProgress.currentFile = file.name || `文件 ${i + 1}`
    uploadProgress.percentage = Math.round(((i + 1) / filesToUpload.length) * 100)

    try {
      const isImage = file.type.startsWith('image/')
      let response

      // 使用选中的目录ID上传，如果没有选中目录则使用默认的 common
      const directoryId = selectedDirectoryId.value
      if (isImage) {
        response = await uploadImage(file, 'common', directoryId)
      } else {
        response = await uploadVideo(file, 'common', directoryId)
      }

      if (response && response.code === 200) {
        uploadProgress.success++
        uploadedFileUrls.push(response.data)
      } else {
        uploadProgress.failed++
        console.error('上传失败:', response?.message || '未知错误')
      }
    } catch (error) {
      uploadProgress.failed++
      console.error('上传失败:', error)
    }
  }

  // 更新最终状态
  if (uploadProgress.failed === 0) {
    uploadProgress.status = 'success'
    ElMessage.success(`成功上传 ${uploadProgress.success} 个文件`)
  } else if (uploadProgress.success === 0) {
    uploadProgress.status = 'exception'
    ElMessage.error('所有文件上传失败')
  } else {
    uploadProgress.status = 'exception'
    ElMessage.warning(`部分文件上传失败：成功 ${uploadProgress.success} 个，失败 ${uploadProgress.failed} 个`)
  }

  // 上传完成后刷新列表
  await loadData()

  // 选中所有成功上传的文件
  if (uploadedFileUrls.length > 0) {
    await selectUploadedFiles(uploadedFileUrls)
  }

  uploading.value = false
  
  // 清空上传组件的文件列表
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

// 关闭上传进度对话框
const handleCloseUploadProgress = () => {
  uploadProgressVisible.value = false
  // 重置进度
  uploadProgress.total = 0
  uploadProgress.current = 0
  uploadProgress.success = 0
  uploadProgress.failed = 0
  uploadProgress.percentage = 0
  uploadProgress.status = 'success'
  uploadProgress.currentFile = ''
  // 清空待上传文件列表
  pendingFiles.value = []
  // 清除定时器
  if (uploadTimer) {
    clearTimeout(uploadTimer)
    uploadTimer = null
  }
}

// 查找并选中新上传的文件（单个）
const selectUploadedFile = async (fileUrl) => {
  const files = await findUploadedFiles([fileUrl])
  if (files.length > 0) {
    await selectFiles(files)
  }
}

// 查找并选中新上传的文件（多个）
const selectUploadedFiles = async (fileUrls) => {
  const files = await findUploadedFiles(fileUrls)
  if (files.length > 0) {
    await selectFiles(files)
  }
}

// 查找上传的文件
const findUploadedFiles = async (fileUrls) => {
  // 等待一小段时间确保后端已保存文件记录
  await new Promise(resolve => setTimeout(resolve, 800))
  
  // 先刷新当前页
  await loadData()
  
  const foundFiles = []
  
  for (const fileUrl of fileUrls) {
    // 从 URL 中提取路径（去掉签名参数和域名）
    const urlPath = fileUrl.split('?')[0] // 去掉签名参数
    const urlParts = urlPath.split('/')
    const fileName = urlParts[urlParts.length - 1] // 文件名
    
    // 在当前列表中查找
    let uploadedFile = fileList.value.find(f => {
      const fileUrlPath = f.fileUrl?.split('?')[0] || ''
      return fileUrlPath.includes(fileName) || 
             fileUrlPath === urlPath ||
             f.originalName === fileName
    })
    
    // 如果当前页没找到，尝试搜索第一页（新上传的文件通常在最新）
    if (!uploadedFile) {
      const firstPageParams = {
        ...searchForm,
        page: 1,
        pageSize: pagination.pageSize,
      }
      try {
        const res = await getFileList(firstPageParams)
        const firstPageFiles = res.data.records || []
        uploadedFile = firstPageFiles.find(f => {
          const fileUrlPath = f.fileUrl?.split('?')[0] || ''
          return fileUrlPath.includes(fileName) || 
                 fileUrlPath === urlPath ||
                 f.originalName === fileName
        })
        
        // 如果找到了，跳转到第一页
        if (uploadedFile) {
          pagination.page = 1
          await loadData()
          uploadedFile = fileList.value.find(f => f.id === uploadedFile.id)
        }
      } catch (error) {
        console.error('查找文件失败:', error)
      }
    }
    
    if (uploadedFile && !foundFiles.find(f => f.id === uploadedFile.id)) {
      foundFiles.push(uploadedFile)
    }
  }
  
  return foundFiles
}

// 选中文件
const selectFiles = async (files) => {
  for (const file of files) {
    if (props.multiple) {
      if (selectedFiles.value.length < props.maxSelect) {
        if (!selectedFiles.value.find(f => f.id === file.id)) {
          selectedFiles.value.push(file)
        }
      }
    } else {
      selectedFiles.value = [file]
      break // 单选模式只选择第一个
    }
  }
  
  // 滚动到第一个选中的文件
  if (files.length > 0) {
    await nextTick()
    const fileElement = document.querySelector(`[data-file-id="${files[0].id}"]`)
    if (fileElement) {
      fileElement.scrollIntoView({ behavior: 'smooth', block: 'center' })
    }
  }
}

// 组件卸载时清理定时器
onBeforeUnmount(() => {
  if (uploadTimer) {
    clearTimeout(uploadTimer)
    uploadTimer = null
  }
})
</script>

<style scoped>
.search-form {
  margin-bottom: -18px;
}

.file-grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
  min-height: 400px;
  max-height: 500px;
  overflow-y: auto;
  padding: 4px;
}

.file-item {
  position: relative;
  border: 2px solid #DCDFE6;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  overflow: hidden;
  background: #fff;
}

.file-item:hover {
  border-color: #409EFF;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.file-item.selected {
  border-color: #409EFF;
  background: #ECF5FF;
}

.file-preview {
  width: 100%;
  height: 140px;
  background: #F5F7FA;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.video-icon,
.other-icon {
  color: #909399;
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #909399;
}

.error-text {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.file-info {
  padding: 8px 12px;
}

.file-name {
  font-size: 13px;
  color: #303133;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.file-size {
  color: #909399;
  font-size: 12px;
}

.file-hint {
  margin-top: 4px;
  font-size: 11px;
  color: #C0C4CC;
  opacity: 0;
  transition: opacity 0.3s;
}

.file-item:hover .file-hint {
  opacity: 1;
}

.selected-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  background: #409EFF;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
}

.empty-state {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  display: flex;
  align-items: center;
  width: 100%;
}

/* 上传按钮样式 */
:deep(.el-form-item:last-child .el-button--success) {
  margin-left: 8px;
}

/* 左右分栏布局 */
.file-selector-layout {
  display: flex;
  gap: 16px;
  min-height: 500px;
  max-height: 600px;
}

.directory-panel {
  width: 30%;
  min-width: 250px;
  border: 1px solid #EBEEF5;
  border-radius: 4px;
  background: #fff;
  overflow: hidden;
}

.file-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* 上传进度样式 */
.upload-progress-content {
  padding: 20px 0;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-size: 14px;
  color: #606266;
}

.progress-text {
  font-size: 12px;
  color: #909399;
}

.current-file {
  margin-top: 12px;
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
