package com.training.gym.common.credentials;

import com.training.gym.common.credentials.generator.PasswordGenerator;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class PasswordGeneratorImpl implements PasswordGenerator {

    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                    + "abcdefghijklmnopqrstuvwxyz"
                    + "0123456789";

    private static final int PASSWORD_LENGTH = 10;

    private final SecureRandom random = new SecureRandom();

    @Override
    public String generate() {
        StringBuilder password =
                new StringBuilder(PASSWORD_LENGTH);

        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(index));
        }
        return password.toString();
    }
}
