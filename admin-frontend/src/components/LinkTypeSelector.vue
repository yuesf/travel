<template>
  <div class="link-type-selector">
    <!-- 链接类型选择 -->
    <el-form-item label="链接类型">
      <el-select
        v-model="linkTypeValue"
        placeholder="请选择链接类型"
        style="width: 100%"
        @change="handleTypeChange"
      >
        <el-option label="无跳转" value="NONE" />
        <el-option label="外部链接" value="EXTERNAL" />
        <el-option label="商品分类" value="PRODUCT_CATEGORY" />
        <el-option label="文章分类" value="ARTICLE_CATEGORY" />
        <el-option label="商品" value="PRODUCT" />
        <el-option label="文章" value="ARTICLE" />
        <el-option label="景点" value="ATTRACTION" />
        <el-option label="酒店" value="HOTEL" />
        <el-option label="地图" value="MAP" />
      </el-select>
    </el-form-item>

    <!-- 根据类型显示不同的配置项 -->
    <el-form-item v-if="linkTypeValue === 'EXTERNAL'" label="链接地址">
      <el-input
        v-model="linkValueData"
        placeholder="请输入完整的URL，如：https://example.com"
        clearable
        @input="handleValueChange"
      />
      <div style="color: #909399; font-size: 12px; margin-top: 4px">
        请输入完整的 URL 地址（需包含 http:// 或 https://）
      </div>
    </el-form-item>

    <!-- 商品分类：两级选择 -->
    <template v-if="linkTypeValue === 'PRODUCT_CATEGORY'">
      <el-form-item label="商品分类">
        <ProductCategorySelector
          v-model="tempCategoryId"
          @change="handleProductCategorySelect"
        />
      </el-form-item>
      <el-form-item v-if="tempCategoryId" label="具体商品">
        <el-select
          v-model="linkValueData"
          placeholder="请选择具体商品（可选）"
          clearable
          filterable
          remote
          :remote-method="searchProductsInCategory"
          :loading="productsLoading"
          style="width: 100%"
          @change="handleProductInCategoryChange"
        >
          <el-option
            :label="`仅选择分类（跳转到商品列表）`"
            :value="tempCategoryId"
          />
          <el-option
            v-for="item in categoryProductList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
        <div style="color: #909399; font-size: 12px; margin-top: 4px;">
          不选择具体商品时，点击轮播图将跳转到该分类的商品列表
        </div>
      </el-form-item>
    </template>

    <!-- 商品：级联选择 -->
    <el-form-item v-if="linkTypeValue === 'PRODUCT'" label="选择商品">
      <ProductSelector
        v-model="linkValueData"
        @change="handleProductChange"
      />
    </el-form-item>

    <!-- 文章分类：两级选择 -->
    <template v-if="linkTypeValue === 'ARTICLE_CATEGORY'">
      <el-form-item label="文章分类">
        <ArticleCategorySelector
          v-model="tempCategoryId"
          @change="handleArticleCategorySelect"
        />
      </el-form-item>
      <el-form-item v-if="tempCategoryId" label="具体文章">
        <el-select
          v-model="linkValueData"
          placeholder="请选择具体文章（可选）"
          clearable
          filterable
          remote
          :remote-method="searchArticlesInCategory"
          :loading="articlesLoading"
          style="width: 100%"
          @change="handleArticleInCategoryChange"
        >
          <el-option
            :label="`仅选择分类（跳转到文章列表）`"
            :value="tempCategoryId"
          />
          <el-option
            v-for="item in categoryArticleList"
            :key="item.id"
            :label="item.title"
            :value="item.id"
          />
        </el-select>
        <div style="color: #909399; font-size: 12px; margin-top: 4px;">
          不选择具体文章时，点击轮播图将跳转到该分类的文章列表
        </div>
      </el-form-item>
    </template>

    <!-- 文章：直接选择 -->
    <el-form-item v-if="linkTypeValue === 'ARTICLE'" label="选择文章">
      <ArticleSelector
        v-model="linkValueData"
        @change="handleArticleChange"
      />
    </el-form-item>

    <!-- 景点 -->
    <el-form-item v-if="linkTypeValue === 'ATTRACTION'" label="选择景点">
      <el-select
        v-model="linkValueData"
        placeholder="请选择景点"
        clearable
        filterable
        remote
        :remote-method="searchAttractions"
        :loading="attractionsLoading"
        style="width: 100%"
        @change="handleAttractionChange"
      >
        <el-option
          v-for="item in attractionList"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        />
      </el-select>
    </el-form-item>

    <!-- 酒店 -->
    <el-form-item v-if="linkTypeValue === 'HOTEL'" label="选择酒店">
      <el-select
        v-model="linkValueData"
        placeholder="请选择酒店"
        clearable
        filterable
        remote
        :remote-method="searchHotels"
        :loading="hotelsLoading"
        style="width: 100%"
        @change="handleHotelChange"
      >
        <el-option
          v-for="item in hotelList"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        />
      </el-select>
    </el-form-item>

    <!-- 地图 -->
    <el-form-item v-if="linkTypeValue === 'MAP'" label="选择地图">
      <el-select
        v-model="linkValueData"
        placeholder="请选择地图（可选，不选择则跳转到地图列表）"
        clearable
        filterable
        :loading="mapsLoading"
        style="width: 100%"
        @change="handleMapChange"
      >
        <el-option
          v-for="item in mapList"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        />
      </el-select>
      <div style="color: #909399; font-size: 12px; margin-top: 4px;">
        不选择具体地图时，点击轮播图将跳转到地图列表页面
      </div>
    </el-form-item>

    <!-- 显示当前配置摘要 -->
    <el-form-item v-if="linkTypeValue !== 'NONE' && linkDisplayValue" label="当前配置">
      <el-tag type="info">
        {{ getLinkTypeLabel(linkTypeValue) }}：{{ linkDisplayValue }}
      </el-tag>
    </el-form-item>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import ProductCategorySelector from './ProductCategorySelector.vue'
