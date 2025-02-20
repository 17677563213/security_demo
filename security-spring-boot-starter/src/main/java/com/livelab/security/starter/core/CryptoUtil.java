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

    /**
     * 处理密钥以确保其符合SM4算法的要求（16字节长度）
     * 
     * 处理规则：
     * 1. 如果密钥长度正好是16字节，直接使用
     * 2. 如果密钥短于16字节，在末尾补充0
     * 3. 如果密钥长于16字节，截取前16字节
     * 
     * @param key 原始密钥字符串
     * @return 处理后的16字节密钥
     * @throws SecurityException 当输入密钥为null或空字符串时抛出
     */
    private byte[] processKey(String key) {
        // 参数校验：确保密钥不为null且不为空字符串
        if (key == null || key.isEmpty()) {
            throw new SecurityException("Key cannot be null or empty");
        }

        // 将输入字符串转换为UTF-8编码的字节数组
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        
        // 如果密钥长度恰好是SM4要求的16字节，直接返回
        if (keyBytes.length == SM4_KEY_LENGTH) { 
            return keyBytes;
        }
        
        // 创建16字节的结果数组
        // 如果原始密钥较短，数组剩余部分会自动填充为0
        // 如果原始密钥较长，将只复制前16字节
        byte[] processedKey = new byte[SM4_KEY_LENGTH];
        
        // 使用System.arraycopy进行数组复制
        // 参数说明：
        // - keyBytes: 源数组
        // - 0: 源数组的起始位置
        // - processedKey: 目标数组
        // - 0: 目标数组的起始位置
        // - Math.min(keyBytes.length, SM4_KEY_LENGTH): 复制的长度，取源数组长度和16的较小值
        System.arraycopy(keyBytes, 0, processedKey, 0, Math.min(keyBytes.length, SM4_KEY_LENGTH));
        
        return processedKey;
    }

    /**
     * 使用SM4算法加密内容，并将密钥ID和加密结果组合成特定格式
     * 
     * 加密流程：
     * 1. 从密钥管理器获取当前有效的密钥信息
     * 2. 使用SM4/ECB/PKCS5Padding方式进行加密
     * 3. 将结果格式化为: $密钥ID$Base64编码的加密内容
     * 
     * @param content 需要加密的原文内容
     * @return 格式化的加密结果，格式为：$密钥ID$加密内容
     *         如果输入为null或空字符串，则直接返回输入值
     * @throws SecurityException 当加密过程发生错误时抛出
     */
    public String encrypt(String content) {
        // 空值校验：如果内容为null或空字符串，直接返回原值
        if (content == null || content.isEmpty()) {
            return content;
        }

        try {
            // 从密钥管理器获取当前有效的密钥信息（包含密钥ID和密钥值）
            KeyInfo keyInfo = keyManager.getKeyInfo();
            // 对密钥进行处理（如填充或截断），确保符合SM4算法要求
            byte[] processedKey = processKey(keyInfo.getKeyValue());

            // 创建SM4算法的密钥规范
            SecretKeySpec skeySpec = new SecretKeySpec(processedKey, "SM4");
            // 获取SM4算法实例，使用ECB模式和PKCS5Padding填充
            // BC表示使用BouncyCastle作为加密提供者
            Cipher cipher = Cipher.getInstance("SM4/ECB/PKCS5Padding", "BC");
            // 初始化加密模式
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            // 将原文转换为UTF-8编码的字节数组，然后进行加密
            byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
            
            // 组装最终结果：$密钥ID$Base64编码的加密内容
            // 使用分隔符将密钥ID和加密内容分开，方便后续解密时提取
            return SEPARATOR + keyInfo.getId() + SEPARATOR + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            // 将加密过程中的异常包装为SecurityException并抛出
            throw new SecurityException("Encryption failed", e);
        }
    }

    /**
     * 使用SM4算法解密加密内容
     *
     * @return 解密后的明文，如果输入为空则直接返回输入值
     * @throws SecurityException 当解密过程发生错误时抛出
     */
    public String decrypt(String encryptedValue) {
        // 解析密钥类型和加密内容
        Long keyIde = Long.valueOf(encryptedValue.substring(1, encryptedValue.indexOf("$", 1)));
        String encryptedContent = encryptedValue.substring(encryptedValue.indexOf("$", 1) + 1);

        // 空值校验：如果加密内容为null或空字符串，直接返回原值
        if (encryptedContent == null || encryptedContent.isEmpty()) {
            return encryptedContent;
        }

        try {
            // 从密钥管理器获取指定ID的密钥值
            String key = keyManager.getKeyValueById(keyIde); 
            // 对密钥进行处理（如填充或截断），确保符合SM4算法要求
            byte[] processedKey = processKey(key);

            // 创建SM4算法的密钥规范
            SecretKeySpec skeySpec = new SecretKeySpec(processedKey, "SM4");
            // 获取SM4算法实例，使用ECB模式和PKCS5Padding填充
            // BC表示使用BouncyCastle作为加密提供者
            Cipher cipher = Cipher.getInstance("SM4/ECB/PKCS5Padding", "BC");
            // 初始化解密模式
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            // 将Base64编码的加密内容解码为字节数组，然后进行解密
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedContent));
            // 将解密后的字节数组转换为UTF-8编码的字符串
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 记录解密失败的错误日志，包含加密内容和异常信息
            log.error("Decryption failed for content: {}", encryptedContent, e);
            // 将异常包装为SecurityException并抛出
            throw new SecurityException("Decryption failed", e);
        }
    }
}
