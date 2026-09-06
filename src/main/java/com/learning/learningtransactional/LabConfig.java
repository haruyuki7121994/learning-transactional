package com.learning.learningtransactional;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableAsync
@EnableTransactionManagement(order = 100)
public class LabConfig {
  @Bean
  @Primary
  JpaTransactionManager transactionManager(EntityManagerFactory emf) {
    return new JpaTransactionManager(emf);
  }

  // Separate JDBC labs: savepoints don't restore JPA's persistence context.
  @Bean
  DataSourceTransactionManager jdbcTxManager(DataSource ds) {
    return new DataSourceTransactionManager(ds);
  }
}
