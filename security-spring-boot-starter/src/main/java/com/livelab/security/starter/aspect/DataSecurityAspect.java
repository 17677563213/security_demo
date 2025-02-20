package com.livelab.security.starter.aspect;

import com.livelab.security.starter.annotation.Decrypt;
import com.livelab.security.starter.annotation.Digest;
import com.livelab.security.starter.annotation.Encrypt;
import com.livelab.security.starter.core.CryptoUtil;
import com.livelab.security.starter.util.DigestUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

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
     * 定义切点：拦截所有Mapper接口和Service接口的方法
     * 包括：
     * 1. BaseMapper接口的所有实现类的方法
     * 2. IService接口的所有实现类的方法
     * 3. 自定义Mapper接口中的方法（XML中实现的SQL）
     */
    @Pointcut("execution(* com.baomidou.mybatisplus.core.mapper.BaseMapper+.*(..)) || " +
              "execution(* com.baomidou.mybatisplus.extension.service.IService+.*(..)) || " +
              "execution(* com.livelab..*.mapper.*Mapper.*(..))")
    public void dataSecurityPointcut() {}

    /**
     * 拦截MyBatis-Plus的Mapper和Service方法，以及自定义Mapper方法
     * 处理数据的加密、解密和摘要
     * - 对insert、save、update等写操作方法的参数进行加密和摘要处理
     * - 对select、get、list等查询结果进行解密处理
     * - 对自定义方法根据方法名判断是读操作还是写操作
     *
     * @param joinPoint 切点
     * @return 处理后的结果
     * @throws Throwable 处理过程中的异常
     */
    @Around("dataSecurityPointcut()")
    public Object handleData(ProceedingJoinPoint joinPoint) throws Throwable {
        // 处理保存前的加密和摘要
        String methodName = joinPoint.getSignature().getName().toLowerCase();
        Class<?> declaringType = joinPoint.getSignature().getDeclaringType();
        
        // 处理写操作
        if (isWriteOperation(methodName)) {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                // 处理每个参数
                for (Object arg : args) {
                    if (arg instanceof Collection) {
                        // 处理批量操作
                        for (Object item : (Collection<?>) arg) {
                            handleEncryptAndDigest(item);
                        }
                    } else if (arg != null && !arg.getClass().isPrimitive() && 
                             !arg.getClass().getName().startsWith("java.lang")) {
                        // 处理非基本类型的参数
                        handleEncryptAndDigest(arg);
                    }
                }
            }
        }

        // 执行原方法
        Object result = joinPoint.proceed();

        // 处理查询结果的解密
        if (isReadOperation(methodName)) {
            try {
                if (result instanceof Collection) {
                    // 处理集合类型的结果
                    for (Object item : (Collection<?>) result) {
                        handleDecrypt(item);
                    }
                } else if (result instanceof IPage) {
                    // 处理分页查询结果
                    IPage<?> page = (IPage<?>) result;
                    for (Object item : page.getRecords()) {
                        handleDecrypt(item);
                    }
                } else if (result instanceof Map) {
                    // 处理Map类型的结果
                    handleMapResult((Map<?, ?>) result);
                } else if (result != null && !result.getClass().isPrimitive() && 
                          !result.getClass().getName().startsWith("java.lang")) {
                    // 处理非基本类型的结果
                    handleDecrypt(result);
                }
            } catch (Exception e) {
                log.error("Error processing result in security aspect: {}", e.getMessage());
            }
        }

        return result;
    }

    /**
     * 处理Map类型的结果
     * @param map 需要处理的Map结果
     */
    private void handleMapResult(Map<?, ?> map) {
        for (Object value : map.values()) {
            if (value instanceof Collection) {
                for (Object item : (Collection<?>) value) {
                    handleDecrypt(item);
                }
            } else if (value != null && !value.getClass().isPrimitive() && 
                      !value.getClass().getName().startsWith("java.lang")) {
                handleDecrypt(value);
            }
        }
    }

    /**
     * 判断是否为写操作方法
     */
    private boolean isWriteOperation(String methodName) {
        return methodName.startsWith("insert") ||
               methodName.startsWith("update") ||
               methodName.startsWith("save") ||
               methodName.startsWith("add") ||
               methodName.startsWith("modify") ||
               methodName.startsWith("create") ||
               methodName.startsWith("batch") ||
               methodName.startsWith("delete"); // 删除操作可能需要加密的查询条件
    }

    /**
     * 判断是否为读操作方法
     */
    private boolean isReadOperation(String methodName) {
        return methodName.startsWith("select") ||
               methodName.startsWith("get") ||
               methodName.startsWith("list") ||
               methodName.startsWith("find") ||
               methodName.startsWith("query") ||
               methodName.startsWith("search") ||
               methodName.startsWith("count") ||
               methodName.startsWith("page");
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
                        String encryptedValue = cryptoUtil.encrypt(strValue);
                        field.set(obj, encryptedValue);
                    }

                    // 处理摘要：生成摘要并存储在对应的摘要字段中
                    Digest digest = field.getAnnotation(Digest.class);
                    if (digest != null) {
                        String digestFieldName = field.getName() + "Digest";
                        try {
                            Field digestField = clazz.getDeclaredField(digestFieldName);
                            digestField.setAccessible(true);
                            String digestValue = digestUtil.digest(strValue);
                            digestField.set(obj, digestValue);
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
                            String decryptedValue = cryptoUtil.decrypt(actualValue, Long.valueOf(keyType));
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
