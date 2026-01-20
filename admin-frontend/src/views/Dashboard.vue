<template>
  <div class="dashboard-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6" v-for="stat in stats" :key="stat.key">
        <StatisticCard
          :title="stat.title"
          :value="stat.value"
          :unit="stat.unit"
          :icon="stat.icon"
          :icon-color="stat.iconColor"
          :change="stat.change"
        />
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>订单趋势</span>
              <el-radio-group v-model="orderTrendType" size="small" @change="loadOrderTrend">
                <el-radio-button value="7">近7天</el-radio-button>
                <el-radio-button value="30">近30天</el-radio-button>
                <el-radio-button value="90">近90天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <v-chart
            :option="orderTrendOption"
            :loading="orderTrendLoading"
            style="height: 300px"
            autoresize
          />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>销售统计</span>
              <el-radio-group v-model="salesStatsType" size="small" @change="loadSalesStats">
                <el-radio-button value="7">近7天</el-radio-button>
                <el-radio-button value="30">近30天</el-radio-button>
                <el-radio-button value="90">近90天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <v-chart
            :option="salesStatsOption"
            :loading="salesStatsLoading"
            style="height: 300px"
            autoresize
          />
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-card class="quick-actions-card" shadow="hover">
      <template #header>
        <span>快捷操作</span>
      </template>
      <el-row :gutter="20">
        <el-col :xs="12" :sm="6" :md="4" v-for="action in quickActions" :key="action.path">
          <div class="quick-action-item" @click="handleQuickAction(action.path)">
            <el-icon :size="32" :color="action.color">
              <component :is="getIconComponent(action.icon)" />
            </el-icon>
            <span class="action-title">{{ action.title }}</span>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 缓存管理 -->
    <el-card class="cache-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>缓存管理</span>
          <div>
            <el-button
              type="info"
              size="small"
              :loading="cacheStatsLoading"
              @click="loadCacheStats"
            >
              <el-icon><Refresh /></el-icon>
              刷新统计
            </el-button>
            <el-button
              type="danger"
              size="small"
              :loading="cacheClearing"
              @click="handleClearAllCache"
              style="margin-left: 8px"
            >
              <el-icon><Delete /></el-icon>
              清除所有缓存
            </el-button>
          </div>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
          v-for="cache in cacheTypes"
          :key="cache.type"
        >
          <div class="cache-item">
            <div class="cache-info">
              <span class="cache-name">{{ cache.name }}</span>
              <span class="cache-desc">{{ cache.desc }}</span>
              <div class="cache-stats" v-if="cacheStats[cache.type]">
                <span class="cache-stat-item">
                  条目: <strong>{{ cacheStats[cache.type].estimatedSize || 0 }}</strong>
                </span>
                <span class="cache-stat-item" v-if="getHitRate(cache.type) !== null">
                  命中率: <strong>{{ getHitRate(cache.type) }}%</strong>
                </span>
              </div>
            </div>
            <el-button
              type="warning"
              size="small"
              :loading="cacheClearingType === cache.type"
              @click="handleClearCache(cache.type)"
            >
              清除
            </el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Refresh } from '@element-plus/icons-vue'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import StatisticCard from '@/components/StatisticCard.vue'
import {
  getDashboardStats,
  getOrderTrend,
  getSalesStats,
  clearAllCache,
  clearCache,
  getCacheStats,
} from '@/api/dashboard'

const router = useRouter()

// 统计数据
const stats = ref([
  {
    key: 'orders',
    title: '今日订单数',
    value: 0,
    unit: '单',
    icon: 'ShoppingCart',
    iconColor: '#409eff',
    change: 0,
  },
  {
    key: 'sales',
    title: '今日销售额',
    value: 0,
    unit: '元',
    icon: 'Money',
    iconColor: '#67c23a',
    change: 0,
  },
  {
    key: 'pending',
    title: '待处理订单',
    value: 0,
    unit: '单',
    icon: 'Clock',
    iconColor: '#e6a23c',
    change: 0,
  },
  {
    key: 'users',
    title: '用户总数',
    value: 0,
    unit: '人',
    icon: 'User',
    iconColor: '#f56c6c',
    change: 0,
  },
])

