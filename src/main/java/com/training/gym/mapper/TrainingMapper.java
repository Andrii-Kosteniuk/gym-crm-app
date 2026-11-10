package com.training.gym.mapper;

import com.training.gym.dto.training.CreateTrainingDto;
import com.training.gym.model.Trainee;
import com.training.gym.model.Trainer;
import com.training.gym.model.Training;
import com.training.gym.model.TrainingType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Duration;

@Mapper(componentModel = "spring")
public interface TrainingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trainee", source = "trainee")
    @Mapping(target = "trainer", source = "trainer")
    @Mapping(target = "trainingType", source = "type")
    @Mapping(target = "trainingName", source = "dto.name")
    @Mapping(target = "trainingDate", source = "dto.date")
    @Mapping(target = "trainingDuration", source = "dto.duration")
    default Training toTraining(CreateTrainingDto dto, Trainee trainee, Trainer trainer, TrainingType trainingType) {
        return Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingType(trainingType)
                .trainingName(dto.name())
                .trainingDate(dto.date())
                .trainingDuration(mapDuration(dto.duration()))
                .build();
    }

    default Duration mapDuration(Long duration) {
        return Duration.ofMinutes(duration);
    }
}
