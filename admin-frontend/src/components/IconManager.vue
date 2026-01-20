<template>
  <div class="icon-manager">
    <div class="icon-header">
      <el-button type="primary" @click="handleAdd" :icon="Plus">添加图标</el-button>
    </div>

    <!-- 图标列表 -->
    <el-table
      :data="iconList"
      v-loading="loading"
      stripe
      border
      row-key="id"
      class="icon-table"
    >
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column label="排序" width="120" align="center">
        <template #default="{ row, $index }">
          <el-button
            link
            type="primary"
            size="small"
            :icon="ArrowUp"
            :disabled="$index === 0"
            @click="handleMoveUp($index)"
            title="上移"
          />
          <el-button
            link
            type="primary"
            size="small"
            :icon="ArrowDown"
            :disabled="$index === iconList.length - 1"
            @click="handleMoveDown($index)"
            title="下移"
          />
        </template>
      </el-table-column>
      <el-table-column label="图标" width="100" align="center">
        <template #default="{ row }">
          <el-image
            :src="getIconImage(row)"
            fit="cover"
            style="width: 60px; height: 60px; border-radius: 4px;"
            :preview-src-list="[getIconImage(row)]"
            preview-teleported
            :lazy="false"
          >
            <template #error>
              <div class="image-slot">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
        </template>
      </el-table-column>
      <el-table-column label="名称" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">
          {{ getIconName(row) }}
        </template>
      </el-table-column>
      <el-table-column label="类型" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="getIconTypeTag(row)">
            {{ getIconTypeLabel(row) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="关联对象" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">
          {{ getRelatedObjectName(row) }}
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row, $index }">
          <el-button link type="primary" size="small" @click="handleEdit(row, $index)">
            编辑
          </el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row, $index)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="图标类型" prop="type">
          <el-select
            v-model="formData.type"
            placeholder="请选择图标类型"
            style="width: 100%"
            @change="handleTypeChange"
          >
            <el-option label="商品分类" value="product_category" />
            <el-option label="文章分类" value="article_category" />
            <el-option label="商品" value="product" />
            <el-option label="文章" value="article" />
            <el-option label="景点" value="attraction" />
            <el-option label="酒店" value="hotel" />
          </el-select>
        </el-form-item>
        <el-form-item 
          v-if="formData.type"
          :label="getTypeLabel(formData.type)"
          prop="relatedId"
        >
          <el-select
            v-model="formData.relatedId"
            :placeholder="`请选择${getTypeLabel(formData.type)}`"
            filterable
            style="width: 100%"
            :loading="optionsLoading"
            @change="handleRelatedIdChange"
          >
            <el-option
              v-for="item in typeOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="图标名称" prop="name">
          <el-input 
            v-model="formData.name" 
            :placeholder="formData.relatedId ? '将自动填充关联对象名称' : '请输入图标名称'"
            :disabled="!!formData.relatedId"
          />
        </el-form-item>
        <el-form-item label="图标图片" prop="icon">
          <ImageUpload
            v-model="formData.icon"
            :limit="1"
            :max-size="2"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ArrowUp, ArrowDown, Picture } from '@element-plus/icons-vue'
import ImageUpload from './ImageUpload.vue'
import {
  getConfigByType,
  createConfig,
  updateConfig,
  deleteConfig,
} from '@/api/miniprogram'
import { getCategoryList } from '@/api/categories'
import { getArticleCategoryList, getArticleList } from '@/api/articles'
import { getAttractionList } from '@/api/attractions'
import { getProductList } from '@/api/products'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['update:modelValue', 'change'])

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('添加图标')
const submitting = ref(false)
const formRef = ref(null)
const iconList = ref([])
const editingIndex = ref(-1)
const optionsLoading = ref(false)
const typeOptions = ref([])

// 表单数据
const formData = reactive({
  type: '', // 'product_category', 'article_category', 'product', 'article', 'attraction', 'hotel'
  relatedId: null,
  name: '',
  icon: '',
  sort: 0,
  status: 1,
})

// 表单验证规则
const formRules = {
  type: [{ required: true, message: '请选择图标类型', trigger: 'change' }],
  relatedId: [
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请选择关联对象'))
        } else {
          callback()
        }
      },
      trigger: 'change',
    },
  ],
  name: [{ required: true, message: '请输入图标名称', trigger: 'blur' }],
  icon: [{ required: true, message: '请上传图标图片', trigger: 'change' }],
}

// 获取图标图片
const getIconImage = (row) => {
  try {
    const configValue = typeof row.configValue === 'string' 
      ? JSON.parse(row.configValue) 
      : row.configValue
    return configValue?.icon || ''
  } catch (e) {
    return ''
  }
}

// 获取图标名称
const getIconName = (row) => {
  try {
    const configValue = typeof row.configValue === 'string'
      ? JSON.parse(row.configValue)
      : row.configValue
    return configValue?.name || '未命名'
  } catch (e) {
    return '未命名'
  }
}

