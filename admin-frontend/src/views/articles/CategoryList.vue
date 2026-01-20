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

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        border
        style="width: 100%"
        row-key="id"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="100" sortable="custom">
          <template #default="{ row }">
            <el-input-number
              v-model="row.sort"
              :min="0"
              :step="1"
              size="small"
              style="width: 100px"
              @change="handleSortChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="articleCount" label="文章数" width="100" />
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
              :disabled="row.articleCount > 0"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分类表单对话框 -->
    <CategoryForm
      v-model="formVisible"
      :form-data="formData"
      :is-edit="isEdit"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { getArticleCategoryList, deleteArticleCategory, updateArticleCategory } from '@/api/articles'
import CategoryForm from './CategoryForm.vue'

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 表单对话框
const formVisible = ref(false)
const formData = ref({})
const isEdit = ref(false)

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getArticleCategoryList()
    if (res.data) {
      tableData.value = res.data || []
    }
  } catch (error) {
    console.error('加载分类列表失败:', error)
    ElMessage.error('加载分类列表失败')
    tableData.value = []
  } finally {
    loading.value = false
  }
}

// 创建
const handleCreate = () => {
  formData.value = {}
  isEdit.value = false
  formVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  formData.value = { ...row }
  isEdit.value = true
  formVisible.value = true
}

// 删除
const handleDelete = async (row) => {
  if (row.articleCount > 0) {
    ElMessage.warning('该分类下有文章，无法删除')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除分类"${row.name}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    loading.value = true
    try {
      await deleteArticleCategory(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除分类失败:', error)
      ElMessage.error(error.message || '删除分类失败')
    } finally {
      loading.value = false
    }
  } catch (error) {
    // 用户取消
  }
}

// 排序变化
const handleSortChange = async (row) => {
  try {
    await updateArticleCategory(row.id, {
      name: row.name,
      description: row.description,
      sort: row.sort,
      status: row.status,
    })
    ElMessage.success('排序更新成功')
  } catch (error) {
    console.error('更新排序失败:', error)
    ElMessage.error('更新排序失败')
    // 重新加载数据恢复原值
    loadData()
  }
}

// 表单提交成功
const handleFormSuccess = () => {
  formVisible.value = false
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
</style>
