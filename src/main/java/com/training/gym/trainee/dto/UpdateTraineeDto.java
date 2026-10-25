package com.training.gym.trainee.dto;

import com.training.gym.trainee.domain.Address;

import java.time.LocalDate;

public record UpdateTraineeDto(
        String firsName,
        String lastName,
        String userName,
        LocalDate dateOfBirth,
        Address address
) {
}
