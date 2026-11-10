package com.training.gym.service;

import com.training.gym.dao.TraineeRepository;
import com.training.gym.dao.TrainingRepository;
import com.training.gym.dao.TrainingTypeRepository;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.dto.training.CreateTrainingDto;
import com.training.gym.exception.AccessDeniedException;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.mapper.TrainingMapper;
import com.training.gym.model.*;
import com.training.gym.validation.TrainingDtoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {
    private static final long TRAINER_ID = 2L, TRAINEE_ID = 3L, TYPE_ID = 4L, TRAINING_ID = 5L;
    @Mock
    TrainingRepository trainingRepository;
    @Mock
    TraineeRepository traineeRepository;
    @Mock
    TrainingTypeRepository trainingTypeRepository;
    @Mock
    TrainingMapper trainingMapper;
    @Mock
    TrainingDtoValidator trainingValidator;
    @Mock
    ProfileAuthorizationService authorizationService;
    @Mock
    CredentialService credentialService;
    @InjectMocks
    TrainingServiceImpl service;

    Credentials credentials;
    Trainer trainer;
    Trainee trainee;
    TrainingType type;
    Training training;
    CreateTrainingDto dto;

    @BeforeEach
    void setUp() {
        credentials = new Credentials("trainer.user", "password");
        trainer = Trainer.builder().id(TRAINER_ID).user(User.builder().userName("trainer.user").build()).build();
        trainee = Trainee.builder().id(TRAINEE_ID).user(User.builder().userName("trainee.user").build()).build();
        type = TrainingType.builder().id(TYPE_ID).name("Fitness").build();
        training = Training.builder().id(TRAINING_ID).trainer(trainer).trainee(trainee).trainingType(type).build();
        dto = new CreateTrainingDto(TRAINEE_ID, TRAINER_ID, "Session", TYPE_ID,
                LocalDate.of(2026, 11, 10), 60L);
    }

    @Nested
    class Creation {
        @Test
        void createsTrainingUsingAuthorizedTrainerAndResolvedEntities() {
            when(authorizationService.requireTrainer(credentials))
                    .thenReturn(trainer);

            when(traineeRepository.findById(TRAINEE_ID))
                    .thenReturn(Optional.of(trainee));

            when(trainingTypeRepository.findById(TYPE_ID))
                    .thenReturn(Optional.of(type));

            when(trainingMapper.toTraining(dto, trainee, trainer, type))
                    .thenReturn(training);

            when(trainingRepository.save(training)).thenReturn(training);
            assertSame(training, service.createTraining(credentials, dto));
            InOrder order = inOrder(authorizationService, trainingValidator, traineeRepository,
                    trainingTypeRepository, trainingMapper, trainingRepository);
            order.verify(authorizationService).requireTrainer(credentials);
            order.verify(trainingValidator).validateCreate(dto);
            order.verify(traineeRepository).findById(TRAINEE_ID);
            order.verify(trainingTypeRepository).findById(TYPE_ID);
            order.verify(trainingMapper).toTraining(dto, trainee, trainer, type);
            order.verify(trainingRepository).save(training);
        }

        @Test
        void rejectsUnauthenticatedTrainerBeforeValidationOrPersistence() {
            when(authorizationService.requireTrainer(credentials)).thenThrow(new SecurityException("Denied"));
            assertThrows(SecurityException.class, () -> service.createTraining(credentials, dto));
            verifyNoInteractions(trainingValidator, traineeRepository, trainingTypeRepository, trainingRepository);
        }

        @Test
        void rejectsInvalidDtoBeforeLookingUpReferences() {
            when(authorizationService.requireTrainer(credentials)).thenReturn(trainer);
            doThrow(new IllegalArgumentException("Invalid DTO")).when(trainingValidator).validateCreate(dto);
            assertThrows(IllegalArgumentException.class, () -> service.createTraining(credentials, dto));
            verifyNoInteractions(traineeRepository, trainingTypeRepository, trainingRepository);
        }

        @Test
        void rejectsTrainerIdMismatch() {
            when(authorizationService.requireTrainer(credentials)).thenReturn(trainer);
            CreateTrainingDto other = new CreateTrainingDto(TRAINEE_ID, 99L, "Session", TYPE_ID,
                    dto.date(), dto.duration());
            assertThrows(AccessDeniedException.class, () -> service.createTraining(credentials, other));
            verifyNoInteractions(traineeRepository, trainingTypeRepository, trainingRepository);
        }

        @Test
        void rejectsMissingTrainee() {
            when(authorizationService.requireTrainer(credentials)).thenReturn(trainer);
            when(traineeRepository.findById(TRAINEE_ID)).thenReturn(Optional.empty());
            assertThrows(EntityNotFoundException.class, () -> service.createTraining(credentials, dto));
            verifyNoInteractions(trainingTypeRepository, trainingRepository);
        }

        @Test
        void rejectsMissingTrainingType() {
            when(authorizationService.requireTrainer(credentials)).thenReturn(trainer);
            when(traineeRepository.findById(TRAINEE_ID)).thenReturn(Optional.of(trainee));
            when(trainingTypeRepository.findById(TYPE_ID)).thenReturn(Optional.empty());
            assertThrows(EntityNotFoundException.class, () -> service.createTraining(credentials, dto));
            verifyNoInteractions(trainingRepository);
        }

        @Test
        void propagatesPersistenceFailure() {
            when(authorizationService.requireTrainer(credentials)).thenReturn(trainer);
            when(traineeRepository.findById(TRAINEE_ID)).thenReturn(Optional.of(trainee));
            when(trainingTypeRepository.findById(TYPE_ID)).thenReturn(Optional.of(type));
            when(trainingMapper.toTraining(dto, trainee, trainer, type)).thenReturn(training);
            when(trainingRepository.save(training)).thenThrow(new IllegalStateException("Database failure"));
            assertThrows(IllegalStateException.class, () -> service.createTraining(credentials, dto));
        }
    }

    @Nested
    class Lookup {
        @Test
        void allowsTrainerParticipantToReadTraining() {
            when(credentialService.authenticate(credentials)).thenReturn(trainer.getUser());
            when(trainingRepository.findById(TRAINING_ID)).thenReturn(Optional.of(training));
            assertSame(training, service.getTrainingById(credentials, TRAINING_ID));
        }

        @Test
        void allowsTraineeParticipantToReadTraining() {
            when(credentialService.authenticate(credentials)).thenReturn(trainee.getUser());
            when(trainingRepository.findById(TRAINING_ID)).thenReturn(Optional.of(training));
            assertSame(training, service.getTrainingById(credentials, TRAINING_ID));
        }

        @Test
        void hidesTrainingFromNonParticipant() {
            when(credentialService.authenticate(credentials)).thenReturn(User.builder().userName("stranger").build());
            when(trainingRepository.findById(TRAINING_ID)).thenReturn(Optional.of(training));
            assertThrows(EntityNotFoundException.class, () -> service.getTrainingById(credentials, TRAINING_ID));
        }

        @Test
        void throwsWhenTrainingDoesNotExist() {
            when(credentialService.authenticate(credentials)).thenReturn(trainer.getUser());
            when(trainingRepository.findById(TRAINING_ID)).thenReturn(Optional.empty());
            assertThrows(EntityNotFoundException.class, () -> service.getTrainingById(credentials, TRAINING_ID));
        }

        @Test
        void doesNotQueryRepositoryWhenAuthenticationFails() {
            when(credentialService.authenticate(credentials)).thenThrow(new SecurityException("Denied"));
            assertThrows(SecurityException.class, () -> service.getTrainingById(credentials, TRAINING_ID));
            verifyNoInteractions(trainingRepository);
        }
    }
}
