package com.livelab.user.controller;

import com.livelab.security.starter.common.ApiResponse;
import com.livelab.user.entity.User;
import com.livelab.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/save")
    public ApiResponse<User> save() {
        User user = new User();
        user.setUsername("张三");
        user.setPhone("13888888888");
        user.setEmail("zhangsan@example.com");
        user.setIdCard("310000199001011234");
        user.setPassword("123456");
        user.setDeleted(0);
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        userService.saveUser(user);
        log.info("Created user: {}", user);
        return ApiResponse.success(user);
    }

    @GetMapping("/{id}")
    public ApiResponse<User> getById(@PathVariable Long id) {
        return ApiResponse.success(userService.getById(id));
    }
    

    @GetMapping("/list")
    public ApiResponse<List<User>> list() {
        return ApiResponse.success(userService.list());
    }

    @GetMapping("/phone/{phone}")
    public ApiResponse<User> getByPhone(@PathVariable String phone) {
        log.info("Querying user by phone: {}", phone);
        return ApiResponse.success(userService.getByPhone(phone));
    }

    @GetMapping("/email/{email}")
    public ApiResponse<User> getByEmail(@PathVariable String email) {
        return ApiResponse.success(userService.getByEmail(email));
    }

    @GetMapping("/idCard/{idCard}")
    public ApiResponse<User> getByIdCard(@PathVariable String idCard) {
        return ApiResponse.success(userService.getByIdCard(idCard));
    }
}
