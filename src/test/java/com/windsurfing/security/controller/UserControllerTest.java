package com.livelab.security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livelab.security.example.UserWithDigestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateAndGetUser() throws Exception {
        // 创建测试用户数据
        UserWithDigestDTO user = new UserWithDigestDTO();
        user.setUsername("测试用户");
        user.setPhone("13800138000");
        user.setIdCard("110101199001011234");

        // 测试创建用户
        String response = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("测试用户"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 从响应中获取用户ID
        Long userId = objectMapper.readTree(response)
                .path("data")
                .path("id")
                .asLong();

        // 测试获取用户
        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("测试用户"));

        // 测试通过手机号查询
        mockMvc.perform(get("/api/users/search/phone/13800138000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].username").value("测试用户"));
    }
}
