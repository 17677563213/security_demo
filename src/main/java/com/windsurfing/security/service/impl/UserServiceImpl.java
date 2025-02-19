package com.windsurfing.security.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.windsurfing.security.entity.User;
import com.windsurfing.security.mapper.UserMapper;
import com.windsurfing.security.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    @Override
    public User getByPhone(String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        return getOne(wrapper);
    }
    
    @Override
    public User getByEmail(String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        return getOne(wrapper);
    }
    
    @Override
    public User getByIdCard(String idCard) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getIdCard, idCard);
        return getOne(wrapper);
    }
}
