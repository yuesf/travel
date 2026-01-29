-- 数据库迁移脚本：创建景点门票预订相关表
-- 创建时间: 2026-01-27
-- 说明：创建可订日期、金顶时间段、票种分类、具体票种表

USE `travel`;

-- ============================================
-- 景点可订日期表
-- ============================================
CREATE TABLE IF NOT EXISTS `attraction_booking_date` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `attraction_id` BIGINT NOT NULL COMMENT '景点ID',
  `booking_date` DATE NOT NULL COMMENT '可订日期',
  `price` DECIMAL(10,2) NOT NULL COMMENT '当日价格',
  `stock` INT DEFAULT 0 COMMENT '库存',
  `available` TINYINT DEFAULT 1 COMMENT '是否可订：0-不可订，1-可订',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_attraction_date` (`attraction_id`, `booking_date`),
  INDEX `idx_attraction_id` (`attraction_id`),
  INDEX `idx_booking_date` (`booking_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='景点可订日期表';

-- ============================================
-- 金顶时间段表
-- ============================================
CREATE TABLE IF NOT EXISTS `attraction_golden_summit_time_slot` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `attraction_id` BIGINT NOT NULL COMMENT '景点ID',
  `booking_date` DATE NOT NULL COMMENT '日期',
  `start_time` TIME NOT NULL COMMENT '开始时间',
  `end_time` TIME NOT NULL COMMENT '结束时间',
  `stock` INT DEFAULT 0 COMMENT '库存',
  `available` TINYINT DEFAULT 1 COMMENT '是否可用：0-不可用，1-可用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_attraction_date` (`attraction_id`, `booking_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='金顶时间段表';

-- ============================================
-- 票种分类表
-- ============================================
CREATE TABLE IF NOT EXISTS `attraction_ticket_category` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `attraction_id` BIGINT NOT NULL COMMENT '景点ID',
  `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
  `description` VARCHAR(500) COMMENT '分类描述',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_attraction_id` (`attraction_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='票种分类表';

-- ============================================
-- 具体票种表
-- ============================================
CREATE TABLE IF NOT EXISTS `attraction_ticket` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `attraction_id` BIGINT NOT NULL COMMENT '景点ID',
  `category_id` BIGINT NOT NULL COMMENT '票种分类ID',
  `name` VARCHAR(200) NOT NULL COMMENT '票种名称',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `included_attractions` JSON COMMENT '包含景点列表（JSON数组）',
  `verification_method` VARCHAR(100) COMMENT '核验方式：ID_CARD-身份证，VALID_DOCUMENT-有效证件',
  `refund_rule` VARCHAR(100) COMMENT '退改规则：ANYTIME_REFUND-随时可退，NO_REFUND-不可退',
  `booking_notice_url` VARCHAR(500) COMMENT '预订须知链接',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_attraction_id` (`attraction_id`),
  INDEX `idx_category_id` (`category_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='具体票种表';
