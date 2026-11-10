package com.training.gym.dao;

import com.training.gym.dto.trainee.TraineeTrainingCriteria;
import com.training.gym.model.Trainee;
import com.training.gym.model.Training;

import java.util.List;
import java.util.Optional;

public interface TraineeRepository extends CrudRepository<Trainee, Long> {

    Trainee update(Trainee trainee);

    Optional<Trainee> findByUserName(String username);

    void delete(Trainee trainee);

    void deleteByUsername(String username);

    List<Training> findTrainings(String username, TraineeTrainingCriteria criteria);

    void activateTrainee(Trainee trainee);

    void deactivateTrainee(Trainee trainee);

}
