package com.livelab.security.starter.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {
    private CryptoProperties crypto = new CryptoProperties();

    @Data
    public static class CryptoProperties {
        private String algorithm = "SM4";
        private int keyExpireMinutes = 30;
    }

    public int getKeyExpireMinutes() {
        return crypto.getKeyExpireMinutes();
    }
}
