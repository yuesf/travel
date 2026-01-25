-- 旅游平台数据库初始化脚本
-- 创建时间: 2024-01-15

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `travel` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `travel`;

-- ============================================
-- 用户相关表
-- ============================================

-- 管理员表
CREATE TABLE IF NOT EXISTS `admin_user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
  `phone` VARCHAR(20) COMMENT '手机号',
  `email` VARCHAR(100) COMMENT '邮箱',
  `real_name` VARCHAR(50) COMMENT '真实姓名',
  `role_id` BIGINT COMMENT '角色ID',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `last_login_time` DATETIME COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) COMMENT '最后登录IP',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_username` (`username`),
  INDEX `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 小程序用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `openid` VARCHAR(100) UNIQUE COMMENT '微信openid',
  `unionid` VARCHAR(100) COMMENT '微信unionid',
  `nickname` VARCHAR(100) COMMENT '昵称',
  `avatar` VARCHAR(500) COMMENT '头像URL',
  `phone` VARCHAR(20) COMMENT '手机号',
  `gender` TINYINT COMMENT '性别：0-未知，1-男，2-女',
  `is_first_order` TINYINT DEFAULT 1 COMMENT '是否首次下单：0-否，1-是',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_openid` (`openid`),
  INDEX `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小程序用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS `role` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称',
  `code` VARCHAR(50) NOT NULL UNIQUE COMMENT '角色代码',
  `description` VARCHAR(200) COMMENT '描述',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS `permission` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL COMMENT '权限名称',
  `code` VARCHAR(100) NOT NULL UNIQUE COMMENT '权限代码',
  `type` VARCHAR(20) COMMENT '权限类型：menu-菜单，button-按钮',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父权限ID',
  `path` VARCHAR(200) COMMENT '路径',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS `role_permission` (
  `role_id` BIGINT NOT NULL,
  `permission_id` BIGINT NOT NULL,
  PRIMARY KEY (`role_id`, `permission_id`),
  INDEX `idx_role_id` (`role_id`),
  INDEX `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 用户收货地址表
CREATE TABLE IF NOT EXISTS `user_address` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人手机号',
  `province` VARCHAR(50) NOT NULL COMMENT '省份',
  `city` VARCHAR(50) NOT NULL COMMENT '城市',
  `district` VARCHAR(50) NOT NULL COMMENT '区县',
  `detail_address` VARCHAR(500) NOT NULL COMMENT '详细地址',
  `is_default` TINYINT DEFAULT 0 COMMENT '是否默认地址：0-否，1-是',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_is_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收货地址表';

-- ============================================
-- 业务相关表
-- ============================================

