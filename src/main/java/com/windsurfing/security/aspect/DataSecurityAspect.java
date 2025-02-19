package com.windsurfing.security.aspect;

import com.windsurfing.security.annotation.Decrypt;
import com.windsurfing.security.annotation.Encrypt;
import com.windsurfing.security.annotation.Digest;
import com.windsurfing.security.annotation.Mask;
import com.windsurfing.security.core.CryptoUtil;
import com.windsurfing.security.core.DigestUtil;
import com.windsurfing.security.core.MaskUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.Field;
import java.util.Collection;

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

    @Around("execution(* com.baomidou.mybatisplus.core.mapper.BaseMapper.insert(..)) || " +
            "execution(* com.baomidou.mybatisplus.core.mapper.BaseMapper.update*(..)) || " +
            "execution(* com.baomidou.mybatisplus.extension.service.IService.save*(..)) || " +
            "execution(* com.baomidou.mybatisplus.extension.service.IService.update*(..))")
    public Object handleSave(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        try {
            for (Object arg : args) {
                if (arg != null) {
                    if (arg instanceof Collection) {
                        ((Collection<?>) arg).forEach(item -> {
                            try {
                                handleEncryptAndDigest(item);
                            } catch (Exception e) {
                                log.error("Failed to handle encryption and digest for collection item: {}", item, e);
                                throw new RuntimeException("Failed to handle encryption and digest", e);
                            }
                        });
                    } else {
                        handleEncryptAndDigest(arg);
                    }
                }
            }
            return joinPoint.proceed();
        } catch (Exception e) {
            log.error("Failed to handle save operation: {}", joinPoint.getSignature(), e);
            throw e;
        }
    }

    @Around("execution(* com.baomidou.mybatisplus.core.mapper.BaseMapper.select*(..)) || " +
            "execution(* com.baomidou.mybatisplus.extension.service.IService.get*(..)) || " +
            "execution(* com.baomidou.mybatisplus.extension.service.IService.list*(..)) || " +
            "execution(* com.windsurfing.security.controller.*.*(..))")
    public Object handleFind(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object result = joinPoint.proceed();
            if (result != null) {
                if (result instanceof Collection) {
                    ((Collection<?>) result).forEach(item -> {
                        try {
                            handleDecryptAndMask(item);
                        } catch (Exception e) {
                            log.error("Failed to handle decryption and mask for collection item: {}", item, e);
                            throw new RuntimeException("Failed to handle decryption and mask", e);
                        }
                    });
                } else {
                    handleDecryptAndMask(result);
                }
            }
            return result;
        } catch (Exception e) {
            log.error("Failed to handle find operation: {}", joinPoint.getSignature(), e);
            throw e;
        }
    }

    private void handleEncryptAndDigest(Object obj) {
        try {
            if (obj == null) return;
            
            Class<?> clazz = obj.getClass();
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(obj);
                if (value == null) continue;

                // 处理加密
                Encrypt encrypt = field.getAnnotation(Encrypt.class);
                if (encrypt != null) {
                    log.debug("Encrypting field: {} with keyId: {}, value: {}", field.getName(), encrypt.keyId(), value);
                    String encrypted = cryptoUtil.encrypt(value.toString(), encrypt.keyId());
                    log.debug("Encrypted result for field {}: {}", field.getName(), encrypted);
                    field.set(obj, encrypted);
                }

                // 处理摘要
                Digest digest = field.getAnnotation(Digest.class);
                if (digest != null) {
                    log.debug("Generating digest for field: {}, value: {}", field.getName(), value);
                    String digestValue = digestUtil.calculateDigest(value.toString());
                    log.debug("Generated digest for field {}: {}", field.getName(), digestValue);
                    try {
                        Field digestField = clazz.getDeclaredField(field.getName() + "Digest");
                        digestField.setAccessible(true);
                        digestField.set(obj, digestValue);
                    } catch (NoSuchFieldException e) {
                        log.warn("Digest field not found for: {}", field.getName());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to handle encryption and digest for object: {}", obj, e);
            throw new RuntimeException("Failed to handle encryption and digest: " + e.getMessage(), e);
        }
    }

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
                boolean isEncrypted = strValue.startsWith("$") && strValue.contains("_KEY$");

                // 处理解密
                Decrypt decrypt = field.getAnnotation(Decrypt.class);
                if (decrypt != null && isEncrypted) {
                    log.debug("Decrypting field: {}, value: {}", field.getName(), value);
                    try {
                        String decrypted = cryptoUtil.decrypt(strValue);
                        log.debug("Decrypted result for field {}: {}", field.getName(), decrypted);
                        field.set(obj, decrypted);
                        value = decrypted; // 更新value以供后续脱敏使用
                    } catch (Exception e) {
                        log.error("Failed to decrypt field: {}, value: {}", field.getName(), value, e);
                        // 如果解密失败，保持原值
                    }
                }

                // 处理脱敏 - 只对未加密或已解密的数据进行脱敏
                Mask mask = field.getAnnotation(Mask.class);
                if (mask != null && value != null && !isEncrypted) {
                    log.debug("Masking field: {} with type: {}, value: {}", field.getName(), mask.type(), value);
                    String maskedValue;
                    switch (mask.type()) {
                        case PHONE:
                            maskedValue = maskUtil.maskPhone(value.toString());
                            break;
                        case ID_CARD:
                            maskedValue = maskUtil.maskIdCard(value.toString());
                            break;
                        case EMAIL:
                            maskedValue = maskUtil.maskEmail(value.toString());
                            break;
                        default:
                            maskedValue = value.toString();
                    }
                    log.debug("Masked result for field {}: {}", field.getName(), maskedValue);
                    field.set(obj, maskedValue);
                }
            }
        } catch (Exception e) {
            log.error("Failed to handle decryption and mask for object: {}", obj, e);
            throw new RuntimeException("Failed to handle decryption and mask: " + e.getMessage(), e);
        }
    }
}
