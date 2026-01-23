<template>
  <div class="map-list-container">
    <!-- 搜索筛选区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="地图名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入地图名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="全部" :value="null" />
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
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
          <el-button type="primary" @click="handleCreate" :icon="Plus">添加地图</el-button>
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
        <el-table-column prop="name" label="地图名称" min-width="150" show-overflow-tooltip />
        <el-table-column label="位置" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div>经度: {{ row.longitude }}</div>
            <div>纬度: {{ row.latitude }}</div>
            <div v-if="row.address" style="color: #909399; font-size: 12px;">{{ row.address }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="announcement" label="公告内容" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.announcement">{{ row.announcement }}</span>
            <span v-else style="color: #c0c4cc;">-</span>
          </template>
        </el-table-column>
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
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

      <!-- 空状态 -->
      <el-empty v-if="!loading && tableData.length === 0" description="暂无地图数据" />
    </el-card>

    <!-- 地图表单对话框 -->
    <MapForm
      v-model="formVisible"
      :map-id="currentMapId"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getMapList,
  deleteMap,
} from '@/api/maps'
import MapForm from './MapForm.vue'

// 搜索表单
const searchForm = reactive({
  name: '',
  status: null,
})

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 表单对话框
const formVisible = ref(false)
const currentMapId = ref(null)

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      name: searchForm.name || undefined,
      status: searchForm.status,
    }

    const res = await getMapList(params)
    if (res.data) {
      tableData.value = res.data || []
    }
  } catch (error) {
    console.error('加载地图列表失败:', error)
    ElMessage.error('加载地图列表失败')
    tableData.value = []
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.name = ''
  searchForm.status = null
  loadData()
}

// 创建
const handleCreate = () => {
  currentMapId.value = null
  formVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  currentMapId.value = row.id
  formVisible.value = true
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除地图"${row.name}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    loading.value = true
    try {
      await deleteMap(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除地图失败:', error)
      ElMessage.error('删除地图失败')
    } finally {
      loading.value = false
    }
  } catch (error) {
    // 用户取消
  }
}

// 表单提交成功
const handleFormSuccess = () => {
  formVisible.value = false
  currentMapId.value = null
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
.map-list-container {
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
</style>
