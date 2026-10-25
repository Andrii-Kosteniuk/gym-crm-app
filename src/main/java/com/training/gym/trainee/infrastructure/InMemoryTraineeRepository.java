package com.training.gym.trainee.infrastructure;

import com.training.gym.trainee.domain.Trainee;
import com.training.gym.trainee.repository.TraineeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryTraineeRepository implements TraineeRepository {

    private Map<Long, Trainee> storage;

    @Autowired
    public void setStorage(@Qualifier("traineeStorage") Map<Long, Trainee> storage) {
        this.storage = storage;
    }

    @Override
    public Trainee save(Trainee entity) {
        storage.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Trainee> findAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }

    @Override
    public boolean existsByUserName(String userName) {
        return storage.values().stream().anyMatch(entity -> entity.getUserName().equals(userName));
    }

    @Override
    public Optional<Trainee> findByUserName(String username) {
        return storage.values()
                .stream()
                .filter(trainee -> trainee.getUserName().equals(username))
                .findFirst();
    }
}
