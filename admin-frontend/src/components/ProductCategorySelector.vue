<template>
  <el-cascader
    :model-value="modelValue"
    :options="categoryOptions"
    :props="cascaderProps"
    placeholder="请选择商品分类"
    clearable
    filterable
    style="width: 100%"
    :loading="loading"
    @update:model-value="handleChange"
  />
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCategoryTree } from '@/api/categories'
import { ElMessage } from 'element-plus'

// Props
const props = defineProps({
  modelValue: {
    type: [Number, String],
    default: null,
  },
})

// Emits
const emit = defineEmits(['update:modelValue', 'change'])

// 数据
const loading = ref(false)
const categoryOptions = ref([])

// Cascader 配置
const cascaderProps = {
  value: 'id',
  label: 'name',
  children: 'children',
  checkStrictly: true, // 可以选择任意一级
  emitPath: false, // 只返回选中节点的值
}

/**
 * 过滤无效分类
 */
const filterCategories = (categories) => {
  return categories
    .filter((cat) => cat && cat.id != null)
    .map((cat) => ({
      ...cat,
      children: cat.children ? filterCategories(cat.children) : undefined,
    }))
}

/**
 * 加载分类树
 */
const loadCategories = async () => {
  loading.value = true
  try {
    const res = await getCategoryTree({ status: 1 })
    if (res.data) {
      categoryOptions.value = filterCategories(res.data || [])
    }
  } catch (error) {
    console.error('加载商品分类失败:', error)
    ElMessage.error('加载商品分类失败')
  } finally {
    loading.value = false
  }
}

/**
 * 查找分类对象
 */
const findCategory = (categories, id) => {
  for (const cat of categories) {
    if (cat.id === id) {
      return cat
    }
    if (cat.children) {
      const found = findCategory(cat.children, id)
      if (found) {
        return found
      }
    }
  }
  return null
}

/**
 * 处理选择变化
 */
const handleChange = (value) => {
  emit('update:modelValue', value)
  
  // 查找完整的分类对象
  if (value) {
    const category = findCategory(categoryOptions.value, value)
    emit('change', category)
  } else {
    emit('change', null)
  }
}

// 组件挂载时加载分类
onMounted(() => {
  loadCategories()
})
</script>
