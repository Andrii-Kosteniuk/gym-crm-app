package com.training.gym.dto.trainer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateTrainerDto(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String userName,
        @NotNull Long specializationId
) {
}
