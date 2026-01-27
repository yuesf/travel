<template>
  <div class="hotel-list-container">
    <!-- 搜索筛选区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="酒店名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入酒店名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="城市">
          <el-input
            v-model="searchForm.city"
            placeholder="请输入城市"
            clearable
            style="width: 150px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="星级">
          <el-select v-model="searchForm.starLevel" placeholder="请选择星级" clearable style="width: 120px">
            <el-option label="全部" :value="null" />
            <el-option label="1星" :value="1" />
            <el-option label="2星" :value="2" />
            <el-option label="3星" :value="3" />
            <el-option label="4星" :value="4" />
            <el-option label="5星" :value="5" />
          </el-select>
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
          <el-button type="primary" @click="handleCreate" :icon="Plus">添加酒店</el-button>
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
        <el-table-column label="酒店图片" width="100">
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
        <el-table-column prop="name" label="酒店名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            {{ `${row.province || ''}${row.city || ''}${row.district || ''}${row.address || ''}` }}
          </template>
        </el-table-column>
        <el-table-column prop="starLevel" label="星级" width="100">
          <template #default="{ row }">
            <span v-if="row.starLevel">{{ row.starLevel }}星</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="房型数量" width="100">
          <template #default="{ row }">
            {{ row.roomCount !== undefined ? row.roomCount : '-' }}
          </template>
        </el-table-column>
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getHotelList,
  deleteHotel,
} from '@/api/hotels'

const router = useRouter()

// 搜索表单
const searchForm = reactive({
  name: '',
  city: '',
  starLevel: null,
  status: null,
})

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      name: searchForm.name || undefined,
      city: searchForm.city || undefined,
      starLevel: searchForm.starLevel,
      status: searchForm.status,
    }

    const res = await getHotelList(params)
    if (res.data) {
      tableData.value = res.data.list || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载酒店列表失败:', error)
    ElMessage.error('加载酒店列表失败')
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
  searchForm.city = ''
  searchForm.starLevel = null
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
  router.push('/hotels/create')
}

// 查看（跳转到详情页面）
const handleView = (row) => {
  router.push(`/hotels/${row.id}`)
}

// 编辑
const handleEdit = (row) => {
  router.push(`/hotels/edit/${row.id}`)
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除酒店"${row.name}"吗？删除后状态将改为下架。`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    loading.value = true
    try {
      await deleteHotel(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除酒店失败:', error)
      ElMessage.error('删除酒店失败')
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
  loadData()
})
</script>

<style scoped>
.hotel-list-container {
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
