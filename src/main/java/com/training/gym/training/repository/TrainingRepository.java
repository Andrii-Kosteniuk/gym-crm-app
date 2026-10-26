package com.training.gym.training.repository;

import com.training.gym.common.repository.CrudRepository;
import com.training.gym.training.domain.Training;

public interface TrainingRepository extends CrudRepository<Training, Long> {

    boolean existsByTraineeOrTrainerId(Long id);
}


