package com.training.gym.trainer.repository;

import com.training.gym.common.repository.CrudRepository;
import com.training.gym.trainer.domain.Trainer;

public interface TrainerRepository extends CrudRepository<Trainer, Long> {

    boolean existsByUserName(String userName);
}


