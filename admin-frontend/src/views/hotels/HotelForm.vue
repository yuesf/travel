<template>
  <div class="hotel-form-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑酒店' : '创建酒店' }}</span>
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
        class="hotel-form"
      >
        <el-tabs v-model="activeTab" type="border-card">
          <!-- Tab 1: 基本信息 -->
          <el-tab-pane label="基本信息" name="basic">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="酒店名称" prop="name">
                  <el-input v-model="formData.name" placeholder="请输入酒店名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="星级" prop="starLevel">
                  <el-select v-model="formData.starLevel" placeholder="请选择星级" style="width: 100%">
                    <el-option label="1星" :value="1" />
                    <el-option label="2星" :value="2" />
                    <el-option label="3星" :value="3" />
                    <el-option label="4星" :value="4" />
                    <el-option label="5星" :value="5" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="省市区">
              <RegionPicker
                v-model="formData.region"
                placeholder="请选择省市区"
                @change="handleRegionChange"
              />
            </el-form-item>

            <el-form-item label="详细地址">
              <el-input
                v-model="formData.address"
                type="textarea"
                :rows="2"
                placeholder="请输入详细地址"
              />
            </el-form-item>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="经度">
                  <el-input
                    v-model="formData.longitude"
                    placeholder="请输入经度（可选，例如：116.397428）"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="纬度">
                  <el-input
                    v-model="formData.latitude"
                    placeholder="请输入纬度（可选，例如：39.90923）"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="联系电话">
                  <el-input v-model="formData.contactPhone" placeholder="请输入联系电话" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="状态" prop="status">
                  <el-radio-group v-model="formData.status">
                    <el-radio :value="1">上架</el-radio>
                    <el-radio :value="0">下架</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="设施">
              <el-input
                v-model="facilitiesText"
                type="textarea"
                :rows="3"
                placeholder="请输入设施，多个设施用逗号分隔，例如：WiFi,停车场,健身房,游泳池"
                @blur="handleFacilitiesChange"
              />
              <div class="form-tip">多个设施用逗号分隔</div>
            </el-form-item>

            <el-form-item label="简介">
              <RichTextEditor
                v-model="formData.description"
                placeholder="请输入酒店简介"
              />
            </el-form-item>
          </el-tab-pane>

          <!-- Tab 2: 媒体资源 -->
          <el-tab-pane label="媒体资源" name="media">
            <el-form-item label="酒店图片">
              <ImageUpload
                v-model="formData.images"
                :limit="9"
                :max-size="5"
                @change="handleImagesChange"
              />
              <div class="form-tip">最多上传9张图片，单张图片不超过5MB，支持JPG、PNG、WebP格式</div>
            </el-form-item>
          </el-tab-pane>

          <!-- Tab 3: 房型管理 -->
          <el-tab-pane label="房型管理" name="rooms">
            <div class="rooms-toolbar">
              <el-button type="primary" :icon="Plus" @click="handleAddRoom" :disabled="!isEdit">
                添加房型
              </el-button>
              <el-button :icon="Refresh" @click="loadRooms" :disabled="!isEdit">刷新</el-button>
            </div>

            <el-table
              :data="roomList"
              v-loading="roomsLoading"
              stripe
              border
              style="width: 100%; margin-top: 16px"
            >
              <el-table-column prop="roomType" label="房型名称" min-width="120" />
              <el-table-column prop="price" label="价格" width="120">
                <template #default="{ row }">
                  <span v-if="row.price">¥{{ row.price }}/晚</span>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column prop="stock" label="库存" width="100" />
              <el-table-column prop="bedType" label="床型" width="120" />
              <el-table-column prop="area" label="面积" width="100">
                <template #default="{ row }">
                  <span v-if="row.area">{{ row.area }}㎡</span>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'info'">
                    {{ row.status === 1 ? '上架' : '下架' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" size="small" @click="handleEditRoom(row)">
                    编辑
                  </el-button>
                  <el-button
                    link
                    type="danger"
                    size="small"
                    @click="handleDeleteRoom(row)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <div v-if="!isEdit" class="rooms-tip">
              <el-alert
                title="提示"
                type="info"
                :closable="false"
                show-icon
              >
                <template #default>
                  请先保存酒店基本信息，然后才能添加房型
                </template>
              </el-alert>
            </div>
          </el-tab-pane>

          <!-- Tab 4: 同步设置 -->
          <el-tab-pane label="同步设置" name="sync">
            <el-form-item label="同步来源">
              <el-input v-model="formData.syncSource" placeholder="请输入同步来源" disabled />
              <div class="form-tip">同步来源由系统自动设置</div>
            </el-form-item>
            <el-form-item label="同步时间">
              <el-input
                v-model="syncTimeText"
                placeholder="未同步"
                disabled
              />
            </el-form-item>
          </el-tab-pane>
        </el-tabs>
      </el-form>
    </el-card>

    <!-- 房型表单对话框 -->
    <RoomForm
      v-model="roomFormVisible"
      :room-data="currentRoomData"
      :hotel-id="hotelId"
      @submit="handleRoomSubmit"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onErrorCaptured } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import ImageUpload from '@/components/ImageUpload.vue'
import RichTextEditor from '@/components/RichTextEditor.vue'
import RegionPicker from '@/components/RegionPicker.vue'
import RoomForm from './RoomForm.vue'
import {
  getHotelById,
  createHotel,
  updateHotel,
  getHotelRooms,
  createHotelRoom,
  updateHotelRoom,
  deleteHotelRoom,
} from '@/api/hotels'

const router = useRouter()
const route = useRoute()

const formRef = ref(null)
const activeTab = ref('basic')
const submitting = ref(false)

const isEdit = computed(() => !!route.params.id)
const hotelId = computed(() => (isEdit.value ? parseInt(route.params.id) : null))

// 设施文本（用于输入）
const facilitiesText = ref('')

// 房型相关
const roomList = ref([])
const roomsLoading = ref(false)
const roomFormVisible = ref(false)
const currentRoomData = ref(null)

// 表单数据
const formData = reactive({
  name: '',
  province: '',
  city: '',
  district: '',
  region: [], // 省市区数组，用于RegionPicker
  address: '',
  starLevel: null,
  description: '',
  images: [],
  facilities: [],
  contactPhone: '',
  longitude: null,
  latitude: null,
  status: 1, // 默认上架
  syncSource: '',
})

// 同步时间文本
const syncTimeText = ref('')

// 表单验证规则
const formRules = {
  name: [{ required: true, message: '请输入酒店名称', trigger: 'blur' }],
  starLevel: [{ required: true, message: '请选择星级', trigger: 'change' }],
}

// 区域变化
const handleRegionChange = (regionData) => {
  formData.province = regionData.province || ''
  formData.city = regionData.city || ''
  formData.district = regionData.district || ''
}

// 处理设施变化
const handleFacilitiesChange = () => {
  if (facilitiesText.value.trim()) {
    formData.facilities = facilitiesText.value
      .split(',')
      .map((item) => item.trim())
      .filter((item) => item)
  } else {
    formData.facilities = []
  }
}

// 图片变化
const handleImagesChange = (urls) => {
  formData.images = urls
}

// 加载详情数据
const loadDetail = async () => {
  if (!isEdit.value) return

  try {
    const res = await getHotelById(route.params.id)
    if (res.data) {
      const data = res.data
      formData.name = data.name || ''
      formData.province = data.province || ''
      formData.city = data.city || ''
      formData.district = data.district || ''
      // 设置省市区数组，确保是有效的字符串数组
      if (data.province || data.city || data.district) {
        formData.region = [data.province, data.city, data.district]
          .filter(Boolean)
          .filter(item => typeof item === 'string' && item.trim() !== '')
      } else {
        formData.region = []
      }
      formData.address = data.address || ''
      formData.starLevel = data.starLevel || null
      formData.description = data.description || ''
      formData.images = Array.isArray(data.images) ? data.images : []
      formData.facilities = Array.isArray(data.facilities) ? data.facilities : []
      formData.contactPhone = data.contactPhone || ''
      formData.longitude = data.longitude ? parseFloat(data.longitude) : null
      formData.latitude = data.latitude ? parseFloat(data.latitude) : null
      formData.status = data.status !== undefined ? data.status : 1
      formData.syncSource = data.syncSource || ''

      // 初始化设施文本
      facilitiesText.value = Array.isArray(formData.facilities)
        ? formData.facilities.join(',')
        : ''

      // 同步时间
      if (data.syncTime) {
        const date = new Date(data.syncTime)
        syncTimeText.value = date.toLocaleString('zh-CN')
      }

      // 加载房型列表
      loadRooms()
    }
  } catch (error) {
    console.error('加载酒店详情失败:', error)
    ElMessage.error('加载酒店详情失败')
  }
}

// 加载房型列表
const loadRooms = async () => {
  if (!isEdit.value) return

  roomsLoading.value = true
  try {
    const res = await getHotelRooms(hotelId.value)
    if (res.data) {
      roomList.value = res.data || []
    }
  } catch (error) {
    console.error('加载房型列表失败:', error)
    ElMessage.error('加载房型列表失败')
    roomList.value = []
  } finally {
    roomsLoading.value = false
  }
}

// 添加房型
const handleAddRoom = () => {
  currentRoomData.value = null
  roomFormVisible.value = true
}

// 编辑房型
const handleEditRoom = (row) => {
  currentRoomData.value = { ...row }
  roomFormVisible.value = true
}

// 删除房型
const handleDeleteRoom = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除房型"${row.roomType}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    roomsLoading.value = true
    try {
      await deleteHotelRoom(row.id)
      ElMessage.success('删除成功')
      loadRooms()
    } catch (error) {
      console.error('删除房型失败:', error)
      ElMessage.error('删除房型失败')
    } finally {
      roomsLoading.value = false
    }
  } catch (error) {
    // 用户取消
  }
}

