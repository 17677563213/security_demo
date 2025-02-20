package com.livelab.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.livelab.user.entity.User;
import com.livelab.user.mapper.UserMapper;
import com.livelab.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    public boolean saveUser(User user) {
        return save(user);
    }

    @Override
    public User getById(Long id) {
        return lambdaQuery()
                .eq(User::getId, id)
                .eq(User::getDeleted, 0)
                .one();
    }

    @Override
    public boolean updateUser(User user) {
        return updateById(user);
    }

    @Override
    public boolean deleteUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setDeleted(1);
        return updateById(user);
    }

    @Override
    public User getByPhone(String phone) {
        String phoneDigest = DigestUtils.md5DigestAsHex(phone.getBytes());
        return lambdaQuery()
                .eq(User::getPhoneDigest, phoneDigest)
                .eq(User::getDeleted, 0)
                .one();
    }

    @Override
    public User getByEmail(String email) {
        String emailDigest = DigestUtils.md5DigestAsHex(email.getBytes());
        return lambdaQuery()
                .eq(User::getEmailDigest, emailDigest)
                .eq(User::getDeleted, 0)
                .one();
    }

    @Override
    public User getByIdCard(String idCard) {
        String idCardDigest = DigestUtils.md5DigestAsHex(idCard.getBytes());
        return lambdaQuery()
                .eq(User::getIdCardDigest, idCardDigest)
                .eq(User::getDeleted, 0)
                .one();
    }
}
