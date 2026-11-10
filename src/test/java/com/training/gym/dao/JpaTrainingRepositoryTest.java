package com.training.gym.dao;

import com.training.gym.dto.trainee.TraineeTrainingCriteria;
import com.training.gym.dto.trainer.TrainerTrainingCriteria;
import com.training.gym.model.Training;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class JpaTrainingRepositoryTest extends RepositoryIntegrationSupport {
    @Autowired
    JpaTrainingRepository repository;

    @Test
    void savesAndLoadsDurationInMinutes() {
        var type = type("Yoga");
        var trainee = trainee("trainee.duration");
        var trainer = trainer("trainer.duration", "James", "Bond", type);
        var t = Training.builder().trainee(trainee).trainer(trainer).trainingType(type)
                .trainingName("Session").trainingDate(LocalDate.of(2026, 11, 15))
                .trainingDuration(Duration.ofMinutes(45)).build();
        assertSame(t, repository.save(t));
        reload();
        assertEquals(Duration.ofMinutes(45), repository.findById(t.getId()).orElseThrow().getTrainingDuration());
        Number stored = (Number) em.createNativeQuery("SELECT duration_minutes FROM trainings WHERE id = :id")
                .setParameter("id", t.getId()).getSingleResult();
        assertEquals(45L, stored.longValue());
        assertTrue(repository.existsById(t.getId()));
    }

    @Test
    void ordersTrainingsByDateDescending() {
        var type = type("Yoga");
        var trainee = trainee("trainee.order");
        var trainer = trainer("trainer.order", "James", "Bond", type);
        var old = training(trainee, trainer, type, LocalDate.of(2026, 11, 10));
        var recent = training(trainee, trainer, type, LocalDate.of(2026, 11, 15));
        reload();
        var result = repository.findAll();
        assertEquals(recent.getId(), result.get(0).getId());
        assertEquals(old.getId(), result.get(1).getId());
    }

    @Test
    void deletesExistingAndRejectsMissingTraining() {
        var type = type("Yoga");
        var trainee = trainee("trainee.delete");
        var trainer = trainer("trainer.delete", "James", "Bond", type);
        var t = training(trainee, trainer, type, LocalDate.of(2026, 11, 15));
        reload();
        repository.deleteById(t.getId());
        reload();
        assertFalse(repository.existsById(t.getId()));
        assertTrue(repository.findById(t.getId()).isEmpty());
        assertThrows(EntityNotFoundException.class, () -> repository.deleteById(t.getId()));
    }

    @Test
    void filtersByTraineeWithAllOptionalCriteria() {
        var type = type("Yoga");
        var otherType = type("Boxing");
        var trainee = trainee("trainee.search");
        var trainer = trainer("trainer.search", "James", "Bond", type);
        var other = trainer("trainer.other", "Alice", "Jones", otherType);
        var date = LocalDate.of(2026, 11, 15);
        var expected = training(trainee, trainer, type, date);
        training(trainee, other, otherType, date.plusDays(1));
        reload();
        var criteria = new TraineeTrainingCriteria(date, date, "James Bond", "Yoga");
        var result = repository.findByTraineeUsername("trainee.search", criteria);
        assertEquals(1, result.size());
        assertEquals(expected.getId(), result.getFirst().getId());
        assertEquals(2, repository.findByTraineeUsername("trainee.search",
                new TraineeTrainingCriteria(null, null, null, null)).size());
    }

    @Test
    void filtersByTrainerWithAllOptionalCriteria() {
        var type = type("Yoga");
        var trainer = trainer("trainer.search", "James", "Bond", type);
        var trainee = trainee("trainee.search");
        var date = LocalDate.of(2026, 11, 15);
        var expected = training(trainee, trainer, type, date);
        reload();
        var criteria = new TrainerTrainingCriteria(date, date, "James Bond");
        var result = repository.findByTrainerUsername("trainer.search", criteria);
        assertEquals(1, result.size());
        assertEquals(expected.getId(), result.getFirst().getId());
        assertEquals(1, repository.findByTrainerUsername("trainer.search",
                new TrainerTrainingCriteria(null, null, null)).size());
    }

    @Test
    void rejectsInvalidCriteriaAndIdentifiers() {
        assertThrows(NullPointerException.class, () -> repository.save(null));
        assertThrows(NullPointerException.class, () -> repository.findById(null));
        assertThrows(NullPointerException.class, () -> repository.existsById(null));
        assertThrows(NullPointerException.class, () -> repository.deleteById(null));
        var t = new Training();
        t.setId(10L);
        assertThrows(IllegalArgumentException.class, () -> repository.save(t));
        assertThrows(IllegalArgumentException.class, () -> repository.findByTraineeUsername(" ",
                new TraineeTrainingCriteria(null, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> repository.findByTrainerUsername(" ",
                new TrainerTrainingCriteria(null, null, null)));
        assertThrows(NullPointerException.class, () -> repository.findByTraineeUsername("valid", null));
        assertThrows(NullPointerException.class, () -> repository.findByTrainerUsername("valid", null));
        var from = LocalDate.of(2026, 11, 20);
        var to = LocalDate.of(2026, 11, 10);
        assertThrows(IllegalArgumentException.class, () -> repository.findByTraineeUsername("valid",
                new TraineeTrainingCriteria(from, to, null, null)));
        assertThrows(IllegalArgumentException.class, () -> repository.findByTrainerUsername("valid",
                new TrainerTrainingCriteria(from, to, null)));
    }
}
