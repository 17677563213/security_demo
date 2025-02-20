# User Service

这是一个示例用户服务，演示如何使用 security-spring-boot-starter 来处理敏感数据。

## 功能特性

- 用户信息的增删改查
- 敏感信息（手机号、邮箱、身份证）的加密存储
- 支持通过摘要进行查询

## 快速开始

1. 确保 MySQL 已启动并创建数据库：

```sql
CREATE DATABASE security_demo;
```

2. 创建用户表：

```sql
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50),
    phone VARCHAR(255),
    email VARCHAR(255),
    id_card VARCHAR(255),
    password VARCHAR(100),
    phone_digest VARCHAR(32),
    email_digest VARCHAR(32),
    id_card_digest VARCHAR(32),
    deleted TINYINT DEFAULT 0
);
```

3. 配置 application.yml：

```yaml
security:
  crypto:
    phone-key: your-phone-key
    email-key: your-email-key
    id-card-key: your-id-card-key
```

4. 运行应用：

```bash
mvn spring-boot:run
```

## API 接口

- POST `/api/users/save`: 创建单个用户
- POST `/api/users/batch-save`: 批量创建用户
- GET `/api/users/{id}`: 根据 ID 查询用户
- GET `/api/users/list`: 查询所有用户
- GET `/api/users/by-phone`: 根据手机号查询用户
- GET `/api/users/by-email`: 根据邮箱查询用户
- GET `/api/users/by-idcard`: 根据身份证查询用户
