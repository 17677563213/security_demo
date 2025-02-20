package com.livelab.security.starter.core;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.livelab.security.starter.autoconfigure.SecurityProperties;
import com.livelab.security.starter.entity.SecurityKey;
import com.livelab.security.starter.exception.SecurityException;
import com.livelab.security.starter.mapper.SecurityKeyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class KeyManager {
    private final SecurityProperties properties;
    private final SecurityKeyMapper securityKeyMapper;
    private final ConcurrentHashMap<String, String> keyCache = new ConcurrentHashMap<>();
    
    public KeyManager(SecurityProperties properties, SecurityKeyMapper securityKeyMapper) {
        this.properties = properties;
        this.securityKeyMapper = securityKeyMapper;
    }

    public String getKey(String keyType) {
        // 首先从缓存获取
        String cachedKey = keyCache.get(keyType);
        if (cachedKey != null) {
            return cachedKey;
        }

        // 从数据库获取有效的密钥
        SecurityKey securityKey = securityKeyMapper.selectOne(
            new LambdaQueryWrapper<SecurityKey>()
                .eq(SecurityKey::getKeyType, keyType)
                .eq(SecurityKey::getStatus, 1)
                .le(SecurityKey::getEffectiveTime, LocalDateTime.now())
                .ge(SecurityKey::getExpiryTime, LocalDateTime.now())
                .orderByDesc(SecurityKey::getId)
                .last("LIMIT 1")
        );

        if (securityKey != null) {
            keyCache.put(keyType, securityKey.getKeyValue());
            return securityKey.getKeyValue();
        }

        // 如果数据库中没有有效的密钥，则使用配置文件中的密钥
        String configKey = getKeyFromConfig(keyType);
        if (configKey != null) {
            // 将配置文件中的密钥保存到数据库
            saveKeyToDatabase(keyType, configKey);
            keyCache.put(keyType, configKey);
            return configKey;
        }

        throw new SecurityException("No valid key found for type: " + keyType);
    }

    private String getKeyFromConfig(String keyType) {
        switch (keyType) {
            case "PHONE_KEY":
                return properties.getPhoneKey();
            case "EMAIL_KEY":
                return properties.getEmailKey();
            case "ID_CARD_KEY":
                return properties.getIdCardKey();
            default:
                return null;
        }
    }

    @Transactional
    public void saveKeyToDatabase(String keyType, String keyValue) {
        SecurityKey securityKey = new SecurityKey();
        securityKey.setKeyType(keyType);
        securityKey.setKeyValue(keyValue);
        securityKey.setStatus(1);
        securityKey.setEffectiveTime(LocalDateTime.now());
        securityKey.setExpiryTime(LocalDateTime.now().plusMinutes(properties.getKeyExpireMinutes()));
        securityKey.setCreateTime(LocalDateTime.now());
        securityKey.setUpdateTime(LocalDateTime.now());
        
        securityKeyMapper.insert(securityKey);
    }

    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    @Transactional
    public void cleanExpiredKeys() {
        try {
            log.info("Starting to clean expired keys...");
            
            // 清理过期的缓存
            keyCache.clear();
            
            // 将过期的密钥状态更新为失效
            LambdaUpdateWrapper<SecurityKey> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(SecurityKey::getStatus, 0)
                        .set(SecurityKey::getUpdateTime, LocalDateTime.now())
                        .eq(SecurityKey::getStatus, 1)
                        .lt(SecurityKey::getExpiryTime, LocalDateTime.now());
            
            int updatedCount = securityKeyMapper.update(null, updateWrapper);
            log.info("Cleaned {} expired keys", updatedCount);
            
        } catch (Exception e) {
            log.error("Error while cleaning expired keys", e);
        }
    }
}
