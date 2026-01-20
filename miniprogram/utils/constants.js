/**
 * 小程序常量定义
 */

// API配置
const API_BASE_URL = 'http://localhost:8080/api/v1';
const API_TIMEOUT = 10000; // 10秒

// 存储Key
const STORAGE_KEYS = {
  TOKEN: 'token', // 保留兼容性
  SESSION_ID: 'sessionId', // 小程序Session ID
  USER_INFO: 'userInfo',
  CART_COUNT: 'cartCount',
  SEARCH_HISTORY: 'searchHistory',
};

// 订单状态
const ORDER_STATUS = {
  PENDING_PAY: 'PENDING_PAY', // 待支付
  PAID: 'PAID', // 已支付
  USED: 'USED', // 已使用
  COMPLETED: 'COMPLETED', // 已完成
  CANCELLED: 'CANCELLED', // 已取消
  REFUNDED: 'REFUNDED', // 已退款
};

// 订单状态文本
const ORDER_STATUS_TEXT = {
  [ORDER_STATUS.PENDING_PAY]: '待支付',
  [ORDER_STATUS.PAID]: '已支付',
  [ORDER_STATUS.USED]: '已使用',
  [ORDER_STATUS.COMPLETED]: '已完成',
  [ORDER_STATUS.CANCELLED]: '已取消',
  [ORDER_STATUS.REFUNDED]: '已退款',
};

// 订单类型
const ORDER_TYPE = {
  ATTRACTION: 'ATTRACTION', // 景点
  HOTEL: 'HOTEL', // 酒店
  PRODUCT: 'PRODUCT', // 商品
};

// 优惠券状态
const COUPON_STATUS = {
  UNUSED: 0, // 未使用
  USED: 1, // 已使用
  EXPIRED: 2, // 已过期
};

// 优惠券状态文本
const COUPON_STATUS_TEXT = {
  [COUPON_STATUS.UNUSED]: '未使用',
  [COUPON_STATUS.USED]: '已使用',
  [COUPON_STATUS.EXPIRED]: '已过期',
};

// 文章状态
const ARTICLE_STATUS = {
  DRAFT: 0, // 草稿
  PUBLISHED: 1, // 已发布
  OFFLINE: 2, // 已下架
};

// 分页配置
const PAGINATION = {
  DEFAULT_PAGE_SIZE: 10,
  MAX_PAGE_SIZE: 100,
};

// 请求重试配置
const RETRY_CONFIG = {
  MAX_RETRIES: 3, // 最大重试次数
  RETRY_DELAY: 1000, // 重试延迟（毫秒）
};

// 默认图片路径（使用相对路径，避免被当作服务器路径）
const DEFAULT_IMAGES = {
  PRODUCT: '../../static/images/default-product.png', // 默认商品图片
};

module.exports = {
  API_BASE_URL,
  API_TIMEOUT,
  STORAGE_KEYS,
  ORDER_STATUS,
  ORDER_STATUS_TEXT,
  ORDER_TYPE,
  COUPON_STATUS,
  COUPON_STATUS_TEXT,
  ARTICLE_STATUS,
  PAGINATION,
  RETRY_CONFIG,
  DEFAULT_IMAGES,
};
