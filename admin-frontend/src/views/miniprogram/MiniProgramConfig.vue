<template>
  <div class="miniprogram-config-container">
    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 首页配置 -->
        <el-tab-pane name="home">
          <template #label>
            <span>首页配置</span>
            <el-button
              link
              type="primary"
              size="small"
              :icon="Refresh"
              :loading="refreshing"
              @click.stop="handleRefreshHome"
              style="margin-left: 8px; padding: 0;"
              title="刷新"
            />
          </template>
          <div class="tab-content">
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
          </div>
        </el-tab-pane>

        <!-- 广告位管理 -->
        <el-tab-pane label="广告位管理" name="ad">
          <div class="tab-content">
            <el-card shadow="never">
              <template #header>
                <div class="card-header">
                  <span>广告位列表</span>
                  <el-button type="primary" size="small" @click="handleAddAd" :icon="Plus">
                    添加广告位
                  </el-button>
                </div>
              </template>
              <el-table
                :data="adList"
                v-loading="loading"
                stripe
                border
                style="width: 100%"
              >
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column label="广告图片" width="150">
                  <template #default="{ row }">
                    <el-image
                      :src="getAdImage(row)"
                      fit="cover"
                      style="width: 120px; height: 80px; border-radius: 4px;"
                      :preview-src-list="[getAdImage(row)]"
                      preview-teleported
                    />
                  </template>
                </el-table-column>
                <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
                <el-table-column prop="configKey" label="配置键" min-width="150" show-overflow-tooltip />
                <el-table-column prop="sort" label="排序" width="80" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.status === 1 ? 'success' : 'info'">
                      {{ row.status === 1 ? '启用' : '禁用' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="200" fixed="right">
                  <template #default="{ row }">
                    <el-button link type="primary" size="small" @click="handleEditAd(row)">
                      编辑
                    </el-button>
                    <el-button link type="danger" size="small" @click="handleDeleteAd(row)">
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </div>
        </el-tab-pane>

        <!-- Logo 配置 -->
        <el-tab-pane label="Logo 配置" name="logo">
          <div class="tab-content">
            <el-card shadow="never">
              <template #header>
                <div class="card-header">
                  <span>小程序 Logo 配置</span>
                </div>
              </template>
              <el-form :model="logoForm" label-width="120px">
                <el-form-item label="Logo 图片">
                  <ImageUpload
                    v-model="logoForm.logoUrl"
                    :limit="1"
                    :max-size="2"
                  />
                  <div style="margin-top: 8px; color: #909399; font-size: 12px;">
                    支持 PNG、JPG、JPEG 格式，建议尺寸 512x512 像素，最大 2MB
                  </div>
                </el-form-item>
                
                <el-form-item>
                  <el-button type="primary" @click="handleSaveLogo" :loading="savingLogo">
                    保存配置
                  </el-button>
                  <el-button @click="handleDeleteLogo" :disabled="!logoForm.logoUrl">
                    删除 Logo（恢复默认）
                  </el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </div>
        </el-tab-pane>

        <!-- 数据统计 -->
        <el-tab-pane label="数据统计" name="statistics">
          <div class="tab-content">
            <!-- 统计卡片 -->
            <el-row :gutter="20" class="statistics-row">
              <el-col :span="6">
                <el-card shadow="hover" class="stat-card">
                  <div class="stat-item">
                    <div class="stat-label">总访问量 (PV)</div>
                    <div class="stat-value">{{ statistics.access?.pv || 0 }}</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="hover" class="stat-card">
                  <div class="stat-item">
                    <div class="stat-label">独立访客 (UV)</div>
                    <div class="stat-value">{{ statistics.access?.uv || 0 }}</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="hover" class="stat-card">
                  <div class="stat-item">
                    <div class="stat-label">总订单数</div>
                    <div class="stat-value">{{ statistics.order?.totalOrders || 0 }}</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="hover" class="stat-card">
                  <div class="stat-item">
                    <div class="stat-label">订单转化率</div>
                    <div class="stat-value">
                      {{ formatPercent(statistics.order?.conversionRate) }}
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <!-- 时间范围选择 -->
            <el-card shadow="never" class="section-card">
              <el-form :inline="true" :model="statisticsForm">
                <el-form-item label="开始日期">
                  <el-date-picker
                    v-model="statisticsForm.startDate"
                    type="date"
                    placeholder="选择开始日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                  />
                </el-form-item>
                <el-form-item label="结束日期">
                  <el-date-picker
                    v-model="statisticsForm.endDate"
                    type="date"
                    placeholder="选择结束日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="loadStatistics">查询</el-button>
                  <el-button @click="handleResetStatistics">重置</el-button>
                </el-form-item>
              </el-form>
            </el-card>

            <!-- 访问统计图表 -->
            <el-card shadow="never" class="section-card">
              <template #header>
                <div class="card-header">
                  <span>访问统计</span>
                </div>
              </template>
              <v-chart
                v-if="activeTab === 'statistics'"
                :option="accessChartOption"
                style="height: 300px; width: 100%"
                autoresize
              />
            </el-card>

            <!-- 订单统计图表 -->
            <el-card shadow="never" class="section-card">
              <template #header>
                <div class="card-header">
                  <span>订单统计</span>
                </div>
              </template>
              <v-chart
                v-if="activeTab === 'statistics'"
                :option="orderChartOption"
                style="height: 300px; width: 100%"
                autoresize
              />
            </el-card>

            <!-- 用户统计图表 -->
            <el-card shadow="never" class="section-card">
              <template #header>
                <div class="card-header">
                  <span>用户统计</span>
                </div>
              </template>
              <v-chart
                v-if="activeTab === 'statistics'"
                :option="userChartOption"
                style="height: 300px; width: 100%"
                autoresize
              />
            </el-card>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 添加/编辑广告位对话框 -->
    <el-dialog
      v-model="adDialogVisible"
      :title="adDialogTitle"
      width="600px"
      @close="handleAdDialogClose"
    >
      <el-form
        ref="adFormRef"
        :model="adFormData"
        :rules="adFormRules"
        label-width="100px"
      >
        <el-form-item label="广告图片" prop="image">
          <ImageUpload
            v-model="adFormData.image"
            :limit="1"
            :max-size="5"
          />
        </el-form-item>
        <el-form-item label="跳转链接" prop="link">
          <el-input v-model="adFormData.link" placeholder="请输入跳转链接（可选）" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="adFormData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入广告描述"
          />
        </el-form-item>
        <el-form-item label="配置键" prop="configKey">
          <el-input
            v-model="adFormData.configKey"
            placeholder="请输入配置键（唯一标识）"
            :disabled="editingAdId !== null"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="adFormData.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="adFormData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitAd" :loading="adSubmitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
} from 'echarts/components'
import VChart from 'vue-echarts'

// 注册ECharts组件
use([
  CanvasRenderer,
  LineChart,
  BarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
])
import BannerManager from '@/components/BannerManager.vue'
import IconManager from '@/components/IconManager.vue'
import ImageUpload from '@/components/ImageUpload.vue'
import {
  getConfigByType,
  getConfigByKey,
  createConfig,
  updateConfig,
  deleteConfig,
  getStatistics,
} from '@/api/miniprogram'
import { getAttractionList } from '@/api/attractions'
import { getHotelList } from '@/api/hotels'
import { getCategoryList } from '@/api/categories'

const activeTab = ref('home')
const loading = ref(false)
const saving = ref(false)
const savingLogo = ref(false)
const refreshing = ref(false)
const banners = ref([])
const icons = ref([])
const adList = ref([])
const attractionOptions = ref([])
const hotelOptions = ref([])
const productCategoryOptions = ref([])
const bannerManagerRef = ref(null)
const iconManagerRef = ref(null)

// Logo 配置表单
const logoForm = reactive({
  logoUrl: '',
})

// 推荐表单
const recommendForm = reactive({
  productCategories: [], // 商品分类ID列表（最多4个，按顺序）
  attractions: [], // 后期待实现
  hotels: [], // 后待实现
})

// 统计数据
const statistics = ref({
  access: {},
  order: {},
  product: {},
  user: {},
})

// 统计表单
const statisticsForm = reactive({
  startDate: '',
  endDate: '',
})

// 广告位对话框
const adDialogVisible = ref(false)
const adDialogTitle = ref('添加广告位')
const adSubmitting = ref(false)
const adFormRef = ref(null)
const editingAdId = ref(null)

// 广告表单数据
const adFormData = reactive({
  image: '',
  link: '',
  description: '',
  configKey: '',
  sort: 0,
  status: 1,
})

// 广告表单验证规则
const adFormRules = {
  image: [{ required: true, message: '请上传广告图片', trigger: 'change' }],
  configKey: [{ required: true, message: '请输入配置键', trigger: 'blur' }],
}

// Tab切换
const handleTabChange = (tabName) => {
  if (tabName === 'home') {
    // 首页配置已加载
  } else if (tabName === 'ad') {
    loadAdList()
  } else if (tabName === 'logo') {
    loadLogoConfig()
  } else if (tabName === 'statistics') {
    loadStatistics()
  }
}

// 刷新首页配置
const handleRefreshHome = async () => {
  refreshing.value = true
  try {
    // 刷新轮播图
    if (bannerManagerRef.value && bannerManagerRef.value.loadBanners) {
      await bannerManagerRef.value.loadBanners()
    }
    // 刷新Icon图标
    if (iconManagerRef.value && iconManagerRef.value.loadIcons) {
      await iconManagerRef.value.loadIcons()
    }
    // 刷新推荐配置
    await loadRecommendConfig()
    // 刷新景点选项
    await loadAttractionOptions()
    // 刷新酒店选项
    await loadHotelOptions()
    // 刷新商品分类选项
    await loadProductCategoryOptions()
    
    ElMessage.success('刷新成功')
  } catch (error) {
    console.error('刷新失败:', error)
    ElMessage.error('刷新失败')
  } finally {
    refreshing.value = false
  }
}

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

// 推荐内容变化
const handleRecommendChange = (type) => {
  // 可以在这里添加实时保存逻辑
}

// 轮播图变化
const handleBannerChange = () => {
  // 轮播图变化处理
}

// Icon图标变化
const handleIconChange = () => {
  // Icon图标变化处理
}

// 加载广告位列表
const loadAdList = async () => {
  loading.value = true
  try {
    const res = await getConfigByType('AD', { status: null })
    if (res.data) {
      adList.value = res.data.sort((a, b) => (a.sort || 0) - (b.sort || 0))
    }
  } catch (error) {
    console.error('加载广告位列表失败:', error)
    ElMessage.error('加载广告位列表失败')
  } finally {
    loading.value = false
  }
}

// 获取广告图片
const getAdImage = (row) => {
  try {
    const configValue = typeof row.configValue === 'string'
      ? JSON.parse(row.configValue)
      : row.configValue
    return configValue?.image || ''
  } catch (e) {
    return ''
  }
}

// 添加广告位
const handleAddAd = () => {
  editingAdId.value = null
  adDialogTitle.value = '添加广告位'
  resetAdForm()
  adDialogVisible.value = true
}

// 编辑广告位
const handleEditAd = (row) => {
  editingAdId.value = row.id
  adDialogTitle.value = '编辑广告位'
  
  try {
    const configValue = typeof row.configValue === 'string'
      ? JSON.parse(row.configValue)
      : row.configValue
    
    adFormData.image = configValue?.image || ''
    adFormData.link = configValue?.link || ''
    adFormData.description = row.description || ''
    adFormData.configKey = row.configKey || ''
    adFormData.sort = row.sort || 0
    adFormData.status = row.status !== undefined ? row.status : 1
  } catch (e) {
    console.error('解析广告配置失败:', e)
  }
  
  adDialogVisible.value = true
}

// 删除广告位
const handleDeleteAd = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除广告位"${row.description || row.configKey}"吗？`,
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
      await loadAdList()
    } catch (error) {
      console.error('删除广告位失败:', error)
      ElMessage.error('删除广告位失败')
    } finally {
      loading.value = false
    }
  } catch (error) {
    // 用户取消
  }
}

// 提交广告表单
const handleSubmitAd = async () => {
  if (!adFormRef.value) return

  try {
    await adFormRef.value.validate()
    adSubmitting.value = true

    const configValue = {
      image: adFormData.image,
      link: adFormData.link || '',
    }

    if (editingAdId.value !== null) {
      // 编辑
      await updateConfig(editingAdId.value, {
        configValue: JSON.stringify(configValue),
        description: adFormData.description,
        sort: adFormData.sort,
        status: adFormData.status,
      })
      ElMessage.success('更新成功')
    } else {
      // 添加
      await createConfig({
        configKey: adFormData.configKey,
        configType: 'AD',
        configValue: JSON.stringify(configValue),
        description: adFormData.description,
        sort: adFormData.sort,
        status: adFormData.status,
      })
      ElMessage.success('添加成功')
    }

    adDialogVisible.value = false
    await loadAdList()
  } catch (error) {
    if (error !== false) {
      console.error('提交失败:', error)
      ElMessage.error('提交失败')
    }
  } finally {
    adSubmitting.value = false
  }
}

// 重置广告表单
const resetAdForm = () => {
  adFormData.image = ''
  adFormData.link = ''
  adFormData.description = ''
  adFormData.configKey = ''
  adFormData.sort = adList.value.length > 0
    ? Math.max(...adList.value.map(item => item.sort || 0)) + 1
    : 1
  adFormData.status = 1
  if (adFormRef.value) {
    adFormRef.value.clearValidate()
  }
}

// 广告对话框关闭
const handleAdDialogClose = () => {
  resetAdForm()
  editingAdId.value = null
}

// 加载统计数据
const loadStatistics = async () => {
  loading.value = true
  try {
    const params = {}
    if (statisticsForm.startDate) {
      params.startDate = statisticsForm.startDate
    }
    if (statisticsForm.endDate) {
      params.endDate = statisticsForm.endDate
    }

    const res = await getStatistics(params)
    if (res.data) {
      statistics.value = res.data
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
}

// 重置统计表单
const handleResetStatistics = () => {
  statisticsForm.startDate = ''
  statisticsForm.endDate = ''
  loadStatistics()
}

// 格式化百分比
const formatPercent = (value) => {
  if (!value) return '0%'
  return `${Number(value).toFixed(2)}%`
}

// 访问统计图表配置
const accessChartOption = computed(() => {
  const pvTrend = statistics.value.access?.pvTrend || []
  const uvTrend = statistics.value.access?.uvTrend || []

  return {
    title: {
      text: '访问趋势',
      left: 'center',
    },
    tooltip: {
      trigger: 'axis',
    },
    legend: {
      data: ['PV', 'UV'],
      bottom: 0,
    },
    xAxis: {
      type: 'category',
      data: pvTrend.map((item) => item.date || item.day),
    },
    yAxis: {
      type: 'value',
    },
    series: [
      {
        name: 'PV',
        type: 'line',
        data: pvTrend.map((item) => item.count || item.value || 0),
      },
      {
        name: 'UV',
        type: 'line',
        data: uvTrend.map((item) => item.count || item.value || 0),
      },
    ],
  }
})

// 订单统计图表配置
const orderChartOption = computed(() => {
  const orderTrend = statistics.value.order?.orderTrend || []
  const amountTrend = statistics.value.order?.amountTrend || []

  return {
    title: {
      text: '订单趋势',
      left: 'center',
    },
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        let result = `${params[0].axisValue}<br/>`
        params.forEach((param) => {
          if (param.seriesName === '订单数') {
            result += `${param.marker}${param.seriesName}: ${param.value}<br/>`
          } else {
            result += `${param.marker}${param.seriesName}: ¥${param.value}<br/>`
          }
        })
        return result
      },
    },
    legend: {
      data: ['订单数', '订单金额'],
      bottom: 0,
    },
    xAxis: {
      type: 'category',
      data: orderTrend.map((item) => item.date || item.day),
    },
    yAxis: [
      {
        type: 'value',
        name: '订单数',
        position: 'left',
      },
      {
        type: 'value',
        name: '订单金额',
        position: 'right',
      },
    ],
    series: [
      {
        name: '订单数',
        type: 'bar',
        data: orderTrend.map((item) => item.count || item.value || 0),
      },
      {
        name: '订单金额',
        type: 'line',
        yAxisIndex: 1,
        data: amountTrend.map((item) => item.amount || item.value || 0),
      },
    ],
  }
})

// 用户统计图表配置
const userChartOption = computed(() => {
  const newUserTrend = statistics.value.user?.newUserTrend || []

  return {
    title: {
      text: '新增用户趋势',
      left: 'center',
    },
    tooltip: {
      trigger: 'axis',
    },
    xAxis: {
      type: 'category',
      data: newUserTrend.map((item) => item.date || item.day),
    },
    yAxis: {
      type: 'value',
    },
    series: [
      {
        name: '新增用户',
        type: 'line',
        data: newUserTrend.map((item) => item.count || item.value || 0),
      },
    ],
  }
})

// 加载 Logo 配置
const loadLogoConfig = async () => {
  try {
    const res = await getConfigByKey('MINIPROGRAM_LOGO')
    if (res.data && res.data.configValue) {
      try {
        const configValue = JSON.parse(res.data.configValue)
        logoForm.logoUrl = configValue.logoUrl || ''
      } catch (e) {
        console.error('解析 Logo 配置失败:', e)
      }
    }
  } catch (error) {
    // 配置不存在时，logoUrl 保持为空字符串
    console.log('Logo 配置不存在，使用默认值')
  }
}

// 保存 Logo 配置
const handleSaveLogo = async () => {
  if (!logoForm.logoUrl) {
    ElMessage.warning('请上传 Logo 图片')
    return
  }
  
  savingLogo.value = true
  try {
    const configValue = JSON.stringify({
      logoUrl: logoForm.logoUrl
    })
    
    // 尝试获取现有配置
    let existingConfig = null
    try {
      const res = await getConfigByKey('MINIPROGRAM_LOGO')
      existingConfig = res.data
    } catch (error) {
      // 配置不存在，需要创建
    }
    
    if (existingConfig) {
      // 更新配置
      await updateConfig(existingConfig.id, {
        configValue: configValue,
      })
    } else {
      // 创建配置
      await createConfig({
        configKey: 'MINIPROGRAM_LOGO',
        configType: 'SYSTEM',
        configValue: configValue,
        description: '小程序 Logo 配置',
        sort: 0,
        status: 1,
      })
    }
    
    ElMessage.success('保存成功')
  } catch (error) {
    console.error('保存 Logo 配置失败:', error)
    ElMessage.error('保存失败')
  } finally {
    savingLogo.value = false
  }
}

// 删除 Logo 配置
const handleDeleteLogo = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要删除 Logo 配置吗？删除后将使用默认 Logo。',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    try {
      const res = await getConfigByKey('MINIPROGRAM_LOGO')
      if (res.data) {
        await deleteConfig(res.data.id)
        logoForm.logoUrl = ''
        ElMessage.success('删除成功')
      }
    } catch (error) {
      // 配置不存在
      ElMessage.info('配置不存在')
    }
  } catch (error) {
    // 用户取消
  }
}

onMounted(() => {
  loadAttractionOptions()
  loadHotelOptions()
  loadProductCategoryOptions()
  loadRecommendConfig()
  loadStatistics()
})
</script>

<style scoped>
.miniprogram-config-container {
  padding: 0;
  width: 100%;
}

.tab-content {
  padding: 20px 0;
}

.section-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.statistics-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

.stat-item {
  padding: 10px 0;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}
</style>
