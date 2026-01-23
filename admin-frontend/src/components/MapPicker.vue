<template>
  <div class="map-picker-container">
    <el-row :gutter="20">
      <el-col :span="16">
        <div ref="mapContainer" class="map-container"></div>
      </el-col>
      <el-col :span="8">
        <div class="map-info">
          <el-form :model="formData" label-width="80px">
            <el-form-item label="经度">
              <el-input
                v-model="longitudeInput"
                placeholder="请输入经度（-180 到 180）"
                clearable
                @blur="handleLongitudeBlur"
                @keyup.enter="handleLongitudeBlur"
              />
              <div style="font-size: 12px; color: #909399; margin-top: 4px;">
                示例：116.397428
              </div>
            </el-form-item>
            <el-form-item label="纬度">
              <el-input
                v-model="latitudeInput"
                placeholder="请输入纬度（-90 到 90）"
                clearable
                @blur="handleLatitudeBlur"
                @keyup.enter="handleLatitudeBlur"
              />
              <div style="font-size: 12px; color: #909399; margin-top: 4px;">
                示例：39.90923
              </div>
            </el-form-item>
            <el-form-item label="搜索地址">
              <el-input
                v-model="searchAddress"
                placeholder="请输入地址进行搜索（支持回车搜索）"
                clearable
                @keyup.enter="handleSearch"
              />
            </el-form-item>
            <el-form-item label="详细地址">
              <el-input
                v-model="formData.address"
                type="textarea"
                :rows="3"
                placeholder="详细地址（可手动编辑）"
                @blur="handleAddressChange"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索地址</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { getAMapApiKey } from '@/config/map'

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({
      longitude: null,
      latitude: null,
      address: '',
    }),
  },
  apiKey: {
    type: String,
    default: () => getAMapApiKey(), // 高德地图API Key，优先使用传入的，否则使用配置的
  },
  height: {
    type: String,
    default: '400px',
  },
})

const emit = defineEmits(['update:modelValue', 'change'])

const mapContainer = ref(null)
const searchAddress = ref('')
const longitudeInput = ref('')
const latitudeInput = ref('')
const formData = ref({
  longitude: null,
  latitude: null,
  address: '',
})
let map = null
let marker = null
let geocoder = null

