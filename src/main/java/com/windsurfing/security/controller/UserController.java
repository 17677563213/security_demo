package com.windsurfing.security.controller;

import com.windsurfing.security.common.ApiResponse;
import com.windsurfing.security.entity.User;
import com.windsurfing.security.service.UserService;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/test/save")
    public ApiResponse<User> testSave() {
        User user = new User();
        user.setName("张三");
        user.setPhone("13800138000");
        user.setEmail("zhangsan@example.com");
        user.setIdCard("310000199001011234");
        user.setPassword("123456");
        
        userService.save(user);
        return ApiResponse.success(user);
    }

    @GetMapping("/test/query/{id}")
    public ApiResponse<User> testQuery(@PathVariable Long id) {
        User user = userService.getById(id);
        return ApiResponse.success(user);
    }

    @PostMapping("/test/batch-save")
    public ApiResponse<List<User>> testBatchSave() {
        // 创建多个测试用户
        User user1 = new User();
        user1.setName("李四");
        user1.setPhone("13900139000");
        user1.setEmail("lisi@example.com");
        user1.setIdCard("310000199001011235");
        user1.setPassword("123456");

        User user2 = new User();
        user2.setName("王五");
        user2.setPhone("13700137000");
        user2.setEmail("wangwu@example.com");
        user2.setIdCard("310000199001011236");
        user2.setPassword("123456");

        List<User> users = Arrays.asList(user1, user2);
        userService.saveBatch(users);
        return ApiResponse.success(users);
    }

    @GetMapping("/test/list")
    public ApiResponse<List<User>> testList() {
        List<User> users = userService.list();
        return ApiResponse.success(users);
    }

    @GetMapping("/test/query-by-phone")
    public ApiResponse<User> testQueryByPhone(@RequestParam String phone) {
        User user = userService.getByPhone(phone);
        return ApiResponse.success(user);
    }

    @GetMapping("/test/query-by-email")
    public ApiResponse<User> testQueryByEmail(@RequestParam String email) {
        User user = userService.getByEmail(email);
        return ApiResponse.success(user);
    }

    @GetMapping("/test/query-by-id-card")
    public ApiResponse<User> testQueryByIdCard(@RequestParam String idCard) {
        User user = userService.getByIdCard(idCard);
        return ApiResponse.success(user);
    }
}
