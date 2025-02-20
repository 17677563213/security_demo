package com.livelab.security.starter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "security.digest")
public class SecurityProperties {
    /**
     * 摘要算法盐值
     */
    private String salt = "liveLab2025";

    /**
     * 摘要算法类型
     */
    private String algorithm = "sm3";
}
