package com.training.gym.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {


    @Bean(destroyMethod = "close")
    @Profile("dev")
    public DataSource devDataSource(
            @Value("${db.url}") String url,
            @Value("${db.username}") String username,
            @Value("${db.password}") String password,
            @Value("${db.pool.maximum-size}") int maximumSize,
            @Value("${db.pool.minimum-idle}") int minimumIdle) {


        return getHikariDataSource("GymCrmDevPool", url, username, password, maximumSize, minimumIdle);
    }


    @Bean(destroyMethod = "close")
    @Profile("test")
    public DataSource testDataSource(
            @Value("${db.url}") String url,
            @Value("${db.username}") String username,
            @Value("${db.password}") String password,
            @Value("${db.pool.maximum-size}") int maximumSize,
            @Value("${db.pool.minimum-idle}") int minimumIdle) {

        return getHikariDataSource("GymCrmTestPool", url, username, password, maximumSize, minimumIdle);
    }

    private static HikariDataSource getHikariDataSource(
            String poolName,
            String url,
            String username,
            String password,
            int maximumSize,
            int minimumIdle) {

        HikariConfig config = new HikariConfig();

        config.setPoolName(poolName);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(maximumSize);
        config.setMinimumIdle(minimumIdle);
        config.setConnectionTimeout(30_000);

        return new HikariDataSource(config);
    }
}
