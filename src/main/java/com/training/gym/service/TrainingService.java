package com.training.gym.service;

import com.training.gym.dto.auth.Credentials;
import com.training.gym.model.Training;
import com.training.gym.dto.training.CreateTrainingDto;

public interface TrainingService {
    Training createTraining(Credentials credentials, CreateTrainingDto trainingDto);
    Training getTrainingById(Credentials credentials, Long id);
}