-- 景点表
CREATE TABLE IF NOT EXISTS `attraction` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(200) NOT NULL COMMENT '景点名称',
  `location` VARCHAR(200) COMMENT '位置',
  `province` VARCHAR(50) COMMENT '省份',
  `city` VARCHAR(50) COMMENT '城市',
  `district` VARCHAR(50) COMMENT '区县',
  `address` VARCHAR(500) COMMENT '详细地址',
  `description` TEXT COMMENT '简介',
  `images` JSON COMMENT '图片列表（JSON数组）',
  `video_url` VARCHAR(500) COMMENT '视频URL',
  `open_time` VARCHAR(200) COMMENT '开放时间',
  `contact_phone` VARCHAR(20) COMMENT '联系电话',
  `longitude` DECIMAL(10,7) COMMENT '经度',
  `latitude` DECIMAL(10,7) COMMENT '纬度',
  `ticket_price` DECIMAL(10,2) COMMENT '门票价格',
  `ticket_stock` INT DEFAULT 0 COMMENT '门票库存',
  `valid_period` VARCHAR(100) COMMENT '有效期',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-下架，1-上架',
  `sync_source` VARCHAR(100) COMMENT '同步来源',
  `sync_time` DATETIME COMMENT '同步时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_name` (`name`),
  INDEX `idx_city` (`city`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='景点表';

-- 酒店表
CREATE TABLE IF NOT EXISTS `hotel` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(200) NOT NULL COMMENT '酒店名称',
  `address` VARCHAR(500) COMMENT '地址',
  `province` VARCHAR(50) COMMENT '省份',
  `city` VARCHAR(50) COMMENT '城市',
  `district` VARCHAR(50) COMMENT '区县',
  `star_level` TINYINT COMMENT '星级：1-5',
  `description` TEXT COMMENT '简介',
  `images` JSON COMMENT '图片列表（JSON数组）',
  `facilities` JSON COMMENT '设施列表（JSON数组）',
  `contact_phone` VARCHAR(20) COMMENT '联系电话',
  `longitude` DECIMAL(10,7) COMMENT '经度',
  `latitude` DECIMAL(10,7) COMMENT '纬度',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-下架，1-上架',
  `sync_source` VARCHAR(100) COMMENT '同步来源',
  `sync_time` DATETIME COMMENT '同步时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_name` (`name`),
  INDEX `idx_city` (`city`),
  INDEX `idx_star_level` (`star_level`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='酒店表';

-- 酒店房型表
CREATE TABLE IF NOT EXISTS `hotel_room` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `hotel_id` BIGINT NOT NULL COMMENT '酒店ID',
  `room_type` VARCHAR(100) NOT NULL COMMENT '房型名称',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `stock` INT DEFAULT 0 COMMENT '库存',
  `bed_type` VARCHAR(50) COMMENT '床型',
  `area` DECIMAL(8,2) COMMENT '面积（平方米）',
  `facilities` JSON COMMENT '设施列表（JSON数组）',
  `images` JSON COMMENT '图片列表（JSON数组）',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-下架，1-上架',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_hotel_id` (`hotel_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='酒店房型表';

-- 商品分类表
CREATE TABLE IF NOT EXISTS `product_category` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
  `icon` VARCHAR(500) COMMENT '分类图标',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID',
  `level` TINYINT DEFAULT 1 COMMENT '分类层级：1-一级，2-二级',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `type` VARCHAR(20) DEFAULT 'DISPLAY' COMMENT '分类类型：DISPLAY-展示类型，CONFIG-配置类型（用于Icon配置）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_parent_id` (`parent_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 商品表
CREATE TABLE IF NOT EXISTS `product` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(200) NOT NULL COMMENT '商品名称',
  `code` VARCHAR(100) COMMENT '商品编码',
  `category_id` BIGINT COMMENT '分类ID',
  `price` DECIMAL(10,2) NULL COMMENT '价格（H5类型商品可为空）',
  `original_price` DECIMAL(10,2) COMMENT '原价',
  `stock` INT NULL COMMENT '库存（H5类型商品可为空）',
  `sales` INT DEFAULT 0 COMMENT '销量',
  `description` TEXT COMMENT '描述',
  `images` JSON COMMENT '图片列表（JSON数组）',
  `specifications` JSON COMMENT '规格（JSON对象）',
  `h5_link` VARCHAR(500) COMMENT '外部链接（H5类型商品使用）',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-下架，1-上架',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_category_id` (`category_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_sales` (`sales`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 优惠券表
CREATE TABLE IF NOT EXISTS `coupon` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL COMMENT '优惠券名称',
  `type` VARCHAR(50) NOT NULL COMMENT '类型：FULL_REDUCTION-满减，DISCOUNT-折扣，FREE_SHIPPING-免运费，FIRST_ORDER_REDUCTION-首次满减',
  `amount` DECIMAL(10,2) COMMENT '面额/折扣值',
  `min_amount` DECIMAL(10,2) COMMENT '最低使用金额',
  `scope` VARCHAR(50) DEFAULT 'ALL' COMMENT '适用范围：ALL-全场，PRODUCT-指定商品，CATEGORY-指定分类',
  `scope_ids` JSON COMMENT '适用范围ID列表（JSON数组）',
  `total_count` INT COMMENT '发放总数',
  `used_count` INT DEFAULT 0 COMMENT '已使用数量',
  `limit_per_user` INT DEFAULT 1 COMMENT '每人限领数量',
  `valid_start_time` DATETIME COMMENT '有效期开始时间',
  `valid_end_time` DATETIME COMMENT '有效期结束时间',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_type` (`type`),
  INDEX `idx_status` (`status`),
  INDEX `idx_valid_time` (`valid_start_time`, `valid_end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

-- 用户优惠券表
CREATE TABLE IF NOT EXISTS `user_coupon` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `coupon_id` BIGINT NOT NULL COMMENT '优惠券ID',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-未使用，1-已使用，2-已过期',
  `used_time` DATETIME COMMENT '使用时间',
  `order_id` BIGINT COMMENT '使用的订单ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_coupon_id` (`coupon_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_user_status` (`user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `order_type` VARCHAR(20) NOT NULL COMMENT '订单类型：ATTRACTION-景点，HOTEL-酒店，PRODUCT-商品',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
  `discount_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '优惠金额',
  `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
  `coupon_id` BIGINT COMMENT '使用的优惠券ID',
  `status` VARCHAR(20) NOT NULL COMMENT '订单状态：PENDING_PAY-待支付，PAID-已支付，USED-已使用，COMPLETED-已完成，CANCELLED-已取消，REFUNDED-已退款',
  `pay_time` DATETIME COMMENT '支付时间',
  `pay_type` VARCHAR(20) COMMENT '支付方式：WECHAT-微信支付',
  `pay_no` VARCHAR(100) COMMENT '支付流水号',
  `contact_name` VARCHAR(50) COMMENT '联系人姓名',
  `contact_phone` VARCHAR(20) COMMENT '联系电话',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_order_no` (`order_no`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单明细表
CREATE TABLE IF NOT EXISTS `order_item` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `item_type` VARCHAR(20) NOT NULL COMMENT '明细类型：ATTRACTION-景点，HOTEL_ROOM-酒店房型，PRODUCT-商品',
  `item_id` BIGINT NOT NULL COMMENT '明细ID（景点ID/房型ID/商品ID）',
  `item_name` VARCHAR(200) COMMENT '明细名称',
  `item_image` VARCHAR(500) COMMENT '商品图片URL',
  `item_code` VARCHAR(100) COMMENT '商品编码',
  `quantity` INT NOT NULL COMMENT '数量',
  `price` DECIMAL(10,2) NOT NULL COMMENT '单价',
  `total_price` DECIMAL(10,2) NOT NULL COMMENT '小计',
  `check_in_date` DATE COMMENT '入住日期（酒店）',
  `check_out_date` DATE COMMENT '退房日期（酒店）',
  `use_date` DATE COMMENT '使用日期（景点）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- 地图表
CREATE TABLE IF NOT EXISTS `map` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(200) NOT NULL COMMENT '地图名称',
  `longitude` DECIMAL(10,7) NOT NULL COMMENT '经度',
  `latitude` DECIMAL(10,7) NOT NULL COMMENT '纬度',
  `address` VARCHAR(500) COMMENT '地址描述',
  `announcement` TEXT COMMENT '公告内容',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地图表';

-- ============================================
-- 系统相关表
-- ============================================

-- 同步配置表
CREATE TABLE IF NOT EXISTS `sync_config` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `sync_type` VARCHAR(50) NOT NULL COMMENT '同步类型：ATTRACTION-景点，HOTEL-酒店',
  `api_url` VARCHAR(500) NOT NULL COMMENT 'API地址',
  `api_key` VARCHAR(200) COMMENT 'API密钥',
  `api_secret` VARCHAR(200) COMMENT 'API密钥',
  `sync_frequency` VARCHAR(50) COMMENT '同步频率：MANUAL-手动，DAILY-每日，WEEKLY-每周',
  `sync_time` VARCHAR(20) COMMENT '同步时间（如：02:00）',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `last_sync_time` DATETIME COMMENT '最后同步时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_sync_type` (`sync_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='同步配置表';

-- 同步日志表
CREATE TABLE IF NOT EXISTS `sync_log` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `sync_type` VARCHAR(50) NOT NULL COMMENT '同步类型',
  `sync_config_id` BIGINT COMMENT '同步配置ID',
  `status` VARCHAR(20) NOT NULL COMMENT '状态：SUCCESS-成功，FAILED-失败',
  `total_count` INT DEFAULT 0 COMMENT '总记录数',
  `success_count` INT DEFAULT 0 COMMENT '成功数量',
  `failed_count` INT DEFAULT 0 COMMENT '失败数量',
  `error_message` TEXT COMMENT '错误信息',
  `start_time` DATETIME COMMENT '开始时间',
  `end_time` DATETIME COMMENT '结束时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_sync_type` (`sync_type`),
  INDEX `idx_status` (`status`),
  INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='同步日志表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `admin_id` BIGINT COMMENT '管理员ID',
  `admin_name` VARCHAR(50) COMMENT '管理员名称',
  `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型',
  `operation_desc` VARCHAR(500) COMMENT '操作描述',
  `request_method` VARCHAR(10) COMMENT '请求方法',
  `request_url` VARCHAR(500) COMMENT '请求URL',
  `request_params` TEXT COMMENT '请求参数',
  `ip_address` VARCHAR(50) COMMENT 'IP地址',
  `status` TINYINT COMMENT '状态：0-失败，1-成功',
  `error_message` TEXT COMMENT '错误信息',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_admin_id` (`admin_id`),
  INDEX `idx_operation_type` (`operation_type`),
  INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 小程序配置表
CREATE TABLE IF NOT EXISTS `miniprogram_config` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `config_key` VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
  `config_value` TEXT COMMENT '配置值（JSON格式）',
  `config_type` VARCHAR(50) COMMENT '配置类型：BANNER-轮播图，RECOMMEND-推荐，CATEGORY-分类',
  `description` VARCHAR(200) COMMENT '描述',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_config_type` (`config_type`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小程序配置表';

-- 支付配置表
CREATE TABLE IF NOT EXISTS `payment_config` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `app_id` VARCHAR(100) NOT NULL COMMENT '小程序AppID',
  `mch_id` VARCHAR(100) COMMENT '商户号',
  `api_key` VARCHAR(500) COMMENT 'API密钥（加密存储）',
  `notify_url` VARCHAR(500) COMMENT '支付回调地址',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `description` VARCHAR(200) COMMENT '描述',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_app_id` (`app_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付配置表';

-- 商家配置表
CREATE TABLE IF NOT EXISTS `merchant_config` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `merchant_name` VARCHAR(200) NOT NULL COMMENT '商家名称',
  `contact_phone` VARCHAR(20) COMMENT '联系电话',
  `contact_email` VARCHAR(100) COMMENT '联系邮箱',
  `address` VARCHAR(500) COMMENT '商家地址',
  `description` VARCHAR(500) COMMENT '商家描述',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家配置表';

-- 购物车表（小程序使用）
CREATE TABLE IF NOT EXISTS `cart` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `item_type` VARCHAR(20) NOT NULL COMMENT '商品类型：ATTRACTION-景点，HOTEL_ROOM-酒店房型，PRODUCT-商品',
  `item_id` BIGINT NOT NULL COMMENT '商品ID',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_user_id` (`user_id`),
  UNIQUE KEY `uk_user_item` (`user_id`, `item_type`, `item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- OSS配置表（单例表，只有一条记录）
CREATE TABLE IF NOT EXISTS `oss_config` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `endpoint` VARCHAR(255) NOT NULL COMMENT 'OSS Endpoint（如：oss-cn-hangzhou.aliyuncs.com）',
  `access_key_id` VARCHAR(255) NOT NULL COMMENT 'Access Key ID',
  `access_key_secret` VARCHAR(500) NOT NULL COMMENT 'Access Key Secret（加密存储）',
  `bucket_name` VARCHAR(255) NOT NULL COMMENT 'Bucket名称',
  `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用（1:启用, 0:禁用）',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT COMMENT '创建人ID',
  `updated_by` BIGINT COMMENT '更新人ID',
  UNIQUE KEY `uk_singleton` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OSS配置表（单例表，只有一条记录）';

-- 文件记录表
CREATE TABLE IF NOT EXISTS `file_record` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `file_name` VARCHAR(255) NOT NULL COMMENT '文件名（UUID生成）',
  `original_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
  `file_path` VARCHAR(500) NOT NULL COMMENT '文件路径（相对路径）',
  `file_url` VARCHAR(500) NOT NULL COMMENT '文件访问URL（完整URL）',
  `file_size` BIGINT NOT NULL COMMENT '文件大小（字节）',
  `file_type` VARCHAR(50) NOT NULL COMMENT '文件类型（image/video）',
  `file_extension` VARCHAR(20) NOT NULL COMMENT '文件扩展名（jpg/png/mp4等）',
  `module` VARCHAR(50) NOT NULL COMMENT '模块名称（common/article/banner等）',
  `storage_type` VARCHAR(20) NOT NULL COMMENT '存储类型（OSS/LOCAL）',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `created_by` BIGINT COMMENT '上传人ID',
  INDEX `idx_module` (`module`) COMMENT '模块索引',
  INDEX `idx_file_type` (`file_type`) COMMENT '文件类型索引',
  INDEX `idx_storage_type` (`storage_type`) COMMENT '存储类型索引',
  INDEX `idx_created_at` (`created_at`) COMMENT '创建时间索引',
  INDEX `idx_created_by` (`created_by`) COMMENT '创建人索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件记录表';

-- 缓存刷新任务表
CREATE TABLE IF NOT EXISTS `cache_refresh_task` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `task_id` VARCHAR(64) NOT NULL COMMENT '任务ID（UUID）',
    `cache_type` VARCHAR(50) NOT NULL COMMENT '缓存类型',
    `status` VARCHAR(20) NOT NULL COMMENT '任务状态：PENDING(等待中), RUNNING(执行中), COMPLETED(已完成), FAILED(失败)',
    `total_count` INT NOT NULL DEFAULT 0 COMMENT '总数量',
    `processed_count` INT NOT NULL DEFAULT 0 COMMENT '已处理数量',
    `success_count` INT NOT NULL DEFAULT 0 COMMENT '成功数量',
    `failure_count` INT NOT NULL DEFAULT 0 COMMENT '失败数量',
    `error_message` TEXT COMMENT '错误信息',
    `start_time` DATETIME COMMENT '开始时间',
    `end_time` DATETIME COMMENT '完成时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_id` (`task_id`),
    KEY `idx_status` (`status`),
    KEY `idx_cache_type` (`cache_type`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='缓存刷新任务表';

-- ============================================
-- 文章相关表
-- ============================================

-- 文章表
CREATE TABLE IF NOT EXISTS `article` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL COMMENT '文章标题',
  `summary` VARCHAR(500) COMMENT '文章摘要',
  `cover_image` VARCHAR(500) COMMENT '封面图URL',
  `content` LONGTEXT NOT NULL COMMENT '文章内容（富文本）',
  `category_id` BIGINT COMMENT '分类ID',
  `author` VARCHAR(50) COMMENT '作者',
  `author_id` BIGINT COMMENT '作者ID（管理员ID）',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-草稿，1-已发布，2-已下架',
  `publish_time` DATETIME COMMENT '发布时间',
  `view_count` INT DEFAULT 0 COMMENT '阅读量',
  `like_count` INT DEFAULT 0 COMMENT '点赞量',
  `favorite_count` INT DEFAULT 0 COMMENT '收藏量',
  `is_recommend` TINYINT DEFAULT 0 COMMENT '是否推荐：0-否，1-是',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_category_id` (`category_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_publish_time` (`publish_time`),
  INDEX `idx_is_recommend` (`is_recommend`),
  INDEX `idx_author_id` (`author_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- 文章分类表
CREATE TABLE IF NOT EXISTS `article_category` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
  `icon` VARCHAR(500) COMMENT '分类图标',
  `description` VARCHAR(500) COMMENT '分类描述',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章分类表';

-- 文章标签表
CREATE TABLE IF NOT EXISTS `article_tag` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名称',
  `color` VARCHAR(20) COMMENT '标签颜色',
  `use_count` INT DEFAULT 0 COMMENT '使用次数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章标签表';

-- 文章标签关联表
CREATE TABLE IF NOT EXISTS `article_tag_relation` (
  `article_id` BIGINT NOT NULL COMMENT '文章ID',
  `tag_id` BIGINT NOT NULL COMMENT '标签ID',
  PRIMARY KEY (`article_id`, `tag_id`),
  INDEX `idx_article_id` (`article_id`),
  INDEX `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章标签关联表';

-- 用户文章收藏表
CREATE TABLE IF NOT EXISTS `user_article_favorite` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `article_id` BIGINT NOT NULL COMMENT '文章ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_article_id` (`article_id`),
  UNIQUE KEY `uk_user_article` (`user_id`, `article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户文章收藏表';

-- 用户文章点赞表
CREATE TABLE IF NOT EXISTS `user_article_like` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `article_id` BIGINT NOT NULL COMMENT '文章ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_article_id` (`article_id`),
  UNIQUE KEY `uk_user_article` (`user_id`, `article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户文章点赞表';
