package com.training.gym.service;

import com.training.gym.dto.auth.ChangePasswordDto;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.dto.trainer.TrainerTrainingCriteria;
import com.training.gym.model.Trainer;
import com.training.gym.dto.trainer.CreateTrainerDto;
import com.training.gym.dto.trainer.UpdateTrainerDto;
import com.training.gym.model.Training;

import java.util.List;

public interface TrainerService {

    Trainer createTrainer(CreateTrainerDto dto);
    Trainer updateTrainer(Credentials credentials, UpdateTrainerDto dto);
    Trainer getTrainerById(Credentials credentials, Long id);
    Trainer findByUsername(Credentials credentials, String username);
    void removeTrainerByUsername(Credentials credentials, String username);
    void activateTrainer(Credentials credentials, Trainer trainer);
    void deactivateTrainer(Credentials credentials, Trainer trainer);
    List<Training> findTrainings(Credentials credentials, String username, TrainerTrainingCriteria criteria);
    List<Trainer> getUnassignedTrainers(Credentials credentials, String username);
    void changePassword(Credentials credentials, ChangePasswordDto dto);
}
