CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `phone` VARCHAR(255) COMMENT '手机号（加密）',
    `email` VARCHAR(255) COMMENT '邮箱（加密）',
    `id_card` VARCHAR(255) COMMENT '身份证号（加密）',
    `phone_digest` VARCHAR(32) COMMENT '手机号摘要',
    `email_digest` VARCHAR(32) COMMENT '邮箱摘要',
    `id_card_digest` VARCHAR(32) COMMENT '身份证号摘要',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_phone_digest` (`phone_digest`),
    INDEX `idx_email_digest` (`email_digest`),
    INDEX `idx_id_card_digest` (`id_card_digest`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
