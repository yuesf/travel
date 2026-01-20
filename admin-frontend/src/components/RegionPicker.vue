<template>
  <div class="region-picker-container">
    <el-cascader
      v-model="regionValue"
      :options="regionOptions"
      :props="cascaderProps"
      :placeholder="placeholder"
      :disabled="disabled"
      :clearable="clearable"
      :show-all-levels="showAllLevels"
      style="width: 100%"
      @change="handleChange"
    />
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => [],
  },
  placeholder: {
    type: String,
    default: '请选择省市区',
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  clearable: {
    type: Boolean,
    default: true,
  },
  showAllLevels: {
    type: Boolean,
    default: true,
  },
})

const emit = defineEmits(['update:modelValue', 'change'])

const regionValue = ref([])

const cascaderProps = {
  value: 'code',
  label: 'name',
  children: 'children',
  emitPath: true, // 返回完整路径数组
}

// 省市区数据（简化版，实际项目中应该从API获取或使用完整的数据文件）
const regionOptions = ref([
  {
    code: '110000',
    name: '北京市',
    children: [
      {
        code: '110100',
        name: '北京市',
        children: [
          { code: '110101', name: '东城区' },
          { code: '110102', name: '西城区' },
          { code: '110105', name: '朝阳区' },
          { code: '110106', name: '丰台区' },
          { code: '110107', name: '石景山区' },
          { code: '110108', name: '海淀区' },
          { code: '110109', name: '门头沟区' },
          { code: '110111', name: '房山区' },
          { code: '110112', name: '通州区' },
          { code: '110113', name: '顺义区' },
          { code: '110114', name: '昌平区' },
          { code: '110115', name: '大兴区' },
          { code: '110116', name: '怀柔区' },
          { code: '110117', name: '平谷区' },
          { code: '110118', name: '密云区' },
          { code: '110119', name: '延庆区' },
        ],
      },
    ],
  },
  {
    code: '120000',
    name: '天津市',
    children: [
      {
        code: '120100',
        name: '天津市',
        children: [
          { code: '120101', name: '和平区' },
          { code: '120102', name: '河东区' },
          { code: '120103', name: '河西区' },
          { code: '120104', name: '南开区' },
          { code: '120105', name: '河北区' },
          { code: '120106', name: '红桥区' },
          { code: '120110', name: '东丽区' },
          { code: '120111', name: '西青区' },
          { code: '120112', name: '津南区' },
          { code: '120113', name: '北辰区' },
          { code: '120114', name: '武清区' },
          { code: '120115', name: '宝坻区' },
          { code: '120116', name: '滨海新区' },
          { code: '120117', name: '宁河区' },
          { code: '120118', name: '静海区' },
          { code: '120119', name: '蓟州区' },
        ],
      },
    ],
  },
  {
    code: '310000',
    name: '上海市',
    children: [
      {
        code: '310100',
        name: '上海市',
        children: [
          { code: '310101', name: '黄浦区' },
          { code: '310104', name: '徐汇区' },
          { code: '310105', name: '长宁区' },
          { code: '310106', name: '静安区' },
          { code: '310107', name: '普陀区' },
          { code: '310109', name: '虹口区' },
          { code: '310110', name: '杨浦区' },
          { code: '310112', name: '闵行区' },
          { code: '310113', name: '宝山区' },
          { code: '310114', name: '嘉定区' },
          { code: '310115', name: '浦东新区' },
          { code: '310116', name: '金山区' },
          { code: '310117', name: '松江区' },
          { code: '310118', name: '青浦区' },
          { code: '310120', name: '奉贤区' },
          { code: '310151', name: '崇明区' },
        ],
      },
    ],
  },
  {
    code: '330000',
    name: '浙江省',
    children: [
      {
        code: '330100',
        name: '杭州市',
        children: [
          { code: '330102', name: '上城区' },
          { code: '330105', name: '拱墅区' },
          { code: '330106', name: '西湖区' },
          { code: '330108', name: '滨江区' },
          { code: '330109', name: '萧山区' },
          { code: '330110', name: '余杭区' },
          { code: '330111', name: '富阳区' },
          { code: '330112', name: '临安区' },
          { code: '330113', name: '临平区' },
          { code: '330114', name: '钱塘区' },
          { code: '330122', name: '桐庐县' },
          { code: '330127', name: '淳安县' },
          { code: '330182', name: '建德市' },
        ],
      },
      {
        code: '330200',
        name: '宁波市',
        children: [
          { code: '330203', name: '海曙区' },
          { code: '330205', name: '江北区' },
          { code: '330206', name: '北仑区' },
          { code: '330211', name: '镇海区' },
          { code: '330212', name: '鄞州区' },
          { code: '330213', name: '奉化区' },
          { code: '330225', name: '象山县' },
          { code: '330226', name: '宁海县' },
          { code: '330281', name: '余姚市' },
          { code: '330282', name: '慈溪市' },
        ],
      },
    ],
  },
  {
    code: '440000',
    name: '广东省',
    children: [
      {
        code: '440100',
        name: '广州市',
        children: [
          { code: '440103', name: '荔湾区' },
          { code: '440104', name: '越秀区' },
          { code: '440105', name: '海珠区' },
          { code: '440106', name: '天河区' },
          { code: '440111', name: '白云区' },
          { code: '440112', name: '黄埔区' },
          { code: '440113', name: '番禺区' },
          { code: '440114', name: '花都区' },
          { code: '440115', name: '南沙区' },
          { code: '440117', name: '从化区' },
          { code: '440118', name: '增城区' },
        ],
      },
      {
        code: '440300',
        name: '深圳市',
        children: [
          { code: '440303', name: '罗湖区' },
          { code: '440304', name: '福田区' },
          { code: '440305', name: '南山区' },
          { code: '440306', name: '宝安区' },
          { code: '440307', name: '龙岗区' },
          { code: '440308', name: '盐田区' },
          { code: '440309', name: '龙华区' },
          { code: '440310', name: '坪山区' },
          { code: '440311', name: '光明区' },
        ],
      },
    ],
  },
])

