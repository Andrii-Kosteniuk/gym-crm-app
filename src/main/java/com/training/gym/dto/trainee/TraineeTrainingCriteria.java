package com.training.gym.dto.trainee;

import java.time.LocalDate;

public record TraineeTrainingCriteria(
        LocalDate fromDate,
        LocalDate toDate,
        String trainerName,
        String trainingType
) {
}
