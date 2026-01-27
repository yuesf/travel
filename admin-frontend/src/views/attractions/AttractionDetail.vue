<template>
  <div class="attraction-detail-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>景点详情</span>
          <div>
            <el-button @click="handleBack">返回</el-button>
            <el-button type="primary" @click="handleEdit">编辑</el-button>
          </div>
        </div>
      </template>

      <div v-loading="loading" class="attraction-detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="景点名称">
            {{ attractionData.name || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="位置">
            {{ attractionData.location || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="景区评级">
            {{ attractionData.rating || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="attractionData.status === 1 ? 'success' : 'info'">
              {{ attractionData.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="标签" :span="2">
            <el-tag
              v-for="tag in attractionData.tags"
              :key="tag"
              type="info"
              style="margin-right: 8px"
            >
              {{ tag }}
            </el-tag>
            <span v-if="!attractionData.tags || attractionData.tags.length === 0">-</span>
          </el-descriptions-item>
          <el-descriptions-item label="省市区" :span="2">
            {{ `${attractionData.province || ''}${attractionData.city || ''}${attractionData.district || ''}` }}
          </el-descriptions-item>
          <el-descriptions-item label="详细地址" :span="2">
            {{ attractionData.address || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="经度">
            {{ attractionData.longitude || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="纬度">
            {{ attractionData.latitude || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="门票价格">
            <span v-if="attractionData.ticketPrice">¥{{ attractionData.ticketPrice }}</span>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="库存">
            {{ attractionData.ticketStock !== null && attractionData.ticketStock !== undefined ? attractionData.ticketStock : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="有效期">
            {{ attractionData.validPeriod || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            {{ attractionData.contactPhone || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="开放时间" :span="2">
            {{ attractionData.openTime || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDate(attractionData.createTime) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDate(attractionData.updateTime) || '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 景点图片 -->
        <div v-if="attractionData.images && attractionData.images.length > 0" class="attraction-images-section">
          <el-divider content-position="left">景点图片</el-divider>
          <div class="attraction-images-container">
            <el-image
              v-for="(imageUrl, index) in attractionData.images"
              :key="index"
              :src="imageUrl"
              fit="cover"
              class="attraction-image-item"
              :preview-src-list="attractionData.images"
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

        <!-- 景点描述 -->
        <div v-if="attractionData.description" class="attraction-description-section">
          <el-divider content-position="left">景点描述</el-divider>
          <div class="attraction-description">
            <RichTextEditor
              :model-value="attractionData.description"
              :disabled="true"
              placeholder="暂无描述"
            />
          </div>
        </div>

        <!-- 视频 -->
        <div v-if="attractionData.videoUrl" class="attraction-video-section">
          <el-divider content-position="left">景点视频</el-divider>
          <div class="attraction-video">
            <video
              :src="attractionData.videoUrl"
              controls
              style="max-width: 100%; max-height: 500px"
            >
              您的浏览器不支持视频播放
            </video>
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
import { getAttractionById } from '@/api/attractions'
import RichTextEditor from '@/components/RichTextEditor.vue'

const router = useRouter()
const route = useRoute()

const loading = ref(false)

// 景点数据
const attractionData = reactive({
  id: null,
  name: '',
  location: '',
  rating: '',
  tags: [],
  province: '',
  city: '',
  district: '',
  address: '',
  longitude: null,
  latitude: null,
  images: [],
  description: '',
  videoUrl: '',
  ticketPrice: null,
  ticketStock: null,
  validPeriod: '',
  openTime: '',
  contactPhone: '',
  status: 0,
  createTime: null,
  updateTime: null,
})

// 加载景点详情
const loadDetail = async () => {
  loading.value = true
  try {
    const res = await getAttractionById(route.params.id)
    if (res.data) {
      const data = res.data
      attractionData.id = data.id
      attractionData.name = data.name || ''
      attractionData.location = data.location || ''
      attractionData.rating = data.rating || ''
      attractionData.tags = data.tags || []
      attractionData.province = data.province || ''
      attractionData.city = data.city || ''
      attractionData.district = data.district || ''
      attractionData.address = data.address || ''
      attractionData.longitude = data.longitude || null
      attractionData.latitude = data.latitude || null
      attractionData.images = data.images || []
      attractionData.description = data.description || ''
      attractionData.videoUrl = data.videoUrl || ''
      attractionData.ticketPrice = data.ticketPrice ? parseFloat(data.ticketPrice) : null
      attractionData.ticketStock = data.ticketStock !== undefined ? data.ticketStock : null
      attractionData.validPeriod = data.validPeriod || ''
      attractionData.openTime = data.openTime || ''
      attractionData.contactPhone = data.contactPhone || ''
      attractionData.status = data.status !== undefined ? data.status : 0
      attractionData.createTime = data.createTime || null
      attractionData.updateTime = data.updateTime || null
    }
  } catch (error) {
    console.error('加载景点详情失败:', error)
    ElMessage.error('加载景点详情失败')
  } finally {
    loading.value = false
  }
}

// 返回
const handleBack = () => {
  router.push('/attractions')
}

// 编辑
const handleEdit = () => {
  router.push(`/attractions/edit/${route.params.id}`)
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
})
</script>

<style scoped>
.attraction-detail-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.attraction-detail-content {
  padding: 20px 0;
}

.attraction-images-section {
  margin: 20px 0;
}

.attraction-images-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 0 20px;
}

.attraction-image-item {
  width: 150px;
  height: 150px;
  border-radius: 4px;
  cursor: pointer;
  transition: transform 0.3s;
}

.attraction-image-item:hover {
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

.attraction-description-section {
  margin: 20px 0;
}

.attraction-description {
  padding: 0 20px;
}

.attraction-video-section {
  margin: 20px 0;
}

.attraction-video {
  padding: 0 20px;
  display: flex;
  justify-content: center;
}
</style>
