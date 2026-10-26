package com.training.gym.trainer.repository;

import com.training.gym.common.repository.CrudRepository;
import com.training.gym.trainer.domain.Trainer;
import java.util.Optional;

public interface TrainerRepository extends CrudRepository<Trainer, Long> {

    boolean existsByUserName(String userName);

    Optional<Trainer> findByUserName(String username);
}


