package com.training.gym.common.credentials.config;

import com.training.gym.common.credentials.IdGeneratorImpl;
import com.training.gym.common.credentials.generator.IdGenerator;
import com.training.gym.trainee.domain.Trainee;
import com.training.gym.trainer.domain.Trainer;
import com.training.gym.training.domain.Training;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class IdGeneratorConfig {

    @Bean("traineeIdGenerator")
    public IdGenerator traineeIdGenerator(@Qualifier("traineeStorage") Map<Long, Trainee> storage) {
        long maxId = getMaxId(storage);

        return new IdGeneratorImpl(maxId);
    }

    @Bean("trainerIdGenerator")
    public IdGenerator trainerIdGenerator(@Qualifier("trainerStorage") Map<Long, Trainer> storage) {
        long maxId = getMaxId(storage);

        return new IdGeneratorImpl(maxId);
    }

    @Bean("trainingIdGenerator")
    public IdGenerator trainingIdGenerator(@Qualifier("trainingStorage") Map<Long, Training> storage) {
        long maxId = getMaxId(storage);

        return new IdGeneratorImpl(maxId);
    }

    private static <T> long getMaxId(Map<Long, T> storage) {
        return storage.keySet()
                .stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);
    }


}
