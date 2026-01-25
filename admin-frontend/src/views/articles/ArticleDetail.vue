<template>
  <div class="article-detail-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>文章详情</span>
          <div>
            <el-button @click="handleBack">返回</el-button>
            <el-button type="primary" @click="handleEdit">编辑</el-button>
          </div>
        </div>
      </template>

      <div v-loading="loading" class="article-detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="文章标题">
            {{ articleData.title }}
          </el-descriptions-item>
          <el-descriptions-item label="文章分类">
            {{ articleData.categoryName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="作者">
            {{ articleData.author || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="articleData.status === 0" type="info">草稿</el-tag>
            <el-tag v-else-if="articleData.status === 1" type="success">已发布</el-tag>
            <el-tag v-else-if="articleData.status === 2" type="warning">已下架</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发布时间" :span="2">
            {{ formatDate(articleData.publishTime) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="阅读量">
            {{ articleData.viewCount || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="点赞量">
            {{ articleData.likeCount || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="封面图" :span="2">
            <el-image
              v-if="articleData.coverImage"
              :src="articleData.coverImage"
              style="max-width: 300px; max-height: 200px"
              fit="contain"
              :preview-src-list="getAllArticleImages()"
              :initial-index="0"
              @error="handleCoverImageError"
              :hide-on-click-modal="false"
            >
              <template #error>
                <div class="image-error">
                  <el-icon :size="40"><Picture /></el-icon>
                  <div class="error-text">图片加载失败</div>
                </div>
              </template>
            </el-image>
            <span v-else class="no-image">-</span>
          </el-descriptions-item>
          <el-descriptions-item label="文章摘要" :span="2">
            {{ articleData.summary || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="标签" :span="2">
            <el-tag
              v-for="tag in articleTags"
              :key="tag.id"
              :type="tag.color ? '' : 'info'"
              :style="tag.color ? { color: tag.color, borderColor: tag.color, marginRight: '8px' } : { marginRight: '8px' }"
            >
              {{ tag.name }}
            </el-tag>
            <span v-if="articleTags.length === 0">-</span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDate(articleData.createTime) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDate(articleData.updateTime) || '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 文章图片轮播 -->
        <div v-if="articleData.images && articleData.images.length > 0" class="article-images-section">
          <el-divider content-position="left">文章图片</el-divider>
          <div class="article-images-container">
            <el-image
              v-for="(imageUrl, index) in articleData.images"
              :key="index"
              :src="imageUrl"
              fit="cover"
              class="article-image-item"
              :preview-src-list="getAllArticleImages()"
              :initial-index="getImageIndexInAll('images', index)"
              @error="handleImageError"
            >
              <template #error>
                <div class="image-error-small">
                  <el-icon :size="20"><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </div>
        </div>

        <el-divider content-position="left">文章内容</el-divider>

        <div class="article-content" ref="contentRef" v-html="articleData.content"></div>
      </div>
    </el-card>

    <!-- 图片预览对话框 -->
    <el-dialog
      v-model="imagePreviewVisible"
      title="图片预览"
      width="90%"
      :close-on-click-modal="true"
      class="image-preview-dialog"
    >
      <div class="image-preview-container">
        <img
          :src="previewImageUrl"
          alt="预览"
          class="preview-image"
          @error="handlePreviewImageError"
        />
        <div v-if="imagePreviewList.length > 1" class="image-preview-nav">
          <el-button
            :icon="ArrowLeft"
            circle
            @click="prevImage"
            :disabled="currentImageIndex === 0"
          />
          <span class="image-index">{{ currentImageIndex + 1 }} / {{ imagePreviewList.length }}</span>
          <el-button
            :icon="ArrowRight"
            circle
            @click="nextImage"
            :disabled="currentImageIndex === imagePreviewList.length - 1"
          />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowRight, Picture } from '@element-plus/icons-vue'
import { getArticleById, getArticleTagList } from '@/api/articles'
import { getSignedUrlByUrl } from '@/api/file'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const contentRef = ref(null)

// 图片预览相关
const imagePreviewVisible = ref(false)
const previewImageUrl = ref('')
const imagePreviewList = ref([])
const currentImageIndex = ref(0)

// 封面图重试标志，避免重复重试
const coverImageRetried = ref(false)
const coverImageOriginalUrl = ref('')

// 文章数据
const articleData = reactive({
  id: null,
  title: '',
  categoryId: null,
  categoryName: '',
  summary: '',
  coverImage: '',
  images: [], // 轮播图列表
  author: '',
  status: 0,
  publishTime: null,
  content: '',
  viewCount: 0,
  likeCount: 0,
  createTime: null,
  updateTime: null,
  tagIds: [],
})

// 标签列表
const tagList = ref([])
const articleTags = ref([])

// 加载文章详情
const loadDetail = async () => {
  loading.value = true
  try {
    const res = await getArticleById(route.params.id)
    if (res.data) {
      const data = res.data
      console.log('文章详情数据:', data)
      console.log('封面图URL:', data.coverImage)
      
      articleData.id = data.id
      articleData.title = data.title || ''
      articleData.categoryId = data.categoryId || null
      articleData.categoryName = data.categoryName || ''
      articleData.summary = data.summary || ''
      articleData.coverImage = data.coverImage || ''
      articleData.images = Array.isArray(data.images) ? data.images : []
      coverImageOriginalUrl.value = data.coverImage || ''
      coverImageRetried.value = false
      articleData.author = data.author || ''
      articleData.status = data.status !== undefined ? data.status : 0
      articleData.publishTime = data.publishTime || null
      articleData.content = data.content || ''
      articleData.viewCount = data.viewCount || 0
      articleData.likeCount = data.likeCount || 0
      articleData.createTime = data.createTime || null
      articleData.updateTime = data.updateTime || null

      // 加载标签
      if (data.tagIds && Array.isArray(data.tagIds)) {
        loadArticleTags(data.tagIds)
      } else if (data.tags && Array.isArray(data.tags)) {
        articleTags.value = data.tags
      } else {
        articleTags.value = []
      }
      
      // 等待DOM更新后，为文章内容中的图片添加预览功能
      nextTick(() => {
        setupImagePreview()
      })
    }
  } catch (error) {
    console.error('加载文章详情失败:', error)
    ElMessage.error('加载文章详情失败')
  } finally {
    loading.value = false
  }
}

// 封面图加载错误处理
const handleCoverImageError = async (e) => {
  const currentUrl = articleData.coverImage
  console.error('封面图加载失败:', currentUrl)
  
  // 如果已经重试过，不再重试，避免循环
  if (coverImageRetried.value) {
    return
  }
  
  // 如果当前URL是签名URL，尝试使用原始URL获取新的签名URL
  const originalUrl = coverImageOriginalUrl.value || currentUrl
  if (originalUrl && isOssUrl(originalUrl) && !currentUrl.includes('?')) {
    // 当前URL可能是未签名的OSS URL，尝试获取签名URL
    coverImageRetried.value = true
    try {
      console.log('尝试获取签名URL:', originalUrl)
      const response = await getSignedUrlByUrl(originalUrl)
      if (response && response.code === 200 && response.data) {
        console.log('获取到签名URL:', response.data)
        articleData.coverImage = response.data
        // 不显示提示，让图片自然加载
        return
      }
    } catch (error) {
      console.error('获取签名URL失败:', error)
    }
  }
  
  // 标记已重试，避免再次触发
  coverImageRetried.value = true
}

// 判断是否为OSS URL
const isOssUrl = (url) => {
  if (!url || typeof url !== 'string') {
    return false
  }
  const ossPatterns = [
    /oss-.*\.aliyuncs\.com/i,
    /\.oss\./i,
    /\.qcloud\.com/i,
    /\.amazonaws\.com/i,
    /\.cos\./i
  ]
  return ossPatterns.some(pattern => pattern.test(url))
}

// 收集所有文章图片（封面+轮播+内容），按编辑顺序
// 注意：允许重复图片，保持编辑时的顺序
const getAllArticleImages = () => {
  const allImages = []
  
  // 1. 封面图
  if (articleData.coverImage) {
    allImages.push(articleData.coverImage)
  }
  
  // 2. 轮播图（按编辑顺序）
  if (articleData.images && Array.isArray(articleData.images)) {
    articleData.images.forEach(url => {
      if (url) {
        allImages.push(url)
      }
    })
  }
  
  // 3. 文章内容中的图片（按在HTML中的顺序）
  if (contentRef.value) {
    const contentImages = contentRef.value.querySelectorAll('img')
    contentImages.forEach(img => {
      // 优先使用当前src（可能是签名URL），如果没有则使用原始URL
      const src = img.src || img.getAttribute('data-original-src')
      if (src) {
        allImages.push(src)
      }
    })
  }
  
  return allImages
}

// 获取图片在所有图片列表中的索引
const getImageIndexInAll = (type, index) => {
  let offset = 0
  
  // 封面图索引为0
  if (articleData.coverImage) {
    offset = 1
  }
  
  if (type === 'images') {
    // 轮播图：封面图之后
    return offset + index
  } else if (type === 'content') {
    // 内容图片：封面图 + 轮播图之后
    const imagesCount = articleData.images ? articleData.images.length : 0
    return offset + imagesCount + index
  }
  
  return 0
}

// 为文章内容中的图片添加预览功能和错误处理
const setupImagePreview = () => {
  if (!contentRef.value) {
    return
  }
  
  const images = contentRef.value.querySelectorAll('img')
  if (images.length === 0) {
    return
  }
  
  // 收集所有图片URL（使用原始src，避免签名URL变化）
  const imageUrls = Array.from(images).map(img => {
    // 保存原始URL
    const originalSrc = img.getAttribute('data-original-src') || img.src
    if (!img.getAttribute('data-original-src')) {
      img.setAttribute('data-original-src', img.src)
    }
    return originalSrc
  }).filter(Boolean)
  
  // 为每个图片添加点击事件和错误处理
  images.forEach((img, index) => {
    // 添加鼠标样式
    img.style.cursor = 'pointer'
    
    // 保存原始URL
    const originalSrc = img.getAttribute('data-original-src') || img.src
    if (!img.getAttribute('data-original-src')) {
      img.setAttribute('data-original-src', originalSrc)
    }
    
    // 添加点击事件
    img.addEventListener('click', () => {
      // 使用所有文章图片列表，找到当前图片的索引
      const allImages = getAllArticleImages()
      // 找到当前图片在所有图片中的索引
      const currentSrc = img.src || img.getAttribute('data-original-src')
      const globalIndex = allImages.findIndex(url => {
        // 比较URL（可能签名URL不同，但原始URL相同）
        const originalUrl = img.getAttribute('data-original-src') || currentSrc
        return url === currentSrc || url === originalUrl
      })
      showImagePreview(allImages, globalIndex >= 0 ? globalIndex : getImageIndexInAll('content', index))
    })
    
    // 添加错误处理
    img.addEventListener('error', async (e) => {
      const currentSrc = img.src
      const originalUrl = img.getAttribute('data-original-src') || currentSrc
      
      console.error('文章内容图片加载失败:', currentSrc)
      
      // 如果已经重试过，不再重试
      if (img.getAttribute('data-retried') === 'true') {
        return
      }
      
      // 如果是OSS URL且未签名，尝试获取签名URL
      if (originalUrl && isOssUrl(originalUrl) && !currentSrc.includes('?')) {
        img.setAttribute('data-retried', 'true')
        try {
          console.log('尝试为文章内容图片获取签名URL:', originalUrl)
          const response = await getSignedUrlByUrl(originalUrl)
          if (response && response.code === 200 && response.data) {
            console.log('获取到签名URL:', response.data)
            img.src = response.data
            // 更新data-original-src为新的签名URL
            img.setAttribute('data-original-src', response.data)
            return
          }
        } catch (error) {
          console.error('获取签名URL失败:', error)
        }
      }
      
      // 标记已重试
      img.setAttribute('data-retried', 'true')
    })
  })
}

// 显示图片预览
const showImagePreview = async (urlList, initialIndex = 0) => {
  if (!urlList || urlList.length === 0) {
    return
  }
  
  // 处理URL列表，如果是OSS URL且未签名，尝试获取签名URL
  const processedUrls = await Promise.all(
    urlList.map(async (url) => {
      if (url && isOssUrl(url) && !url.includes('?')) {
        try {
          const response = await getSignedUrlByUrl(url)
          if (response && response.code === 200 && response.data) {
            return response.data
          }
        } catch (error) {
          console.error('获取预览图片签名URL失败:', error)
        }
      }
      return url
    })
  )
  
  imagePreviewList.value = processedUrls.filter(Boolean)
  currentImageIndex.value = Math.min(initialIndex, imagePreviewList.value.length - 1)
  previewImageUrl.value = imagePreviewList.value[currentImageIndex.value]
  imagePreviewVisible.value = true
}

// 上一张图片
const prevImage = () => {
  if (currentImageIndex.value > 0) {
    currentImageIndex.value--
    previewImageUrl.value = imagePreviewList.value[currentImageIndex.value]
  }
}

// 下一张图片
const nextImage = () => {
  if (currentImageIndex.value < imagePreviewList.value.length - 1) {
    currentImageIndex.value++
    previewImageUrl.value = imagePreviewList.value[currentImageIndex.value]
  }
}

// 预览图片加载错误
const handlePreviewImageError = () => {
  ElMessage.error('图片加载失败')
}

// 轮播图加载错误处理
const handleImageError = (e) => {
  console.error('轮播图加载失败:', e)
}

// 加载标签列表并匹配文章标签
const loadTags = async () => {
  try {
    const res = await getArticleTagList()
    if (res.data) {
      tagList.value = res.data || []
    }
  } catch (error) {
    console.error('加载标签列表失败:', error)
  }
}

// 根据tagIds加载标签信息
const loadArticleTags = (tagIds) => {
  articleTags.value = tagList.value.filter((tag) => tagIds.includes(tag.id))
}

// 返回
const handleBack = () => {
  router.push('/articles')
}

// 编辑
const handleEdit = () => {
  router.push(`/articles/edit/${route.params.id}`)
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

onMounted(() => {
  loadTags().then(() => {
    loadDetail()
  })
})
</script>

<style scoped>
.article-detail-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.article-detail-content {
  padding: 20px 0;
}

.article-content {
  min-height: 200px;
  padding: 20px;
  background-color: #fafafa;
  border-radius: 4px;
}

:deep(.article-content img) {
  max-width: 100%;
  height: auto;
  cursor: pointer;
  transition: opacity 0.3s;
  display: block;
}

:deep(.article-content img:hover) {
  opacity: 0.8;
}

/* 图片加载失败时的占位符样式 */
:deep(.article-content img[data-retried="true"]) {
  min-height: 100px;
  background-color: #f5f7fa;
  border: 1px dashed #dcdfe6;
  position: relative;
}

:deep(.article-content img[data-retried="true"]::after) {
  content: '图片加载失败，点击查看';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #909399;
  font-size: 12px;
  white-space: nowrap;
}

:deep(.article-content p) {
  margin-bottom: 10px;
  line-height: 1.6;
}

:deep(.article-content h1),
:deep(.article-content h2),
:deep(.article-content h3),
:deep(.article-content h4),
:deep(.article-content h5),
:deep(.article-content h6) {
  margin-top: 20px;
  margin-bottom: 10px;
  font-weight: bold;
}

/* 图片错误样式 */
.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 150px;
  background-color: #f5f7fa;
  color: #909399;
  border-radius: 4px;
}

.error-text {
  margin-top: 10px;
  font-size: 14px;
  color: #909399;
}

.no-image {
  color: #909399;
}

/* 文章图片轮播区域 */
.article-images-section {
  margin: 20px 0;
}

.article-images-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 0 20px;
}

.article-image-item {
  width: 150px;
  height: 150px;
  border-radius: 4px;
  cursor: pointer;
  transition: transform 0.3s;
}

.article-image-item:hover {
  transform: scale(1.05);
}

.image-error-small {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background-color: #f5f7fa;
  color: #909399;
}

/* 图片预览对话框样式 */
.image-preview-dialog :deep(.el-dialog__body) {
  padding: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.image-preview-container {
  position: relative;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.preview-image {
  max-width: 100%;
  max-height: 70vh;
  object-fit: contain;
  border-radius: 4px;
}

.image-preview-nav {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 10px 20px;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 20px;
  color: white;
}

.image-index {
  font-size: 14px;
  min-width: 60px;
  text-align: center;
}
</style>
