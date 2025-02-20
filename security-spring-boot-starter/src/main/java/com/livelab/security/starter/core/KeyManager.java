package com.livelab.security.starter.core;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.KeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@EnableScheduling
public class KeyManager {
    private final SecurityKeyMapper securityKeyMapper;
    private final ConcurrentHashMap<String, SecurityKey> keyCache = new ConcurrentHashMap<>();
    private volatile SecurityKey currentKey;
    private final int keyExpireMinutes;

    public KeyManager(SecurityKeyMapper securityKeyMapper, int keyExpireMinutes) {
        this.securityKeyMapper = securityKeyMapper;
        this.keyExpireMinutes = keyExpireMinutes;
    }

    public SecurityKey getCurrentKey() {
        SecurityKey key = securityKeyMapper.selectLatestKey();
        if (key != null && key.getExpiryTime().isAfter(LocalDateTime.now())) {
            return key;
        }

        return generateNewKey();
    }

    public SecurityKey getKeyById(String keyId) {
        SecurityKey key = keyCache.get(keyId);
        if (key == null) {
            key = securityKeyMapper.selectById(keyId);
            if (key != null) {
                keyCache.put(keyId, key);
            }
        }
        return key;
    }

    private SecurityKey generateNewKey() {
        byte[] keyBytes = KeyUtil.generateKey("SM4").getEncoded();
        String keyValue = Base64.encode(keyBytes);
        
        SecurityKey key = new SecurityKey();
        key.setId(IdUtil.fastSimpleUUID());
        key.setKeyValue(keyValue);
        key.setCreateTime(LocalDateTime.now());
        key.setExpiryTime(LocalDateTime.now().plusMinutes(keyExpireMinutes));
        key.setStatus(1);
        securityKeyMapper.insert(key);
        log.info("Generated new key: {}", key.getId());
        return key;
    }

    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void cleanExpiredKeys() {
        try {
            securityKeyMapper.deleteExpiredKeys();
            // 清理缓存中的过期密钥
            keyCache.entrySet().removeIf(entry -> entry.getValue().getExpiryTime().isBefore(LocalDateTime.now()));
            log.debug("Cleaned expired keys");
        } catch (Exception e) {
            log.error("Failed to clean expired keys", e);
        }
    }
}
