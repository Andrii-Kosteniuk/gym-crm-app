package com.training.gym.trainee.dto;

import com.training.gym.trainee.domain.Address;

import java.time.LocalDate;

public record CreateTraineeDto(
        String firsName,
        String lastName,
        LocalDate dateOfBirth,
        Address address
) {
}
