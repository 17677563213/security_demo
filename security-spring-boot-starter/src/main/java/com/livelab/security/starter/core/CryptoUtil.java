package com.livelab.security.starter.core;

import com.livelab.security.starter.entity.SecurityKey;
import com.livelab.security.starter.exception.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Base64;

@Slf4j
public class CryptoUtil {
    private static final String ALGORITHM = "SM4/ECB/PKCS5Padding";
    private static final String SEPARATOR = ":";
    private final KeyManager keyManager;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public CryptoUtil(KeyManager keyManager) {
        this.keyManager = keyManager;
    }

    public String encrypt(String content) {
        try {
            SecurityKey currentKey = keyManager.getCurrentKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");
            SecretKeySpec keySpec = new SecretKeySpec(currentKey.getKeyValue().getBytes(StandardCharsets.UTF_8), "SM4");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
            String encryptedBase64 = Base64.getEncoder().encodeToString(encrypted);
            
            // 拼接密钥ID和加密内容
            return currentKey.getId() + SEPARATOR + encryptedBase64;
            
        } catch (Exception e) {
            log.error("Encryption failed", e);
            throw new SecurityException("Encryption failed", e);
        }
    }

    public String decrypt(String encryptedContent) {
        if (encryptedContent == null || encryptedContent.isEmpty()) {
            return encryptedContent;
        }

        try {
            String[] parts = encryptedContent.split(SEPARATOR);
            if (parts.length != 2) {
                log.error("Invalid encrypted content format");
                return encryptedContent;
            }

            Long keyId = Long.parseLong(parts[0]);
            String encryptedBase64 = parts[1];
            
            SecurityKey key = keyManager.getKeyById(keyId);
            
            Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");
            SecretKeySpec keySpec = new SecretKeySpec(key.getKeyValue().getBytes(StandardCharsets.UTF_8), "SM4");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedBase64);
            byte[] decrypted = cipher.doFinal(encryptedBytes);
            return new String(decrypted, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.error("Decryption failed", e);
            return encryptedContent;
        }
    }
}
