<template>
  <div class="attraction-form-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑景点' : '创建景点' }}</span>
          <div>
            <el-button @click="handleCancel">取消</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="submitting">
              保存
            </el-button>
          </div>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        class="attraction-form"
      >
        <el-tabs v-model="activeTab" type="border-card">
          <!-- Tab 1: 基本信息 -->
          <el-tab-pane label="基本信息" name="basic">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="景点名称" prop="name">
                  <el-input v-model="formData.name" placeholder="请输入景点名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="位置" prop="location">
                  <el-input v-model="formData.location" placeholder="请输入位置" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="省市区" prop="region">
              <RegionPicker
                v-model="formData.region"
                placeholder="请选择省市区"
                @change="handleRegionChange"
              />
            </el-form-item>

            <el-form-item label="地图位置" prop="location">
              <MapPicker
                v-model="formData.mapLocation"
                :height="'400px'"
                @change="handleMapChange"
              />
              <div class="form-tip">点击地图选择位置，或手动输入经纬度</div>
            </el-form-item>

            <el-form-item label="简介" prop="description">
              <RichTextEditor
                v-model="formData.description"
                placeholder="请输入景点简介"
              />
            </el-form-item>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="开放时间" prop="openTime">
                  <TimePicker
                    v-model="formData.openTime"
                    :is-range="true"
                    placeholder="请选择开放时间"
                    start-placeholder="开始时间"
                    end-placeholder="结束时间"
                  />
                  <div class="form-tip">例如：09:00-18:00</div>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="联系电话" prop="contactPhone">
                  <el-input v-model="formData.contactPhone" placeholder="请输入联系电话" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-tab-pane>

          <!-- Tab 2: 媒体资源 -->
          <el-tab-pane label="媒体资源" name="media">
            <el-form-item label="景点图片">
              <ImageUpload
                v-model="formData.images"
                :limit="9"
                :max-size="5"
                @change="handleImagesChange"
              />
              <div class="form-tip">最多上传9张图片，单张图片不超过5MB，支持JPG、PNG、WebP格式</div>
            </el-form-item>

            <el-form-item label="景点视频" prop="videoUrl">
              <VideoUpload
                v-model="formData.videoUrl"
                :max-size="50"
                @change="handleVideoChange"
              />
              <div class="form-tip">支持MP4格式的视频，文件大小不超过50MB</div>
            </el-form-item>
          </el-tab-pane>

          <!-- Tab 3: 门票信息 -->
          <el-tab-pane label="门票信息" name="ticket">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="门票价格" prop="ticketPrice">
                  <el-input-number
                    v-model="formData.ticketPrice"
                    :precision="2"
                    :step="0.01"
                    :min="0"
                    placeholder="请输入门票价格"
                    style="width: 100%"
                  />
                  <span class="form-unit">元</span>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="门票库存" prop="ticketStock">
                  <el-input-number
                    v-model="formData.ticketStock"
                    :min="0"
                    placeholder="请输入门票库存"
                    style="width: 100%"
                  />
                  <span class="form-unit">张</span>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="有效期" prop="validPeriod">
              <el-input v-model="formData.validPeriod" placeholder="请输入有效期，如：7天" />
              <div class="form-tip">例如：7天、30天、永久有效</div>
            </el-form-item>
          </el-tab-pane>

          <!-- Tab 4: 状态设置 -->
          <el-tab-pane label="状态设置" name="status">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio :value="1">上架</el-radio>
                <el-radio :value="0">下架</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-tab-pane>
        </el-tabs>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImageUpload from '@/components/ImageUpload.vue'
import VideoUpload from '@/components/VideoUpload.vue'
import RichTextEditor from '@/components/RichTextEditor.vue'
import MapPicker from '@/components/MapPicker.vue'
import RegionPicker from '@/components/RegionPicker.vue'
import TimePicker from '@/components/TimePicker.vue'
import { getAttractionById, createAttraction, updateAttraction } from '@/api/attractions'

const router = useRouter()
const route = useRoute()

const formRef = ref(null)
const activeTab = ref('basic')
const submitting = ref(false)

const isEdit = computed(() => !!route.params.id)

