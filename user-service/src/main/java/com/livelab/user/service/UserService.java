package com.livelab.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.livelab.user.entity.User;

public interface UserService extends IService<User> {
    boolean saveUser(User user);
    User getById(Long id);
    boolean updateUser(User user);
    boolean deleteUser(Long id);
    User getByPhone(String phone);
    User getByEmail(String email);
    User getByIdCard(String idCard);
}