import ProductSelector from './ProductSelector.vue'
import ArticleCategorySelector from './ArticleCategorySelector.vue'
import ArticleSelector from './ArticleSelector.vue'
import { getProductList } from '@/api/products'
import { getArticleList } from '@/api/articles'
import { getAttractionList } from '@/api/attractions'
import { getHotelList } from '@/api/hotels'
import { getMapList } from '@/api/maps'

// Props
const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({
      linkType: 'NONE',
      linkValue: '',
      linkDisplay: '',
    }),
  },
})

// Emits
const emit = defineEmits(['update:modelValue', 'change'])

// 链接类型标签映射
const linkTypeLabels = {
  NONE: '无跳转',
  EXTERNAL: '外部链接',
  PRODUCT_CATEGORY: '商品分类',
  PRODUCT: '商品',
  ARTICLE_CATEGORY: '文章分类',
  ARTICLE: '文章',
  ATTRACTION: '景点',
  HOTEL: '酒店',
  MAP: '地图',
}

// 获取链接类型标签
const getLinkTypeLabel = (type) => {
  return linkTypeLabels[type] || '未知类型'
}

// 内部数据
const linkTypeValue = ref(props.modelValue?.linkType || 'NONE')
const linkValueData = ref(props.modelValue?.linkValue || '')
const linkDisplayValue = ref(props.modelValue?.linkDisplay || '')

// 临时数据
const tempCategoryId = ref(null)

// 加载状态
const productsLoading = ref(false)
const articlesLoading = ref(false)
const attractionsLoading = ref(false)
const hotelsLoading = ref(false)
const mapsLoading = ref(false)

// 列表数据
const categoryProductList = ref([])
const categoryArticleList = ref([])
const attractionList = ref([])
const hotelList = ref([])
const mapList = ref([])

// 搜索定时器
let searchTimer = null

// 监听 props 变化，同步内部数据
watch(
  () => props.modelValue,
  (newValue) => {
    if (newValue) {
      linkTypeValue.value = newValue.linkType || 'NONE'
      linkValueData.value = newValue.linkValue || ''
      linkDisplayValue.value = newValue.linkDisplay || ''
    }
  },
  { deep: true }
)

