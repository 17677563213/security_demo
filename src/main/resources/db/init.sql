-- 创建数据库
CREATE DATABASE IF NOT EXISTS security_demo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE security_demo;

-- 删除已存在的表
DROP TABLE IF EXISTS payment_info;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS user;

-- 创建用户表
CREATE TABLE `user` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` varchar(50) DEFAULT NULL COMMENT '姓名',
    `phone` varchar(512) DEFAULT NULL COMMENT '手机号（加密）',
    `email` varchar(512) DEFAULT NULL COMMENT '邮箱（加密）',
    `id_card` varchar(512) DEFAULT NULL COMMENT '身份证号（加密）',
    `password` varchar(255) DEFAULT NULL COMMENT '密码',
    `phone_digest` varchar(128) DEFAULT NULL COMMENT '手机号摘要',
    `id_card_digest` varchar(128) DEFAULT NULL COMMENT '身份证号摘要',
    `email_digest` varchar(128) DEFAULT NULL COMMENT '邮箱摘要',
    `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_phone_digest` (`phone_digest`),
    KEY `idx_id_card_digest` (`id_card_digest`),
    KEY `idx_email_digest` (`email_digest`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 创建订单表（示例关联表）
CREATE TABLE orders (
    -- 主键
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    
    -- 订单信息
    order_no VARCHAR(32) NOT NULL COMMENT '订单编号',
    amount DECIMAL(10,2) NOT NULL COMMENT '订单金额',
    
    -- 加密字段
    user_phone VARCHAR(512) NOT NULL COMMENT '用户手机号(加密)',
    user_phone_digest VARCHAR(128) COMMENT '用户手机号摘要',
    
    shipping_address VARCHAR(512) NOT NULL COMMENT '收货地址(加密)',
    
    -- 时间戳
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    
    -- 索引
    INDEX idx_order_no (order_no),
    INDEX idx_user_phone_digest (user_phone_digest)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单信息表';

-- 创建支付信息表（示例敏感信息表）
CREATE TABLE payment_info (
    -- 主键
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    
    -- 订单信息
    order_id BIGINT NOT NULL COMMENT '订单ID',
    
    -- 加密字段
    card_number VARCHAR(512) NOT NULL COMMENT '银行卡号(加密)',
    card_number_digest VARCHAR(128) COMMENT '银行卡号摘要',
    
    -- 时间戳
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    
    -- 索引
    INDEX idx_order_id (order_id),
    INDEX idx_card_number_digest (card_number_digest),
    
    -- 外键
    FOREIGN KEY (order_id) REFERENCES orders(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付信息表';

-- 添加一些测试数据（注意：实际值会被加密存储）
INSERT INTO user (name, phone, id_card, email, password, create_time, update_time)
VALUES 
('张三', '13800138000', '110101199001011234', 'zhangsan@example.com', 'password', NOW(), NOW()),
('李四', '13900139000', '110101199001011235', 'lisi@example.com', 'password', NOW(), NOW());
