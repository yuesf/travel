<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑房型' : '添加房型'"
    width="800px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="房型名称" prop="roomType">
            <el-input
              v-model="formData.roomType"
              placeholder="请输入房型名称"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="价格" prop="price">
            <el-input-number
              v-model="formData.price"
              :precision="2"
              :step="0.01"
              :min="0"
              placeholder="请输入价格"
              style="width: 100%"
            />
            <span class="form-unit">元/晚</span>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="库存" prop="stock">
            <el-input-number
              v-model="formData.stock"
              :min="0"
              :precision="0"
              placeholder="请输入库存"
              style="width: 100%"
            />
            <span class="form-unit">间</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="床型">
            <el-input
              v-model="formData.bedType"
              placeholder="例如：大床、双床、单人床"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="面积">
            <el-input-number
              v-model="formData.area"
              :precision="2"
              :step="1"
              :min="0"
              placeholder="请输入面积"
              style="width: 100%"
            />
            <span class="form-unit">平方米</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="formData.status">
              <el-radio :value="1">上架</el-radio>
              <el-radio :value="0">下架</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="设施">
        <el-input
          v-model="facilitiesText"
          type="textarea"
          :rows="3"
          placeholder="请输入设施，多个设施用逗号分隔，例如：WiFi,空调,电视,冰箱"
          @blur="handleFacilitiesChange"
        />
        <div class="form-tip">多个设施用逗号分隔</div>
      </el-form-item>

      <el-form-item label="房型图片">
        <ImageUpload
          v-model="formData.images"
          :limit="9"
          :max-size="5"
        />
        <div class="form-tip">最多上传9张图片，单张图片不超过5MB</div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import ImageUpload from '@/components/ImageUpload.vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  roomData: {
    type: Object,
    default: null,
  },
  hotelId: {
    type: Number,
    required: true,
  },
})

const emit = defineEmits(['update:modelValue', 'submit'])

const formRef = ref(null)
const submitting = ref(false)

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
})

const isEdit = computed(() => !!props.roomData?.id)

// 表单数据
const formData = reactive({
  roomType: '',
  price: null,
  stock: null,
  bedType: '',
  area: null,
  facilities: [],
  images: [],
  status: 1, // 默认上架
})

// 设施文本（用于输入）
const facilitiesText = ref('')

// 表单验证规则
const formRules = {
  roomType: [{ required: true, message: '请输入房型名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
}

// 重置表单
const resetForm = () => {
  formData.roomType = ''
  formData.price = null
  formData.stock = null
  formData.bedType = ''
  formData.area = null
  formData.facilities = []
  formData.images = []
  formData.status = 1
  facilitiesText.value = ''
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 监听roomData变化，初始化表单
watch(
  () => props.roomData,
  (newData) => {
    if (newData) {
      formData.roomType = newData.roomType || ''
      formData.price = newData.price ? parseFloat(newData.price) : null
      formData.stock = newData.stock || null
      formData.bedType = newData.bedType || ''
      formData.area = newData.area ? parseFloat(newData.area) : null
      formData.facilities = Array.isArray(newData.facilities) ? newData.facilities : []
      formData.images = Array.isArray(newData.images) ? newData.images : []
      formData.status = newData.status !== undefined ? newData.status : 1

      // 初始化设施文本
      facilitiesText.value = Array.isArray(formData.facilities)
        ? formData.facilities.join(',')
        : ''
    } else {
      // 重置表单
      resetForm()
    }
  },
  { immediate: true }
)

// 监听visible变化，重置表单
watch(visible, (val) => {
  if (!val) {
    resetForm()
  }
})

// 处理设施变化
const handleFacilitiesChange = () => {
  if (facilitiesText.value.trim()) {
    formData.facilities = facilitiesText.value
      .split(',')
      .map((item) => item.trim())
      .filter((item) => item)
  } else {
    formData.facilities = []
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      // 处理设施
      handleFacilitiesChange()

      submitting.value = true
      try {
        const submitData = {
          hotelId: props.hotelId,
          roomType: formData.roomType,
          price: formData.price,
          stock: formData.stock,
          bedType: formData.bedType || undefined,
          area: formData.area || undefined,
          facilities: formData.facilities.length > 0 ? formData.facilities : undefined,
          images: Array.isArray(formData.images) ? formData.images : [],
          status: formData.status,
        }

        emit('submit', {
          ...submitData,
          id: isEdit.value ? props.roomData.id : undefined,
        })

        visible.value = false
      } catch (error) {
        console.error('提交失败:', error)
        ElMessage.error(error.message || '提交失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
}
</script>

<style scoped>
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.form-unit {
  margin-left: 8px;
  color: #909399;
}
</style>
