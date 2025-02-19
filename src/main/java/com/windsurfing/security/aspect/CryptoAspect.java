package com.windsurfing.security.aspect;

import com.windsurfing.security.annotation.Decrypt;
import com.windsurfing.security.annotation.Encrypt;
import com.windsurfing.security.core.CryptoUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * 加密解密切面
 * 处理标注了@Encrypt和@Decrypt注解的字段
 */
@Aspect
@Component
public class CryptoAspect {

    private final CryptoUtil cryptoUtil;

    public CryptoAspect(CryptoUtil cryptoUtil) {
        this.cryptoUtil = cryptoUtil;
    }

    @Around("execution(* com.windsurfing..*.*(..)) && @annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public Object handleCrypto(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        // 处理请求参数的解密
        for (Object arg : args) {
            if (arg != null) {
                handleDecryption(arg);
            }
        }

        // 执行原方法
        Object result = joinPoint.proceed();

        // 处理响应数据的加密
        if (result != null) {
            handleEncryption(result);
        }

        return result;
    }

    private void handleEncryption(Object obj) throws Exception {
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            Encrypt encrypt = field.getAnnotation(Encrypt.class);
            if (encrypt != null && field.get(obj) != null) {
                String value = field.get(obj).toString();
                String encrypted = cryptoUtil.encrypt(value, encrypt.keyId());
                field.set(obj, encrypted);
            }
        }
    }

    private void handleDecryption(Object obj) throws Exception {
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            Decrypt decrypt = field.getAnnotation(Decrypt.class);
            if (decrypt != null && field.get(obj) != null) {
                String value = field.get(obj).toString();
                String decrypted = cryptoUtil.decrypt(value);
                field.set(obj, decrypted);
            }
        }
    }
}
