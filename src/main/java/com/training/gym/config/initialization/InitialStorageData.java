package com.training.gym.config.initialization;

import com.training.gym.trainee.domain.Trainee;
import com.training.gym.trainer.domain.Trainer;
import com.training.gym.training.domain.Training;

import java.util.List;

public record InitialStorageData(
        List<Trainee> trainees,
        List<Trainer> trainers,
        List<Training> trainings
) {

}
