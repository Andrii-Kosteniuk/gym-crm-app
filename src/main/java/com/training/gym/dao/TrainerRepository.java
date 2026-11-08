package com.training.gym.dao;

import com.training.gym.dto.trainer.TrainerTrainingCriteria;
import com.training.gym.model.Trainer;
import com.training.gym.model.Training;

import java.util.List;
import java.util.Optional;

public interface TrainerRepository extends CrudRepository<Trainer, Long> {

    Trainer update(Trainer trainer);

    Optional<Trainer> findByUsername(String username);

    void delete(Trainer trainer);

    void deleteByUsername(String username);

    List<Training> findTrainings(String username, TrainerTrainingCriteria criteria);
}



