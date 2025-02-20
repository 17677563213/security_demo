package com.livelab.security.starter.core;

import com.livelab.security.starter.exception.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Arrays;
import java.util.Base64;

@Slf4j
public class CryptoUtil {
    private static final String SEPARATOR = "$";
    private static final int SM4_KEY_LENGTH = 16; // SM4 requires 128-bit (16-byte) key
    private final KeyManager keyManager;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public CryptoUtil(KeyManager keyManager) {
        this.keyManager = keyManager;
    }

    private byte[] processKey(String key) {
        if (key == null || key.isEmpty()) {
            throw new SecurityException("Key cannot be null or empty");
        }

        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        
        // If key length is exactly 16 bytes, use it as is
        if (keyBytes.length == SM4_KEY_LENGTH) {
            return keyBytes;
        }
        
        // If key is shorter than 16 bytes, pad it with zeros
        // If key is longer than 16 bytes, truncate it
        byte[] processedKey = new byte[SM4_KEY_LENGTH];
        System.arraycopy(keyBytes, 0, processedKey, 0, Math.min(keyBytes.length, SM4_KEY_LENGTH));
        
        return processedKey;
    }

    public String encrypt(String content, String keyType) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        try {
            String key = keyManager.getKey(keyType);
            byte[] processedKey = processKey(key);
            SecretKeySpec skeySpec = new SecretKeySpec(processedKey, "SM4");
            Cipher cipher = Cipher.getInstance("SM4/ECB/PKCS5Padding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return SEPARATOR + keyType + SEPARATOR + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("Encryption failed for content with keyType: {}", keyType, e);
            throw new SecurityException("Encryption failed", e);
        }
    }

    public String decrypt(String content) {
        if (content == null || content.isEmpty() || !content.startsWith(SEPARATOR)) {
            return content;
        }

        try {
            String[] parts = content.split("\\" + SEPARATOR);
            if (parts.length != 3) {
                log.error("Invalid encrypted content format: {}", content);
                throw new SecurityException("Invalid encrypted content format");
            }

            String keyType = parts[1];
            String encryptedContent = parts[2];
            String key = keyManager.getKey(keyType);
            byte[] processedKey = processKey(key);

            SecretKeySpec skeySpec = new SecretKeySpec(processedKey, "SM4");
            Cipher cipher = Cipher.getInstance("SM4/ECB/PKCS5Padding", "BC");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedContent));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Decryption failed for content: {}", content, e);
            throw new SecurityException("Decryption failed", e);
        }
    }
}
