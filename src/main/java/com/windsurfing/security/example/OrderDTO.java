package com.windsurfing.security.example;

import com.windsurfing.security.annotation.Encrypt;
import com.windsurfing.security.annotation.Digest;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 订单数据传输对象
 * 展示如何使用摘要进行关联查询
 */
@Data
public class OrderDTO {
    private Long id;
    
    private BigDecimal amount;
    
    @Encrypt(keyId = "PHONE_KEY")
    @Digest(index = true)
    private String userPhone;  // 用户手机号（加密存储）
    
    private String userPhoneDigest;  // 用户手机号摘要（用于关联查询）
    
    private String orderNo;
}
