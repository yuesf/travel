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

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="景区评级">
                  <el-select v-model="formData.rating" placeholder="请选择景区评级" clearable>
                    <el-option label="1A景区" value="1A" />
                    <el-option label="2A景区" value="2A" />
                    <el-option label="3A景区" value="3A" />
                    <el-option label="4A景区" value="4A" />
                    <el-option label="5A景区" value="5A" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="景区标签">
              <el-select
                v-model="formData.tags"
                multiple
                filterable
                allow-create
                default-first-option
                placeholder="请选择或输入标签"
                style="width: 100%"
              >
                <el-option
                  v-for="tag in commonTags"
                  :key="tag"
                  :label="tag"
                  :value="tag"
                />
              </el-select>
              <div class="form-tip">可输入自定义标签，按回车确认</div>
            </el-form-item>

            <el-form-item label="省市区" prop="region">
              <RegionPicker
                v-model="formData.region"
                placeholder="请选择省市区"
                @change="handleRegionChange"
              />
            </el-form-item>

            <el-form-item label="详细地址" prop="address">
              <el-input
                v-model="formData.address"
                type="textarea"
                :rows="3"
                placeholder="请输入详细地址"
              />
            </el-form-item>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="经度">
                  <el-input
                    v-model="formData.longitude"
                    placeholder="请输入经度（可选，-180到180）"
                    type="number"
                  />
                  <div class="form-tip">例如：116.397428</div>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="纬度">
                  <el-input
                    v-model="formData.latitude"
                    placeholder="请输入纬度（可选，-90到90）"
                    type="number"
                  />
                  <div class="form-tip">例如：39.90923</div>
                </el-form-item>
              </el-col>
            </el-row>

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
                    :minute-step="30"
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

            <el-form-item label="入园须知">
              <RichTextEditor
                v-model="formData.admissionNotice"
                placeholder="请输入入园须知内容"
              />
            </el-form-item>

            <el-form-item label="入园须知链接">
              <el-input
                v-model="formData.admissionNoticeUrl"
                placeholder="请输入入园须知链接（可选）"
              />
              <div class="form-tip">如果填写了链接，小程序端将显示链接入口</div>
            </el-form-item>
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
                @change="handleVideoChange"
              />
              <div class="form-tip">请从文件库选择视频，文件库支持上传图片和视频</div>
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

          <!-- Tab 5: 可订日期管理 -->
          <el-tab-pane label="可订日期" name="booking-dates" v-if="isEdit">
            <BookingDateManager :attraction-id="route.params.id" />
          </el-tab-pane>

          <!-- Tab 6: 票种分类 -->
          <el-tab-pane label="票种分类" name="ticket-categories" v-if="isEdit">
            <TicketCategoryManager :attraction-id="route.params.id" />
          </el-tab-pane>

          <!-- Tab 7: 具体票种 -->
          <el-tab-pane label="具体票种" name="tickets" v-if="isEdit">
            <TicketManager :attraction-id="route.params.id" />
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
import RegionPicker from '@/components/RegionPicker.vue'
import TimePicker from '@/components/TimePicker.vue'
import BookingDateManager from '@/components/BookingDateManager.vue'
import TicketCategoryManager from '@/components/TicketCategoryManager.vue'
import TicketManager from '@/components/TicketManager.vue'
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
  ticketPrice: null,
  ticketStock: null,
  validPeriod: '',
  status: 1, // 默认上架
  rating: '', // 景区评级
  tags: [], // 景区标签
  admissionNotice: '', // 入园须知
  admissionNoticeUrl: '', // 入园须知链接
  goldenSummitEnabled: 0, // 是否启用金顶预约
})

// 常用标签
const commonTags = [
  '自然峡谷',
  '天地之道',
  '历史文化',
  '自然风光',
  '人文景观',
  '休闲度假',
  '亲子游',
  '摄影圣地',
]

// 表单验证规则
const formRules = {
  name: [{ required: true, message: '请输入景点名称', trigger: 'blur' }],
  // 门票价格和库存改为可选，因为现在通过可订日期和票种来管理
  ticketPrice: [],
  ticketStock: [],
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
      formData.ticketPrice = data.ticketPrice ? parseFloat(data.ticketPrice) : null
      formData.ticketStock = data.ticketStock || null
      formData.validPeriod = data.validPeriod || ''
      formData.status = data.status !== undefined ? data.status : 1
      formData.rating = data.rating || ''
      formData.tags = data.tags || []
      formData.admissionNotice = data.admissionNotice || ''
      formData.admissionNoticeUrl = data.admissionNoticeUrl || ''
      formData.goldenSummitEnabled = data.goldenSummitEnabled !== undefined ? data.goldenSummitEnabled : 0
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


// 视频变化
const handleVideoChange = (url) => {
  formData.videoUrl = url || ''
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
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
          const { region, ...restData } = formData
          const submitData = {
            ...restData,
            images: Array.isArray(formData.images) ? formData.images : [],
            openTime: openTimeStr,
            // 确保经纬度为数字类型
            longitude: formData.longitude ? parseFloat(formData.longitude) : null,
            latitude: formData.latitude ? parseFloat(formData.latitude) : null,
            // 门票价格和库存改为可选，如果为空则不发送
            ticketPrice: formData.ticketPrice || null,
            ticketStock: formData.ticketStock || null,
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
          ElMessage.error(error.message || error.response?.data?.message || '保存失败')
        } finally {
          submitting.value = false
        }
      } else {
        // 验证失败，显示提示
        ElMessage.warning('请完善表单信息')
        return false
      }
    })
  } catch (error) {
    // validate方法本身可能抛出错误
    console.error('表单验证错误:', error)
    ElMessage.warning('表单验证失败，请检查必填项')
  }
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
