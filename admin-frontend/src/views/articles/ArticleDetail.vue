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
            />
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

        <div class="article-content" v-html="articleData.content"></div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getArticleById, getArticleTagList } from '@/api/articles'

const router = useRouter()
const route = useRoute()

const loading = ref(false)

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
    }
  } catch (error) {
    console.error('加载文章详情失败:', error)
    ElMessage.error('加载文章详情失败')
  } finally {
    loading.value = false
  }
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
</style>
