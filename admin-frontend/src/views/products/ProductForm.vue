<template>
  <div class="product-form-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑商品' : '创建商品' }}</span>
          <div>
            <el-button @click="handleCancel">取消</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="submitting">
              保存
            </el-button>
          </div>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        class="product-form"
      >
        <el-tabs v-model="activeTab" type="border-card" @tab-change="handleTabChange">
          <!-- Tab 1: 基本信息 -->
          <el-tab-pane label="基本信息" name="basic">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="商品名称" prop="name">
                  <el-input 
                    id="product-name"
                    v-model="formData.name" 
                    placeholder="请输入商品名称" 
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="商品分类" prop="categoryId">
                  <el-cascader
                    id="product-category"
                    v-model="formData.categoryId"
                    :options="categoryOptions"
                    :props="{ 
                      value: 'id', 
                      label: 'name', 
                      children: 'children', 
                      checkStrictly: true, 
                      emitPath: false 
                    }"
                    placeholder="请选择商品分类"
                    clearable
                    style="width: 100%"
                    :filterable="false"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="价格" prop="price">
                  <el-input-number
                    id="product-price"
                    v-model="formData.price"
                    :precision="2"
                    :step="0.01"
                    :min="0"
                    placeholder="请输入价格"
                    style="width: 100%"
                  />
                  <span class="form-unit">元</span>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="原价">
                  <el-input-number
                    id="product-original-price"
                    v-model="formData.originalPrice"
                    :precision="2"
                    :step="0.01"
                    :min="0"
                    placeholder="请输入原价（可选）"
                    style="width: 100%"
                  />
                  <span class="form-unit">元</span>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="库存" prop="stock">
                  <el-input-number
                    id="product-stock"
                    v-model="formData.stock"
                    :min="0"
                    :precision="0"
                    placeholder="请输入库存"
                    style="width: 100%"
                  />
                  <span class="form-unit">件</span>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="状态" prop="status">
                  <el-radio-group id="product-status" v-model="formData.status">
                    <el-radio :value="1">上架</el-radio>
                    <el-radio :value="0">下架</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="商品描述">
              <RichTextEditor
                id="product-description"
                v-model="formData.description"
                placeholder="请输入商品描述"
              />
            </el-form-item>
          </el-tab-pane>

          <!-- Tab 2: 商品图片 -->
          <el-tab-pane label="商品图片" name="images">
            <el-form-item label="商品图片">
              <div id="product-images">
                <ImageUpload
                  ref="imageUploadRef"
                  v-model="formData.images"
                  :limit="9"
                  :max-size="5"
                  @change="handleImagesChange"
                />
              </div>
              <div class="form-tip">最多上传9张图片，单张图片不超过5MB，支持JPG、PNG、WebP格式</div>
            </el-form-item>
          </el-tab-pane>

          <!-- Tab 3: 商品规格 -->
          <el-tab-pane label="商品规格" name="specifications">
            <el-form-item label="规格设置">
              <el-radio-group id="product-spec-mode" v-model="specMode" style="margin-bottom: 16px">
                <el-radio value="form">表单模式</el-radio>
                <el-radio value="json">JSON模式</el-radio>
              </el-radio-group>

              <!-- 表单模式 -->
              <div v-if="specMode === 'form'" class="spec-form-mode">
                <div v-for="(item, index) in specFormItems" :key="index" class="spec-form-item">
                  <el-input
                    v-model="item.key"
                    placeholder="规格名称"
                    style="width: 200px; margin-right: 8px"
                  />
                  <el-input
                    v-model="item.value"
                    placeholder="规格值"
                    style="width: 200px; margin-right: 8px"
                  />
                  <el-button
                    type="danger"
                    :icon="Delete"
                    circle
                    @click="removeSpecFormItem(index)"
                  />
                </div>
                <el-button
                  type="primary"
                  :icon="Plus"
                  @click="addSpecFormItem"
                  style="margin-top: 8px"
                >
                  添加规格
                </el-button>
              </div>

              <!-- JSON模式 -->
              <div v-else class="spec-json-mode">
                <el-input
                  v-model="specJsonText"
                  type="textarea"
                  :rows="10"
                  placeholder='请输入JSON格式的规格，例如：{"颜色": "红色", "尺寸": "L", "材质": "纯棉"}'
                  @blur="handleSpecJsonChange"
                />
                <div class="form-tip">
                  JSON格式示例：{"颜色": "红色", "尺寸": "L", "材质": "纯棉"}
                </div>
              </div>
            </el-form-item>
          </el-tab-pane>
        </el-tabs>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import ImageUpload from '@/components/ImageUpload.vue'
import RichTextEditor from '@/components/RichTextEditor.vue'
import { getProductById, createProduct, updateProduct } from '@/api/products'
import { getCategoryTree } from '@/api/categories'

const router = useRouter()
const route = useRoute()

const formRef = ref(null)
const imageUploadRef = ref(null)
const activeTab = ref('basic')
const submitting = ref(false)

const isEdit = computed(() => !!route.params.id)

// 分类选项
const categoryOptions = ref([])

// 规格模式
const specMode = ref('form')
const specFormItems = ref([])
const specJsonText = ref('')

// 表单数据
const formData = reactive({
  name: '',
  categoryId: null,
  price: null,
  originalPrice: null,
  stock: null,
  description: '',
  images: [],
  specifications: {},
  status: 1, // 默认上架
})