// 订单趋势
const orderTrendType = ref('7')
const orderTrendLoading = ref(false)
const orderTrendOption = reactive({
  tooltip: {
    trigger: 'axis',
  },
  legend: {
    data: ['订单数'],
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true,
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: [],
  },
  yAxis: {
    type: 'value',
  },
  series: [
    {
      name: '订单数',
      type: 'line',
      smooth: true,
      data: [],
      itemStyle: {
        color: '#409eff',
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.1)' },
          ],
        },
      },
    },
  ],
})

// 销售统计
const salesStatsType = ref('7')
const salesStatsLoading = ref(false)
const salesStatsOption = reactive({
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow',
    },
  },
  legend: {
    data: ['销售额'],
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true,
  },
  xAxis: {
    type: 'category',
    data: [],
  },
  yAxis: {
    type: 'value',
  },
  series: [
    {
      name: '销售额',
      type: 'bar',
      data: [],
      itemStyle: {
        color: '#67c23a',
      },
    },
  ],
})

// 快捷操作
const quickActions = ref([
  { title: '创建景点', icon: 'Plus', path: '/attractions/create', color: '#409eff' },
  { title: '创建酒店', icon: 'OfficeBuilding', path: '/hotels/create', color: '#67c23a' },
  { title: '创建商品', icon: 'Goods', path: '/products/create', color: '#e6a23c' },
  { title: '创建优惠券', icon: 'Ticket', path: '/coupons/create', color: '#f56c6c' },
  { title: '查看订单', icon: 'Document', path: '/orders', color: '#909399' },
])

// 缓存管理
const cacheClearing = ref(false)
const cacheClearingType = ref('')
const cacheStatsLoading = ref(false)
const cacheStats = ref({})
const cacheTypes = ref([
  { type: 'token', name: 'Token黑名单', desc: '清除登录Token黑名单缓存', key: 'tokenBlacklist' },
  { type: 'sms', name: '短信验证码', desc: '清除短信验证码缓存', key: 'smsCode' },
  { type: 'miniprogram', name: '小程序配置', desc: '清除小程序配置缓存', key: 'miniprogramConfig' },
  { type: 'home', name: '首页数据', desc: '清除首页数据缓存', key: 'home' },
  { type: 'attraction', name: '景点详情', desc: '清除景点详情缓存', key: 'attractionDetail' },
  { type: 'product', name: '商品详情', desc: '清除商品详情缓存', key: 'productDetail' },
  { type: 'article', name: '文章详情', desc: '清除文章详情缓存', key: 'articleDetail' },
  { type: 'articleView', name: '文章阅读量', desc: '清除文章阅读量缓存', key: 'articleView' },
])

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await getDashboardStats()
    if (res.data) {
      const data = res.data
      stats.value[0].value = data.todayOrders || 0
      stats.value[0].change = data.todayOrdersChange || 0
      stats.value[1].value = data.todaySales || 0
      stats.value[1].change = data.todaySalesChange || 0
      stats.value[2].value = data.pendingOrders || 0
      stats.value[3].value = data.totalUsers || 0
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    // 使用模拟数据
    stats.value[0].value = 128
    stats.value[0].change = 12.5
    stats.value[1].value = 56890
    stats.value[1].change = 8.3
    stats.value[2].value = 23
    stats.value[3].value = 1234
  }
}

// 加载订单趋势
const loadOrderTrend = async () => {
  orderTrendLoading.value = true
  try {
    const res = await getOrderTrend({ days: orderTrendType.value })
    if (res.data) {
      orderTrendOption.xAxis.data = res.data.dates || []
      orderTrendOption.series[0].data = res.data.values || []
    }
  } catch (error) {
    console.error('加载订单趋势失败:', error)
    // 使用模拟数据
    const days = parseInt(orderTrendType.value)
    orderTrendOption.xAxis.data = Array.from({ length: days }, (_, i) => {
      const date = new Date()
      date.setDate(date.getDate() - (days - i - 1))
      return `${date.getMonth() + 1}/${date.getDate()}`
    })
    orderTrendOption.series[0].data = Array.from({ length: days }, () =>
      Math.floor(Math.random() * 100) + 50
    )
  } finally {
    orderTrendLoading.value = false
  }
}

