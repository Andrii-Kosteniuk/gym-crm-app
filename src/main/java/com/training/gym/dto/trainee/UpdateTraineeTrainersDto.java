package com.training.gym.dto.trainee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Set;

public record UpdateTraineeTrainersDto(
        @NotBlank String traineeUsername,
        @NotNull Set<@NotNull @Positive Long> trainerIds
) {}