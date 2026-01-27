-- 数据迁移脚本：将 file_record 表中的 module 数据迁移到 directory 表
-- 创建时间: 2026-01-27
-- 说明：从 file_record 表中提取所有唯一的 module 值，创建对应的目录记录

-- 临时表：存储需要创建的目录信息
CREATE TEMPORARY TABLE IF NOT EXISTS `temp_module_directories` (
  `module_path` VARCHAR(500) NOT NULL PRIMARY KEY,
  `module_name` VARCHAR(100) NOT NULL,
  `parent_path` VARCHAR(500),
  `level` INT DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 清空临时表
TRUNCATE TABLE `temp_module_directories`;

-- 步骤1：提取所有唯一的 module 值，并解析目录结构
-- 对于简单模块（如 "common"），直接创建根目录
-- 对于层级模块（如 "common/subfolder"），需要创建多级目录
INSERT INTO `temp_module_directories` (`module_path`, `module_name`, `parent_path`, `level`)
SELECT DISTINCT
  `module` AS `module_path`,
  CASE 
    -- 如果包含 "/"，取最后一部分作为目录名
    WHEN LOCATE('/', `module`) > 0 THEN SUBSTRING_INDEX(`module`, '/', -1)
    -- 否则直接使用 module 作为目录名
    ELSE `module`
  END AS `module_name`,
  CASE 
    -- 如果包含 "/"，提取父路径（去掉最后一部分）
    -- 例如 "common/subfolder" -> "common"
    WHEN LOCATE('/', `module`) > 0 THEN 
      SUBSTRING(`module`, 1, LENGTH(`module`) - LENGTH(SUBSTRING_INDEX(`module`, '/', -1)) - 1)
    -- 否则为根目录
    ELSE NULL
  END AS `parent_path`,
  CASE 
    -- 计算层级："/" 的数量 + 1
    WHEN LOCATE('/', `module`) > 0 THEN (LENGTH(`module`) - LENGTH(REPLACE(`module`, '/', ''))) + 1
    ELSE 1
  END AS `level`
FROM `file_record`
WHERE `module` IS NOT NULL AND `module` != '';

-- 步骤2：先创建所有根目录（level = 1）
INSERT INTO `directory` (`name`, `path`, `parent_id`, `level`, `sort`, `created_at`)
SELECT 
  `module_name`,
  `module_path`,
  NULL,
  1,
  0,
  NOW()
FROM `temp_module_directories`
WHERE `level` = 1
  AND NOT EXISTS (
    SELECT 1 FROM `directory` 
    WHERE `path` COLLATE utf8mb4_unicode_ci = `temp_module_directories`.`module_path` COLLATE utf8mb4_unicode_ci
  );

-- 步骤3：创建子目录（需要递归处理，先处理 level=2，再处理 level=3，以此类推）
-- 这里使用存储过程或循环来处理多级目录
-- 由于 MySQL 的限制，我们使用临时表来存储已创建的目录路径和ID的映射

-- 创建临时映射表
CREATE TEMPORARY TABLE IF NOT EXISTS `temp_directory_map` (
  `path` VARCHAR(500) NOT NULL PRIMARY KEY,
  `id` BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 清空映射表
TRUNCATE TABLE `temp_directory_map`;

-- 插入已存在的根目录映射
INSERT INTO `temp_directory_map` (`path`, `id`)
SELECT `path`, `id` FROM `directory` WHERE `level` = 1;

-- 循环创建子目录（最多支持5级目录）
-- Level 2
INSERT INTO `directory` (`name`, `path`, `parent_id`, `level`, `sort`, `created_at`)
SELECT 
  t.`module_name`,
  t.`module_path`,
  p.`id`,
  2,
  0,
  NOW()
FROM `temp_module_directories` t
INNER JOIN `temp_directory_map` p ON t.`parent_path` COLLATE utf8mb4_unicode_ci = p.`path` COLLATE utf8mb4_unicode_ci
WHERE t.`level` = 2
  AND NOT EXISTS (
    SELECT 1 FROM `directory` 
    WHERE `path` COLLATE utf8mb4_unicode_ci = t.`module_path` COLLATE utf8mb4_unicode_ci
  );

-- 更新映射表（Level 2）
INSERT INTO `temp_directory_map` (`path`, `id`)
SELECT `path`, `id` FROM `directory` WHERE `level` = 2
ON DUPLICATE KEY UPDATE `id` = VALUES(`id`);

-- Level 3
INSERT INTO `directory` (`name`, `path`, `parent_id`, `level`, `sort`, `created_at`)
SELECT 
  t.`module_name`,
  t.`module_path`,
  p.`id`,
  3,
  0,
  NOW()
FROM `temp_module_directories` t
INNER JOIN `temp_directory_map` p ON t.`parent_path` COLLATE utf8mb4_unicode_ci = p.`path` COLLATE utf8mb4_unicode_ci
WHERE t.`level` = 3
  AND NOT EXISTS (
    SELECT 1 FROM `directory` 
    WHERE `path` COLLATE utf8mb4_unicode_ci = t.`module_path` COLLATE utf8mb4_unicode_ci
  );

-- 更新映射表（Level 3）
INSERT INTO `temp_directory_map` (`path`, `id`)
SELECT `path`, `id` FROM `directory` WHERE `level` = 3
ON DUPLICATE KEY UPDATE `id` = VALUES(`id`);

-- Level 4
INSERT INTO `directory` (`name`, `path`, `parent_id`, `level`, `sort`, `created_at`)
SELECT 
  t.`module_name`,
  t.`module_path`,
  p.`id`,
  4,
  0,
  NOW()
FROM `temp_module_directories` t
INNER JOIN `temp_directory_map` p ON t.`parent_path` COLLATE utf8mb4_unicode_ci = p.`path` COLLATE utf8mb4_unicode_ci
WHERE t.`level` = 4
  AND NOT EXISTS (
    SELECT 1 FROM `directory` 
    WHERE `path` COLLATE utf8mb4_unicode_ci = t.`module_path` COLLATE utf8mb4_unicode_ci
  );

-- 更新映射表（Level 4）
INSERT INTO `temp_directory_map` (`path`, `id`)
SELECT `path`, `id` FROM `directory` WHERE `level` = 4
ON DUPLICATE KEY UPDATE `id` = VALUES(`id`);

-- Level 5
INSERT INTO `directory` (`name`, `path`, `parent_id`, `level`, `sort`, `created_at`)
SELECT 
  t.`module_name`,
  t.`module_path`,
  p.`id`,
  5,
  0,
  NOW()
FROM `temp_module_directories` t
INNER JOIN `temp_directory_map` p ON t.`parent_path` COLLATE utf8mb4_unicode_ci = p.`path` COLLATE utf8mb4_unicode_ci
WHERE t.`level` = 5
  AND NOT EXISTS (
    SELECT 1 FROM `directory` 
    WHERE `path` COLLATE utf8mb4_unicode_ci = t.`module_path` COLLATE utf8mb4_unicode_ci
  );

-- 清理临时表（MySQL 会在会话结束时自动清理，但显式清理更安全）
DROP TEMPORARY TABLE IF EXISTS `temp_module_directories`;
DROP TEMPORARY TABLE IF EXISTS `temp_directory_map`;

-- 验证：显示迁移结果统计
SELECT 
  COUNT(*) AS total_directories,
  COUNT(DISTINCT `level`) AS directory_levels,
  MAX(`level`) AS max_level
FROM `directory`;
