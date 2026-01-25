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
              :preview-src-list="[articleData.coverImage]"
              @error="handleCoverImageError"
            >
              <template #error>
                <div class="image-error">
                  <el-icon :size="40"><Picture /></el-icon>
                  <div class="error-text">加载失败</div>
                </div>
              </template>
            </el-image>
            <span v-else>-</span>
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

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const contentRef = ref(null)

// 图片预览相关
const imagePreviewVisible = ref(false)
const previewImageUrl = ref('')
const imagePreviewList = ref([])
const currentImageIndex = ref(0)

// 文章数据
const articleData = reactive({
  id: null,
  title: '',
  categoryId: null,
  categoryName: '',
  summary: '',
  coverImage: '',
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
      articleData.id = data.id
      articleData.title = data.title || ''
      articleData.categoryId = data.categoryId || null
      articleData.categoryName = data.categoryName || ''
      articleData.summary = data.summary || ''
      articleData.coverImage = data.coverImage || ''
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

// 为文章内容中的图片添加预览功能
const setupImagePreview = () => {
  if (!contentRef.value) {
    return
  }
  
  const images = contentRef.value.querySelectorAll('img')
  if (images.length === 0) {
    return
  }
  
  // 收集所有图片URL
  const imageUrls = Array.from(images).map(img => img.src).filter(Boolean)
  
  // 为每个图片添加点击事件
  images.forEach((img, index) => {
    // 添加鼠标样式
    img.style.cursor = 'pointer'
    
    // 添加点击事件
    img.addEventListener('click', () => {
      showImagePreview(imageUrls, index)
    })
  })
}

// 显示图片预览
const showImagePreview = (urlList, initialIndex = 0) => {
  if (!urlList || urlList.length === 0) {
    return
  }
  imagePreviewList.value = urlList
  currentImageIndex.value = initialIndex
  previewImageUrl.value = urlList[initialIndex]
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
}

:deep(.article-content img:hover) {
  opacity: 0.8;
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
