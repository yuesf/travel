<template>
  <div class="category-list-container">
    <!-- 操作按钮区域 -->
    <el-card class="toolbar-card" shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-button type="primary" @click="handleCreate" :icon="Plus">添加分类</el-button>
        </div>
        <div class="toolbar-right">
          <el-button :icon="Refresh" @click="loadData">刷新</el-button>
        </div>
      </div>
    </el-card>

    <!-- 分类树表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        border
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        default-expand-all
        style="width: 100%"
      >
        <el-table-column prop="name" label="分类名称" min-width="200">
          <template #default="{ row }">
            <span v-if="row.icon" class="category-icon">
              <el-image
                :src="getImageUrl(row)"
                fit="cover"
                style="width: 24px; height: 24px; margin-right: 8px; vertical-align: middle"
                @error="(e) => handleImageError(row, e)"
              />
            </span>
            {{ row.name }}
          </template>
        </el-table-column>
        <el-table-column prop="level" label="层级" width="100">
          <template #default="{ row }">
            <el-tag :type="row.level === 1 ? 'primary' : 'success'">
              {{ row.level === 1 ? '一级分类' : '二级分类' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.type === 'CONFIG' ? 'warning' : row.type === 'H5' ? 'danger' : 'info'">
              {{ row.type === 'CONFIG' ? '配置类型' : row.type === 'H5' ? 'H5类型' : '展示类型' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleAddChild(row)">
              添加子分类
            </el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              link
              type="danger"
              size="small"
              @click="handleDelete(row)"
              :disabled="row.status === 0"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分类表单对话框 -->
    <CategoryForm
      v-model="formDialogVisible"
      :form-data="currentFormData"
      :parent-category="parentCategory"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Plus } from '@element-plus/icons-vue'
import { getCategoryTree, deleteCategory, deleteCategoryWithProducts } from '@/api/categories'
import { getSignedUrlByUrl } from '@/api/file'
import CategoryForm from './CategoryForm.vue'

// 签名URL缓存
const signedUrlCache = ref(new Map())

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 表单对话框
const formDialogVisible = ref(false)
const currentFormData = ref(null)
const parentCategory = ref(null)

// 判断是否是OSS URL
const isOssUrl = (url) => {
  if (!url || typeof url !== 'string') {
    return false
  }
  // 检查是否是OSS URL：包含 oss- 或 aliyuncs.com 等常见OSS域名特征
  const ossPatterns = [
    /oss-.*\.aliyuncs\.com/i,
    /\.oss\./i,
    /\.qcloud\.com/i,
    /\.amazonaws\.com/i,
    /\.cos\./i,
  ]
  return url.startsWith('https://') && 
         !url.includes('localhost') && 
         ossPatterns.some(pattern => pattern.test(url))
}

// 递归处理分类树中的OSS URL（预加载签名URL到缓存）
const processCategoryOssUrls = async (categories) => {
  if (!categories || !Array.isArray(categories)) {
    return
  }
  
  for (const category of categories) {
    if (category.icon && isOssUrl(category.icon)) {
      // 如果缓存中没有，异步获取签名URL
      if (!signedUrlCache.value.has(category.icon)) {
        try {
          const response = await getSignedUrlByUrl(category.icon)
          if (response && response.code === 200 && response.data) {
            signedUrlCache.value.set(category.icon, response.data)
          }
        } catch (error) {
          console.warn('获取OSS签名URL失败:', category.icon, error)
        }
      }
    }
    
    // 递归处理子分类
    if (category.children && category.children.length > 0) {
      await processCategoryOssUrls(category.children)
    }
  }
}

// 获取图片URL（优先使用签名URL）
const getImageUrl = (row) => {
  if (!row.icon) {
    return ''
  }
  // 如果是OSS URL且缓存中有签名URL，使用签名URL
  if (isOssUrl(row.icon) && signedUrlCache.value.has(row.icon)) {
    return signedUrlCache.value.get(row.icon)
  }
  // 否则使用原始URL
  return row.icon
}

// 处理图片加载错误（作为后备方案，如果签名URL失效）
const handleImageError = async (row, event) => {
  const originalUrl = row.icon
  // 如果图片加载失败，可能是签名URL已过期，尝试重新获取
  if (originalUrl && isOssUrl(originalUrl)) {
    try {
      const response = await getSignedUrlByUrl(originalUrl)
      if (response && response.code === 200 && response.data) {
        // 更新缓存的签名URL
        signedUrlCache.value.set(originalUrl, response.data)
        // 触发重新渲染（通过更新tableData）
        tableData.value = [...tableData.value]
      }
    } catch (error) {
      console.warn('重新获取签名URL失败:', error)
    }
  }
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getCategoryTree()
    if (res.data) {
      tableData.value = res.data || []
      // 处理OSS URL，获取签名URL
      await processCategoryOssUrls(tableData.value)
    }
  } catch (error) {
    console.error('加载分类列表失败:', error)
    ElMessage.error('加载分类列表失败')
    tableData.value = []
  } finally {
    loading.value = false
  }
}

// 创建分类
const handleCreate = () => {
  currentFormData.value = null
  parentCategory.value = null
  formDialogVisible.value = true
}

// 添加子分类
const handleAddChild = (row) => {
  if (row.level >= 2) {
    ElMessage.warning('最多支持两级分类')
    return
  }
  currentFormData.value = null
  parentCategory.value = row
  formDialogVisible.value = true
}

// 编辑分类
const handleEdit = (row) => {
  currentFormData.value = { ...row }
  parentCategory.value = null
  formDialogVisible.value = true
}

// 删除分类
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除分类"${row.name}"吗？删除前会检查是否有商品使用该分类。`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    loading.value = true
    try {
      await deleteCategory(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除分类失败:', error)
      const errorMessage = error?.response?.data?.message || error?.message || '删除分类失败'
      
      // 检查错误消息是否包含商品数量信息
      if (errorMessage.includes('商品') && errorMessage.match(/\d+/)) {
        // 提取商品数量
        const match = errorMessage.match(/(\d+)\s*个商品/)
        const productCount = match ? match[1] : '0'
        
        // 显示确认对话框
        try {
          await ElMessageBox.confirm(
            `该分类下有 ${productCount} 个商品，删除分类将同时删除这些商品，确定要继续吗？`,
            '提示',
            {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning',
            }
          )
          
          // 用户确认，执行级联删除
          try {
            await deleteCategoryWithProducts(row.id)
            ElMessage.success('删除成功')
            loadData()
          } catch (deleteError) {
            console.error('级联删除失败:', deleteError)
            ElMessage.error(deleteError?.response?.data?.message || '删除分类失败')
          }
        } catch (confirmError) {
          // 用户取消级联删除
        }
      } else {
        ElMessage.error(errorMessage)
      }
    } finally {
      loading.value = false
    }
  } catch (error) {
    // 用户取消
    loading.value = false
  }
}

// 表单提交成功
const handleFormSuccess = () => {
  formDialogVisible.value = false
  loadData()
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
  loadData()
})
</script>


<style scoped>
.category-list-container {
  padding: 0;
}

.toolbar-card {
  margin-bottom: 16px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-card {
  margin-bottom: 16px;
}

.category-icon {
  display: inline-block;
  vertical-align: middle;
}
</style>
