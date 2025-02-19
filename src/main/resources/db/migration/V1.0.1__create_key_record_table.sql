CREATE TABLE IF NOT EXISTS key_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    key_id VARCHAR(50) NOT NULL COMMENT '密钥ID（如：PHONE_KEY, EMAIL_KEY等）',
    version VARCHAR(20) NOT NULL COMMENT '密钥版本号（格式：yyyyMMddHHmmss）',
    content TEXT NOT NULL COMMENT '密钥内容（Base64编码）',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    effective_time DATETIME NOT NULL COMMENT '生效时间',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    active BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否是当前活跃的密钥',
    status VARCHAR(20) NOT NULL COMMENT '密钥状态：ACTIVE-活跃, INACTIVE-不活跃, EXPIRED-已过期',
    creator VARCHAR(50) NOT NULL COMMENT '创建者',
    remark VARCHAR(200) COMMENT '备注',
    INDEX idx_key_id (key_id),
    INDEX idx_version (version),
    INDEX idx_active (active),
    UNIQUE INDEX uk_key_id_version (key_id, version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='密钥记录表';