/**
 * 触发更新
 */
const emitUpdate = () => {
  const value = {
    linkType: linkTypeValue.value,
    linkValue: linkValueData.value,
    linkDisplay: linkDisplayValue.value,
  }
  emit('update:modelValue', value)
  emit('change', value)
}

/**
 * 处理类型变化
 */
const handleTypeChange = (type) => {
  linkTypeValue.value = type
  linkValueData.value = ''
  linkDisplayValue.value = ''
  tempCategoryId.value = null
  
  // 清空所有列表
  categoryProductList.value = []
  categoryArticleList.value = []
  attractionList.value = []
  hotelList.value = []
  mapList.value = []
  
  // 根据类型加载初始数据
  if (type === 'ATTRACTION') {
    searchAttractions('')
  } else if (type === 'HOTEL') {
    searchHotels('')
  } else if (type === 'MAP') {
    loadMaps()
  }
  
  emitUpdate()
}

/**
 * 处理值变化（外部链接）
 */
const handleValueChange = () => {
  linkDisplayValue.value = linkValueData.value
  emitUpdate()
}

/**
 * 处理商品分类选择（第一步）
 */
const handleProductCategorySelect = async (category) => {
  tempCategoryId.value = category ? category.id : null
  linkValueData.value = ''
  linkDisplayValue.value = ''
  
  if (category) {
    // 加载该分类下的商品
    await searchProductsInCategory('')
  } else {
    categoryProductList.value = []
  }
  
  emitUpdate()
}

/**
 * 搜索分类下的商品
 */
const searchProductsInCategory = async (query = '') => {
  if (!tempCategoryId.value) return
  
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  
  searchTimer = setTimeout(async () => {
    productsLoading.value = true
    try {
      const params = {
        page: 1,
        pageSize: 50,
        status: 1,
        categoryId: tempCategoryId.value,
      }
      
      if (query) {
        params.name = query
      }
      
      const res = await getProductList(params)
      if (res.data && res.data.list) {
        categoryProductList.value = res.data.list
      }
    } catch (error) {
      console.error('搜索商品失败:', error)
      ElMessage.error('搜索商品失败')
    } finally {
      productsLoading.value = false
    }
  }, 300)
}

/**
 * 处理商品分类中的商品选择（第二步）
 */
const handleProductInCategoryChange = (value) => {
  if (!value) {
    linkValueData.value = ''
    linkDisplayValue.value = ''
  } else if (value === tempCategoryId.value) {
    // 选择了"仅选择分类"
    linkValueData.value = value
    const category = categoryProductList.value[0]?.categoryName || '该分类'
    linkDisplayValue.value = category
  } else {
    // 选择了具体商品
    const product = categoryProductList.value.find(p => p.id === value)
    linkValueData.value = value
    linkDisplayValue.value = product ? product.name : ''
  }
  
  emitUpdate()
}

/**
 * 处理商品选择
 */
const handleProductChange = (product) => {
  if (product) {
    linkValueData.value = product.id
    linkDisplayValue.value = product.name
  } else {
    linkValueData.value = ''
    linkDisplayValue.value = ''
  }
  emitUpdate()
}

/**
 * 处理文章分类选择（第一步）
 */
const handleArticleCategorySelect = async (category) => {
  tempCategoryId.value = category ? category.id : null
  linkValueData.value = ''
  linkDisplayValue.value = ''
  
  if (category) {
    // 加载该分类下的文章
    await searchArticlesInCategory('')
  } else {
    categoryArticleList.value = []
  }
  
  emitUpdate()
}

/**
 * 搜索分类下的文章
 */
const searchArticlesInCategory = async (query = '') => {
  if (!tempCategoryId.value) return
  
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  
  searchTimer = setTimeout(async () => {
    articlesLoading.value = true
    try {
      const params = {
        page: 1,
        pageSize: 50,
        status: 1,
        categoryId: tempCategoryId.value,
      }
      
      if (query) {
        params.title = query
      }
      
      const res = await getArticleList(params)
      if (res.data && res.data.list) {
        categoryArticleList.value = res.data.list
      }
    } catch (error) {
      console.error('搜索文章失败:', error)
      ElMessage.error('搜索文章失败')
    } finally {
      articlesLoading.value = false
    }
  }, 300)
}

