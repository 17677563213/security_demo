package com.livelab.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.livelab.security.starter.annotation.Decrypt;
import com.livelab.security.starter.annotation.Digest;
import com.livelab.security.starter.annotation.Encrypt;
import com.livelab.security.starter.annotation.Mask;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    
    private String password;

    @Encrypt(keyType = "PHONE_KEY")
    @Decrypt
    @Digest
    @Mask(type = Mask.MaskType.PHONE)
    private String phone;

    @Encrypt(keyType = "EMAIL_KEY")
    @Decrypt
    @Digest
    @Mask(type = Mask.MaskType.EMAIL)
    private String email;

    @Encrypt(keyType = "ID_CARD_KEY")
    @Decrypt
    @Digest
    @Mask(type = Mask.MaskType.ID_CARD)
    private String idCard;

    @JsonIgnore
    @TableField("phone_digest")
    private String phoneDigest;

    @JsonIgnore
    @TableField("email_digest")
    private String emailDigest;

    @JsonIgnore
    @TableField("id_card_digest")
    private String idCardDigest;

    private Integer deleted;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
