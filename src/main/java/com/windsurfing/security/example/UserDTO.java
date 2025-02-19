package com.windsurfing.security.example;

import com.windsurfing.security.annotation.Decrypt;
import com.windsurfing.security.annotation.Encrypt;
import lombok.Data;

/**
 * 用户数据传输对象
 * 展示加密解密注解的使用方式
 */
@Data
public class UserDTO {
    private Long id;
    
    private String username;
    
    @Encrypt(keyId = "PHONE_KEY")
    @Decrypt
    private String phone;
    
    @Encrypt(keyId = "ID_CARD_KEY")
    @Decrypt
    private String idCard;
}
