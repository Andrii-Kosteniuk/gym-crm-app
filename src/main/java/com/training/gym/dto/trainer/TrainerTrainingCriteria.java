package com.training.gym.dto.trainer;

import java.time.LocalDate;

public record TrainerTrainingCriteria(
        LocalDate fromDate,
        LocalDate toDate,
        String traineeName
) {
}
