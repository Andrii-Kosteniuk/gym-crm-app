package com.training.gym.dao;

import com.training.gym.dto.trainee.TraineeTrainingCriteria;
import com.training.gym.dto.trainer.TrainerTrainingCriteria;
import com.training.gym.model.Training;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class JpaTrainingRepository implements TrainingRepository {

    public static final String TRAINING_MUST_NOT_BE_NULL = "Training must not be null";
    public static final String ID_MUST_NOT_BE_NULL = "Training ID must not be null";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Training save(Training training) {
        Objects.requireNonNull(training, TRAINING_MUST_NOT_BE_NULL);

        if (training.getId() != null) {
            throw new IllegalArgumentException("New training must not have an ID");
        }

        entityManager.persist(training);
        return training;
    }

    @Override
    public Optional<Training> findById(Long id) {
        Objects.requireNonNull(id, ID_MUST_NOT_BE_NULL);

        return Optional.ofNullable(entityManager.find(Training.class, id));
    }

    @Override
    public List<Training> findAll() {
        return entityManager.createQuery("""
                SELECT t
                FROM Training t
                ORDER BY t.trainingDate DESC, t.id DESC
                """, Training.class)
                .getResultList();
    }

    @Override
    public boolean existsById(Long id) {
        Objects.requireNonNull(id, ID_MUST_NOT_BE_NULL);

        Long count = entityManager.createQuery("""
                SELECT COUNT(t)
                FROM Training t
                WHERE t.id = :id
                """, Long.class)
                .setParameter("id", id)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public void deleteById(Long id) {
        Objects.requireNonNull(id, ID_MUST_NOT_BE_NULL);

        Training training = entityManager.find(Training.class, id);

        if (training == null) {
            throw new EntityNotFoundException("Training not found: " + id);
        }

        entityManager.remove(training);
    }

    @Override
    public List<Training> findByTraineeUsername(String username, TraineeTrainingCriteria criteria) {

        validateUsername(username);

        Objects.requireNonNull(criteria, "Criteria must not be null");

        validateDateRange(criteria.fromDate(), criteria.toDate());

        StringBuilder criteriaQuery = getCriteriaBuilder(criteria);

        TypedQuery<Training> query = entityManager.createQuery(
                criteriaQuery.toString(), Training.class
        );

        query.setParameter("username", username);

        if (criteria.fromDate() != null) {
            query.setParameter("fromDate", criteria.fromDate());
        }

        if (criteria.toDate() != null) {
            query.setParameter("toDate", criteria.toDate());
        }

        if (hasText(criteria.trainerName())) {
            query.setParameter(
                    "trainerName", criteria.trainerName().trim()
            );
        }

        if (hasText(criteria.trainingType())) {
            query.setParameter(
                    "trainingType", criteria.trainingType().trim()
            );
        }

        return query.getResultList();
    }

    @Override
    public List<Training> findByTrainerUsername(
            String username,
            TrainerTrainingCriteria criteria
    ) {
        validateUsername(username);
        Objects.requireNonNull(criteria, "Criteria must not be null");

        validateDateRange(criteria.fromDate(), criteria.toDate());

        StringBuilder jpql = getCriteriaBuilder(criteria);

        TypedQuery<Training> query = entityManager.createQuery(
                jpql.toString(), Training.class
        );

        query.setParameter("username", username);

        if (criteria.fromDate() != null) {
            query.setParameter("fromDate", criteria.fromDate());
        }

        if (criteria.toDate() != null) {
            query.setParameter("toDate", criteria.toDate());
        }

        if (hasText(criteria.traineeName())) {
            query.setParameter(
                    "traineeName", criteria.traineeName().trim()
            );
        }

        return query.getResultList();
    }

    private StringBuilder getCriteriaBuilder(TrainerTrainingCriteria criteria) {
        StringBuilder jpql = new StringBuilder("""
                SELECT t
                FROM Training t
                JOIN t.trainer trainer
                JOIN trainer.user trainerUser
                JOIN t.trainee trainee
                JOIN trainee.user traineeUser
                WHERE trainerUser.userName = :username
                """);

        if (criteria.fromDate() != null) {
            jpql.append(" AND t.trainingDate >= :fromDate");
        }

        if (criteria.toDate() != null) {
            jpql.append(" AND t.trainingDate <= :toDate");
        }

        if (hasText(criteria.traineeName())) {
            jpql.append("""
                    AND CONCAT(
                        CONCAT(traineeUser.firstName, ' '),
                        traineeUser.lastName
                    ) = :traineeName
                    """);
        }

        jpql.append(" ORDER BY t.trainingDate DESC, t.id DESC");
        return jpql;
    }

    private StringBuilder getCriteriaBuilder(TraineeTrainingCriteria criteria) {
        StringBuilder criteriaQuery = new StringBuilder("""
                SELECT t
                FROM Training t
                JOIN t.trainee trainee
                JOIN trainee.user traineeUser
                JOIN t.trainer trainer
                JOIN trainer.user trainerUser
                JOIN t.trainingType type
                WHERE traineeUser.userName = :username
                """);

        if (criteria.fromDate() != null) {
            criteriaQuery.append(" AND t.trainingDate >= :fromDate");
        }

        if (criteria.toDate() != null) {
            criteriaQuery.append(" AND t.trainingDate <= :toDate");
        }

        if (hasText(criteria.trainerName())) {
            criteriaQuery.append("""
                    AND CONCAT(
                        CONCAT(trainerUser.firstName, ' '),
                        trainerUser.lastName
                    ) = :trainerName
                    """);
        }

        if (hasText(criteria.trainingType())) {
            criteriaQuery.append(" AND type.name = :trainingType");
        }

        criteriaQuery.append(" ORDER BY t.trainingDate DESC, t.id DESC");
        return criteriaQuery;
    }

    private void validateUsername(String username) {
        if (!hasText(username)) {
            throw new IllegalArgumentException("Username must not be blank");
        }
    }

    private void validateDateRange(
            LocalDate fromDate,
            LocalDate toDate
    ) {
        if (fromDate != null && toDate != null
                && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("From date must not be after to date");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}