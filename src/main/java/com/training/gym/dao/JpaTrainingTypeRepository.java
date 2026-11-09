package com.training.gym.dao;

import com.training.gym.model.TrainingType;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class JpaTrainingTypeRepository implements TrainingTypeRepository {
    private static final String NOT_NULL_ID_MESSAGE = "TrainingType ID must not be null";

    private EntityManager entityManager;

    @Override
    public Optional<TrainingType> findById(Long id) {
        Objects.requireNonNull(id, NOT_NULL_ID_MESSAGE);

        return Optional.ofNullable(entityManager.find(TrainingType.class, id));
    }

    @Override
    public List<TrainingType> findAll() {
        return entityManager.createQuery("""
                        SELECT t
                        FROM TrainingType t
                        ORDER BY t.name
                        """, TrainingType.class)
                .getResultList();
    }
}
