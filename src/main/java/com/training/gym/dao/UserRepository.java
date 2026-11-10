package com.training.gym.dao;

import com.training.gym.model.User;

import java.util.Optional;

public interface UserRepository {

    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);

}
