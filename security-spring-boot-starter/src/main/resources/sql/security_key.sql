CREATE DATABASE IF NOT EXISTS security_key_db;
USE security_key_db;


DROP TABLE IF EXISTS `security_key`;
CREATE TABLE `security_key` (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `key_type` varchar(50) NOT NULL COMMENT '密钥类型',
                                `key_value` varchar(255) NOT NULL COMMENT '密钥值',
                                `effective_time` datetime NOT NULL COMMENT '生效时间',
                                `expiry_time` datetime NOT NULL COMMENT '过期时间',
                                `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1-有效，0-无效',
                                `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                PRIMARY KEY (`id`),
                                KEY `idx_key_type_status` (`key_type`,`status`),
                                KEY `idx_expiry_time` (`expiry_time`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='安全密钥表';

SET FOREIGN_KEY_CHECKS = 1;
