package com.training.gym.facade;

import com.training.gym.dto.auth.ChangePasswordDto;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.dto.trainee.CreateTraineeDto;
import com.training.gym.dto.trainee.TraineeTrainingCriteria;
import com.training.gym.dto.trainee.UpdateTraineeDto;
import com.training.gym.dto.trainee.UpdateTraineeTrainersDto;
import com.training.gym.dto.trainer.CreateTrainerDto;
import com.training.gym.dto.trainer.TrainerTrainingCriteria;
import com.training.gym.dto.trainer.UpdateTrainerDto;
import com.training.gym.dto.training.CreateTrainingDto;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.exception.ResourceConflictException;
import com.training.gym.model.Trainee;
import com.training.gym.model.Trainer;
import com.training.gym.model.Training;
import com.training.gym.service.TraineeService;
import com.training.gym.service.TrainerService;
import com.training.gym.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GymCrmFacadeTest {

    private static final Long TRAINEE_ID = 1L;
    private static final Long TRAINER_ID = 2L;
    private static final Long TRAINING_ID = 3L;
    public static final String JAMES_BOND = "james.bond";

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @InjectMocks
    private GymCrmFacade gymCrmFacade;

    Credentials credentials;

    @BeforeEach
    void setUp() {
        credentials = mock(Credentials.class);
    }

    @Nested
    class TraineeFacadeTests {

        @Test
        void shouldDelegateCreateTraineeToTraineeService() {
            // Arrange
            CreateTraineeDto dto = mock(CreateTraineeDto.class);

            Trainee expectedTrainee = new Trainee();
            expectedTrainee.setId(TRAINEE_ID);

            when(traineeService.createTrainee(dto))
                    .thenReturn(expectedTrainee);

            // Act
            Trainee result = gymCrmFacade.createTrainee(dto);

            // Assert
            assertNotNull(result);
            assertSame(expectedTrainee, result);
            assertEquals(TRAINEE_ID, result.getId());

            verify(traineeService).createTrainee(dto);
            verifyNoInteractions(trainerService, trainingService);
        }

        @Test
        void shouldDelegateUpdateTraineeToTraineeService() {
            // Arrange
            UpdateTraineeDto dto = mock(UpdateTraineeDto.class);

            Trainee expectedTrainee = new Trainee();
            expectedTrainee.setId(TRAINEE_ID);

            when(traineeService.updateTrainee(credentials, dto))
                    .thenReturn(expectedTrainee);

            // Act
            Trainee result = gymCrmFacade.updateTrainee(credentials, dto);

            // Assert
            assertNotNull(result);
            assertSame(expectedTrainee, result);

            verify(traineeService).updateTrainee(credentials, dto);
            verifyNoInteractions(trainerService, trainingService);
        }

        @Test
        void shouldDelegateGetTraineeByIdToTraineeService() {
            // Arrange
            Trainee expectedTrainee = new Trainee();
            expectedTrainee.setId(TRAINEE_ID);

            when(traineeService.getTraineeById(credentials, TRAINEE_ID))
                    .thenReturn(expectedTrainee);

            // Act
            Trainee result = gymCrmFacade.getTraineeById(credentials, TRAINEE_ID);

            // Assert
            assertNotNull(result);
            assertSame(expectedTrainee, result);
            assertEquals(TRAINEE_ID, result.getId());

            verify(traineeService).getTraineeById(credentials, TRAINEE_ID);
            verifyNoInteractions(trainerService, trainingService);
        }

        @Test
        void shouldDelegateDeleteTraineeToTraineeService() {
            // Act
            gymCrmFacade.deleteTraineeByUsername(credentials, JAMES_BOND);

            // Assert
            verify(traineeService).removeTraineeByUsername(credentials, JAMES_BOND);

            verifyNoInteractions(
                    trainerService,
                    trainingService
            );
        }

        @Test
        void shouldPropagateExceptionWhenCreatingTraineeFails() {
            // Arrange
            CreateTraineeDto dto = mock(CreateTraineeDto.class);

            RuntimeException expectedException =
                    new RuntimeException("Trainee creation failed");

            when(traineeService.createTrainee(dto))
                    .thenThrow(expectedException);

            // Act
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> gymCrmFacade.createTrainee(dto)
            );

            // Assert
            assertSame(expectedException, exception);

            verify(traineeService).createTrainee(dto);
            verifyNoInteractions(trainerService, trainingService);
        }

        @Test
        void shouldPropagateExceptionWhenUpdatingTraineeFails() {
            // Arrange
            UpdateTraineeDto dto = mock(UpdateTraineeDto.class);

            EntityNotFoundException expectedException =
                    new EntityNotFoundException(
                            "Trainee not found"
                    );

            when(traineeService.updateTrainee(credentials, dto))
                    .thenThrow(expectedException);

            // Act
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> gymCrmFacade.updateTrainee(credentials, dto)
            );

            // Assert
            assertSame(expectedException, exception);

            verify(traineeService).updateTrainee(credentials, dto);
        }

        @Test
        void shouldPropagateExceptionWhenTraineeDoesNotExist() {
            // Arrange
            EntityNotFoundException expectedException =
                    new EntityNotFoundException(
                            "Trainee with id 1 not found"
                    );

            when(traineeService.getTraineeById(credentials, TRAINEE_ID))
                    .thenThrow(expectedException);

            // Act
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> gymCrmFacade.getTraineeById(credentials, TRAINEE_ID)
            );

            // Assert
            assertSame(expectedException, exception);

            verify(traineeService).getTraineeById(credentials, TRAINEE_ID);
        }

        @Test
        void shouldPropagateConflictExceptionWhenDeletingTrainee() {
            // Arrange
            ResourceConflictException expectedException =
                    new ResourceConflictException(
                            "Cannot delete trainee because related trainings exist"
                    );

            doThrow(expectedException)
                    .when(traineeService)
                    .removeTraineeByUsername(credentials, JAMES_BOND);

            // Act
            ResourceConflictException exception = assertThrows(
                    ResourceConflictException.class,
                    () -> gymCrmFacade.deleteTraineeByUsername(credentials, JAMES_BOND)
            );

            // Assert
            assertSame(expectedException, exception);

            verify(traineeService).removeTraineeByUsername(credentials, JAMES_BOND);
            verifyNoInteractions(trainerService, trainingService);
        }

        @Test
        void shouldPassNullDtoToTraineeServiceWithoutModification() {
            // Act
            gymCrmFacade.createTrainee(null);

            // Assert
            verify(traineeService).createTrainee(null);
        }

        @Test
        void shouldPassNullIdToTraineeServiceWithoutModification() {
            // Act
            gymCrmFacade.getTraineeById(credentials, null);

            // Assert
            verify(traineeService).getTraineeById(credentials, null);
        }


        @Test
        void shouldDelegateFindTraineeByUsernameToTraineeService() {
            // Arrange
            Trainee expectedTrainee = new Trainee();
            expectedTrainee.setId(TRAINEE_ID);

            when(traineeService.findByUserName(credentials, JAMES_BOND))
                    .thenReturn(expectedTrainee);

            // Act
            Trainee result = gymCrmFacade.findTraineeByUsername(
                    credentials, JAMES_BOND
            );

            // Assert
            assertNotNull(result);
            assertSame(expectedTrainee, result);

            verify(traineeService).findByUserName(credentials, JAMES_BOND);
            verifyNoInteractions(trainerService, trainingService);
        }

        @Test
        void shouldDelegateActivateTraineeToTraineeService() {
            // Arrange
            Trainee trainee = new Trainee();
            trainee.setId(TRAINEE_ID);

            // Act
            gymCrmFacade.activateTrainee(credentials, trainee);

            // Assert
            verify(traineeService).activateTrainee(credentials, trainee);
            verifyNoInteractions(trainerService, trainingService);
        }

        @Test
        void shouldDelegateDeactivateTraineeToTraineeService() {
            // Arrange
            Trainee trainee = new Trainee();
            trainee.setId(TRAINEE_ID);

            // Act
            gymCrmFacade.deactivateTrainee(credentials, trainee);

            // Assert
            verify(traineeService).deactivateTrainee(credentials, trainee);
            verifyNoInteractions(trainerService, trainingService);
        }

        @Test
        void shouldDelegateFindTraineeTrainingsToTraineeService() {
            // Arrange
            TraineeTrainingCriteria criteria =
                    mock(TraineeTrainingCriteria.class);

            Training training = new Training();
            training.setId(TRAINING_ID);

            List<Training> expectedTrainings = List.of(training);

            when(traineeService.findTrainings(
                    credentials, JAMES_BOND, criteria
            )).thenReturn(expectedTrainings);

            // Act
            List<Training> result = gymCrmFacade.findTraineeTrainings(
                    credentials, JAMES_BOND, criteria
            );

            // Assert
            assertNotNull(result);
            assertSame(expectedTrainings, result);
            assertEquals(1, result.size());

            verify(traineeService).findTrainings(
                    credentials, JAMES_BOND, criteria
            );
            verifyNoInteractions(trainerService, trainingService);
        }

        @Test
        void shouldDelegateChangeTraineePasswordToTraineeService() {
            // Arrange
            ChangePasswordDto dto = mock(ChangePasswordDto.class);

            // Act
            gymCrmFacade.changeTraineePassword(credentials, dto);

            // Assert
            verify(traineeService).changePassword(credentials, dto);
            verifyNoInteractions(trainerService, trainingService);
        }

        @Test
        void shouldDelegateReplaceTraineeTrainersToTraineeService() {
            // Arrange
            UpdateTraineeTrainersDto dto =
                    mock(UpdateTraineeTrainersDto.class);

            Trainer trainer = new Trainer();
            trainer.setId(TRAINER_ID);

            Set<Trainer> expectedTrainers = Set.of(trainer);

            when(traineeService.replaceTrainers(credentials, dto))
                    .thenReturn(expectedTrainers);

            // Act
            Set<Trainer> result = gymCrmFacade.replaceTraineeTrainers(
                    credentials, dto
            );

            // Assert
            assertNotNull(result);
            assertSame(expectedTrainers, result);
            assertEquals(1, result.size());
            assertTrue(result.contains(trainer));

            verify(traineeService).replaceTrainers(credentials, dto);
            verifyNoInteractions(trainerService, trainingService);
        }

    }

    @Nested
    class TrainerFacadeTests {

        @Test
        void shouldDelegateCreateTrainerToTrainerService() {
            // Arrange
            CreateTrainerDto dto = mock(CreateTrainerDto.class);

            Trainer expectedTrainer = new Trainer();
            expectedTrainer.setId(TRAINER_ID);

            when(trainerService.createTrainer(dto))
                    .thenReturn(expectedTrainer);

            // Act
            Trainer result = gymCrmFacade.createTrainer(dto);

            // Assert
            assertNotNull(result);
            assertSame(expectedTrainer, result);
            assertEquals(TRAINER_ID, result.getId());

            verify(trainerService).createTrainer(dto);
            verifyNoInteractions(traineeService, trainingService);
        }

        @Test
        void shouldDelegateUpdateTrainerToTrainerService() {
            // Arrange
            UpdateTrainerDto dto = mock(UpdateTrainerDto.class);

            Trainer expectedTrainer = new Trainer();
            expectedTrainer.setId(TRAINER_ID);

            when(trainerService.updateTrainer(credentials, dto))
                    .thenReturn(expectedTrainer);

            // Act
            Trainer result = gymCrmFacade.updateTrainer(credentials, dto);

            // Assert
            assertNotNull(result);
            assertSame(expectedTrainer, result);

            verify(trainerService).updateTrainer(credentials, dto);
            verifyNoInteractions(traineeService, trainingService);
        }

        @Test
        void shouldDelegateGetTrainerByIdToTrainerService() {
            // Arrange
            Trainer expectedTrainer = new Trainer();
            expectedTrainer.setId(TRAINER_ID);

            when(trainerService.getTrainerById(credentials, TRAINER_ID))
                    .thenReturn(expectedTrainer);

            // Act
            Trainer result = gymCrmFacade.getTrainerById(credentials, TRAINER_ID);

            // Assert
            assertNotNull(result);
            assertSame(expectedTrainer, result);
            assertEquals(TRAINER_ID, result.getId());

            verify(trainerService).getTrainerById(credentials, TRAINER_ID);
            verifyNoInteractions(traineeService, trainingService);
        }

        @Test
        void shouldPropagateExceptionWhenCreatingTrainerFails() {
            // Arrange
            CreateTrainerDto dto = mock(CreateTrainerDto.class);

            RuntimeException expectedException =
                    new RuntimeException("Trainer creation failed");

            when(trainerService.createTrainer(dto))
                    .thenThrow(expectedException);

            // Act
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> gymCrmFacade.createTrainer(dto)
            );

            // Assert
            assertSame(expectedException, exception);

            verify(trainerService).createTrainer(dto);
        }

        @Test
        void shouldPropagateExceptionWhenUpdatingTrainerFails() {
            // Arrange
            UpdateTrainerDto dto = mock(UpdateTrainerDto.class);

            EntityNotFoundException expectedException =
                    new EntityNotFoundException(
                            "Trainer not found"
                    );

            when(trainerService.updateTrainer(credentials, dto))
                    .thenThrow(expectedException);

            // Act
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> gymCrmFacade.updateTrainer(credentials, dto)
            );

            // Assert
            assertSame(expectedException, exception);

            verify(trainerService).updateTrainer(credentials, dto);
        }

        @Test
        void shouldPropagateExceptionWhenTrainerDoesNotExist() {
            // Arrange
            EntityNotFoundException expectedException =
                    new EntityNotFoundException(
                            "Trainer with id 2 not found"
                    );

            when(trainerService.getTrainerById(credentials, TRAINER_ID))
                    .thenThrow(expectedException);

            // Act
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> gymCrmFacade.getTrainerById(credentials, TRAINER_ID)
            );

            // Assert
            assertSame(expectedException, exception);

            verify(trainerService).getTrainerById(credentials, TRAINER_ID);
        }

        @Test
        void shouldPassNullDtoToTrainerServiceWithoutModification() {
            // Act
            gymCrmFacade.createTrainer(null);

            // Assert
            verify(trainerService).createTrainer(null);
        }

        @Test
        void shouldPassNullIdToTrainerServiceWithoutModification() {
            // Act
            gymCrmFacade.getTrainerById(credentials, null);

            // Assert
            verify(trainerService).getTrainerById(credentials, null);
        }


        @Test
        void shouldDelegateFindTrainerByUsernameToTrainerService() {
            // Arrange
            Trainer expectedTrainer = new Trainer();
            expectedTrainer.setId(TRAINER_ID);

            when(trainerService.findByUsername(credentials, JAMES_BOND))
                    .thenReturn(expectedTrainer);

            // Act
            Trainer result = gymCrmFacade.findTrainerByUsername(
                    credentials, JAMES_BOND
            );

            // Assert
            assertNotNull(result);
            assertSame(expectedTrainer, result);

            verify(trainerService).findByUsername(credentials, JAMES_BOND);
            verifyNoInteractions(traineeService, trainingService);
        }

        @Test
        void shouldDelegateDeleteTrainerToTrainerService() {
            // Act
            gymCrmFacade.deleteTrainerByUsername(credentials, JAMES_BOND);

            // Assert
            verify(trainerService).removeTrainerByUsername(
                    credentials, JAMES_BOND
            );
            verifyNoInteractions(traineeService, trainingService);
        }

        @Test
        void shouldDelegateActivateTrainerToTrainerService() {
            // Arrange
            Trainer trainer = new Trainer();
            trainer.setId(TRAINER_ID);

            // Act
            gymCrmFacade.activateTrainer(credentials, trainer);

            // Assert
            verify(trainerService).activateTrainer(credentials, trainer);
            verifyNoInteractions(traineeService, trainingService);
        }

        @Test
        void shouldDelegateDeactivateTrainerToTrainerService() {
            // Arrange
            Trainer trainer = new Trainer();
            trainer.setId(TRAINER_ID);

            // Act
            gymCrmFacade.deactivateTrainer(credentials, trainer);

            // Assert
            verify(trainerService).deactivateTrainer(credentials, trainer);
            verifyNoInteractions(traineeService, trainingService);
        }

        @Test
        void shouldDelegateFindTrainerTrainingsToTrainerService() {
            // Arrange
            TrainerTrainingCriteria criteria =
                    mock(TrainerTrainingCriteria.class);

            Training training = new Training();
            training.setId(TRAINING_ID);

            List<Training> expectedTrainings = List.of(training);

            when(trainerService.findTrainings(
                    credentials, JAMES_BOND, criteria
            )).thenReturn(expectedTrainings);

            // Act
            List<Training> result = gymCrmFacade.findTrainerTrainings(
                    credentials, JAMES_BOND, criteria
            );

            // Assert
            assertNotNull(result);
            assertSame(expectedTrainings, result);
            assertEquals(1, result.size());

            verify(trainerService).findTrainings(
                    credentials, JAMES_BOND, criteria
            );
            verifyNoInteractions(traineeService, trainingService);
        }

        @Test
        void shouldDelegateGetUnassignedTrainersToTrainerService() {
            // Arrange
            Trainer trainer = new Trainer();
            trainer.setId(TRAINER_ID);

            List<Trainer> expectedTrainers = List.of(trainer);

            when(trainerService.getUnassignedTrainers(
                    credentials, JAMES_BOND
            )).thenReturn(expectedTrainers);

            // Act
            List<Trainer> result = gymCrmFacade.getUnassignedTrainers(
                    credentials, JAMES_BOND
            );

            // Assert
            assertNotNull(result);
            assertSame(expectedTrainers, result);
            assertEquals(1, result.size());

            verify(trainerService).getUnassignedTrainers(
                    credentials, JAMES_BOND
            );
            verifyNoInteractions(traineeService, trainingService);
        }

        @Test
        void shouldDelegateChangeTrainerPasswordToTrainerService() {
            // Arrange
            ChangePasswordDto dto = mock(ChangePasswordDto.class);

            // Act
            gymCrmFacade.changeTrainerPassword(credentials, dto);

            // Assert
            verify(trainerService).changePassword(credentials, dto);
            verifyNoInteractions(traineeService, trainingService);
        }

    }

    @Nested
    class TrainingFacadeTests {

        @Test
        void shouldDelegateCreateTrainingToTrainingService() {
            // Arrange
            CreateTrainingDto dto = mock(CreateTrainingDto.class);

            Training expectedTraining = new Training();
            expectedTraining.setId(TRAINING_ID);

            when(trainingService.createTraining(credentials, dto))
                    .thenReturn(expectedTraining);

            // Act
            Training result = gymCrmFacade.createTraining(credentials, dto);

            // Assert
            assertNotNull(result);
            assertSame(expectedTraining, result);
            assertEquals(TRAINING_ID, result.getId());

            verify(trainingService).createTraining(credentials, dto);
            verifyNoInteractions(traineeService, trainerService);
        }

        @Test
        void shouldDelegateGetTrainingByIdToTrainingService() {
            // Arrange
            Training expectedTraining = new Training();
            expectedTraining.setId(TRAINING_ID);

            when(trainingService.getTrainingById(credentials, TRAINING_ID))
                    .thenReturn(expectedTraining);

            // Act
            Training result = gymCrmFacade.getTrainingById(credentials, TRAINING_ID);

            // Assert
            assertNotNull(result);
            assertSame(expectedTraining, result);
            assertEquals(TRAINING_ID, result.getId());

            verify(trainingService).getTrainingById(credentials, TRAINING_ID);
            verifyNoInteractions(traineeService, trainerService);
        }

        @Test
        void shouldPropagateExceptionWhenCreatingTrainingFails() {
            // Arrange
            CreateTrainingDto dto = mock(CreateTrainingDto.class);

            RuntimeException expectedException =
                    new RuntimeException("Training creation failed");

            when(trainingService.createTraining(credentials, dto))
                    .thenThrow(expectedException);

            // Act
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> gymCrmFacade.createTraining(credentials, dto)
            );

            // Assert
            assertSame(expectedException, exception);

            verify(trainingService).createTraining(credentials, dto);
        }

        @Test
        void shouldPropagateExceptionWhenTrainingDoesNotExist() {
            // Arrange
            EntityNotFoundException expectedException =
                    new EntityNotFoundException(
                            "Training with id 3 not found"
                    );

            when(trainingService.getTrainingById(credentials, TRAINING_ID))
                    .thenThrow(expectedException);

            // Act
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> gymCrmFacade.getTrainingById(credentials, TRAINING_ID)
            );

            // Assert
            assertSame(expectedException, exception);

            verify(trainingService).getTrainingById(credentials, TRAINING_ID);
        }

        @Test
        void shouldPassNullDtoToTrainingServiceWithoutModification() {
            // Act
            gymCrmFacade.createTraining(credentials, null);

            // Assert
            verify(trainingService).createTraining(credentials, null);
        }

        @Test
        void shouldPassNullIdToTrainingServiceWithoutModification() {
            // Act
            gymCrmFacade.getTrainingById(credentials, null);

            // Assert
            verify(trainingService).getTrainingById(credentials, null);
        }

