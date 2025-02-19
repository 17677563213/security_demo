package com.windsurfing.security.example;

import com.windsurfing.security.annotation.Decrypt;
import com.windsurfing.security.annotation.Encrypt;
import com.windsurfing.security.annotation.Digest;
import lombok.Data;

/**
 * 用户数据传输对象
 * 展示加密解密和摘要注解的使用方式
 */
@Data
public class UserWithDigestDTO {
    private Long id;
    
    private String username;
    
    @Encrypt(keyId = "PHONE_KEY")
    @Decrypt
    @Digest(index = true)
    private String phone;
    
    @Encrypt(keyId = "ID_CARD_KEY")
    @Decrypt
    @Digest(index = true)
    private String idCard;
    
    // 用于存储摘要值的字段
    private String phoneDigest;
    private String idCardDigest;
}
