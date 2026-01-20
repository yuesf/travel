<template>
  <el-dialog
    v-model="dialogVisible"
    :title="formData?.id ? '编辑分类' : parentCategory ? '添加子分类' : '添加分类'"
    width="600px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="formRules"
      label-width="100px"
    >
      <el-form-item label="父分类" v-if="parentCategory">
        <el-input :value="parentCategory.name" disabled />
      </el-form-item>

      <el-form-item label="分类名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入分类名称" />
      </el-form-item>

      <el-form-item label="分类图标">
        <div class="icon-upload-wrapper">
          <el-input
            v-model="form.icon"
            placeholder="请输入图标URL或上传图片"
            style="flex: 1; margin-right: 8px"
          />
          <ImageUpload
            v-model="form.icon"
            :limit="1"
            :max-size="2"
            style="display: inline-block"
          />
        </div>
        <div class="form-tip">支持图片URL或上传图片，建议尺寸：64x64px</div>
      </el-form-item>

      <el-form-item label="分类类型" prop="type">
        <el-radio-group v-model="form.type">
          <el-radio value="DISPLAY">展示类型</el-radio>
          <el-radio value="CONFIG">配置类型（用于Icon配置）</el-radio>
        </el-radio-group>
        <div class="form-tip">展示类型：用于商品分类展示；配置类型：用于Icon配置选择</div>
      </el-form-item>

      <el-form-item label="排序" prop="sort">
        <el-input-number
          v-model="form.sort"
          :min="0"
          :precision="0"
          placeholder="数字越小越靠前"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">禁用</el-radio>
        </el-radio-group>
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
import { createCategory, updateCategory } from '@/api/categories'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  formData: {
    type: Object,
    default: null,
  },
  parentCategory: {
    type: Object,
    default: null,
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
const form = reactive({
  name: '',
  icon: '',
  parentId: null,
  level: 1,
  sort: 0,
  status: 1,
  type: 'DISPLAY', // DISPLAY-展示类型，CONFIG-配置类型
})

// 表单验证规则
const formRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
}

// 初始化表单数据
const initForm = () => {
  if (props.formData) {
    // 编辑模式
    form.name = props.formData.name || ''
    form.icon = props.formData.icon || ''
    form.parentId = props.formData.parentId || null
    form.level = props.formData.level || 1
    form.sort = props.formData.sort || 0
    form.status = props.formData.status !== undefined ? props.formData.status : 1
    form.type = props.formData.type || 'DISPLAY'
  } else if (props.parentCategory) {
    // 添加子分类
    form.name = ''
    form.icon = ''
    form.parentId = props.parentCategory.id
    form.level = 2
    form.sort = 0
    form.status = 1
    form.type = 'DISPLAY'
  } else {
    // 添加一级分类
    form.name = ''
    form.icon = ''
    form.parentId = 0
    form.level = 1
    form.sort = 0
    form.status = 1
    form.type = 'DISPLAY'
  }
}

// 监听对话框显示
watch(
  () => props.modelValue,
  (val) => {
    if (val) {
      initForm()
    }
  },
  { immediate: true }
)

// 监听表单数据变化
watch(
  () => props.formData,
  () => {
    if (props.modelValue) {
      initForm()
    }
  }
)

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const submitData = {
          name: form.name,
          icon: form.icon || null,
          parentId: form.parentId,
          level: form.level,
          sort: form.sort,
          status: form.status,
          type: form.type,
        }

        if (props.formData?.id) {
          await updateCategory(props.formData.id, submitData)
          ElMessage.success('更新成功')
        } else {
          await createCategory(submitData)
          ElMessage.success('创建成功')
        }

        emit('success')
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
.icon-upload-wrapper {
  display: flex;
  align-items: center;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
