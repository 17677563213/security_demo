package com.livelab.security.annotation;

import java.lang.annotation.*;

/**
 * 摘要注解
 * 用于标记需要生成摘要的字段
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Digest {
    /**
     * 是否需要建立索引，默认需要
     */
    boolean index() default true;
}
