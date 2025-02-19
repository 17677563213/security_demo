package com.windsurfing.security.annotation;

import java.lang.annotation.*;

/**
 * 解密注解
 * 用于标记需要解密的字段
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Decrypt {
}
