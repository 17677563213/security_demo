package com.livelab.security.starter.annotation;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Mask {
    MaskType type();

    enum MaskType {
        PHONE,    // 手机号码，保留前3后4
        EMAIL,    // 邮箱，保留@前3后4和域名
        ID_CARD,  // 身份证，保留前6后4
        CUSTOM    // 自定义规则
    }

    String pattern() default "";  // 自定义脱敏规则的正则表达式
}
