package com.livelab.security.starter.aspect;

import com.livelab.security.starter.annotation.Mask;
import com.livelab.security.starter.core.MaskUtil;
import com.livelab.security.starter.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Collection;

@Slf4j
@Aspect
@Order(2)  // 在DataSecurityAspect(Order=1)之后执行
@Component
public class DataMaskAspect {

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object handleMask(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        
        try {
            if (result instanceof ApiResponse) {
                Object data = ((ApiResponse<?>) result).getData();
                if (data instanceof Collection) {
                    for (Object item : (Collection<?>) data) {
                        maskFields(item);
                    }
                } else if (data != null) {
                    maskFields(data);
                }
            }
        } catch (Exception e) {
            log.error("Error processing result in mask aspect", e);
        }
        
        return result;
    }

    private void maskFields(Object obj) {
        if (obj == null) {
            return;
        }

        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            try {
                Mask mask = field.getAnnotation(Mask.class);
                if (mask != null) {
                    field.setAccessible(true);
                    Object value = field.get(obj);
                    if (value instanceof String) {
                        String maskedValue = MaskUtil.maskValue((String) value, mask.type(), mask.pattern());
                        field.set(obj, maskedValue);
                    }
                }
            } catch (Exception e) {
                log.error("Error masking field: " + field.getName(), e);
            }
        }
    }
}