// 根据代码查找名称的辅助函数
const findNameByCode = (code, options) => {
  for (const option of options) {
    if (option.code === code) {
      return option.name
    }
    if (option.children) {
      const found = findNameByCode(code, option.children)
      if (found) return found
    }
  }
  return null
}

// 根据名称查找代码的辅助函数
const findCodeByName = (name, options) => {
  for (const option of options) {
    if (option.name === name) {
      return option.code
    }
    if (option.children) {
      const found = findCodeByName(name, option.children)
      if (found) return found
    }
  }
  return null
}

// 同步外部值到内部（支持字符串数组格式：['省份', '城市', '区县']）
watch(
  () => props.modelValue,
  (newVal) => {
    if (Array.isArray(newVal)) {
      if (newVal.length === 0) {
        regionValue.value = []
      } else if (typeof newVal[0] === 'string') {
        // 如果是字符串数组，转换为代码数组
        const codes = []
        let currentOptions = regionOptions.value
        for (const name of newVal) {
          const code = findCodeByName(name, currentOptions)
          if (code) {
            codes.push(code)
            const option = currentOptions.find((opt) => opt.code === code)
            currentOptions = option?.children || []
          } else {
            break
          }
        }
        regionValue.value = codes
      } else {
        // 如果是代码数组
        regionValue.value = newVal
      }
    } else {
      regionValue.value = []
    }
  },
  { immediate: true }
)

// 区域变化
const handleChange = (val) => {
  if (!val || val.length === 0) {
    emit('update:modelValue', [])
    emit('change', { province: '', city: '', district: '', codes: [] })
    return
  }

  // 获取选中的名称
  const names = []
  let currentOptions = regionOptions.value
  for (const code of val) {
    const name = findNameByCode(code, currentOptions)
    if (name) {
      names.push(name)
      const option = currentOptions.find((opt) => opt.code === code)
      currentOptions = option?.children || []
    }
  }

  emit('update:modelValue', names)
  emit('change', {
    province: names[0] || '',
    city: names[1] || '',
    district: names[2] || '',
    codes: val,
  })
}

onMounted(() => {
  // 可以在这里加载完整的省市区数据
  // 实际项目中应该从API获取或使用完整的数据文件
})
</script>

<style scoped>
.region-picker-container {
  width: 100%;
}
</style>
