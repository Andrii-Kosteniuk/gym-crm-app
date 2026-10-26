package com.training.gym.trainee.repository;

import com.training.gym.trainee.domain.Trainee;
import com.training.gym.trainee.infrastructure.InMemoryTraineeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InMemoryTraineeRepositoryTest {

    private InMemoryTraineeRepository repository;
    private Map<Long, Trainee> storage;

    private Trainee trainee;

    @BeforeEach
    void setUp() {
        storage = new HashMap<>();

        repository = new InMemoryTraineeRepository();
        repository.setStorage(storage);

        trainee = new Trainee();
        trainee.setId(1L);
        trainee.setFirstName("Andrii");
        trainee.setLastName("Kosteniuk");
        trainee.setUserName("andrii.kosteniuk");
        trainee.setActive(true);
    }

    @Test
    void shouldSaveNewTrainee() {
        // Act
        Trainee saved = repository.save(trainee);

        // Assert
        assertNotNull(saved);
        assertSame(trainee, saved);

        assertEquals(1, storage.size());
        assertSame(trainee, storage.get(1L));
    }

    @Test
    void shouldReplaceExistingTraineeWithSameId() {
        // Arrange
        repository.save(trainee);

        Trainee updated = new Trainee();
        updated.setId(1L);
        updated.setFirstName("Andrew");
        updated.setLastName("Kosteniuk");
        updated.setUserName("andrew.kosteniuk");

        // Act
        repository.save(updated);

        // Assert
        assertEquals(1, storage.size());
        assertSame(updated, storage.get(1L));

        assertEquals(
                "Andrew",
                repository.findById(1L)
                        .orElseThrow()
                        .getFirstName()
        );
    }


    @Test
    void shouldFindTraineeById() {
        // Arrange
        repository.save(trainee);

        // Act
        Optional<Trainee> result = repository.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertSame(trainee, result.get());
    }

    @Test
    void shouldReturnEmptyWhenIdDoesNotExist() {
        // Act
        Optional<Trainee> result = repository.findById(999L);

        // Assert
        assertTrue(result.isEmpty());
    }


    @Test
    void shouldReturnAllTrainees() {
        // Arrange
        Trainee secondTrainee = new Trainee();
        secondTrainee.setId(2L);
        secondTrainee.setUserName("james.bond");

        repository.save(trainee);
        repository.save(secondTrainee);

        // Act
        List<Trainee> result = repository.findAll();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(trainee));
        assertTrue(result.contains(secondTrainee));
    }

    @Test
    void shouldReturnEmptyListWhenStorageIsEmpty() {
        // Act
        List<Trainee> result = repository.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    void shouldReturnTrueWhenIdExists() {
        // Arrange
        repository.save(trainee);

        // Act
        boolean exists = repository.existsById(1L);

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


    @Test
    void shouldDeleteExistingTrainee() {
        // Arrange
        repository.save(trainee);

        // Act
        repository.deleteById(1L);

        // Assert
        assertFalse(repository.existsById(1L));
        assertTrue(repository.findById(1L).isEmpty());
        assertTrue(storage.isEmpty());
    }

    @Test
    void shouldNotThrowWhenDeletingNonexistentId() {
        // Act & Assert
        assertDoesNotThrow(() -> repository.deleteById(999L));
    }


    @Test
    void shouldReturnTrueWhenUsernameExists() {
        // Arrange
        repository.save(trainee);

        // Act
        boolean exists =
                repository.existsByUserName("andrii.kosteniuk");

        // Assert
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenUsernameDoesNotExist() {
        // Arrange
        repository.save(trainee);

        // Act
        boolean exists =
                repository.existsByUserName("unknown.user");

        // Assert
        assertFalse(exists);
    }


    @Test
    void shouldFindTraineeByUsername() {
        // Arrange
        repository.save(trainee);

        // Act
        Optional<Trainee> result =
                repository.findByUserName("andrii.kosteniuk");

        // Assert
        assertTrue(result.isPresent());
        assertSame(trainee, result.get());
    }

    @Test
    void shouldReturnEmptyWhenUsernameDoesNotExist() {
        // Arrange
        repository.save(trainee);

        // Act
        Optional<Trainee> result =
                repository.findByUserName("unknown.user");

        // Assert
        assertTrue(result.isEmpty());
    }
}