// 加载高德地图脚本
const loadAMapScript = () => {
  return new Promise((resolve, reject) => {
    if (window.AMap) {
      resolve(window.AMap)
      return
    }

    const apiKey = props.apiKey || getAMapApiKey()
    
    if (!apiKey || apiKey === 'YOUR_API_KEY') {
      reject(new Error('请配置高德地图API Key'))
      return
    }

    const script = document.createElement('script')
    script.type = 'text/javascript'
    script.async = true
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${apiKey}&callback=initAMap`
    
    // 设置超时处理
    const timeout = setTimeout(() => {
      reject(new Error('加载高德地图超时，请检查网络连接'))
    }, 10000)
    
    window.initAMap = () => {
      clearTimeout(timeout)
      resolve(window.AMap)
      delete window.initAMap
    }
    
    script.onerror = () => {
      clearTimeout(timeout)
      reject(new Error('加载高德地图失败，请检查网络连接或API Key配置'))
    }
    
    document.head.appendChild(script)
  })
}

// 初始化地图
const initMap = async () => {
  if (!mapContainer.value) {
    // 如果容器还未准备好，延迟重试
    setTimeout(() => {
      initMap()
    }, 100)
    return
  }

  try {
    const apiKey = props.apiKey || getAMapApiKey()
    
    // 如果没有API Key，显示提示
    if (!apiKey || apiKey === 'YOUR_API_KEY') {
      ElMessage.warning('请配置高德地图API Key，否则地图功能无法使用')
      if (mapContainer.value) {
        mapContainer.value.innerHTML = `
          <div style="padding: 40px 20px; text-align: center; color: #909399; background: #f5f5f5; border-radius: 4px; height: 100%; display: flex; flex-direction: column; justify-content: center; align-items: center;">
            <div style="font-size: 48px; margin-bottom: 16px;">🗺️</div>
            <div style="font-size: 16px; font-weight: 500; margin-bottom: 8px; color: #606266;">地图功能需要配置API Key</div>
            <div style="font-size: 14px; color: #909399; margin-bottom: 16px;">请在环境变量中设置 VITE_AMAP_API_KEY</div>
            <div style="font-size: 12px; color: #C0C4CC;">您仍然可以通过手动输入经纬度来设置位置</div>
          </div>
        `
      }
      return
    }

    const AMap = await loadAMapScript()

    // 确保容器有内容区域
    if (mapContainer.value.offsetHeight === 0) {
      mapContainer.value.style.minHeight = props.height || '400px'
    }

    // 清空容器内容（移除可能的占位内容）
    mapContainer.value.innerHTML = ''

    // 创建地图实例
    const center = props.modelValue?.longitude && props.modelValue?.latitude
      ? [props.modelValue.longitude, props.modelValue.latitude]
      : [116.397428, 39.90923] // 默认北京天安门

    map = new AMap.Map(mapContainer.value, {
      zoom: 13,
      center: center,
      viewMode: '3D',
    })
    
    // 等待地图加载完成
    map.on('complete', () => {
      console.log('地图加载完成')
      
      // 加载Geocoder插件
      AMap.plugin('AMap.Geocoder', () => {
        // 创建地理编码实例
        geocoder = new AMap.Geocoder({
          city: '全国',
        })
        
        console.log('Geocoder插件加载完成')
        
        // 创建标记
        if (props.modelValue?.longitude && props.modelValue?.latitude) {
          addMarker([props.modelValue.longitude, props.modelValue.latitude])
          // 如果有坐标但没有地址，尝试获取地址
          if (!props.modelValue?.address) {
            setTimeout(() => {
              reverseGeocode(props.modelValue.longitude, props.modelValue.latitude)
            }, 200)
          }
        }
      })
    })

    // 地图点击事件
    map.on('click', (e) => {
      const { lng, lat } = e.lnglat
      const longitude = parseFloat(lng.toFixed(7))
      const latitude = parseFloat(lat.toFixed(7))
      
      // 更新数据
      formData.value.longitude = longitude
      formData.value.latitude = latitude
      
      // 同步更新输入框
      longitudeInput.value = longitude.toString()
      latitudeInput.value = latitude.toString()
      
      // 添加标记
      addMarker([lng, lat])
      
      // 立即更新值，触发 change 事件
      updateValue()
      
      // 延迟调用，确保geocoder已加载，获取详细地址
      setTimeout(() => {
        reverseGeocode(lng, lat)
      }, 100)
    })
  } catch (error) {
    console.error('地图初始化失败:', error)
    ElMessage.error(error.message || '地图初始化失败')
    if (mapContainer.value) {
      mapContainer.value.innerHTML = `<div style="padding: 20px; text-align: center; color: #F56C6C;">${error.message}</div>`
    }
  }
}

// 添加标记
const addMarker = (position) => {
  if (!map) return

  // 移除旧标记
  if (marker) {
    map.remove(marker)
  }

  // 创建新标记
  marker = new window.AMap.Marker({
    position: position,
    draggable: true,
  })

  map.add(marker)
  map.setCenter(position)

  // 标记拖拽事件
  marker.on('dragend', (e) => {
    const { lng, lat } = e.lnglat
    const longitude = parseFloat(lng.toFixed(7))
    const latitude = parseFloat(lat.toFixed(7))
    
    // 更新数据
    formData.value.longitude = longitude
    formData.value.latitude = latitude
    
    // 同步更新输入框
    longitudeInput.value = longitude.toString()
    latitudeInput.value = latitude.toString()
    
    // 立即更新值，触发 change 事件
    updateValue()
    
    // 获取地址
    reverseGeocode(lng, lat)
  })
}

// 逆地理编码（根据经纬度获取地址）
const reverseGeocode = (lng, lat) => {
  if (!geocoder || !window.AMap) {
    console.warn('Geocoder插件未加载，无法获取地址')
    // 即使 geocoder 未加载，也要更新值（地址为空）
    updateValue()
    return
  }

  geocoder.getAddress([lng, lat], (status, result) => {
    if (status === 'complete' && result.info === 'OK') {
      // 优先使用完整地址，如果没有则使用格式化地址，最后使用区县
      const address = result.regeocode.formattedAddress || 
                     (result.regeocode.addressComponent ? 
                       `${result.regeocode.addressComponent.province || ''}${result.regeocode.addressComponent.city || ''}${result.regeocode.addressComponent.district || ''}${result.regeocode.addressComponent.street || ''}${result.regeocode.addressComponent.streetNumber || ''}` : '') ||
                     result.regeocode.addressComponent?.district || ''
      formData.value.address = address.trim()
      // 地址获取成功后，再次更新值，触发 change 事件
      updateValue()
      console.log('地址获取成功:', address)
    } else {
      // 地址获取失败，也要更新值（地址为空）
      console.warn('地址获取失败:', status, result)
      formData.value.address = ''
      updateValue()
    }
  })
}

// 地理编码（根据地址获取经纬度）
const geocode = (address) => {
  if (!geocoder || !address || !window.AMap) {
    if (!geocoder) {
      ElMessage.warning('地理编码插件未加载，请稍候再试')
    }
    return
  }

  geocoder.getLocation(address, (status, result) => {
    if (status === 'complete' && result.geocodes && result.geocodes.length > 0) {
      const location = result.geocodes[0].location
      const longitude = parseFloat(location.lng.toFixed(7))
      const latitude = parseFloat(location.lat.toFixed(7))
      
      // 更新数据
      formData.value.longitude = longitude
      formData.value.latitude = latitude
      
      // 同步更新输入框
      longitudeInput.value = longitude.toString()
      latitudeInput.value = latitude.toString()
      
      // 优先使用格式化地址，如果没有则使用搜索的地址
      formData.value.address = result.geocodes[0].formattedAddress || address
      
      // 添加标记并定位地图
      addMarker([location.lng, location.lat])
      
      // 清空搜索框
      searchAddress.value = ''
      
      // 更新值，触发 change 事件
      updateValue()
      
      ElMessage.success('搜索成功，已定位到该地址')
    } else {
      ElMessage.error('未找到该地址，请尝试更详细的地址信息')
    }
  })
}

// 搜索地址
const handleSearch = () => {
  const address = searchAddress.value?.trim()
  if (!address) {
    ElMessage.warning('请输入地址')
    return
  }
  
  // 检查 geocoder 是否已加载
  if (!geocoder || !window.AMap) {
    ElMessage.warning('地图服务正在加载中，请稍候再试')
    // 如果地图还在加载，等待一下再重试
    if (map) {
      setTimeout(() => {
        handleSearch()
      }, 500)
    }
    return
  }
  
  geocode(address)
}

// 验证并解析经纬度
const parseCoordinate = (value, type) => {
  if (!value || value.trim() === '') {
    return null
  }
  
  const num = parseFloat(value.trim())
  if (isNaN(num)) {
    return null
  }
  
  if (type === 'longitude') {
    if (num < -180 || num > 180) {
      ElMessage.warning('经度范围应在 -180 到 180 之间')
      return null
    }
  } else if (type === 'latitude') {
    if (num < -90 || num > 90) {
      ElMessage.warning('纬度范围应在 -90 到 90 之间')
      return null
    }
  }
  
  return parseFloat(num.toFixed(7))
}

// 经度输入失焦或回车
const handleLongitudeBlur = () => {
  const longitude = parseCoordinate(longitudeInput.value, 'longitude')
  if (longitude !== null) {
    formData.value.longitude = longitude
    longitudeInput.value = longitude.toString()
    // 如果纬度也有值，立即定位
    if (formData.value.latitude !== null) {
      locateByCoordinates()
    } else {
      updateValue()
    }
  } else if (longitudeInput.value.trim() !== '') {
    ElMessage.warning('请输入有效的经度数值')
    // 恢复原来的值
    longitudeInput.value = formData.value.longitude !== null ? formData.value.longitude.toString() : ''
  } else {
    formData.value.longitude = null
    updateValue()
  }
}

// 纬度输入失焦或回车
const handleLatitudeBlur = () => {
  const latitude = parseCoordinate(latitudeInput.value, 'latitude')
  if (latitude !== null) {
    formData.value.latitude = latitude
    latitudeInput.value = latitude.toString()
    // 如果经度也有值，立即定位
    if (formData.value.longitude !== null) {
      locateByCoordinates()
    } else {
      updateValue()
    }
  } else if (latitudeInput.value.trim() !== '') {
    ElMessage.warning('请输入有效的纬度数值')
    // 恢复原来的值
    latitudeInput.value = formData.value.latitude !== null ? formData.value.latitude.toString() : ''
  } else {
    formData.value.latitude = null
    updateValue()
  }
}

// 根据经纬度定位地图
const locateByCoordinates = () => {
  if (formData.value.longitude === null || formData.value.latitude === null) {
    return
  }
  
  // 如果地图已加载，添加标记并获取地址
  if (map) {
    addMarker([formData.value.longitude, formData.value.latitude])
    // 立即更新值，触发 change 事件
    updateValue()
    // 延迟获取地址，确保 geocoder 已加载
    setTimeout(() => {
      reverseGeocode(formData.value.longitude, formData.value.latitude)
    }, 100)
    ElMessage.success('已定位到指定坐标')
  } else {
    // 地图未加载，先更新值，等待地图加载完成后再定位
    updateValue()
  }
}

// 地址手动编辑
const handleAddressChange = () => {
  // 用户手动编辑地址后，更新值
  updateValue()
}

// 重置
const handleReset = () => {
  searchAddress.value = ''
  longitudeInput.value = ''
  latitudeInput.value = ''
  formData.value = {
    longitude: null,
    latitude: null,
    address: '',
  }
  if (marker) {
    map?.remove(marker)
    marker = null
  }
  if (map) {
    // 重置地图中心到默认位置
    map.setCenter([116.397428, 39.90923])
    map.setZoom(13)
  }
  updateValue()
}

// 更新值
const updateValue = () => {
  emit('update:modelValue', {
    longitude: formData.value.longitude,
    latitude: formData.value.latitude,
    address: formData.value.address,
  })
  emit('change', {
    longitude: formData.value.longitude,
    latitude: formData.value.latitude,
    address: formData.value.address,
  })
}

// 监听外部值变化
watch(
  () => props.modelValue,
  (newVal) => {
    if (newVal) {
      const newLongitude = newVal.longitude || null
      const newLatitude = newVal.latitude || null
      const newAddress = newVal.address || ''
      
      // 只有当值真正改变时才更新
      if (formData.value.longitude !== newLongitude || 
          formData.value.latitude !== newLatitude || 
          formData.value.address !== newAddress) {
        formData.value = {
          longitude: newLongitude,
          latitude: newLatitude,
          address: newAddress,
        }
        
        // 同步输入框的值
        longitudeInput.value = newLongitude !== null ? newLongitude.toString() : ''
        latitudeInput.value = newLatitude !== null ? newLatitude.toString() : ''
        
        // 如果地图已加载且有坐标，更新地图位置
        if (newLongitude && newLatitude && map) {
          addMarker([newLongitude, newLatitude])
          // 如果有坐标但没有地址，尝试获取地址
          if (!newAddress && geocoder) {
            setTimeout(() => {
              reverseGeocode(newLongitude, newLatitude)
            }, 200)
          }
        }
      }
    } else {
      // 如果外部值为空，清空输入框
      longitudeInput.value = ''
      latitudeInput.value = ''
    }
  },
  { immediate: true, deep: true }
)

onMounted(() => {
  nextTick(() => {
    initMap()
  })
})

onBeforeUnmount(() => {
  if (map) {
    map.destroy()
    map = null
  }
  marker = null
  geocoder = null
})
</script>

<style scoped>
.map-picker-container {
  width: 100%;
}

.map-container {
  width: 100%;
  min-height: v-bind(height);
  height: v-bind(height);
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #f5f5f5;
}

.map-info {
  padding: 0 10px;
}

:deep(.el-form-item) {
  margin-bottom: 18px;
}
</style>
