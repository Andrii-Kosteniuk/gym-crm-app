package com.training.gym.trainer.service;

import com.training.gym.trainer.domain.Trainer;
import com.training.gym.trainer.dto.CreateTrainerDto;
import com.training.gym.trainer.dto.UpdateTrainerDto;

public interface TrainerService {

    Trainer createTrainer(CreateTrainerDto trainerDto);
    Trainer updateTrainer(UpdateTrainerDto trainerDto);
    Trainer getTrainerById(Long id);
}