// 表单数据
const formData = reactive({
  name: '',
  location: '',
  province: '',
  city: '',
  district: '',
  region: [], // 省市区数组，用于RegionPicker
  address: '',
  description: '',
  images: [],
  videoUrl: '',
  openTime: null, // 开放时间，时间范围数组
  contactPhone: '',
  longitude: null,
  latitude: null,
  mapLocation: { // 地图位置对象，用于MapPicker
    longitude: null,
    latitude: null,
    address: '',
  },
  ticketPrice: null,
  ticketStock: null,
  validPeriod: '',
  status: 1, // 默认上架
})

// 表单验证规则
const formRules = {
  name: [{ required: true, message: '请输入景点名称', trigger: 'blur' }],
  ticketPrice: [{ required: true, message: '请输入门票价格', trigger: 'blur' }],
  ticketStock: [{ required: true, message: '请输入门票库存', trigger: 'blur' }],
}

// 加载详情数据
const loadDetail = async () => {
  if (!isEdit.value) return

  try {
    const res = await getAttractionById(route.params.id)
    if (res.data) {
      const data = res.data
      formData.name = data.name || ''
      formData.location = data.location || ''
      formData.province = data.province || ''
      formData.city = data.city || ''
      formData.district = data.district || ''
      // 设置省市区数组
      if (data.province || data.city || data.district) {
        formData.region = [data.province, data.city, data.district].filter(Boolean)
      } else {
        formData.region = []
      }
      formData.address = data.address || ''
      formData.description = data.description || ''
      formData.images = data.images || []
      formData.videoUrl = data.videoUrl || ''
      // 解析开放时间（假设格式为 "09:00-18:00"）
      if (data.openTime && typeof data.openTime === 'string' && data.openTime.includes('-')) {
        const [start, end] = data.openTime.split('-').map((t) => t.trim())
        formData.openTime = [start, end]
      } else {
        formData.openTime = null
      }
      formData.contactPhone = data.contactPhone || ''
      formData.longitude = data.longitude ? parseFloat(data.longitude) : null
      formData.latitude = data.latitude ? parseFloat(data.latitude) : null
      // 设置地图位置
      formData.mapLocation = {
        longitude: formData.longitude,
        latitude: formData.latitude,
        address: formData.address || '',
      }
      formData.ticketPrice = data.ticketPrice ? parseFloat(data.ticketPrice) : null
      formData.ticketStock = data.ticketStock || null
      formData.validPeriod = data.validPeriod || ''
      formData.status = data.status !== undefined ? data.status : 1
    }
  } catch (error) {
    console.error('加载景点详情失败:', error)
    ElMessage.error('加载景点详情失败')
  }
}

// 图片变化
const handleImagesChange = (urls) => {
  formData.images = urls
}

// 区域变化
const handleRegionChange = (regionData) => {
  formData.province = regionData.province || ''
  formData.city = regionData.city || ''
  formData.district = regionData.district || ''
}

// 地图位置变化
const handleMapChange = (mapData) => {
  formData.longitude = mapData.longitude || null
  formData.latitude = mapData.latitude || null
  // 当地图获取到地址时，自动填写到详细地址字段
  if (mapData.address) {
    formData.address = mapData.address
  }
  // 同步更新地图位置对象
  formData.mapLocation = {
    longitude: formData.longitude,
    latitude: formData.latitude,
    address: mapData.address || formData.address || '',
  }
}

// 视频变化
const handleVideoChange = (url) => {
  formData.videoUrl = url || ''
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        // 格式化开放时间
        let openTimeStr = ''
        if (Array.isArray(formData.openTime) && formData.openTime.length === 2) {
          openTimeStr = `${formData.openTime[0]}-${formData.openTime[1]}`
        } else if (formData.openTime) {
          openTimeStr = String(formData.openTime)
        }

        // 构建提交数据，排除内部使用的字段
        const { region, mapLocation, ...restData } = formData
        const submitData = {
          ...restData,
          images: Array.isArray(formData.images) ? formData.images : [],
          openTime: openTimeStr,
        }

        if (isEdit.value) {
          await updateAttraction(route.params.id, submitData)
          ElMessage.success('更新成功')
        } else {
          await createAttraction(submitData)
          ElMessage.success('创建成功')
        }

        router.push('/attractions')
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error(error.message || '保存失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 取消
const handleCancel = () => {
  ElMessageBox.confirm('确定要取消吗？未保存的数据将丢失。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      router.push('/attractions')
    })
    .catch(() => {
      // 用户取消
    })
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.attraction-form-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.attraction-form {
  padding: 20px 0;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.form-unit {
  margin-left: 8px;
  color: #909399;
}

:deep(.el-tabs__content) {
  padding: 20px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}
</style>
