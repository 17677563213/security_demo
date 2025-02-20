package com.livelab.security.starter.core;

import com.livelab.security.starter.annotation.Mask;
import org.springframework.util.StringUtils;

/**
 * 数据脱敏工具类
 * 提供多种数据脱敏策略，包括：
 * - 手机号脱敏：保留前3后4位
 * - 邮箱脱敏：保留前3后4位和域名
 * - 身份证号脱敏：保留前6后4位
 * - 自定义脱敏：根据指定的模式进行脱敏
 */
public class MaskUtil {
    
    /**
     * 根据指定的脱敏类型和模式对值进行脱敏
     *
     * @param value 需要脱敏的原始值
     * @param type 脱敏类型
     * @param pattern 自定义脱敏模式（仅在type为CUSTOM时使用）
     * @return 脱敏后的值
     */
    public static String maskValue(String value, Mask.MaskType type, String pattern) {
        if (!StringUtils.hasText(value)) {
            return value;
        }

        switch (type) {
            case PHONE:
                return maskPhone(value);
            case EMAIL:
                return maskEmail(value);
            case ID_CARD:
                return maskIdCard(value);
            case CUSTOM:
                return maskCustom(value, pattern);
            default:
                return value;
        }
    }

    /**
     * 手机号脱敏
     * 规则：保留前3后4位，中间用****代替
     * 例如：13812345678 -> 138****5678
     *
     * @param phone 手机号
     * @return 脱敏后的手机号
     */
    private static String maskPhone(String phone) {
        if (phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 邮箱脱敏
     * 规则：
     * - 如果用户名长度小于等于6位，保留第1位，后面用****代替
     * - 如果用户名长度大于6位，保留前3后3位，中间用****代替
     * - 域名保持不变
     * 例如：
     * - test@example.com -> t****@example.com
     * - usermail@example.com -> use****ail@example.com
     *
     * @param email 邮箱地址
     * @return 脱敏后的邮箱地址
     */
    private static String maskEmail(String email) {
        if (!email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String name = parts[0];
        String domain = parts[1];
        
        if (name.length() <= 6) {
            return name.substring(0, 1) + "****" + "@" + domain;
        }
        return name.substring(0, 3) + "****" + name.substring(name.length() - 3) + "@" + domain;
    }

    /**
     * 身份证号脱敏
     * 规则：保留前6后4位，中间用8个*号代替
     * 例如：310123199001011234 -> 310123********1234
     *
     * @param idCard 身份证号
     * @return 脱敏后的身份证号
     */
    private static String maskIdCard(String idCard) {
        if (idCard.length() < 10) {
            return idCard;
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(idCard.length() - 4);
    }

    /**
     * 自定义脱敏
     * 根据指定的模式对字符串进行脱敏
     * 模式中的*号表示需要脱敏的位置
     * 例如：pattern="###***####"，value="123456789" -> "123***789"
     *
     * @param value 需要脱敏的值
     * @param pattern 脱敏模式
     * @return 脱敏后的值
     */
    private static String maskCustom(String value, String pattern) {
        if (!StringUtils.hasText(pattern) || value.length() != pattern.length()) {
            return value;
        }
        
        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < pattern.length(); i++) {
            if (pattern.charAt(i) == '*') {
                masked.append('*');
            } else {
                masked.append(value.charAt(i));
            }
        }
        return masked.toString();
    }
}
