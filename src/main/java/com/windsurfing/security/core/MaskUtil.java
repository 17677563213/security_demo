package com.windsurfing.security.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 数据脱敏工具类
 */
@Slf4j
@Component
public class MaskUtil {
    
    /**
     * 手机号码脱敏
     * 保留前3位和后4位，中间用*代替
     */
    public String maskPhone(String phone) {
        if (StringUtils.isBlank(phone)) {
            return phone;
        }
        
        // 如果是加密数据，不进行脱敏
        if (phone.startsWith("$") && phone.contains("_KEY$")) {
            log.debug("Skip masking encrypted phone: {}", phone);
            return phone;
        }
        
        return StringUtils.overlay(phone, "****", 3, phone.length() - 4);
    }
    
    /**
     * 身份证号脱敏
     * 保留前6位和后4位，中间用*代替
     */
    public String maskIdCard(String idCard) {
        if (StringUtils.isBlank(idCard)) {
            return idCard;
        }
        
        // 如果是加密数据，不进行脱敏
        if (idCard.startsWith("$") && idCard.contains("_KEY$")) {
            log.debug("Skip masking encrypted idCard: {}", idCard);
            return idCard;
        }
        
        return StringUtils.overlay(idCard, "********", 6, idCard.length() - 4);
    }
    
    /**
     * 银行卡号脱敏
     * 仅显示后4位，其余用*代替
     */
    public String maskBankCard(String bankCard) {
        if (StringUtils.isBlank(bankCard)) {
            return bankCard;
        }
        
        // 如果是加密数据，不进行脱敏
        if (bankCard.startsWith("$") && bankCard.contains("_KEY$")) {
            log.debug("Skip masking encrypted bankCard: {}", bankCard);
            return bankCard;
        }
        
        return StringUtils.overlay(bankCard, StringUtils.repeat("*", bankCard.length() - 4), 0, bankCard.length() - 4);
    }
    
    /**
     * 姓名脱敏
     * 仅显示第一个字符，其余用*代替
     */
    public String maskName(String name) {
        if (StringUtils.isBlank(name)) {
            return name;
        }
        if (name.length() <= 1) {
            return name;
        }
        
        // 如果是加密数据，不进行脱敏
        if (name.startsWith("$") && name.contains("_KEY$")) {
            log.debug("Skip masking encrypted name: {}", name);
            return name;
        }
        
        return StringUtils.overlay(name, StringUtils.repeat("*", name.length() - 1), 1, name.length());
    }
    
    /**
     * 邮箱脱敏
     * 邮箱前缀仅显示第一个字符，其余用*代替
     */
    public String maskEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return email;
        }
        
        // 如果是加密数据，不进行脱敏
        if (email.startsWith("$") && email.contains("_KEY$")) {
            log.debug("Skip masking encrypted email: {}", email);
            return email;
        }
        
        String prefix = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        return StringUtils.overlay(prefix, StringUtils.repeat("*", prefix.length() - 1), 1, prefix.length()) + domain;
    }
}
