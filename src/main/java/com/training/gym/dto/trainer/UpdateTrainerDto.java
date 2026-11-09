package com.training.gym.dto.trainer;

import com.training.gym.model.TrainingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateTrainerDto(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String userName,
        @NotNull TrainingType specialization
) {
}
