package com.training.gym.service;

import com.training.gym.dao.TraineeRepository;
import com.training.gym.dao.TrainerRepository;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.exception.AccessDeniedException;
import com.training.gym.exception.AuthenticationFailedException;
import com.training.gym.model.Trainee;
import com.training.gym.model.Trainer;
import com.training.gym.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@
        Service
@RequiredArgsConstructor
public class ProfileAuthorizationService {

    private final CredentialService credentialService;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    public Trainee requireTrainee(Credentials credentials) {
        User user = credentialService.authenticate(credentials);

        return traineeRepository.findByUserName(user.getUserName())
                .orElseThrow(AuthenticationFailedException::new);
    }

    public Trainer requireTrainer(Credentials credentials) {
        User user = credentialService.authenticate(credentials);

        return trainerRepository.findByUserName(user.getUserName())
                .orElseThrow(AuthenticationFailedException::new);
    }

    public Trainee requireTraineeOwner(Credentials credentials,String targetUsername) {
        Trainee trainee = requireTrainee(credentials);

        if (!trainee.getUser().getUserName().equals(targetUsername)) {
            throw new AccessDeniedException("Not authorized to modify this trainee");
        }

        return trainee;
    }

    public Trainer requireTrainerOwner(Credentials credentials,String targetUsername) {
        Trainer trainer = requireTrainer(credentials);

        if (!trainer.getUser().getUserName().equals(targetUsername)) {
            throw new AccessDeniedException("Not authorized to modify this trainer");
        }

        return trainer;
    }
}