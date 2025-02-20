package com.livelab.security.starter.util;

import org.springframework.util.DigestUtils;

public class DigestUtil {
    
    public static String md5Digest(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(content.getBytes());
    }
    
    public static boolean matches(String content, String digest) {
        if (content == null || digest == null) {
            return false;
        }
        String calculatedDigest = md5Digest(content);
        return digest.equals(calculatedDigest);
    }
}
