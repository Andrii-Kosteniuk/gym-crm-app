package com.training.gym.common.credentials.generator;

import java.util.function.Predicate;

public interface UsernameGenerator {

    String generate(String firstName, String lastName, Predicate<String> usernameExists);
}
