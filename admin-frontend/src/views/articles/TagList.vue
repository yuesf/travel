<template>
  <div class="tag-list-container">
    <!-- 操作按钮区域 -->
    <el-card class="toolbar-card" shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-button type="primary" @click="handleCreate" :icon="Plus">添加标签</el-button>
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
        <el-table-column prop="name" label="标签名称" min-width="150" />
        <el-table-column prop="color" label="颜色" width="120">
          <template #default="{ row }">
            <div v-if="row.color" class="color-cell">
              <span
                :style="{ color: row.color }"
                class="color-dot"
              >●</span>
              <span>{{ row.color }}</span>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="useCount" label="使用次数" width="120" sortable="custom" />
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
              :disabled="row.useCount > 0"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 标签表单对话框 -->
    <TagForm
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
import { getArticleTagList, deleteArticleTag } from '@/api/articles'
import TagForm from './TagForm.vue'

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
    const res = await getArticleTagList()
    if (res.data) {
      tableData.value = res.data || []
    }
  } catch (error) {
    console.error('加载标签列表失败:', error)
    ElMessage.error('加载标签列表失败')
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
  if (row.useCount > 0) {
    ElMessage.warning('该标签正在使用中，无法删除')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除标签"${row.name}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    loading.value = true
    try {
      await deleteArticleTag(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除标签失败:', error)
      ElMessage.error(error.message || '删除标签失败')
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
.tag-list-container {
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

.color-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.color-dot {
  font-size: 16px;
}
</style>
