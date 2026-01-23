<template>
  <el-dialog
    v-model="visible"
    :title="mapId ? '编辑地图' : '添加地图'"
    width="800px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
      <el-form-item label="地图名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入地图名称" />
      </el-form-item>

      <el-form-item label="地图位置" prop="longitude">
        <MapPicker
          v-model="locationData"
          @change="handleLocationChange"
        />
        <div style="margin-top: 8px; color: #909399; font-size: 12px;">
          <div v-if="formData.address">
            <span style="color: #606266;">已选择地址：</span>{{ formData.address }}
          </div>
          <div v-else style="color: #c0c4cc;">请在地图上点击选择位置，系统将自动获取地址</div>
        </div>
      </el-form-item>

      <el-form-item label="公告内容">
        <el-input
          v-model="formData.announcement"
          type="textarea"
          :rows="4"
          placeholder="请输入公告内容（可选）"
        />
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :label="1">启用</el-radio>
          <el-radio :label="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getMapById, createMap, updateMap } from '@/api/maps'
import MapPicker from '@/components/MapPicker.vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  mapId: {
    type: Number,
    default: null,
  },
})

const emit = defineEmits(['update:modelValue', 'success'])

const formRef = ref(null)
const submitting = ref(false)
const visible = ref(false)

const formData = reactive({
  name: '',
  longitude: null,
  latitude: null,
  address: '',
  announcement: '',
  status: 1,
})

const locationData = ref({
  longitude: null,
  latitude: null,
  address: '',
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入地图名称', trigger: 'blur' },
  ],
  longitude: [
    {
      validator: (rule, value, callback) => {
        if (!formData.longitude || !formData.latitude) {
          callback(new Error('请选择地图位置'))
        } else {
          callback()
        }
      },
      trigger: 'change',
    },
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' },
  ],
}

// 监听 visible 变化
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    if (props.mapId) {
      loadMapData()
    } else {
      resetForm()
    }
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 加载地图数据
const loadMapData = async () => {
  if (!props.mapId) {
    resetForm()
    return
  }

  try {
    const res = await getMapById(props.mapId)
    if (res.data) {
      const map = res.data
      formData.name = map.name || ''
      formData.longitude = map.longitude
      formData.latitude = map.latitude
      formData.address = map.address || ''
      formData.announcement = map.announcement || ''
      formData.status = map.status !== undefined ? map.status : 1

      locationData.value = {
        longitude: map.longitude,
        latitude: map.latitude,
        address: map.address || '',
      }
      
      // 触发验证更新
      if (formRef.value) {
        formRef.value.validateField('longitude')
      }
    }
  } catch (error) {
    console.error('加载地图数据失败:', error)
    ElMessage.error('加载地图数据失败')
  }
}

// 重置表单
const resetForm = () => {
  formData.name = ''
  formData.longitude = null
  formData.latitude = null
  formData.address = ''
  formData.announcement = ''
  formData.status = 1

  locationData.value = {
    longitude: null,
    latitude: null,
    address: '',
  }

  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 位置变化
const handleLocationChange = (data) => {
  // 更新经纬度
  if (data.longitude !== undefined && data.longitude !== null) {
    formData.longitude = data.longitude
  }
  if (data.latitude !== undefined && data.latitude !== null) {
    formData.latitude = data.latitude
  }
  // 自动从地图获取地址（可能异步获取，所以每次 change 都更新）
  if (data.address !== undefined && data.address !== null && data.address !== '') {
    formData.address = data.address
  }
  // 同步 locationData，确保双向绑定
  locationData.value = {
    longitude: formData.longitude,
    latitude: formData.latitude,
    address: formData.address || '',
  }
  // 触发验证更新 - 验证 longitude 字段
  if (formRef.value) {
    formRef.value.validateField('longitude')
  }
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    if (!formData.longitude || !formData.latitude) {
      ElMessage.error('请选择地图位置')
      return
    }

    submitting.value = true

    const submitData = {
      name: formData.name,
      longitude: formData.longitude,
      latitude: formData.latitude,
      address: formData.address || undefined,
      announcement: formData.announcement || undefined,
      status: formData.status,
    }

    if (props.mapId) {
      await updateMap(props.mapId, submitData)
      ElMessage.success('更新成功')
    } else {
      await createMap(submitData)
      ElMessage.success('创建成功')
    }

    emit('success')
    handleClose()
  } catch (error) {
    if (error !== false) {
      console.error('提交失败:', error)
      ElMessage.error('提交失败')
    }
  } finally {
    submitting.value = false
  }
}

// 关闭
const handleClose = () => {
  visible.value = false
  resetForm()
}
</script>

<style scoped>
</style>
