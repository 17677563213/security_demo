# 数据安全加密系统

这是一个基于Spring Boot的数据安全加密系统，提供了完整的数据加密、解密、脱敏和摘要功能。系统使用AOP方式实现，对业务代码无侵入，同时支持灵活的配置和扩展。

## 功能特性

- **数据加密解密**：使用国密SM4算法进行数据加密和解密
- **数据脱敏**：支持手机号、邮箱、身份证等敏感信息的脱敏处理
- **数据摘要**：使用国密SM3算法生成数据摘要，用于数据完整性校验
- **密钥管理**：支持多密钥管理，可配置不同字段使用不同的密钥
- **AOP实现**：使用Spring AOP实现，对业务代码无侵入
- **注解驱动**：通过注解即可实现加密、解密、脱敏和摘要功能

## 技术栈

- Spring Boot 2.7.0
- MyBatis-Plus 3.5.3
- BouncyCastle (用于国密算法实现)
- MySQL 8.0
- Maven

## 系统架构

### 核心组件

1. **CryptoUtil**: 加密解密工具类
   - 实现SM4算法的加密解密
   - 支持密文格式：`$keyId$encryptedContent`
   - 异常处理和日志记录

2. **MaskUtil**: 数据脱敏工具类
   - 支持多种脱敏类型（手机号、邮箱、身份证等）
   - 智能识别加密数据，避免重复处理
   - 异常处理和日志记录

3. **KeyManager**: 密钥管理器
   - 支持多密钥管理
   - 密钥的Base64编码处理
   - 配置文件驱动的密钥管理

4. **DataSecurityAspect**: 安全处理切面
   - 处理数据的保存和查询操作
   - 自动进行加密、解密、脱敏和摘要处理
   - 异常处理和日志记录

### 注解说明

1. **@Encrypt**: 标记需要加密的字段
   - 参数：keyId - 使用的密钥ID

2. **@Decrypt**: 标记需要解密的字段
   - 配合@Encrypt使用，在查询时自动解密

3. **@Mask**: 标记需要脱敏的字段
   - 参数：type - 脱敏类型（PHONE/EMAIL/ID_CARD等）

4. **@Digest**: 标记需要生成摘要的字段
   - 自动生成对应的摘要字段（字段名+Digest）

## 配置说明

### application.yml配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/security_demo
    username: your_username
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

security:
  key:
    phone-key: MTIzNDU2Nzg5MDEyMzQ1Ng==    # Base64编码的16字节密钥
    email-key: OTg3NjU0MzIxMDEyMzQ1Ng==    # Base64编码的16字节密钥
    id-card-key: NjU0MzIxMDk4NzY1NDMyMQ==  # Base64编码的16字节密钥
```

## 使用示例

### 实体类定义

```java
@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    @Mask(type = Mask.MaskType.PHONE)
    @Decrypt
    @Encrypt(keyId = "PHONE_KEY")
    @Digest
    private String phone;
    
    @Mask(type = Mask.MaskType.EMAIL)
    @Decrypt
    @Encrypt(keyId = "EMAIL_KEY")
    @Digest
    private String email;
    
    @Mask(type = Mask.MaskType.ID_CARD)
    @Decrypt
    @Encrypt(keyId = "ID_CARD_KEY")
    @Digest
    private String idCard;
    
    private String password;
    
    private String phoneDigest;
    private String emailDigest;
    private String idCardDigest;
    
    @TableLogic
    private Integer deleted;
}
```

### 数据库表结构

```sql
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `id_card` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `phone_digest` varchar(64) DEFAULT NULL,
  `email_digest` varchar(64) DEFAULT NULL,
  `id_card_digest` varchar(64) DEFAULT NULL,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 数据处理流程

1. **保存数据流程**
   - 接收原始数据
   - 生成数据摘要
   - 加密敏感字段
   - 保存到数据库

2. **查询数据流程**
   - 查询数据库获取记录
   - 解密加密字段
   - 对解密后的数据进行脱敏
   - 返回处理后的数据

## 安全考虑

1. **密钥管理**
   - 密钥以Base64编码存储
   - 支持不同字段使用不同密钥
   - 建议在生产环境使用密钥管理系统

2. **数据安全**
   - 使用国密算法保证数据安全
   - 支持数据完整性校验
   - 敏感数据脱敏展示

3. **异常处理**
   - 完善的异常处理机制
   - 详细的日志记录
   - 优雅的错误返回

## 最佳实践

1. **密钥管理**
   - 定期轮换密钥
   - 使用配置中心管理密钥
   - 避免密钥硬编码

2. **性能优化**
   - 合理使用缓存
   - 避免重复加解密
   - 批量处理优化

3. **安全建议**
   - 使用HTTPS传输
   - 实施访问控制
   - 记录安全审计日志

## 注意事项

1. 确保密钥长度符合SM4算法要求（16字节）
2. 注意加密字段的长度要预留足够空间
3. 建议对敏感操作进行日志记录
4. 在生产环境中要做好密钥备份
5. 建议定期进行安全审计

## 开发计划

- [ ] 支持更多加密算法
- [ ] 添加密钥轮换功能
- [ ] 实现分布式缓存支持
- [ ] 添加更多数据脱敏规则
- [ ] 支持自定义加密策略
