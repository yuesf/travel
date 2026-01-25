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
 * 获取文件的签名URL（用于私有Bucket访问）
 */
export function getSignedUrl(id) {
  return request({
    url: `/admin/file-manage/${id}/signed-url`,
    method: 'get',
  })
}

/**
 * 批量获取文件的签名URL
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
 * 根据URL获取签名URL（用于私有Bucket访问）
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
