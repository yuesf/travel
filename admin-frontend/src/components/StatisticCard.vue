<template>
  <el-card class="statistic-card" shadow="hover">
    <div class="card-content">
      <div class="card-left">
        <div class="card-title">{{ title }}</div>
        <div class="card-value">
          <span class="value-number">{{ formatValue(value) }}</span>
          <span v-if="unit" class="value-unit">{{ unit }}</span>
        </div>
      </div>
      <div class="card-right">
        <el-icon :size="48" :color="iconColor">
          <component :is="iconComponent" />
        </el-icon>
      </div>
    </div>
    <div v-if="change !== undefined" class="card-footer">
      <span :class="['change-text', change >= 0 ? 'positive' : 'negative']">
        <el-icon :size="14">
          <component :is="change >= 0 ? 'ArrowUp' : 'ArrowDown'" />
        </el-icon>
        {{ Math.abs(change) }}% {{ change >= 0 ? '增长' : '下降' }}
      </span>
      <span class="change-label">较昨日</span>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const props = defineProps({
  title: {
    type: String,
    required: true,
  },
  value: {
    type: [Number, String],
    required: true,
  },
  unit: {
    type: String,
    default: '',
  },
  icon: {
    type: String,
    required: true,
  },
  iconColor: {
    type: String,
    default: '#409eff',
  },
  change: {
    type: Number,
    default: undefined,
  },
})

// 获取图标组件
const iconComponent = computed(() => {
  return ElementPlusIconsVue[props.icon] || ElementPlusIconsVue['QuestionFilled']
})

const formatValue = (val) => {
  if (typeof val === 'number') {
    if (val >= 10000) {
      return (val / 10000).toFixed(2) + '万'
    }
    return val.toLocaleString()
  }
  return val
}
</script>

<style scoped>
.statistic-card {
  height: 100%;
}

.card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-left {
  flex: 1;
}

.card-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 12px;
}

.card-value {
  display: flex;
  align-items: baseline;
}

.value-number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
}

.value-unit {
  font-size: 14px;
  color: #909399;
  margin-left: 4px;
}

.card-right {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 8px;
  background-color: rgba(64, 158, 255, 0.1);
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
  font-size: 12px;
}

.change-text {
  display: flex;
  align-items: center;
  gap: 4px;
}

.change-text.positive {
  color: #67c23a;
}

.change-text.negative {
  color: #f56c6c;
}

.change-label {
  color: #909399;
}
</style>
