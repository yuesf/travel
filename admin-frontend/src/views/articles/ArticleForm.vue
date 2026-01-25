<template>
  <div class="article-form-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑文章' : '创建文章' }}</span>
          <div>
            <el-button @click="handleCancel">取消</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="submitting">
              保存
            </el-button>
          </div>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        class="article-form"
      >
        <el-tabs v-model="activeTab" type="border-card">
          <!-- Tab 1: 基本信息 -->
          <el-tab-pane label="基本信息" name="basic">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="文章标题" prop="title">
                  <el-input v-model="formData.title" placeholder="请输入文章标题" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="文章分类" prop="categoryId">
                  <el-select
                    v-model="formData.categoryId"
                    placeholder="请选择分类"
                    style="width: 100%"
                    filterable
                  >
                    <el-option
                      v-for="category in categoryList"
                      :key="category.id"
                      :label="category.name"
                      :value="category.id"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="文章摘要" prop="summary">
              <el-input
                v-model="formData.summary"
                type="textarea"
                :rows="3"
                placeholder="请输入文章摘要"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="文章图片" prop="images">
              <div style="display: flex; align-items: flex-start; gap: 12px; flex-wrap: wrap;">
                <ImageUpload
                  v-model="formData.images"
                  :limit="10"
                  :max-size="5"
                  @change="handleImagesChange"
                />
                <el-button 
                  type="info" 
                  :icon="Folder"
                  @click="handleSelectImagesFromFileList"
                >
                  从文件库选择
                </el-button>
              </div>
              <div class="form-tip">最多上传10张图片，支持拖拽排序，单张图片不超过5MB。第一张图片将自动作为封面图</div>
            </el-form-item>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="作者" prop="author">
                  <el-input v-model="formData.author" placeholder="请输入作者" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="状态" prop="status">
                  <el-radio-group v-model="formData.status">
                    <el-radio :value="0">草稿</el-radio>
                    <el-radio :value="1">已发布</el-radio>
                    <el-radio :value="2">已下架</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="发布时间" prop="publishTime">
              <el-date-picker
                v-model="formData.publishTime"
                type="datetime"
                placeholder="选择发布时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
              <div class="form-tip">不选择则立即发布，选择后将在指定时间自动发布</div>
            </el-form-item>
          </el-tab-pane>

          <!-- Tab 2: 文章内容 -->
          <el-tab-pane label="文章内容" name="content">
            <el-form-item label="文章内容" prop="content">
              <RichTextEditor
                v-model="formData.content"
                placeholder="请输入文章内容..."
                @change="handleContentChange"
              />
            </el-form-item>
          </el-tab-pane>

          <!-- Tab 3: 标签设置 -->
          <el-tab-pane label="标签设置" name="tags">
            <el-form-item label="选择标签">
              <el-select
                v-model="formData.tagIds"
                multiple
                placeholder="请选择标签"
                style="width: 100%"
                filterable
                @change="handleTagsChange"
              >
                <el-option
                  v-for="tag in tagList"
                  :key="tag.id"
                  :label="tag.name"
                  :value="tag.id"
                >
                  <span style="float: left">{{ tag.name }}</span>
                  <span
                    v-if="tag.color"
                    style="float: right; color: #8492a6; font-size: 13px"
                  >
                    <span
                      :style="{ color: tag.color }"
                      class="tag-color-dot"
                    >●</span>
                  </span>
                </el-option>
              </el-select>
              <div class="form-tip">可以选择多个标签，也可以创建新标签</div>
            </el-form-item>

            <el-form-item label="已选标签">
              <div v-if="selectedTags.length === 0" class="no-tags">暂无标签</div>
              <div v-else class="selected-tags">
                <el-tag
                  v-for="tag in selectedTags"
                  :key="tag.id"
                  :type="tag.color ? '' : 'info'"
                  :style="tag.color ? { color: tag.color, borderColor: tag.color } : {}"
                  closable
                  @close="handleRemoveTag(tag.id)"
                  style="margin-right: 8px; margin-bottom: 8px"
                >
                  {{ tag.name }}
                </el-tag>
              </div>
            </el-form-item>

            <el-form-item label="创建新标签">
              <el-input
                v-model="newTagName"
                placeholder="请输入新标签名称"
                style="width: 200px; margin-right: 10px"
                @keyup.enter="handleCreateTag"
              />
              <el-color-picker v-model="newTagColor" />
              <el-button type="primary" @click="handleCreateTag" style="margin-left: 10px">
                创建标签
              </el-button>
            </el-form-item>
          </el-tab-pane>
        </el-tabs>
      </el-form>
    </el-card>

    <!-- 文件选择器对话框 -->
    <FileSelector
      v-model="fileSelectorVisible"
      file-type="image"
      :multiple="true"
      :max-select="10"
      @select="handleFileSelect"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Folder } from '@element-plus/icons-vue'
