<template>
  <div class="file-manage-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409EFF">
              <el-icon><Files /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalCount || 0 }}</div>
              <div class="stat-label">总文件数</div>
              <div class="stat-desc">{{ statistics.totalSizeFormatted || '0 B' }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67C23A">
              <el-icon><Upload /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.ossCount || 0 }}</div>
              <div class="stat-label">OSS文件</div>
              <div class="stat-desc">{{ statistics.ossSizeFormatted || '0 B' }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #E6A23C">
              <el-icon><Folder /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.localCount || 0 }}</div>
              <div class="stat-label">本地文件</div>
              <div class="stat-desc">{{ statistics.localSizeFormatted || '0 B' }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #909399">
              <el-icon><Picture /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ imageCount || 0 }}</div>
              <div class="stat-label">图片 / {{ videoCount || 0 }} 视频</div>
              <div class="stat-desc">分类统计</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索筛选区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="文件类型">
          <el-select v-model="searchForm.fileType" placeholder="全部" clearable style="width: 120px">
            <el-option label="全部" value="all" />
            <el-option label="图片" value="image" />
            <el-option label="视频" value="video" />
          </el-select>
        </el-form-item>
        <el-form-item label="存储类型">
          <el-select v-model="searchForm.storageType" placeholder="全部" clearable style="width: 120px">
            <el-option label="全部" value="all" />
            <el-option label="OSS" value="OSS" />
            <el-option label="本地" value="LOCAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="模块">
          <el-input
            v-model="searchForm.module"
            placeholder="如：banner、article"
            clearable
            style="width: 180px"
          />
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
          <el-button type="primary" @click="handleSearch" :icon="Search">搜索</el-button>
          <el-button @click="handleReset" :icon="Refresh">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <span class="toolbar-title">文件列表</span>
          <span v-if="selectedFiles.length > 0" class="selected-count">
            已选择 {{ selectedFiles.length }} 个文件
          </span>
        </div>
        <div class="toolbar-right">
          <el-button
            v-if="selectedFiles.length > 0"
            type="danger"
            :icon="Delete"
            @click="handleBatchDelete"
            :disabled="batchDeleting"
          >
            批量删除 ({{ selectedFiles.length }})
          </el-button>
          <el-button type="primary" :icon="Upload" @click="handleOpenUploadDialog">上传文件</el-button>
          <el-button :icon="Refresh" @click="loadData">刷新</el-button>
        </div>
      </div>

      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        border
        style="width: 100%"
        row-key="id"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" :selectable="() => true" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="预览" width="120">
          <template #default="{ row }">
            <el-image
              v-if="row.fileType === 'image'"
              :src="getImageUrl(row)"
              :preview-src-list="[getImageUrl(row)]"
              fit="cover"
              style="width: 80px; height: 80px; border-radius: 4px"
              lazy
              @error="() => handleImageError(row)"
            />
            <el-icon v-else style="font-size: 40px; color: #409EFF"><VideoPlay /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="originalName" label="文件名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="fileType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.fileType === 'image'" type="success" size="small">图片</el-tag>
            <el-tag v-else-if="row.fileType === 'video'" type="warning" size="small">视频</el-tag>
            <el-tag v-else type="info" size="small">其他</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="storageType" label="存储" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.storageType === 'OSS'" type="success" size="small">OSS</el-tag>
            <el-tag v-else type="info" size="small">本地</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="模块" width="100" />
        <el-table-column prop="fileSizeFormatted" label="大小" width="100" />
        <el-table-column prop="createdAt" label="上传时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button link type="primary" size="small" @click="handleCopyUrl(row)">复制URL</el-button>
            <el-button
              link
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 查看文件详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="文件详情"
      width="800px"
    >
      <el-descriptions :column="2" border v-if="currentFile">
        <el-descriptions-item label="文件ID">{{ currentFile.id }}</el-descriptions-item>
        <el-descriptions-item label="文件类型">
          <el-tag v-if="currentFile.fileType === 'image'" type="success" size="small">图片</el-tag>
          <el-tag v-else-if="currentFile.fileType === 'video'" type="warning" size="small">视频</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="原始文件名" :span="2">{{ currentFile.originalName }}</el-descriptions-item>
        <el-descriptions-item label="存储类型">
          <el-tag v-if="currentFile.storageType === 'OSS'" type="success" size="small">OSS</el-tag>
          <el-tag v-else type="info" size="small">本地</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="文件大小">{{ currentFile.fileSizeFormatted }}</el-descriptions-item>
        <el-descriptions-item label="模块">{{ currentFile.module }}</el-descriptions-item>
        <el-descriptions-item label="文件扩展名">{{ currentFile.fileExtension }}</el-descriptions-item>
        <el-descriptions-item label="上传时间" :span="2">{{ formatDate(currentFile.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="文件路径" :span="2">{{ currentFile.filePath }}</el-descriptions-item>
        <el-descriptions-item label="访问URL" :span="2">
          <el-link :href="currentFile.fileUrl" target="_blank" type="primary">{{ currentFile.fileUrl }}</el-link>
        </el-descriptions-item>
      </el-descriptions>

      <div class="preview-section" v-if="currentFile">
        <h4>文件预览</h4>
        <el-image
          v-if="currentFile.fileType === 'image'"
          :src="getImageUrl(currentFile)"
          :preview-src-list="[getImageUrl(currentFile)]"
          fit="contain"
          style="max-width: 100%; max-height: 400px"
        />
        <video
          v-else-if="currentFile.fileType === 'video'"
          :src="getImageUrl(currentFile)"
          controls
          style="max-width: 100%; max-height: 400px"
        />
      </div>

      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 上传文件对话框 -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="上传文件"
      width="600px"
      @close="handleCloseUploadDialog"
    >
      <el-form :model="uploadForm" label-width="100px">
        <el-form-item label="文件类型" required>
          <el-radio-group v-model="uploadForm.fileType">
            <el-radio value="image">图片</el-radio>
            <el-radio value="video">视频</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="模块" required>
          <el-input
            v-model="uploadForm.module"
            placeholder="如：common、banner、article 等"
            clearable
          />
          <div style="color: #909399; font-size: 12px; margin-top: 4px;">
            用于文件分类管理，建议使用有意义的模块名称
          </div>
        </el-form-item>
        <el-form-item 
          v-if="uploadForm.fileType === 'image'"
          label="图片文件"
          required
        >
          <el-upload
            ref="imageUploadRef"
            :before-upload="beforeImageUpload"
            :limit="20"
            multiple
            :file-list="imageFileList"
            :on-change="handleImageFileChange"
            accept="image/jpeg,image/jpg,image/png,image/webp"
            :auto-upload="false"
            :http-request="handleImageUpload"
          >
            <template #trigger>
              <el-button type="primary">选择图片</el-button>
            </template>
            <template #tip>
              <div class="el-upload__tip">
                支持 JPG、PNG、WebP 格式，单张图片不超过 5MB，最多可选择 20 张
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item 
          v-if="uploadForm.fileType === 'video'"
          label="视频文件"
          required
        >
          <el-upload
            ref="videoUploadRef"
            :before-upload="beforeVideoUpload"
            :limit="10"
            multiple
            :file-list="videoFileList"
            :on-change="handleVideoFileChange"
            accept="video/mp4"
            :auto-upload="false"
            :http-request="handleVideoUpload"
          >
            <template #trigger>
              <el-button type="primary">选择视频</el-button>
            </template>
            <template #tip>
              <div class="el-upload__tip">
                支持 MP4 格式，单个文件大小不超过 50MB，最多可选择 10 个
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="display: flex; align-items: center; justify-content: space-between; width: 100%;">
          <div v-if="uploading && uploadProgress.total > 0" style="flex: 1; margin-right: 20px;">
            <el-progress 
              :percentage="uploadProgress.percentage" 
              :status="uploadProgress.status"
              :format="() => `${uploadProgress.success}/${uploadProgress.total}`"
            />
            <div style="font-size: 12px; color: #909399; margin-top: 4px;">
              正在上传：{{ uploadProgress.current }} / {{ uploadProgress.total }}
            </div>
          </div>
          <div>
            <el-button @click="uploadDialogVisible = false" :disabled="uploading">取消</el-button>
            <el-button type="primary" @click="handleSubmitUpload" :loading="uploading">
              {{ uploading ? `上传中 (${uploadProgress.success}/${uploadProgress.total})` : '上传' }}
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, Refresh, Files, Upload, Folder, Picture, VideoPlay, 
  Document, CollectionTag, FolderOpened, DataLine, Box, 
  DocumentCopy, Edit, Location, Link, View, Clock, Calendar, Delete
} from '@element-plus/icons-vue'
import { getFileList, deleteFile, deleteFilesBatch, getFileStatistics, getSignedUrl, uploadImage, uploadVideo } from '@/api/file'
import { useClipboard } from '@vueuse/core'

const loading = ref(false)
const tableData = ref([])
const selectedFiles = ref([])
const batchDeleting = ref(false)
const viewDialogVisible = ref(false)
const currentFile = ref(null)
const uploadDialogVisible = ref(false)
const uploading = ref(false)
const imageUploadRef = ref(null)
const videoUploadRef = ref(null)
const imageFileList = ref([])
const videoFileList = ref([])
const imageFileMap = ref(new Map())
const videoFileMap = ref(new Map())

// 上传进度
const uploadProgress = reactive({
  total: 0,
  current: 0,
  success: 0,
  failed: 0,
  percentage: 0,
  status: 'success', // success, exception
})

const { copy } = useClipboard()

// 上传表单
const uploadForm = reactive({
  fileType: 'image',
  module: 'common',
})

const searchForm = reactive({
  fileType: 'all',
  storageType: 'all',
  module: '',
  keyword: '',
})

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0,
})

