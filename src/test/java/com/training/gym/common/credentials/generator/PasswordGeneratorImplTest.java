package com.training.gym.common.credentials.generator;

import com.training.gym.common.credentials.PasswordGeneratorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class PasswordGeneratorImplTest {

    private static final int EXPECTED_PASSWORD_LENGTH = 10;

    private static final Pattern ALLOWED_CHARACTERS =
            Pattern.compile("[A-Za-z0-9]+");

    private PasswordGenerator passwordGenerator;

    @BeforeEach
    void setUp() {
        passwordGenerator = new PasswordGeneratorImpl();
    }

    @Nested
    class PasswordGenerationTests {

        @Test
        void shouldGenerateNonNullPassword() {
            // Act
            String password = passwordGenerator.generate();

            // Assert
            assertNotNull(password);
        }

        @Test
        void shouldGenerateNonEmptyPassword() {
            // Act
            String password = passwordGenerator.generate();

            // Assert
            assertFalse(password.isEmpty());
            assertFalse(password.isBlank());
        }

        @Test
        void shouldGeneratePasswordWithCorrectLength() {
            // Act
            String password = passwordGenerator.generate();

            // Assert
            assertEquals(
                    EXPECTED_PASSWORD_LENGTH,
                    password.length()
            );
        }


        @Test
        void shouldGenerateValidPasswordsAcrossMultipleInvocations() {
            // Arrange
            int iterations = 100;

            // Act & Assert
            for (int i = 0; i < iterations; i++) {
                String password = passwordGenerator.generate();

                assertAll(
                        () -> assertNotNull(password),
                        () -> assertEquals(
                                EXPECTED_PASSWORD_LENGTH,
                                password.length()
                        ),
                        () -> assertTrue(
                                ALLOWED_CHARACTERS.matcher(password).matches()
                        )
                );
            }
        }
    }

    @Nested
    class RandomnessTests {

        @Test
        void shouldGenerateDifferentPasswordsAcrossMultipleInvocations() {
            // Arrange
            Set<String> generatedPasswords = new HashSet<>();
            int iterations = 100;

            // Act
            for (int i = 0; i < iterations; i++) {
                generatedPasswords.add(passwordGenerator.generate());
            }

            // Assert
            assertTrue(
                    generatedPasswords.size() > 1,
                    "Generator should not return the same password every time"
            );
        }

        @Test
        void shouldGeneratePasswordsWithDifferentCharacterCombinations() {
            // Arrange
            Set<String> generatedPasswords = new HashSet<>();
            int iterations = 100;

            // Act
            for (int i = 0; i < iterations; i++) {
                generatedPasswords.add(passwordGenerator.generate());
            }

            // Assert
            assertTrue(
                    generatedPasswords.size() >= 90,
                    "Expected at least 90 unique passwords from 100 generations"
            );
        }
    }
}