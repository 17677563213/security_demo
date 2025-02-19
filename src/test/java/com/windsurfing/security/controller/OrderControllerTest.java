package com.livelab.security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livelab.security.example.OrderDTO;
import com.livelab.security.example.UserWithDigestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateAndQueryOrder() throws Exception {
        // 1. 先创建一个用户
        UserWithDigestDTO user = new UserWithDigestDTO();
        user.setUsername("张三");
        user.setPhone("13800138000");
        user.setIdCard("110101199001011234");

        String userResponse = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 2. 创建该用户的订单
        OrderDTO order = new OrderDTO();
        order.setUserPhone("13800138000");
        order.setAmount(new BigDecimal("100.00"));
        order.setOrderNo("ORDER_001");

        String orderResponse = mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long orderId = objectMapper.readTree(orderResponse)
                .path("data")
                .path("id")
                .asLong();

        // 3. 测试通过手机号查询用户订单
        mockMvc.perform(get("/api/orders/user/13800138000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].orderNo").value("ORDER_001"));

        // 4. 测试查询订单详情（包含用户信息）
        mockMvc.perform(get("/api/orders/" + orderId + "/detail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.order.orderNo").value("ORDER_001"))
                .andExpect(jsonPath("$.data.user.username").value("张三"));
    }
}
