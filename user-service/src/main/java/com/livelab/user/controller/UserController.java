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

    @PostMapping("/batch-save")
    public ApiResponse<List<User>> batchSave() {
        User user1 = new User();
        user1.setUsername("李四");
        user1.setPhone("13888888888");
        user1.setEmail("lisi@example.com");
        user1.setIdCard("310000199001011235");
        user1.setPassword("123456");
        user1.setDeleted(0);
        user1.setStatus(1);
        user1.setCreateTime(LocalDateTime.now());
        user1.setUpdateTime(LocalDateTime.now());

        User user2 = new User();
        user2.setUsername("王五");
        user2.setPhone("13888888888");
        user2.setEmail("wangwu@example.com");
        user2.setIdCard("310000199001011236");
        user2.setPassword("123456");
        user2.setDeleted(0);
        user2.setStatus(1);
        user2.setCreateTime(LocalDateTime.now());
        user2.setUpdateTime(LocalDateTime.now());

        List<User> users = Arrays.asList(user1, user2);
        userService.saveBatch(users);
        return ApiResponse.success(users);
    }

    @GetMapping("/list")
    public ApiResponse<List<User>> list() {
        return ApiResponse.success(userService.list());
    }

    @GetMapping("/by-phone")
    public ApiResponse<User> getByPhone(@RequestParam String phone) {
        log.info("Querying user by phone: {}", phone);
        return ApiResponse.success(userService.getByPhone(phone));
    }

    @GetMapping("/by-email")
    public ApiResponse<User> getByEmail(@RequestParam String email) {
        return ApiResponse.success(userService.getByEmail(email));
    }

    @GetMapping("/by-idcard")
    public ApiResponse<User> getByIdCard(@RequestParam String idCard) {
        return ApiResponse.success(userService.getByIdCard(idCard));
    }
}
