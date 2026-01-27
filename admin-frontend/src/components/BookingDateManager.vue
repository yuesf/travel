<template>
  <div class="booking-date-manager">
    <div class="manager-header">
      <el-button type="primary" @click="handleBatchAdd">批量添加日期</el-button>
    </div>

    <el-table :data="tableData" border style="width: 100%" v-loading="loading">
      <el-table-column prop="bookingDate" label="日期" width="150" />
      <el-table-column prop="price" label="价格" width="120">
        <template #default="{ row }">
          ¥{{ row.price }}
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="100" />
      <el-table-column prop="available" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.available === 1 ? 'success' : 'danger'">
            {{ row.available === 1 ? '可订' : '不可订' }}
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

    <!-- 批量添加对话框 -->
    <el-dialog v-model="batchDialogVisible" title="批量添加日期" width="600px">
      <el-form :model="batchForm" label-width="120px">
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="batchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number
            v-model="batchForm.price"
            :precision="2"
            :step="0.01"
            :min="0"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="batchForm.stock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="batchForm.available">
            <el-radio :value="1">可订</el-radio>
            <el-radio :value="0">不可订</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatchSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 编辑对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑可订日期" width="500px">
      <el-form :model="editForm" label-width="120px">
        <el-form-item label="日期">
          <el-date-picker
            v-model="editForm.bookingDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
            disabled
          />
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
          <el-input-number v-model="editForm.stock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="editForm.available">
            <el-radio :value="1">可订</el-radio>
            <el-radio :value="0">不可订</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getBookingDateList,
  batchCreateBookingDate,
  updateBookingDate,
  deleteBookingDate,
} from '@/api/attractionBooking'

const props = defineProps({
  attractionId: {
    type: [String, Number],
    required: true,
  },
})

const loading = ref(false)
const tableData = ref([])
const batchDialogVisible = ref(false)
const editDialogVisible = ref(false)

const batchForm = reactive({
  dateRange: null,
  price: null,
  stock: 0,
  available: 1,
})

const editForm = reactive({
  id: null,
  bookingDate: null,
  price: null,
  stock: 0,
  available: 1,
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getBookingDateList(props.attractionId)
    if (res.data) {
      tableData.value = res.data || []
    }
  } catch (error) {
    console.error('加载可订日期失败:', error)
    ElMessage.error('加载可订日期失败')
  } finally {
    loading.value = false
  }
}

// 批量添加
const handleBatchAdd = () => {
  batchForm.dateRange = null
  batchForm.price = null
  batchForm.stock = 0
  batchForm.available = 1
  batchDialogVisible.value = true
}

// 批量提交
const handleBatchSubmit = async () => {
  if (!batchForm.dateRange || !batchForm.dateRange.length) {
    ElMessage.warning('请选择日期范围')
    return
  }
  if (batchForm.price === null) {
    ElMessage.warning('请输入价格')
    return
  }

  try {
    const [startDate, endDate] = batchForm.dateRange
    const dates = []
    const currentDate = new Date(startDate)
    const end = new Date(endDate)

    while (currentDate <= end) {
      dates.push({
        bookingDate: currentDate.toISOString().split('T')[0],
        price: batchForm.price,
        stock: batchForm.stock,
        available: batchForm.available,
      })
      currentDate.setDate(currentDate.getDate() + 1)
    }

    await batchCreateBookingDate(props.attractionId, { dates })
    ElMessage.success('批量添加成功')
    batchDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('批量添加失败:', error)
    ElMessage.error(error.message || '批量添加失败')
  }
}

// 编辑
const handleEdit = (row) => {
  editForm.id = row.id
  editForm.bookingDate = row.bookingDate
  editForm.price = parseFloat(row.price)
  editForm.stock = row.stock
  editForm.available = row.available
  editDialogVisible.value = true
}

// 编辑提交
const handleEditSubmit = async () => {
  if (editForm.price === null) {
    ElMessage.warning('请输入价格')
    return
  }

  try {
    await updateBookingDate(props.attractionId, editForm.id, {
      price: editForm.price,
      stock: editForm.stock,
      available: editForm.available,
    })
    ElMessage.success('更新成功')
    editDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('更新失败:', error)
    ElMessage.error(error.message || '更新失败')
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该可订日期吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteBookingDate(props.attractionId, row.id)
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
.booking-date-manager {
  padding: 20px 0;
}

.manager-header {
  margin-bottom: 20px;
}
</style>
