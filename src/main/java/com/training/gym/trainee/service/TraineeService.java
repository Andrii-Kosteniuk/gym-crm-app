package com.training.gym.trainee.service;

import com.training.gym.trainee.domain.Trainee;
import com.training.gym.trainee.dto.CreateTraineeDto;
import com.training.gym.trainee.dto.UpdateTraineeDto;

public interface TraineeService {

    Trainee createTrainee(CreateTraineeDto trainee);
    Trainee updateTrainee(UpdateTraineeDto trainee);
    Trainee getTraineeById(Long id);
    void removeTrainee(Long id);
}
