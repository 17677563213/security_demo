package com.livelab.security.controller;

import com.livelab.security.common.ApiResponse;
import com.livelab.security.entity.User;
import com.livelab.security.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/users/test")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/save")
    public ApiResponse<User> testSave() {
        User user = new User();
        user.setName("张三");
        String phone = "13888888888";
        log.info("Creating user with phone: {}", phone);
        user.setPhone(phone);
        user.setEmail("zhangsan@example.com");
        user.setIdCard("310000199001011234");
        user.setPassword("123456");
        
        userService.save(user);
        log.info("Created user: {}", user);
        return ApiResponse.success(user);
    }

    @GetMapping("/query/{id}")
    public ApiResponse<User> testQuery(@PathVariable Long id) {
        User user = userService.getById(id);
        return ApiResponse.success(user);
    }

    @PostMapping("/batch-save")
    public ApiResponse<List<User>> testBatchSave() {
        // 创建多个测试用户
        User user1 = new User();
        user1.setName("李四");
        user1.setPhone("13888888888");  
        user1.setEmail("lisi@example.com");
        user1.setIdCard("310000199001011235");
        user1.setPassword("123456");

        User user2 = new User();
        user2.setName("王五");
        user2.setPhone("13888888888");  
        user2.setEmail("wangwu@example.com");
        user2.setIdCard("310000199001011236");
        user2.setPassword("123456");

        List<User> users = Arrays.asList(user1, user2);
        userService.saveBatch(users);
        return ApiResponse.success(users);
    }

    @GetMapping("/list")
    public ApiResponse<List<User>> testList() {
        List<User> users = userService.list();
        return ApiResponse.success(users);
    }

    @GetMapping("/query-by-phone")
    public ApiResponse<User> testQueryByPhone(@RequestParam String phone) {
        log.info("Received request to query user by phone: {}", phone);
        User user = userService.getByPhone(phone);
        log.info("Query result: {}", user);
        return ApiResponse.success(user);
    }

    @GetMapping("/query-by-email")
    public ApiResponse<User> testQueryByEmail(@RequestParam String email) {
        User user = userService.getByEmail(email);
        return ApiResponse.success(user);
    }

    @GetMapping("/query-by-idcard")
    public ApiResponse<User> testQueryByIdCard(@RequestParam String idCard) {
        User user = userService.getByIdCard(idCard);
        return ApiResponse.success(user);
    }
}
