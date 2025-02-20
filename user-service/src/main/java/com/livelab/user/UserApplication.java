package com.livelab.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 用户服务启动类
 * 
 * MapperScan配置说明：
 * - basePackages: 指定扫描的Mapper接口包路径
 * - sqlSessionFactoryRef: 显式指定使用MyBatis-Plus的sqlSessionFactory
 * - sqlSessionTemplateRef: 显式指定使用MyBatis-Plus的sqlSessionTemplate
 * 这样可以避免和security-starter中的SqlSessionFactory产生冲突
 */
@SpringBootApplication
@MapperScan(
    basePackages = "com.livelab.user.mapper",
    sqlSessionFactoryRef = "sqlSessionFactory",
    sqlSessionTemplateRef = "sqlSessionTemplate"
)
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
