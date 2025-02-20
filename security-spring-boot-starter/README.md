# Security Spring Boot Starter

这是一个用于处理数据加密、解密和摘要的 Spring Boot Starter。

## 功能特性

- 支持字段级别的加密和解密
- 支持字段级别的摘要计算
- 使用 SM4 算法进行加密
- 支持多种密钥类型
- 支持独立的密钥数据库管理
- 支持密钥自动轮换和过期处理
- 自动配置和集成

## 快速开始

1. 创建数据库和表：

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS security_key_db;

-- 创建密钥表
CREATE TABLE IF NOT EXISTS security_key (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    key_type VARCHAR(50) NOT NULL COMMENT '密钥类型',
    key_value VARCHAR(255) NOT NULL COMMENT '密钥值',
    effective_time DATETIME NOT NULL COMMENT '生效时间',
    expiry_time DATETIME NOT NULL COMMENT '过期时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-有效，0-无效',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_key_type_status (key_type, status),
    INDEX idx_expiry_time (expiry_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全密钥表';
```

2. 添加依赖：

```xml
<dependency>
    <groupId>com.livelab.security</groupId>
    <artifactId>security-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

3. 配置密钥数据库（security-db.properties）：

```properties
# Security Key Database Configuration
security.datasource.url=jdbc:mysql://localhost:3306/security_key_db?useUnicode=true&characterEncoding=utf-8&useSSL=false
security.datasource.username=security_admin
security.datasource.password=security_123456
security.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Connection Pool Configuration
security.datasource.minimum-idle=5
security.datasource.maximum-pool-size=20
security.datasource.idle-timeout=300000
security.datasource.connection-timeout=20000
security.datasource.pool-name=SecurityHikariCP
```

4. 配置加密属性（application.yml）：

```yaml
security:
  crypto:
    phone-key: your-phone-key
    email-key: your-email-key
    id-card-key: your-id-card-key
    algorithm: SM4
    key-expire-minutes: 30
```

5. 在实体类中使用注解：

```java
public class User {
    @Encrypt(keyType = "PHONE_KEY")
    @Decrypt
    @Digest
    private String phone;
    
    // 用于存储摘要值的字段
    private String phoneDigest;
}
```

## 数据源说明

starter 使用独立的配置文件（security-db.properties）管理密钥数据库连接，这样做的好处是：

1. 配置隔离：密钥数据库配置与业务数据库配置完全分离
2. 安全管理：密钥数据库的连接信息可以单独管理和保护
3. 灵活部署：可以轻松将密钥数据库部署在不同的服务器上
4. 独立维护：可以独立修改密钥数据库配置而不影响业务配置

## 密钥管理

starter 提供了两种密钥管理方式：

1. 配置文件方式：
   - 在 application.yml 中配置密钥
   - 适合简单场景和开发测试

2. 数据库方式：
   - 密钥存储在独立的数据库中
   - 支持密钥自动轮换
   - 支持密钥过期处理
   - 适合生产环境

当同时配置了两种方式时，系统会：
1. 优先使用数据库中的有效密钥
2. 如果数据库中没有有效密钥，则使用配置文件中的密钥，并将其保存到数据库
3. 定期清理过期的密钥

## 注解说明

- `@Encrypt`: 标记需要加密的字段
- `@Decrypt`: 标记需要解密的字段
- `@Digest`: 标记需要计算摘要的字段

## 异常处理

starter 提供了全局异常处理，会自动捕获并处理加解密过程中的异常。

## 最佳实践

1. 使用独立的数据库用户管理密钥数据库
2. 为密钥数据库配置适当的访问权限
3. 定期备份密钥数据库
4. 监控密钥数据库的连接状态
5. 在生产环境中使用数据库管理密钥
6. 定期轮换密钥（通过设置 key-expire-minutes）
7. 为敏感字段同时使用加密和摘要
8. 使用摘要字段进行查询操作
