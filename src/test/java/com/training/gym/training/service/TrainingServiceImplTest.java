package com.training.gym.training.service;

import com.training.gym.common.credentials.generator.IdGenerator;
import com.training.gym.common.exception.EntityNotFoundException;
import com.training.gym.training.domain.Training;
import com.training.gym.training.dto.CreateTrainingDto;
import com.training.gym.training.mapper.TrainingMapper;
import com.training.gym.training.repository.TrainingRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

    private static final Long TRAINING_ID = 1L;

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private TrainingMapper trainingMapper;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    private Training training;
    private CreateTrainingDto createDto;

    @BeforeEach
    void setUp() {
        training = new Training();
        createDto = mock(CreateTrainingDto.class);
    }

    @Nested
    class CreateTests {

        @Test
        void shouldCreateTrainingSuccessfully() {
            // Arrange
            when(trainingMapper.toTraining(createDto))
                    .thenReturn(training);

            when(idGenerator.generate())
                    .thenReturn(TRAINING_ID);

            when(trainingRepository.save(training))
                    .thenReturn(training);

            // Act
            Training result = trainingService.createTraining(createDto);

            // Assert
            assertNotNull(result);
            assertSame(training, result);
            assertEquals(TRAINING_ID, result.getId());

            verify(trainingMapper).toTraining(createDto);
            verify(idGenerator).generate();
            verify(trainingRepository).save(training);
        }

        @Test
        void shouldAssignGeneratedIdBeforeSavingTraining() {
            // Arrange
            when(trainingMapper.toTraining(createDto))
                    .thenReturn(training);

            when(idGenerator.generate())
                    .thenReturn(TRAINING_ID);

            when(trainingRepository.save(any(Training.class)))
                    .thenAnswer(invocation -> {
                        Training entity = invocation.getArgument(0);

                        assertEquals(TRAINING_ID, entity.getId());

                        return entity;
                    });

            // Act
            Training result = trainingService.createTraining(createDto);

            // Assert
            assertEquals(TRAINING_ID, result.getId());

            ArgumentCaptor<Training> captor =
                    ArgumentCaptor.forClass(Training.class);

            verify(trainingRepository).save(captor.capture());

            assertSame(training, captor.getValue());
            assertEquals(TRAINING_ID, captor.getValue().getId());
        }

        @Test
        void shouldRejectNullCreateTrainingDto() {
            // Act
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> trainingService.createTraining(null)
            );

            // Assert
            assertEquals(
                    "Create trainer DTO must not be null",
                    exception.getMessage()
            );

            verifyNoInteractions(
                    trainingMapper,
                    idGenerator,
                    trainingRepository
            );
        }

        @Test
        void shouldReturnEntityReturnedByRepository() {
            // Arrange
            Training persistedTraining = new Training();
            persistedTraining.setId(TRAINING_ID);

            when(trainingMapper.toTraining(createDto))
                    .thenReturn(training);

            when(idGenerator.generate())
                    .thenReturn(TRAINING_ID);

            when(trainingRepository.save(training))
                    .thenReturn(persistedTraining);

            // Act
            Training result = trainingService.createTraining(createDto);

            // Assert
            assertSame(persistedTraining, result);

            verify(trainingRepository).save(training);
        }

        @Test
        void shouldPropagateRepositoryExceptionDuringCreation() {
            // Arrange
            when(trainingMapper.toTraining(createDto))
                    .thenReturn(training);

            when(idGenerator.generate())
                    .thenReturn(TRAINING_ID);

            RuntimeException repositoryException =
                    new RuntimeException("Storage failure");

            when(trainingRepository.save(training))
                    .thenThrow(repositoryException);

            // Act
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> trainingService.createTraining(createDto)
            );

            // Assert
            assertSame(repositoryException, exception);

            verify(trainingRepository).save(training);
        }
    }

    @Nested
    class GetByIdTests {

        @Test
        void shouldReturnTrainingWhenIdExists() {
            // Arrange
            training.setId(TRAINING_ID);

            when(trainingRepository.findById(TRAINING_ID))
                    .thenReturn(Optional.of(training));

            // Act
            Training result =
                    trainingService.getTrainingById(TRAINING_ID);

            // Assert
            assertNotNull(result);
            assertSame(training, result);
            assertEquals(TRAINING_ID, result.getId());

            verify(trainingRepository).findById(TRAINING_ID);
        }

        @Test
        void shouldThrowExceptionWhenTrainingDoesNotExist() {
            // Arrange
            Long nonexistentId = 999L;

            when(trainingRepository.findById(nonexistentId))
                    .thenReturn(Optional.empty());

            // Act
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> trainingService.getTrainingById(nonexistentId)
            );

            // Assert
            assertEquals(
                    "Training with id 999 not found",
                    exception.getMessage()
            );

            verify(trainingRepository).findById(nonexistentId);
        }

        @Test
        void shouldReturnCorrectTrainingForRequestedId() {
            // Arrange
            Long requestedId = 5L;

            Training expectedTraining = new Training();
            expectedTraining.setId(requestedId);

            when(trainingRepository.findById(requestedId))
                    .thenReturn(Optional.of(expectedTraining));

            // Act
            Training result =
                    trainingService.getTrainingById(requestedId);

            // Assert
            assertSame(expectedTraining, result);
            assertEquals(requestedId, result.getId());

            verify(trainingRepository).findById(requestedId);
        }

        @Test
        void shouldNotCallOtherDependenciesWhenRetrievingTraining() {
            // Arrange
            training.setId(TRAINING_ID);

            when(trainingRepository.findById(TRAINING_ID))
                    .thenReturn(Optional.of(training));

            // Act
            Training result =
                    trainingService.getTrainingById(TRAINING_ID);

            // Assert
            assertNotNull(result);

            verify(trainingRepository).findById(TRAINING_ID);
            verifyNoInteractions(idGenerator, trainingMapper);
        }
    }
}