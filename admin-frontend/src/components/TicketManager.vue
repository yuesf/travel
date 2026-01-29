<template>
  <div class="ticket-manager">
    <div class="manager-header">
      <el-button type="primary" @click="handleAdd">添加票种</el-button>
      <el-select
        v-model="selectedCategoryId"
        placeholder="筛选分类"
        clearable
        style="width: 200px; margin-left: 10px"
        @change="loadData"
      >
        <el-option
          v-for="category in categories"
          :key="category.id"
          :label="category.name"
          :value="category.id"
        />
      </el-select>
    </div>

    <el-table :data="tableData" border style="width: 100%" v-loading="loading">
      <el-table-column prop="name" label="票种名称" />
      <el-table-column prop="price" label="价格" width="120">
        <template #default="{ row }">
          ¥{{ row.price }}
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="120" />
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
      :title="editForm.id ? '编辑票种' : '添加票种'"
      width="600px"
    >
      <el-form :model="editForm" label-width="120px">
        <el-form-item label="票种分类">
          <el-select
            v-model="editForm.categoryId"
            placeholder="请选择分类"
            style="width: 100%"
            :disabled="!!editForm.id"
          >
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="票种名称">
          <el-input v-model="editForm.name" placeholder="请输入票种名称" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number
            v-model="editForm.price"
            :precision="2"
            :step="0.01"
            :min="0"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number
            v-model="editForm.stock"
            :min="0"
            style="width: 100%"
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
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getTicketList,
  getTicketCategoryList,
  createTicket,
  updateTicket,
  deleteTicket,
} from '@/api/attractionBooking'

const props = defineProps({
  attractionId: {
    type: [String, Number],
    required: true,
  },
})

const loading = ref(false)
const tableData = ref([])
const categories = ref([])
const selectedCategoryId = ref(null)
const dialogVisible = ref(false)

const editForm = reactive({
  id: null,
  categoryId: null,
  name: '',
  price: null,
  stock: null,
  sort: 0,
  status: 1,
})

// 加载分类
const loadCategories = async () => {
  try {
    const res = await getTicketCategoryList(props.attractionId)
    if (res.data) {
      categories.value = res.data || []
    }
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = selectedCategoryId.value ? { categoryId: selectedCategoryId.value } : {}
    const res = await getTicketList(props.attractionId, params)
    if (res.data) {
      tableData.value = res.data || []
    }
  } catch (error) {
    console.error('加载票种失败:', error)
    ElMessage.error('加载票种失败')
  } finally {
    loading.value = false
  }
}

// 添加
const handleAdd = () => {
  editForm.id = null
  editForm.categoryId = null
  editForm.name = ''
  editForm.price = null
  editForm.stock = null
  editForm.sort = 0
  editForm.status = 1
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  editForm.id = row.id
  editForm.categoryId = row.categoryId
  editForm.name = row.name
  editForm.price = parseFloat(row.price)
  editForm.stock = row.stock != null ? row.stock : null
  editForm.sort = row.sort
  editForm.status = row.status
  dialogVisible.value = true
}

// 提交
const handleSubmit = async () => {
  if (!editForm.categoryId) {
    ElMessage.warning('请选择票种分类')
    return
  }
  if (!editForm.name) {
    ElMessage.warning('请输入票种名称')
    return
  }
  if (editForm.price === null) {
    ElMessage.warning('请输入价格')
    return
  }
  if (editForm.stock === null) {
    ElMessage.warning('请输入库存')
    return
  }

  try {
    if (editForm.id) {
      await updateTicket(props.attractionId, editForm.id, {
        name: editForm.name,
        price: editForm.price,
        stock: editForm.stock,
        sort: editForm.sort,
        status: editForm.status,
      })
      ElMessage.success('更新成功')
    } else {
      await createTicket(props.attractionId, {
        categoryId: editForm.categoryId,
        name: editForm.name,
        price: editForm.price,
        stock: editForm.stock,
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
    await ElMessageBox.confirm('确定要删除该票种吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteTicket(props.attractionId, row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 监听对话框打开，自动刷新分类列表
watch(dialogVisible, (visible) => {
  if (visible) {
    // 对话框打开时重新加载分类列表，确保获取最新数据
    loadCategories()
  }
})

onMounted(() => {
  loadCategories()
  loadData()
})
</script>

<style scoped>
.ticket-manager {
  padding: 20px 0;
}

.manager-header {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}
</style>
