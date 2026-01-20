import request from '@/utils/request'

/**
 * 分页查询商品列表
 */
export function getProductList(params) {
  return request({
    url: '/admin/products',
    method: 'get',
    params,
  })
}

/**
 * 根据ID查询商品详情
 */
export function getProductById(id) {
  return request({
    url: `/admin/products/${id}`,
    method: 'get',
  })
}

/**
 * 创建商品
 */
export function createProduct(data) {
  return request({
    url: '/admin/products',
    method: 'post',
    data,
  })
}

/**
 * 更新商品
 */
export function updateProduct(id, data) {
  return request({
    url: `/admin/products/${id}`,
    method: 'put',
    data,
  })
}

/**
 * 删除商品（软删除）
 */
export function deleteProduct(id) {
  return request({
    url: `/admin/products/${id}`,
    method: 'delete',
  })
}

/**
 * 更新商品库存
 */
export function updateProductStock(id, stock) {
  return request({
    url: `/admin/products/${id}/stock`,
    method: 'put',
    params: { stock },
  })
}
