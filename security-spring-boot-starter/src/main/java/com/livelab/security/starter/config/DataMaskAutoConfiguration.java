package com.livelab.security.starter.config;

import com.livelab.security.starter.aspect.DataMaskAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataMaskAutoConfiguration {
    
    @Bean
    public DataMaskAspect dataMaskAspect() {
        return new DataMaskAspect();
    }
}
