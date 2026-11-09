package com.training.gym.dao;

import com.training.gym.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaUserRepository implements UserRepository {

    public static final String USERNAME_MUST_NOT_BE_BLANK = "Username must not be blank";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public boolean existsByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(USERNAME_MUST_NOT_BE_BLANK);
        }

        Long count = entityManager.createQuery("""
                SELECT COUNT(u)
                FROM User u
                WHERE u.userName = :username
                """, Long.class)
                .setParameter("username", username)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(USERNAME_MUST_NOT_BE_BLANK);
        }

        return entityManager.createQuery("""
            SELECT u
            FROM User u
            WHERE u.userName = :username
            """, User.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }
}
