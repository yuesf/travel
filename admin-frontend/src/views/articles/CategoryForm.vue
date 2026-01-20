<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑分类' : '创建分类'"
    width="600px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
      <el-form-item label="分类名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入分类名称" />
      </el-form-item>

      <el-form-item label="描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入分类描述"
        />
      </el-form-item>

      <el-form-item label="排序" prop="sort">
        <el-input-number
          v-model="formData.sort"
          :min="0"
          :step="1"
          placeholder="请输入排序值"
          style="width: 100%"
        />
        <div class="form-tip">数字越小越靠前</div>
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { createArticleCategory, updateArticleCategory } from '@/api/articles'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  formData: {
    type: Object,
    default: () => ({}),
  },
  isEdit: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:modelValue', 'success'])

const formRef = ref(null)
const submitting = ref(false)

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
})

// 表单数据
const formData = reactive({
  name: '',
  description: '',
  sort: 0,
  status: 1, // 默认启用
})

// 表单验证规则
const formRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入排序值', trigger: 'blur' }],
}

// 监听外部数据变化
watch(
  () => props.formData,
  (newVal) => {
    if (newVal && Object.keys(newVal).length > 0) {
      formData.name = newVal.name || ''
      formData.description = newVal.description || ''
      formData.sort = newVal.sort !== undefined ? newVal.sort : 0
      formData.status = newVal.status !== undefined ? newVal.status : 1
    } else {
      // 重置表单
      formData.name = ''
      formData.description = ''
      formData.sort = 0
      formData.status = 1
    }
  },
  { immediate: true, deep: true }
)

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const submitData = {
          name: formData.name,
          description: formData.description,
          sort: formData.sort,
          status: formData.status,
        }

        if (props.isEdit && props.formData.id) {
          await updateArticleCategory(props.formData.id, submitData)
          ElMessage.success('更新成功')
        } else {
          await createArticleCategory(submitData)
          ElMessage.success('创建成功')
        }

        emit('success')
        handleClose()
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error(error.message || '保存失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false
  if (formRef.value) {
    formRef.value.resetFields()
  }
}
</script>

<style scoped>
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
