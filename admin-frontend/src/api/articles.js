import request from '@/utils/request'

/**
 * 分页查询文章列表
 */
export function getArticleList(params) {
  return request({
    url: '/admin/articles',
    method: 'get',
    params,
  })
}

/**
 * 根据ID查询文章详情
 */
export function getArticleById(id) {
  return request({
    url: `/admin/articles/${id}`,
    method: 'get',
  })
}

/**
 * 创建文章
 */
export function createArticle(data) {
  return request({
    url: '/admin/articles',
    method: 'post',
    data,
  })
}

/**
 * 更新文章
 */
export function updateArticle(id, data) {
  return request({
    url: `/admin/articles/${id}`,
    method: 'put',
    data,
  })
}

/**
 * 删除文章（软删除）
 */
export function deleteArticle(id) {
  return request({
    url: `/admin/articles/${id}`,
    method: 'delete',
  })
}

/**
 * 批量删除文章
 */
export function deleteArticlesBatch(ids) {
  return request({
    url: '/admin/articles/batch',
    method: 'delete',
    data: ids,
  })
}

/**
 * 查询所有文章分类列表
 */
export function getArticleCategoryList(status) {
  return request({
    url: '/admin/article-categories',
    method: 'get',
    params: status ? { status } : {},
  })
}

/**
 * 根据ID查询文章分类详情
 */
export function getArticleCategoryById(id) {
  return request({
    url: `/admin/article-categories/${id}`,
    method: 'get',
  })
}

/**
 * 创建文章分类
 */
export function createArticleCategory(data) {
  return request({
    url: '/admin/article-categories',
    method: 'post',
    data,
  })
}

/**
 * 更新文章分类
 */
export function updateArticleCategory(id, data) {
  return request({
    url: `/admin/article-categories/${id}`,
    method: 'put',
    data,
  })
}

/**
 * 删除文章分类
 */
export function deleteArticleCategory(id) {
  return request({
    url: `/admin/article-categories/${id}`,
    method: 'delete',
  })
}

/**
 * 查询所有文章标签列表
 */
export function getArticleTagList() {
  return request({
    url: '/admin/article-tags',
    method: 'get',
  })
}

/**
 * 根据ID查询文章标签详情
 */
export function getArticleTagById(id) {
  return request({
    url: `/admin/article-tags/${id}`,
    method: 'get',
  })
}

/**
 * 创建文章标签
 */
export function createArticleTag(data) {
  return request({
    url: '/admin/article-tags',
    method: 'post',
    data,
  })
}

/**
 * 更新文章标签
 */
export function updateArticleTag(id, data) {
  return request({
    url: `/admin/article-tags/${id}`,
    method: 'put',
    data,
  })
}

/**
 * 删除文章标签
 */
export function deleteArticleTag(id) {
  return request({
    url: `/admin/article-tags/${id}`,
    method: 'delete',
  })
}

/**
 * 为文章添加标签
 */
export function addTagToArticle(articleId, tagId) {
  return request({
    url: `/admin/article-tags/${tagId}/articles/${articleId}`,
    method: 'post',
  })
}

/**
 * 移除文章的标签
 */
export function removeTagFromArticle(articleId, tagId) {
  return request({
    url: `/admin/article-tags/${tagId}/articles/${articleId}`,
    method: 'delete',
  })
}
