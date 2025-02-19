-- 创建数据库
CREATE DATABASE IF NOT EXISTS security_demo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE security_demo;

-- 删除已存在的表
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
