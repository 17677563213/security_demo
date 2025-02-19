package com.livelab.security;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.livelab.security.mapper")
@EnableScheduling
public class DataSecurityApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(DataSecurityApplication.class, args);
    }
}
