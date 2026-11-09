package com.training.gym.facade;

import com.training.gym.dto.auth.Credentials;
import com.training.gym.dto.trainee.CreateTraineeDto;
import com.training.gym.dto.trainee.UpdateTraineeDto;
import com.training.gym.dto.trainer.CreateTrainerDto;
import com.training.gym.dto.trainer.UpdateTrainerDto;
import com.training.gym.dto.training.CreateTrainingDto;
import com.training.gym.model.Trainee;
import com.training.gym.model.Trainer;
import com.training.gym.model.Training;
import com.training.gym.service.TraineeService;
import com.training.gym.service.TrainerService;
import com.training.gym.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GymCrmFacade {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public Trainee createTrainee(CreateTraineeDto dto) {
        return traineeService.createTrainee(dto);
    }

    public Trainee updateTrainee(Credentials credentials, UpdateTraineeDto dto) {
        return traineeService.updateTrainee(credentials, dto);
    }

    public Trainee getTraineeById(Credentials credentials, Long id) {
        return traineeService.getTraineeById(credentials, id);
    }

    public void deleteTraineeByUserName(Credentials credentials, String username) {
        traineeService.removeTraineeByUsername(credentials, username);
    }

    public Trainer createTrainer(CreateTrainerDto dto) {
        return trainerService.createTrainer(dto);
    }

    public Trainer updateTrainer(Credentials credentials, UpdateTrainerDto dto) {
        return trainerService.updateTrainer(credentials, dto);
    }

    public Trainer getTrainerById(Credentials credentials, Long id) {
        return trainerService.getTrainerById(credentials, id);
    }

    public Training createTraining(Credentials credentials, CreateTrainingDto dto) {
        return trainingService.createTraining(credentials, dto);
    }

    public Training getTrainingById(Long id) {
        return trainingService.getTrainingById(id);
    }


}


