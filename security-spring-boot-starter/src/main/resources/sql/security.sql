CREATE DATABASE IF NOT EXISTS security_db;
USE security_db;

CREATE TABLE IF NOT EXISTS `encryption_key` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `key_type` VARCHAR(50) NOT NULL COMMENT '密钥类型',
    `key_value` VARCHAR(255) NOT NULL COMMENT '密钥值',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `expire_time` DATETIME NOT NULL COMMENT '过期时间',
    INDEX `idx_key_type` (`key_type`),
    INDEX `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='加密密钥表';
