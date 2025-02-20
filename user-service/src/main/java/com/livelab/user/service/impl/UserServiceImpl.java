package com.livelab.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.livelab.user.entity.User;
import com.livelab.user.mapper.UserMapper;
import com.livelab.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.List;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    @Transactional
    public boolean saveUser(User user) {
        log.info("Saving user: {}", user.getUsername());
        return save(user);
    }

    @Override
    @Transactional
    public boolean saveBatch(List<User> users) {
        log.info("Batch saving {} users", users.size());
        return super.saveBatch(users, 100);
    }

    @Override
    public User getById(Long id) {
        log.info("Getting user by id: {}", id);
        return lambdaQuery()
                .eq(User::getId, id)
                .eq(User::getDeleted, 0)
                .one();
    }

    @Override
    @Transactional
    public boolean updateUser(User user) {
        log.info("Updating user: {}", user.getId());
        return updateById(user);
    }

    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        log.info("Deleting user: {}", id);
        User user = new User();
        user.setId(id);
        user.setDeleted(1);
        return updateById(user);
    }

    @Override
    public User getByPhone(String phone) {
        log.info("Getting user by phone");
        String phoneDigest = DigestUtils.md5DigestAsHex(phone.getBytes());
        return lambdaQuery()
                .eq(User::getPhoneDigest, phoneDigest)
                .eq(User::getDeleted, 0)
                .one();
    }

    @Override
    public User getByEmail(String email) {
        log.info("Getting user by email");
        String emailDigest = DigestUtils.md5DigestAsHex(email.getBytes());
        return lambdaQuery()
                .eq(User::getEmailDigest, emailDigest)
                .eq(User::getDeleted, 0)
                .one();
    }

    @Override
    public User getByIdCard(String idCard) {
        log.info("Getting user by idCard");
        String idCardDigest = DigestUtils.md5DigestAsHex(idCard.getBytes());
        return lambdaQuery()
                .eq(User::getIdCardDigest, idCardDigest)
                .eq(User::getDeleted, 0)
                .one();
    }
}
