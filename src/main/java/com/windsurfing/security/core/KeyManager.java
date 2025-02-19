package com.windsurfing.security.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.PostConstruct;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 密钥管理中心
 * 负责密钥的生成、存储和管理
 */
@Slf4j
@Component
@Configuration
@ConfigurationProperties(prefix = "security.crypto")
public class KeyManager {
    
    // 存储当前有效的密钥
    private Map<String, String> activeKeys = new ConcurrentHashMap<>();
    // 存储历史密钥
    private Map<String, String> historicalKeys = new ConcurrentHashMap<>();
    // 从配置文件中读取的密钥
    private Map<String, String> keys = new ConcurrentHashMap<>();
    
    private String algorithm;
    private String mode;
    private String padding;
    
    public void setKeys(Map<String, String> keys) {
        this.keys = keys;
    }
    
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
    
    public void setMode(String mode) {
        this.mode = mode;
    }
    
    public void setPadding(String padding) {
        this.padding = padding;
    }
    
    /**
     * 获取当前有效的加密密钥
     * @param keyId 密钥ID
     * @return 密钥内容
     */
    public String getActiveKey(String keyId) {
        String key = activeKeys.get(keyId);
        if (key == null) {
            // 如果activeKeys中没有，尝试从配置的keys中获取
            key = keys.get(keyId);
            if (key != null) {
                // 将配置的key放入activeKeys
                activeKeys.put(keyId, key);
                log.debug("Loaded key for keyId: {} from configuration", keyId);
            } else {
                log.warn("No key found for keyId: {}", keyId);
            }
        }
        return key;
    }
    
    /**
     * 获取历史密钥
     * @param keyId 密钥ID
     * @return 密钥内容
     */
    public String getHistoricalKey(String keyId) {
        return historicalKeys.get(keyId);
    }
    
    /**
     * 生成新的密钥
     * @param keyId 密钥ID
     * @return 新生成的密钥
     */
    public String generateNewKey(String keyId) {
        try {
            // 如果存在当前密钥，将其移动到历史密钥中
            String currentKey = activeKeys.get(keyId);
            if (currentKey != null) {
                historicalKeys.put(keyId, currentKey);
                log.debug("Moved current key to historical keys for keyId: {}", keyId);
            }
            
            // 生成新的密钥
            String newKey = generateSecureKey();
            activeKeys.put(keyId, newKey);
            log.debug("Generated new key for keyId: {}", keyId);
            return newKey;
        } catch (Exception e) {
            log.error("Failed to generate new key for keyId: {}", keyId, e);
            throw new RuntimeException("Failed to generate new key", e);
        }
    }
    
    /**
     * 生成安全的密钥
     * 注意：这里仅作示例，实际应使用更安全的密钥生成方式
     */
    private String generateSecureKey() {
        try {
            // 使用SM4算法要求的密钥长度（128位）
            byte[] key = new byte[16];
            // 使用安全随机数生成器生成密钥
            new java.security.SecureRandom().nextBytes(key);
            String base64Key = Base64.getEncoder().encodeToString(key);
            log.debug("Generated new secure key");
            return base64Key;
        } catch (Exception e) {
            log.error("Failed to generate secure key", e);
            throw new RuntimeException("Failed to generate secure key", e);
        }
    }
    
    @PostConstruct
    public void init() {
        try {
            log.info("Initializing KeyManager with {} keys", keys.size());
            // 确保所有密钥都是Base64编码的
            for (Map.Entry<String, String> entry : keys.entrySet()) {
                String keyId = entry.getKey();
                String key = entry.getValue();
                try {
                    // 尝试Base64解码，如果失败则进行编码
                    Base64.getDecoder().decode(key);
                    log.debug("Key for {} is already in Base64 format", keyId);
                } catch (IllegalArgumentException e) {
                    // 如果不是Base64格式，则进行编码
                    String base64Key = Base64.getEncoder().encodeToString(key.getBytes());
                    log.debug("Converting key for {} to Base64 format", keyId);
                    keys.put(keyId, base64Key);
                }
                // 将配置的key放入activeKeys
                activeKeys.put(keyId, keys.get(keyId));
            }
            log.info("KeyManager initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize KeyManager", e);
            throw new RuntimeException("Failed to initialize KeyManager", e);
        }
    }
}
