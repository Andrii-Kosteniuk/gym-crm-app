package com.training.gym.trainer.repository;
import com.training.gym.trainer.domain.Trainer;
import com.training.gym.trainer.infrastructure.InMemoryTrainerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTrainerRepositoryTest {

    private static final Long TRAINER_ID = 1L;
    private static final String USERNAME = "andrii.kosteniuk";

    private InMemoryTrainerRepository repository;
    private Map<Long, Trainer> storage;
    private Trainer trainer;

    @BeforeEach
    void setUp() {
        storage = new HashMap<>();

        repository = new InMemoryTrainerRepository();
        repository.setStorage(storage);

        trainer = new Trainer();
        trainer.setId(TRAINER_ID);
        trainer.setFirsName("Andrii");
        trainer.setLastName("Kosteniuk");
        trainer.setUserName(USERNAME);
        trainer.setActive(true);
    }

    @Nested
    class SaveTests {

        @Test
        void shouldSaveNewTrainerSuccessfully() {
            // Act
            Trainer saved = repository.save(trainer);

            // Assert
            assertNotNull(saved);
            assertSame(trainer, saved);

            assertEquals(1, storage.size());
            assertSame(trainer, storage.get(TRAINER_ID));
        }

        @Test
        void shouldReplaceExistingTrainerWithSameId() {
            // Arrange
            repository.save(trainer);

            Trainer updated = new Trainer();
            updated.setId(TRAINER_ID);
            updated.setFirsName("Andrew");
            updated.setLastName("Kosteniuk");
            updated.setUserName("andrew.kosteniuk");

            // Act
            Trainer saved = repository.save(updated);

            // Assert
            assertSame(updated, saved);
            assertEquals(1, storage.size());
            assertSame(updated, storage.get(TRAINER_ID));

            assertEquals(
                    "Andrew",
                    repository.findById(TRAINER_ID)
                            .orElseThrow()
                            .getFirsName()
            );
        }
    }

    @Nested
    class FindByIdTests {

        @Test
        void shouldFindTrainerById() {
            // Arrange
            repository.save(trainer);

            // Act
            Optional<Trainer> result =
                    repository.findById(TRAINER_ID);

            // Assert
            assertTrue(result.isPresent());
            assertSame(trainer, result.get());
        }

        @Test
        void shouldReturnEmptyWhenIdDoesNotExist() {
            // Act
            Optional<Trainer> result =
                    repository.findById(999L);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class FindAllTests {

        @Test
        void shouldReturnAllTrainers() {
            // Arrange
            Trainer secondTrainer = new Trainer();
            secondTrainer.setId(2L);
            secondTrainer.setFirsName("Andrii");
            secondTrainer.setLastName("Kosteniuk");
            secondTrainer.setUserName("andrii.kosteniuk");

            repository.save(trainer);
            repository.save(secondTrainer);

            // Act
            List<Trainer> result = repository.findAll();

            // Assert
            assertEquals(2, result.size());
            assertTrue(result.contains(trainer));
            assertTrue(result.contains(secondTrainer));
        }

        @Test
        void shouldReturnEmptyListWhenStorageIsEmpty() {
            // Act
            List<Trainer> result = repository.findAll();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        void shouldReturnUnmodifiableList() {
            // Arrange
            repository.save(trainer);

            // Act
            List<Trainer> result = repository.findAll();

            // Assert
            assertThrows(
                    UnsupportedOperationException.class,
                    () -> result.add(new Trainer())
            );
        }
    }

    @Nested
    class ExistsByIdTests {

        @Test
        void shouldReturnTrueWhenIdExists() {
            // Arrange
            repository.save(trainer);

            // Act
            boolean exists = repository.existsById(TRAINER_ID);

            // Assert
            assertTrue(exists);
        }

        @Test
        void shouldReturnFalseWhenIdDoesNotExist() {
            // Act
            boolean exists = repository.existsById(999L);

            // Assert
            assertFalse(exists);
        }
    }

    @Nested
    class DeleteByIdTests {

        @Test
        void shouldDeleteExistingTrainer() {
            // Arrange
            repository.save(trainer);

            // Act
            repository.deleteById(TRAINER_ID);

            // Assert
            assertFalse(repository.existsById(TRAINER_ID));
            assertTrue(repository.findById(TRAINER_ID).isEmpty());
            assertTrue(storage.isEmpty());
        }

        @Test
        void shouldNotThrowWhenDeletingNonexistentId() {
            // Act & Assert
            assertDoesNotThrow(
                    () -> repository.deleteById(999L)
            );

            assertTrue(storage.isEmpty());
        }
    }

    @Nested
    class ExistsByUsernameTests {

        @Test
        void shouldReturnTrueWhenUsernameExists() {
            // Arrange
            repository.save(trainer);

            // Act
            boolean exists =
                    repository.existsByUserName(USERNAME);

            // Assert
            assertTrue(exists);
        }

        @Test
        void shouldReturnFalseWhenUsernameDoesNotExist() {
            // Arrange
            repository.save(trainer);

            // Act
            boolean exists =
                    repository.existsByUserName("unknown.user");

            // Assert
            assertFalse(exists);
        }
    }

    @Nested
    class FindByUsernameTests {

        @Test
        void shouldFindTrainerByUsername() {
            // Arrange
            repository.save(trainer);

            // Act
            Optional<Trainer> result =
                    repository.findByUserName(USERNAME);

            // Assert
            assertTrue(result.isPresent());
            assertSame(trainer, result.get());
        }

        @Test
        void shouldReturnEmptyWhenUsernameDoesNotExist() {
            // Arrange
            repository.save(trainer);

            // Act
            Optional<Trainer> result =
                    repository.findByUserName("unknown.user");

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        void shouldReturnCorrectTrainerWhenMultipleExist() {
            // Arrange
            Trainer secondTrainer = new Trainer();
            secondTrainer.setId(2L);
            secondTrainer.setFirsName("James");
            secondTrainer.setLastName("Bond");
            secondTrainer.setUserName("james.bond");

            repository.save(trainer);
            repository.save(secondTrainer);

            // Act
            Trainer result = repository
                    .findByUserName("james.bond")
                    .orElseThrow();

            // Assert
            assertAll(
                    () -> assertEquals(2L, result.getId()),
                    () -> assertEquals(
                            "james.bond",
                            result.getUserName()
                    )
            );
        }
    }
}