-- 添加景点票种库存字段
-- 注意：执行前请确认当前数据库中尚未存在 stock 列

ALTER TABLE attraction_ticket
    ADD COLUMN stock INT NOT NULL DEFAULT 0 COMMENT '库存' AFTER price;

