package com.livelab.security.starter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("security_key")
public class SecurityKey {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String keyType;
    
    private String keyValue;
    
    private LocalDateTime effectiveTime;
    
    private LocalDateTime expiryTime;
    
    private Integer status;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
