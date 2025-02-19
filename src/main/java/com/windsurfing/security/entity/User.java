package com.windsurfing.security.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.windsurfing.security.annotation.Decrypt;
import com.windsurfing.security.annotation.Encrypt;
import com.windsurfing.security.annotation.Mask;
import com.windsurfing.security.annotation.Digest;
import lombok.Data;

@Data
@TableName("user")
public class User {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    @Mask(type = Mask.MaskType.PHONE)
    @Decrypt
    @Encrypt(keyId = "PHONE_KEY")
    @Digest
    private String phone;
    
    @Mask(type = Mask.MaskType.EMAIL)
    @Decrypt
    @Encrypt(keyId = "EMAIL_KEY")
    @Digest
    private String email;
    
    @Mask(type = Mask.MaskType.ID_CARD)
    @Decrypt
    @Encrypt(keyId = "ID_CARD_KEY")
    @Digest
    private String idCard;
    
    private String password;
    
    @TableField(fill = FieldFill.INSERT)
    private String phoneDigest;
    
    @TableField(fill = FieldFill.INSERT)
    private String idCardDigest;
    
    @TableField(fill = FieldFill.INSERT)
    private String emailDigest;
    
    @TableLogic
    private Integer deleted;
}
