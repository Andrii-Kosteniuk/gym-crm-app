package com.training.gym.trainee.repository;

import com.training.gym.common.repository.CrudRepository;
import com.training.gym.trainee.domain.Trainee;

import java.util.Optional;

public interface TraineeRepository extends CrudRepository<Trainee, Long> {

    Optional<Trainee> findByUserName(String username);
    boolean existsByUserName(String userName);
}
