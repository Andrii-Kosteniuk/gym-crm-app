package com.training.gym.config.initialization;

import com.training.gym.trainee.domain.Trainee;
import com.training.gym.trainer.domain.Trainer;
import com.training.gym.training.domain.Training;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Slf4j
public final class StorageInitializationPostProcessor implements BeanPostProcessor {

    private final InitialStorageData initialData;

    public StorageInitializationPostProcessor(InitialStorageData initialData) {
        this.initialData = Objects.requireNonNull(initialData);
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, String beanName ) {

        switch (beanName) {
            case "traineeStorage" -> initialize(
                    bean,
                    initialData.trainees(),
                    Trainee::getId,
                    beanName
            );

            case "trainerStorage" -> initialize(
                    bean,
                    initialData.trainers(),
                    Trainer::getId,
                    beanName
            );

            case "trainingStorage" -> initialize(
                    bean,
                    initialData.trainings(),
                    Training::getId,
                    beanName
            );

            default -> {
                return bean;
            }
        }

        return bean;
    }

    @SuppressWarnings("unchecked")
    private <T> void initialize(Object bean, List<T> entities, Function<T, Long> idExtractor, String beanName) {

        if (!(bean instanceof Map<?, ?>)) {
            throw new IllegalStateException("Storage bean must be a Map: " + beanName);
        }

        Map<Long, T> storage = (Map<Long, T>) bean;

        if (!storage.isEmpty()) {
            throw new IllegalStateException("Storage is already initialized: " + beanName);
        }

        for (T entity : entities) {
            storage.put(idExtractor.apply(entity), entity);
        }

        log.info("Initialized storage bean '{}' with {} records", beanName, storage.size());
    }

}