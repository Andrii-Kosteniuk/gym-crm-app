package com.training.gym.facade;

import com.training.gym.dto.auth.ChangePasswordDto;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.dto.trainee.CreateTraineeDto;
import com.training.gym.dto.trainee.TraineeTrainingCriteria;
import com.training.gym.dto.trainee.UpdateTraineeDto;
import com.training.gym.dto.trainee.UpdateTraineeTrainersDto;
import com.training.gym.dto.trainer.CreateTrainerDto;
import com.training.gym.dto.trainer.TrainerTrainingCriteria;
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

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GymCrmFacade {
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    // Trainee operations
    public Trainee createTrainee(CreateTraineeDto dto) {
        return traineeService.createTrainee(dto);
    }

    public Trainee updateTrainee(Credentials credentials, UpdateTraineeDto dto) {
        return traineeService.updateTrainee(credentials, dto);
    }

    public Trainee getTraineeById(Credentials credentials, Long id) {
        return traineeService.getTraineeById(credentials, id);
    }

    public Trainee findTraineeByUsername(Credentials credentials, String username) {
        return traineeService.findByUserName(credentials, username);
    }

    public void deleteTraineeByUsername(Credentials credentials, String username) {
        traineeService.removeTraineeByUsername(credentials, username);
    }

    public void activateTrainee(Credentials credentials, Trainee trainee) {
        traineeService.activateTrainee(credentials, trainee);
    }

    public void deactivateTrainee(Credentials credentials, Trainee trainee) {
        traineeService.deactivateTrainee(credentials, trainee);
    }

    public List<Training> findTraineeTrainings(Credentials credentials, String username, TraineeTrainingCriteria criteria) {
        return traineeService.findTrainings(credentials, username, criteria);
    }

    public void changeTraineePassword(Credentials credentials, ChangePasswordDto dto) {
        traineeService.changePassword(credentials, dto);
    }

    public Set<Trainer> replaceTraineeTrainers(Credentials credentials, UpdateTraineeTrainersDto dto) {
        return traineeService.replaceTrainers(credentials, dto);
    }

    // Trainer operations
    public Trainer createTrainer(CreateTrainerDto dto) {
        return trainerService.createTrainer(dto);
    }

    public Trainer updateTrainer(Credentials credentials, UpdateTrainerDto dto) {
        return trainerService.updateTrainer(credentials, dto);
    }

    public Trainer getTrainerById(Credentials credentials, Long id) {
        return trainerService.getTrainerById(credentials, id);
    }

    public Trainer findTrainerByUsername(Credentials credentials, String username) {
        return trainerService.findByUsername(credentials, username);
    }

    public void deleteTrainerByUsername(Credentials credentials, String username) {
        trainerService.removeTrainerByUsername(credentials, username);
    }

    public void activateTrainer(Credentials credentials, Trainer trainer) {
        trainerService.activateTrainer(credentials, trainer);
    }

    public void deactivateTrainer(Credentials credentials, Trainer trainer) {
        trainerService.deactivateTrainer(credentials, trainer);
    }

    public List<Training> findTrainerTrainings(Credentials credentials, String username, TrainerTrainingCriteria criteria) {
        return trainerService.findTrainings(credentials, username, criteria);
    }

    public List<Trainer> getUnassignedTrainers(Credentials credentials, String traineeUsername) {
        return trainerService.getUnassignedTrainers(credentials, traineeUsername);
    }

    public void changeTrainerPassword(Credentials credentials, ChangePasswordDto dto) {
        trainerService.changePassword(credentials, dto);
    }

    // Training operations
    public Training createTraining(Credentials credentials, CreateTrainingDto dto) {
        return trainingService.createTraining(credentials, dto);
    }

    public Training getTrainingById(Credentials credentials, Long id) {
        return trainingService.getTrainingById(credentials, id);
    }
}
