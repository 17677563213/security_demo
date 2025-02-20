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

/**
 * 数据脱敏切面，负责对敏感数据进行脱敏处理
 * - 在Controller返回数据前进行脱敏
 * - 使用@Order(2)确保在数据解密(@Order(1))之后执行
 * - 支持多种脱敏类型：手机号、邮箱、身份证、自定义
 */
@Slf4j
@Aspect
@Order(2)  // 在DataSecurityAspect(Order=1)之后执行
@Component
public class DataMaskAspect {

    /**
     * 拦截Controller层的方法，对返回结果进行脱敏处理
     * - 支持对ApiResponse中的数据进行脱敏
     * - 可以处理单个对象或集合类型的数据
     *
     * @param joinPoint 切点
     * @return 处理后的结果
     * @throws Throwable 处理过程中的异常
     */
    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object handleMask(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        
        try {
            if (result instanceof ApiResponse) {
                Object data = ((ApiResponse<?>) result).getData();
                if (data instanceof Collection) {
                    // 处理集合类型的数据
                    for (Object item : (Collection<?>) data) {
                        maskFields(item);
                    }
                } else if (data != null) {
                    // 处理单个对象
                    maskFields(data);
                }
            }
        } catch (Exception e) {
            log.error("Error processing result in mask aspect", e);
        }
        
        return result;
    }

    /**
     * 处理对象的字段脱敏
     * - 查找带有@Mask注解的字段
     * - 根据注解中指定的脱敏类型进行相应的脱敏处理
     * - 支持的脱敏类型：
     *   - PHONE: 手机号，显示前3后4位，中间用****代替
     *   - EMAIL: 邮箱，显示前3后4位加域名，中间用****代替
     *   - ID_CARD: 身份证号，显示前6后4位，中间用********代替
     *   - CUSTOM: 自定义脱敏规则
     *
     * @param obj 需要脱敏的对象
     */
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
                        // 根据注解指定的类型和模式进行脱敏
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
