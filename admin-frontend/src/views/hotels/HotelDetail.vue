<template>
  <div class="hotel-detail-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>酒店详情</span>
          <div>
            <el-button @click="handleBack">返回</el-button>
            <el-button type="primary" @click="handleEdit">编辑</el-button>
          </div>
        </div>
      </template>

      <div v-loading="loading" class="hotel-detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="酒店名称">
            {{ hotelData.name || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="星级">
            <span v-if="hotelData.starLevel">{{ hotelData.starLevel }}星</span>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="hotelData.status === 1 ? 'success' : 'info'">
              {{ hotelData.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            {{ hotelData.contactPhone || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="省市区" :span="2">
            {{ `${hotelData.province || ''}${hotelData.city || ''}${hotelData.district || ''}` }}
          </el-descriptions-item>
          <el-descriptions-item label="详细地址" :span="2">
            {{ hotelData.address || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="经度">
            {{ hotelData.longitude || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="纬度">
            {{ hotelData.latitude || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="设施" :span="2">
            <el-tag
              v-for="facility in hotelData.facilities"
              :key="facility"
              type="info"
              style="margin-right: 8px"
            >
              {{ facility }}
            </el-tag>
            <span v-if="!hotelData.facilities || hotelData.facilities.length === 0">-</span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDate(hotelData.createTime) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDate(hotelData.updateTime) || '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 酒店图片 -->
        <div v-if="hotelData.images && hotelData.images.length > 0" class="hotel-images-section">
          <el-divider content-position="left">酒店图片</el-divider>
          <div class="hotel-images-container">
            <el-image
              v-for="(imageUrl, index) in hotelData.images"
              :key="index"
              :src="imageUrl"
              fit="cover"
              class="hotel-image-item"
              :preview-src-list="hotelData.images"
              :initial-index="index"
            >
              <template #error>
                <div class="image-error-small">
                  <el-icon :size="20"><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </div>
        </div>

        <!-- 酒店描述 -->
        <div v-if="hotelData.description" class="hotel-description-section">
          <el-divider content-position="left">酒店描述</el-divider>
          <div class="hotel-description">
            <RichTextEditor
              :model-value="hotelData.description"
              :disabled="true"
              placeholder="暂无描述"
            />
          </div>
        </div>

        <!-- 房型列表 -->
        <div class="hotel-rooms-section">
          <el-divider content-position="left">房型列表</el-divider>
          <el-table
            v-loading="roomsLoading"
            :data="roomList"
            stripe
            border
            style="width: 100%"
          >
            <el-table-column prop="name" label="房型名称" min-width="150" />
            <el-table-column prop="price" label="价格" width="120">
              <template #default="{ row }">
                <span v-if="row.price">¥{{ row.price }}</span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="stock" label="库存" width="100">
              <template #default="{ row }">
                {{ row.stock !== null && row.stock !== undefined ? row.stock : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'">
                  {{ row.status === 1 ? '上架' : '下架' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.description || '-' }}
              </template>
            </el-table-column>
          </el-table>
          <div v-if="!roomsLoading && (!roomList || roomList.length === 0)" class="no-rooms">
            <el-empty description="暂无房型" :image-size="100" />
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { getHotelById, getHotelRooms } from '@/api/hotels'
import RichTextEditor from '@/components/RichTextEditor.vue'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const roomsLoading = ref(false)

// 酒店数据
const hotelData = reactive({
  id: null,
  name: '',
  starLevel: null,
  province: '',
  city: '',
  district: '',
  address: '',
  longitude: null,
  latitude: null,
  images: [],
  facilities: [],
  contactPhone: '',
  description: '',
  status: 0,
  createTime: null,
  updateTime: null,
})

// 房型列表
const roomList = ref([])

// 加载酒店详情
const loadDetail = async () => {
  loading.value = true
  try {
    const res = await getHotelById(route.params.id)
    if (res.data) {
      const data = res.data
      hotelData.id = data.id
      hotelData.name = data.name || ''
      hotelData.starLevel = data.starLevel || null
      hotelData.province = data.province || ''
      hotelData.city = data.city || ''
      hotelData.district = data.district || ''
      hotelData.address = data.address || ''
      hotelData.longitude = data.longitude || null
      hotelData.latitude = data.latitude || null
      hotelData.images = data.images || []
      hotelData.facilities = data.facilities || []
      hotelData.contactPhone = data.contactPhone || ''
      hotelData.description = data.description || ''
      hotelData.status = data.status !== undefined ? data.status : 0
      hotelData.createTime = data.createTime || null
      hotelData.updateTime = data.updateTime || null
    }
  } catch (error) {
    console.error('加载酒店详情失败:', error)
    ElMessage.error('加载酒店详情失败')
  } finally {
    loading.value = false
  }
}

// 加载房型列表
const loadRooms = async () => {
  roomsLoading.value = true
  try {
    const res = await getHotelRooms(route.params.id)
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

// 返回
const handleBack = () => {
  router.push('/hotels')
}

// 编辑
const handleEdit = () => {
  router.push(`/hotels/edit/${route.params.id}`)
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

onMounted(() => {
  loadDetail()
  loadRooms()
})
</script>

<style scoped>
.hotel-detail-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.hotel-detail-content {
  padding: 20px 0;
}

.hotel-images-section {
  margin: 20px 0;
}

.hotel-images-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 0 20px;
}

.hotel-image-item {
  width: 150px;
  height: 150px;
  border-radius: 4px;
  cursor: pointer;
  transition: transform 0.3s;
}

.hotel-image-item:hover {
  transform: scale(1.05);
}

.image-error-small {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background-color: #f5f7fa;
  color: #909399;
}

.hotel-description-section {
  margin: 20px 0;
}

.hotel-description {
  padding: 0 20px;
}

.hotel-rooms-section {
  margin: 20px 0;
}

.no-rooms {
  padding: 40px 0;
  text-align: center;
}
</style>
