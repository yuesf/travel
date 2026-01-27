<template>
  <div class="ticket-category-manager">
    <div class="manager-header">
      <el-button type="primary" @click="handleAdd">添加分类</el-button>
    </div>

    <el-table :data="tableData" border style="width: 100%" v-loading="loading">
      <el-table-column prop="name" label="分类名称" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="sort" label="排序" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editForm.id ? '编辑分类' : '添加分类'"
      width="500px"
    >
      <el-form :model="editForm" label-width="120px">
        <el-form-item label="分类名称">
          <el-input v-model="editForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="editForm.sort" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="editForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getTicketCategoryList,
  createTicketCategory,
  updateTicketCategory,
  deleteTicketCategory,
} from '@/api/attractionBooking'

const props = defineProps({
  attractionId: {
    type: [String, Number],
    required: true,
  },
})

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)

const editForm = reactive({
  id: null,
  name: '',
  description: '',
  sort: 0,
  status: 1,
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getTicketCategoryList(props.attractionId)
    if (res.data) {
      tableData.value = res.data || []
    }
  } catch (error) {
    console.error('加载分类失败:', error)
    ElMessage.error('加载分类失败')
  } finally {
    loading.value = false
  }
}

// 添加
const handleAdd = () => {
  editForm.id = null
  editForm.name = ''
  editForm.description = ''
  editForm.sort = 0
  editForm.status = 1
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  editForm.id = row.id
  editForm.name = row.name
  editForm.description = row.description || ''
  editForm.sort = row.sort
  editForm.status = row.status
  dialogVisible.value = true
}

// 提交
const handleSubmit = async () => {
  if (!editForm.name) {
    ElMessage.warning('请输入分类名称')
    return
  }

  try {
    if (editForm.id) {
      await updateTicketCategory(props.attractionId, editForm.id, {
        name: editForm.name,
        description: editForm.description,
        sort: editForm.sort,
        status: editForm.status,
      })
      ElMessage.success('更新成功')
    } else {
      await createTicketCategory(props.attractionId, {
        name: editForm.name,
        description: editForm.description,
        sort: editForm.sort,
        status: editForm.status,
      })
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error(error.message || '操作失败')
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该分类吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteTicketCategory(props.attractionId, row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.ticket-category-manager {
  padding: 20px 0;
}

.manager-header {
  margin-bottom: 20px;
}
</style>
