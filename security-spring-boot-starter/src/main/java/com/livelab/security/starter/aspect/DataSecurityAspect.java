package com.livelab.security.starter.aspect;

import com.livelab.security.starter.annotation.DataSecurity;
import com.livelab.security.starter.annotation.Decrypt;
import com.livelab.security.starter.annotation.Digest;
import com.livelab.security.starter.annotation.Encrypt;
import com.livelab.security.starter.core.CryptoUtil;
import com.livelab.security.starter.core.DigestUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 数据安全切面，处理数据的加密、解密和摘要
 * - 在数据保存前进行加密和摘要处理
 * - 在数据查询后进行解密处理
 * - 使用@Order(1)确保在数据脱敏(@Order(2))之前执行
 */
@Slf4j
@Aspect
@Order(1)  // 在DataMaskAspect之前执行
@Component
public class DataSecurityAspect {
    private final CryptoUtil cryptoUtil;
    private final DigestUtil digestUtil;

    public DataSecurityAspect(CryptoUtil cryptoUtil, DigestUtil digestUtil) {
        this.cryptoUtil = cryptoUtil;
        this.digestUtil = digestUtil;
    }

    /**
     * 拦截MyBatis-Plus的Mapper方法，处理数据的加密、解密和摘要
     * - 对insert和update方法的参数进行加密和摘要处理
     * - 对查询结果进行解密处理
     *
     * @param joinPoint 切点
     * @return 处理后的结果
     * @throws Throwable 处理过程中的异常
     */
    @Around("@annotation(com.livelab.security.starter.annotation.DataSecurity) || @within(com.livelab.security.starter.annotation.DataSecurity)")
    public Object handleDataSecurity(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("DataSecurityAspect is processing method: {}", joinPoint.getSignature().getName());
        Object[] args = joinPoint.getArgs();
        
        // 处理入参加密和摘要
        for (Object arg : args) {
            if (arg != null) {
                processObject(arg, true);
            }
        }

        // 执行原方法
        Object result = joinPoint.proceed();

        // 处理出参解密
        if (result != null) {
            processObject(result, false);
        }

        return result;
    }

    /**
     * 处理对象的加密、解密和摘要
     * - 查找带有@Encrypt注解的字段进行加密
     * - 查找带有@Decrypt注解的字段进行解密
     * - 查找带有@Digest注解的字段生成摘要
     *
     * @param obj 需要处理的对象
     * @param isRequest 是否是请求参数
     */
    private void processObject(Object obj, boolean isRequest) throws Exception {
        if (obj == null) return;
        
        // 如果是基本类型或字符串，直接返回
        if (obj.getClass().isPrimitive() || obj instanceof String || obj instanceof Number) {
            return;
        }

        Class<?> clazz = obj.getClass();
        Field[] fields = getAllFields(clazz);

        for (Field field : fields) {
            field.setAccessible(true);
            Object value = field.get(obj);
            if (value == null) continue;

            // 处理加密字段
            if (field.isAnnotationPresent(Encrypt.class) && isRequest) {
                String encryptedValue = cryptoUtil.encrypt(value.toString());
                field.set(obj, encryptedValue);
                log.debug("Encrypted field: {}", field.getName());
            }

            // 处理解密字段
            if (field.isAnnotationPresent(Decrypt.class) && !isRequest) {
                String decryptedValue = cryptoUtil.decrypt(value.toString());
                field.set(obj, decryptedValue);
                log.debug("Decrypted field: {}", field.getName());
            }

            // 处理摘要字段
            if (field.isAnnotationPresent(Digest.class) && isRequest) {
                Digest digest = field.getAnnotation(Digest.class);
                String[] sourceFields = digest.sourceFields();
                if (sourceFields.length > 0) {
                    List<String> sourceValues = new ArrayList<>();
                    for (String sourceFieldName : sourceFields) {
                        Field sourceField = clazz.getDeclaredField(sourceFieldName);
                        sourceField.setAccessible(true);
                        Object sourceValue = sourceField.get(obj);
                        if (sourceValue != null) {
                            sourceValues.add(sourceValue.toString());
                        }
                    }
                    String digestValue = digestUtil.generateDigest(String.join("", sourceValues));
                    field.set(obj, digestValue);
                    log.debug("Generated digest for field: {}", field.getName());
                }
            }
        }
    }

    /**
     * 获取所有字段，包括父类字段
     *
     * @param clazz 类
     * @return 所有字段
     */
    private Field[] getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && !clazz.equals(Object.class)) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields.toArray(new Field[0]);
    }
}
