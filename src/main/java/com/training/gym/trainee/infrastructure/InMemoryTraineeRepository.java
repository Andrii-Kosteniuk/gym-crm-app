package com.training.gym.trainee.infrastructure;

import com.training.gym.trainee.domain.Trainee;
import com.training.gym.trainee.repository.TraineeRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Repository
public class InMemoryTraineeRepository implements TraineeRepository {

    private Map<Long, Trainee> storage;

    @Autowired
    public void setStorage(@Qualifier("traineeStorage") Map<Long, Trainee> storage) {
        this.storage = Objects.requireNonNull(storage);
    }

    @Override
    public Trainee save(Trainee entity) {
        Objects.requireNonNull(entity, "Trainee must not be null");

        Long id = Objects.requireNonNull(
                entity.getId(), "Trainer ID must not be null");

        storage.put(id, entity);
        return entity;
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        Objects.requireNonNull(id, "Trainee ID must not be null");
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Trainee> findAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public boolean existsById(Long id) {
        Objects.requireNonNull(id, "ID must not be null");
        return storage.containsKey(id);
    }

    @Override
    public void deleteById(Long id) {
        Objects.requireNonNull(id, "ID must not be null");
        storage.remove(id);
    }

    @Override
    public boolean existsByUserName(String userName) {
        Objects.requireNonNull(userName, "Username must not be null");
        return storage.values()
                .stream()
                .anyMatch(entity -> entity.getUserName().equals(userName));
    }

    @Override
    public Optional<Trainee> findByUserName(String username) {
        Objects.requireNonNull(username, "Username must not be null");
        return storage.values()
                .stream()
                .filter(trainee -> trainee.getUserName().equals(username))
                .findFirst();
    }
}
