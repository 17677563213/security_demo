package com.livelab.security.starter.core;

import com.livelab.security.starter.annotation.Mask;
import org.springframework.util.StringUtils;

public class MaskUtil {
    
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

    private static String maskPhone(String phone) {
        if (phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

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

    private static String maskIdCard(String idCard) {
        if (idCard.length() < 10) {
            return idCard;
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(idCard.length() - 4);
    }

    private static String maskCustom(String value, String pattern) {
        if (!StringUtils.hasText(pattern)) {
            return value;
        }
        return value.replaceAll(pattern, "*");
    }
}
