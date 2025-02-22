package com.livelab.security.starter.autoconfigure;

import com.livelab.security.starter.aspect.DataSecurityAspect;
import com.livelab.security.starter.config.SecurityDataSourceConfig;
import com.livelab.security.starter.core.CryptoUtil;
import com.livelab.security.starter.core.KeyManager;
import com.livelab.security.starter.mapper.SecurityKeyMapper;
import com.livelab.security.starter.util.DigestUtil;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 安全模块的自动配置类
 * 
 * 该配置类是安全模块的核心配置类，负责：
 * 1. 初始化和配置所有安全相关的组件
 * 2. 管理组件之间的依赖关系
 * 3. 提供配置的自动化和默认值
 *
 * 使用的Spring注解说明：
 * 
 * @Configuration: 
 *   - 标识这是一个Spring配置类
 *   - 允许通过@Bean注解声明Bean实例
 *   - 配置类本身也会被注册为Bean
 * 
 * @EnableConfigurationProperties(SecurityProperties.class):
 *   - 启用配置属性的自动绑定功能
 *   - 将application.properties中的配置项自动注入到SecurityProperties类中
 *   - 例如：security.digest.salt会被自动赋值到对应字段
 * 
 * @EnableScheduling:
 *   - 启用Spring的定时任务调度功能
 *   - 允许使用@Scheduled注解创建定时任务
 *   - 用于密钥定期更新、清理等维护工作
 * 
 * @Import(SecurityDataSourceConfig.class):
 *   - 导入数据源配置类
 *   - 确保SecurityDataSourceConfig中的所有配置被加载
 *   - 使数据源配置和当前配置形成一个整体
 * 
 * @AutoConfigureAfter(SecurityDataSourceConfig.class):
 *   - 控制配置类的加载顺序
 *   - 保证当前配置在SecurityDataSourceConfig之后初始化
 *   - 因为当前配置中的组件依赖于数据源的配置
 *
 * 组件依赖关系：
 * 1. KeyManager依赖于SecurityProperties和SecurityKeyMapper
 * 2. CryptoUtil依赖于KeyManager
 * 3. DataSecurityAspect依赖于CryptoUtil和DigestUtil
 */
@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
@EnableScheduling
@Import(SecurityDataSourceConfig.class)
@AutoConfigureAfter(SecurityDataSourceConfig.class)
public class SecurityAutoConfiguration {

    /**
     * 配置密钥管理器
     * 
     * 主要职责：
     * 1. 管理密钥的生命周期（创建、更新、删除）
     * 2. 提供密钥的存取接口
     * 3. 确保密钥的安全性和有效性
     *
     * @param properties 安全模块的配置属性，包含密钥管理的相关配置
     * @param securityKeyMapper 密钥数据访问接口，用于密钥的持久化操作
     * @return KeyManager实例
     */
    @Bean
    @ConditionalOnMissingBean
    public KeyManager keyManager(SecurityProperties properties, SecurityKeyMapper securityKeyMapper) {
        return new KeyManager(properties, securityKeyMapper);
    }

    /**
     * 配置加密工具类
     * 
     * 主要职责：
     * 1. 提供数据加密和解密功能
     * 2. 使用KeyManager获取最新的密钥
     * 3. 实现加密算法的封装
     *
     * @param keyManager 密钥管理器，提供密钥服务
     * @return CryptoUtil实例
     */
    @Bean
    @ConditionalOnMissingBean
    public CryptoUtil cryptoUtil(KeyManager keyManager) {
        return new CryptoUtil(keyManager);
    }

    /**
     * 配置摘要工具类
     * 
     * 主要职责：
     * 1. 提供数据摘要（如哈希）计算功能
     * 2. 支持多种摘要算法
     * 3. 用于数据完整性校验
     *
     * @return DigestUtil实例
     */
    @Bean
    @ConditionalOnMissingBean
    public DigestUtil digestUtil() {
        return new DigestUtil();
    }

    /**
     * 配置数据安全切面
     * 
     * 主要职责：
     * 1. 拦截需要加密/解密的方法调用
     * 2. 自动处理数据的加密和解密
     * 3. 确保数据在传输和存储过程中的安全性
     *
     * @param cryptoUtil 加密工具，用于数据加密解密
     * @param digestUtil 摘要工具，用于数据完整性校验
     * @return DataSecurityAspect实例
     */
    @Bean
    @ConditionalOnMissingBean
    public DataSecurityAspect dataSecurityAspect(CryptoUtil cryptoUtil, DigestUtil digestUtil) {
        return new DataSecurityAspect(cryptoUtil, digestUtil);
    }
}