import ImageUpload from '@/components/ImageUpload.vue'
import RichTextEditor from '@/components/RichTextEditor.vue'
import FileSelector from '@/components/FileSelector.vue'
import {
  getArticleById,
  createArticle,
  updateArticle,
  getArticleCategoryList,
  getArticleTagList,
  createArticleTag,
  addTagToArticle,
  removeTagFromArticle,
  getArticleImages,
  saveArticleImages,
} from '@/api/articles'
import { getSignedUrlByUrl } from '@/api/file'

const router = useRouter()
const route = useRoute()

const formRef = ref(null)
const activeTab = ref('basic')
const submitting = ref(false)
const fileSelectorVisible = ref(false)

const isEdit = computed(() => !!route.params.id)

// 分类列表
const categoryList = ref([])

// 标签列表
const tagList = ref([])

// 表单数据
const formData = reactive({
  title: '',
  categoryId: null,
  summary: '',
  coverImage: '',
  images: [], // 文章图片列表
  author: '',
  status: 0, // 默认草稿
  publishTime: null,
  content: '',
  tagIds: [],
})

// 新标签
const newTagName = ref('')
const newTagColor = ref('#409EFF')

// 已选标签
const selectedTags = computed(() => {
  return tagList.value.filter((tag) => formData.tagIds.includes(tag.id))
})

// 表单验证规则
const formRules = {
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择文章分类', trigger: 'change' }],
  summary: [{ required: true, message: '请输入文章摘要', trigger: 'blur' }],
  content: [{ required: true, message: '请输入文章内容', trigger: 'blur' }],
}

// 加载分类列表
const loadCategories = async () => {
  try {
    // 加载所有分类（不传status参数）
    const res = await getArticleCategoryList()
    if (res.data) {
      // 过滤掉 id 为 null 或 undefined 的分类，避免 el-option 报错
      categoryList.value = (res.data || []).filter(cat => cat && cat.id != null)
    }
  } catch (error) {
    console.error('加载分类列表失败:', error)
  }
}

// 加载标签列表
const loadTags = async () => {
  try {
    const res = await getArticleTagList()
    if (res.data) {
      // 过滤掉 id 为 null 或 undefined 的标签，避免 el-option 报错
      tagList.value = (res.data || []).filter(tag => tag && tag.id != null)
    }
  } catch (error) {
    console.error('加载标签列表失败:', error)
  }
}

// 加载详情数据
const loadDetail = async () => {
  if (!isEdit.value) return

  try {
    const res = await getArticleById(route.params.id)
    if (res.data) {
      const data = res.data
      formData.title = data.title || ''
      formData.categoryId = data.categoryId || null
      formData.summary = data.summary || ''
      formData.coverImage = data.coverImage || ''
      formData.author = data.author || ''
      formData.status = data.status !== undefined ? data.status : 0
      formData.publishTime = data.publishTime || null
      // 处理文章内容中的图片URL
      formData.content = await processContentImages(data.content || '')
      // 加载文章的标签
      if (data.tagIds && Array.isArray(data.tagIds)) {
        formData.tagIds = data.tagIds
      } else if (data.tags && Array.isArray(data.tags)) {
        formData.tagIds = data.tags.map((tag) => tag.id)
      } else {
        formData.tagIds = []
      }
      
      // 加载文章图片列表
      try {
        const imagesRes = await getArticleImages(route.params.id)
        if (imagesRes.data && Array.isArray(imagesRes.data)) {
          formData.images = imagesRes.data.map((img) => img.imageUrl).filter(Boolean)
          // 如果文章图片列表有数据，优先使用第一张作为封面图
          if (formData.images.length > 0) {
            formData.coverImage = formData.images[0]
          } else if (!formData.coverImage) {
            // 如果没有文章图片且封面图也为空，保持为空
            formData.coverImage = ''
          }
        } else {
          formData.images = []
          // 如果没有文章图片，但原有封面图存在，保持封面图（向后兼容）
          if (!formData.coverImage) {
            formData.coverImage = ''
          }
        }
      } catch (error) {
        console.error('加载文章图片失败:', error)
        formData.images = []
        // 加载失败时，如果封面图存在则保持，否则为空
        if (!formData.coverImage) {
          formData.coverImage = ''
        }
      }
    }
  } catch (error) {
    console.error('加载文章详情失败:', error)
    ElMessage.error('加载文章详情失败')
  }
}

// 从文章图片列表同步封面图
const syncCoverImageFromImages = () => {
  if (formData.images && formData.images.length > 0) {
    formData.coverImage = formData.images[0]
  } else {
    formData.coverImage = ''
  }
}

// 文章图片变化
const handleImagesChange = (urls) => {
  formData.images = Array.isArray(urls) ? urls : []
  // 自动同步封面图
  syncCoverImageFromImages()
}

// 打开文件选择器选择文章图片
const handleSelectImagesFromFileList = () => {
  fileSelectorVisible.value = true
}

