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
      </el-form-item>
    </el-form>

    <!-- 文件网格 -->
    <div v-loading="loading" class="file-grid-container">
      <div
        v-for="file in fileList"
        :key="file.id"
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
        <el-empty description="暂无文件" />
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
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { VideoPlay, Document, Check, Picture } from '@element-plus/icons-vue'
import { getFileList, getSignedUrl } from '@/api/file'

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
const fileList = ref([])
const selectedFiles = ref([])

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

// 处理图片加载错误（作为后备方案，如果后台返回的签名URL失效）
const handleImageError = async (file, event) => {
  // 如果图片加载失败，可能是签名URL已过期，尝试重新获取
  if (file.storageType === 'OSS' && file.id) {
    try {
      const response = await getSignedUrl(file.id)
      if (response && response.code === 200 && response.data) {
        // 更新文件的URL为新的签名URL
        const index = fileList.value.findIndex(item => item.id === file.id)
        if (index > -1) {
          fileList.value[index] = {
            ...fileList.value[index],
            fileUrl: response.data,
          }
          fileList.value = [...fileList.value]
        }
      }
    } catch (error) {
      console.warn('重新获取签名URL失败:', error)
    }
  }
}
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
</style>
