package com.windsurfing.security.annotation;

import java.lang.annotation.*;

/**
 * 数据脱敏注解
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Mask {
    /**
     * 脱敏类型
     */
    MaskType type();
    
    /**
     * 是否在返回前端时脱敏
     */
    boolean maskInResponse() default true;
    
    enum MaskType {
        PHONE,      // 手机号
        ID_CARD,    // 身份证号
        BANK_CARD,  // 银行卡号
        NAME,       // 姓名
        EMAIL       // 邮箱
    }
}