// 表单验证规则
const formRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
}

// 加载分类树
const loadCategories = async () => {
  try {
    const res = await getCategoryTree({ status: 1 })
    if (res.data) {
      // 过滤掉 id 为 null 或 undefined 的分类，避免 el-cascader 报错
      const filterCategories = (categories) => {
        return categories
          .filter(cat => cat && cat.id != null)
          .map(cat => ({
            ...cat,
            children: cat.children ? filterCategories(cat.children) : undefined
          }))
      }
      categoryOptions.value = filterCategories(res.data || [])
    }
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 加载详情数据
const loadDetail = async () => {
  if (!isEdit.value) return

  try {
    const res = await getProductById(route.params.id)
    if (res.data) {
      const data = res.data
      formData.name = data.name || ''
      formData.categoryId = data.categoryId || null
      formData.price = data.price ? parseFloat(data.price) : null
      formData.originalPrice = data.originalPrice ? parseFloat(data.originalPrice) : null
      formData.stock = data.stock || null
      formData.description = data.description || ''
      formData.images = Array.isArray(data.images) ? data.images : (data.images ? [data.images] : [])
      formData.specifications = data.specifications || {}
      formData.status = data.status !== undefined ? data.status : 1

      // 初始化规格表单
      if (formData.specifications && Object.keys(formData.specifications).length > 0) {
        specFormItems.value = Object.keys(formData.specifications).map((key) => ({
          key,
          value: formData.specifications[key],
        }))
        specJsonText.value = JSON.stringify(formData.specifications, null, 2)
      }
    }
  } catch (error) {
    console.error('加载商品详情失败:', error)
    ElMessage.error('加载商品详情失败')
  }
}

// 图片变化
const handleImagesChange = (urls) => {
  formData.images = urls
}

// 标签页切换
const handleTabChange = (tabName) => {
  // 当切换到图片标签页时，刷新图片上传组件
  if (tabName === 'images') {
    nextTick(() => {
      if (imageUploadRef.value && imageUploadRef.value.refresh) {
        imageUploadRef.value.refresh()
      }
    })
  }
}

// 添加规格项
const addSpecFormItem = () => {
  specFormItems.value.push({ key: '', value: '' })
}

// 删除规格项
const removeSpecFormItem = (index) => {
  specFormItems.value.splice(index, 1)
}

// JSON规格变化
const handleSpecJsonChange = () => {
  try {
    if (specJsonText.value.trim()) {
      const parsed = JSON.parse(specJsonText.value)
      if (typeof parsed === 'object' && !Array.isArray(parsed)) {
        formData.specifications = parsed
      } else {
        ElMessage.warning('规格必须是JSON对象格式')
      }
    } else {
      formData.specifications = {}
    }
  } catch (error) {
    ElMessage.warning('JSON格式错误，请检查')
  }
}

// 监听规格表单变化
watch(
  () => specFormItems.value,
  () => {
    if (specMode.value === 'form') {
      const specs = {}
      specFormItems.value.forEach((item) => {
        if (item.key && item.value) {
          specs[item.key] = item.value
        }
      })
      formData.specifications = specs
    }
  },
  { deep: true }
)

// 监听规格模式变化
watch(specMode, (newMode) => {
  if (newMode === 'json') {
    specJsonText.value = JSON.stringify(formData.specifications, null, 2)
  } else {
    specFormItems.value = Object.keys(formData.specifications || {}).map((key) => ({
      key,
      value: formData.specifications[key],
    }))
  }
})

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      // 验证规格
      if (specMode.value === 'json') {
        try {
          if (specJsonText.value.trim()) {
            const parsed = JSON.parse(specJsonText.value)
            if (typeof parsed !== 'object' || Array.isArray(parsed)) {
              ElMessage.warning('规格必须是JSON对象格式')
              return
            }
            formData.specifications = parsed
          } else {
            formData.specifications = {}
          }
        } catch (error) {
          ElMessage.warning('JSON格式错误，请检查')
          return
        }
      }

      submitting.value = true
      try {
        const submitData = {
          ...formData,
          images: Array.isArray(formData.images) ? formData.images : [],
          specifications: formData.specifications || {},
        }

        if (isEdit.value) {
          await updateProduct(route.params.id, submitData)
          ElMessage.success('更新成功')
        } else {
          await createProduct(submitData)
          ElMessage.success('创建成功')
        }

        router.push('/products')
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error(error.message || '保存失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 取消
const handleCancel = () => {
  ElMessageBox.confirm('确定要取消吗？未保存的数据将丢失。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      router.push('/products')
    })
    .catch(() => {
      // 用户取消
    })
}

onMounted(() => {
  loadCategories()
  loadDetail()
})
</script>

<style scoped>
.product-form-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-form {
  padding: 20px 0;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.form-unit {
  margin-left: 8px;
  color: #909399;
}

.spec-form-mode {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 16px;
}

.spec-form-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.spec-json-mode {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 16px;
}

:deep(.el-tabs__content) {
  padding: 20px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

/* 确保图片上传组件在 tab 中正常工作 */
:deep(.el-tab-pane) {
  position: relative;
}

:deep(.el-tabs__content) {
  overflow: visible;
}
</style>
