package com.training.gym.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record Credentials (
        @NotBlank String username,
        @NotBlank String password
) {
}
