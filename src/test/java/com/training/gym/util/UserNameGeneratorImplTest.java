package com.training.gym.util;

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
                    "James",
                    "Bond",
                    usernameExists
            );

            // Assert
            assertEquals("james.bond", result);
        }

        @Test
        void shouldGenerateUsernameInLowercase() {
            // Arrange
            Predicate<String> usernameExists = username -> false;

            // Act
            String result = usernameGenerator.generate(
                    "James",
                    "Bond",
                    usernameExists
            );

            // Assert
            assertEquals("james.bond", result);
        }

        @Test
        void shouldTrimLeadingAndTrailingWhitespace() {
            // Arrange
            Predicate<String> usernameExists = username -> false;

            // Act
            String result = usernameGenerator.generate(
                    "  James  ",
                    "  Bond  ",
                    usernameExists
            );

            // Assert
            assertEquals("james.bond", result);
        }

        @ParameterizedTest
        @CsvSource({
                "James, Bond, james.bond",
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
                    "james.bond"
            );

            // Act
            String result = usernameGenerator.generate(
                    "James",
                    "Bond",
                    existingUsernames::contains
            );

            // Assert
            assertEquals("james.bond1", result);
        }

        @Test
        void shouldIncrementSuffixUntilUniqueUsernameIsFound() {
            // Arrange
            Set<String> existingUsernames = Set.of(
                    "james.bond",
                    "james.bond1",
                    "james.bond2",
                    "james.bond3"
            );

            // Act
            String result = usernameGenerator.generate(
                    "James",
                    "Bond",
                    existingUsernames::contains
            );

            // Assert
            assertEquals("james.bond4", result);
        }

        @Test
        void shouldUseFirstAvailableSuffix() {
            // Arrange
            Set<String> existingUsernames = Set.of(
                    "james.bond",
                    "james.bond1",
                    "james.bond3"
            );

            // Act
            String result = usernameGenerator.generate(
                    "James",
                    "Bond",
                    existingUsernames::contains
            );

            // Assert
            assertEquals("james.bond2", result);
        }

        @Test
        void shouldNotAppendSuffixWhenBaseUsernameIsAvailable() {
            // Arrange
            Set<String> existingUsernames = Set.of(
                    "james.bond1",
                    "james.bond2"
            );

            // Act
            String result = usernameGenerator.generate(
                    "James",
                    "Bond",
                    existingUsernames::contains
            );

            // Assert
            assertEquals("james.bond", result);
        }

        @Test
        void shouldGenerateUniqueUsernamesForMultipleUsers() {
            // Arrange
            Set<String> existingUsernames = new HashSet<>();

            // Act
            String first = usernameGenerator.generate(
                    "James",
                    "Bond",
                    existingUsernames::contains
            );
            existingUsernames.add(first);

            String second = usernameGenerator.generate(
                    "James",
                    "Bond",
                    existingUsernames::contains
            );
            existingUsernames.add(second);

            String third = usernameGenerator.generate(
                    "James",
                    "Bond",
                    existingUsernames::contains
            );
            existingUsernames.add(third);

            // Assert
            assertAll(
                    () -> assertEquals("james.bond", first),
                    () -> assertEquals("james.bond1", second),
                    () -> assertEquals("james.bond2", third),
                    () -> assertEquals(3, existingUsernames.size())
            );
        }

        @Test
        void shouldCheckUsernameExistenceUntilAvailableCandidateIsFound() {
            // Arrange
            Set<String> checkedUsernames = new HashSet<>();

            Predicate<String> usernameExists = username -> {
                checkedUsernames.add(username);

                return username.equals("james.bond")
                        || username.equals("james.bond1");
            };

            // Act
            String result = usernameGenerator.generate(
                    "James",
                    "Bond",
                    usernameExists
            );

            // Assert
            assertEquals("james.bond2", result);

            assertEquals(
                    Set.of(
                            "james.bond",
                            "james.bond1",
                            "james.bond2"
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
                            "Bond",
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
                            "James",
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
                            "James",
                            "Bond",
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