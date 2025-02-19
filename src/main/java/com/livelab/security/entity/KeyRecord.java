package com.livelab.security.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 密钥记录实体
 * 用于存储密钥的版本历史和状态信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("key_record")
public class KeyRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 密钥ID（如：PHONE_KEY, EMAIL_KEY等）
     */
    private String keyId;
    
    /**
     * 密钥版本号（格式：yyyyMMddHHmmss）
     */
    private String version;
    
    /**
     * 密钥内容（Base64编码）
     */
    private String content;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 生效时间
     */
    private LocalDateTime effectiveTime;
    
    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
    
    /**
     * 是否是当前活跃的密钥
     */
    private Boolean active;
    
    /**
     * 密钥状态：ACTIVE-活跃, INACTIVE-不活跃, EXPIRED-已过期
     */
    private String status;
    
    /**
     * 创建者
     */
    private String creator;
    
    /**
     * 备注
     */
    private String remark;
}
