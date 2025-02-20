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
import java.util.UUID;

@Slf4j
public class KeyManager {
    private final SecurityProperties properties;
    private final SecurityKeyMapper securityKeyMapper;
    private static final String GLOBAL_KEY_TYPE = "GLOBAL_KEY";
    private static final long KEY_EXPIRE_MINUTES = 2L;
    private volatile String currentKey;
    
    public KeyManager(SecurityProperties properties, SecurityKeyMapper securityKeyMapper) {
        this.properties = properties;
        this.securityKeyMapper = securityKeyMapper;
    }

    @Transactional
    public String getKey(String keyType) {
        // 不再区分keyType，统一使用全局密钥
        if (currentKey != null) {
            // 检查当前密钥是否在数据库中仍然有效
            SecurityKey securityKey = securityKeyMapper.selectOne(
                new LambdaQueryWrapper<SecurityKey>()
                    .eq(SecurityKey::getKeyType, GLOBAL_KEY_TYPE)
                    .eq(SecurityKey::getKeyValue, currentKey)
                    .eq(SecurityKey::getStatus, 1)
                    .le(SecurityKey::getEffectiveTime, LocalDateTime.now())
                    .ge(SecurityKey::getExpiryTime, LocalDateTime.now())
                    .last("LIMIT 1")
            );
            if (securityKey != null) {
                return currentKey;
            }
        }

        // 从数据库获取最新的有效密钥
        SecurityKey securityKey = securityKeyMapper.selectOne(
            new LambdaQueryWrapper<SecurityKey>()
                .eq(SecurityKey::getKeyType, GLOBAL_KEY_TYPE)
                .eq(SecurityKey::getStatus, 1)
                .le(SecurityKey::getEffectiveTime, LocalDateTime.now())
                .ge(SecurityKey::getExpiryTime, LocalDateTime.now())
                .orderByDesc(SecurityKey::getId)
                .last("LIMIT 1")
        );

        if (securityKey != null) {
            currentKey = securityKey.getKeyValue();
            return currentKey;
        }

        // 如果没有有效的密钥，生成新密钥
        return generateAndSaveNewKey();
    }

    @Transactional
    public String generateAndSaveNewKey() {
        String newKey = UUID.randomUUID().toString().replace("-", "");
        SecurityKey securityKey = new SecurityKey();
        securityKey.setKeyType(GLOBAL_KEY_TYPE);
        securityKey.setKeyValue(newKey);
        securityKey.setStatus(1);
        securityKey.setEffectiveTime(LocalDateTime.now());
        securityKey.setExpiryTime(LocalDateTime.now().plusMinutes(KEY_EXPIRE_MINUTES));
        securityKey.setCreateTime(LocalDateTime.now());
        securityKey.setUpdateTime(LocalDateTime.now());
        
        securityKeyMapper.insert(securityKey);
        currentKey = newKey;
        return newKey;
    }

    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    @Transactional
    public void cleanExpiredKeys() {
        try {
            log.info("Starting to clean expired keys...");
            
            // 将过期的密钥状态更新为失效
            LambdaUpdateWrapper<SecurityKey> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(SecurityKey::getStatus, 0)
                        .set(SecurityKey::getUpdateTime, LocalDateTime.now())
                        .eq(SecurityKey::getStatus, 1)
                        .lt(SecurityKey::getExpiryTime, LocalDateTime.now());
            
            int updatedCount = securityKeyMapper.update(null, updateWrapper);
            log.info("Cleaned {} expired keys", updatedCount);
            
            // 如果当前密钥已过期，生成新密钥
            if (currentKey != null) {
                SecurityKey currentKeyInfo = securityKeyMapper.selectOne(
                    new LambdaQueryWrapper<SecurityKey>()
                        .eq(SecurityKey::getKeyType, GLOBAL_KEY_TYPE)
                        .eq(SecurityKey::getKeyValue, currentKey)
                        .eq(SecurityKey::getStatus, 1)
                );
                if (currentKeyInfo == null || currentKeyInfo.getExpiryTime().isBefore(LocalDateTime.now())) {
                    generateAndSaveNewKey();
                }
            }
        } catch (Exception e) {
            log.error("Error while cleaning expired keys", e);
        }
    }
}
