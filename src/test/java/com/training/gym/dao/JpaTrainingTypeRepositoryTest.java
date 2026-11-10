package com.training.gym.dao;

import com.training.gym.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class JpaTrainingTypeRepositoryTest extends RepositoryIntegrationSupport {
    @Autowired
    JpaTrainingTypeRepository repository;

    @Test
    void findsTypeById() {
        var yoga = type("Yoga");
        reload();
        assertEquals("Yoga", repository.findById(yoga.getId()).orElseThrow().getName());
    }

    @Test
    void returnsEmptyForUnknownId() {
        assertTrue(repository.findById(Long.MAX_VALUE).isEmpty());
    }

    @Test
    void listsTypesAlphabetically() {
        type("Zumba");
        type("Aerobics");
        reload();
        assertEquals(java.util.List.of("Aerobics", "Zumba"),
                repository.findAll().stream().map(TrainingType::getName).toList());
    }

    @Test
    void rejectsNullId() {
        assertThrows(NullPointerException.class, () -> repository.findById(null));
    }
}
