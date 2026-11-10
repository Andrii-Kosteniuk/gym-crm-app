
package com.training.gym.validation;

import com.training.gym.dto.trainer.CreateTrainerDto;
import com.training.gym.dto.trainer.UpdateTrainerDto;
import com.training.gym.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class TrainerDtoValidatorTest {

    private TrainerDtoValidator validator;
    private TrainingType specialization;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-11-07T12:00:00Z"),
                ZoneId.of("Europe/Warsaw")
        );

        validator = new TrainerDtoValidator(clock);
        specialization = new TrainingType(1L, "Strength");
    }

    private CreateTrainerDto validCreateDto() {
        return new CreateTrainerDto(
                "James",
                "Bond",
                1L
        );
    }

    private UpdateTrainerDto validUpdateDto() {
        return new UpdateTrainerDto(
                "James",
                "Bond",
                "james.bond",
                1L
        );
    }

    @Nested
    class CreateTests {

        @Test
        void shouldAcceptValidCreateDto() {
            assertDoesNotThrow(
                    () -> validator.validateCreate(validCreateDto())
            );
        }

        @Test
        void shouldRejectNullCreateDto() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateCreate(null)
            );

            assertEquals(
                    "CreateTrainerDto must not be null",
                    exception.getMessage()
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   ", "\t", "\n"})
        void shouldRejectInvalidFirstName(String firstName) {
            CreateTrainerDto dto = new CreateTrainerDto(
                    firstName,
                    "Bond",
                    1L
            );

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateCreate(dto)
            );

            assertEquals(
                    "First name must not be blank",
                    exception.getMessage()
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   ", "\t", "\n"})
        void shouldRejectInvalidLastName(String lastName) {
            CreateTrainerDto dto = new CreateTrainerDto(
                    "James",
                    lastName,
                    1L
            );

            assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateCreate(dto)
            );
        }


        @Test
        void shouldAcceptDateOfBirthEqualToToday() {
            CreateTrainerDto dto = new CreateTrainerDto(
                    "James",
                    "Bond",
                    1L
            );

            assertDoesNotThrow(
                    () -> validator.validateCreate(dto)
            );
        }
    }

    @Nested
    class UpdateTests {

        @Test
        void shouldAcceptValidUpdateDto() {
            assertDoesNotThrow(
                    () -> validator.validateUpdate(validUpdateDto())
            );
        }

        @Test
        void shouldRejectNullUpdateDto() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateUpdate(null)
            );

            assertEquals(
                    "UpdateTrainerDto must not be null",
                    exception.getMessage()
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   ", "\t", "\n"})
        void shouldRejectInvalidUsername(String username) {
            UpdateTrainerDto dto = new UpdateTrainerDto(
                    "James",
                    "Bond",
                    username,
                    1L
            );

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateUpdate(dto)
            );

            assertEquals(
                    "Username must not be blank",
                    exception.getMessage()
            );
        }

        @Test
        void shouldRejectBlankFirstName() {
            UpdateTrainerDto dto = new UpdateTrainerDto(
                    " ",
                    "Bond",
                    "james.bond",
                    1L
            );

            assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateUpdate(dto)
            );
        }

        @Test
        void shouldRejectBlankLastName() {
            UpdateTrainerDto dto = new UpdateTrainerDto(
                    "James",
                    " ",
                    "james.bond",
                    1L
            );

            assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateUpdate(dto)
            );
        }


    }
}
