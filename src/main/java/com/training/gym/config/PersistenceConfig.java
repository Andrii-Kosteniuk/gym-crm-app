package com.training.gym.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@ComponentScan(basePackages = "com.training.gym")
@Configuration
@Import({
        ApplicationPropertiesConfig.class,
        DataSourceConfig.class,
        FlywayConfig.class,
        JpaConfig.class,
        ValidationConfig.class
})
public class PersistenceConfig {
}