// TraineeFacadeTests

        @Test
        void shouldPropagateExceptionWhenTraineeActivationFails() {
            // Arrange
            Trainee trainee = new Trainee();
            trainee.setId(TRAINEE_ID);

            IllegalStateException expectedException =
                    new IllegalStateException("Trainee is already active");

            doThrow(expectedException)
                    .when(traineeService)
                    .activateTrainee(credentials, trainee);

            // Act
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> gymCrmFacade.activateTrainee(credentials, trainee)
            );

            // Assert
            assertSame(expectedException, exception);

            verify(traineeService).activateTrainee(credentials, trainee);
            verifyNoInteractions(trainerService, trainingService);
        }

        @Test
        void shouldPropagateExceptionWhenReplacingTrainersFails() {
            // Arrange
            UpdateTraineeTrainersDto dto =
                    mock(UpdateTraineeTrainersDto.class);

            EntityNotFoundException expectedException =
                    new EntityNotFoundException("Trainer not found");

            when(traineeService.replaceTrainers(credentials, dto))
                    .thenThrow(expectedException);

            // Act
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> gymCrmFacade.replaceTraineeTrainers(credentials, dto)
            );

            // Assert
            assertSame(expectedException, exception);

            verify(traineeService).replaceTrainers(credentials, dto);
            verifyNoInteractions(trainerService, trainingService);
        }

