package com.training.gym.service;

import com.training.gym.dao.TraineeRepository;
import com.training.gym.dao.TrainerRepository;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.exception.AccessDeniedException;
import com.training.gym.exception.AuthenticationFailedException;
import com.training.gym.model.Trainee;
import com.training.gym.model.Trainer;
import com.training.gym.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileAuthorizationServiceTest {
    @Mock CredentialService credentialService;
    @Mock TraineeRepository traineeRepository;
    @Mock TrainerRepository trainerRepository;
    @InjectMocks ProfileAuthorizationService service;

    Credentials credentials;
    User user;
    Trainee trainee;
    Trainer trainer;

    @BeforeEach
    void setUp() {
        credentials = new Credentials("james.bond", "password");
        user = User.builder().userName("james.bond").build();
        trainee = Trainee.builder().id(1L).user(user).build();
        trainer = Trainer.builder().id(2L).user(user).build();
    }

    @Test
    void resolvesAuthenticatedTrainee() {
        when(credentialService.authenticate(credentials)).thenReturn(user);
        when(traineeRepository.findByUserName("james.bond")).thenReturn(Optional.of(trainee));
        assertSame(trainee, service.requireTrainee(credentials));
        verifyNoInteractions(trainerRepository);
    }

    @Test
    void rejectsUserWithoutTraineeProfile() {
        when(credentialService.authenticate(credentials)).thenReturn(user);
        when(traineeRepository.findByUserName("james.bond")).thenReturn(Optional.empty());
        assertThrows(AuthenticationFailedException.class, () -> service.requireTrainee(credentials));
    }

    @Test
    void resolvesAuthenticatedTrainer() {
        when(credentialService.authenticate(credentials)).thenReturn(user);
        when(trainerRepository.findByUserName("james.bond")).thenReturn(Optional.of(trainer));
        assertSame(trainer, service.requireTrainer(credentials));
        verifyNoInteractions(traineeRepository);
    }

    @Test
    void rejectsUserWithoutTrainerProfile() {
        when(credentialService.authenticate(credentials)).thenReturn(user);
        when(trainerRepository.findByUserName("james.bond")).thenReturn(Optional.empty());
        assertThrows(AuthenticationFailedException.class, () -> service.requireTrainer(credentials));
    }

    @Test
    void allowsTraineeOwner() {
        when(credentialService.authenticate(credentials)).thenReturn(user);
        when(traineeRepository.findByUserName("james.bond")).thenReturn(Optional.of(trainee));
        assertSame(trainee, service.requireTraineeOwner(credentials, "james.bond"));
    }

    @Test
    void rejectsDifferentTraineeOwner() {
        when(credentialService.authenticate(credentials)).thenReturn(user);
        when(traineeRepository.findByUserName("james.bond")).thenReturn(Optional.of(trainee));
        assertThrows(AccessDeniedException.class,
                () -> service.requireTraineeOwner(credentials, "another.user"));
    }

    @Test
    void allowsTrainerOwner() {
        when(credentialService.authenticate(credentials)).thenReturn(user);
        when(trainerRepository.findByUserName("james.bond")).thenReturn(Optional.of(trainer));
        assertSame(trainer, service.requireTrainerOwner(credentials, "james.bond"));
    }

    @Test
    void rejectsDifferentTrainerOwner() {
        when(credentialService.authenticate(credentials)).thenReturn(user);
        when(trainerRepository.findByUserName("james.bond")).thenReturn(Optional.of(trainer));
        assertThrows(AccessDeniedException.class,
                () -> service.requireTrainerOwner(credentials, "another.user"));
    }

    @Test
    void propagatesAuthenticationFailureWithoutRepositoryAccess() {
        when(credentialService.authenticate(credentials)).thenThrow(new AuthenticationFailedException());
        assertThrows(AuthenticationFailedException.class, () -> service.requireTrainee(credentials));
        verifyNoInteractions(traineeRepository, trainerRepository);
    }
}
