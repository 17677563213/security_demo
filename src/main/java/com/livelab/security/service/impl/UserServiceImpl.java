package com.livelab.security.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.livelab.security.entity.User;
import com.livelab.security.mapper.UserMapper;
import com.livelab.security.service.UserService;
import com.livelab.security.core.DigestUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    @Autowired
    private DigestUtil digestUtil;

    @Override
    public User getByPhone(String phone) {
        log.info("Calculating digest for phone: {}", phone);
        String phoneDigest = digestUtil.calculateDigest(phone);
        log.info("Querying user by phone digest: {}", phoneDigest);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhoneDigest, phoneDigest)
              .orderByDesc(User::getId)
              .last("LIMIT 1");  // 只返回最新的一条记录
        User user = getOne(wrapper);
        log.info("Query result: {}", user);
        return user;
    }

    @Override
    public User getByEmail(String email) {
        String emailDigest = digestUtil.calculateDigest(email);
        log.info("Querying user by email: {}, digest: {}", email, emailDigest);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmailDigest, emailDigest);
        User user = getOne(wrapper);
        log.info("Query result: {}", user);
        return user;
    }

    @Override
    public User getByIdCard(String idCard) {
        String idCardDigest = digestUtil.calculateDigest(idCard);
        log.info("Querying user by idCard: {}, digest: {}", idCard, idCardDigest);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getIdCardDigest, idCardDigest);
        User user = getOne(wrapper);
        log.info("Query result: {}", user);
        return user;
    }

    @Override
    public List<User> list() {
        // 获取原始数据
        List<User> users = baseMapper.selectList(null);
        log.info("Found {} users in total", users.size());
        // 记录每个用户的摘要值
        for (User user : users) {
            log.info("User: {}, phoneDigest: {}, emailDigest: {}, idCardDigest: {}", 
                    user.getName(), user.getPhoneDigest(), user.getEmailDigest(), user.getIdCardDigest());
        }
        // DataSecurityAspect 会自动处理解密和掩码
        return users;
    }
}
