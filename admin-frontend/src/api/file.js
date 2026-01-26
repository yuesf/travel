import request from '@/utils/request'

/**
 * 分页查询文件列表
 */
export function getFileList(params) {
  return request({
    url: '/admin/file-manage/list',
    method: 'get',
    params,
  })
}

/**
 * 根据ID查询文件详情
 */
export function getFileById(id) {
  return request({
    url: `/admin/file-manage/${id}`,
    method: 'get',
  })
}

/**
 * 删除文件
 */
export function deleteFile(id) {
  return request({
    url: `/admin/file-manage/${id}`,
    method: 'delete',
  })
}

/**
 * 批量删除文件
 */
export function deleteFilesBatch(ids) {
  return request({
    url: '/admin/file-manage/batch',
    method: 'delete',
    data: ids,
  })
}

/**
 * 获取文件统计信息
 */
export function getFileStatistics() {
  return request({
    url: '/admin/file-manage/statistics',
    method: 'get',
  })
}

/**
 * 上传图片
 */
export function uploadImage(file, module = 'common') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('module', module)
  
  return request({
    url: '/common/file/upload/image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

/**
 * 上传视频
 */
export function uploadVideo(file, module = 'common') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('module', module)
  
  return request({
    url: '/common/file/upload/video',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

/**
 * 获取文件的公开URL（已废弃：OSS bucket已改为"私有写公有读"模式，不再需要签名URL）
 * 保留此函数用于向后兼容，直接返回公开URL
 * @deprecated 请直接使用fileUrl，不再需要获取签名URL
 */
export function getSignedUrl(id) {
  return request({
    url: `/admin/file-manage/${id}/signed-url`,
    method: 'get',
  })
}

/**
 * 批量获取文件的公开URL（已废弃：OSS bucket已改为"私有写公有读"模式，不再需要签名URL）
 * 保留此函数用于向后兼容，直接返回公开URL列表
 * @deprecated 请直接使用fileUrl，不再需要获取签名URL
 */
export function getSignedUrls(ids) {
  return request({
    url: '/admin/file-manage/signed-urls',
    method: 'get',
    params: {
      ids: Array.isArray(ids) ? ids.join(',') : ids,
    },
  })
}

/**
 * 根据URL获取公开URL（已废弃：OSS bucket已改为"私有写公有读"模式，不再需要签名URL）
 * 保留此函数用于向后兼容，如果是签名URL则提取基础URL部分
 * @deprecated 请直接使用URL，不再需要获取签名URL
 */
export function getSignedUrlByUrl(url) {
  return request({
    url: '/common/file/signed-url',
    method: 'get',
    params: {
      url,
    },
  })
}
