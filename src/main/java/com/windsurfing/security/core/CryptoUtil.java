package com.windsurfing.security.core;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
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
    
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    
    private final KeyManager keyManager;
    
    public CryptoUtil(KeyManager keyManager) {
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
            
            Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");
            SecretKeySpec keySpec = new SecretKeySpec(Base64.getDecoder().decode(key), "SM4");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            byte[] encrypted = cipher.doFinal(content.getBytes());
            String encryptedContent = Base64.getEncoder().encodeToString(encrypted);
            
            // 返回格式：$keyId$encryptedContent
            String result = String.format("$%s$%s", keyId, encryptedContent);
            log.debug("Encrypted content with keyId: {}, result: {}", keyId, result);
            
            return result;
        } catch (Exception e) {
            log.error("Failed to encrypt content", e);
            throw new RuntimeException("Failed to encrypt content: " + e.getMessage(), e);
        }
    }
    
    /**
     * 解密数据
     * @param encryptedContent 加密的内容（格式：$keyId$加密后内容）
     * @return 解密后的原文
     */
    public String decrypt(String encryptedContent) {
        if (encryptedContent == null || encryptedContent.isEmpty()) {
            return encryptedContent;
        }

        try {
            // 解析格式：$keyId$encryptedContent
            String[] parts = encryptedContent.split("\\$", 3);
            if (parts.length != 3 || !encryptedContent.startsWith("$")) {
                log.error("Invalid encrypted content format: {}", encryptedContent);
                throw new IllegalArgumentException("Invalid encrypted content format");
            }

            String keyId = parts[1];
            String content = parts[2];

            log.debug("Decrypting content with keyId: {}, content: {}", keyId, content);

            String key = keyManager.getActiveKey(keyId);
            if (key == null) {
                throw new IllegalArgumentException("No key found for keyId: " + keyId);
            }

            log.debug("Using key for decryption: {}", key);

            Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");
            SecretKeySpec keySpec = new SecretKeySpec(Base64.getDecoder().decode(key), "SM4");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(content));
            String result = new String(decrypted);
            
            log.debug("Decrypted content: {}", result);
            
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Invalid encrypted content format: {}", encryptedContent);
            throw e;
        } catch (Exception e) {
            log.error("Failed to decrypt content: {}", encryptedContent, e);
            throw new RuntimeException("Failed to decrypt content: " + e.getMessage(), e);
        }
    }
}
