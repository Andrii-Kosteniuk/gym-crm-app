
package com.training.gym.validation;

import com.training.gym.dto.training.CreateTrainingDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TrainingDtoValidatorTest {

    private TrainingDtoValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TrainingDtoValidator();
    }

    private CreateTrainingDto validDto() {
        return new CreateTrainingDto(
                1L,
                2L,
                "Strength Training",
                1L,
                LocalDate.of(2026, 11, 10),
                Duration.ofMinutes(60)
        );
    }

    @Nested
    class ValidTrainingTests {

        @Test
        void shouldAcceptValidTraining() {
            assertDoesNotThrow(
                    () -> validator.validateCreate(validDto())
            );
        }

        @Test
        void shouldAcceptHistoricalTrainingDate() {
            CreateTrainingDto dto = new CreateTrainingDto(
                    1L,
                    2L,
                    "Strength Training",
                    1L,
                    LocalDate.of(2020, 1, 1),
                    Duration.ofMinutes(60)
            );

            assertDoesNotThrow(
                    () -> validator.validateCreate(dto)
            );
        }

        @Test
        void shouldAcceptPositiveDurationOfOneSecond() {
            CreateTrainingDto dto = new CreateTrainingDto(
                    1L,
                    2L,
                    "Strength Training",
                    1L,
                    LocalDate.of(2026, 11, 10),
                    Duration.ofSeconds(1)
            );

            assertDoesNotThrow(
                    () -> validator.validateCreate(dto)
            );
        }
    }

    @Nested
    class InvalidTrainingTests {

        @Test
        void shouldRejectNullDto() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateCreate(null)
            );

            assertEquals(
                    "CreateTrainingDto must not be null",
                    exception.getMessage()
            );
        }

        @Test
        void shouldRejectNullTraineeId() {
            CreateTrainingDto dto = new CreateTrainingDto(
                    null, 2L, "Strength Training",
                    1L, LocalDate.of(2026, 11, 10),
                    Duration.ofMinutes(60)
            );

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateCreate(dto)
            );

            assertEquals(
                    "Trainee ID must not be null",
                    exception.getMessage()
            );
        }

        @Test
        void shouldRejectNullTrainerId() {
            CreateTrainingDto dto = new CreateTrainingDto(
                    1L, null, "Strength Training",
                    1L, LocalDate.of(2026, 11, 10),
                    Duration.ofMinutes(60)
            );

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateCreate(dto)
            );

            assertEquals(
                    "Trainer ID must not be null",
                    exception.getMessage()
            );
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -100L})
        void shouldRejectNonPositiveTraineeId(long traineeId) {
            CreateTrainingDto dto = new CreateTrainingDto(
                    traineeId, 2L, "Strength Training",
                    1L, LocalDate.of(2026, 11, 10),
                    Duration.ofMinutes(60)
            );

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateCreate(dto)
            );

            assertEquals(
                    "Trainee ID must be positive",
                    exception.getMessage()
            );
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -100L})
        void shouldRejectNonPositiveTrainerId(long trainerId) {
            CreateTrainingDto dto = new CreateTrainingDto(
                    1L, trainerId, "Strength Training",
                    1L, LocalDate.of(2026, 11, 10),
                    Duration.ofMinutes(60)
            );

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateCreate(dto)
            );

            assertEquals(
                    "Trainer ID must be positive",
                    exception.getMessage()
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   ", "\t", "\n"})
        void shouldRejectBlankTrainingName(String name) {
            CreateTrainingDto dto = new CreateTrainingDto(
                    1L, 2L, name,
                    1L, LocalDate.of(2026, 11, 10),
                    Duration.ofMinutes(60)
            );

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateCreate(dto)
            );

            assertEquals(
                    "Training name must not be blank",
                    exception.getMessage()
            );
        }

        @Test
        void shouldRejectNullTrainingType() {
            CreateTrainingDto dto = new CreateTrainingDto(
                    1L, 2L, "Strength Training",
                    null, LocalDate.of(2026, 11, 10),
                    Duration.ofMinutes(60)
            );

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateCreate(dto)
            );

            assertEquals(
                    "Training type must not be null",
                    exception.getMessage()
            );
        }

        @Test
        void shouldRejectNullTrainingDate() {
            CreateTrainingDto dto = new CreateTrainingDto(
                    1L, 2L, "Strength Training",
                    1L, null, Duration.ofMinutes(60)
            );

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateCreate(dto)
            );

            assertEquals(
                    "Training date must not be null",
                    exception.getMessage()
            );
        }

        @Test
        void shouldRejectNullDuration() {
            CreateTrainingDto dto = new CreateTrainingDto(
                    1L, 2L, "Strength Training",
                    1L, LocalDate.of(2026, 11, 10), null
            );

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateCreate(dto)
            );

            assertEquals(
                    "Training duration must not be null",
                    exception.getMessage()
            );
        }

        @Test
        void shouldRejectZeroDuration() {
            CreateTrainingDto dto = new CreateTrainingDto(
                    1L, 2L, "Strength Training",
                    1L, LocalDate.of(2026, 11, 10),
                    Duration.ZERO
            );

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateCreate(dto)
            );

            assertEquals(
                    "Training duration must be positive",
                    exception.getMessage()
            );
        }

        @Test
        void shouldRejectNegativeDuration() {
            CreateTrainingDto dto = new CreateTrainingDto(
                    1L, 2L, "Strength Training",
                    1L, LocalDate.of(2026, 11, 10),
                    Duration.ofMinutes(-30)
            );

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateCreate(dto)
            );

            assertEquals(
                    "Training duration must be positive",
                    exception.getMessage()
            );
        }
    }
}
