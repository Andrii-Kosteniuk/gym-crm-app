package com.training.gym.trainer.dto;

import com.training.gym.trainee.domain.Address;
import com.training.gym.training.domain.TrainingType;

import java.time.LocalDate;

public record UpdateTrainerDto(
        String firsName,
        String lastName,
        String userName,
        LocalDate dateOfBirth,
        Address address,
        TrainingType specialization
) {
}
