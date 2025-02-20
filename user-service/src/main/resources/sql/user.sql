DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
                        `id` bigint NOT NULL AUTO_INCREMENT,
                        `username` varchar(50) NOT NULL COMMENT '用户名',
                        `password` varchar(100) NOT NULL COMMENT '密码',
                        `phone` varchar(255) DEFAULT NULL COMMENT '手机号（加密）',
                        `email` varchar(255) DEFAULT NULL COMMENT '邮箱（加密）',
                        `id_card` varchar(255) DEFAULT NULL COMMENT '身份证号（加密）',
                        `phone_digest` varchar(32) DEFAULT NULL COMMENT '手机号摘要',
                        `email_digest` varchar(32) DEFAULT NULL COMMENT '邮箱摘要',
                        `id_card_digest` varchar(32) DEFAULT NULL COMMENT '身份证号摘要',
                        `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
                        `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        `status` int DEFAULT NULL,
                        PRIMARY KEY (`id`),
                        KEY `idx_phone_digest` (`phone_digest`),
                        KEY `idx_email_digest` (`email_digest`),
                        KEY `idx_id_card_digest` (`id_card_digest`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

SET FOREIGN_KEY_CHECKS = 1;