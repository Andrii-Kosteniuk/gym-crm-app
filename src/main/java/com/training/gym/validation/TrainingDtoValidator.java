package com.training.gym.validation;

import com.training.gym.dto.training.CreateTrainingDto;
import org.springframework.stereotype.Component;

import static com.training.gym.validation.ValidationUtils.*;

@Component
public class TrainingDtoValidator {

    public void validateCreate(CreateTrainingDto dto) {
        requireNotNull(dto, "CreateTrainingDto");

        requirePositiveId(dto.traineeId(), "Trainee ID");
        requirePositiveId(dto.trainerId(), "Trainer ID");

        requireNotBlank(dto.name(), "Training name");

        requireNotNull(dto.trainingTypeId(), "Training type");
        requireNotNull(dto.date(), "Training date");

        requirePositiveDuration(dto.duration());
    }
}