const statistics = ref({
  totalCount: 0,
  totalSize: 0,
  ossCount: 0,
  ossSize: 0,
  localCount: 0,
  localSize: 0,
  totalSizeFormatted: '0 B',
  ossSizeFormatted: '0 B',
  localSizeFormatted: '0 B',
})

// 计算图片和视频数量
const imageCount = computed(() => {
  return tableData.value.filter(f => f.fileType === 'image').length
})

const videoCount = computed(() => {
  return tableData.value.filter(f => f.fileType === 'video').length
})

// 加载统计信息
const loadStatistics = async () => {
  try {
    const res = await getFileStatistics()
    statistics.value = res.data || {}
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      page: pagination.page,
      pageSize: pagination.pageSize,
    }
    const res = await getFileList(params)
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
    
    // 清空选择状态（因为数据已刷新）
    selectedFiles.value = []
    
    // 同时刷新统计信息
    await loadStatistics()
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
    fileType: 'all',
    storageType: 'all',
    module: '',
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

// 查看详情
const handleView = async (row) => {
  // 使用当前行的数据，优先使用 previewUrl
  currentFile.value = {
    ...row,
    // 确保使用签名URL（如果存在）
    previewUrl: row.previewUrl || row.fileUrl,
  }
  viewDialogVisible.value = true
  
  // 如果是OSS存储的文件但没有 previewUrl，尝试获取签名URL
  if (row.storageType === 'OSS' && !row.previewUrl) {
    try {
      const res = await getSignedUrl(row.id)
      if (res.data) {
        // 更新预览URL
        currentFile.value = {
          ...row,
          previewUrl: res.data,
        }
      }
    } catch (error) {
      console.error('获取签名URL失败:', error)
      ElMessage.warning('获取预览链接失败，可能无法预览私有文件')
    }
  }
}

// 复制文本
const handleCopyText = async (text) => {
  try {
    await copy(text)
    ElMessage.success('已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败')
  }
}

// 复制URL
const handleCopyUrl = async (row) => {
  try {
    let urlToCopy = row.fileUrl
    
    // 如果是OSS存储的文件，获取签名URL
    if (row.storageType === 'OSS') {
      try {
        const res = await getSignedUrl(row.id)
        if (res.data) {
          urlToCopy = res.data
          ElMessage.success('已复制签名URL（1小时有效）到剪贴板')
        }
      } catch (error) {
        console.error('获取签名URL失败:', error)
        ElMessage.warning('获取签名URL失败，已复制原始URL')
      }
    }
    
    await copy(urlToCopy)
    if (row.storageType !== 'OSS') {
      ElMessage.success('URL已复制到剪贴板')
    }
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败')
  }
}

// 处理表格选择变化
const handleSelectionChange = (selection) => {
  selectedFiles.value = selection
}

// 批量删除文件
const handleBatchDelete = async () => {
  if (selectedFiles.value.length === 0) {
    ElMessage.warning('请先选择要删除的文件')
    return
  }

  try {
    const ossCount = selectedFiles.value.filter(f => f.storageType === 'OSS').length
    const localCount = selectedFiles.value.filter(f => f.storageType === 'LOCAL').length
    
    let storageTypeText = ''
    if (ossCount > 0 && localCount > 0) {
      storageTypeText = `OSS和本地存储中的文件`
    } else if (ossCount > 0) {
      storageTypeText = `OSS存储中的文件`
    } else {
      storageTypeText = `本地存储中的文件`
    }

    // 构建确认消息
    let confirmMessage = `确定要删除选中的 ${selectedFiles.value.length} 个文件吗？\n\n此操作将同时删除${storageTypeText}，且无法恢复。`
    
    // 如果文件数量较少（<=5个），显示文件名列表
    if (selectedFiles.value.length <= 5) {
      const fileNames = selectedFiles.value.map(f => f.originalName).join('\n')
      confirmMessage += `\n\n文件列表：\n${fileNames}`
    } else {
      // 如果文件数量较多，只显示前3个文件名
      const firstThreeNames = selectedFiles.value.slice(0, 3).map(f => f.originalName).join('\n')
      confirmMessage += `\n\n部分文件列表：\n${firstThreeNames}\n... 等共 ${selectedFiles.value.length} 个文件`
    }

    await ElMessageBox.confirm(
      confirmMessage,
      '确认批量删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: false,
      }
    )

    batchDeleting.value = true
    const ids = selectedFiles.value.map(f => f.id)
    const res = await deleteFilesBatch(ids)
    
    if (res.code === 200) {
      const result = res.data || {}
      const successCount = result.successCount || 0
      const failCount = result.failCount || 0
      
      if (failCount === 0) {
        ElMessage.success(`批量删除成功，共删除 ${successCount} 个文件`)
      } else if (successCount > 0) {
        ElMessage.warning(`部分删除成功：成功 ${successCount} 个，失败 ${failCount} 个`)
        // 显示失败详情
        if (result.failDetails && result.failDetails.length > 0) {
          const failMessages = result.failDetails.map(d => `${d.fileName}: ${d.reason}`).join('\n')
          console.warn('删除失败的文件：', result.failDetails)
          ElMessageBox.alert(
            `以下文件删除失败：\n\n${failMessages}`,
            '删除失败详情',
            {
              type: 'warning',
            }
          )
        }
      } else {
        ElMessage.error('批量删除失败：' + (result.message || '未知错误'))
      }
      
      // 清空选择
      selectedFiles.value = []
      // 刷新数据
      loadData()
    } else {
      ElMessage.error(res.message || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除文件失败:', error)
      ElMessage.error(error.message || '批量删除失败')
    }
  } finally {
    batchDeleting.value = false
  }
}

// 删除文件
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文件 "${row.originalName}" 吗？此操作将同时删除${row.storageType === 'OSS' ? 'OSS' : '本地'}存储中的文件，且无法恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    await deleteFile(row.id)
    ElMessage.success('删除成功')
    // 如果删除的文件在选中列表中，从选中列表中移除
    const index = selectedFiles.value.findIndex(f => f.id === row.id)
    if (index > -1) {
      selectedFiles.value.splice(index, 1)
    }
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除文件失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 获取图片URL（优先使用签名URL）
const getImageUrl = (row) => {
  // 优先使用 previewUrl（后端返回的签名URL）
  if (row.previewUrl) {
    return row.previewUrl
  }
  // 如果没有 previewUrl，使用原始 fileUrl
  return row.fileUrl || ''
}

// 处理图片加载错误（签名URL可能已过期）
const handleImageError = async (row) => {
  // 如果是OSS文件且使用的是签名URL但加载失败，尝试重新获取签名URL
  if (row.storageType === 'OSS' && row.previewUrl && !row._signedUrlRefreshed) {
    try {
      const res = await getSignedUrl(row.id)
      if (res.data) {
        // 更新该行的 previewUrl 为新的签名URL
        row.previewUrl = res.data
        row._signedUrlRefreshed = true
        // 强制重新渲染
        const index = tableData.value.findIndex(item => item.id === row.id)
        if (index > -1) {
          tableData.value[index] = { ...row }
        }
      }
    } catch (error) {
      console.error('重新获取签名URL失败:', error)
      // 如果重新获取失败，回退到原始URL
      row.previewUrl = null
    }
  }
}

// 打开上传对话框
const handleOpenUploadDialog = () => {
  uploadForm.fileType = 'image'
  uploadForm.module = 'common'
  imageFileList.value = []
  videoFileList.value = []
  uploadDialogVisible.value = true
}

// 关闭上传对话框
const handleCloseUploadDialog = () => {
  uploadForm.fileType = 'image'
  uploadForm.module = 'common'
  imageFileList.value = []
  videoFileList.value = []
  imageFileMap.value.clear()
  videoFileMap.value.clear()
  // 重置上传进度
  uploadProgress.total = 0
  uploadProgress.current = 0
  uploadProgress.success = 0
  uploadProgress.failed = 0
  uploadProgress.percentage = 0
  uploadProgress.status = 'success'
  if (imageUploadRef.value) {
    imageUploadRef.value.clearFiles()
  }
  if (videoUploadRef.value) {
    videoUploadRef.value.clearFiles()
  }
}

// 处理图片文件变化
const handleImageFileChange = (file, fileList) => {
  // file 参数是当前变化的文件对象，fileList 是完整的文件列表
  // 保存原始文件对象到映射中
  if (file && file.raw) {
    imageFileMap.value.set(file.uid, file.raw)
  } else if (file instanceof File) {
    // 如果 file 本身就是 File 对象，需要找到对应的 uid
    const fileItem = fileList.find(f => f.name === file.name)
    if (fileItem) {
      imageFileMap.value.set(fileItem.uid, file)
    }
  }
  
  // 更新文件列表
  imageFileList.value = fileList
  console.log('图片文件变化:', { file, fileList, imageFileList: imageFileList.value, fileMap: imageFileMap.value })
}

// 处理视频文件变化
const handleVideoFileChange = (file, fileList) => {
  // file 参数是当前变化的文件对象，fileList 是完整的文件列表
  // 保存原始文件对象到映射中
  if (file && file.raw) {
    videoFileMap.value.set(file.uid, file.raw)
  } else if (file instanceof File) {
    // 如果 file 本身就是 File 对象，需要找到对应的 uid
    const fileItem = fileList.find(f => f.name === file.name)
    if (fileItem) {
      videoFileMap.value.set(fileItem.uid, file)
    }
  }
  
  // 更新文件列表
  videoFileList.value = fileList
  console.log('视频文件变化:', { file, fileList, videoFileList: videoFileList.value, fileMap: videoFileMap.value })
}

// 上传前检查（图片）
const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isValidType = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'].includes(file.type)
  const isLtMaxSize = file.size / 1024 / 1024 < 5

  if (!isImage || !isValidType) {
    ElMessage.error('只能上传图片文件（JPG、PNG、WebP格式）')
    return false
  }

  if (!isLtMaxSize) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }

  return true
}

