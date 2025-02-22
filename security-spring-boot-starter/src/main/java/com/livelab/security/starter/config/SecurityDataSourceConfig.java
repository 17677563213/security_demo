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

/**
 * 安全模块数据源配置类
 * 
 * 该配置类主要用于设置安全模块专用的数据库连接和MyBatis配置。
 * 通过独立的数据源配置，实现了安全模块与其他业务模块的数据隔离，提高系统安全性。
 *
 * 主要功能：
 * 1. 配置独立的HikariCP数据源，用于安全模块的数据库连接
 * 2. 配置MyBatis的SqlSessionFactory，管理数据库会话
 * 3. 配置事务管理器，处理数据库事务
 * 4. 配置SqlSessionTemplate，提供线程安全的数据库操作模板
 *
 * 使用说明：
 * - 通过@Configuration注解标记为Spring配置类
 * - 通过@MapperScan注解自动扫描Mapper接口
 * - 所有配置参数都可在application.properties中自定义
 */
@Configuration
@MapperScan(basePackages = "com.livelab.security.starter.mapper", sqlSessionTemplateRef = "securitySqlSessionTemplate")
public class SecurityDataSourceConfig {

    /**
     * MySQL数据库驱动类名
     */
    @Value("${security.datasource.driver-class-name}")
    private String driverClassName;

    /**
     * 数据库连接URL
     */
    @Value("${security.datasource.url}")
    private String jdbcUrl;

    /**
     * 数据库用户名
     */
    @Value("${security.datasource.username}")
    private String username;

    /**
     * 数据库密码
     */
    @Value("${security.datasource.password}")
    private String password;

    /**
     * 连接池最小空闲连接数，默认值：5
     */
    @Value("${security.datasource.minimum-idle:5}")
    private int minimumIdle;

    /**
     * 连接池最大连接数，默认值：20
     */
    @Value("${security.datasource.maximum-pool-size:20}")
    private int maximumPoolSize;

    /**
     * 连接空闲超时时间，默认值：300000ms（5分钟）
     */
    @Value("${security.datasource.idle-timeout:300000}")
    private long idleTimeout;

    /**
     * 连接获取超时时间，默认值：20000ms（20秒）
     */
    @Value("${security.datasource.connection-timeout:20000}")
    private long connectionTimeout;

    /**
     * 连接池名称，默认值：SecurityHikariCP
     */
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
