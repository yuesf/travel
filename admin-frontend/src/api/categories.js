import request from '@/utils/request'

/**
 * 查询分类列表
 */
export function getCategoryList(params) {
  return request({
    url: '/admin/product-categories',
    method: 'get',
    params,
  })
}

/**
 * 查询分类树
 */
export function getCategoryTree(params) {
  return request({
    url: '/admin/product-categories/tree',
    method: 'get',
    params,
  })
}

/**
 * 根据ID查询分类详情
 */
export function getCategoryById(id) {
  return request({
    url: `/admin/product-categories/${id}`,
    method: 'get',
  })
}

/**
 * 创建分类
 */
export function createCategory(data) {
  return request({
    url: '/admin/product-categories',
    method: 'post',
    data,
  })
}

/**
 * 更新分类
 */
export function updateCategory(id, data) {
  return request({
    url: `/admin/product-categories/${id}`,
    method: 'put',
    data,
  })
}

/**
 * 删除分类
 */
export function deleteCategory(id) {
  return request({
    url: `/admin/product-categories/${id}`,
    method: 'delete',
  })
}

/**
 * 删除分类及其下的所有商品
 */
export function deleteCategoryWithProducts(id) {
  return request({
    url: `/admin/product-categories/${id}/with-products`,
    method: 'delete',
  })
}