// 上传前检查（视频）
const beforeVideoUpload = (file) => {
  const isVideo = file.type.startsWith('video/')
  const isValidType = file.type === 'video/mp4'
  const isLtMaxSize = file.size / 1024 / 1024 < 50

  if (!isVideo || !isValidType) {
    ElMessage.error('只能上传MP4格式的视频文件')
    return false
  }

  if (!isLtMaxSize) {
    ElMessage.error('视频大小不能超过 50MB')
    return false
  }

  return true
}

// 上传成功
const handleUploadSuccess = (response, file) => {
  uploading.value = false
  if (response && response.code === 200) {
    ElMessage.success('上传成功')
    uploadDialogVisible.value = false
    // 清空文件列表
    imageFileList.value = []
    videoFileList.value = []
    imageFileMap.value.clear()
    videoFileMap.value.clear()
    if (imageUploadRef.value) {
      imageUploadRef.value.clearFiles()
    }
    if (videoUploadRef.value) {
      videoUploadRef.value.clearFiles()
    }
    // 刷新文件列表
    loadData()
    loadStatistics()
  } else {
    ElMessage.error(response?.message || '上传失败')
  }
}

// 上传失败
const handleUploadError = (error, file) => {
  uploading.value = false
  console.error('上传失败:', error)
  ElMessage.error('上传失败，请重试')
}

