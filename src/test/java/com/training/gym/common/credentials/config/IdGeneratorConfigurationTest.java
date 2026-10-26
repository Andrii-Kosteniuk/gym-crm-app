package com.training.gym.common.credentials.config;

import com.training.gym.common.credentials.generator.IdGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class IdGeneratorConfigurationTest {

    @Autowired
    @Qualifier("traineeIdGenerator")
    private IdGenerator traineeIdGenerator;

    @Autowired
    @Qualifier("trainerIdGenerator")
    private IdGenerator trainerIdGenerator;

    @Autowired
    @Qualifier("trainingIdGenerator")
    private IdGenerator trainingIdGenerator;

    @Test
    void shouldGenerateIndependentIdsForEachEntityType() {
        // Act & Assert
        assertEquals(1L, traineeIdGenerator.generate());
        assertEquals(2L, traineeIdGenerator.generate());

        assertEquals(1L, trainerIdGenerator.generate());
        assertEquals(2L, trainerIdGenerator.generate());

        assertEquals(1L, trainingIdGenerator.generate());
        assertEquals(2L, trainingIdGenerator.generate());
    }
}
