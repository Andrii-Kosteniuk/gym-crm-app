package com.training.gym.dao;

import com.training.gym.model.TrainingType;

import java.util.List;
import java.util.Optional;

public interface TrainingTypeRepository {

    Optional<TrainingType> findById(Long id);

    List<TrainingType> findAll();

}




