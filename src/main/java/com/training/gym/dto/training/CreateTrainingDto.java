package com.training.gym.dto.training;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record CreateTrainingDto(

        @NotNull Long traineeId,
        @NotNull Long trainerId,
        @NotBlank String name,
        @NotNull @Positive Long trainingTypeId,
        @NotNull LocalDate date,
        @NotNull @Positive Long duration
) {
}
