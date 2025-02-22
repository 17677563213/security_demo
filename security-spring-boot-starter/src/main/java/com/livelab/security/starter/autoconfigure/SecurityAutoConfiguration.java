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
 * 安全模块的自动配置类，负责配置和初始化安全相关的组件
 * 
 * 配置说明：
 * 1. 启用配置属性绑定功能，自动注入SecurityProperties中定义的配置项
 * 2. 启用定时任务功能，用于密钥定期更新等场景
 * 3. 导入数据源配置，并确保在数据源配置之后初始化
 */
@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
@EnableScheduling
@Import(SecurityDataSourceConfig.class)
@AutoConfigureAfter(SecurityDataSourceConfig.class)
public class SecurityAutoConfiguration {

    /**
     * 配置密钥管理器
     * 负责密钥的生成、存储、更新和过期处理
     * 
     * @param properties 安全模块的配置属性
     * @param securityKeyMapper 密钥数据访问接口
     * @return KeyManager实例
     */
    @Bean
    @ConditionalOnMissingBean
    public KeyManager keyManager(SecurityProperties properties, SecurityKeyMapper securityKeyMapper) {
        return new KeyManager(properties, securityKeyMapper);
    }

    /**
     * 配置加解密工具类
     * 提供SM4算法的加密和解密功能
     * 
     * @param keyManager 密钥管理器，用于获取加解密密钥
     * @return CryptoUtil实例
     */
    @Bean
    @ConditionalOnMissingBean
    public CryptoUtil cryptoUtil(KeyManager keyManager) {
        return new CryptoUtil(keyManager);
    }

    /**
     * 配置摘要工具类
     * 提供数据摘要计算功能，支持多种摘要算法
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
     * 通过AOP方式自动处理数据的加解密和摘要计算
     * 
     * @param cryptoUtil 加解密工具类
     * @param digestUtil 摘要工具类
     * @return DataSecurityAspect实例
     */
    @Bean
    @ConditionalOnMissingBean
    public DataSecurityAspect dataSecurityAspect(CryptoUtil cryptoUtil, DigestUtil digestUtil) {
        return new DataSecurityAspect(cryptoUtil, digestUtil);
    }
}
