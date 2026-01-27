<template>
  <div class="golden-summit-manager">
    <div class="manager-header">
      <el-button type="primary" @click="handleAdd">添加时间段</el-button>
    </div>

    <el-table :data="tableData" border style="width: 100%" v-loading="loading">
      <el-table-column prop="bookingDate" label="日期" width="150" />
      <el-table-column prop="startTime" label="开始时间" width="120" />
      <el-table-column prop="endTime" label="结束时间" width="120" />
      <el-table-column prop="stock" label="库存" width="100" />
      <el-table-column prop="available" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.available === 1 ? 'success' : 'danger'">
            {{ row.available === 1 ? '可用' : '不可用' }}
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
      :title="editForm.id ? '编辑时间段' : '添加时间段'"
      width="500px"
    >
      <el-form :model="editForm" label-width="120px">
        <el-form-item label="日期">
          <el-date-picker
            v-model="editForm.bookingDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
            :disabled="!!editForm.id"
          />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-time-picker
            v-model="editForm.startTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="选择开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-time-picker
            v-model="editForm.endTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="选择结束时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="editForm.stock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="editForm.available">
            <el-radio :value="1">可用</el-radio>
            <el-radio :value="0">不可用</el-radio>
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
  getGoldenSummitTimeSlotList,
  createGoldenSummitTimeSlot,
  updateGoldenSummitTimeSlot,
  deleteGoldenSummitTimeSlot,
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
  bookingDate: null,
  startTime: null,
  endTime: null,
  stock: 0,
  available: 1,
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getGoldenSummitTimeSlotList(props.attractionId)
    if (res.data) {
      tableData.value = res.data || []
    }
  } catch (error) {
    console.error('加载时间段失败:', error)
    ElMessage.error('加载时间段失败')
  } finally {
    loading.value = false
  }
}

// 添加
const handleAdd = () => {
  editForm.id = null
  editForm.bookingDate = null
  editForm.startTime = null
  editForm.endTime = null
  editForm.stock = 0
  editForm.available = 1
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  editForm.id = row.id
  editForm.bookingDate = row.bookingDate
  editForm.startTime = row.startTime
  editForm.endTime = row.endTime
  editForm.stock = row.stock
  editForm.available = row.available
  dialogVisible.value = true
}

// 提交
const handleSubmit = async () => {
  if (!editForm.bookingDate) {
    ElMessage.warning('请选择日期')
    return
  }
  if (!editForm.startTime || !editForm.endTime) {
    ElMessage.warning('请选择时间段')
    return
  }

  try {
    if (editForm.id) {
      await updateGoldenSummitTimeSlot(props.attractionId, editForm.id, {
        startTime: editForm.startTime,
        endTime: editForm.endTime,
        stock: editForm.stock,
        available: editForm.available,
      })
      ElMessage.success('更新成功')
    } else {
      await createGoldenSummitTimeSlot(props.attractionId, {
        bookingDate: editForm.bookingDate,
        startTime: editForm.startTime,
        endTime: editForm.endTime,
        stock: editForm.stock,
        available: editForm.available,
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
    await ElMessageBox.confirm('确定要删除该时间段吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteGoldenSummitTimeSlot(props.attractionId, row.id)
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
.golden-summit-manager {
  padding: 20px 0;
}

.manager-header {
  margin-bottom: 20px;
}
</style>
