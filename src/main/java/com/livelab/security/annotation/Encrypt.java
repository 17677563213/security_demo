package com.livelab.security.annotation;

import java.lang.annotation.*;

/**
 * 加密注解
 * 用于标记需要加密的字段
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Encrypt {
    /**
     * 密钥ID
     */
    String keyId() default "DEFAULT_KEY";
}
