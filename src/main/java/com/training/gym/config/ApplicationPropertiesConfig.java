package com.training.gym.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySource("classpath:application.properties")
public class ApplicationPropertiesConfig {

    @Configuration
    @Profile("dev")
    @PropertySource("classpath:application-dev.properties")
    static class DevProperties {
    }

    @Configuration
    @Profile("test")
    @PropertySource("classpath:application-test.properties")
    static class TestProperties {
    }

    @Bean
    static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
