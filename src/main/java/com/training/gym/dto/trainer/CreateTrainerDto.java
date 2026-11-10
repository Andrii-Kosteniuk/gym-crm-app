package com.training.gym.dto.trainer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTrainerDto(
        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        @NotNull
        Long specializationId
) {}
