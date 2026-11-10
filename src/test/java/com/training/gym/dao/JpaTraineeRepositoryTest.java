package com.training.gym.dao;

import com.training.gym.dto.trainee.TraineeTrainingCriteria;
import com.training.gym.model.Trainee;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class JpaTraineeRepositoryTest extends RepositoryIntegrationSupport {
    @Autowired
    JpaTraineeRepository repository;

    @Test
    void persistsAndFindsTrainee() {
        var trainee = Trainee.builder().user(user("James", "Bond", "trainee.save")).build();
        assertSame(trainee, repository.save(trainee));
        reload();
        assertEquals(trainee.getId(), repository.findById(trainee.getId()).orElseThrow().getId());
        assertEquals(trainee.getId(), repository.findByUserName("trainee.save").orElseThrow().getId());
        assertTrue(repository.existsById(trainee.getId()));
    }

    @Test
    void updatesDetachedTrainee() {
        var trainee = trainee("trainee.update");
        reload();
        var detached = repository.findById(trainee.getId()).orElseThrow();
        em.detach(detached);
        detached.setDateOfBirth(LocalDate.of(1999, 1, 1));
        repository.update(detached);
        reload();
        assertEquals(LocalDate.of(1999, 1, 1), repository.findById(trainee.getId()).orElseThrow().getDateOfBirth());
    }

    @Test
    void deletesById() {
        var trainee = trainee("trainee.delete.id");
        repository.deleteById(trainee.getId());
        reload();
        assertTrue(repository.findById(trainee.getId()).isEmpty());
    }

    @Test
    void deletesByUsername() {
        var trainee = trainee("trainee.delete.name");
        repository.deleteByUsername("trainee.delete.name");
        reload();
        assertTrue(repository.findById(trainee.getId()).isEmpty());
    }

    @Test
    void deletesDetachedTrainee() {
        var trainee = trainee("trainee.detached");
        reload();
        var detached = repository.findById(trainee.getId()).orElseThrow();
        em.detach(detached);
        repository.delete(detached);
        reload();
        assertFalse(repository.existsById(trainee.getId()));
    }

    @Test
    void listsTraineesAndHandlesMissing() {
        var trainee = trainee("trainee.list");
        reload();
        assertTrue(repository.findAll().stream().anyMatch(x -> x.getId().equals(trainee.getId())));
        assertTrue(repository.findById(Long.MAX_VALUE).isEmpty());
        assertTrue(repository.findByUserName("unknown").isEmpty());
        assertFalse(repository.existsById(Long.MAX_VALUE));
        assertThrows(EntityNotFoundException.class, () -> repository.deleteById(Long.MAX_VALUE));
        assertThrows(EntityNotFoundException.class, () -> repository.deleteByUsername("unknown"));
    }

    @Test
    void changesActiveFlagInDatabase() {
        var trainee = trainee("trainee.active");
        reload();
        repository.deactivateTrainee(repository.findById(trainee.getId()).orElseThrow());
        reload();
        assertFalse(repository.findById(trainee.getId()).orElseThrow().getUser().getActive());
        repository.activateTrainee(repository.findById(trainee.getId()).orElseThrow());
        reload();
        assertTrue(repository.findById(trainee.getId()).orElseThrow().getUser().getActive());
    }

    @Test
    void filtersTrainingsByDateTrainerAndType() {
        var trainee = trainee("trainee.filter");
        var yoga = type("Yoga");
        var boxing = type("Boxing");
        var james = trainer("trainer.James", "James", "Bond", yoga);
        var other = trainer("trainer.other", "Alice", "Jones", boxing);
        var date = LocalDate.of(2026, 11, 15);
        var expected = training(trainee, james, yoga, date);
        training(trainee, other, boxing, date.plusDays(2));
        reload();
        var criteria = new TraineeTrainingCriteria(date, date, "James Bond", "Yoga");
        var result = repository.findTrainings("trainee.filter", criteria);
        assertEquals(1, result.size());
        assertEquals(expected.getId(), result.getFirst().getId());
    }

    @Test
    void rejectsInvalidInputs() {
        assertThrows(NullPointerException.class, () -> repository.save(null));
        assertThrows(NullPointerException.class, () -> repository.update(null));
        assertThrows(NullPointerException.class, () -> repository.findById(null));
        assertThrows(NullPointerException.class, () -> repository.existsById(null));
        assertThrows(NullPointerException.class, () -> repository.delete(null));
        assertThrows(NullPointerException.class, () -> repository.deleteById(null));
        assertThrows(IllegalArgumentException.class, () -> repository.findByUserName(" "));
        assertThrows(IllegalArgumentException.class, () -> repository.deleteByUsername(null));
        assertThrows(IllegalArgumentException.class, () -> repository.save(new Trainee() {{
            setId(42L);
        }}));
        assertThrows(IllegalArgumentException.class, () -> repository.update(new Trainee()));
    }
}