// 获取图标类型
const getIconType = (row) => {
  try {
    const configValue = typeof row.configValue === 'string'
      ? JSON.parse(row.configValue)
      : row.configValue
    return configValue?.type || ''
  } catch (e) {
    return ''
  }
}

// 获取图标类型标签
const getIconTypeLabel = (row) => {
  const type = getIconType(row)
  const typeMap = {
    product_category: '商品分类',
    article_category: '文章分类',
    product: '商品',
    article: '文章',
    attraction: '景点',
    hotel: '酒店',
  }
  return typeMap[type] || '未知'
}

// 获取图标类型标签颜色
const getIconTypeTag = (row) => {
  const type = getIconType(row)
  const typeMap = {
    product_category: 'success',
    article_category: 'primary',
    product: 'success',
    article: 'info',
    attraction: 'warning',
    hotel: 'danger',
  }
  return typeMap[type] || ''
}

// 获取关联对象名称
const getRelatedObjectName = (row) => {
  try {
    const configValue = typeof row.configValue === 'string'
      ? JSON.parse(row.configValue)
      : row.configValue
    return configValue?.relatedName || '-'
  } catch (e) {
    return '-'
  }
}

// 获取类型标签
const getTypeLabel = (type) => {
  const typeMap = {
    product_category: '商品分类',
    article_category: '文章分类',
    product: '商品',
    article: '文章',
    attraction: '景点',
    hotel: '酒店',
  }
  return typeMap[type] || ''
}

// 加载类型选项
const loadTypeOptions = async (type) => {
  optionsLoading.value = true
  typeOptions.value = []
  
  try {
    if (type === 'product_category') {
      const res = await getCategoryList({ status: 1, type: 'CONFIG' })
      if (res.data) {
        typeOptions.value = res.data.map(item => ({
          id: item.id,
          name: item.name,
          icon: item.icon, // 保存商品分类的图标
        }))
      }
    } else if (type === 'article_category') {
      const res = await getArticleCategoryList(1)
      if (res.data) {
        typeOptions.value = res.data.map(item => ({
          id: item.id,
          name: item.name,
        }))
      }
    } else if (type === 'product') {
      const res = await getProductList({ page: 1, pageSize: 100, status: 1 })
      if (res.data && res.data.list) {
        typeOptions.value = res.data.list.map(item => ({
          id: item.id,
          name: item.name,
        }))
      }
    } else if (type === 'article') {
      const res = await getArticleList({ page: 1, pageSize: 100, status: 1 })
      if (res.data && res.data.list) {
        typeOptions.value = res.data.list.map(item => ({
          id: item.id,
          name: item.title,
        }))
      }
    } else if (type === 'attraction') {
      const res = await getAttractionList({ page: 1, pageSize: 100, status: 1 })
      if (res.data && res.data.list) {
        typeOptions.value = res.data.list.map(item => ({
          id: item.id,
          name: item.name,
        }))
      }
    } else if (type === 'hotel') {
      // 酒店功能待实现
      typeOptions.value = []
      ElMessage.warning('酒店功能待实现')
    }
  } catch (error) {
    console.error('加载选项失败:', error)
    ElMessage.error('加载选项失败')
  } finally {
    optionsLoading.value = false
  }
}

// 类型变化处理
const handleTypeChange = async (type) => {
  formData.relatedId = null
  formData.name = ''
  if (type) {
    await loadTypeOptions(type)
  }
}

// 处理关联ID变化
const handleRelatedIdChange = (newId) => {
  if (newId && typeOptions.value.length > 0) {
    const option = typeOptions.value.find(item => item.id === newId)
    if (option) {
      formData.name = option.name
      // 如果是商品分类，自动填充图标（如果商品分类有图标）
      if (formData.type === 'product_category' && option.icon) {
        formData.icon = option.icon
      }
    }
  } else {
    // 清空选择时，清空名称和图标
    formData.name = ''
    if (formData.type === 'product_category') {
      formData.icon = ''
    }
  }
}

// 监听关联ID变化，自动填充名称和图标（用于编辑时的初始化）
watch(() => formData.relatedId, (newId) => {
  if (newId && typeOptions.value.length > 0) {
    const option = typeOptions.value.find(item => item.id === newId)
    if (option) {
      formData.name = option.name
      // 如果是商品分类，且当前图标为空，自动填充图标
      if (formData.type === 'product_category' && option.icon && !formData.icon) {
        formData.icon = option.icon
      }
    }
  }
})

// 加载图标列表
const loadIcons = async () => {
  loading.value = true
  try {
    const res = await getConfigByType('ICON', { status: null })
    if (res.data) {
      iconList.value = res.data.sort((a, b) => (a.sort || 0) - (b.sort || 0))
      emit('update:modelValue', iconList.value)
      emit('change', iconList.value)
    }
  } catch (error) {
    console.error('加载图标列表失败:', error)
    ElMessage.error('加载图标列表失败')
  } finally {
    loading.value = false
  }
}

