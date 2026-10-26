package com.training.gym.common.credentials;

import com.training.gym.common.credentials.generator.UsernameGenerator;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;

@Component
public class UserNameGeneratorImpl implements UsernameGenerator {

    @Override
    public String generate(String firstName, String lastName, Predicate<String> usernameExists) {
        if (firstName == null
                || firstName.isBlank()
                || lastName == null
                || lastName.isBlank()) {
            throw new IllegalArgumentException("First name and last name must not be blank");
        }

        Objects.requireNonNull(usernameExists, "Username existence checker must not be null");

        String baseUsername =
                firstName.trim().toLowerCase(Locale.ROOT)
                        + "."
                        + lastName.trim().toLowerCase(Locale.ROOT);

        String candidate = baseUsername;
        int suffix = 1;

        while (usernameExists.test(candidate)) {
            candidate = baseUsername + suffix++;
        }

        return candidate;
    }
}
