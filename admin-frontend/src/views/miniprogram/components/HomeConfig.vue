<template>
  <div class="home-config-container">
    <el-card shadow="never">
      <!-- 轮播图管理 -->
      <el-card shadow="never" class="section-card">
        <template #header>
          <div class="card-header">
            <span>轮播图管理</span>
          </div>
        </template>
        <BannerManager ref="bannerManagerRef" v-model="banners" @change="handleBannerChange" />
      </el-card>

      <!-- Icon图标配置 -->
      <el-card shadow="never" class="section-card">
        <template #header>
          <div class="card-header">
            <span>Icon图标配置</span>
          </div>
        </template>
        <IconManager ref="iconManagerRef" v-model="icons" @change="handleIconChange" />
      </el-card>

      <!-- 推荐内容配置 -->
      <el-card shadow="never" class="section-card">
        <template #header>
          <div class="card-header">
            <span>热门推荐</span>
            <el-alert
              type="info"
              :closable="false"
              show-icon
              style="margin-left: 10px;"
            >
              <template #default>
                <span style="font-size: 12px;">可通过拖拽调整顺序</span>
              </template>
            </el-alert>
          </div>
        </template>
        <el-form :model="recommendForm" label-width="120px">
          <!-- 推荐商品分类 -->
          <el-form-item label="推荐商品分类">
            <div style="width: 100%;">
              <el-select
                v-model="recommendForm.productCategories"
                multiple
                filterable
                placeholder="请选择推荐商品分类（最多4个）"
                style="width: 100%"
                :max-collapse-tags="3"
                @change="handleProductCategoryChange"
              >
                <el-option
                  v-for="item in productCategoryOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                  :disabled="recommendForm.productCategories.length >= 4 && !recommendForm.productCategories.includes(item.id)"
                />
              </el-select>
              <div style="margin-top: 8px; color: #909399; font-size: 12px;">
                已选择 {{ recommendForm.productCategories.length }}/4 个商品分类
              </div>
              <!-- 排序列表 -->
              <div v-if="recommendForm.productCategories.length > 0" style="margin-top: 16px;">
                <div style="margin-bottom: 8px; font-weight: 500;">排序（拖拽调整顺序）：</div>
                <el-table
                  :data="sortedProductCategories"
                  row-key="id"
                  border
                  style="width: 100%"
                >
                  <el-table-column type="index" label="序号" width="60" />
                  <el-table-column label="分类名称" prop="name" />
                  <el-table-column label="图标" width="80" align="center">
                    <template #default="{ row }">
                      <el-image
                        v-if="row.icon"
                        :src="row.icon"
                        fit="cover"
                        style="width: 40px; height: 40px; border-radius: 4px;"
                        :preview-src-list="[row.icon]"
                        preview-teleported
                      />
                      <span v-else style="color: #909399;">无</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="120">
                    <template #default="{ row, $index }">
                      <el-button
                        link
                        type="primary"
                        size="small"
                        :icon="ArrowUp"
                        :disabled="$index === 0"
                        @click="handleMoveProductCategoryUp($index)"
                        title="上移"
                      />
                      <el-button
                        link
                        type="primary"
                        size="small"
                        :icon="ArrowDown"
                        :disabled="$index === sortedProductCategories.length - 1"
                        @click="handleMoveProductCategoryDown($index)"
                        title="下移"
                      />
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </el-form-item>
          
          <!-- 推荐景点 -->
          <el-form-item label="推荐景点">
            <div style="width: 100%;">
              <el-select
                v-model="recommendForm.attractions"
                multiple
                filterable
                placeholder="请选择推荐景点（可选）"
                style="width: 100%"
                :max-collapse-tags="3"
              >
                <el-option
                  v-for="item in attractionOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
              <div style="margin-top: 8px; color: #909399; font-size: 12px;">
                已选择 {{ recommendForm.attractions.length }} 个景点
              </div>
            </div>
          </el-form-item>
          
          <!-- 推荐酒店 -->
          <el-form-item label="推荐酒店">
            <div style="width: 100%;">
              <el-select
                v-model="recommendForm.hotels"
                multiple
                filterable
                placeholder="请选择推荐酒店（可选）"
                style="width: 100%"
                :max-collapse-tags="3"
              >
                <el-option
                  v-for="item in hotelOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
              <div style="margin-top: 8px; color: #909399; font-size: 12px;">
                已选择 {{ recommendForm.hotels.length }} 个酒店
              </div>
            </div>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleSaveRecommend" :loading="saving">
              保存推荐配置
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import BannerManager from '@/components/BannerManager.vue'
import IconManager from '@/components/IconManager.vue'
import {
  getConfigByType,
  createConfig,
  updateConfig,
} from '@/api/miniprogram'
import { getAttractionList } from '@/api/attractions'
import { getHotelList } from '@/api/hotels'
import { getCategoryList } from '@/api/categories'

const saving = ref(false)
const banners = ref([])
const icons = ref([])
const attractionOptions = ref([])
const hotelOptions = ref([])
const productCategoryOptions = ref([])
const bannerManagerRef = ref(null)
const iconManagerRef = ref(null)

// 推荐表单
const recommendForm = reactive({
  productCategories: [], // 商品分类ID列表（最多4个，按顺序）
  attractions: [], // 推荐景点ID列表
  hotels: [], // 推荐酒店ID列表
})

