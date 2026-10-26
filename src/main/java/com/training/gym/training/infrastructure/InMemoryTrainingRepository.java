package com.training.gym.training.infrastructure;

import com.training.gym.training.domain.Training;
import com.training.gym.training.repository.TrainingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Repository
public class InMemoryTrainingRepository implements TrainingRepository {

    private static final String NOT_NULL_ID_MESSAGE = "Training ID must not be null";
    private Map<Long, Training> storage;

    @Autowired
    public void setStorage(@Qualifier("trainingStorage") Map<Long, Training> storage) {
        this.storage = Objects.requireNonNull(storage);
    }

    @Override
    public Training save(Training entity) {
        Objects.requireNonNull(entity, "Training must not be null");

        Long id = Objects.requireNonNull(
                entity.getId(), NOT_NULL_ID_MESSAGE);

        storage.put(id, entity);
        return entity;
    }

    @Override
    public Optional<Training> findById(Long id) {
        Objects.requireNonNull(id, NOT_NULL_ID_MESSAGE);
        Training training = storage.get(id);
        return Optional.ofNullable(training);
    }

    @Override
    public List<Training> findAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public boolean existsById(Long id) {
        Objects.requireNonNull(id, NOT_NULL_ID_MESSAGE);
        return storage.containsKey(id);
    }

    @Override
    public void deleteById(Long id) {
        Objects.requireNonNull(id, NOT_NULL_ID_MESSAGE);
        storage.remove(id);
    }

    @Override
    public boolean existsByTraineeOrTrainerId(Long id) {
        Objects.requireNonNull(id, NOT_NULL_ID_MESSAGE);
        return storage.values()
                .stream()
                .anyMatch(entity -> {
                    Long traineeId = entity.getTraineeId();

                    Long trainerId = entity.getTrainerId();
                    return (traineeId != null && traineeId.equals(id)) || (trainerId != null && trainerId.equals(id));
                });
    }
}