// 处理文件选择
const handleFileSelect = (files) => {
  if (files && files.length > 0) {
    // 选择文章图片
    const urls = files.map((file) => file.fileUrl || file.url).filter(Boolean)
    if (urls.length > 0) {
      // 合并到现有图片列表，去重
      const existingUrls = formData.images || []
      const newUrls = [...existingUrls, ...urls]
      // 去重
      const uniqueUrls = [...new Set(newUrls)]
      // 限制最多10张
      if (uniqueUrls.length > 10) {
        formData.images = uniqueUrls.slice(0, 10)
        ElMessage.warning('最多只能添加10张图片，已自动截取前10张')
      } else {
        formData.images = uniqueUrls
      }
      // 自动同步封面图
      syncCoverImageFromImages()
      ElMessage.success(`已添加 ${urls.length} 张图片`)
    }
  }
  fileSelectorVisible.value = false
}

// 处理文章内容中的图片URL，为OSS URL获取签名URL
// 注意：无论URL是否已签名，都尝试获取新的签名URL，确保图片能正常显示
const processContentImages = async (html) => {
  if (!html || typeof html !== 'string') {
    return html
  }
  
  // 匹配所有img标签的src属性
  const imgRegex = /<img([^>]+)src\s*=\s*["']([^"']+)["']([^>]*)>/gi
  const matches = [...html.matchAll(imgRegex)]
  
  if (matches.length === 0) {
    return html
  }
  
  // 处理每个图片URL
  let processedHtml = html
  for (const match of matches) {
    const fullMatch = match[0]
    const beforeSrc = match[1]
    const imageUrl = match[2]
    const afterSrc = match[3]
    
    // 如果是OSS URL，无论是否已签名，都尝试获取新的签名URL
    // 这样可以确保即使签名URL过期，也能获取新的签名URL
    if (isOssUrl(imageUrl)) {
      try {
        // 如果是签名URL，先提取原始URL（去掉查询参数）
        const baseUrl = imageUrl.includes('?') 
          ? imageUrl.substring(0, imageUrl.indexOf('?'))
          : imageUrl
        
        const response = await getSignedUrlByUrl(baseUrl)
        if (response && response.code === 200 && response.data) {
          // 替换为新的签名URL
          const signedUrl = response.data
          const newImgTag = `<img${beforeSrc}src="${signedUrl}"${afterSrc}>`
          processedHtml = processedHtml.replace(fullMatch, newImgTag)
        }
      } catch (error) {
        console.error('获取图片签名URL失败:', error)
        // 如果获取失败，保持原URL不变
      }
    }
  }
  
  return processedHtml
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

// 内容变化
const handleContentChange = (content) => {
  formData.content = content
}

// 标签变化
const handleTagsChange = (tagIds) => {
  formData.tagIds = tagIds
}

// 移除标签
const handleRemoveTag = (tagId) => {
  const index = formData.tagIds.indexOf(tagId)
  if (index > -1) {
    formData.tagIds.splice(index, 1)
  }
}

// 创建新标签
const handleCreateTag = async () => {
  if (!newTagName.value.trim()) {
    ElMessage.warning('请输入标签名称')
    return
  }

  try {
    const res = await createArticleTag({
      name: newTagName.value.trim(),
      color: newTagColor.value,
    })
    if (res.data) {
      ElMessage.success('标签创建成功')
      // 添加到标签列表
      tagList.value.push(res.data)
      // 自动选中新创建的标签
      formData.tagIds.push(res.data.id)
      // 清空输入
      newTagName.value = ''
      newTagColor.value = '#409EFF'
    }
  } catch (error) {
    console.error('创建标签失败:', error)
    ElMessage.error(error.message || '创建标签失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        // 提交前确保封面图已同步
        syncCoverImageFromImages()
        
        const submitData = {
          title: formData.title,
          categoryId: formData.categoryId,
          summary: formData.summary,
          coverImage: formData.coverImage,
          author: formData.author,
          status: formData.status,
          publishTime: formData.publishTime,
          content: formData.content,
          tagIds: formData.tagIds,
        }

        let articleId
        if (isEdit.value) {
          articleId = route.params.id
          await updateArticle(articleId, submitData)
          ElMessage.success('更新成功')
        } else {
          const createRes = await createArticle(submitData)
          articleId = createRes.data?.id
          ElMessage.success('创建成功')
        }

        // 保存文章图片
        if (articleId) {
          try {
            const imageUrls = Array.isArray(formData.images) ? formData.images : []
            await saveArticleImages(articleId, imageUrls)
            console.log('文章图片保存成功')
          } catch (error) {
            console.error('保存文章图片失败:', error)
            // 图片保存失败不影响文章保存，只记录错误
            ElMessage.warning('文章保存成功，但图片保存失败，请稍后重新上传图片')
          }
        }

        router.push('/articles')
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error(error.message || '保存失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 取消
const handleCancel = () => {
  ElMessageBox.confirm('确定要取消吗？未保存的数据将丢失。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      router.push('/articles')
    })
    .catch(() => {
      // 用户取消
    })
}

onMounted(() => {
  loadCategories()
  loadTags()
  loadDetail()
})
</script>

<style scoped>
.article-form-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.article-form {
  padding: 20px 0;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.no-tags {
  color: #909399;
  font-size: 14px;
}

.selected-tags {
  min-height: 40px;
}

.tag-color-dot {
  font-size: 16px;
}

:deep(.el-tabs__content) {
  padding: 20px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}
</style>
