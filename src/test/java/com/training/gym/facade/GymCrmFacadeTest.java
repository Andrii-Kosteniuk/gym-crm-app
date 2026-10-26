package com.training.gym.facade;

import com.training.gym.common.exception.EntityNotFoundException;
import com.training.gym.common.exception.ResourceConflictException;
import com.training.gym.fasade.GymCrmFacade;
import com.training.gym.trainee.domain.Trainee;
import com.training.gym.trainee.dto.CreateTraineeDto;
import com.training.gym.trainee.dto.UpdateTraineeDto;
import com.training.gym.trainee.service.TraineeService;
import com.training.gym.trainer.domain.Trainer;
import com.training.gym.trainer.dto.CreateTrainerDto;
import com.training.gym.trainer.dto.UpdateTrainerDto;
import com.training.gym.trainer.service.TrainerService;
import com.training.gym.training.domain.Training;
import com.training.gym.training.dto.CreateTrainingDto;
import com.training.gym.training.service.TrainingService;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GymCrmFacadeTest {

    private static final Long TRAINEE_ID = 1L;
    private static final Long TRAINER_ID = 2L;
    private static final Long TRAINING_ID = 3L;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @InjectMocks
    private GymCrmFacade gymCrmFacade;

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

            when(traineeService.updateTrainee(dto))
                    .thenReturn(expectedTrainee);

            // Act
            Trainee result = gymCrmFacade.updateTrainee(dto);

            // Assert
            assertNotNull(result);
            assertSame(expectedTrainee, result);

            verify(traineeService).updateTrainee(dto);
            verifyNoInteractions(trainerService, trainingService);
        }

        @Test
        void shouldDelegateGetTraineeByIdToTraineeService() {
            // Arrange
            Trainee expectedTrainee = new Trainee();
            expectedTrainee.setId(TRAINEE_ID);

            when(traineeService.getTraineeById(TRAINEE_ID))
                    .thenReturn(expectedTrainee);

            // Act
            Trainee result = gymCrmFacade.getTraineeById(TRAINEE_ID);

            // Assert
            assertNotNull(result);
            assertSame(expectedTrainee, result);
            assertEquals(TRAINEE_ID, result.getId());

            verify(traineeService).getTraineeById(TRAINEE_ID);
            verifyNoInteractions(trainerService, trainingService);
        }

        @Test
        void shouldDelegateDeleteTraineeToTraineeService() {
            // Act
            gymCrmFacade.deleteTrainee(TRAINEE_ID);

            // Assert
            verify(traineeService).removeTrainee(TRAINEE_ID);

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

            when(traineeService.updateTrainee(dto))
                    .thenThrow(expectedException);

            // Act
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> gymCrmFacade.updateTrainee(dto)
            );

            // Assert
            assertSame(expectedException, exception);

            verify(traineeService).updateTrainee(dto);
        }

        @Test
        void shouldPropagateExceptionWhenTraineeDoesNotExist() {
            // Arrange
            EntityNotFoundException expectedException =
                    new EntityNotFoundException(
                            "Trainee with id 1 not found"
                    );

            when(traineeService.getTraineeById(TRAINEE_ID))
                    .thenThrow(expectedException);

            // Act
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> gymCrmFacade.getTraineeById(TRAINEE_ID)
            );

            // Assert
            assertSame(expectedException, exception);

            verify(traineeService).getTraineeById(TRAINEE_ID);
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
                    .removeTrainee(TRAINEE_ID);

            // Act
            ResourceConflictException exception = assertThrows(
                    ResourceConflictException.class,
                    () -> gymCrmFacade.deleteTrainee(TRAINEE_ID)
            );

            // Assert
            assertSame(expectedException, exception);

            verify(traineeService).removeTrainee(TRAINEE_ID);
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
            gymCrmFacade.getTraineeById(null);

            // Assert
            verify(traineeService).getTraineeById(null);
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

            when(trainerService.updateTrainer(dto))
                    .thenReturn(expectedTrainer);

            // Act
            Trainer result = gymCrmFacade.updateTrainer(dto);

            // Assert
            assertNotNull(result);
            assertSame(expectedTrainer, result);

            verify(trainerService).updateTrainer(dto);
            verifyNoInteractions(traineeService, trainingService);
        }

        @Test
        void shouldDelegateGetTrainerByIdToTrainerService() {
            // Arrange
            Trainer expectedTrainer = new Trainer();
            expectedTrainer.setId(TRAINER_ID);

            when(trainerService.getTrainerById(TRAINER_ID))
                    .thenReturn(expectedTrainer);

            // Act
            Trainer result = gymCrmFacade.getTrainerById(TRAINER_ID);

            // Assert
            assertNotNull(result);
            assertSame(expectedTrainer, result);
            assertEquals(TRAINER_ID, result.getId());

            verify(trainerService).getTrainerById(TRAINER_ID);
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

            when(trainerService.updateTrainer(dto))
                    .thenThrow(expectedException);

            // Act
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> gymCrmFacade.updateTrainer(dto)
            );

            // Assert
            assertSame(expectedException, exception);

            verify(trainerService).updateTrainer(dto);
        }

        @Test
        void shouldPropagateExceptionWhenTrainerDoesNotExist() {
            // Arrange
            EntityNotFoundException expectedException =
                    new EntityNotFoundException(
                            "Trainer with id 2 not found"
                    );

            when(trainerService.getTrainerById(TRAINER_ID))
                    .thenThrow(expectedException);

            // Act
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> gymCrmFacade.getTrainerById(TRAINER_ID)
            );

            // Assert
            assertSame(expectedException, exception);

            verify(trainerService).getTrainerById(TRAINER_ID);
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
            gymCrmFacade.getTrainerById(null);

            // Assert
            verify(trainerService).getTrainerById(null);
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

            when(trainingService.createTraining(dto))
                    .thenReturn(expectedTraining);

            // Act
            Training result = gymCrmFacade.createTraining(dto);

            // Assert
            assertNotNull(result);
            assertSame(expectedTraining, result);
            assertEquals(TRAINING_ID, result.getId());

            verify(trainingService).createTraining(dto);
            verifyNoInteractions(traineeService, trainerService);
        }

        @Test
        void shouldDelegateGetTrainingByIdToTrainingService() {
            // Arrange
            Training expectedTraining = new Training();
            expectedTraining.setId(TRAINING_ID);

            when(trainingService.getTrainingById(TRAINING_ID))
                    .thenReturn(expectedTraining);

            // Act
            Training result = gymCrmFacade.getTrainingById(TRAINING_ID);

            // Assert
            assertNotNull(result);
            assertSame(expectedTraining, result);
            assertEquals(TRAINING_ID, result.getId());

            verify(trainingService).getTrainingById(TRAINING_ID);
            verifyNoInteractions(traineeService, trainerService);
        }

        @Test
        void shouldPropagateExceptionWhenCreatingTrainingFails() {
            // Arrange
            CreateTrainingDto dto = mock(CreateTrainingDto.class);

            RuntimeException expectedException =
                    new RuntimeException("Training creation failed");

            when(trainingService.createTraining(dto))
                    .thenThrow(expectedException);

            // Act
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> gymCrmFacade.createTraining(dto)
            );

            // Assert
            assertSame(expectedException, exception);

            verify(trainingService).createTraining(dto);
        }

        @Test
        void shouldPropagateExceptionWhenTrainingDoesNotExist() {
            // Arrange
            EntityNotFoundException expectedException =
                    new EntityNotFoundException(
                            "Training with id 3 not found"
                    );

            when(trainingService.getTrainingById(TRAINING_ID))
                    .thenThrow(expectedException);

            // Act
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> gymCrmFacade.getTrainingById(TRAINING_ID)
            );

            // Assert
            assertSame(expectedException, exception);

            verify(trainingService).getTrainingById(TRAINING_ID);
        }

        @Test
        void shouldPassNullDtoToTrainingServiceWithoutModification() {
            // Act
            gymCrmFacade.createTraining(null);

            // Assert
            verify(trainingService).createTraining(null);
        }

        @Test
        void shouldPassNullIdToTrainingServiceWithoutModification() {
            // Act
            gymCrmFacade.getTrainingById(null);

            // Assert
            verify(trainingService).getTrainingById(null);
        }
    }
}