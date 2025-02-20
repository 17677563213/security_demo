package com.livelab.security.starter.core;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CryptoUtil {
    private final KeyManager keyManager;

    public CryptoUtil(KeyManager keyManager) {
        this.keyManager = keyManager;
    }

    public String encrypt(String content) {
        try {
            SecurityKey currentKey = keyManager.getCurrentKey();
            byte[] key = Base64.decode(currentKey.getKeyValue());
            SymmetricCrypto sm4 = SmUtil.sm4(key);
            String encryptedContent = sm4.encryptBase64(content);
            return currentKey.getId() + "$" + encryptedContent;
        } catch (Exception e) {
            log.error("Encryption failed", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String content) {
        try {
            String[] parts = content.split("\\$", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid encrypted content format");
            }

            String keyId = parts[0];
            String encryptedContent = parts[1];
            
            SecurityKey key = keyManager.getKeyById(keyId);
            if (key == null) {
                throw new IllegalStateException("Key not found: " + keyId);
            }

            byte[] keyBytes = Base64.decode(key.getKeyValue());
            SymmetricCrypto sm4 = SmUtil.sm4(keyBytes);
            return sm4.decryptStr(encryptedContent);
        } catch (Exception e) {
            log.error("Decryption failed", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
