package com.ibsoft.ibMsg.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class DataSourceConfig {

   private static final Log logger = LogFactory.getLog(DataSourceConfig.class);

   @Value("${datasource.jndi.driver-class-name}")
   private String driverClassName;

   @Value("${datasource.jndi.url}")
   private String jdbcUrl;

   @Value("${datasource.jndi.username}")
   private String username;

   @Value("${datasource.jndi.password}")
   private String password;

   @Value("${datasource.jndi.max-active:20}")
   private int maxActive;

   @Value("${datasource.jndi.max-idle:10}")
   private int maxIdle;

   @Value("${datasource.jndi.min-idle:5}")
   private int minIdle;

   @Value("${datasource.jndi.initial-size:5}")
   private int initialSize;

   @Value("${datasource.jndi.test-on-borrow:true}")
   private boolean testOnBorrow;

   @Value("${datasource.jndi.validation-query:SELECT 1 FROM DUAL}")
   private String validationQuery;

   @Value("${datasource.jndi.max-wait:10000}")
   private int maxWait;

   @Bean(destroyMethod = "close")
   public DataSource dataSource() {
      logger.info("Creando pool Tomcat JDBC: " + jdbcUrl);

      PoolProperties poolProperties = new PoolProperties();
      poolProperties.setDriverClassName(driverClassName);
      poolProperties.setUrl(jdbcUrl);
      poolProperties.setUsername(username);
      poolProperties.setPassword(password);
      poolProperties.setMaxActive(maxActive);
      poolProperties.setMaxIdle(maxIdle);
      poolProperties.setMinIdle(minIdle);
      poolProperties.setInitialSize(initialSize);
      poolProperties.setTestOnBorrow(testOnBorrow);
      poolProperties.setValidationQuery(validationQuery);
      poolProperties.setMaxWait(maxWait);
      poolProperties.setDefaultAutoCommit(true);
      poolProperties.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
            + "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");

      return new DataSource(poolProperties);
   }

   @Bean(name = "transactionManager")
   public PlatformTransactionManager transactionManager(DataSource dataSource) {
      return new DataSourceTransactionManager(dataSource);
   }

   @Bean
   public JdbcTemplate jdbcTemplate(DataSource dataSource) {
      return new JdbcTemplate(dataSource);
   }
}
