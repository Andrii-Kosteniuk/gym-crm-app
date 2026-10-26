package com.training.gym.trainer.dto;

import com.training.gym.trainee.domain.Address;
import com.training.gym.training.domain.TrainingType;

import java.time.LocalDate;

public record CreateTrainerDto(
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        Address address,
        TrainingType specialization
) {}
