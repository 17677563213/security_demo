package com.livelab.security.starter.aspect;

import com.livelab.security.starter.annotation.Decrypt;
import com.livelab.security.starter.annotation.Digest;
import com.livelab.security.starter.annotation.Encrypt;
import com.livelab.security.starter.core.CryptoUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.lang.reflect.Field;
import java.util.Collection;

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

    public DataSecurityAspect(CryptoUtil cryptoUtil) {
        this.cryptoUtil = cryptoUtil;
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
    @Around("execution(* com.baomidou.mybatisplus.core.mapper.BaseMapper+.*(..))")
    public Object handleFind(ProceedingJoinPoint joinPoint) throws Throwable {
        // 处理保存前的加密和摘要
        String methodName = joinPoint.getSignature().getName();
        if (methodName.startsWith("insert") || methodName.startsWith("update")) {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                Object param = args[0];
                if (param instanceof Collection) {
                    // 处理批量操作
                    for (Object item : (Collection<?>) param) {
                        handleEncryptAndDigest(item);
                    }
                } else {
                    // 处理单个对象
                    handleEncryptAndDigest(param);
                }
            }
        }

        // 执行原方法
        Object result = joinPoint.proceed();

        // 处理查询结果的解密
        try {
            if (result instanceof Collection) {
                // 处理集合类型的结果
                for (Object item : (Collection<?>) result) {
                    handleDecrypt(item);
                }
            } else if (result != null) {
                // 处理单个对象的结果
                handleDecrypt(result);
            }
        } catch (Exception e) {
            log.error("Error processing result in security aspect", e);
        }

        return result;
    }

    /**
     * 处理对象的加密和摘要
     * - 查找带有@Encrypt注解的字段进行加密
     * - 查找带有@Digest注解的字段生成摘要
     * - 摘要会存储在同名的{字段名}Digest字段中
     *
     * @param obj 需要处理的对象
     */
    private void handleEncryptAndDigest(Object obj) {
        if (obj == null) {
            return;
        }

        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(obj);
                if (value instanceof String) {
                    String strValue = (String) value;

                    // 处理加密：使用@Encrypt注解指定的密钥类型进行加密
                    Encrypt encrypt = field.getAnnotation(Encrypt.class);
                    if (encrypt != null) {
                        String encryptedValue = cryptoUtil.encrypt(strValue, encrypt.keyType());
                        field.set(obj, encryptedValue);
                    }

                    // 处理摘要：生成MD5摘要并存储在对应的摘要字段中
                    Digest digest = field.getAnnotation(Digest.class);
                    if (digest != null) {
                        String digestFieldName = field.getName() + "Digest";
                        try {
                            Field digestField = clazz.getDeclaredField(digestFieldName);
                            digestField.setAccessible(true);
                            String md5 = DigestUtils.md5DigestAsHex(strValue.getBytes());
                            digestField.set(obj, md5);
                        } catch (NoSuchFieldException e) {
                            log.error("No digest field found for: " + field.getName(), e);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error processing field: " + field.getName(), e);
            }
        }
    }

    /**
     * 处理对象的解密
     * - 查找同时带有@Decrypt和@Encrypt注解的字段
     * - 解析加密字符串中的密钥类型和实际加密内容
     * - 使用对应的密钥进行解密
     * 
     * 加密格式：$密钥类型$加密内容
     * 例如：$PHONE_KEY$encrypted_content
     *
     * @param obj 需要解密的对象
     */
    private void handleDecrypt(Object obj) {
        if (obj == null) {
            return;
        }

        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            try {
                Decrypt decrypt = field.getAnnotation(Decrypt.class);
                Encrypt encrypt = field.getAnnotation(Encrypt.class);
                if (decrypt != null && encrypt != null) {
                    field.setAccessible(true);
                    Object value = field.get(obj);
                    if (value instanceof String) {
                        String encryptedValue = (String) value;
                        if (encryptedValue.contains("$")) {
                            // 解析密钥类型和加密内容
                            String keyType = encryptedValue.substring(1, encryptedValue.indexOf("$", 1));
                            String actualValue = encryptedValue.substring(encryptedValue.indexOf("$", 1) + 1);
                            // 使用对应的密钥进行解密
                            String decryptedValue = cryptoUtil.decrypt(actualValue, keyType);
                            field.set(obj, decryptedValue);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error decrypting field: " + field.getName(), e);
            }
        }
    }
}
