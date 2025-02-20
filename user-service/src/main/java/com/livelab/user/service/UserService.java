package com.livelab.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.livelab.user.entity.User;
import java.util.List;

public interface UserService extends IService<User> {
    boolean saveUser(User user);
    boolean saveBatch(List<User> users);
    User getById(Long id);
    boolean updateUser(User user);
    boolean deleteUser(Long id);
    User getByPhone(String phone);
    User getByEmail(String email);
    User getByIdCard(String idCard);
}