// 上移
const handleMoveUp = async (index) => {
  if (index === 0) return
  
  const item = iconList.value[index]
  const prevItem = iconList.value[index - 1]
  
  // 交换排序值
  const tempSort = item.sort
  item.sort = prevItem.sort
  prevItem.sort = tempSort
  
  // 更新列表顺序
  iconList.value.splice(index - 1, 2, item, prevItem)
  
  // 更新数据库
  try {
    await Promise.all([
      updateConfig(item.id, { sort: item.sort }),
      updateConfig(prevItem.id, { sort: prevItem.sort }),
    ])
    ElMessage.success('排序更新成功')
    emit('update:modelValue', iconList.value)
    emit('change', iconList.value)
  } catch (error) {
    console.error('更新排序失败:', error)
    ElMessage.error('更新排序失败')
    await loadIcons()
  }
}

// 下移
const handleMoveDown = async (index) => {
  if (index === iconList.value.length - 1) return
  
  const item = iconList.value[index]
  const nextItem = iconList.value[index + 1]
  
  // 交换排序值
  const tempSort = item.sort
  item.sort = nextItem.sort
  nextItem.sort = tempSort
  
  // 更新列表顺序
  iconList.value.splice(index, 2, nextItem, item)
  
  // 更新数据库
  try {
    await Promise.all([
      updateConfig(item.id, { sort: item.sort }),
      updateConfig(nextItem.id, { sort: nextItem.sort }),
    ])
    ElMessage.success('排序更新成功')
    emit('update:modelValue', iconList.value)
    emit('change', iconList.value)
  } catch (error) {
    console.error('更新排序失败:', error)
    ElMessage.error('更新排序失败')
    await loadIcons()
  }
}

// 添加
const handleAdd = () => {
  editingIndex.value = -1
  dialogTitle.value = '添加图标'
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row, index) => {
  editingIndex.value = index
  dialogTitle.value = '编辑图标'
  
  try {
    const configValue = typeof row.configValue === 'string'
      ? JSON.parse(row.configValue)
      : row.configValue
    
    formData.type = configValue?.type || ''
    formData.relatedId = configValue?.relatedId || null
    formData.name = configValue?.name || ''
    formData.icon = configValue?.icon || ''
    formData.sort = row.sort || 0
    formData.status = row.status !== undefined ? row.status : 1
    
    // 加载对应类型的选项
    if (formData.type) {
      await loadTypeOptions(formData.type)
    }
  } catch (e) {
    console.error('解析配置值失败:', e)
  }
  
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row, index) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除图标"${getIconName(row)}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    loading.value = true
    try {
      await deleteConfig(row.id)
      ElMessage.success('删除成功')
      await loadIcons()
    } catch (error) {
      console.error('删除图标失败:', error)
      ElMessage.error('删除图标失败')
    } finally {
      loading.value = false
    }
  } catch (error) {
    // 用户取消
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    // 获取关联对象名称
    const relatedOption = typeOptions.value.find(item => item.id === formData.relatedId)
    const relatedName = relatedOption ? relatedOption.name : ''

    const configValue = {
      type: formData.type,
      relatedId: formData.relatedId,
      relatedName: relatedName,
      name: formData.name,
      icon: formData.icon,
    }

    if (editingIndex.value >= 0) {
      // 编辑
      const row = iconList.value[editingIndex.value]
      await updateConfig(row.id, {
        configValue: JSON.stringify(configValue),
        sort: formData.sort,
        status: formData.status,
      })
      ElMessage.success('更新成功')
    } else {
      // 添加
      const maxSort = iconList.value.length > 0
        ? Math.max(...iconList.value.map(item => item.sort || 0))
        : 0
      
      await createConfig({
        configKey: `ICON_${formData.type}_${Date.now()}`,
        configType: 'ICON',
        configValue: JSON.stringify(configValue),
        description: `${getTypeLabel(formData.type)}：${formData.name}`,
        sort: formData.sort || maxSort + 1,
        status: formData.status,
      })
      ElMessage.success('添加成功')
    }

    dialogVisible.value = false
    await loadIcons()
  } catch (error) {
    if (error !== false) {
      console.error('提交失败:', error)
      ElMessage.error('提交失败')
    }
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  formData.type = ''
  formData.relatedId = null
  formData.name = ''
  formData.icon = ''
  formData.sort = iconList.value.length > 0
    ? Math.max(...iconList.value.map(item => item.sort || 0)) + 1
    : 1
  formData.status = 1
  typeOptions.value = []
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 对话框关闭
const handleDialogClose = () => {
  resetForm()
  editingIndex.value = -1
}

onMounted(() => {
  loadIcons()
})

// 暴露方法供父组件调用
defineExpose({
  loadIcons,
})
</script>

<style scoped>
.icon-manager {
  width: 100%;
}

.icon-header {
  margin-bottom: 16px;
}

.icon-table {
  width: 100%;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 30px;
}
</style>
