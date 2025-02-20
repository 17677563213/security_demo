package com.livelab.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.livelab.security.starter.annotation.Decrypt;
import com.livelab.security.starter.annotation.Digest;
import com.livelab.security.starter.annotation.Encrypt;
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
    private String phone;

    @Encrypt(keyType = "EMAIL_KEY")
    @Decrypt
    @Digest
    private String email;

    @Encrypt(keyType = "ID_CARD_KEY")
    @Decrypt
    @Digest
    private String idCard;

    @TableField("phone_digest")
    private String phoneDigest;

    @TableField("email_digest")
    private String emailDigest;

    @TableField("id_card_digest")
    private String idCardDigest;

    private Integer deleted;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
