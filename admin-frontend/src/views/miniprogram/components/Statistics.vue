<template>
  <div class="statistics-container">
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
        :option="userChartOption"
        style="height: 300px; width: 100%"
        autoresize
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
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
import { getStatistics } from '@/api/miniprogram'

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

const loading = ref(false)

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

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.statistics-container {
  padding: 0;
  width: 100%;
}

.statistics-row {
  margin-bottom: 20px;
}

.section-card {
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
