package com.training.gym.fasade;

import com.training.gym.trainee.domain.Trainee;
import com.training.gym.trainee.dto.CreateTraineeDto;
import com.training.gym.trainee.dto.UpdateTraineeDto;
import com.training.gym.trainee.service.TraineeService;
import com.training.gym.trainer.domain.Trainer;
import com.training.gym.trainer.dto.CreateTrainerDto;
import com.training.gym.trainer.dto.UpdateTrainerDto;
import com.training.gym.trainer.service.TrainerService;
import com.training.gym.training.domain.Training;
import com.training.gym.training.dto.CreateTrainingDto;
import com.training.gym.training.service.TrainingService;
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

    public Trainee updateTrainee(UpdateTraineeDto dto) {
        return traineeService.updateTrainee(dto);
    }

    public Trainee getTraineeById(Long id) {
        return traineeService.getTraineeById(id);
    }

    public void deleteTrainee(Long id) {
        traineeService.removeTrainee(id);
    }

    public Trainer createTrainer(CreateTrainerDto dto) {
        return trainerService.createTrainer(dto);
    }

    public Trainer updateTrainer(UpdateTrainerDto dto) {
        return trainerService.updateTrainer(dto);
    }

    public Trainer getTrainerById(Long id) {
        return trainerService.getTrainerById(id);
    }

    public Training createTraining(CreateTrainingDto dto) {
        return trainingService.createTraining(dto);
    }

    public Training getTrainingById(Long id) {
        return trainingService.getTrainingById(id);
    }


}


