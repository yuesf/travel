<template>
  <div class="product-list-container">
    <!-- 搜索筛选区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="商品名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入商品名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-cascader
            v-model="searchForm.categoryId"
            :options="categoryOptions"
            :props="{ value: 'id', label: 'name', children: 'children', checkStrictly: true, emitPath: false }"
            placeholder="请选择分类"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="价格区间">
          <el-input-number
            v-model="searchForm.minPrice"
            :precision="2"
            :step="0.01"
            :min="0"
            placeholder="最低价"
            style="width: 120px"
          />
          <span style="margin: 0 8px">-</span>
          <el-input-number
            v-model="searchForm.maxPrice"
            :precision="2"
            :step="0.01"
            :min="0"
            placeholder="最高价"
            style="width: 120px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="全部" :value="null" />
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :icon="Search">搜索</el-button>
          <el-button @click="handleReset" :icon="Refresh">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮区域 -->
    <el-card class="toolbar-card" shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-button type="primary" @click="handleCreate" :icon="Plus">添加商品</el-button>
        </div>
        <div class="toolbar-right">
          <el-button :icon="Refresh" @click="loadData">刷新</el-button>
        </div>
      </div>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="商品图片" width="100">
          <template #default="{ row }">
            <el-image
              v-if="row.images && row.images.length > 0"
              :src="getProductImageUrl(row.images[0])"
              :preview-src-list="getProductImageList(row.images)"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 4px"
              @error="() => handleProductImageError(row, $event)"
            >
              <template #error>
                <div class="image-slot">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="price" label="价格" width="100" sortable="custom">
          <template #default="{ row }">
            <span v-if="row.price">¥{{ row.price }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="originalPrice" label="原价" width="100">
          <template #default="{ row }">
            <span v-if="row.originalPrice" style="text-decoration: line-through; color: #999">
              ¥{{ row.originalPrice }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" sortable="custom" />
        <el-table-column prop="sales" label="销量" width="100" sortable="custom" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" sortable="custom">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="handleUpdateStock(row)">库存</el-button>
            <el-button
              v-if="row.status === 1"
              link
              type="warning"
              size="small"
              @click="handleOffline(row)"
            >
              下架
            </el-button>
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

    <!-- 更新库存对话框 -->
    <el-dialog v-model="stockDialogVisible" title="更新库存" width="400px">
      <el-form :model="stockForm" label-width="80px">
        <el-form-item label="商品名称">
          <el-input v-model="stockForm.productName" disabled />
        </el-form-item>
        <el-form-item label="当前库存">
          <el-input-number v-model="stockForm.currentStock" disabled style="width: 100%" />
        </el-form-item>
        <el-form-item label="新库存" required>
          <el-input-number
            v-model="stockForm.stock"
            :min="0"
            :precision="0"
            style="width: 100%"
            placeholder="请输入库存数量"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleStockSubmit" :loading="stockSubmitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Picture } from '@element-plus/icons-vue'
import {
  getProductList,
  deleteProduct,
  offlineProduct,
  updateProductStock,
} from '@/api/products'
import { getCategoryTree } from '@/api/categories'
import { getSignedUrlByUrl } from '@/api/file'

const router = useRouter()

// 搜索表单
const searchForm = reactive({
  name: '',
  categoryId: null,
  minPrice: null,
  maxPrice: null,
  status: null,
})

// 分类选项
const categoryOptions = ref([])

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

// 库存对话框
const stockDialogVisible = ref(false)
const stockSubmitting = ref(false)
const stockForm = reactive({
  productId: null,
  productName: '',
  currentStock: 0,
  stock: 0,
})

// 加载分类树
const loadCategories = async () => {
  try {
    const res = await getCategoryTree({ status: 1 })
    if (res.data) {
      categoryOptions.value = res.data || []
    }
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      name: searchForm.name || undefined,
      categoryId: searchForm.categoryId || undefined,
      minPrice: searchForm.minPrice || undefined,
      maxPrice: searchForm.maxPrice || undefined,
      status: searchForm.status,
    }

    const res = await getProductList(params)
    if (res.data) {
      tableData.value = res.data.list || []
      pagination.total = res.data.total || 0
      
      // 批量预加载所有未签名OSS图片的签名URL
      const imageUrls = []
      tableData.value.forEach(product => {
        if (product.images && Array.isArray(product.images)) {
          product.images.forEach(url => {
            if (url && isUnsignedOssUrl(url)) {
              imageUrls.push(url)
            }
          })
        }
      })
      
      // 异步获取所有未签名URL的签名URL
      if (imageUrls.length > 0) {
        Promise.all(imageUrls.map(url => getSignedUrlForPreview(url)))
          .then(() => {
            // 获取完成后，强制更新列表以触发重新渲染
            tableData.value = [...tableData.value]
          })
          .catch(error => {
            console.warn('批量获取签名URL失败:', error)
          })
      }
    }
  } catch (error) {
    console.error('加载商品列表失败:', error)
    ElMessage.error('加载商品列表失败')
    tableData.value = []
    pagination.total = 0
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
  searchForm.name = ''
  searchForm.categoryId = null
  searchForm.minPrice = null
  searchForm.maxPrice = null
  searchForm.status = null
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

// 创建
const handleCreate = () => {
  router.push('/products/create')
}

// 查看（跳转到编辑页面查看详情，只读模式）
const handleView = (row) => {
  router.push(`/products/edit/${row.id}?view=true`)
}

// 编辑
const handleEdit = (row) => {
  router.push(`/products/edit/${row.id}`)
}

// 更新库存
const handleUpdateStock = (row) => {
  stockForm.productId = row.id
  stockForm.productName = row.name
  stockForm.currentStock = row.stock || 0
  stockForm.stock = row.stock || 0
  stockDialogVisible.value = true
}

// 提交库存更新
const handleStockSubmit = async () => {
  if (stockForm.stock === null || stockForm.stock < 0) {
    ElMessage.warning('请输入有效的库存数量')
    return
  }

  stockSubmitting.value = true
  try {
    await updateProductStock(stockForm.productId, stockForm.stock)
    ElMessage.success('更新库存成功')
    stockDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('更新库存失败:', error)
    ElMessage.error('更新库存失败')
  } finally {
    stockSubmitting.value = false
  }
}

// 下架
const handleOffline = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要下架商品"${row.name}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    loading.value = true
    try {
      await offlineProduct(row.id)
      ElMessage.success('下架成功')
      loadData()
    } catch (error) {
      console.error('下架商品失败:', error)
      ElMessage.error(error?.response?.data?.message || '下架商品失败')
    } finally {
      loading.value = false
    }
  } catch (error) {
    // 用户取消
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除商品"${row.name}"吗？删除后无法恢复。`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    loading.value = true
    try {
      await deleteProduct(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除商品失败:', error)
      ElMessage.error(error?.response?.data?.message || '删除商品失败')
    } finally {
      loading.value = false
    }
  } catch (error) {
    // 用户取消
  }
}

/**
 * 判断是否为OSS URL（未签名的）
 */
const isUnsignedOssUrl = (url) => {
  if (!url || typeof url !== 'string') {
    return false
  }
  // 检查是否是OSS URL（包含oss-*.aliyuncs.com等）
  const ossPatterns = [
    /oss-.*\.aliyuncs\.com/i,
    /\.oss\./i,
    /\.qcloud\.com/i,
    /\.amazonaws\.com/i,
    /\.cos\./i
  ]
  
  // 如果是签名URL（包含查询参数），则不是未签名的
  if (url.includes('?') && (url.includes('Expires=') || url.includes('Signature='))) {
    return false
  }
  
  // 检查是否匹配OSS URL模式
  return ossPatterns.some(pattern => pattern.test(url))
}

// 签名URL缓存（避免重复请求）
const signedUrlCache = new Map()

/**
 * 为OSS URL获取签名URL（用于预览）
 */
const getSignedUrlForPreview = async (url) => {
  if (!isUnsignedOssUrl(url)) {
    return url // 不是未签名的OSS URL，直接返回
  }
  
  // 检查缓存
  if (signedUrlCache.has(url)) {
    return signedUrlCache.get(url)
  }
  
  try {
    const response = await getSignedUrlByUrl(url)
    if (response && response.code === 200 && response.data) {
      // 缓存签名URL（1小时后过期）
      signedUrlCache.set(url, response.data)
      setTimeout(() => {
        signedUrlCache.delete(url)
      }, 3600000) // 1小时
      return response.data
    }
  } catch (error) {
    console.warn('获取签名URL失败:', error)
  }
  
  // 如果获取失败，返回原URL（可能会403，但至少不会报错）
  return url
}

/**
 * 获取商品图片URL（自动处理OSS URL签名）
 */
const getProductImageUrl = (url) => {
  if (!url) {
    return ''
  }
  
  // 如果是未签名的OSS URL，返回缓存中的签名URL或原URL
  if (isUnsignedOssUrl(url)) {
    return signedUrlCache.get(url) || url
  }
  
  return url
}

/**
 * 获取商品图片列表（自动处理OSS URL签名）
 */
const getProductImageList = (images) => {
  if (!images || !Array.isArray(images) || images.length === 0) {
    return []
  }
  
  return images.map(url => getProductImageUrl(url))
}

/**
 * 处理商品图片加载错误
 */
const handleProductImageError = async (row, event) => {
  console.error('商品图片加载失败:', row)
  
  if (!row.images || row.images.length === 0) {
    return
  }
  
  try {
    const imageUrl = row.images[0]
    
    // 如果是未签名的OSS URL，尝试获取签名URL
    if (imageUrl && isUnsignedOssUrl(imageUrl)) {
      const signedUrl = await getSignedUrlForPreview(imageUrl)
      if (signedUrl !== imageUrl) {
        // 更新缓存，触发重新渲染
        signedUrlCache.set(imageUrl, signedUrl)
        // 更新商品数据中的图片URL
        const index = tableData.value.findIndex(item => item.id === row.id)
        if (index > -1) {
          // 更新图片数组中的第一个URL
          const updatedImages = [...row.images]
          updatedImages[0] = signedUrl
          tableData.value[index] = {
            ...tableData.value[index],
            images: updatedImages,
          }
          tableData.value = [...tableData.value]
        }
      }
    }
  } catch (error) {
    console.error('处理商品图片加载错误失败:', error)
  }
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
  loadCategories()
  loadData()
})
</script>

<style scoped>
.product-list-container {
  padding: 0;
}

.search-card {
  margin-bottom: 16px;
}

.search-form {
  margin: 0;
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

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 20px;
}
</style>
