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

    /**
     * 配置安全模块专用的数据源
     * 使用HikariCP连接池，这是Spring Boot推荐的高性能数据库连接池
     * 
     * 配置项包括：
     * - 基本连接信息（驱动、URL、用户名、密码）
     * - 连接池设置（最小空闲连接数、最大连接数、超时时间等）
     * 
     * @return 配置好的HikariCP数据源
     */
    @Bean(name = "securityDataSource")
    public DataSource securityDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        // 设置JDBC驱动类名
        dataSource.setDriverClassName(driverClassName);
        // 设置数据库连接URL
        dataSource.setJdbcUrl(jdbcUrl);
        // 设置数据库用户名
        dataSource.setUsername(username);
        // 设置数据库密码
        dataSource.setPassword(password);
        // 设置连接池最小空闲连接数
        dataSource.setMinimumIdle(minimumIdle);
        // 设置连接池最大连接数
        dataSource.setMaximumPoolSize(maximumPoolSize);
        // 设置连接的最大空闲时间
        dataSource.setIdleTimeout(idleTimeout);
        // 设置连接超时时间
        dataSource.setConnectionTimeout(connectionTimeout);
        // 设置连接池名称，便于监控和调试
        dataSource.setPoolName(poolName);
        return dataSource;
    }

    /**
     * 配置安全模块专用的MyBatis SqlSessionFactory
     * SqlSessionFactory是MyBatis的核心组件，用于创建SqlSession
     * 
     * 配置包括：
     * - 使用专用数据源
     * - 配置Mapper XML文件位置
     * - 配置实体类包路径，用于类型别名
     * 
     * @param dataSource 安全模块专用数据源，通过@Qualifier指定具体的数据源Bean
     * @return 配置好的SqlSessionFactory
     * @throws Exception 当配置SqlSessionFactory失败时抛出
     */
    @Bean(name = "securitySqlSessionFactory")
    public SqlSessionFactory securitySqlSessionFactory(@Qualifier("securityDataSource") DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        // 设置数据源
        bean.setDataSource(dataSource);
        // 设置Mapper XML文件位置
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*.xml"));
        // 设置实体类包路径，MyBatis会自动将类名作为别名
        bean.setTypeAliasesPackage("com.livelab.security.starter.entity");
        return bean.getObject();
    }

    /**
     * 配置安全模块专用的事务管理器
     * 
     * @param dataSource 安全模块专用数据源，通过@Qualifier指定具体的数据源Bean
     * @return 配置好的事务管理器
     */
    @Bean(name = "securityTransactionManager")
    public DataSourceTransactionManager securityTransactionManager(@Qualifier("securityDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * 配置安全模块专用的SqlSessionTemplate
     * SqlSessionTemplate是MyBatis的SqlSession的实现类，用于执行数据库操作
     * 
     * @param sqlSessionFactory 安全模块专用的SqlSessionFactory，通过@Qualifier指定具体的SqlSessionFactory Bean
     * @return 配置好的SqlSessionTemplate
     */
    @Bean(name = "securitySqlSessionTemplate")
    public SqlSessionTemplate securitySqlSessionTemplate(@Qualifier("securitySqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
