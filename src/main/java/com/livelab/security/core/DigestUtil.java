package com.livelab.security.core;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.MessageDigest;
import java.security.Security;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 摘要计算工具类
 * 使用SM3算法计算摘要，并添加salt防止彩虹表攻击
 */
@Component
public class DigestUtil {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Value("${security.digest.salt}")
    private String salt;

    @Value("${security.digest.algorithm}")
    private String algorithm;

    private static final Logger log = LoggerFactory.getLogger(DigestUtil.class);

    /**
     * 计算字符串的摘要值
     * @param content 待计算的内容
     * @return Base64编码的摘要值
     */
    public String calculateDigest(String content) {
        try {
            if (content == null) {
                return null;
            }

            log.info("Calculating digest for content: {}", content);
            log.info("Using salt: {}", salt);
            log.info("Using algorithm: {}", algorithm);
            
            // 将salt和内容组合
            String contentWithSalt = content + salt;
            log.info("Content with salt: {}", contentWithSalt);

            // 使用SM3计算摘要
            MessageDigest digest = MessageDigest.getInstance(algorithm, "BC");
            byte[] hash = digest.digest(contentWithSalt.getBytes(StandardCharsets.UTF_8));

            // 使用Base64编码
            String result = Base64.getEncoder().encodeToString(hash);
            log.info("Generated digest: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Failed to calculate digest", e);
            throw new RuntimeException("Failed to calculate digest", e);
        }
    }

    /**
     * 验证内容与摘要是否匹配
     * @param content 原始内容
     * @param digestValue 摘要值
     * @return 是否匹配
     */
    public boolean verifyDigest(String content, String digestValue) {
        if (content == null || digestValue == null) {
            return false;
        }
        String calculatedDigest = calculateDigest(content);
        return digestValue.equals(calculatedDigest);
    }
}
