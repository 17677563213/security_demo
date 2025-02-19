package com.livelab.security.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.livelab.security.annotation.Decrypt;
import com.livelab.security.annotation.Encrypt;
import com.livelab.security.annotation.Mask;
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
    private String phone;
    
    @Mask(type = Mask.MaskType.EMAIL)
    @Decrypt
    @Encrypt(keyId = "EMAIL_KEY")
    private String email;
    
    @Mask(type = Mask.MaskType.ID_CARD)
    @Decrypt
    @Encrypt(keyId = "ID_CARD_KEY")
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
