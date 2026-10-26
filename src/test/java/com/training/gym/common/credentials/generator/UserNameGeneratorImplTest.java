package com.training.gym.common.credentials.generator;

import com.training.gym.common.credentials.UserNameGeneratorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class UserNameGeneratorImplTest {

    private UsernameGenerator usernameGenerator;

    @BeforeEach
    void setUp() {
        usernameGenerator = new UserNameGeneratorImpl();
    }

    @Nested
    class GenerateUsernameTests {

        @Test
        void shouldGenerateUsernameWhenItDoesNotExist() {
            // Arrange
            Predicate<String> usernameExists = username -> false;

            // Act
            String result = usernameGenerator.generate(
                    "Andrii",
                    "Kosteniuk",
                    usernameExists
            );

            // Assert
            assertEquals("andrii.kosteniuk", result);
        }

        @Test
        void shouldGenerateUsernameInLowercase() {
            // Arrange
            Predicate<String> usernameExists = username -> false;

            // Act
            String result = usernameGenerator.generate(
                    "ANDRII",
                    "KOSTENIUK",
                    usernameExists
            );

            // Assert
            assertEquals("andrii.kosteniuk", result);
        }

        @Test
        void shouldTrimLeadingAndTrailingWhitespace() {
            // Arrange
            Predicate<String> usernameExists = username -> false;

            // Act
            String result = usernameGenerator.generate(
                    "  Andrii  ",
                    "  Kosteniuk  ",
                    usernameExists
            );

            // Assert
            assertEquals("andrii.kosteniuk", result);
        }

        @ParameterizedTest
        @CsvSource({
                "Andrii, Kosteniuk, andrii.kosteniuk",
                "JAMES, BOND, james.bond",
                "Alina, Bond, alina.bond",
                "michael, born, michael.born",
                "  Anna  ,  White  , anna.white"
        })
        void shouldGenerateCorrectUsernameForDifferentNames(
                String firstName,
                String lastName,
                String expected
        ) {
            // Arrange
            Predicate<String> usernameExists = username -> false;

            // Act
            String result = usernameGenerator.generate(
                    firstName,
                    lastName,
                    usernameExists
            );

            // Assert
            assertEquals(expected, result);
        }

        @Test
        void shouldUseRootLocaleRegardlessOfDefaultLocale() {
            // Arrange
            Locale originalLocale = Locale.getDefault();

            try {
                Locale.setDefault(Locale.forLanguageTag("tr-TR"));

                // Act
                String result = usernameGenerator.generate(
                        "IRA",
                        "MON",
                        username -> false
                );

                // Assert
                assertEquals("ira.mon", result);

            } finally {
                Locale.setDefault(originalLocale);
            }
        }
    }

    @Nested
    class UsernameUniquenessTests {

        @Test
        void shouldAppendSuffixOneWhenBaseUsernameExists() {
            // Arrange
            Set<String> existingUsernames = Set.of(
                    "andrii.kosteniuk"
            );

            // Act
            String result = usernameGenerator.generate(
                    "Andrii",
                    "Kosteniuk",
                    existingUsernames::contains
            );

            // Assert
            assertEquals("andrii.kosteniuk1", result);
        }

        @Test
        void shouldIncrementSuffixUntilUniqueUsernameIsFound() {
            // Arrange
            Set<String> existingUsernames = Set.of(
                    "andrii.kosteniuk",
                    "andrii.kosteniuk1",
                    "andrii.kosteniuk2",
                    "andrii.kosteniuk3"
            );

            // Act
            String result = usernameGenerator.generate(
                    "Andrii",
                    "Kosteniuk",
                    existingUsernames::contains
            );

            // Assert
            assertEquals("andrii.kosteniuk4", result);
        }

        @Test
        void shouldUseFirstAvailableSuffix() {
            // Arrange
            Set<String> existingUsernames = Set.of(
                    "andrii.kosteniuk",
                    "andrii.kosteniuk1",
                    "andrii.kosteniuk3"
            );

            // Act
            String result = usernameGenerator.generate(
                    "Andrii",
                    "Kosteniuk",
                    existingUsernames::contains
            );

            // Assert
            assertEquals("andrii.kosteniuk2", result);
        }

        @Test
        void shouldNotAppendSuffixWhenBaseUsernameIsAvailable() {
            // Arrange
            Set<String> existingUsernames = Set.of(
                    "andrii.kosteniuk1",
                    "andrii.kosteniuk2"
            );

            // Act
            String result = usernameGenerator.generate(
                    "Andrii",
                    "Kosteniuk",
                    existingUsernames::contains
            );

            // Assert
            assertEquals("andrii.kosteniuk", result);
        }

        @Test
        void shouldGenerateUniqueUsernamesForMultipleUsers() {
            // Arrange
            Set<String> existingUsernames = new HashSet<>();

            // Act
            String first = usernameGenerator.generate(
                    "Andrii",
                    "Kosteniuk",
                    existingUsernames::contains
            );
            existingUsernames.add(first);

            String second = usernameGenerator.generate(
                    "Andrii",
                    "Kosteniuk",
                    existingUsernames::contains
            );
            existingUsernames.add(second);

            String third = usernameGenerator.generate(
                    "Andrii",
                    "Kosteniuk",
                    existingUsernames::contains
            );
            existingUsernames.add(third);

            // Assert
            assertAll(
                    () -> assertEquals("andrii.kosteniuk", first),
                    () -> assertEquals("andrii.kosteniuk1", second),
                    () -> assertEquals("andrii.kosteniuk2", third),
                    () -> assertEquals(3, existingUsernames.size())
            );
        }

        @Test
        void shouldCheckUsernameExistenceUntilAvailableCandidateIsFound() {
            // Arrange
            Set<String> checkedUsernames = new HashSet<>();

            Predicate<String> usernameExists = username -> {
                checkedUsernames.add(username);

                return username.equals("andrii.kosteniuk")
                        || username.equals("andrii.kosteniuk1");
            };

            // Act
            String result = usernameGenerator.generate(
                    "Andrii",
                    "Kosteniuk",
                    usernameExists
            );

            // Assert
            assertEquals("andrii.kosteniuk2", result);

            assertEquals(
                    Set.of(
                            "andrii.kosteniuk",
                            "andrii.kosteniuk1",
                            "andrii.kosteniuk2"
                    ),
                    checkedUsernames
            );
        }
    }

    @Nested
    class ValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   ", "\t", "\n"})
        void shouldRejectNullOrBlankFirstName(String firstName) {
            // Arrange
            Predicate<String> usernameExists = username -> false;

            // Act
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> usernameGenerator.generate(
                            firstName,
                            "Kosteniuk",
                            usernameExists
                    )
            );

            // Assert
            assertEquals(
                    "First name and last name must not be blank",
                    exception.getMessage()
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   ", "\t", "\n"})
        void shouldRejectNullOrBlankLastName(String lastName) {
            // Arrange
            Predicate<String> usernameExists = username -> false;

            // Act
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> usernameGenerator.generate(
                            "Andrii",
                            lastName,
                            usernameExists
                    )
            );

            // Assert
            assertEquals(
                    "First name and last name must not be blank",
                    exception.getMessage()
            );
        }

        @Test
        void shouldRejectNullUsernameExistenceChecker() {
            // Act
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> usernameGenerator.generate(
                            "Andrii",
                            "Kosteniuk",
                            null
                    )
            );

            // Assert
            assertEquals(
                    "Username existence checker must not be null",
                    exception.getMessage()
            );
        }
    }
}