// TrainerFacadeTests

        @Test
        void shouldPropagateExceptionWhenTrainerDeactivationFails() {
            // Arrange
            Trainer trainer = new Trainer();
            trainer.setId(TRAINER_ID);

            IllegalStateException expectedException =
                    new IllegalStateException("Trainer is already inactive");

            doThrow(expectedException)
                    .when(trainerService)
                    .deactivateTrainer(credentials, trainer);

            // Act
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> gymCrmFacade.deactivateTrainer(credentials, trainer)
            );

            // Assert
            assertSame(expectedException, exception);

            verify(trainerService).deactivateTrainer(credentials, trainer);
            verifyNoInteractions(traineeService, trainingService);
        }

        @Test
        void shouldPropagateExceptionWhenTrainerUsernameNotFound() {
            // Arrange
            EntityNotFoundException expectedException =
                    new EntityNotFoundException("Trainer not found");

            when(trainerService.findByUsername(credentials, JAMES_BOND))
                    .thenThrow(expectedException);

            // Act
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> gymCrmFacade.findTrainerByUsername(
                            credentials, JAMES_BOND
                    )
            );

            // Assert
            assertSame(expectedException, exception);

            verify(trainerService).findByUsername(credentials, JAMES_BOND);
            verifyNoInteractions(traineeService, trainingService);
        }

    }

}