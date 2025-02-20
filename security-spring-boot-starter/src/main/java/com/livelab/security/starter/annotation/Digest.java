package com.livelab.security.starter.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Digest {
    String[] sourceFields();
}
