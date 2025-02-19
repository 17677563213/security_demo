package com.livelab.security.aspect;

import com.livelab.security.annotation.Decrypt;
import com.livelab.security.annotation.Digest;
import com.livelab.security.annotation.Encrypt;
import com.livelab.security.annotation.Mask;
import com.livelab.security.core.CryptoUtil;
import com.livelab.security.core.DigestUtil;
import com.livelab.security.core.MaskUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * 数据安全处理切面
 * 负责在数据存储和查询时自动进行加密、解密、脱敏和摘要处理
 */
@Slf4j
@Aspect
@Component
@Order(1)
public class DataSecurityAspect {

    private final CryptoUtil cryptoUtil;
    private final DigestUtil digestUtil;
    private final MaskUtil maskUtil;

    public DataSecurityAspect(CryptoUtil cryptoUtil, DigestUtil digestUtil, MaskUtil maskUtil) {
        this.cryptoUtil = cryptoUtil;
        this.digestUtil = digestUtil;
        this.maskUtil = maskUtil;
    }

    /**
     * 处理数据保存操作的切面
     * 拦截所有的 MyBatis-Plus 的插入和更新操作
     * 在数据保存前进行加密和摘要处理
     */
    @Around("execution(* com.baomidou.mybatisplus.core.mapper.BaseMapper.insert(..)) || " +
            "execution(* com.baomidou.mybatisplus.core.mapper.BaseMapper.update*(..)) || " +
            "execution(* com.baomidou.mybatisplus.extension.service.IService.save*(..)) || " +
            "execution(* com.baomidou.mybatisplus.extension.service.IService.update*(..))")
    public Object handleSave(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        try {
            // 遍历所有参数
            for (Object arg : args) {
                if (arg != null) {
                    // 处理集合类型的参数
                    if (arg instanceof Collection) {
                        for (Object item : (Collection<?>) arg) {
                            handleEncryptAndDigest(item);
                        }
                    } else {
                        // 处理单个对象参数
                        handleEncryptAndDigest(arg);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error in handleSave", e);
            throw e;
        }
        return joinPoint.proceed();
    }

    /**
     * 处理数据查询操作的切面
     * 拦截所有的 MyBatis-Plus 的查询操作
     * 在数据返回前进行解密和脱敏处理
     */
    @Around("execution(* com.baomidou.mybatisplus.core.mapper.BaseMapper.select*(..)) || " +
            "execution(* com.baomidou.mybatisplus.core.mapper.BaseMapper.selectList(..)) || " +
            "execution(* com.baomidou.mybatisplus.extension.service.IService.list*(..)) || " +
            "execution(* com.baomidou.mybatisplus.extension.service.IService.get*(..))")
    public Object handleFind(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        try {
            if (result != null) {
                // 处理集合类型的结果
                if (result instanceof Collection) {
                    for (Object item : (Collection<?>) result) {
                        handleDecryptAndMask(item);
                    }
                } else {
                    // 处理单个对象结果
                    handleDecryptAndMask(result);
                }
            }
        } catch (Exception e) {
            log.error("Error in handleFind", e);
            throw e;
        }
        return result;
    }

    /**
     * 处理加密和摘要逻辑
     * 1. 检查字段是否有 @Encrypt 注解，如果有则进行加密
     * 2. 检查字段是否有 @Digest 注解，如果有则生成摘要
     * 加密后的格式为：$keyId$encryptedContent
     */
    private void handleEncryptAndDigest(Object obj) {
        try {
            if (obj == null) return;
            
            Class<?> clazz = obj.getClass();
            Field[] fields = clazz.getDeclaredFields();
            log.info("Processing object of type: {}", clazz.getName());

            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(obj);
                if (value == null) continue;

                String fieldName = field.getName();
                log.info("Processing field: {} with value: {}", fieldName, value);

                // 先生成摘要，因为摘要应该基于原始值而不是加密后的值
                if (fieldName.equals("phone") || fieldName.equals("email") || fieldName.equals("idCard")) {
                    String originalValue = value.toString();
                    log.info("Calculating digest for field: {} with value: {}", fieldName, originalValue);
                    String digestValue = digestUtil.calculateDigest(originalValue);
                    String digestFieldName = fieldName + "Digest";
                    try {
                        Field digestField = clazz.getDeclaredField(digestFieldName);
                        digestField.setAccessible(true);
                        digestField.set(obj, digestValue);
                        log.info("Set digest for field: {}, value: {}, digest: {}", 
                                fieldName, originalValue, digestValue);
                    } catch (NoSuchFieldException e) {
                        log.warn("Digest field not found: {}", digestFieldName);
                    }
                }

                // 处理加密注解 @Encrypt
                Encrypt encrypt = field.getAnnotation(Encrypt.class);
                if (encrypt != null) {
                    String originalValue = value.toString();
                    log.info("Encrypting field: {} with keyId: {}, original value: {}", 
                            field.getName(), encrypt.keyId(), originalValue);
                    // 使用指定的密钥ID进行加密
                    String encrypted = cryptoUtil.encrypt(originalValue, encrypt.keyId());
                    log.info("Encrypted result for field {}: {}", field.getName(), encrypted);
                    field.set(obj, encrypted);
                    log.debug("Updated field: {} with encrypted value: {}", field.getName(), encrypted);
                }
                log.debug("Processed field: {} with final value: {}", field.getName(), field.get(obj));
            }
        } catch (Exception e) {
            log.error("Error in handleEncryptAndDigest", e);
            throw new RuntimeException("Failed to process encryption and digest", e);
        }
    }

    /**
     * 处理解密和脱敏逻辑
     * 1. 检查字段是否有 @Decrypt 注解且内容是加密的，如果是则进行解密
     * 2. 检查字段是否有 @Mask 注解，如果有则进行脱敏处理
     * 加密内容的判断依据：以 $ 开头且包含 _KEY$
     */
    private void handleDecryptAndMask(Object obj) {
        try {
            if (obj == null) return;
            
            Class<?> clazz = obj.getClass();
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(obj);
                if (value == null) continue;

                String strValue = value.toString();
                // 判断是否是加密内容：以 $ 开头且包含 _KEY$
                boolean isEncrypted = strValue.startsWith("$") && strValue.contains("_KEY$");

                // 处理解密注解 @Decrypt
                Decrypt decrypt = field.getAnnotation(Decrypt.class);
                if (decrypt != null && isEncrypted) {
                    log.debug("Decrypting field: {}, value: {}", field.getName(), value);
                    try {
                        // 解密数据，密钥ID包含在加密内容中
                        String decrypted = cryptoUtil.decrypt(strValue);
                        log.debug("Decrypted result for field {}: {}", field.getName(), decrypted);
                        field.set(obj, decrypted);
                        value = decrypted; // 更新value以供后续脱敏使用
                    } catch (Exception e) {
                        log.error("Failed to decrypt field: " + field.getName(), e);
                    }
                }

                // 处理脱敏注解 @Mask
                Mask mask = field.getAnnotation(Mask.class);
                if (mask != null && value != null) {
                    log.debug("Masking field: {}, type: {}, value: {}", field.getName(), mask.type(), value);
                    try {
                        // 根据脱敏类型调用对应的脱敏方法
                        String masked;
                        switch (mask.type()) {
                            case PHONE:
                                masked = maskUtil.maskPhone(value.toString());
                                break;
                            case ID_CARD:
                                masked = maskUtil.maskIdCard(value.toString());
                                break;
                            case BANK_CARD:
                                masked = maskUtil.maskBankCard(value.toString());
                                break;
                            case EMAIL:
                                masked = maskUtil.maskEmail(value.toString());
                                break;
                            default:
                                masked = value.toString();
                        }
                        log.debug("Masked result for field {}: {}", field.getName(), masked);
                        field.set(obj, masked);
                    } catch (Exception e) {
                        log.error("Failed to mask field: " + field.getName(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error in handleDecryptAndMask", e);
            throw new RuntimeException("Failed to process decryption and masking", e);
        }
    }
}
