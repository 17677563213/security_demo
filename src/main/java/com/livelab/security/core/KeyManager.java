package com.livelab.security.core;

import com.livelab.security.entity.KeyRecord;
import com.livelab.security.mapper.KeyRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 密钥管理器
 * 支持密钥自动更新、版本管理和手动刷新
 */
@Slf4j
@Component
public class KeyManager {
    
    @Autowired
    private KeyRecordMapper keyRecordMapper;
    
    /**
     * 密钥有效期（分钟）
     */
    @Value("${security.key.expireMinutes:1}")
    private int keyExpireMinutes;
    
    /**
     * 密钥配置，格式：{keyId=base64EncodedKey}
     */
    @Value("${security.keys}")
    private String keyConfigStr;
    
    private Map<String, String> keyConfig;
    
    @PostConstruct
    @Transactional
    public void init() {
        // 解析密钥配置
        keyConfig = parseKeyConfig(keyConfigStr);
        
        // 初始化所有密钥
        keyConfig.forEach((keyId, initialKey) -> {
            KeyRecord activeKey = keyRecordMapper.getActiveKey(keyId);
            if (activeKey == null) {
                createNewKeyVersion(keyId, initialKey);
            }
        });
        log.info("KeyManager initialized successfully");
    }
    
    private Map<String, String> parseKeyConfig(String configStr) {
        Map<String, String> result = new HashMap<>();
        // 移除首尾的大括号
        configStr = configStr.trim();
        if (configStr.startsWith("{")) {
            configStr = configStr.substring(1);
        }
        if (configStr.endsWith("}")) {
            configStr = configStr.substring(0, configStr.length() - 1);
        }
        
        // 解析键值对
        String[] pairs = configStr.split(",");
        for (String pair : pairs) {
            String[] parts = pair.trim().split(":");
            if (parts.length == 2) {
                String key = parts[0].trim();
                String value = parts[1].trim();
                // 移除键的引号
                if (key.startsWith("'") || key.startsWith("\"")) {
                    key = key.substring(1);
                }
                if (key.endsWith("'") || key.endsWith("\"")) {
                    key = key.substring(0, key.length() - 1);
                }
                // 移除值的引号
                if (value.startsWith("'") || value.startsWith("\"")) {
                    value = value.substring(1);
                }
                if (value.endsWith("'") || value.endsWith("\"")) {
                    value = value.substring(0, value.length() - 1);
                }
                result.put(key, value);
            }
        }
        return result;
    }
    
    /**
     * 获取当前活跃的密钥
     * 如果密钥过期，会自动创建新密钥
     */
    public String getActiveKey(String keyId) {
        KeyRecord activeKey = keyRecordMapper.getActiveKey(keyId);
        if (activeKey == null) {
            log.error("No active key found for keyId: {}", keyId);
            throw new IllegalArgumentException("Invalid keyId: " + keyId);
        }
        
        // 如果密钥过期，创建新密钥
        if (LocalDateTime.now().isAfter(activeKey.getExpireTime())) {
            log.info("Key {} has expired, creating new version", keyId);
            String newKey = generateNewKey();
            createNewKeyVersion(keyId, newKey);
            activeKey = keyRecordMapper.getActiveKey(keyId);
        }
        
        return activeKey.getContent();
    }
    
    /**
     * 获取当前活跃密钥的版本号
     */
    public String getActiveVersion(String keyId) {
        KeyRecord activeKey = keyRecordMapper.getActiveKey(keyId);
        if (activeKey == null) {
            log.error("No active key found for keyId: {}", keyId);
            throw new IllegalArgumentException("Invalid keyId: " + keyId);
        }
        return activeKey.getVersion();
    }
    
    /**
     * 根据密钥ID和版本号获取历史密钥
     */
    public String getKeyByVersion(String keyId, String version) {
        KeyRecord keyRecord = keyRecordMapper.getKeyByVersion(keyId, version);
        if (keyRecord == null) {
            log.error("No key found for version: {} of keyId: {}", version, keyId);
            throw new IllegalArgumentException("Invalid version: " + version);
        }
        return keyRecord.getContent();
    }
    
    /**
     * 强制刷新指定的密钥
     */
    @Transactional
    public void forceRefreshKey(String keyId) {
        // 将当前所有密钥设置为非活跃
        keyRecordMapper.deactivateAllKeys(keyId);
        
        // 创建新密钥
        String newKey = generateNewKey();
        createNewKeyVersion(keyId, newKey);
        
        log.info("Key {} has been forcefully refreshed", keyId);
    }
    
    /**
     * 定期检查并更新过期的密钥
     */
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void checkAndUpdateExpiredKeys() {
        keyConfig.keySet().forEach(keyId -> {
            KeyRecord activeKey = keyRecordMapper.getActiveKey(keyId);
            if (activeKey != null && LocalDateTime.now().isAfter(activeKey.getExpireTime())) {
                log.info("Key {} has expired, creating new version", keyId);
                forceRefreshKey(keyId);
            }
        });
    }
    
    /**
     * 创建新的密钥版本
     */
    @Transactional
    protected void createNewKeyVersion(String keyId, String key) {
        LocalDateTime now = LocalDateTime.now();
        KeyRecord newKey = KeyRecord.builder()
                .keyId(keyId)
                .version(generateVersion())
                .content(key)
                .createTime(now)
                .effectiveTime(now)
                .expireTime(now.plusMinutes(keyExpireMinutes))
                .active(true)
                .status("ACTIVE")
                .creator("SYSTEM")
                .remark("Auto generated key")
                .build();
        
        keyRecordMapper.insert(newKey);
        log.info("Created new key version {} for keyId {}", newKey.getVersion(), keyId);
    }
    
    /**
     * 生成新的密钥
     * 这里使用UUID作为示例，实际应用中应该使用更安全的密钥生成算法
     */
    private String generateNewKey() {
        return Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
    }
    
    /**
     * 生成版本号
     * 格式：yyyyMMddHHmmss
     */
    private String generateVersion() {
        return String.format("%tY%<tm%<td%<tH%<tM%<tS", new Date());
    }
    
    /**
     * 获取指定密钥的所有历史记录
     */
    public List<KeyRecord> getKeyHistory(String keyId) {
        return keyRecordMapper.getKeyHistory(keyId);
    }
}
