<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑标签' : '创建标签'"
    width="600px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
      <el-form-item label="标签名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入标签名称" />
      </el-form-item>

      <el-form-item label="标签颜色" prop="color">
        <el-color-picker v-model="formData.color" />
        <div class="form-tip">选择标签显示颜色，用于区分不同标签</div>
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
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createArticleTag, updateArticleTag } from '@/api/articles'

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
  color: '#409EFF',
})

// 表单验证规则
const formRules = {
  name: [{ required: true, message: '请输入标签名称', trigger: 'blur' }],
}

// 监听外部数据变化
watch(
  () => props.formData,
  (newVal) => {
    if (newVal && Object.keys(newVal).length > 0) {
      formData.name = newVal.name || ''
      formData.color = newVal.color || '#409EFF'
    } else {
      // 重置表单
      formData.name = ''
      formData.color = '#409EFF'
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
          color: formData.color,
        }

        if (props.isEdit && props.formData.id) {
          await updateArticleTag(props.formData.id, submitData)
          ElMessage.success('更新成功')
        } else {
          await createArticleTag(submitData)
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
