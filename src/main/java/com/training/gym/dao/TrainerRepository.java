package com.training.gym.dao;

import com.training.gym.dto.trainer.TrainerTrainingCriteria;
import com.training.gym.model.Trainer;
import com.training.gym.model.Training;

import java.util.List;
import java.util.Optional;

public interface TrainerRepository extends CrudRepository<Trainer, Long> {

    Trainer update(Trainer trainer);

    Optional<Trainer> findByUserName(String username);

    void delete(Trainer trainer);

    void deleteByUsername(String username);

    void activateTrainer(Trainer trainer);

    void deactivateTrainer(Trainer trainer);

    List<Training> findTrainings(String username, TrainerTrainingCriteria criteria);

    List<Trainer> findUnassignedTrainers(String username);
}




