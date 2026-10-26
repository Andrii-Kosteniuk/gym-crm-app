package com.training.gym.training.service;

import com.training.gym.training.domain.Training;
import com.training.gym.training.dto.CreateTrainingDto;

public interface TrainingService {
    Training createTraining(CreateTrainingDto trainingDto);
    Training getTrainingById(Long id);
}