// 自定义图片上传（当 auto-upload 为 true 时使用，但我们现在不使用）
const handleImageUpload = async (options) => {
  const { file } = options
  uploading.value = true
  try {
    const response = await uploadImage(file, uploadForm.module)
    handleUploadSuccess(response, file)
  } catch (error) {
    handleUploadError(error, file)
  }
}

// 自定义视频上传（当 auto-upload 为 true 时使用，但我们现在不使用）
const handleVideoUpload = async (options) => {
  const { file } = options
  uploading.value = true
  try {
    const response = await uploadVideo(file, uploadForm.module)
    handleUploadSuccess(response, file)
  } catch (error) {
    handleUploadError(error, file)
  }
}

// 获取所有待上传的文件
const getFilesToUpload = (fileType) => {
  const files = []
  
  if (fileType === 'image') {
    // 从 imageFileList 获取所有文件
    imageFileList.value.forEach(fileItem => {
      const file = imageFileMap.value.get(fileItem.uid) || fileItem.raw || (fileItem instanceof File ? fileItem : null)
      if (file && file instanceof File) {
        files.push(file)
      }
    })
    
    // 如果 imageFileList 为空，尝试从 uploadFiles 获取
    if (files.length === 0 && imageUploadRef.value?.uploadFiles?.length > 0) {
      imageUploadRef.value.uploadFiles.forEach(fileItem => {
        const file = imageFileMap.value.get(fileItem.uid) || fileItem.raw || (fileItem instanceof File ? fileItem : null)
        if (file && file instanceof File) {
          files.push(file)
        }
      })
    }
  } else if (fileType === 'video') {
    // 从 videoFileList 获取所有文件
    videoFileList.value.forEach(fileItem => {
      const file = videoFileMap.value.get(fileItem.uid) || fileItem.raw || (fileItem instanceof File ? fileItem : null)
      if (file && file instanceof File) {
        files.push(file)
      }
    })
    
    // 如果 videoFileList 为空，尝试从 uploadFiles 获取
    if (files.length === 0 && videoUploadRef.value?.uploadFiles?.length > 0) {
      videoUploadRef.value.uploadFiles.forEach(fileItem => {
        const file = videoFileMap.value.get(fileItem.uid) || fileItem.raw || (fileItem instanceof File ? fileItem : null)
        if (file && file instanceof File) {
          files.push(file)
        }
      })
    }
  }
  
  return files
}

