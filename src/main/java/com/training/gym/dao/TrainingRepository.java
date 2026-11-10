package com.training.gym.dao;

import com.training.gym.dto.trainee.TraineeTrainingCriteria;
import com.training.gym.dto.trainer.TrainerTrainingCriteria;
import com.training.gym.model.Training;

import java.util.List;

public interface TrainingRepository extends CrudRepository<Training, Long> {

    List<Training> findByTraineeUsername(String username, TraineeTrainingCriteria criteria);

    List<Training> findByTrainerUsername(String username, TrainerTrainingCriteria criteria
    );
}


