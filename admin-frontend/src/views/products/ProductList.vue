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
              :src="row.images[0]"
              :preview-src-list="row.images"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 4px"
            />
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
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="handleUpdateStock(row)">库存</el-button>
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
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getProductList,
  deleteProduct,
  updateProductStock,
} from '@/api/products'
import { getCategoryTree } from '@/api/categories'

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

// 查看（跳转到编辑页面查看详情）
const handleView = (row) => {
  router.push(`/products/edit/${row.id}`)
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

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除商品"${row.name}"吗？删除后状态将改为下架。`,
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
      ElMessage.error('删除商品失败')
    } finally {
      loading.value = false
    }
  } catch (error) {
    // 用户取消
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
</style>
