<template>
  <el-cascader
    v-model="selectedPath"
    :options="directoryOptions"
    :props="cascaderProps"
    placeholder="请选择目录"
    clearable
    filterable
    style="width: 100%"
    :loading="loading"
    @change="handleChange"
  />
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getDirectoryTree } from '@/api/file'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: {
    type: Number,
    default: null,
  },
})

const emit = defineEmits(['update:modelValue', 'change'])

const loading = ref(false)
const directoryOptions = ref([])
const selectedPath = ref([])

// Cascader 配置
const cascaderProps = {
  value: 'id',
  label: 'name',
  children: 'children',
  checkStrictly: true, // 可以选择任意一级
  emitPath: false, // 只返回选中节点的值
}

/**
 * 转换目录树为级联选择器格式
 */
const convertToCascaderOptions = (directories) => {
  if (!directories || directories.length === 0) {
    return []
  }
  
  return directories.map(dir => ({
    id: dir.id,
    name: dir.name,
    path: dir.path,
    children: dir.children && dir.children.length > 0 
      ? convertToCascaderOptions(dir.children) 
      : undefined,
  }))
}

/**
 * 加载目录树
 */
const loadDirectories = async () => {
  loading.value = true
  try {
    const res = await getDirectoryTree()
    if (res && res.code === 200) {
      directoryOptions.value = convertToCascaderOptions(res.data || [])
    } else {
      ElMessage.error(res?.message || '加载目录树失败')
      directoryOptions.value = []
    }
  } catch (error) {
    console.error('加载目录树失败:', error)
    ElMessage.error('加载目录树失败')
    directoryOptions.value = []
  } finally {
    loading.value = false
  }
}

/**
 * 处理选择变化
 */
const handleChange = (value) => {
  emit('update:modelValue', value)
  emit('change', value)
}

// 监听外部值变化
watch(
  () => props.modelValue,
  (newVal) => {
    // 如果外部值变化，更新内部值
    if (newVal !== selectedPath.value) {
      selectedPath.value = newVal
    }
  },
  { immediate: true }
)

// 监听内部值变化
watch(selectedPath, (newVal) => {
  if (newVal !== props.modelValue) {
    emit('update:modelValue', newVal)
  }
})

onMounted(() => {
  loadDirectories()
})
</script>

<style scoped>
/* 样式可以根据需要添加 */
</style>
