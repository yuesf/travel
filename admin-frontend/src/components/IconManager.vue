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
            <el-option label="H5链接" value="h5_link" />
          </el-select>
        </el-form-item>
        <!-- 文章分类两级选择 -->
        <template v-if="formData.type === 'article_category'">
          <el-form-item label="文章分类" prop="categoryId">
            <el-select
              v-model="formData.categoryId"
              placeholder="请选择文章分类"
              filterable
              style="width: 100%"
              :loading="categoryLoading"
              @change="onArticleCategoryChange"
            >
              <el-option
                v-for="item in categoryList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item 
            v-if="formData.categoryId"
            label="具体文章"
            prop="relatedId"
          >
            <el-select
              v-model="formData.relatedId"
              :placeholder="articlesLoading ? '正在加载文章列表...' : (articleList.length === 0 ? '该分类下暂无文章' : '请选择具体文章（可选）')"
              filterable
              style="width: 100%"
              :loading="articlesLoading"
              :disabled="articlesLoading"
              @change="handleArticleChange"
            >
              <el-option
                label="仅选择分类（跳转到文章列表）"
                :value="formData.categoryId"
              />
              <el-option
                v-for="item in articleList"
                :key="item.id"
                :label="item.title"
                :value="item.id"
              />
            </el-select>
            <div v-if="!articlesLoading && articleList.length === 0 && formData.categoryId" style="color: #909399; font-size: 12px; margin-top: 4px;">
              该分类下暂无已发布的文章
            </div>
          </el-form-item>
        </template>
        <!-- 其他类型的单级选择 -->
        <el-form-item 
          v-else-if="formData.type && formData.type !== 'h5_link'"
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
            :placeholder="(formData.relatedId || formData.categoryId) ? '将自动填充关联对象名称' : '请输入图标名称'"
            :disabled="!!(formData.relatedId || formData.categoryId)"
          />
        </el-form-item>
        <!-- H5链接地址输入 -->
        <el-form-item 
          v-if="formData.type === 'h5_link'"
          label="链接地址"
          prop="linkUrl"
        >
          <el-input 
            v-model="formData.linkUrl" 
            placeholder="请输入完整的HTTP/HTTPS链接，例如：https://www.example.com"
          />
        </el-form-item>
        <el-form-item label="图标图片" prop="icon">
          <ImageUpload
            v-model="formData.icon"
            :limit="1"
            :max-size="2"
            :compress="true"
            :compress-size="{ width: 32, height: 32 }"
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
// 文章分类相关状态
const categoryList = ref([])
const categoryLoading = ref(false)
const articleList = ref([])
const articlesLoading = ref(false)

// 表单数据
const formData = reactive({
  type: '', // 'product_category', 'article_category', 'product', 'article', 'attraction', 'hotel', 'h5_link'
  relatedId: null,
  categoryId: null, // 文章分类ID（仅用于 article_category 类型）
  name: '',
  icon: '',
  linkUrl: '', // H5链接地址（仅用于 h5_link 类型）
  sort: 0,
  status: 1,
})

