package com.training.gym.dao;

import com.training.gym.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Properties;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RepositoryIntegrationSupport.TestJpaConfig.class)
@Transactional
abstract class RepositoryIntegrationSupport {
    @PersistenceContext
    protected EntityManager em;

    @Configuration
    @EnableTransactionManagement
    @ComponentScan(basePackageClasses = JpaTraineeRepository.class,
            includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                    classes = {JpaTraineeRepository.class, JpaTrainerRepository.class,
                            JpaTrainingRepository.class, JpaTrainingTypeRepository.class, JpaUserRepository.class}),
            useDefaultFilters = false)
    static class TestJpaConfig {
        @Bean
        DataSource dataSource() {
            DriverManagerDataSource ds = new DriverManagerDataSource();
            ds.setDriverClassName("org.h2.Driver");
            ds.setUrl("jdbc:h2:mem:repository_integration;DB_CLOSE_DELAY=-1");
            ds.setUsername("sa");
            return ds;
        }

        @Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan("com.training.gym.model");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            var properties = new Properties();
            properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
            factory.setJpaProperties(properties);
            return factory;
        }

        @Bean
        PlatformTransactionManager transactionManager(jakarta.persistence.EntityManagerFactory emf) {
            return new JpaTransactionManager(emf);
        }
    }

    protected User user(String first, String last, String username) {
        return User.builder().firstName(first).lastName(last).userName(username)
                .password("encoded-password").active(true).build();
    }

    protected TrainingType type(String name) {
        TrainingType result = TrainingType.builder().name(name).build();
        em.persist(result);
        return result;
    }

    protected Trainee trainee(String username) {
        Trainee result = Trainee.builder().user(user("James", "Bond", username)).build();
        em.persist(result);
        em.flush();
        return result;
    }

    protected Trainer trainer(String username, String first, String last, TrainingType type) {
        Trainer result = Trainer.builder().user(user(first, last, username)).specialization(type).build();
        em.persist(result);
        em.flush();
        return result;
    }

    protected Training training(Trainee trainee, Trainer trainer, TrainingType type, LocalDate date) {
        Training result = Training.builder().trainee(trainee).trainer(trainer)
                .trainingType(type).trainingName("Session").trainingDate(date)
                .trainingDuration(Duration.ofMinutes(45)).build();
        em.persist(result);
        em.flush();
        return result;
    }

    protected void reload() {
        em.flush();
        em.clear();
    }
}
