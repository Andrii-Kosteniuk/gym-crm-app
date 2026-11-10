package com.training.gym.dao;

import com.training.gym.model.Trainer;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.training.gym.dto.trainer.TrainerTrainingCriteria;
import com.training.gym.model.Trainee;
import com.training.gym.model.Training;
import com.training.gym.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JpaTrainerRepositoryTest extends RepositoryIntegrationSupport {
    @Autowired
    JpaTrainerRepository repository;

    @Test
    void persistsAndFindsTrainer() {
        var yoga = type("Yoga");
        var t = Trainer.builder().user(user("James", "Bond", "trainer.save")).specialization(yoga).build();
        assertSame(t, repository.save(t));
        reload();
        assertEquals(t.getId(), repository.findById(t.getId()).orElseThrow().getId());
        assertEquals(t.getId(), repository.findByUserName("trainer.save").orElseThrow().getId());
        assertTrue(repository.existsById(t.getId()));
    }

    @Test
    void updatesTrainerAndRejectsMissing() {
        var yoga = type("Yoga");
        var boxing = type("Boxing");
        var t = trainer("trainer.update", "James", "Bond", yoga);
        reload();
        var detached = repository.findById(t.getId()).orElseThrow();
        em.detach(detached);
        detached.setSpecialization(boxing);
        repository.update(detached);
        reload();
        assertEquals(boxing.getId(), repository.findById(t.getId()).orElseThrow().getSpecialization().getId());
        var missing = new Trainer();
        missing.setId(Long.MAX_VALUE);
        assertThrows(EntityNotFoundException.class, () -> repository.update(missing));
    }

    @Test
    void deletesByIdAndUsername() {
        var yoga = type("Yoga");
        var first = trainer("trainer.delete.id", "A", "One", yoga);
        var second = trainer("trainer.delete.name", "B", "Two", yoga);
        repository.deleteById(first.getId());
        repository.deleteByUsername("trainer.delete.name");
        reload();
        assertFalse(repository.existsById(first.getId()));
        assertFalse(repository.existsById(second.getId()));
    }

    @Test
    void listsTrainersAndHandlesMissing() {
        var yoga = type("Yoga");
        var t = trainer("trainer.list", "James", "Bond", yoga);
        reload();
        assertTrue(repository.findAll().stream().anyMatch(x -> x.getId().equals(t.getId())));
        assertTrue(repository.findById(Long.MAX_VALUE).isEmpty());
        assertTrue(repository.findByUserName("unknown").isEmpty());
        assertFalse(repository.existsById(Long.MAX_VALUE));
        assertThrows(EntityNotFoundException.class, () -> repository.deleteById(Long.MAX_VALUE));
    }

    @Test
    void changesActiveFlagInDatabase() {
        var yoga = type("Yoga");
        var t = trainer("trainer.active", "James", "Bond", yoga);
        reload();
        repository.deactivateTrainer(repository.findById(t.getId()).orElseThrow());
        reload();
        assertFalse(repository.findById(t.getId()).orElseThrow().getUser().getActive());
        repository.activateTrainer(repository.findById(t.getId()).orElseThrow());
        reload();
        assertTrue(repository.findById(t.getId()).orElseThrow().getUser().getActive());
    }

    @Test
    void findsOnlyUnassignedTrainersForGivenTrainee() {
        var yoga = type("Yoga");
        var trainee = trainee("trainee.assigned");
        var assigned = trainer("trainer.assigned", "A", "One", yoga);
        var available = trainer("trainer.available", "B", "Two", yoga);
        trainee.assignTrainer(assigned);
        reload();
        var result = repository.findUnassignedTrainers("trainee.assigned");
        assertTrue(result.stream().anyMatch(t -> t.getId().equals(available.getId())));
        assertFalse(result.stream().anyMatch(t -> t.getId().equals(assigned.getId())));
    }

    @Test
    void rejectsInvalidInputs() {
        assertThrows(NullPointerException.class, () -> repository.save(null));
        assertThrows(NullPointerException.class, () -> repository.update(null));
        assertThrows(NullPointerException.class, () -> repository.findById(null));
        assertThrows(NullPointerException.class, () -> repository.existsById(null));
        assertThrows(NullPointerException.class, () -> repository.delete(null));
        assertThrows(IllegalArgumentException.class, () -> repository.findByUserName(" "));
        assertThrows(IllegalArgumentException.class, () -> repository.save(new Trainer() {{
            setId(42L);
        }}));
        assertThrows(IllegalArgumentException.class, () -> repository.update(new Trainer()));
    }


    @Nested
    class FindTrainingsTests {

        private static final String USERNAME = "trainer.search";

        private Trainer trainer;
        private Trainee james;
        private Trainee anotherTrainee;
        private Training older;
        private Training newer;

        @BeforeEach
        void setUp() {
            TrainingType yoga = type("Yoga");

            trainer = trainer(USERNAME, "James", "Bond", yoga);
            james = trainee("trainee.james");
            anotherTrainee = trainee("trainee.another");

            older = training(
                    james, trainer, yoga, LocalDate.of(2026, 1, 10)
            );

            newer = training(
                    anotherTrainee, trainer, yoga, LocalDate.of(2026, 3, 20)
            );

            reload();
        }

        @Test
        void shouldFindAllTrainingsForTrainerOrderedByDateDescending() {
            // Arrange
            TrainerTrainingCriteria criteria =
                    new TrainerTrainingCriteria(null, null, null);

            // Act
            List<Training> result =
                    repository.findTrainings(USERNAME, criteria);

            // Assert
            assertEquals(
                    List.of(newer.getId(), older.getId()),
                    result.stream().map(Training::getId).toList()
            );
        }

        @Test
        void shouldFilterTrainingsByFromDateInclusively() {
            // Arrange
            TrainerTrainingCriteria criteria =
                    new TrainerTrainingCriteria(
                            LocalDate.of(2026, 3, 20),
                            null,
                            null
                    );

            // Act
            List<Training> result =
                    repository.findTrainings(USERNAME, criteria);

            // Assert
            assertEquals(1, result.size());
            assertEquals(newer.getId(), result.getFirst().getId());
        }

        @Test
        void shouldFilterTrainingsByToDateInclusively() {
            // Arrange
            TrainerTrainingCriteria criteria =
                    new TrainerTrainingCriteria(
                            null,
                            LocalDate.of(2026, 1, 10),
                            null
                    );

            // Act
            List<Training> result =
                    repository.findTrainings(USERNAME, criteria);

            // Assert
            assertEquals(1, result.size());
            assertEquals(older.getId(), result.getFirst().getId());
        }

        @Test
        void shouldFilterTrainingsByDateRange() {
            // Arrange
            TrainerTrainingCriteria criteria =
                    new TrainerTrainingCriteria(
                            LocalDate.of(2026, 1, 1),
                            LocalDate.of(2026, 2, 1),
                            null
                    );

            // Act
            List<Training> result =
                    repository.findTrainings(USERNAME, criteria);

            // Assert
            assertEquals(1, result.size());
            assertEquals(older.getId(), result.getFirst().getId());
        }

        @Test
        void shouldFilterTrainingsByTraineeFullName() {
            // Arrange
            TrainerTrainingCriteria criteria = new TrainerTrainingCriteria(null, null, "James Bond");

            // Act
            List<Training> result =
                    repository.findTrainings(USERNAME, criteria);

            // Assert
            assertEquals(2, result.size());
        }

        @Test
        void shouldTrimTraineeNameBeforeFiltering() {
            // Arrange
            TrainerTrainingCriteria criteria =
                    new TrainerTrainingCriteria(
                            null,
                            null,
                            "  James Bond  "
                    );

            // Act
            List<Training> result =
                    repository.findTrainings(USERNAME, criteria);

            // Assert
            assertEquals(2, result.size());
        }

        @Test
        void shouldIgnoreBlankTraineeName() {
            // Arrange
            TrainerTrainingCriteria criteria =
                    new TrainerTrainingCriteria(
                            null,
                            null,
                            "   "
                    );

            // Act
            List<Training> result =
                    repository.findTrainings(USERNAME, criteria);

            // Assert
            assertEquals(2, result.size());
        }

        @Test
        void shouldReturnEmptyListWhenTraineeNameDoesNotMatch() {
            // Arrange
            TrainerTrainingCriteria criteria =
                    new TrainerTrainingCriteria(
                            null,
                            null,
                            "Unknown Person"
                    );

            // Act
            List<Training> result =
                    repository.findTrainings(USERNAME, criteria);

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        void shouldReturnOnlyTrainingsBelongingToRequestedTrainer() {
            // Arrange
            TrainingType boxing = type("Boxing");

            Trainer otherTrainer = trainer(
                    "trainer.other", "Alex", "Brown", boxing
            );

            training(
                    james,
                    otherTrainer,
                    boxing,
                    LocalDate.of(2026, 4, 10)
            );

            reload();

            TrainerTrainingCriteria criteria =
                    new TrainerTrainingCriteria(null, null, null);

            // Act
            List<Training> result =
                    repository.findTrainings(USERNAME, criteria);

            // Assert
            assertEquals(2, result.size());

            assertTrue(result.stream().allMatch(
                    training -> training.getTrainer()
                            .getId().equals(trainer.getId())
            ));
        }

        @Test
        void shouldRejectNullCriteria() {
            // Act & Assert
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> repository.findTrainings(USERNAME, null)
            );

            assertEquals(
                    "Training criteria must not be null",
                    exception.getMessage()
            );
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", " ", "   "})
        void shouldRejectInvalidUsername(String username) {
            // Arrange
            TrainerTrainingCriteria criteria =
                    new TrainerTrainingCriteria(null, null, null);

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> repository.findTrainings(username, criteria)
            );
        }

        @Test
        void shouldRejectFromDateAfterToDate() {
            // Arrange
            TrainerTrainingCriteria criteria =
                    new TrainerTrainingCriteria(
                            LocalDate.of(2026, 5, 1),
                            LocalDate.of(2026, 4, 1),
                            null
                    );

            // Act
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> repository.findTrainings(USERNAME, criteria)
            );

            // Assert
            assertEquals(
                    "From date must not be after to date",
                    exception.getMessage()
            );
        }

        @Test
        void shouldAcceptEqualFromAndToDates() {
            // Arrange
            LocalDate date = LocalDate.of(2026, 1, 10);

            TrainerTrainingCriteria criteria =
                    new TrainerTrainingCriteria(date, date, null);

            // Act
            List<Training> result =
                    repository.findTrainings(USERNAME, criteria);

            // Assert
            assertEquals(1, result.size());
            assertEquals(older.getId(), result.getFirst().getId());
        }

        @Test
        void shouldReturnEmptyListForNonexistentTrainer() {
            // Arrange
            TrainerTrainingCriteria criteria =
                    new TrainerTrainingCriteria(null, null, null);

            // Act
            List<Training> result =
                    repository.findTrainings("trainer.unknown", criteria);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

}
