<template>
  <el-select
    :model-value="modelValue"
    placeholder="请选择文章分类"
    clearable
    filterable
    style="width: 100%"
    :loading="loading"
    @update:model-value="handleChange"
  >
    <el-option
      v-for="item in categoryList"
      :key="item.id"
      :label="item.name"
      :value="item.id"
    />
  </el-select>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getArticleCategoryList } from '@/api/articles'
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
const categoryList = ref([])

/**
 * 加载文章分类列表
 */
const loadCategories = async () => {
  loading.value = true
  try {
    const res = await getArticleCategoryList(1) // 只显示已启用的分类
    if (res.data) {
      // 过滤掉 id 为 null 或 undefined 的分类
      categoryList.value = (res.data || []).filter((cat) => cat && cat.id != null)
    }
  } catch (error) {
    console.error('加载文章分类失败:', error)
    ElMessage.error('加载文章分类失败')
  } finally {
    loading.value = false
  }
}

/**
 * 处理选择变化
 */
const handleChange = (value) => {
  emit('update:modelValue', value)
  
  // 查找完整的分类对象
  if (value) {
    const category = categoryList.value.find((c) => c.id === value)
    emit('change', category || null)
  } else {
    emit('change', null)
  }
}

// 组件挂载时加载分类
onMounted(() => {
  loadCategories()
})
</script>
