package com.training.gym.common.credentials.config;

import com.training.gym.common.credentials.generator.IdGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotSame;

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

        // Because of that fact that storages are initialized the Ids sequence is changed
        assertNotSame(1L, traineeIdGenerator.generate());
        assertNotSame(2L, traineeIdGenerator.generate());

        assertNotSame(1L, trainerIdGenerator.generate());
        assertNotSame(2L, trainerIdGenerator.generate());

        assertNotSame(1L, trainingIdGenerator.generate());
        assertNotSame(2L, trainingIdGenerator.generate());
    }
}
