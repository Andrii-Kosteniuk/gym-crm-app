package com.training.gym.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class JpaUserRepositoryTest extends RepositoryIntegrationSupport {
    @Autowired
    JpaUserRepository repository;

    @Test
    void findsExistingUserByUsername() {
        var user = user("James", "Bond", "James.Bond");
        em.persist(user);
        reload();
        assertEquals(user.getId(), repository.findByUsername("James.Bond").orElseThrow().getId());
        assertTrue(repository.existsByUsername("James.Bond"));
    }

    @Test
    void returnsEmptyForUnknownUsername() {
        assertTrue(repository.findByUsername("missing").isEmpty());
        assertFalse(repository.existsByUsername("missing"));
    }

    @Test
    void rejectsInvalidUsernames() {
        for (String value : new String[]{"", " "}) {
            assertThrows(IllegalArgumentException.class, () -> repository.findByUsername(value));
            assertThrows(IllegalArgumentException.class, () -> repository.existsByUsername(value));
        }
        assertThrows(IllegalArgumentException.class, () -> repository.findByUsername(null));
        assertThrows(IllegalArgumentException.class, () -> repository.existsByUsername(null));
    }
}
