package com.training.gym.training.dto;

import com.training.gym.training.domain.TrainingType;

import java.time.Duration;
import java.time.LocalDate;

public record CreateTrainingDto(

        Long traineeId,
        Long trainerId,
        String name,
        TrainingType type,
        LocalDate date,
        Duration duration
) {
}