// 提交上传（支持批量上传）
const handleSubmitUpload = async () => {
  // 验证模块名称
  if (!uploadForm.module || !uploadForm.module.trim()) {
    ElMessage.warning('请输入模块名称')
      return
    }
    
  // 获取所有待上传的文件
  const files = getFilesToUpload(uploadForm.fileType)
  
  if (files.length === 0) {
    ElMessage.warning(`请选择${uploadForm.fileType === 'image' ? '图片' : '视频'}文件`)
      return
    }
    
  // 初始化上传进度
  uploadProgress.total = files.length
  uploadProgress.current = 0
  uploadProgress.success = 0
  uploadProgress.failed = 0
  uploadProgress.percentage = 0
  uploadProgress.status = 'success'
  
    uploading.value = true

  // 批量上传文件
  const uploadPromises = files.map(async (file, index) => {
    try {
      uploadProgress.current = index + 1
      
      let response
      if (uploadForm.fileType === 'image') {
        response = await uploadImage(file, uploadForm.module)
      } else {
        response = await uploadVideo(file, uploadForm.module)
      }
      
      if (response && response.code === 200) {
        uploadProgress.success++
      } else {
        uploadProgress.failed++
        uploadProgress.status = 'exception'
        console.error(`文件 ${file.name} 上传失败:`, response?.message || '未知错误')
      }
    } catch (error) {
      uploadProgress.failed++
      uploadProgress.status = 'exception'
      console.error(`文件 ${file.name} 上传失败:`, error)
    } finally {
      // 更新进度百分比
      uploadProgress.percentage = Math.round(((uploadProgress.success + uploadProgress.failed) / uploadProgress.total) * 100)
    }
  })

  // 等待所有上传完成
  await Promise.all(uploadPromises)
  
  uploading.value = false

  // 显示上传结果
  if (uploadProgress.failed === 0) {
    ElMessage.success(`成功上传 ${uploadProgress.success} 个文件`)
    uploadDialogVisible.value = false
    // 清空文件列表
    imageFileList.value = []
    videoFileList.value = []
    imageFileMap.value.clear()
    videoFileMap.value.clear()
    if (imageUploadRef.value) {
      imageUploadRef.value.clearFiles()
    }
    if (videoUploadRef.value) {
      videoUploadRef.value.clearFiles()
    }
    // 刷新文件列表
    loadData()
    loadStatistics()
  } else if (uploadProgress.success > 0) {
    ElMessage.warning(`上传完成：成功 ${uploadProgress.success} 个，失败 ${uploadProgress.failed} 个`)
    // 即使有失败，也刷新列表显示已上传的文件
    loadData()
    loadStatistics()
  } else {
    ElMessage.error(`所有文件上传失败`)
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
}

onMounted(() => {
  loadData()
  loadStatistics()
})
</script>

<style scoped>
.file-manage-container {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 28px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}

.stat-desc {
  font-size: 12px;
  color: #909399;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: -18px;
}

.table-card {
  margin-bottom: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.toolbar-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.selected-count {
  margin-left: 12px;
  font-size: 14px;
  color: #409eff;
  font-weight: 500;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.preview-section {
  margin-top: 20px;
}

.preview-section h4 {
  margin-bottom: 12px;
  font-size: 14px;
  color: #606266;
}

/* 文件详情对话框样式 */
:deep(.file-detail-dialog) {
  .el-dialog__body {
    padding: 0;
  }
}

.file-detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-height: 70vh;
  overflow-y: auto;
  padding: 20px;
}

/* 预览卡片 */
.preview-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.preview-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  color: white;
}

.preview-icon {
  font-size: 20px;
}

.preview-title {
  font-size: 16px;
  font-weight: 600;
}

.preview-body {
  background: white;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
}

.preview-image {
  max-width: 100%;
  max-height: 400px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.preview-video {
  max-width: 100%;
  max-height: 400px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 信息区域 */
.info-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  transition: all 0.3s;
}

.info-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border-color: #c0c4cc;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
  font-size: 15px;
}

.card-header .el-icon {
  font-size: 18px;
  color: #409eff;
}

/* 信息网格 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

.info-label .el-icon {
  font-size: 16px;
  color: #606266;
}

.info-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 文件名称 */
.file-name-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
}

.file-name-text {
  flex: 1;
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

/* 路径内容 */
.path-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.path-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.path-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

.path-label .el-icon {
  font-size: 16px;
  color: #606266;
}

.path-value {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
}

.path-text {
  flex: 1;
  font-size: 13px;
  color: #606266;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  word-break: break-all;
}

.url-text {
  color: #409eff;
}

.url-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.copy-btn {
  padding: 4px 8px;
}

/* 时间内容 */
.time-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.time-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
}

.time-icon {
  font-size: 20px;
  color: #409eff;
  margin-top: 2px;
}

.time-info {
  flex: 1;
}

.time-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}

.time-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

/* 对话框底部 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px;
  border-top: 1px solid #e4e7ed;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .file-detail-content {
    padding: 16px;
  }
  
  .preview-body {
    min-height: 200px;
  }
}
</style>
