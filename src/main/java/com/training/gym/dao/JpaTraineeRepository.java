package com.training.gym.dao;

import com.training.gym.dto.trainee.TraineeTrainingCriteria;
import com.training.gym.model.Trainee;
import com.training.gym.model.Training;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class JpaTraineeRepository implements TraineeRepository {

    public static final String TRAINEE_MUST_NOT_BE_NULL = "Trainee must not be null";
    public static final String ID_MUST_NOT_BE_NULL = "Trainee ID must not be null";
    public static final String USERNAME_MUST_NOT_BE_BLANK = "Trainee username must not be blank";
    public static final String USERNAME = "username";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Trainee save(Trainee trainee) {
        Objects.requireNonNull(trainee, TRAINEE_MUST_NOT_BE_NULL);

        if (trainee.getId() != null) {
            throw new IllegalArgumentException("Cannot persist a trainee with an existing ID");
        }

        entityManager.persist(trainee);
        return trainee;
    }

    @Override
    public Trainee update(Trainee trainee) {
        Objects.requireNonNull(trainee, TRAINEE_MUST_NOT_BE_NULL);

        if (trainee.getId() == null) {
            throw new IllegalArgumentException("Cannot update a trainee without an ID");
        }

        return entityManager.merge(trainee);
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        Objects.requireNonNull(id, ID_MUST_NOT_BE_NULL);

        return Optional.ofNullable(entityManager.find(Trainee.class, id));
    }

    @Override
    public Optional<Trainee> findByUserName(String username) {
        validateUsername(username);

        return entityManager.createQuery("""
                        SELECT t FROM Trainee t
                        JOIN FETCH t.user u
                        WHERE u.userName = :username
                        """, Trainee.class)
                .setParameter(USERNAME, username)
                .getResultList().stream()
                .findFirst();
    }

    @Override
    public List<Trainee> findAll() {
        return entityManager.createQuery("""
                        SELECT t FROM Trainee t
                        JOIN FETCH t.user
                        """, Trainee.class)
                .getResultList();
    }

    @Override
    public boolean existsById(Long id) {
        Objects.requireNonNull(id, ID_MUST_NOT_BE_NULL);

        Long count = entityManager.createQuery("""
                        SELECT COUNT(t)
                        FROM Trainee t
                        WHERE t.id = :id
                        """, Long.class)
                .setParameter("id", id)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public void delete(Trainee trainee) {
        Objects.requireNonNull(trainee, TRAINEE_MUST_NOT_BE_NULL);

        Trainee managedTrainee = entityManager.contains(trainee)
                ? trainee
                : entityManager.find(Trainee.class, trainee.getId());

        if (managedTrainee == null) {
            throw new EntityNotFoundException("Trainee not found: " + trainee.getId());
        }

        entityManager.remove(managedTrainee);
    }

    @Override
    public void deleteById(Long id) {
        Objects.requireNonNull(id, ID_MUST_NOT_BE_NULL);

        Trainee trainee = findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Trainee not found: " + id));

        delete(trainee);
    }

    @Override
    public void deleteByUsername(String username) {
        validateUsername(username);

        Trainee trainee = findByUserName(username)
                .orElseThrow(() ->
                        new EntityNotFoundException("Trainee not found: " + username));

        delete(trainee);
    }

    @Override
    public void activateTrainee(Trainee trainee) {
        Objects.requireNonNull(trainee, ID_MUST_NOT_BE_NULL);

        Long id = trainee.getId();
        entityManager.createQuery("""
                        UPDATE Trainee t SET t.active = true WHERE t.id = :id
                        """)
                .setParameter("id", id);

    }

    @Override
    public void deactivateTrainee(Trainee trainee) {
        Objects.requireNonNull(trainee, ID_MUST_NOT_BE_NULL);

        Long id = trainee.getId();
        entityManager.createQuery("""
                        UPDATE Trainee t SET t.active = false WHERE t.id = :id
                        """)
                .setParameter("id", id);
    }


    @Override
    public List<Training> findTrainings(String username, TraineeTrainingCriteria criteria) {
        validateUsername(username);
        Objects.requireNonNull(criteria, "Criteria must not be null");

        if (criteria.fromDate() != null
                && criteria.toDate() != null
                && criteria.fromDate().isAfter(criteria.toDate())) {
            throw new IllegalArgumentException("From date must not be after to date");
        }

        StringBuilder criteriaQuery = new StringBuilder("""
                SELECT tr FROM Training tr
                JOIN tr.trainee t
                JOIN t.user u
                JOIN tr.trainer trainer
                JOIN trainer.user trainerUser
                JOIN tr.trainingType tt
                WHERE u.userName = :username
                """);

        if (criteria.fromDate() != null) {
            criteriaQuery.append(" AND tr.trainingDate >= :fromDate");
        }

        if (criteria.toDate() != null) {
            criteriaQuery.append(" AND tr.trainingDate <= :toDate");
        }

        boolean notNullAndNotBlankName = criteria.trainerName() != null
                && !criteria.trainerName().isBlank();


        if (notNullAndNotBlankName) {
            criteriaQuery.append("""
                     AND CONCAT(
                         CONCAT(trainerUser.firstName, ' '),
                         trainerUser.lastName
                     ) = :trainerName
                    """);
        }

        boolean notNullAndNotBlankType = criteria.trainingType() != null
                && !criteria.trainingType().isBlank();

        if (notNullAndNotBlankType) {
            criteriaQuery.append(" AND tt.name = :trainingType");
        }

        criteriaQuery.append(" ORDER BY tr.trainingDate DESC, tr.id DESC");

        var query = entityManager.createQuery(
                criteriaQuery.toString(), Training.class
        );

        query.setParameter(USERNAME, username);

        if (criteria.fromDate() != null) {
            query.setParameter("fromDate", criteria.fromDate());
        }

        if (criteria.toDate() != null) {
            query.setParameter("toDate", criteria.toDate());
        }

        if (notNullAndNotBlankName) {
            query.setParameter("trainerName", criteria.trainerName());
        }

        if (notNullAndNotBlankType) {
            query.setParameter("trainingType", criteria.trainingType());
        }

        return query.getResultList();
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(USERNAME_MUST_NOT_BE_BLANK);
        }
    }
}