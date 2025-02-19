package com.livelab.security.core;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;
import java.util.Base64;

/**
 * 加密解密工具类
 * 实现SM4算法的加密解密操作
 */
@Slf4j
@Component
public class CryptoUtil {

    private static final String ALGORITHM = "SM4/ECB/PKCS5Padding";
    private static final String SEPARATOR = "$";
    private static final int SM4_KEY_LENGTH = 16; // SM4 requires 128-bit (16-byte) key
    
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    
    private final KeyManager keyManager;
    
    public CryptoUtil(@Lazy KeyManager keyManager) {
        this.keyManager = keyManager;
    }
    
    /**
     * 加密数据
     * @param content 待加密内容
     * @param keyId 密钥ID
     * @return 加密后的内容，格式：$keyId$加密后内容
     */
    public String encrypt(String content, String keyId) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        try {
            String key = keyManager.getActiveKey(keyId);
            if (key == null) {
                throw new IllegalArgumentException("No key found for keyId: " + keyId);
            }

            log.debug("Using key for encryption: {}", key);
            
            // 将 Base64 密钥解码为字节数组
            byte[] keyBytes = Base64.getDecoder().decode(key);
            
            // 确保密钥长度为 128 位（16 字节）
            if (keyBytes.length != SM4_KEY_LENGTH) {
                byte[] adjustedKey = new byte[SM4_KEY_LENGTH];
                System.arraycopy(keyBytes, 0, adjustedKey, 0, Math.min(keyBytes.length, SM4_KEY_LENGTH));
                keyBytes = adjustedKey;
            }
            
            Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "SM4"));
            byte[] encrypted = cipher.doFinal(content.getBytes());
            return SEPARATOR + keyId + SEPARATOR + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("Encryption failed for keyId: {}", keyId, e);
            throw new RuntimeException("Encryption failed", e);
        }
    }
    
    /**
     * 解密数据
     * @param content 待解密内容，格式：$keyId$加密后内容
     * @return 解密后的内容
     */
    public String decrypt(String content) {
        if (content == null || content.isEmpty() || !content.startsWith(SEPARATOR)) {
            return content;
        }

        try {
            String[] parts = content.split("\\$");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid encrypted content format");
            }

            String keyId = parts[1];
            String encryptedContent = parts[2];
            String key = keyManager.getActiveKey(keyId);
            if (key == null) {
                throw new IllegalArgumentException("No key found for keyId: " + keyId);
            }

            log.debug("Using key for decryption: {}", key);
            
            // 将 Base64 密钥解码为字节数组
            byte[] keyBytes = Base64.getDecoder().decode(key);
            
            // 确保密钥长度为 128 位（16 字节）
            if (keyBytes.length != SM4_KEY_LENGTH) {
                byte[] adjustedKey = new byte[SM4_KEY_LENGTH];
                System.arraycopy(keyBytes, 0, adjustedKey, 0, Math.min(keyBytes.length, SM4_KEY_LENGTH));
                keyBytes = adjustedKey;
            }
            
            Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "SM4"));
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedContent));
            return new String(decrypted);
        } catch (Exception e) {
            log.error("Decryption failed", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
