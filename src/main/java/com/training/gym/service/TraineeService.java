package com.training.gym.service;

import com.training.gym.dto.auth.ChangePasswordDto;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.dto.trainee.CreateTraineeDto;
import com.training.gym.dto.trainee.TraineeTrainingCriteria;
import com.training.gym.dto.trainee.UpdateTraineeDto;
import com.training.gym.dto.trainee.UpdateTraineeTrainersDto;
import com.training.gym.model.Trainee;
import com.training.gym.model.Trainer;
import com.training.gym.model.Training;

import java.util.List;
import java.util.Set;

public interface TraineeService {

    Trainee createTrainee(CreateTraineeDto trainee);

    Trainee updateTrainee(Credentials credentials, UpdateTraineeDto trainee);

    Trainee getTraineeById(Credentials credentials, Long id);

    Trainee findByUserName(Credentials credentials, String username);

    void removeTraineeByUsername(Credentials credentials, String username);

    void activateTrainee(Credentials credentials, Trainee trainee);

    void deactivateTrainee(Credentials credentials, Trainee trainee);

    List<Training> findTrainings(Credentials credentials, String username, TraineeTrainingCriteria criteria);

    void changePassword(Credentials credentials, ChangePasswordDto dto);

    Set<Trainer> replaceTrainers(Credentials credentials, UpdateTraineeTrainersDto dto);
}
