package com.training.gym.training.repository;

import com.training.gym.training.domain.Training;
import com.training.gym.training.infrastructure.InMemoryTrainingRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTrainingRepositoryTest {

    private static final Long TRAINING_ID = 1L;
    private static final Long TRAINEE_ID = 10L;
    private static final Long TRAINER_ID = 20L;

    private static final String NULL_ID_MESSAGE = "Training ID must not be null";

    private InMemoryTrainingRepository repository;
    private Map<Long, Training> storage;
    private Training training;

    @BeforeEach
    void setUp() {
        storage = new ConcurrentHashMap<>();

        repository = new InMemoryTrainingRepository();
        repository.setStorage(storage);

        training = createTraining(
                TRAINING_ID,
                TRAINEE_ID,
                TRAINER_ID
        );
    }

    private Training createTraining(
            Long id,
            Long traineeId,
            Long trainerId
    ) {
        Training result = new Training();

        result.setId(id);
        result.setTraineeId(traineeId);
        result.setTrainerId(trainerId);

        return result;
    }

    @Nested
    class StorageTests {

        @Test
        void shouldUseProvidedStorage() {
            // Arrange
            Training existing = createTraining(2L, 30L, 40L);
            storage.put(existing.getId(), existing);

            // Act
            Optional<Training> result = repository.findById(2L);

            // Assert
            assertTrue(result.isPresent());
            assertSame(existing, result.get());
        }

        @Test
        void shouldRejectNullStorage() {
            // Act
            assertThrows(
                    NullPointerException.class,
                    () -> repository.setStorage(null)
            );
        }

        @Test
        void shouldUseNewStorageWhenStorageIsReplaced() {
            // Arrange
            repository.save(training);

            Map<Long, Training> newStorage = new ConcurrentHashMap<>();

            // Act
            repository.setStorage(newStorage);

            // Assert
            assertTrue(repository.findAll().isEmpty());
            assertFalse(repository.existsById(TRAINING_ID));

            Training newTraining = createTraining(2L, 30L, 40L);
            repository.save(newTraining);

            assertSame(newTraining, newStorage.get(2L));
            assertEquals(1, newStorage.size());

            // Original storage is unchanged.
            assertSame(training, storage.get(TRAINING_ID));
        }
    }

    @Nested
    class SaveTests {

        @Test
        void shouldSaveTrainingSuccessfully() {
            // Act
            Training result = repository.save(training);

            // Assert
            assertNotNull(result);
            assertSame(training, result);

            assertEquals(1, storage.size());
            assertSame(training, storage.get(TRAINING_ID));
        }

        @Test
        void shouldReplaceExistingTrainingWithSameId() {
            // Arrange
            repository.save(training);

            Training updated = createTraining(
                    TRAINING_ID,
                    30L,
                    40L
            );

            // Act
            Training result = repository.save(updated);

            // Assert
            assertSame(updated, result);
            assertEquals(1, storage.size());
            assertSame(updated, storage.get(TRAINING_ID));

            assertEquals(
                    30L,
                    repository.findById(TRAINING_ID)
                            .orElseThrow()
                            .getTraineeId()
            );
        }

        @Test
        void shouldSaveMultipleTrainingsWithDifferentIds() {
            // Arrange
            Training second = createTraining(2L, 30L, 40L);

            // Act
            repository.save(training);
            repository.save(second);

            // Assert
            assertEquals(2, storage.size());
            assertSame(training, storage.get(TRAINING_ID));
            assertSame(second, storage.get(2L));
        }

        @Test
        void shouldRejectNullTraining() {
            // Act
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> repository.save(null)
            );

            // Assert
            assertEquals(
                    "Training must not be null",
                    exception.getMessage()
            );

            assertTrue(storage.isEmpty());
        }

        @Test
        void shouldRejectTrainingWithNullId() {
            // Arrange
            training.setId(null);

            // Act
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> repository.save(training)
            );

            // Assert
            assertEquals(
                    NULL_ID_MESSAGE,
                    exception.getMessage()
            );

            assertTrue(storage.isEmpty());
        }
    }

    @Nested
    class FindByIdTests {

        @Test
        void shouldFindTrainingById() {
            // Arrange
            repository.save(training);

            // Act
            Optional<Training> result =
                    repository.findById(TRAINING_ID);

            // Assert
            assertTrue(result.isPresent());
            assertSame(training, result.get());
        }

        @Test
        void shouldReturnEmptyWhenTrainingDoesNotExist() {
            // Act
            Optional<Training> result =
                    repository.findById(999L);

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        void shouldRejectNullId() {
            // Act
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> repository.findById(null)
            );

            // Assert
            assertEquals(
                    NULL_ID_MESSAGE,
                    exception.getMessage()
            );
        }
    }

    @Nested
    class FindAllTests {

        @Test
        void shouldReturnAllTrainings() {
            // Arrange
            Training second = createTraining(2L, 30L, 40L);

            repository.save(training);
            repository.save(second);

            // Act
            List<Training> result = repository.findAll();

            // Assert
            assertEquals(2, result.size());
            assertTrue(result.contains(training));
            assertTrue(result.contains(second));
        }

        @Test
        void shouldReturnEmptyListWhenStorageIsEmpty() {
            // Act
            List<Training> result = repository.findAll();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        void shouldReturnUnmodifiableList() {
            // Arrange
            repository.save(training);

            // Act
            List<Training> result = repository.findAll();

            // Assert
            assertThrows(
                    UnsupportedOperationException.class,
                    () -> result.add(new Training())
            );
        }

        @Test
        void shouldReturnSnapshotOfStoredTrainings() {
            // Arrange
            repository.save(training);

            // Act
            List<Training> result = repository.findAll();

            Training second = createTraining(2L, 30L, 40L);
            repository.save(second);

            // Assert
            assertEquals(1, result.size());
            assertEquals(2, repository.findAll().size());
        }
    }

    @Nested
    class ExistsByIdTests {

        @Test
        void shouldReturnTrueWhenTrainingExists() {
            // Arrange
            repository.save(training);

            // Act
            boolean exists = repository.existsById(TRAINING_ID);

            // Assert
            assertTrue(exists);
        }

        @Test
        void shouldReturnFalseWhenTrainingDoesNotExist() {
            // Act
            boolean exists = repository.existsById(999L);

            // Assert
            assertFalse(exists);
        }

        @Test
        void shouldRejectNullId() {
            // Act
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> repository.existsById(null)
            );

            // Assert
            assertEquals(
                    NULL_ID_MESSAGE,
                    exception.getMessage()
            );
        }
    }

    @Nested
    class DeleteByIdTests {

        @Test
        void shouldDeleteExistingTraining() {
            // Arrange
            repository.save(training);

            // Act
            repository.deleteById(TRAINING_ID);

            // Assert
            assertFalse(repository.existsById(TRAINING_ID));
            assertTrue(repository.findById(TRAINING_ID).isEmpty());
            assertTrue(storage.isEmpty());
        }

        @Test
        void shouldNotThrowWhenDeletingNonexistentTraining() {
            // Act
            assertDoesNotThrow(
                    () -> repository.deleteById(999L)
            );

            // Assert
            assertTrue(storage.isEmpty());
        }

        @Test
        void shouldDeleteOnlyTrainingWithSpecifiedId() {
            // Arrange
            Training second = createTraining(2L, 30L, 40L);

            repository.save(training);
            repository.save(second);

            // Act
            repository.deleteById(TRAINING_ID);

            // Assert
            assertFalse(repository.existsById(TRAINING_ID));
            assertTrue(repository.existsById(2L));

            assertEquals(1, storage.size());
            assertSame(second, storage.get(2L));
        }

        @Test
        void shouldRejectNullId() {
            // Act
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> repository.deleteById(null)
            );

            // Assert
            assertEquals(
                    NULL_ID_MESSAGE,
                    exception.getMessage()
            );
        }
    }

    @Nested
    class ExistsByCorrespondingIdTests {

        @Test
        void shouldReturnTrueWhenTrainingReferencesTrainee() {
            // Arrange
            repository.save(training);

            // Act
            boolean exists =
                    repository.existsByTraineeOrTrainerId(
                            TRAINEE_ID
                    );

            // Assert
            assertTrue(exists);
        }

        @Test
        void shouldReturnTrueWhenTrainingReferencesTrainer() {
            // Arrange
            repository.save(training);

            // Act
            boolean exists =
                    repository.existsByTraineeOrTrainerId(
                            TRAINER_ID
                    );

            // Assert
            assertTrue(exists);
        }

        @Test
        void shouldReturnFalseWhenIdMatchesNeitherTraineeNorTrainer() {
            // Arrange
            repository.save(training);

            // Act
            boolean exists =
                    repository.existsByTraineeOrTrainerId(
                            999L
                    );

            // Assert
            assertFalse(exists);
        }

        @Test
        void shouldReturnFalseWhenStorageIsEmpty() {
            // Act
            boolean exists =
                    repository.existsByTraineeOrTrainerId(
                            TRAINEE_ID
                    );

            // Assert
            assertFalse(exists);
        }

        @Test
        void shouldReturnTrueWhenTraineeIdMatchesAndTrainerIdIsNull() {
            // Arrange
            Training trainingWithNullTrainer = createTraining(
                    TRAINING_ID,
                    TRAINEE_ID,
                    null
            );

            repository.save(trainingWithNullTrainer);

            // Act
            boolean exists =
                    repository.existsByTraineeOrTrainerId(
                            TRAINEE_ID
                    );

            // Assert
            assertTrue(exists);
        }

        @Test
        void shouldReturnTrueWhenTrainerIdMatchesAndTraineeIdIsNull() {
            // Arrange
            Training trainingWithNullTrainee = createTraining(
                    TRAINING_ID,
                    null,
                    TRAINER_ID
            );

            repository.save(trainingWithNullTrainee);

            // Act
            boolean exists =
                    repository.existsByTraineeOrTrainerId(
                            TRAINER_ID
                    );

            // Assert
            assertTrue(exists);
        }

        @Test
        void shouldReturnFalseWhenBothReferencesAreNull() {
            // Arrange
            Training trainingWithoutReferences = createTraining(
                    TRAINING_ID,
                    null,
                    null
            );

            repository.save(trainingWithoutReferences);

            // Act
            boolean exists =
                    repository.existsByTraineeOrTrainerId(
                            TRAINEE_ID
                    );

            // Assert
            assertFalse(exists);
        }

        @Test
        void shouldReturnFalseWhenTraineeIdIsNullAndTrainerIdDoesNotMatch() {
            // Arrange
            Training trainingWithNullTrainee = createTraining(
                    TRAINING_ID,
                    null,
                    TRAINER_ID
            );

            repository.save(trainingWithNullTrainee);

            // Act
            boolean exists =
                    repository.existsByTraineeOrTrainerId(
                            999L
                    );

            // Assert
            assertFalse(exists);
        }

        @Test
        void shouldReturnFalseWhenTrainerIdIsNullAndTraineeIdDoesNotMatch() {
            // Arrange
            Training trainingWithNullTrainer = createTraining(
                    TRAINING_ID,
                    TRAINEE_ID,
                    null
            );

            repository.save(trainingWithNullTrainer);

            // Act
            boolean exists =
                    repository.existsByTraineeOrTrainerId(
                            999L
                    );

            // Assert
            assertFalse(exists);
        }

        @Test
        void shouldFindMatchingReferenceAmongMultipleTrainings() {
            // Arrange
            Training first = createTraining(1L, 10L, 20L);
            Training second = createTraining(2L, 30L, 40L);
            Training third = createTraining(3L, 50L, 60L);

            repository.save(first);
            repository.save(second);
            repository.save(third);

            // Act
            boolean exists =
                    repository.existsByTraineeOrTrainerId(
                            40L
                    );

            // Assert
            assertTrue(exists);
        }

        @Test
        void shouldReturnFalseWhenNoTrainingContainsRequestedReference() {
            // Arrange
            repository.save(createTraining(1L, 10L, 20L));
            repository.save(createTraining(2L, 30L, 40L));
            repository.save(createTraining(3L, 50L, 60L));

            // Act
            boolean exists =
                    repository.existsByTraineeOrTrainerId(
                            999L
                    );

            // Assert
            assertFalse(exists);
        }

        @Test
        void shouldRejectNullCorrespondingId() {
            // Act
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> repository.existsByTraineeOrTrainerId(
                            null
                    )
            );

            // Assert
            assertEquals(
                    NULL_ID_MESSAGE,
                    exception.getMessage()
            );
        }
    }
}