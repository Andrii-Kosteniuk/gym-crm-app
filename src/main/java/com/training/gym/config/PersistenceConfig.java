package com.training.gym.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

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