// 房型表单提交
const handleRoomSubmit = async (roomData) => {
  try {
    if (roomData.id) {
      // 更新房型
      await updateHotelRoom(roomData.id, roomData)
      ElMessage.success('更新房型成功')
    } else {
      // 创建房型
      await createHotelRoom(roomData)
      ElMessage.success('添加房型成功')
    }
    loadRooms()
  } catch (error) {
    console.error('保存房型失败:', error)
    ElMessage.error(error.message || '保存房型失败')
    throw error
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      // 处理设施
      handleFacilitiesChange()

      submitting.value = true
      try {
        // 处理经纬度：如果是字符串，转换为数字；如果为空，设为 undefined
        const longitude = formData.longitude
          ? (typeof formData.longitude === 'string' ? parseFloat(formData.longitude) : formData.longitude)
          : undefined
        const latitude = formData.latitude
          ? (typeof formData.latitude === 'string' ? parseFloat(formData.latitude) : formData.latitude)
          : undefined

        const submitData = {
          name: formData.name,
          province: formData.province || undefined,
          city: formData.city || undefined,
          district: formData.district || undefined,
          address: formData.address || undefined,
          starLevel: formData.starLevel,
          description: formData.description || undefined,
          images: Array.isArray(formData.images) ? formData.images : [],
          facilities: formData.facilities.length > 0 ? formData.facilities : undefined,
          contactPhone: formData.contactPhone || undefined,
          longitude: longitude,
          latitude: latitude,
          status: formData.status,
        }

        if (isEdit.value) {
          await updateHotel(route.params.id, submitData)
          ElMessage.success('更新成功')
        } else {
          const res = await createHotel(submitData)
          ElMessage.success('创建成功')
          // 创建成功后跳转到编辑页面
          if (res.data && res.data.id) {
            router.push(`/hotels/edit/${res.data.id}`)
          } else {
            // 如果返回数据异常，跳转到列表页
            console.warn('创建成功但返回数据异常:', res)
            router.push('/hotels')
          }
          return
        }

        router.push('/hotels')
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
      router.push('/hotels')
    })
    .catch(() => {
      // 用户取消
    })
}

// 错误捕获，防止子组件错误导致整个页面崩溃
onErrorCaptured((err, instance, info) => {
  console.error('组件错误:', err, info, err.stack)
  // 只在开发环境或关键错误时显示提示
  if (process.env.NODE_ENV === 'development' || err.message?.includes('Cannot read')) {
    ElMessage.error(`页面组件加载失败: ${err.message || '未知错误'}`)
  }
  // 返回false阻止错误继续传播，但允许页面继续渲染
  return false
})

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.hotel-form-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.hotel-form {
  padding: 20px 0;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.rooms-toolbar {
  display: flex;
  gap: 8px;
}

.rooms-tip {
  margin-top: 16px;
}

:deep(.el-tabs__content) {
  padding: 20px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}
</style>