// 加载推荐景点选项
const loadAttractionOptions = async () => {
  try {
    const res = await getAttractionList({ page: 1, pageSize: 100, status: 1 })
    if (res.data && res.data.list) {
      attractionOptions.value = res.data.list
    }
  } catch (error) {
    console.error('加载景点选项失败:', error)
  }
}

// 加载推荐酒店选项
const loadHotelOptions = async () => {
  try {
    const res = await getHotelList({ page: 1, pageSize: 100, status: 1 })
    if (res.data && res.data.list) {
      hotelOptions.value = res.data.list
    }
  } catch (error) {
    console.error('加载酒店选项失败:', error)
  }
}

// 加载商品分类选项
const loadProductCategoryOptions = async () => {
  try {
    const res = await getCategoryList({ status: 1 })
    if (res.data) {
      // 过滤掉 id 为 null 或 undefined 的分类，避免 el-option 报错
      productCategoryOptions.value = res.data
        .filter(item => item && item.id != null)
        .map(item => ({
          id: item.id,
          name: item.name,
          icon: item.icon,
        }))
    }
  } catch (error) {
    console.error('加载商品分类选项失败:', error)
  }
}

// 计算排序后的商品分类列表
const sortedProductCategories = computed(() => {
  return recommendForm.productCategories
    .map(id => productCategoryOptions.value.find(item => item.id === id))
    .filter(item => item !== undefined)
})

// 商品分类变化处理
const handleProductCategoryChange = (value) => {
  // 限制最多4个
  if (value.length > 4) {
    ElMessage.warning('最多只能选择4个商品分类')
    recommendForm.productCategories = value.slice(0, 4)
  }
}

// 上移商品分类
const handleMoveProductCategoryUp = (index) => {
  if (index === 0) return
  const arr = recommendForm.productCategories
  const temp = arr[index]
  arr[index] = arr[index - 1]
  arr[index - 1] = temp
}

// 下移商品分类
const handleMoveProductCategoryDown = (index) => {
  if (index === recommendForm.productCategories.length - 1) return
  const arr = recommendForm.productCategories
  const temp = arr[index]
  arr[index] = arr[index + 1]
  arr[index + 1] = temp
}

// 加载推荐配置
const loadRecommendConfig = async () => {
  try {
    const res = await getConfigByType('RECOMMEND')
    if (res.data && res.data.length > 0) {
      res.data.forEach((config) => {
        try {
          const configValue = typeof config.configValue === 'string'
            ? JSON.parse(config.configValue)
            : config.configValue
          
          if (config.configKey === 'RECOMMEND_PRODUCT_CATEGORY') {
            // 推荐商品分类（最多4个）
            recommendForm.productCategories = configValue?.ids || []
          } else if (config.configKey === 'RECOMMEND_ATTRACTION') {
            // 推荐景点
            recommendForm.attractions = configValue?.ids || []
          } else if (config.configKey === 'RECOMMEND_HOTEL') {
            // 推荐酒店
            recommendForm.hotels = configValue?.ids || []
          }
        } catch (e) {
          console.error('解析推荐配置失败:', e)
        }
      })
    }
  } catch (error) {
    console.error('加载推荐配置失败:', error)
  }
}

// 保存推荐配置
const handleSaveRecommend = async () => {
  // 验证商品分类数量
  if (recommendForm.productCategories.length === 0) {
    ElMessage.warning('请至少选择一个商品分类')
    return
  }
  
  if (recommendForm.productCategories.length > 4) {
    ElMessage.warning('最多只能选择4个商品分类')
    return
  }
  
  saving.value = true
  try {
    const configs = [
      {
        configKey: 'RECOMMEND_PRODUCT_CATEGORY',
        configType: 'RECOMMEND',
        configValue: JSON.stringify({ ids: recommendForm.productCategories }),
        description: '推荐商品分类',
      },
      {
        configKey: 'RECOMMEND_ATTRACTION',
        configType: 'RECOMMEND',
        configValue: JSON.stringify({ ids: recommendForm.attractions }),
        description: '推荐景点',
      },
      {
        configKey: 'RECOMMEND_HOTEL',
        configType: 'RECOMMEND',
        configValue: JSON.stringify({ ids: recommendForm.hotels }),
        description: '推荐酒店',
      },
    ]

    for (const config of configs) {
      try {
        // 尝试获取现有配置
        const existingRes = await getConfigByType('RECOMMEND')
        const existing = existingRes.data?.find(
          (item) => item.configKey === config.configKey
        )

        if (existing) {
          // 更新
          await updateConfig(existing.id, {
            configValue: config.configValue,
          })
        } else {
          // 创建
          await createConfig({
            ...config,
            sort: 0,
            status: 1,
          })
        }
      } catch (error) {
        console.error(`保存${config.description}失败:`, error)
      }
    }

    ElMessage.success('保存成功')
  } catch (error) {
    console.error('保存推荐配置失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 轮播图变化
const handleBannerChange = () => {
  // 轮播图变化处理
}

// Icon图标变化
const handleIconChange = () => {
  // Icon图标变化处理
}

onMounted(() => {
  loadAttractionOptions()
  loadHotelOptions()
  loadProductCategoryOptions()
  loadRecommendConfig()
})
</script>

<style scoped>
.home-config-container {
  padding: 0;
  width: 100%;
}

.section-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
