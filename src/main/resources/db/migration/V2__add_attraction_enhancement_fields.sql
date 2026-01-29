-- 数据库迁移脚本：为attraction表添加增强字段
-- 创建时间: 2026-01-27
-- 说明：添加景区评级、标签、入园须知、金顶预约等字段

USE `travel`;

-- 为attraction表添加新字段
-- 注意：MySQL不支持IF NOT EXISTS，需要通过存储过程或手动检查
-- 如果字段已存在，执行会报错，需要手动处理或使用存储过程

-- 添加景区评级字段
-- 如果字段已存在，需要先删除：ALTER TABLE `attraction` DROP COLUMN `rating`;
ALTER TABLE `attraction` 
ADD COLUMN `rating` VARCHAR(10) COMMENT '景区评级：1A-5A';

-- 添加景区标签字段（JSON数组）
ALTER TABLE `attraction` 
ADD COLUMN `tags` JSON COMMENT '景区标签列表（JSON数组）';

-- 添加入园须知内容字段
ALTER TABLE `attraction` 
ADD COLUMN `admission_notice` TEXT COMMENT '入园须知内容';

-- 添加入园须知链接字段
ALTER TABLE `attraction` 
ADD COLUMN `admission_notice_url` VARCHAR(500) COMMENT '入园须知链接';

-- 添加金顶预约启用字段
ALTER TABLE `attraction` 
ADD COLUMN `golden_summit_enabled` TINYINT DEFAULT 0 COMMENT '是否启用金顶预约：0-否，1-是';
