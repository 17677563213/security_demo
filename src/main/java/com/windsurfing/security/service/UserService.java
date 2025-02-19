package com.windsurfing.security.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.windsurfing.security.entity.User;

public interface UserService extends IService<User> {
    
    /**
     * 根据手机号查询用户
     */
    User getByPhone(String phone);
    
    /**
     * 根据邮箱查询用户
     */
    User getByEmail(String email);
    
    /**
     * 根据身份证号查询用户
     */
    User getByIdCard(String idCard);
}
