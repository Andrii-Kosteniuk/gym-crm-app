package com.training.gym.config.initialization;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

@Configuration(proxyBeanMethods = false)
public class InitializationConfiguration {

    private InitializationConfiguration() {
    }

    @Bean
    public static BeanPostProcessor storageInitializationPostProcessor(
            @Value("${gym.storage.initial-data-file}") String location) {

        ObjectMapper mapper = JsonMapper.builder()
                .findAndAddModules()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();

        Resource resource = new DefaultResourceLoader().getResource(location);

        try (InputStream input = resource.getInputStream()) {

            InitialStorageData data = mapper.readValue(input, InitialStorageData.class);

            if (data == null) {
                throw new IllegalStateException("JSON file must contain an object");
            }

            return new StorageInitializationPostProcessor(data);

        } catch (IOException exception) {
            throw new IllegalStateException("Failed to initialize storage from: " + location, exception);
        }
    }
}