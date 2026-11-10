package com.training.gym.validation;

import java.time.LocalDate;

public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static <T> void requireNotNull(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException("%s must not be null".formatted(fieldName));
        }

    }

    public static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("%s must not be blank".formatted(fieldName));
        }
    }

    public static void requireValidDateOfBirth(LocalDate date, LocalDate currentDate) {
        requireNotNull(date, "Date of birth");
        requireNotNull(currentDate, "Current date");

        if (date.isAfter(currentDate)) {
            throw new IllegalArgumentException("Date of birth must not be in the future");
        }
    }

    public static void requirePositiveDuration(Long duration) {
        requireNotNull(duration, "Training duration");

        if (duration == 0 || duration < 0) {
            throw new IllegalArgumentException("Training duration must be positive");
        }
    }

    public static void requirePositiveId(Long id, String fieldName) {
        requireNotNull(id, fieldName);

        if (id <= 0) {
            throw new IllegalArgumentException("%s must be positive".formatted(fieldName));
        }
    }
}