package com.livelab.security.starter.config;

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "com.livelab.security.starter.mapper", sqlSessionTemplateRef = "securitySqlSessionTemplate")
public class SecurityDataSourceConfig {

    @Value("${security.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${security.datasource.url}")
    private String jdbcUrl;

    @Value("${security.datasource.username}")
    private String username;

    @Value("${security.datasource.password}")
    private String password;

    @Value("${security.datasource.minimum-idle:5}")
    private int minimumIdle;

    @Value("${security.datasource.maximum-pool-size:20}")
    private int maximumPoolSize;

    @Value("${security.datasource.idle-timeout:300000}")
    private long idleTimeout;

    @Value("${security.datasource.connection-timeout:20000}")
    private long connectionTimeout;

    @Value("${security.datasource.pool-name:SecurityHikariCP}")
    private String poolName;

    @Bean(name = "securityDataSource")
    public DataSource securityDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setMinimumIdle(minimumIdle);
        dataSource.setMaximumPoolSize(maximumPoolSize);
        dataSource.setIdleTimeout(idleTimeout);
        dataSource.setConnectionTimeout(connectionTimeout);
        dataSource.setPoolName(poolName);
        return dataSource;
    }

    @Bean(name = "securitySqlSessionFactory")
    public SqlSessionFactory securitySqlSessionFactory(@Qualifier("securityDataSource") DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*.xml"));
        bean.setTypeAliasesPackage("com.livelab.security.starter.entity");
        return bean.getObject();
    }

    @Bean(name = "securityTransactionManager")
    public DataSourceTransactionManager securityTransactionManager(@Qualifier("securityDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "securitySqlSessionTemplate")
    public SqlSessionTemplate securitySqlSessionTemplate(@Qualifier("securitySqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
