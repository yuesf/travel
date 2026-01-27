<template>
  <div class="time-picker-container">
    <el-time-picker
      v-model="timeValue"
      :placeholder="placeholder"
      :disabled="disabled"
      :format="format"
      :value-format="valueFormat"
      :is-range="isRange"
      :range-separator="rangeSeparator"
      :start-placeholder="startPlaceholder"
      :end-placeholder="endPlaceholder"
      :disabled-minutes="disabledMinutes"
      style="width: 100%"
      @change="handleChange"
    />
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: [String, Array],
    default: null,
  },
  placeholder: {
    type: String,
    default: '请选择时间',
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  format: {
    type: String,
    default: 'HH:mm',
  },
  valueFormat: {
    type: String,
    default: 'HH:mm',
  },
  // 是否时间范围选择
  isRange: {
    type: Boolean,
    default: false,
  },
  rangeSeparator: {
    type: String,
    default: '至',
  },
  startPlaceholder: {
    type: String,
    default: '开始时间',
  },
  endPlaceholder: {
    type: String,
    default: '结束时间',
  },
  // 分钟步长
  minuteStep: {
    type: Number,
    default: 30,
  },
})

const emit = defineEmits(['update:modelValue', 'change'])

const timeValue = ref(null)

// 配置时间选择器选项，设置分钟步长为30（每半点一个刻度）
const disabledMinutes = computed(() => {
  const step = props.minuteStep || 30
  // 生成允许的分钟选项：00, 30
  const allowedMinutes = []
  for (let i = 0; i < 60; i += step) {
    allowedMinutes.push(i)
  }
  
  // 生成需要禁用的分钟列表
  const disabledMinutesList = []
  for (let i = 0; i < 60; i++) {
    if (!allowedMinutes.includes(i)) {
      disabledMinutesList.push(i)
    }
  }
  
  // 返回一个函数，Element Plus 2.x 需要函数形式
  return (hour) => disabledMinutesList
})

// 同步外部值到内部
watch(
  () => props.modelValue,
  (newVal) => {
    timeValue.value = newVal
  },
  { immediate: true }
)

// 时间变化
const handleChange = (val) => {
  emit('update:modelValue', val)
  emit('change', val)
}
</script>

<style scoped>
.time-picker-container {
  width: 100%;
}
</style>
