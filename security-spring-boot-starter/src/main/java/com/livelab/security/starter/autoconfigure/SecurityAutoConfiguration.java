package com.livelab.security.starter.autoconfigure;

import com.livelab.security.starter.aspect.DataSecurityAspect;
import com.livelab.security.starter.config.SecurityDataSourceConfig;
import com.livelab.security.starter.core.CryptoUtil;
import com.livelab.security.starter.core.KeyManager;
import com.livelab.security.starter.mapper.SecurityKeyMapper;
import com.livelab.security.starter.util.DigestUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
@EnableScheduling
@MapperScan(basePackages = "com.livelab.security.starter.mapper", sqlSessionFactoryRef = "securitySqlSessionFactory")
@Import(SecurityDataSourceConfig.class)
@AutoConfigureAfter(SecurityDataSourceConfig.class)
public class SecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public KeyManager keyManager(SecurityProperties properties, SecurityKeyMapper securityKeyMapper) {
        return new KeyManager(properties, securityKeyMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public CryptoUtil cryptoUtil(KeyManager keyManager) {
        return new CryptoUtil(keyManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public DigestUtil digestUtil() {
        return new DigestUtil();
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSecurityAspect dataSecurityAspect(CryptoUtil cryptoUtil, DigestUtil digestUtil) {
        return new DataSecurityAspect(cryptoUtil, digestUtil);
    }
}