// 表单验证规则
const formRules = {
  type: [{ required: true, message: '请选择图标类型', trigger: 'change' }],
  categoryId: [
    {
      validator: (rule, value, callback) => {
        if (formData.type === 'article_category' && !value) {
          callback(new Error('请选择文章分类'))
        } else {
          callback()
        }
      },
      trigger: 'change',
    },
  ],
  relatedId: [
    {
      validator: (rule, value, callback) => {
        // H5链接类型时，relatedId 可以为空
        if (formData.type === 'h5_link') {
          callback()
          return
        }
        // 文章分类类型时，relatedId 可以为空（只选择分类）
        if (formData.type === 'article_category') {
          callback()
          return
        }
        // 其他类型必须选择
        if (!value) {
          callback(new Error('请选择关联对象'))
        } else {
          callback()
        }
      },
      trigger: 'change',
    },
  ],
  linkUrl: [
    {
      validator: (rule, value, callback) => {
        if (formData.type === 'h5_link') {
          if (!value || !value.trim()) {
            callback(new Error('请输入链接地址'))
          } else if (!value.startsWith('http://') && !value.startsWith('https://')) {
            callback(new Error('链接地址必须以 http:// 或 https:// 开头'))
          } else {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  name: [{ required: true, message: '请输入图标名称', trigger: 'blur' }],
  icon: [
    {
      validator: (rule, value, callback) => {
        if (!value || (typeof value === 'string' && value.trim() === '')) {
          callback(new Error('请上传图标图片'))
        } else {
          callback()
        }
      },
      trigger: ['change', 'blur'],
    },
  ],
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
    h5_link: 'H5链接',
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
    h5_link: 'warning',
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
    h5_link: 'H5链接',
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
      // 文章分类类型使用两级选择，这里只加载分类列表
      await loadArticleCategories()
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

// 加载文章分类列表
const loadArticleCategories = async () => {
  categoryLoading.value = true
  try {
    const res = await getArticleCategoryList(1)
    if (res.data) {
      categoryList.value = res.data.map(item => ({
        id: item.id,
        name: item.name,
      }))
    }
  } catch (error) {
    console.error('加载文章分类列表失败:', error)
    ElMessage.error('加载文章分类列表失败')
  } finally {
    categoryLoading.value = false
  }
}

// 加载指定分类下的文章列表
const loadArticlesByCategory = async (categoryId) => {
  if (!categoryId) {
    articleList.value = []
    return
  }
  
  articlesLoading.value = true
  try {
    // 确保 categoryId 是数字类型
    const categoryIdNum = typeof categoryId === 'string' ? Number(categoryId) : categoryId
    
    console.log('开始加载文章列表，分类ID:', categoryIdNum, '类型:', typeof categoryIdNum)
    
    const params = { 
      page: 1, 
      pageSize: 100, 
      categoryId: categoryIdNum,
      status: 1 
    }
    console.log('请求参数:', params)
    
    const res = await getArticleList(params)
    
    console.log('加载文章列表完整响应:', JSON.stringify(res, null, 2))
    
    // 处理响应数据：res 是 Result 对象，res.data 是 PageResult 对象，包含 list 和 total
    if (res && res.code === 200 && res.data) {
      const list = res.data.list || []
      console.log('解析到的文章列表（原始）:', list)
      console.log('文章总数:', res.data.total)
      
      articleList.value = list.map(item => ({
        id: item.id,
        title: item.title,
      }))
      
      console.log('处理后的文章列表:', articleList.value)
      console.log('文章列表数量:', articleList.value.length)
      
      if (articleList.value.length === 0) {
        console.warn('该分类下没有文章')
        ElMessage.warning('该分类下暂无文章')
      }
    } else {
      articleList.value = []
      console.warn('文章列表数据格式异常:', res)
      if (res && res.code !== 200) {
        ElMessage.error(res.message || '加载文章列表失败')
      }
    }
  } catch (error) {
    console.error('加载文章列表失败，错误详情:', error)
    console.error('错误堆栈:', error.stack)
    ElMessage.error('加载文章列表失败: ' + (error.message || '未知错误'))
    articleList.value = []
  } finally {
    articlesLoading.value = false
  }
}

// 文章分类变化处理
const onArticleCategoryChange = async (categoryId) => {
  // 确保 categoryId 是数字类型
  const categoryIdNum = categoryId ? (typeof categoryId === 'string' ? Number(categoryId) : categoryId) : null
  formData.categoryId = categoryIdNum
  formData.relatedId = null // 清空文章选择
  articleList.value = []
  
  if (categoryIdNum) {
    // 自动加载该分类下的文章列表
    await loadArticlesByCategory(categoryIdNum)
    
    // 自动填充分类名称
    const category = categoryList.value.find(item => item.id === categoryIdNum)
    if (category) {
      formData.name = category.name
    }
  } else {
    formData.name = ''
  }
}

// 文章选择变化处理
const handleArticleChange = (articleId) => {
  if (articleId === formData.categoryId) {
    // 选择了"仅选择分类"选项
    const category = categoryList.value.find(item => item.id === articleId)
    if (category) {
      formData.name = category.name
    }
    // 只选择分类时，relatedId = categoryId
    formData.relatedId = formData.categoryId
  } else {
    // 选择了具体文章
    const article = articleList.value.find(item => item.id === articleId)
    if (article) {
      formData.name = article.title
    }
    // 选择具体文章时，relatedId = 文章ID（保持 type 为 article_category）
    formData.relatedId = articleId
  }
}

// 类型变化处理
const handleTypeChange = async (type) => {
  formData.relatedId = null
  formData.categoryId = null
  formData.name = ''
  formData.linkUrl = ''
  articleList.value = []
  categoryList.value = []
  
  if (type === 'article_category') {
    // 文章分类类型，加载分类列表
    await loadArticleCategories()
  } else if (type) {
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
    formData.categoryId = configValue?.categoryId || null
    formData.name = configValue?.name || ''
    formData.icon = configValue?.icon || ''
    formData.linkUrl = configValue?.linkUrl || ''
    formData.sort = row.sort || 0
    formData.status = row.status !== undefined ? row.status : 1
    
    // 加载对应类型的选项
    if (formData.type === 'article_category') {
      await loadArticleCategories()
      // 如果已有分类ID，加载该分类下的文章列表
      if (formData.categoryId) {
        await loadArticlesByCategory(formData.categoryId)
      }
    } else if (formData.type) {
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
    // 手动验证 icon 字段
    if (!formData.icon || (typeof formData.icon === 'string' && formData.icon.trim() === '')) {
      ElMessage.error('请上传图标图片')
      await formRef.value.validateField('icon')
      return
    }
    
    await formRef.value.validate()
    submitting.value = true

    // 获取关联对象名称
    let relatedName = ''
    if (formData.type === 'article_category') {
      // 文章分类类型：判断是分类还是文章
      // 如果 relatedId 为空，但 categoryId 不为空，视为只选择了分类
      const finalRelatedId = formData.relatedId || formData.categoryId
      if (finalRelatedId === formData.categoryId) {
        // 只选择了分类
        const category = categoryList.value.find(item => item.id === formData.categoryId)
        relatedName = category ? category.name : ''
      } else {
        // 选择了具体文章
        const article = articleList.value.find(item => item.id === finalRelatedId)
        relatedName = article ? article.title : ''
      }
    } else if (formData.type === 'h5_link') {
      // H5链接类型，relatedName 可以为空
      relatedName = formData.name
    } else {
      // 其他类型
      const relatedOption = typeOptions.value.find(item => item.id === formData.relatedId)
      relatedName = relatedOption ? relatedOption.name : ''
    }

    const configValue = {
      type: formData.type,
      relatedId: formData.relatedId,
      relatedName: relatedName,
      name: formData.name,
      icon: formData.icon,
    }
    
    // 文章分类类型时，保存 categoryId
    if (formData.type === 'article_category' && formData.categoryId) {
      configValue.categoryId = formData.categoryId
      // 如果 relatedId 为空，但 categoryId 不为空，自动设置 relatedId = categoryId（兼容只选择分类的情况）
      if (!configValue.relatedId && formData.categoryId) {
        configValue.relatedId = formData.categoryId
        // 同时更新 relatedName 为分类名称
        const category = categoryList.value.find(item => item.id === formData.categoryId)
        configValue.relatedName = category ? category.name : ''
      }
    }
    
    // H5链接类型时，保存 linkUrl
    if (formData.type === 'h5_link' && formData.linkUrl) {
      configValue.linkUrl = formData.linkUrl
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
  formData.categoryId = null
  formData.name = ''
  formData.icon = ''
  formData.linkUrl = ''
  formData.sort = iconList.value.length > 0
    ? Math.max(...iconList.value.map(item => item.sort || 0)) + 1
    : 1
  formData.status = 1
  typeOptions.value = []
  categoryList.value = []
  articleList.value = []
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
