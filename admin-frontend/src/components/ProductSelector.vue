<template>
  <div class="product-selector">
    <!-- 第一步：选择商品分类 -->
    <div class="selector-item">
      <div class="selector-label">商品分类</div>
      <ProductCategorySelector
        v-model="selectedCategoryId"
        @change="handleCategoryChange"
      />
    </div>

    <!-- 第二步：选择具体商品 -->
    <div class="selector-item">
      <div class="selector-label">选择商品</div>
      <el-select
        :model-value="modelValue"
        :placeholder="categoryPlaceholder"
        clearable
        filterable
        remote
        reserve-keyword
        :remote-method="handleSearch"
        :loading="loading"
        :disabled="!selectedCategoryId"
        style="width: 100%"
        @update:model-value="handleChange"
      >
        <el-option
          v-for="item in productList"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        >
          <div style="display: flex; align-items: center">
            <img
              v-if="item.image || item.coverImage"
              :src="item.image || item.coverImage"
              style="width: 30px; height: 30px; object-fit: cover; border-radius: 4px; margin-right: 10px"
            />
            <span>{{ item.name }}</span>
          </div>
        </el-option>
      </el-select>
      <div v-if="!selectedCategoryId" style="color: #909399; font-size: 12px; margin-top: 4px">
        请先选择商品分类
      </div>
      <div v-else-if="productList.length === 0 && !loading" style="color: #909399; font-size: 12px; margin-top: 4px">
        该分类下暂无已上架的商品
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { getProductList } from '@/api/products'
import { ElMessage } from 'element-plus'
import ProductCategorySelector from './ProductCategorySelector.vue'

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
const productList = ref([])
const selectedCategoryId = ref(null)
let searchTimer = null

// 计算提示文本
const categoryPlaceholder = computed(() => {
  if (!selectedCategoryId.value) {
    return '请先选择商品分类'
  }
  return '请输入商品名称搜索或从列表中选择'
})

/**
 * 搜索商品（按分类筛选）
 */
const searchProducts = async (query = '') => {
  // 如果没有选择分类，不加载商品
  if (!selectedCategoryId.value) {
    productList.value = []
    return
  }

  loading.value = true
  try {
    const params = {
      page: 1,
      pageSize: 50, // 增加每页数量，方便选择
      status: 1, // 只显示已上架的商品
      categoryId: selectedCategoryId.value, // 按分类筛选
    }
    
    if (query) {
      params.name = query
    }
    
    const res = await getProductList(params)
    if (res.data && res.data.list) {
      productList.value = res.data.list
    } else {
      productList.value = []
    }
  } catch (error) {
    console.error('搜索商品失败:', error)
    ElMessage.error('搜索商品失败')
  } finally {
    loading.value = false
  }
}

/**
 * 处理分类变化
 */
const handleCategoryChange = (category) => {
  // 分类变化时，清空商品选择并重新加载商品列表
  emit('update:modelValue', null)
  emit('change', null)
  
  if (category) {
    // 自动加载该分类下的商品
    searchProducts()
  } else {
    productList.value = []
  }
}

/**
 * 处理搜索（带防抖）
 */
const handleSearch = (query) => {
  // 清除之前的定时器
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  
  // 设置新的定时器，300ms 防抖
  searchTimer = setTimeout(() => {
    searchProducts(query)
  }, 300)
}

/**
 * 处理选择变化
 */
const handleChange = (value) => {
  emit('update:modelValue', value)
  
  // 查找完整的商品对象
  if (value) {
    const product = productList.value.find((p) => p.id === value)
    emit('change', product || null)
  } else {
    emit('change', null)
  }
}

// 监听分类变化
watch(selectedCategoryId, (newVal) => {
  if (newVal) {
    searchProducts()
  }
})
</script>

<style scoped>
.product-selector {
  width: 100%;
}

.selector-item {
  margin-bottom: 18px;
}

.selector-item:last-child {
  margin-bottom: 0;
}

.selector-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
  margin-bottom: 8px;
}
</style>