// 加载销售统计
const loadSalesStats = async () => {
  salesStatsLoading.value = true
  try {
    const res = await getSalesStats({ days: salesStatsType.value })
    if (res.data) {
      salesStatsOption.xAxis.data = res.data.dates || []
      salesStatsOption.series[0].data = res.data.values || []
    }
  } catch (error) {
    console.error('加载销售统计失败:', error)
    // 使用模拟数据
    const days = parseInt(salesStatsType.value)
    salesStatsOption.xAxis.data = Array.from({ length: days }, (_, i) => {
      const date = new Date()
      date.setDate(date.getDate() - (days - i - 1))
      return `${date.getMonth() + 1}/${date.getDate()}`
    })
    salesStatsOption.series[0].data = Array.from({ length: days }, () =>
      Math.floor(Math.random() * 50000) + 20000
    )
  } finally {
    salesStatsLoading.value = false
  }
}

// 获取图标组件
const getIconComponent = (iconName) => {
  return ElementPlusIconsVue[iconName] || ElementPlusIconsVue['QuestionFilled']
}

// 快捷操作
const handleQuickAction = (path) => {
  router.push(path)
}

// 清除所有缓存
const handleClearAllCache = async () => {
  try {
    await ElMessageBox.confirm('确定要清除所有缓存吗？此操作不可恢复。', '确认清除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    
    cacheClearing.value = true
    await clearAllCache()
    ElMessage.success('所有缓存已清除')
    // 清除后刷新统计信息
    await loadCacheStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清除缓存失败:', error)
      ElMessage.error('清除缓存失败')
    }
  } finally {
    cacheClearing.value = false
  }
}

// 清除指定类型的缓存
const handleClearCache = async (cacheType) => {
  try {
    const cacheInfo = cacheTypes.value.find((c) => c.type === cacheType)
    await ElMessageBox.confirm(
      `确定要清除${cacheInfo?.name}缓存吗？`,
      '确认清除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    cacheClearingType.value = cacheType
    await clearCache(cacheType)
    ElMessage.success(`${cacheInfo?.name}缓存已清除`)
    // 清除后刷新统计信息
    await loadCacheStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清除缓存失败:', error)
      ElMessage.error('清除缓存失败')
    }
  } finally {
    cacheClearingType.value = ''
  }
}

// 加载缓存统计信息
const loadCacheStats = async () => {
  cacheStatsLoading.value = true
  try {
    const res = await getCacheStats()
    if (res.data) {
      cacheStats.value = {}
      // 将后端返回的统计信息映射到前端缓存类型
      cacheTypes.value.forEach((cache) => {
        if (res.data[cache.key]) {
          cacheStats.value[cache.type] = res.data[cache.key]
        }
      })
    }
  } catch (error) {
    console.error('加载缓存统计失败:', error)
  } finally {
    cacheStatsLoading.value = false
  }
}

// 计算缓存命中率
const getHitRate = (cacheType) => {
  const stats = cacheStats.value[cacheType]
  if (!stats || stats.hitCount === undefined || stats.missCount === undefined) {
    return null
  }
  const total = stats.hitCount + stats.missCount
  if (total === 0) {
    return null
  }
  return ((stats.hitCount / total) * 100).toFixed(1)
}

onMounted(() => {
  loadStats()
  loadOrderTrend()
  loadSalesStats()
  loadCacheStats()

  // 定时刷新统计数据（每5分钟）
  setInterval(() => {
    loadStats()
  }, 5 * 60 * 1000)
})
</script>

<style scoped>
.dashboard-container {
  padding: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.quick-actions-card {
  margin-bottom: 20px;
}

.quick-action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #f5f7fa;
}

.quick-action-item:hover {
  background-color: #e4e7ed;
  transform: translateY(-2px);
}

.action-title {
  margin-top: 8px;
  font-size: 14px;
  color: #606266;
}

.cache-card {
  margin-bottom: 20px;
}

.cache-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  margin-bottom: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background-color: #fafafa;
  transition: all 0.3s;
}

.cache-item:hover {
  border-color: #c0c4cc;
  background-color: #f5f7fa;
}

.cache-info {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.cache-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.cache-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.cache-stats {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.cache-stat-item {
  font-size: 12px;
  color: #606266;
}

.cache-stat-item strong {
  color: #409eff;
  font-weight: 600;
}
</style>
