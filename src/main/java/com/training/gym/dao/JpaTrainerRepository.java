package com.training.gym.dao;

import com.training.gym.dto.trainer.TrainerTrainingCriteria;
import com.training.gym.model.Trainer;
import com.training.gym.model.Training;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class JpaTrainerRepository implements TrainerRepository {

    private static final String NOT_NULL_ID_MESSAGE = "Trainer ID must not be null";
    public static final String TRAINER_MUST_NOT_BE_NULL = "Trainer must not be null";
    private static final String NOT_BLANK_USERNAME_MESSAGE = "Trainer username must not be blank";
    public static final String USERNAME = "username";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Trainer save(Trainer trainer) {
        Objects.requireNonNull(trainer, TRAINER_MUST_NOT_BE_NULL);

        if (trainer.getId() != null) {
            throw new IllegalArgumentException("Cannot persist a Trainer with an existing ID");
        }

        entityManager.persist(trainer);

        return trainer;
    }

    @Override
    public Trainer update(Trainer trainer) {
        Objects.requireNonNull(trainer, TRAINER_MUST_NOT_BE_NULL);

        if (trainer.getId() == null) {
            throw new IllegalArgumentException("Cannot update Trainer without an ID");
        }

        if (entityManager.find(Trainer.class, trainer.getId()) == null) {
            throw new EntityNotFoundException("Trainer not found with ID: " + trainer.getId());
        }

        return entityManager.merge(trainer);
    }

    @Override
    public void deleteById(Long id) {
        Trainer trainer = findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + id));

        delete(trainer);
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        Objects.requireNonNull(id, NOT_NULL_ID_MESSAGE);

        return Optional.ofNullable(entityManager.find(Trainer.class, id));
    }

    @Override
    public Optional<Trainer> findByUserName(String username) {
        validateUsername(username);

        return entityManager.createQuery("""
                        SELECT t
                        FROM Trainer t
                        JOIN FETCH t.user u
                        WHERE u.userName = :username
                        """, Trainer.class)
                .setParameter(USERNAME, username)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public void delete(Trainer trainer) {
        Objects.requireNonNull(trainer, TRAINER_MUST_NOT_BE_NULL);

        Trainer managedTrainer = entityManager.contains(trainer)
                ? trainer
                : entityManager.find(Trainer.class, trainer.getId());

        if (managedTrainer == null) {
            throw new EntityNotFoundException("Trainer not found: " + trainer.getId());
        }

        entityManager.remove(managedTrainer);
    }

    @Override
    public void deleteByUsername(String username) {
        Trainer trainee = findByUserName(username)
                .orElseThrow(() ->
                        new EntityNotFoundException("Trainee not found: " + username));

        delete(trainee);
    }

    @Override
    public void activateTrainer(Trainer trainer) {
        Objects.requireNonNull(trainer, TRAINER_MUST_NOT_BE_NULL);
        changeActive(trainer.getId(), true);
    }

    @Override
    public void deactivateTrainer(Trainer trainer) {
        Objects.requireNonNull(trainer, TRAINER_MUST_NOT_BE_NULL);
        changeActive(trainer.getId(), false);
    }

    @Override
    public List<Trainer> findAll() {
        return entityManager.createQuery("""
                        SELECT t
                        FROM Trainer t
                        JOIN FETCH t.user
                        ORDER BY t.id
                        """, Trainer.class)
                .getResultList();
    }

    @Override
    public boolean existsById(Long id) {
        Objects.requireNonNull(id, NOT_NULL_ID_MESSAGE);

        Long count = entityManager.createQuery("""
                        SELECT COUNT(t)
                        FROM Trainer t
                        WHERE t.id = :id
                        """, Long.class)
                .setParameter("id", id)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public List<Training> findTrainings(String username, TrainerTrainingCriteria criteria) {
        validateUsername(username);
        Objects.requireNonNull(criteria, "Training criteria must not be null");

        validateDateRange(criteria);

        StringBuilder criteriaQuery = getCriteriaQuery(criteria);

        TypedQuery<Training> query = entityManager.createQuery(
                criteriaQuery.toString(),
                Training.class
        );

        query.setParameter(USERNAME, username);

        if (criteria.fromDate() != null) {
            query.setParameter("fromDate", criteria.fromDate());
        }

        if (criteria.toDate() != null) {
            query.setParameter("toDate", criteria.toDate());
        }

        if (hasText(criteria.traineeName())) {
            query.setParameter("traineeName", criteria.traineeName().trim());
        }

        return query.getResultList();
    }

    @Override
    public List<Trainer> findUnassignedTrainers(String username) {
        return entityManager.createQuery("""
                                   SELECT t
                        FROM Trainer t
                        JOIN FETCH t.user
                        WHERE NOT EXISTS (
                            SELECT 1
                            FROM Trainee trainee
                            JOIN trainee.trainers assignedTrainer
                            WHERE trainee.user.userName = :username
                              AND assignedTrainer = t
                        )
                        ORDER BY t.user.lastName, t.user.firstName
                        """, Trainer.class)
                .setParameter(USERNAME, username)
                .getResultList();
    }

    private StringBuilder getCriteriaQuery(TrainerTrainingCriteria criteria) {
        StringBuilder criteriaQuery = new StringBuilder("""
                SELECT tr
                FROM Training tr
                JOIN tr.trainer trainer
                JOIN trainer.user trainerUser
                JOIN tr.trainee trainee
                JOIN trainee.user traineeUser
                WHERE trainerUser.userName = :username
                """);

        if (criteria.fromDate() != null) {
            criteriaQuery.append("""
                    AND tr.trainingDate >= :fromDate
                    """);
        }

        if (criteria.toDate() != null) {
            criteriaQuery.append("""
                    AND tr.trainingDate <= :toDate
                    """);
        }

        if (hasText(criteria.traineeName())) {
            criteriaQuery.append("""
                    AND CONCAT(
                        CONCAT(traineeUser.firstName, ' '),
                        traineeUser.lastName
                    ) = :traineeName
                    """);
        }

        criteriaQuery.append("""
                ORDER BY tr.trainingDate DESC, tr.id DESC
                """);
        return criteriaQuery;
    }

    private void validateUsername(String username) {
        if (!hasText(username)) {
            throw new IllegalArgumentException(NOT_BLANK_USERNAME_MESSAGE);
        }
    }

    private void validateDateRange(TrainerTrainingCriteria criteria) {
        if (criteria.fromDate() != null
                && criteria.toDate() != null
                && criteria.fromDate().isAfter(criteria.toDate())) {

            throw new IllegalArgumentException("From date must not be after to date");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private void changeActive(Long id, boolean active) {
        Objects.requireNonNull(id, NOT_NULL_ID_MESSAGE);

        entityManager.createQuery("""
                        UPDATE User u SET u.active = :active
                        WHERE u.id = (SELECT t.user.id FROM Trainer t WHERE t.id = :id)
                        """)
                .setParameter("active", active)
                .setParameter("id", id)
                .executeUpdate();

    }

}