/**
 * 处理文章分类中的文章选择（第二步）
 */
const handleArticleInCategoryChange = (value) => {
  if (!value) {
    linkValueData.value = ''
    linkDisplayValue.value = ''
  } else if (value === tempCategoryId.value) {
    // 选择了"仅选择分类"
    linkValueData.value = value
    const category = categoryArticleList.value[0]?.categoryName || '该分类'
    linkDisplayValue.value = category
  } else {
    // 选择了具体文章
    const article = categoryArticleList.value.find(a => a.id === value)
    linkValueData.value = value
    linkDisplayValue.value = article ? article.title : ''
  }
  
  emitUpdate()
}

/**
 * 处理文章选择
 */
const handleArticleChange = (article) => {
  if (article) {
    linkValueData.value = article.id
    linkDisplayValue.value = article.title
  } else {
    linkValueData.value = ''
    linkDisplayValue.value = ''
  }
  emitUpdate()
}

/**
 * 搜索景点
 */
const searchAttractions = async (query = '') => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  
  searchTimer = setTimeout(async () => {
    attractionsLoading.value = true
    try {
      const params = {
        page: 1,
        pageSize: 50,
        status: 1,
      }
      
      if (query) {
        params.name = query
      }
      
      const res = await getAttractionList(params)
      if (res.data && res.data.list) {
        attractionList.value = res.data.list
      }
    } catch (error) {
      console.error('搜索景点失败:', error)
      ElMessage.error('搜索景点失败')
    } finally {
      attractionsLoading.value = false
    }
  }, 300)
}

/**
 * 处理景点选择
 */
const handleAttractionChange = (value) => {
  if (!value) {
    linkValueData.value = ''
    linkDisplayValue.value = ''
  } else {
    const attraction = attractionList.value.find(a => a.id === value)
    linkValueData.value = value
    linkDisplayValue.value = attraction ? attraction.name : ''
  }
  
  emitUpdate()
}

/**
 * 搜索酒店
 */
const searchHotels = async (query = '') => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  
  searchTimer = setTimeout(async () => {
    hotelsLoading.value = true
    try {
      const params = {
        page: 1,
        pageSize: 50,
        status: 1,
      }
      
      if (query) {
        params.name = query
      }
      
      const res = await getHotelList(params)
      if (res.data && res.data.list) {
        hotelList.value = res.data.list
      }
    } catch (error) {
      console.error('搜索酒店失败:', error)
      ElMessage.error('搜索酒店失败')
    } finally {
      hotelsLoading.value = false
    }
  }, 300)
}

/**
 * 处理酒店选择
 */
const handleHotelChange = (value) => {
  if (!value) {
    linkValueData.value = ''
    linkDisplayValue.value = ''
  } else {
    const hotel = hotelList.value.find(h => h.id === value)
    linkValueData.value = value
    linkDisplayValue.value = hotel ? hotel.name : ''
  }
  
  emitUpdate()
}

/**
 * 加载地图列表
 */
const loadMaps = async () => {
  mapsLoading.value = true
  try {
    const res = await getMapList({ status: 1 })
    if (res.data) {
      mapList.value = res.data
    }
  } catch (error) {
    console.error('加载地图列表失败:', error)
    ElMessage.error('加载地图列表失败')
  } finally {
    mapsLoading.value = false
  }
}

/**
 * 处理地图选择
 */
const handleMapChange = (value) => {
  if (!value) {
    linkValueData.value = ''
    linkDisplayValue.value = '地图列表'
  } else {
    const map = mapList.value.find(m => m.id === value)
    linkValueData.value = value
    linkDisplayValue.value = map ? map.name : ''
  }
  
  emitUpdate()
}
</script>

<style scoped>
.link-type-selector {
  width: 100%;
}
</style>
