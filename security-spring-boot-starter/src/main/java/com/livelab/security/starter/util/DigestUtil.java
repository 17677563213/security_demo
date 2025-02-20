package com.livelab.security.starter.util;

import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.digest.SM3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DigestUtil {
    
    @Value("${security.digest.salt}")
    private String salt;

    @Value("${security.digest.algorithm}")
    private String algorithm;
    
    /**
     * 根据配置的算法计算摘要
     * @param content 原始内容
     * @return 摘要值
     */
    public String digest(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        // 加盐处理
        String contentWithSalt = content + salt;
        
        if ("sm3".equalsIgnoreCase(algorithm)) {
            SM3 sm3 = SmUtil.sm3();
            return sm3.digestHex(contentWithSalt);
        } else {
            throw new IllegalArgumentException("Unsupported digest algorithm: " + algorithm);
        }
    }
    
    /**
     * 验证内容与摘要是否匹配
     * @param content 原始内容
     * @param digest 摘要值
     * @return 是否匹配
     */
    public boolean matches(String content, String digest) {
        if (content == null || digest == null) {
            return false;
        }
        String calculatedDigest = digest(content);
        return digest.equals(calculatedDigest);
    }
}
