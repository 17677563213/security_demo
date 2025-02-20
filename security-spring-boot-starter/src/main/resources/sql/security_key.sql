CREATE TABLE IF NOT EXISTS security_key (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    key_type VARCHAR(50) NOT NULL COMMENT '密钥类型',
    key_value VARCHAR(255) NOT NULL COMMENT '密钥值',
    effective_time DATETIME NOT NULL COMMENT '生效时间',
    expiry_time DATETIME NOT NULL COMMENT '过期时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-有效，0-无效',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_key_type_status (key_type, status),
    INDEX idx_expiry_time (expiry_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全密钥表';
