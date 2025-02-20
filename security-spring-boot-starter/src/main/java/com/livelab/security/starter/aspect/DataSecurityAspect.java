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

@Slf4j
@Aspect
@Order(1)  // 在DataMaskAspect之前执行
@Component
public class DataSecurityAspect {
    private final CryptoUtil cryptoUtil;

    public DataSecurityAspect(CryptoUtil cryptoUtil) {
        this.cryptoUtil = cryptoUtil;
    }

    @Around("execution(* com.baomidou.mybatisplus.core.mapper.BaseMapper+.*(..))")
    public Object handleFind(ProceedingJoinPoint joinPoint) throws Throwable {
        // 处理保存前的加密和摘要
        String methodName = joinPoint.getSignature().getName();
        if (methodName.startsWith("insert") || methodName.startsWith("update")) {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                Object param = args[0];
                if (param instanceof Collection) {
                    for (Object item : (Collection<?>) param) {
                        handleEncryptAndDigest(item);
                    }
                } else {
                    handleEncryptAndDigest(param);
                }
            }
        }

        // 执行原方法
        Object result = joinPoint.proceed();

        // 处理查询结果的解密
        try {
            if (result instanceof Collection) {
                for (Object item : (Collection<?>) result) {
                    handleDecrypt(item);
                }
            } else if (result != null) {
                handleDecrypt(result);
            }
        } catch (Exception e) {
            log.error("Error processing result in security aspect", e);
        }

        return result;
    }

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

                    // 处理加密
                    Encrypt encrypt = field.getAnnotation(Encrypt.class);
                    if (encrypt != null) {
                        String encryptedValue = cryptoUtil.encrypt(strValue, encrypt.keyType());
                        field.set(obj, encryptedValue);
                    }

                    // 处理摘要
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
                            String keyType = encryptedValue.substring(1, encryptedValue.indexOf("$", 1));
                            String actualValue = encryptedValue.substring(encryptedValue.indexOf("$", 1) + 1);
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
