<template>
  <el-select
    :model-value="modelValue"
    placeholder="请输入文章标题搜索"
    clearable
    filterable
    remote
    reserve-keyword
    :remote-method="handleSearch"
    :loading="loading"
    style="width: 100%"
    @update:model-value="handleChange"
  >
    <el-option
      v-for="item in articleList"
      :key="item.id"
      :label="item.title"
      :value="item.id"
    >
      <div style="display: flex; justify-content: space-between; align-items: center">
        <span>{{ item.title }}</span>
        <span style="color: #909399; font-size: 12px; margin-left: 10px">
          {{ formatDate(item.publishTime) }}
        </span>
      </div>
    </el-option>
  </el-select>
</template>

<script setup>
import { ref, watch } from 'vue'
import { getArticleList } from '@/api/articles'
import { ElMessage } from 'element-plus'

// Props
const props = defineProps({
  modelValue: {
    type: [Number, String],
    default: null,
  },
  categoryId: {
    type: [Number, String],
    default: null,
  },
})

// Emits
const emit = defineEmits(['update:modelValue', 'change'])

// 数据
const loading = ref(false)
const articleList = ref([])
let searchTimer = null

/**
 * 格式化日期
 */
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

/**
 * 搜索文章
 */
const searchArticles = async (query = '') => {
  loading.value = true
  try {
    const params = {
      page: 1,
      pageSize: 20,
      status: 1, // 只显示已发布的文章
    }
    
    if (query) {
      params.title = query
    }
    
    // 如果有分类 ID，添加分类过滤
    if (props.categoryId) {
      params.categoryId = props.categoryId
    }
    
    const res = await getArticleList(params)
    if (res.data && res.data.list) {
      articleList.value = res.data.list
    } else {
      articleList.value = []
    }
  } catch (error) {
    console.error('搜索文章失败:', error)
    ElMessage.error('搜索文章失败')
  } finally {
    loading.value = false
  }
}

/**
 * 处理搜索（带防抖）
 */
const handleSearch = (query) => {
  // 清除之前的定时器
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  
  // 设置新的定时器，300ms 防抖
  searchTimer = setTimeout(() => {
    searchArticles(query)
  }, 300)
}

/**
 * 处理选择变化
 */
const handleChange = (value) => {
  emit('update:modelValue', value)
  
  // 查找完整的文章对象
  if (value) {
    const article = articleList.value.find((a) => a.id === value)
    emit('change', article || null)
  } else {
    emit('change', null)
  }
}

// 监听分类 ID 变化，重新加载文章
watch(
  () => props.categoryId,
  () => {
    searchArticles()
  }
)

// 初始加载一些文章（可选，用于默认显示）
searchArticles()
</script>
